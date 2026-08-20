package duke;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests durable task round-trips, first-run behavior, and corrupted data handling. */
class StorageTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void saveAndLoadTasks_roundTripsTypesStatusesAndEscapedText() throws Exception {
        Path file = temporaryDirectory.resolve("data/tasks.txt");
        Storage storage = new Storage(file);
        Task todo = new Todo("read | book\\notes");
        todo.markAsDone();
        Task deadline = new Deadline("return book", LocalDate.of(2019, 12, 2));
        Task event = new Event("meeting", "2pm", "4pm");

        storage.saveTasks(List.of(todo, deadline, event));

        assertTrue(Files.exists(file));
        assertTrue(Files.readString(file).contains("D | 0 | return book | 2019-12-02"));
        List<Task> loaded = storage.loadTasks();
        assertEquals(3, loaded.size());
        assertTrue(loaded.get(0).isDone());
        assertEquals("read | book\\notes", loaded.get(0).getDescription());
        assertEquals(LocalDate.of(2019, 12, 2), ((Deadline) loaded.get(1)).getBy());
        assertEquals("4pm", ((Event) loaded.get(2)).getTo());
    }

    @Test
    void loadTasks_whenFileIsMissing_returnsEmptyList() throws Exception {
        Storage storage = new Storage(temporaryDirectory.resolve("missing/tasks.txt"));

        assertTrue(storage.loadTasks().isEmpty());
        assertFalse(Files.exists(temporaryDirectory.resolve("missing")));
    }

    @Test
    void loadTasks_whenDateIsCorrupted_reportsTheLineNumber() throws Exception {
        Path file = temporaryDirectory.resolve("tasks.txt");
        Files.writeString(file, "D | 0 | return book | not-a-date\n");

        StorageException exception = assertThrows(StorageException.class, new Storage(file)::loadTasks);
        assertTrue(exception.getMessage().contains("line 1"));
        assertTrue(exception.getMessage().contains("yyyy-MM-dd"));
    }
}
