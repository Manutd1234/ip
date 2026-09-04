package duke;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines the persistence operations needed by the task application.
 *
 * <p>Keeping this contract separate from concrete implementations allows Wangsa to
 * switch between the legacy text format, SQLite, or an in-memory repository for tests
 * without changing task-management code.</p>
 */
public interface TaskRepository {
    /**
     * Loads tasks from the repository.
     *
     * @return saved tasks in their original order
     * @throws StorageException if the repository cannot provide valid task data
     */
    List<Task> loadTasks() throws StorageException;

    /**
     * Saves the supplied tasks to the repository.
     *
     * @param tasks tasks to persist
     * @throws StorageException if the repository cannot save the tasks
     */
    void saveTasks(List<Task> tasks) throws StorageException;

    /**
     * Inserts one task at a zero-based display position.
     *
     * <p>The default implementation preserves compatibility with repositories that
     * only support complete snapshots. Database repositories can override this method
     * to perform a focused insert.</p>
     *
     * @param task task to insert
     * @param position zero-based display position
     * @throws StorageException if the task cannot be inserted
     */
    default void insertTask(Task task, int position) throws StorageException {
        List<Task> tasks = new ArrayList<>(loadTasks());
        validateInsertPosition(position, tasks.size());
        tasks.add(position, task);
        saveTasks(tasks);
    }

    /**
     * Updates one task at a zero-based display position.
     *
     * <p>The default implementation preserves compatibility with repositories that
     * only support complete snapshots. Database repositories can override this method
     * to perform a focused update.</p>
     *
     * @param task replacement task data
     * @param position zero-based display position
     * @throws StorageException if the task cannot be updated
     */
    default void updateTask(Task task, int position) throws StorageException {
        List<Task> tasks = new ArrayList<>(loadTasks());
        validateExistingPosition(position, tasks.size());
        tasks.set(position, task);
        saveTasks(tasks);
    }

    /**
     * Deletes one task at a zero-based display position.
     *
     * <p>The default implementation preserves compatibility with repositories that
     * only support complete snapshots. Database repositories can override this method
     * to perform a focused delete.</p>
     *
     * @param position zero-based display position
     * @throws StorageException if the task cannot be deleted
     */
    default void deleteTask(int position) throws StorageException {
        List<Task> tasks = new ArrayList<>(loadTasks());
        validateExistingPosition(position, tasks.size());
        tasks.remove(position);
        saveTasks(tasks);
    }

    /** Validates a position at which a new task may be inserted. */
    private static void validateInsertPosition(int position, int taskCount) throws StorageException {
        if (position < 0 || position > taskCount) {
            throw new StorageException("OOPS!!! Database insert position is outside the task list.");
        }
    }

    /** Validates a position occupied by an existing task. */
    private static void validateExistingPosition(int position, int taskCount) throws StorageException {
        if (position < 0 || position >= taskCount) {
            throw new StorageException("OOPS!!! Database task position is outside the task list.");
        }
    }
}
