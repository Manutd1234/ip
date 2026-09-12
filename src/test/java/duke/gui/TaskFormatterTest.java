package duke.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import duke.Deadline;
import duke.Task;
import duke.TaskMatch;
import duke.Todo;

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
        assertEquals("Your quest log is clear.\nUse `todo DESCRIPTION` below to catch a new quest.",
                TaskFormatter.renderTasks(List.of()));

        List<Task> tasks = List.of(new Todo("read book"), new Deadline("submit report", LocalDate.of(2026, 9, 20)));
        assertEquals("Here's your current quest log:" + System.lineSeparator()
                        + "1. [T][ ] read book" + System.lineSeparator()
                        + "2. [D][ ] submit report (by: Sep 20 2026)", TaskFormatter.renderTasks(tasks));
    }

    @Test
    void progressSummary_andHeaderStats_reportCompletion() {
        Task completed = new Todo("done");
        completed.markAsDone();
        Task pending = new Todo("next");
        List<Task> tasks = List.of(completed, pending);

        assertEquals("1 of 2 quests complete. Keep the streak going!", TaskFormatter.progressSummary(tasks));
        assertEquals("2 QUESTS  ·  1 DONE", TaskFormatter.headerStats(tasks));
        assertEquals("All 1 quests complete — 1/1! Great run, trainer.",
                TaskFormatter.progressSummary(List.of(completed)));
    }
}
