package duke.gui;

import java.nio.file.Path;
import java.util.List;

import duke.Parser;
import duke.Storage;
import duke.StorageException;
import duke.Task;
import duke.TaskService;
import duke.WangsaException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

/**
 * Provides a polished JavaFX interface for Wangsa's task-management commands.
 *
 * <p>The interface combines JavaFX controls, CSS, and bundled character art. This keeps
 * the starter project easy to run while giving the chat a Pokédex-inspired visual
 * language and a clear command conversation.</p>
 */
public class Main extends Application {
    private static final Path DATA_FILE_PATH = Path.of("data", "wangsa.txt");

    private static final String STYLESHEET_PATH = "main.css";

    private static final String CHARIZARD_IMAGE_PATH = "/duke/gui/assets/charizard.jpg";

    private static final String ASH_IMAGE_PATH = "/duke/gui/assets/ash.jpeg";

    private final Parser parser = new Parser();

    private VBox messageList;

    private ScrollPane chatScrollPane;

    private TextField commandField;

    private TaskService taskService;

    /** Starts the JavaFX window and loads saved tasks. */
    @Override
    public void start(Stage stage) {
        BorderPane root = createRoot();
        Scene scene = new Scene(root, 1180, 760);
        String stylesheet = Main.class.getResource(STYLESHEET_PATH).toExternalForm();
        scene.getStylesheets().add(stylesheet);

        stage.setTitle("Wangsa // Level 10");
        stage.setMinWidth(900);
        stage.setMinHeight(620);
        stage.setScene(scene);
        stage.show();

        loadTasks();
        commandField.requestFocus();
    }

    /** Builds the application shell and its three primary areas. */
    private BorderPane createRoot() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-shell");
        root.setTop(createHeader());
        root.setCenter(createChatPanel());
        root.setBottom(createComposer());
        return root;
    }

    /** Creates the compact application header. */
    private Node createHeader() {
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("top-bar");

        StackPane pokeball = createPokeball(16);
        pokeball.getStyleClass().add("brand-mark");

        Label appName = new Label("WANGSA");
        appName.getStyleClass().add("brand-name");
        Label appSubtitle = new Label("AI QUEST ASSISTANT");
        appSubtitle.getStyleClass().add("brand-subtitle");
        VBox brand = new VBox(1, appName, appSubtitle);

        Label levelBadge = new Label("LEVEL 10");
        levelBadge.getStyleClass().add("level-badge");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Circle statusDot = new Circle(4, Color.web("#8ee7bd"));
        Label statusText = new Label("SYSTEM READY");
        statusText.getStyleClass().add("status-text");
        HBox status = new HBox(7, statusDot, statusText);
        status.setAlignment(Pos.CENTER);

        header.getChildren().addAll(pokeball, brand, levelBadge, spacer, status);
        return header;
    }

    /** Creates the central conversation panel. */
    private Node createChatPanel() {
        VBox chatPanel = new VBox(12);
        chatPanel.getStyleClass().add("chat-panel");
        chatPanel.setPadding(new Insets(16, 30, 10, 30));

        HBox chatHeading = new HBox(8);
        chatHeading.setAlignment(Pos.CENTER_LEFT);
        Label heading = new Label("FIELD NOTES");
        heading.getStyleClass().add("section-kicker");
        Label divider = new Label("/");
        divider.getStyleClass().add("heading-divider");
        Label headingHint = new Label("Your live quest conversation");
        headingHint.getStyleClass().add("heading-hint");
        Region headingSpacer = new Region();
        HBox.setHgrow(headingSpacer, Priority.ALWAYS);
        Label liveBadge = new Label("●  LIVE");
        liveBadge.getStyleClass().add("live-badge");
        chatHeading.getChildren().addAll(heading, divider, headingHint, headingSpacer, liveBadge);

        messageList = new VBox(14);
        messageList.setPadding(new Insets(2, 4, 20, 4));
        messageList.setFillWidth(true);

        chatScrollPane = new ScrollPane(messageList);
        chatScrollPane.setFitToWidth(true);
        chatScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        chatScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        chatScrollPane.getStyleClass().add("chat-scroll");
        VBox.setVgrow(chatScrollPane, Priority.ALWAYS);

        chatPanel.getChildren().addAll(chatHeading, chatScrollPane);
        return chatPanel;
    }

    /** Creates the command composer at the bottom of the window. */
    private Node createComposer() {
        VBox composer = new VBox(9);
        composer.getStyleClass().add("composer");

        Node cheatsheet = createCheatsheet();

        HBox composerRow = new HBox(10);
        composerRow.setAlignment(Pos.CENTER_LEFT);
        Label prompt = new Label(">_");
        prompt.getStyleClass().add("prompt-symbol");

        commandField = new TextField();
        commandField.setPromptText("Type a command, then press Enter");
        commandField.getStyleClass().add("command-field");
        commandField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleCommand();
                event.consume();
            }
        });

        Button sendButton = new Button("SEND  ↗");
        sendButton.getStyleClass().add("send-button");
        sendButton.setDefaultButton(true);
        sendButton.setOnAction(event -> handleCommand());
        HBox.setHgrow(commandField, Priority.ALWAYS);
        composerRow.getChildren().addAll(prompt, commandField, sendButton);

        HBox suggestions = new HBox(7);
        suggestions.setAlignment(Pos.CENTER_LEFT);
        Label suggestionLabel = new Label("TRY");
        suggestionLabel.getStyleClass().add("try-label");
        suggestions.getChildren().addAll(
                suggestionLabel,
                createSuggestion("list", "list"),
                createSuggestion("new todo", "todo "),
                createSuggestion("find", "find "),
                createSuggestion("mark #", "mark "));
        Region suggestionSpacer = new Region();
        HBox.setHgrow(suggestionSpacer, Priority.ALWAYS);
        Label keyboardHint = new Label("ENTER TO SEND");
        keyboardHint.getStyleClass().add("keyboard-hint");
        suggestions.getChildren().addAll(suggestionSpacer, keyboardHint);

        composer.getChildren().addAll(cheatsheet, composerRow, suggestions);
        return composer;
    }

    /** Creates the compact command reference above the composer. */
    private Node createCheatsheet() {
        VBox cheatsheet = new VBox(3);
        cheatsheet.getStyleClass().add("cheatsheet");

        Label title = new Label("COMMAND CHEATSHEET");
        title.getStyleClass().add("cheatsheet-title");
        Label commands = new Label("ADD  todo <description>  ·  deadline <description> /by <yyyy-MM-dd>  ·  "
                + "event <description> /from <start> /to <end>\n"
                + "VIEW  list  ·  find <keyword>    STATUS  mark <#>  ·  unmark <#>    "
                + "REMOVE  delete <#>    EXIT  bye");
        commands.setWrapText(true);
        commands.getStyleClass().add("cheatsheet-text");
        cheatsheet.getChildren().addAll(title, commands);
        return cheatsheet;
    }

    /** Creates a small command suggestion button. */
    private Button createSuggestion(String label, String command) {
        Button button = new Button(label);
        button.getStyleClass().add("suggestion-chip");
        button.setOnAction(event -> {
            prepareCommand(command);
            if (command.equals("list")) {
                handleCommand();
            }
        });
        return button;
    }

    /** Creates a simple Pokéball-inspired icon without requiring an image asset. */
    private StackPane createPokeball(double radius) {
        StackPane icon = new StackPane();
        Circle ball = new Circle(radius, Color.web("#d4473f"));
        ball.setStroke(Color.web("#f8f1e5"));
        ball.setStrokeWidth(1.5);
        Rectangle band = new Rectangle(radius * 2, 3);
        band.setFill(Color.web("#20263b"));
        Circle button = new Circle(radius * 0.34, Color.web("#f8f1e5"));
        button.setStroke(Color.web("#20263b"));
        button.setStrokeWidth(1.2);
        icon.getChildren().addAll(ball, band, button);
        return icon;
    }

    /** Creates a fixed-size character frame backed by an image resource. */
    private StackPane createCharacterAvatar(String resourcePath, double width, double height, String styleClass) {
        StackPane frame = new StackPane();
        frame.setMinSize(width, height);
        frame.setPrefSize(width, height);
        frame.setMaxSize(width, height);
        frame.getStyleClass().addAll("character-avatar", styleClass);

        // Decode at the source resolution; ImageView performs the final display scaling.
        Image image = new Image(Main.class.getResource(resourcePath).toExternalForm());
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(width - 4);
        imageView.setFitHeight(height - 4);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        frame.getChildren().add(imageView);
        return frame;
    }

    /** Loads persisted tasks and renders the initial conversation. */
    private void loadTasks() {
        try {
            taskService = new TaskService(new Storage(DATA_FILE_PATH), parser);
            appendAssistant("Hey, trainer! I'm Wangsa, your Level-10 quest partner.\n"
                    + "Try `list`, `todo ...`, or `mark #` and I'll keep your day moving.");
            appendAssistant(renderTasks(taskService.getTasks()));
        } catch (StorageException | WangsaException exception) {
            taskService = TaskService.empty(new Storage(DATA_FILE_PATH), parser);
            appendAssistant("I couldn't load the saved quest log, so I opened a fresh one.\n"
                    + exception.getMessage());
        }
    }

    /** Parses and executes the command currently entered in the command field. */
    private void handleCommand() {
        String command = commandField.getText().trim();
        if (command.isEmpty()) {
            return;
        }
        commandField.clear();
        appendUser(command);
        try {
            execute(command);
        } catch (WangsaException | StorageException exception) {
            appendAssistant(exception.getMessage());
        }
    }

    /** Places a command in the composer without executing it. */
    private void prepareCommand(String command) {
        commandField.setText(command);
        commandField.positionCaret(command.length());
        commandField.requestFocus();
    }

    /** Executes a parsed command and persists mutations. */
    private void execute(String command) throws WangsaException, StorageException {
        Parser.CommandType commandType = parser.parseCommandType(command);
        switch (commandType) {
        case BYE:
            appendAssistant("Quest paused. See you next time, trainer!");
            Platform.exit();
            break;
        case LIST:
            appendAssistant(renderTasks(taskService.getTasks()));
            break;
        case FIND:
            appendAssistant(renderTasks(taskService.find(command)));
            break;
        case MARK:
        case UNMARK:
            updateStatus(command, commandType);
            break;
        case DELETE:
            Task removedTask = taskService.delete(command);
            appendAssistant("Quest cleared from your log:\n" + removedTask + "\n\n" + progressSummary());
            break;
        case ADD_TASK:
            Task addedTask = taskService.add(command);
            appendAssistant("New quest added to your log:\n" + addedTask + "\n\n" + progressSummary());
            break;
        default:
            throw new IllegalStateException("Unsupported command type: " + commandType);
        }
    }

    /** Updates a task's completion status and persists the change. */
    private void updateStatus(String command, Parser.CommandType commandType)
            throws WangsaException, StorageException {
        Task updatedTask;
        if (commandType == Parser.CommandType.MARK) {
            updatedTask = taskService.mark(command);
        } else {
            updatedTask = taskService.unmark(command);
        }
        appendAssistant((commandType == Parser.CommandType.MARK ? "Quest complete! " : "Quest reopened: ")
                + updatedTask + "\n\n" + progressSummary());
    }

    /** Formats tasks for display in the conversation. */
    private String renderTasks(List<Task> taskList) {
        if (taskList.isEmpty()) {
            return "Your quest log is clear.\nUse `todo DESCRIPTION` below to catch a new quest.";
        }
        StringBuilder result = new StringBuilder("Here's your current quest log:\n");
        for (int i = 0; i < taskList.size(); i++) {
            result.append(i + 1).append(". ").append(taskList.get(i));
            if (i < taskList.size() - 1) {
                result.append(System.lineSeparator());
            }
        }
        return result.toString();
    }

    /** Returns a short progress message for mutation confirmations. */
    private String progressSummary() {
        int total = taskService.getTasks().size();
        int completed = countCompletedTasks();
        if (total > 0 && completed == total) {
            return "All " + total + " quests complete — " + completed + "/" + total
                    + "! Great run, trainer.";
        }
        return completed + " of " + total + " quests complete. Keep the streak going!";
    }

    /** Counts completed tasks in the service's current snapshot. */
    private int countCompletedTasks() {
        int completed = 0;
        for (Task task : taskService.getTasks()) {
            if (task.isDone()) {
                completed++;
            }
        }
        return completed;
    }

    /** Adds an assistant message to the chat and scrolls to the latest entry. */
    private void appendAssistant(String message) {
        messageList.getChildren().add(createMessage(message, false));
        scrollToLatestMessage();
    }

    /** Adds a user message to the chat and scrolls to the latest entry. */
    private void appendUser(String message) {
        messageList.getChildren().add(createMessage(message, true));
        scrollToLatestMessage();
    }

    /** Creates a left-aligned assistant or right-aligned user message bubble. */
    private Node createMessage(String message, boolean isUser) {
        HBox row = new HBox(10);
        row.setAlignment(isUser ? Pos.TOP_RIGHT : Pos.TOP_LEFT);
        row.setMaxWidth(Double.MAX_VALUE);
        row.getStyleClass().add("message-row");

        VBox bubble = new VBox(6);
        bubble.setMaxWidth(720);
        bubble.getStyleClass().add(isUser ? "user-bubble" : "assistant-bubble");

        Label speaker = new Label(isUser ? "TRAINER" : "CHARIZARD  //  AI QUEST ASSISTANT");
        speaker.getStyleClass().add("message-speaker");
        Label body = new Label(message);
        body.setWrapText(true);
        body.setMaxWidth(690);
        body.getStyleClass().add("message-body");
        Label metadata = new Label(isUser ? "COMMAND SENT" : "READY TO HELP");
        metadata.getStyleClass().add("message-metadata");
        bubble.getChildren().addAll(speaker, body, metadata);

        Node avatar = isUser
                ? createCharacterAvatar(ASH_IMAGE_PATH, 76, 104, "ash-avatar")
                : createCharacterAvatar(CHARIZARD_IMAGE_PATH, 110, 84, "charizard-avatar");
        if (isUser) {
            row.getChildren().addAll(bubble, avatar);
        } else {
            row.getChildren().addAll(avatar, bubble);
        }
        return row;
    }

    /** Scrolls the transcript after JavaFX has laid out the newly added message. */
    private void scrollToLatestMessage() {
        Platform.runLater(() -> chatScrollPane.setVvalue(1.0));
    }

}
