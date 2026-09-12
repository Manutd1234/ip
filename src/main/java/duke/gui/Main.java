package duke.gui;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import duke.AiHelper;
import duke.Command;
import duke.CommandHelp;
import duke.Parser;
import duke.SqliteTaskRepository;
import duke.StorageException;
import duke.Task;
import duke.TaskRepository;
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
 * Displays Wangsa's task conversation in a JavaFX window.
 *
 * <p>Task commands use the same parser and service as the terminal interface.
 * AI requests run in the background so the window stays responsive.</p>
 */
public class Main extends Application {
    private static final Path DATABASE_PATH = Path.of("data", "wangsa.db");

    private static final Path LEGACY_DATA_FILE_PATH = Path.of("data", "wangsa.txt");

    private static final String STYLESHEET_PATH = "main.css";

    private static final String CHARIZARD_IMAGE_PATH = "/duke/gui/assets/charizard.jpg";

    private static final String ASH_IMAGE_PATH = "/duke/gui/assets/ash.jpeg";

    private final Parser parser = new Parser();

    private final AiHelper aiHelper = new AiHelper();

    /**
     * At most one AI request runs at a time; normal task commands remain available.
     */
    private javafx.concurrent.Task<String> aiRequest;

    private VBox messageList;

    private ScrollPane chatScrollPane;

    private TextField commandField;

    private Label statusText;

    private Label taskStats;

    private final List<String> commandHistory = new ArrayList<>();

    private int historyIndex;

    private TaskService taskService;

    /**
     * Creates the JavaFX application instance used by the launcher.
     */
    public Main() {
    }

    /**
     * Starts the JavaFX window and loads saved tasks.
     */
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

    /**
     * Cancels an outstanding AI request when the window closes.
     */
    @Override
    public void stop() {
        if (aiRequest != null) {
            aiRequest.cancel();
        }
    }

    /**
     * Builds the application shell and its three primary areas.
     */
    private BorderPane createRoot() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-shell");
        root.setTop(createHeader());
        root.setCenter(createChatPanel());
        root.setBottom(createComposer());
        return root;
    }

    /**
     * Creates the compact application header.
     */
    private Node createHeader() {
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("top-bar");

        StackPane pokeball = createPokeball(16);
        pokeball.getStyleClass().add("brand-mark");

        Label appName = new Label("WANGSA");
        appName.getStyleClass().add("brand-name");
        Label appSubtitle = new Label("YOUR QUEST PARTNER");
        appSubtitle.getStyleClass().add("brand-subtitle");
        VBox brand = new VBox(1, appName, appSubtitle);

        Label levelBadge = new Label("LEVEL 10");
        levelBadge.getStyleClass().add("level-badge");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Circle statusDot = new Circle(4, Color.web("#8ee7bd"));
        statusText = new Label("SYSTEM READY");
        statusText.getStyleClass().add("status-text");
        taskStats = new Label("0 QUESTS  ·  0 DONE");
        taskStats.getStyleClass().add("task-stats");
        VBox statusCopy = new VBox(1, statusText, taskStats);
        statusCopy.setAlignment(Pos.CENTER_RIGHT);
        HBox status = new HBox(7, statusDot, statusCopy);
        status.setAlignment(Pos.CENTER);

        header.getChildren().addAll(pokeball, brand, levelBadge, spacer, status);
        return header;
    }

    /**
     * Creates the central conversation panel.
     */
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

    /**
     * Creates the command composer at the bottom of the window.
     */
    private Node createComposer() {
        VBox composer = new VBox(9);
        composer.getStyleClass().add("composer");
        composer.getChildren().addAll(createCheatsheet(), createComposerRow(), createSuggestions());
        return composer;
    }

    /**
     * Creates the input field and send action on a shared row.
     */
    private Node createComposerRow() {
        HBox composerRow = new HBox(10);
        composerRow.setAlignment(Pos.CENTER_LEFT);
        Label prompt = new Label(">_");
        prompt.getStyleClass().add("prompt-symbol");

        commandField = createCommandField();
        HBox.setHgrow(commandField, Priority.ALWAYS);
        composerRow.getChildren().addAll(prompt, commandField, createSendButton());
        return composerRow;
    }

    /**
     * Creates the command field with submission and history keyboard actions.
     */
    private TextField createCommandField() {
        TextField field = new TextField();
        field.setPromptText("Type a command, then press Enter");
        field.setAccessibleText("Wangsa command input");
        field.getStyleClass().add("command-field");
        field.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleCommand();
                event.consume();
            } else if (event.getCode() == KeyCode.UP || event.getCode() == KeyCode.DOWN) {
                navigateHistory(event.getCode());
                event.consume();
            }
        });
        return field;
    }

    /**
     * Creates the default action for submitting a command.
     */
    private Button createSendButton() {
        Button sendButton = new Button("SEND  ↗");
        sendButton.getStyleClass().add("send-button");
        sendButton.setDefaultButton(true);
        sendButton.setOnAction(event -> handleCommand());
        return sendButton;
    }

    /**
     * Creates command shortcuts and the keyboard reminder.
     */
    private Node createSuggestions() {
        HBox suggestions = new HBox(7);
        suggestions.setAlignment(Pos.CENTER_LEFT);
        Label suggestionLabel = new Label("TRY");
        suggestionLabel.getStyleClass().add("try-label");
        suggestions.getChildren().addAll(
                suggestionLabel,
                createSuggestion("list", "list"),
                createSuggestion("new todo", "todo "),
                createSuggestion("find", "find "),
                createSuggestion("mark #", "mark "),
                createSuggestion("ask AI", "@ai "));
        Region suggestionSpacer = new Region();
        HBox.setHgrow(suggestionSpacer, Priority.ALWAYS);
        Label keyboardHint = new Label("ENTER TO SEND");
        keyboardHint.getStyleClass().add("keyboard-hint");
        suggestions.getChildren().addAll(suggestionSpacer, keyboardHint);

        return suggestions;
    }

    /**
     * Creates the compact command reference above the composer.
     */
    private Node createCheatsheet() {
        VBox cheatsheet = new VBox(3);
        cheatsheet.getStyleClass().add("cheatsheet");

        Label title = new Label("COMMAND CHEATSHEET");
        title.getStyleClass().add("cheatsheet-title");
        Label commands = new Label("ADD  todo <description>  ·  deadline <description> /by <yyyy-MM-dd>  ·  "
                + "event <description> /from <start> /to <end>\n"
                + "VIEW  list  ·  find <keyword>  ·  sort    STATUS  mark <#>  ·  unmark <#>    "
                + "REMOVE  delete <#>    EXIT  bye\n"
                + "HELP  help  ·  @ai <question>");
        commands.setWrapText(true);
        commands.getStyleClass().add("cheatsheet-text");
        cheatsheet.getChildren().addAll(title, commands);
        return cheatsheet;
    }

    /**
     * Creates a small command suggestion button.
     */
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

    /**
     * Creates a simple Pokéball-inspired icon without requiring an image asset.
     */
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

    /**
     * Creates a fixed-size character frame backed by an image resource.
     */
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

    /**
     * Loads persisted tasks and renders the initial conversation.
     */
    private void loadTasks() {
        try {
            taskService = new TaskService(createRepository(), parser);
            appendAssistant("Hi, trainer! I'm Wangsa. What would you like to work on today?\n"
                    + "Try `todo read a book` to add a task, or `help` to see the commands.\n"
                    + "You can also ask about commands with `@ai How do I add a deadline?`.");
            appendAssistant(TaskFormatter.renderTasks(taskService.getTasks()));
        } catch (StorageException | WangsaException exception) {
            appendAssistant("I couldn't load the saved quest log. Commands are blocked to protect your saved tasks.\n"
                    + exception.getMessage()
                    + "\nFix the data or folder access, then restart Wangsa. Use `bye` to close.");
        }
        refreshHeader();
    }

    /**
     * Creates the default repository shared by the JavaFX workflows.
     */
    private TaskRepository createRepository() {
        return new SqliteTaskRepository(DATABASE_PATH, LEGACY_DATA_FILE_PATH);
    }

    /**
     * Parses and executes the command currently entered in the command field.
     */
    private void handleCommand() {
        String command = commandField.getText().trim();
        if (command.isEmpty()) {
            return;
        }
        commandField.clear();
        appendUser(command);
        try {
            execute(command);
            rememberCommand(command);
        } catch (WangsaException | StorageException exception) {
            appendAssistant(exception.getMessage());
        }
        refreshHeader();
    }

    /**
     * Places a command in the composer without executing it.
     */
    private void prepareCommand(String command) {
        commandField.setText(command);
        commandField.positionCaret(command.length());
        commandField.requestFocus();
    }

    /**
     * Stores a successful command once so history remains useful and compact.
     */
    private void rememberCommand(String command) {
        if (commandHistory.isEmpty() || !commandHistory.get(commandHistory.size() - 1).equals(command)) {
            commandHistory.add(command);
        }
        historyIndex = commandHistory.size();
    }

    /**
     * Navigates the command history without leaving the current conversation.
     */
    private void navigateHistory(KeyCode direction) {
        if (commandHistory.isEmpty()) {
            return;
        }
        historyIndex += direction == KeyCode.UP ? -1 : 1;
        historyIndex = Math.max(0, Math.min(historyIndex, commandHistory.size()));
        commandField.setText(historyIndex == commandHistory.size() ? "" : commandHistory.get(historyIndex));
        commandField.positionCaret(commandField.getText().length());
    }

    /**
     * Executes a parsed command and persists mutations.
     */
    private void execute(String command) throws WangsaException, StorageException {
        Command parsedCommand = parser.parse(command);
        if (taskService == null && parsedCommand.type() != Parser.CommandType.BYE) {
            throw new StorageException("Your saved quest log is unavailable. Fix the startup error and restart "
                    + "Wangsa before using task commands.");
        }
        switch (parsedCommand.type()) {
            case BYE -> {
                appendAssistant("See you next time, trainer.");
                Platform.exit();
            }
            case LIST -> appendAssistant(TaskFormatter.renderTasks(taskService.getTasks()));
            case HELP -> appendAssistant(CommandHelp.getText());
            case AI -> askAi(((Command.AiQuestion) parsedCommand).question());
            case FIND -> appendAssistant(TaskFormatter.renderMatches(
                    taskService.findMatches(((Command.Search) parsedCommand).keyword())));
            case SORT -> sortTasks();
            case MARK, UNMARK -> updateStatus((Command.TaskNumber) parsedCommand);
            case DELETE -> deleteTask((Command.TaskNumber) parsedCommand);
            case ADD_TASK -> addTask((Command.AddTask) parsedCommand);
            default -> throw new IllegalStateException("Unsupported command type: " + parsedCommand.type());
        }
    }

    /**
     * Saves the new order before showing the user any changed task numbers.
     */
    private void sortTasks() throws StorageException {
        taskService.sortByDeadline();
        appendAssistant("I've put the earliest deadlines first, followed by tasks without dates.\n"
                + TaskFormatter.renderTasks(taskService.getTasks()));
    }

    /**
     * Removes a task and shows the remaining progress after the save succeeds.
     */
    private void deleteTask(Command.TaskNumber command) throws WangsaException, StorageException {
        Task removedTask = taskService.delete(command.taskNumber());
        appendAssistant("I've removed this quest:\n" + removedTask + "\n\n"
                + TaskFormatter.formatProgressSummary(taskService.getTasks()));
    }

    /**
     * Adds a task and confirms it only after the service saves it.
     */
    private void addTask(Command.AddTask command) throws WangsaException, StorageException {
        Task addedTask = taskService.add(command.task());
        appendAssistant("Added to your quest log:\n" + addedTask + "\n\n"
                + TaskFormatter.formatProgressSummary(taskService.getTasks()));
    }

    /**
     * Starts one background request while allowing the user to keep managing tasks.
     */
    private void askAi(String question) throws WangsaException {
        if (aiRequest != null) {
            throw new WangsaException("I'm still checking your last question. You can keep using task commands.");
        }
        Node pendingMessage = createMessage("Let me check that for you...", false);
        messageList.getChildren().add(pendingMessage);
        scrollToLatestMessage();

        javafx.concurrent.Task<String> request = new javafx.concurrent.Task<>() {
            @Override
            protected String call() {
                return aiHelper.ask(question);
            }
        };
        request.setOnSucceeded(event -> finishAiRequest(pendingMessage, request.getValue()));
        request.setOnFailed(event -> finishAiRequest(pendingMessage,
                "I couldn't get an AI answer just now. Type `help` for the command guide."));
        aiRequest = request;
        Thread worker = new Thread(request, "wangsa-ai-help");
        worker.setDaemon(true); // A slow network request must not keep the app open after the window closes.
        worker.start();
    }

    /**
     * Replaces the loading bubble on the JavaFX thread, keeping the answer beside its question.
     */
    private void finishAiRequest(Node pendingMessage, String answer) {
        int messageIndex = messageList.getChildren().indexOf(pendingMessage);
        messageList.getChildren().set(messageIndex, createMessage(answer, false));
        aiRequest = null;
        scrollToLatestMessage();
    }

    /**
     * Updates a task's completion status and persists the change.
     */
    private void updateStatus(Command.TaskNumber command)
            throws WangsaException, StorageException {
        boolean isMarked = command.type() == Parser.CommandType.MARK;
        Task updatedTask = isMarked ? taskService.mark(command.taskNumber()) : taskService.unmark(command.taskNumber());
        appendAssistant((isMarked ? "Nice work! You've completed:\n" : "I've reopened this quest for you:\n")
                + updatedTask + "\n\n" + TaskFormatter.formatProgressSummary(taskService.getTasks()));
    }

    /**
     * Refreshes the live status copy after loading or mutating tasks.
     */
    private void refreshHeader() {
        if (statusText == null || taskStats == null) {
            return;
        }
        if (taskService == null) {
            statusText.setText("STORAGE UNAVAILABLE");
            taskStats.setText("TASK COMMANDS BLOCKED");
            return;
        }
        statusText.setText("SYSTEM READY");
        taskStats.setText(TaskFormatter.formatHeaderStats(taskService.getTasks()));
    }

    /**
     * Adds an assistant message to the chat and scrolls to the latest entry.
     */
    private void appendAssistant(String message) {
        messageList.getChildren().add(createMessage(message, false));
        scrollToLatestMessage();
    }

    /**
     * Adds a user message to the chat and scrolls to the latest entry.
     */
    private void appendUser(String message) {
        messageList.getChildren().add(createMessage(message, true));
        scrollToLatestMessage();
    }

    /**
     * Creates a left-aligned assistant or right-aligned user message bubble.
     */
    private Node createMessage(String message, boolean isUser) {
        HBox row = new HBox(10);
        row.setAlignment(isUser ? Pos.TOP_RIGHT : Pos.TOP_LEFT);
        row.setMaxWidth(Double.MAX_VALUE);
        row.getStyleClass().add("message-row");

        VBox bubble = new VBox(6);
        bubble.setMaxWidth(720);
        bubble.getStyleClass().add(isUser ? "user-bubble" : "assistant-bubble");

        Label speaker = new Label(isUser ? "TRAINER" : "WANGSA  //  YOUR QUEST PARTNER");
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

    /**
     * Scrolls the transcript after JavaFX has laid out the newly added message.
     */
    private void scrollToLatestMessage() {
        Platform.runLater(() -> chatScrollPane.setVvalue(1.0));
    }

}
