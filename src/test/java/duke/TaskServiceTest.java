package duke;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests that shared task operations coordinate parsing, mutation, and persistence. */
class TaskServiceTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void taskOperations_updateAndPersistSharedTaskState() throws Exception {
        Storage storage = new Storage(temporaryDirectory.resolve("tasks.txt"));
        TaskService service = new TaskService(storage, new Parser());

        Task added = service.add("todo read book");
        assertEquals("read book", added.getDescription());

        service.mark("mark 1");
        assertTrue(service.getTasks().get(0).isDone());

        service.unmark("unmark 1");
        assertFalse(service.getTasks().get(0).isDone());

        service.delete("delete 1");
        assertTrue(service.getTasks().isEmpty());
        assertTrue(new TaskService(storage, new Parser()).getTasks().isEmpty());
    }
}
