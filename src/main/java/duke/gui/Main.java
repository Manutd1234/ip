package duke.gui;

import java.nio.file.Path;
import java.util.List;

import duke.Parser;
import duke.Storage;
import duke.StorageException;
import duke.Task;
import duke.TaskList;
import duke.WangsaException;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/** Provides a graphical interface for Wangsa's task-management commands. */
public class Main extends Application {
    private static final Path DATA_FILE_PATH = Path.of("data", "wangsa.txt");

    private final Storage storage = new Storage(DATA_FILE_PATH);

    private final Parser parser = new Parser();

    private TextArea transcript;

    private TextField commandField;

    private TaskList tasks;

    /** Starts the JavaFX window and loads saved tasks. */
    @Override
    public void start(Stage stage) {
        transcript = new TextArea();
        transcript.setEditable(false);
        transcript.setWrapText(true);
        commandField = new TextField();
        commandField.setPromptText("Type a command, e.g. todo read book");
        Button sendButton = new Button("Send");
        sendButton.setDefaultButton(true);
        sendButton.setOnAction(event -> handleCommand());

        HBox commandBar = new HBox(8, commandField, sendButton);
        HBox.setHgrow(commandField, Priority.ALWAYS);
        Label title = new Label("Wangsa");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        VBox content = new VBox(12, title, transcript, commandBar);
        VBox.setVgrow(transcript, Priority.ALWAYS);
        content.setPadding(new Insets(16));

        stage.setTitle("Wangsa");
        stage.setScene(new Scene(new BorderPane(content), 640, 480));
        stage.show();
        loadTasks();
    }

    /** Loads persisted tasks and renders the initial task list. */
    private void loadTasks() {
        try {
            tasks = new TaskList(storage.loadTasks());
            append("Hello! I'm Wangsa.\n" + renderTasks(tasks.getTasks()));
        } catch (StorageException | WangsaException exception) {
            tasks = new TaskList();
            append(exception.getMessage());
        }
    }

    /** Parses and executes the command currently entered in the command field. */
    private void handleCommand() {
        String command = commandField.getText().trim();
        if (command.isEmpty()) {
            return;
        }
        commandField.clear();
        append("> " + command);
        try {
            execute(command);
        } catch (WangsaException | StorageException exception) {
            append(exception.getMessage());
        }
    }

    /** Executes a parsed command and persists mutations. */
    private void execute(String command) throws WangsaException, StorageException {
        Parser.CommandType commandType = parser.parseCommandType(command);
        switch (commandType) {
        case BYE:
            append("Bye. Hope to see you again soon!");
            javafx.application.Platform.exit();
            break;
        case LIST:
            append(renderTasks(tasks.getTasks()));
            break;
        case FIND:
            append(renderTasks(tasks.find(parser.parseSearchKeyword(command))));
            break;
        case MARK:
        case UNMARK:
            updateStatus(command, commandType);
            break;
        case DELETE:
            tasks.delete(parser.parseTaskNumber(command));
            storage.saveTasks(tasks.getTasks());
            append(renderTasks(tasks.getTasks()));
            break;
        case ADD_TASK:
            tasks.add(parser.parseTask(command));
            storage.saveTasks(tasks.getTasks());
            append(renderTasks(tasks.getTasks()));
            break;
        default:
            throw new IllegalStateException("Unsupported command type: " + commandType);
        }
    }

    /** Updates a task's completion status and persists the change. */
    private void updateStatus(String command, Parser.CommandType commandType)
            throws WangsaException, StorageException {
        int taskNumber = parser.parseTaskNumber(command);
        if (commandType == Parser.CommandType.MARK) {
            tasks.mark(taskNumber);
        } else {
            tasks.unmark(taskNumber);
        }
        storage.saveTasks(tasks.getTasks());
        append(renderTasks(tasks.getTasks()));
    }

    /** Formats tasks for display in the transcript. */
    private String renderTasks(List<Task> taskList) {
        if (taskList.isEmpty()) {
            return "No tasks found.";
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < taskList.size(); i++) {
            result.append(i + 1).append('.').append(taskList.get(i)).append(System.lineSeparator());
        }
        return result.toString().stripTrailing();
    }

    /** Appends one message to the GUI transcript. */
    private void append(String message) {
        transcript.appendText(message + System.lineSeparator() + System.lineSeparator());
    }
}
