package duke;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests SQLite persistence, ordering, task conversion, and legacy-file migration. */
class SqliteTaskRepositoryTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void loadTasks_invalidLegacyFile_canRetryAfterCorrection() throws Exception {
        Path databasePath = temporaryDirectory.resolve("retry.db");
        Path legacyPath = temporaryDirectory.resolve("wangsa.txt");
        Files.writeString(legacyPath, "T | 2 | recoverable task\n");

        assertThrows(StorageException.class, () -> new SqliteTaskRepository(databasePath, legacyPath).loadTasks());
        assertFalse(Files.exists(databasePath));

        Files.writeString(legacyPath, "T | 0 | recoverable task\n");
        assertEquals(List.of("recoverable task"),
                descriptions(new SqliteTaskRepository(databasePath, legacyPath).loadTasks()));
    }

    @Test
    void insertAndDelete_multipleShiftedRows_preservesUniquePositions() throws Exception {
        SqliteTaskRepository repository = new SqliteTaskRepository(temporaryDirectory.resolve("ordered.db"), null);
        repository.saveTasks(List.of(new Todo("first"), new Todo("second"), new Todo("third")));

        repository.insertTask(new Todo("new first"), 0);
        repository.insertTask(new Todo("middle"), 2);
        assertEquals(List.of("new first", "first", "middle", "second", "third"),
                descriptions(repository.loadTasks()));

        repository.deleteTask(0);
        repository.deleteTask(1);
        assertEquals(List.of("first", "second", "third"), descriptions(repository.loadTasks()));
        assertThrows(StorageException.class, () -> repository.insertTask(new Todo("invalid"), 5));
        assertEquals(List.of("first", "second", "third"), descriptions(repository.loadTasks()));
    }

    @Test
    void saveAndLoadTasks_roundTripsAllTaskTypesAndOrder() throws Exception {
        Path databasePath = temporaryDirectory.resolve("wangsa.db");
        SqliteTaskRepository repository = new SqliteTaskRepository(databasePath, null);
        Task todo = new Todo("read book");
        todo.markAsDone();
        Task deadline = new Deadline("return book", LocalDate.of(2026, 9, 20));
        Task event = new Event("project meeting", "2pm", "4pm");

        repository.saveTasks(List.of(todo, deadline, event));

        List<Task> loadedTasks = new SqliteTaskRepository(databasePath, null).loadTasks();
        assertTrue(Files.exists(databasePath));
        assertEquals(List.of(todo.toString(), deadline.toString(), event.toString()),
                loadedTasks.stream().map(Task::toString).toList());
        assertTrue(loadedTasks.get(0).isDone());
        assertEquals(LocalDate.of(2026, 9, 20), ((Deadline) loadedTasks.get(1)).getBy());
        assertEquals("4pm", ((Event) loadedTasks.get(2)).getTo());
    }

    @Test
    void loadTasks_whenDatabaseIsNew_importsLegacyTextOnlyOnce() throws Exception {
        Path databasePath = temporaryDirectory.resolve("wangsa.db");
        Path legacyFilePath = temporaryDirectory.resolve("wangsa.txt");
        new Storage(legacyFilePath).saveTasks(List.of(new Todo("migrated task")));
        SqliteTaskRepository repository = new SqliteTaskRepository(databasePath, legacyFilePath);

        assertEquals(List.of("migrated task"), descriptions(repository.loadTasks()));

        repository.saveTasks(List.of());

        assertTrue(repository.loadTasks().isEmpty());
        assertTrue(new SqliteTaskRepository(databasePath, legacyFilePath).loadTasks().isEmpty());
    }

    @Test
    void focusedOperations_insertUpdateAndDeleteWithoutReplacingSnapshot() throws Exception {
        Path databasePath = temporaryDirectory.resolve("wangsa.db");
        SqliteTaskRepository repository = new SqliteTaskRepository(databasePath, null);
        repository.saveTasks(List.of(new Todo("first"), new Todo("last")));

        repository.insertTask(new Todo("middle"), 1);
        repository.updateTask(new Deadline("updated", LocalDate.of(2026, 9, 20)), 0);
        repository.deleteTask(1);

        assertEquals(List.of("updated", "last"), descriptions(repository.loadTasks()));
        assertEquals("[D][ ] updated (by: Sep 20 2026)", repository.loadTasks().get(0).toString());
    }

    @Test
    void saveTasks_whenInsertFails_keepsThePreviousSnapshot() throws Exception {
        Path databasePath = temporaryDirectory.resolve("wangsa.db");
        SqliteTaskRepository repository = new SqliteTaskRepository(databasePath, null);
        repository.saveTasks(List.of(new Todo("original")));

        assertThrows(StorageException.class, () -> repository.saveTasks(List.of(new Task(null))));

        assertEquals(List.of("original"), descriptions(repository.loadTasks()));
    }

    @Test
    void schema_enforcesUniquePositionsAndTaskSpecificFields() throws Exception {
        Path databasePath = temporaryDirectory.resolve("wangsa.db");
        SqliteTaskRepository repository = new SqliteTaskRepository(databasePath, null);
        repository.saveTasks(List.of(new Todo("existing")));

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
                PreparedStatement duplicatePosition = connection.prepareStatement(
                        "INSERT INTO tasks (task_type, is_done, description, position) "
                                + "VALUES ('T', 0, 'duplicate', 0)")) {
            assertThrows(SQLException.class, duplicatePosition::executeUpdate);
        }

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
                PreparedStatement mismatchedFields = connection.prepareStatement(
                        "INSERT INTO tasks (task_type, is_done, description, deadline, position) "
                                + "VALUES ('T', 0, 'invalid', '2026-09-20', 1)")) {
            assertThrows(SQLException.class, mismatchedFields::executeUpdate);
        }
    }

    @Test
    void loadTasks_upgradesExistingSchema() throws Exception {
        Path databasePath = temporaryDirectory.resolve("wangsa.db");
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
                Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE tasks ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "task_type TEXT NOT NULL CHECK (task_type IN ('T', 'D', 'E')), "
                    + "is_done INTEGER NOT NULL CHECK (is_done IN (0, 1)), "
                    + "description TEXT NOT NULL, deadline TEXT, event_from TEXT, event_to TEXT, "
                    + "position INTEGER NOT NULL CHECK (position >= 0))");
            statement.executeUpdate("INSERT INTO tasks (task_type, is_done, description, position) "
                    + "VALUES ('T', 0, 'existing task', 0)");
        }

        SqliteTaskRepository repository = new SqliteTaskRepository(databasePath, null);
        assertEquals(List.of("existing task"), descriptions(repository.loadTasks()));
    }

    @Test
    void loadTasks_rejectsInconsistentRowsFromExistingSchema() throws Exception {
        Path databasePath = temporaryDirectory.resolve("wangsa.db");
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
                Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE tasks ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, task_type TEXT NOT NULL, is_done INTEGER NOT NULL, "
                    + "description TEXT NOT NULL, deadline TEXT, event_from TEXT, event_to TEXT, "
                    + "position INTEGER NOT NULL)");
            statement.executeUpdate("INSERT INTO tasks (task_type, is_done, description, deadline, position) "
                    + "VALUES ('T', 0, 'invalid', '2026-09-20', 0)");
        }

        StorageException exception = assertThrows(StorageException.class,
                () -> new SqliteTaskRepository(databasePath, null).loadTasks());
        assertTrue(exception.getMessage().contains("todo deadline must be empty"));
    }

    /** Returns task descriptions for concise order assertions. */
    private List<String> descriptions(List<Task> tasks) {
        return tasks.stream().map(Task::getDescription).toList();
    }
}
