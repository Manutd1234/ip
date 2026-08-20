package duke;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/** Tests task-list ordering, mutations, validation, and capacity handling. */
class TaskListTest {
    @Test
    void addMarkUnmarkAndDelete_preserveTaskStateAndOrder() throws WangsaException {
        TaskList tasks = new TaskList();
        Task todo = new Todo("read book");
        Task deadline = new Deadline("return book", java.time.LocalDate.of(2019, 12, 2));

        assertSame(todo, tasks.add(todo));
        assertSame(deadline, tasks.add(deadline));
        assertEquals(List.of(todo, deadline), tasks.getTasks());

        assertSame(deadline, tasks.mark(2));
        assertTrue(deadline.isDone());
        assertSame(deadline, tasks.unmark(2));
        assertFalse(deadline.isDone());

        assertSame(todo, tasks.delete(1));
        assertEquals(1, tasks.size());
        assertEquals(List.of(deadline), tasks.getTasks());
    }

    @Test
    void invalidTaskNumbers_throwWithoutChangingTheList() throws WangsaException {
        TaskList tasks = new TaskList();
        Task task = new Todo("read book");
        tasks.add(task);

        assertThrows(WangsaException.class, () -> tasks.mark(0));
        assertThrows(WangsaException.class, () -> tasks.unmark(2));
        assertThrows(WangsaException.class, () -> tasks.delete(-1));
        assertEquals(List.of(task), tasks.getTasks());
        assertFalse(task.isDone());
    }

    @Test
    void addingMoreThanOneHundredTasks_throwsAndKeepsCapacity() throws WangsaException {
        TaskList tasks = new TaskList();
        for (int i = 0; i < 100; i++) {
            tasks.add(new Todo("task " + i));
        }

        assertThrows(WangsaException.class, () -> tasks.add(new Todo("overflow")));
        assertEquals(100, tasks.size());
    }
}
