package duke;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** Tests durable task round-trips, first-run behavior, and corrupted data handling. */
class StorageTest {
    @TempDir
    Path temporaryDirectory;

    @ParameterizedTest
    @CsvSource(delimiter = ';', value = {
        "T | 0;not enough fields",
        "T | 2 | task;status must be 0 or 1",
        "T | 0 | ;task description cannot be empty",
        "T | 0 | task | extra;unexpected number of fields",
        "D | 0 | task | ;deadline value cannot be empty",
        "E | 0 | task | start | ;event start and end values cannot be empty",
        "X | 0 | task;unknown task type"
    })
    void loadTasks_invalidFields_reportsReasonAndLine(String line, String reason) throws Exception {
        Path file = temporaryDirectory.resolve("invalid.txt");
        Files.writeString(file, "T | 0 | valid\n" + line + "\n");

        StorageException exception = assertThrows(StorageException.class, new Storage(file)::loadTasks);

        assertTrue(exception.getMessage().contains("line 2: " + reason));
    }

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
