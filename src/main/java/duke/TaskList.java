package duke;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Owns Wangsa's ordered task collection and the operations that change it.
 */
public class TaskList {
    private static final int MAX_TASKS = 100;

    private final List<Task> tasks;

    /** Creates an empty task list. */
    public TaskList() {
        this.tasks = new ArrayList<>();
        assertInvariant();
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
        assertInvariant();
    }

    /** Returns the number of tasks currently stored.
     * @return the task count
     */
    public int size() {
        return tasks.size();
    }

    /** Returns an unmodifiable snapshot of tasks in their current order.
     * @return an immutable task snapshot
     */
    public List<Task> getTasks() {
        return List.copyOf(tasks);
    }

    /**
     * Adds and returns a task, provided the list still has capacity.
     * @param task task to add
     * @return the added task
     * @throws WangsaException if the list is full
     */
    public Task add(Task task) throws WangsaException {
        assert task != null : "task must not be null";
        if (tasks.size() >= MAX_TASKS) {
            throw new WangsaException("OOPS!!! Your task list is full (maximum 100 tasks).");
        }
        tasks.add(task);
        assertInvariant();
        return task;
    }

    /**
     * Adds several tasks as one capacity-checked operation.
     *
     * @param newTasks tasks to add
     * @throws WangsaException if the combined list would exceed capacity
     */
    public void addAll(Task... newTasks) throws WangsaException {
        assert newTasks != null : "newTasks must not be null";
        if (newTasks.length > MAX_TASKS - tasks.size()) {
            throw new WangsaException("OOPS!!! Your task list is full (maximum 100 tasks).");
        }
        tasks.addAll(Arrays.asList(newTasks));
        assertInvariant();
    }

    /**
     * Marks and returns the numbered task as done.
     * @param taskNumber one-based task number
     * @return the updated task
     * @throws WangsaException if the number is invalid
     */
    public Task mark(int taskNumber) throws WangsaException {
        Task task = getTask(taskNumber);
        task.markAsDone();
        return task;
    }

    /**
     * Marks and returns the numbered task as not done.
     * @param taskNumber one-based task number
     * @return the updated task
     * @throws WangsaException if the number is invalid
     */
    public Task unmark(int taskNumber) throws WangsaException {
        Task task = getTask(taskNumber);
        task.markAsNotDone();
        return task;
    }

    /**
     * Deletes and returns the numbered task.
     * @param taskNumber one-based task number
     * @return the removed task
     * @throws WangsaException if the number is invalid
     */
    public Task delete(int taskNumber) throws WangsaException {
        getTask(taskNumber);
        return tasks.remove(taskNumber - 1);
    }

    /**
     * Finds tasks whose descriptions contain the keyword, ignoring case.
     *
     * @param keyword text to search for.
     * @return matching tasks in their original order.
     */
    public List<Task> find(String keyword) {
        String normalizedKeyword = keyword.toLowerCase(Locale.ROOT);
        return tasks.stream()
                .filter(task -> task.getDescription().toLowerCase(Locale.ROOT).contains(normalizedKeyword))
                .toList();
    }

    /** Sorts deadlines chronologically and keeps tasks without deadlines after them. */
    public void sortByDeadline() {
        tasks.sort(Comparator.comparing(this::getSortDate));
    }

    /** Resolves a one-based task number or reports that it is outside the list. */
    private Task getTask(int taskNumber) throws WangsaException {
        if (taskNumber < 1 || taskNumber > tasks.size()) {
            throw new WangsaException("OOPS!!! Task number must be between 1 and "
                    + tasks.size() + ".");
        }
        return tasks.get(taskNumber - 1);
    }

    /** Returns a task's deadline or a sentinel date that sorts undated tasks last. */
    private LocalDate getSortDate(Task task) {
        return task instanceof Deadline deadline ? deadline.getBy() : LocalDate.MAX;
    }

    /** Checks invariants that should hold for every task-list state. */
    private void assertInvariant() {
        assert tasks.size() <= MAX_TASKS : "task list cannot exceed its capacity";
    }
}
