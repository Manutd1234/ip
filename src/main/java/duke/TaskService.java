package duke;

import java.util.List;

/**
 * Coordinates task operations that are shared by Wangsa's user interfaces.
 *
 * <p>The service keeps parsing, task-list mutation, and persistence together so that
 * the command-line and JavaFX interfaces do not need to duplicate application logic.</p>
 */
public final class TaskService {
    private final Parser parser;

    private final TaskRepository repository;

    private final TaskList tasks;

    /**
     * Creates a service and loads its task list from storage.
     *
     * @param repository source and destination for saved tasks
     * @param parser parser for Wangsa commands
     * @throws StorageException if saved tasks cannot be loaded
     * @throws WangsaException if the saved task list is invalid
     */
    public TaskService(TaskRepository repository, Parser parser) throws StorageException, WangsaException {
        this(repository, parser, new TaskList(repository.loadTasks()));
    }

    /**
     * Creates an empty service for use when previously saved data cannot be loaded.
     *
     * @param repository destination for future saves
     * @param parser parser for future commands
     * @return an empty task service
     */
    public static TaskService empty(TaskRepository repository, Parser parser) {
        return new TaskService(repository, parser, new TaskList());
    }

    /**
     * Creates a service around an already prepared task list.
     *
     * <p>This constructor is used by the empty-service fallback so the UI can remain
     * usable even when an existing save file cannot be loaded.</p>
     *
     * @param repository destination for future saves
     * @param parser parser for future commands
     * @param tasks task list managed by this service
     */
    private TaskService(TaskRepository repository, Parser parser, TaskList tasks) {
        this.parser = parser;
        this.repository = repository;
        this.tasks = tasks;
    }

    /** Returns a snapshot of all tasks in their current order.
     * @return all current tasks
     */
    public List<Task> getTasks() {
        return tasks.getTasks();
    }

    /**
     * Finds tasks matching the keyword in a complete {@code find} command.
     *
     * @param command complete find command
     * @return matching tasks in their original order
     * @throws WangsaException if the search keyword is missing
     */
    public List<Task> find(String command) throws WangsaException {
        return tasks.find(parser.parseSearchKeyword(command));
    }

    /**
     * Finds tasks by a validated keyword supplied by a parsed command.
     * @param keyword search keyword
     * @return matching tasks in their original order
     */
    public List<Task> findKeyword(String keyword) {
        return tasks.find(keyword);
    }

    /** Sorts tasks by deadline and saves the resulting order.
     * @throws StorageException if the updated order cannot be saved
     */
    public void sortByDeadline() throws StorageException {
        List<Task> previousOrder = tasks.getTasks();
        tasks.sortByDeadline();
        try {
            repository.saveTasks(tasks.getTasks());
        } catch (StorageException exception) {
            tasks.restoreOrder(previousOrder);
            throw exception;
        }
    }

    /**
     * Parses, adds, and saves a task.
     *
     * @param command complete task-creation command
     * @return the added task
     * @throws WangsaException if the command or task is invalid
     * @throws StorageException if the updated list cannot be saved
     */
    public Task add(String command) throws WangsaException, StorageException {
        return add(parser.parseTask(command));
    }

    /**
     * Adds a validated task and persists it at the end of the current order.
     * @param task task to add
     * @return the added task
     * @throws WangsaException if the list cannot accept the task
     * @throws StorageException if the task cannot be persisted
     */
    public Task add(Task task) throws WangsaException, StorageException {
        tasks.add(task);
        try {
            repository.insertTask(task, tasks.size() - 1);
        } catch (StorageException exception) {
            rollbackAddedTask(exception);
            throw exception;
        }
        return task;
    }

    /**
     * Marks the task identified by a complete {@code mark} command as done and saves it.
     *
     * @param command complete mark command
     * @return the updated task
     * @throws WangsaException if the task number is invalid
     * @throws StorageException if the updated list cannot be saved
     */
    public Task mark(String command) throws WangsaException, StorageException {
        return mark(parser.parseTaskNumber(command));
    }

    /**
     * Marks the supplied one-based task number as done and persists the change.
     * @param taskNumber one-based task number
     * @return the updated task
     * @throws WangsaException if the task number is invalid
     * @throws StorageException if the update cannot be persisted
     */
    public Task mark(int taskNumber) throws WangsaException, StorageException {
        return updateStatus(taskNumber, true);
    }

    /**
     * Marks the task identified by a complete {@code unmark} command as not done and saves it.
     *
     * @param command complete unmark command
     * @return the updated task
     * @throws WangsaException if the task number is invalid
     * @throws StorageException if the updated list cannot be saved
     */
    public Task unmark(String command) throws WangsaException, StorageException {
        return unmark(parser.parseTaskNumber(command));
    }

    /**
     * Marks the supplied one-based task number as not done and persists the change.
     * @param taskNumber one-based task number
     * @return the updated task
     * @throws WangsaException if the task number is invalid
     * @throws StorageException if the update cannot be persisted
     */
    public Task unmark(int taskNumber) throws WangsaException, StorageException {
        return updateStatus(taskNumber, false);
    }

    /** Changes a task's completion state and restores it if persistence fails. */
    private Task updateStatus(int taskNumber, boolean markAsDone)
            throws WangsaException, StorageException {
        Task task = tasks.get(taskNumber);
        boolean wasDone = task.isDone();
        if (markAsDone) {
            task.markAsDone();
        } else {
            task.markAsNotDone();
        }
        try {
            repository.updateTask(task, taskNumber - 1);
        } catch (StorageException exception) {
            restoreStatus(task, wasDone);
            throw exception;
        }
        return task;
    }

    /**
     * Deletes the task identified by a complete {@code delete} command and saves the result.
     *
     * @param command complete delete command
     * @return the removed task
     * @throws WangsaException if the task number is invalid
     * @throws StorageException if the updated list cannot be saved
     */
    public Task delete(String command) throws WangsaException, StorageException {
        int taskNumber = parser.parseTaskNumber(command);
        return delete(taskNumber);
    }

    /**
     * Deletes the supplied one-based task number and persists the change.
     * @param taskNumber one-based task number
     * @return the removed task
     * @throws WangsaException if the task number is invalid
     * @throws StorageException if the deletion cannot be persisted
     */
    public Task delete(int taskNumber) throws WangsaException, StorageException {
        Task task = tasks.get(taskNumber);
        repository.deleteTask(taskNumber - 1);
        tasks.delete(taskNumber);
        return task;
    }

    /** Removes an in-memory task after a failed persistence insert. */
    private void rollbackAddedTask(StorageException exception) {
        try {
            tasks.delete(tasks.size());
        } catch (WangsaException rollbackException) {
            exception.addSuppressed(rollbackException);
        }
    }

    /** Restores an in-memory status after a failed persistence update. */
    private void restoreStatus(Task task, boolean wasDone) {
        if (wasDone) {
            task.markAsDone();
        } else {
            task.markAsNotDone();
        }
    }
}
