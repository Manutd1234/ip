package duke.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;

import duke.Deadline;
import duke.Task;
import duke.TaskMatch;
import duke.Todo;
import org.junit.jupiter.api.Test;

/** Tests the pure text formatting used by the JavaFX conversation. */
class TaskFormatterTest {
    @Test
    void renderMatches_nonFirstResult_keepsOriginalNumberAndSearchHeading() {
        assertEquals("Here are your matching quests:" + System.lineSeparator() + "3. [T][ ] read book",
                TaskFormatter.renderMatches(List.of(new TaskMatch(3, new Todo("read book")))));
        assertEquals("No matching quests found. Try a different keyword.", TaskFormatter.renderMatches(List.of()));
    }

    @Test
    void renderTasks_formatsEmptyAndNumberedLists() {
        assertEquals("Your quest log is empty.\nTry `todo read a book` to add your first task.",
                TaskFormatter.renderTasks(List.of()));

        List<Task> tasks = List.of(new Todo("read book"), new Deadline("submit report", LocalDate.of(2026, 9, 20)));
        assertEquals("Here's your current quest log:" + System.lineSeparator()
                        + "1. [T][ ] read book" + System.lineSeparator()
                        + "2. [D][ ] submit report (by: Sep 20 2026)", TaskFormatter.renderTasks(tasks));
    }

    @Test
    void formatProgressSummary_andHeaderStats_reportCompletion() {
        Task completed = new Todo("done");
        completed.markAsDone();
        Task pending = new Todo("next");
        List<Task> tasks = List.of(completed, pending);

        assertEquals("1 of 2 quests complete. One step at a time.", TaskFormatter.formatProgressSummary(tasks));
        assertEquals("2 QUESTS  ·  1 DONE", TaskFormatter.formatHeaderStats(tasks));
        assertEquals("You've finished your quest. Nice work, trainer!",
                TaskFormatter.formatProgressSummary(List.of(completed)));
        assertEquals("0 of 1 quest complete. One step at a time.",
                TaskFormatter.formatProgressSummary(List.of(pending)));
        assertEquals("No quests in your log. Add one whenever you're ready.",
                TaskFormatter.formatProgressSummary(List.of()));
    }
}
