package duke;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests SQLite persistence, ordering, task conversion, and legacy-file migration. */
class SqliteTaskRepositoryTest {
    @TempDir
    Path temporaryDirectory;

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

    /** Returns task descriptions for concise order assertions. */
    private List<String> descriptions(List<Task> tasks) {
        return tasks.stream().map(Task::getDescription).toList();
    }
}
