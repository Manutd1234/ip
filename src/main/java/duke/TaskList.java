package duke;

import java.util.ArrayList;
import java.util.List;

/**
 * Owns Wangsa's ordered task collection and the operations that change it.
 */
public class TaskList {
    private static final int MAX_TASKS = 100;

    private final List<Task> tasks;

    /** Creates an empty task list. */
    public TaskList() {
        this.tasks = new ArrayList<>();
    }

    /**
     * Creates a task list containing the supplied saved tasks.
     *
     * @param savedTasks tasks loaded from storage
     * @throws WangsaException if the saved list exceeds Wangsa's capacity
     */
    public TaskList(List<Task> savedTasks) throws WangsaException {
        if (savedTasks.size() > MAX_TASKS) {
            throw new WangsaException("OOPS!!! The saved task list contains more than "
                    + MAX_TASKS + " tasks.");
        }
        this.tasks = new ArrayList<>(savedTasks);
    }

    /** Returns the number of tasks currently stored. */
    public int size() {
        return tasks.size();
    }

    /** Returns an unmodifiable snapshot of the tasks in their current order. */
    public List<Task> getTasks() {
        return List.copyOf(tasks);
    }

    /** Adds and returns a task, provided the list still has capacity. */
    public Task add(Task task) throws WangsaException {
        if (tasks.size() >= MAX_TASKS) {
            throw new WangsaException("OOPS!!! Your task list is full (maximum 100 tasks).");
        }
        tasks.add(task);
        return task;
    }

    /** Marks and returns the numbered task as done. */
    public Task mark(int taskNumber) throws WangsaException {
        Task task = getTask(taskNumber);
        task.markAsDone();
        return task;
    }

    /** Marks and returns the numbered task as not done. */
    public Task unmark(int taskNumber) throws WangsaException {
        Task task = getTask(taskNumber);
        task.markAsNotDone();
        return task;
    }

    /** Deletes and returns the numbered task. */
    public Task delete(int taskNumber) throws WangsaException {
        getTask(taskNumber);
        return tasks.remove(taskNumber - 1);
    }

    /** Resolves a one-based task number or reports that it is outside the list. */
    private Task getTask(int taskNumber) throws WangsaException {
        if (taskNumber < 1 || taskNumber > tasks.size()) {
            throw new WangsaException("OOPS!!! Task number must be between 1 and "
                    + tasks.size() + ".");
        }
        return tasks.get(taskNumber - 1);
    }
}
