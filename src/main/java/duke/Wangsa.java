package duke;

import java.nio.file.Path;

/**
 * Runs Wangsa's command-line adapter.
 *
 * <p>This class is intentionally responsible for input/output flow only. Command parsing,
 * task mutations, and persistence are delegated to {@link Parser} and {@link TaskService},
 * which keeps new interfaces from needing to duplicate business logic.</p>
 */
public class Wangsa {
    private static final Path DATABASE_PATH = Path.of("data", "wangsa.db");

    private final TaskRepository repository;

    private final Parser parser;

    private final Ui ui;

    /**
     * Creates Wangsa with console interaction and a SQLite database at the supplied path.
     * @param databasePath database location
     */
    public Wangsa(Path databasePath) {
        this(new SqliteTaskRepository(databasePath, databasePath.resolveSibling("wangsa.txt")),
                new Parser(), new Ui());
    }

    /** Creates Wangsa with supplied collaborators, allowing isolated testing. */
    Wangsa(TaskRepository repository, Parser parser, Ui ui) {
        this.repository = repository;
        this.parser = parser;
        this.ui = ui;
    }

    /** Runs the command loop until the user exits, input ends, or storage fails. */
    public void run() {
        try (ui) {
            ui.showWelcome();

            TaskService tasks;
            try {
                tasks = new TaskService(repository, parser);
            } catch (StorageException | WangsaException exception) {
                ui.showError(exception.getMessage());
                ui.showLine();
                return;
            }

            while (ui.hasNextCommand()) {
                String command = ui.readCommand();
                ui.showLine();
                try {
                    if (executeCommand(command, tasks)) {
                        ui.showLine();
                        return;
                    }
                } catch (WangsaException exception) {
                    ui.showError(exception.getMessage());
                } catch (StorageException exception) {
                    ui.showError(exception.getMessage());
                    ui.showLine();
                    return;
                }
                ui.showLine();
            }
        }
    }

    /**
     * Executes one command and returns whether it requests an exit.
     *
     * <p>The switch selects presentation behavior, while all state-changing work is
     * delegated to the shared service.</p>
     */
    private boolean executeCommand(String command, TaskService tasks)
            throws WangsaException, StorageException {
        Parser.CommandType commandType = parser.parseCommandType(command);
        switch (commandType) {
        case BYE:
            ui.showGoodbye();
            return true;
        case LIST:
            ui.showTaskList(tasks.getTasks());
            break;
        case FIND:
            ui.showMatchingTasks(tasks.find(command));
            break;
        case SORT:
            sortTasks(tasks);
            break;
        case MARK:
        case UNMARK:
            updateTaskStatus(command, commandType, tasks);
            break;
        case DELETE:
            deleteTask(command, tasks);
            break;
        case ADD_TASK:
            addTask(command, tasks);
            break;
        default:
            throw new IllegalStateException("Unsupported command type: " + commandType);
        }
        return false;
    }

    /** Saves and displays a task status change. */
    private void updateTaskStatus(String command, Parser.CommandType commandType, TaskService tasks)
            throws WangsaException, StorageException {
        boolean isMarked = commandType == Parser.CommandType.MARK;
        Task updatedTask = isMarked ? tasks.mark(command) : tasks.unmark(command);
        ui.showTaskStatusUpdate(updatedTask, isMarked);
    }

    /** Deletes, saves, and displays a task removal. */
    private void deleteTask(String command, TaskService tasks) throws WangsaException, StorageException {
        Task removedTask = tasks.delete(command);
        ui.showTaskDeleted(removedTask, tasks.getTasks().size());
    }

    /** Adds, saves, and displays a new task. */
    private void addTask(String command, TaskService tasks) throws WangsaException, StorageException {
        Task task = tasks.add(command);
        ui.showTaskAdded(task, tasks.getTasks().size());
    }

    /** Sorts, saves, and displays tasks by deadline. */
    private void sortTasks(TaskService tasks) throws StorageException {
        tasks.sortByDeadline();
        ui.showSortedTaskList(tasks.getTasks());
    }

    /**
     * Starts Wangsa using its default relative database path.
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        new Wangsa(DATABASE_PATH).run();
    }
}
