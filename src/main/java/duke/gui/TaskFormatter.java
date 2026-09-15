package duke.gui;

import java.util.List;

import duke.Task;
import duke.TaskMatch;

/**
 * Formats task snapshots and progress summaries for the desktop conversation.
 */
public final class TaskFormatter {
    private TaskFormatter() {
    }

    /**
     * Formats search results with actionable task numbers from the full list.
     *
     * @param matches Search results in full-list order.
     * @return Matching tasks or an explicit no-match message.
     */
    public static String renderMatches(List<TaskMatch> matches) {
        if (matches.isEmpty()) {
            return "No matching quests found. Try a different keyword.";
        }
        StringBuilder result = new StringBuilder("Here are your matching quests:");
        for (TaskMatch match : matches) {
            result.append(System.lineSeparator()).append(match.taskNumber()).append(". ").append(match.task());
        }
        return result.toString();
    }

    /**
     * Formats tasks with the one-based numbering used by the command interface.
     *
     * @param tasks Tasks to display.
     * @return A conversation-ready task list.
     */
    public static String renderTasks(List<Task> tasks) {
        if (tasks.isEmpty()) {
            return "Your quest log is empty.\nTry `todo read a book` to add your first task.";
        }

        StringBuilder result = new StringBuilder("Here's your current quest log:").append(System.lineSeparator());
        for (int i = 0; i < tasks.size(); i++) {
            result.append(i + 1).append(". ").append(tasks.get(i));
            if (i < tasks.size() - 1) {
                result.append(System.lineSeparator());
            }
        }
        return result.toString();
    }

    /**
     * Summarizes completed and total tasks for confirmations.
     *
     * @param tasks Tasks whose progress should be summarized.
     * @return A concise progress summary.
     */
    public static String formatProgressSummary(List<Task> tasks) {
        int completed = (int) tasks.stream().filter(Task::isDone).count();
        int total = tasks.size();
        if (total == 0) {
            return "No quests in your log. Add one whenever you're ready.";
        }
        String questLabel = total == 1 ? "quest" : "quests";
        if (completed == total) {
            return "You've finished " + (total == 1 ? "your quest" : "all " + total + " quests")
                    + ". Nice work, trainer!";
        }
        return completed + " of " + total + " " + questLabel + " complete. One step at a time.";
    }

    /**
     * Returns the compact task count shown in the application header.
     *
     * @param tasks Tasks whose count should be displayed.
     * @return A compact count of total and completed tasks.
     */
    public static String formatHeaderStats(List<Task> tasks) {
        long completed = tasks.stream().filter(Task::isDone).count();
        return tasks.size() + " QUEST" + (tasks.size() == 1 ? "" : "S") + "  ·  " + completed + " DONE";
    }
}
