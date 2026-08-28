package duke;

import java.util.List;

/**
 * Defines the persistence operations needed by the task application.
 *
 * <p>Keeping this contract separate from the file-based implementation allows future
 * storage options, such as an in-memory repository for tests or a database repository,
 * to be added without changing task-management code.</p>
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
}
