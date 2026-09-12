package duke.gui;

import java.util.List;

import duke.Task;
import duke.TaskMatch;

/** Formats task snapshots and progress summaries for the desktop conversation. */
public final class TaskFormatter {
    private TaskFormatter() {
    }

    /**
     * Formats search results with actionable task numbers from the full list.
     *
     * @param matches search results in full-list order
     * @return matching tasks or an explicit no-match message
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
     * @param tasks tasks to display
     * @return a conversation-ready task list
     */
    public static String renderTasks(List<Task> tasks) {
        if (tasks.isEmpty()) {
            return "Your quest log is clear.\nUse `todo DESCRIPTION` below to catch a new quest.";
        }

        StringBuilder result = new StringBuilder("Here's your current quest log:\n");
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
     * @param tasks tasks whose progress should be summarized
     * @return a concise progress summary
     */
    public static String progressSummary(List<Task> tasks) {
        int completed = (int) tasks.stream().filter(Task::isDone).count();
        int total = tasks.size();
        if (total > 0 && completed == total) {
            return "All " + total + " quests complete — " + completed + "/" + total + "! Great run, trainer.";
        }
        return completed + " of " + total + " quests complete. Keep the streak going!";
    }

    /**
     * Returns the compact task count shown in the application header.
     *
     * @param tasks tasks whose count should be displayed
     * @return a compact count of total and completed tasks
     */
    public static String headerStats(List<Task> tasks) {
        long completed = tasks.stream().filter(Task::isDone).count();
        return tasks.size() + " QUEST" + (tasks.size() == 1 ? "" : "S") + "  ·  " + completed + " DONE";
    }
}
