package duke;

import java.nio.file.Path;

/**
 * Coordinates Wangsa's user interface, parser, task list, and storage.
 */
public class Wangsa {
    private static final Path DATA_FILE_PATH = Path.of("data", "wangsa.txt");

    private final Storage storage;

    private final Parser parser;

    private final Ui ui;

    /**
     * Creates Wangsa with console interaction and storage at the supplied path.
     * @param dataFilePath save-file location
     */
    public Wangsa(Path dataFilePath) {
        this(new Storage(dataFilePath), new Parser(), new Ui());
    }

    /** Creates Wangsa with supplied collaborators, allowing isolated testing. */
    Wangsa(Storage storage, Parser parser, Ui ui) {
        this.storage = storage;
        this.parser = parser;
        this.ui = ui;
    }

    /** Runs the command loop until the user exits, input ends, or storage fails. */
    public void run() {
        try (ui) {
            ui.showWelcome();

            TaskService tasks;
            try {
                tasks = new TaskService(storage, parser);
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

    /** Executes one parsed command and returns whether it requests an exit. */
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

    /**
     * Starts Wangsa using its default relative data-file path.
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        new Wangsa(DATA_FILE_PATH).run();
    }
}
