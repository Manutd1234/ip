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

            TaskList tasks;
            try {
                tasks = new TaskList(storage.loadTasks());
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
    private boolean executeCommand(String command, TaskList tasks)
            throws WangsaException, StorageException {
        Parser.CommandType commandType = parser.parseCommandType(command);
        if (commandType == Parser.CommandType.BYE) {
            ui.showGoodbye();
            return true;
        } else if (commandType == Parser.CommandType.LIST) {
            ui.showTaskList(tasks.getTasks());
        } else if (commandType == Parser.CommandType.FIND) {
            ui.showMatchingTasks(tasks.find(parser.parseSearchKeyword(command)));
        } else if (commandType == Parser.CommandType.MARK
                || commandType == Parser.CommandType.UNMARK) {
            int taskNumber = parser.parseTaskNumber(command);
            boolean isMarked = commandType == Parser.CommandType.MARK;
            Task updatedTask = isMarked ? tasks.mark(taskNumber) : tasks.unmark(taskNumber);
            storage.saveTasks(tasks.getTasks());
            ui.showTaskStatusUpdate(updatedTask, isMarked);
        } else if (commandType == Parser.CommandType.DELETE) {
            Task removedTask = tasks.delete(parser.parseTaskNumber(command));
            storage.saveTasks(tasks.getTasks());
            ui.showTaskDeleted(removedTask, tasks.size());
        } else if (commandType == Parser.CommandType.ADD_TASK) {
            Task task = parser.parseTask(command);
            tasks.add(task);
            storage.saveTasks(tasks.getTasks());
            ui.showTaskAdded(task, tasks.size());
        } else {
            throw new IllegalStateException("Unsupported command type: " + commandType);
        }
        return false;
    }

    /**
     * Starts Wangsa using its default relative data-file path.
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        new Wangsa(DATA_FILE_PATH).run();
    }
}
