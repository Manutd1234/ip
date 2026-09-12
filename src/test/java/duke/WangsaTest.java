package duke;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests console commands through parsing, presentation, and persistence. */
class WangsaTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void run_invalidSavedData_stopsBeforeProcessingCommands() throws Exception {
        Path file = temporaryDirectory.resolve("invalid.txt");
        String originalData = "T | 2 | keep this record\n";
        Files.writeString(file, originalData);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Ui ui = new Ui(new ByteArrayInputStream("sort\ntodo replace\n".getBytes(StandardCharsets.UTF_8)),
                new PrintStream(output, true, StandardCharsets.UTF_8));

        new Wangsa(new Storage(file), new Parser(), ui).run();

        assertEquals(originalData, Files.readString(file));
        assertTrue(output.toString(StandardCharsets.UTF_8).contains("status must be 0 or 1"));
        assertFalse(output.toString(StandardCharsets.UTF_8).contains("I've added this task"));
    }

    @Test
    void findThenMark_nonFirstMatch_updatesTheDisplayedTask() throws Exception {
        Storage repository = new Storage(temporaryDirectory.resolve("tasks.txt"));
        repository.saveTasks(List.of(new Todo("meeting"), new Todo("read book")));
        String commands = "  find book  \nmark 2\nfind missing\nbye\n";
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Ui ui = new Ui(new ByteArrayInputStream(commands.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(output, true, StandardCharsets.UTF_8));

        new Wangsa(repository, new Parser(), ui).run();

        assertTrue(output.toString(StandardCharsets.UTF_8).contains("2.[T][ ] read book"));
        assertTrue(output.toString(StandardCharsets.UTF_8).contains("No matching tasks found."));
        assertFalse(repository.loadTasks().get(0).isDone());
        assertTrue(repository.loadTasks().get(1).isDone());
    }
}
