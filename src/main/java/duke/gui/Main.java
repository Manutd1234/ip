package duke.gui;

import java.util.ArrayList;
import java.util.List;

import duke.AiHelper;
import duke.Command;
import duke.Parser;
import duke.SaveLocation;
import duke.Storage;
import duke.StorageException;
import duke.Task;
import duke.TaskRepository;
import duke.TaskService;
import duke.WangsaException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * Displays Wangsa's task conversation in a JavaFX window.
 *
 * <p>Task commands use the same parser and service as the terminal interface.
 * AI requests run in the background so the window stays responsive.</p>
 */
public class Main extends Application {
    private static final String STYLESHEET_PATH = "main.css";

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
        Typography.apply(root);
        Rectangle2D screen = Screen.getPrimary().getVisualBounds();
        Scene scene = new Scene(root, Math.min(1120, screen.getWidth() - 48),
                Math.min(700, screen.getHeight() - 76));
        String stylesheet = Main.class.getResource(STYLESHEET_PATH).toExternalForm();
        scene.getStylesheets().add(stylesheet);

        stage.setTitle("Wangsa");
        stage.setMinWidth(780);
        stage.setMinHeight(580);
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

        QuestIcon emblem = new QuestIcon(34, false);
        emblem.getStyleClass().add("brand-mark");

        Label appName = new Label("WANGSA");
        appName.getStyleClass().add("brand-name");
        Label appSubtitle = new Label("YOUR TASK COMPANION");
        appSubtitle.getStyleClass().add("brand-subtitle");
        VBox brand = new VBox(1, appName, appSubtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(emblem, brand, spacer, createStatusPanel());
        return header;
    }

    /**
     * Aligns the status indicator with its heading and both text lines in a shared column.
     */
    private Node createStatusPanel() {
        Circle statusDot = new Circle(4, Color.web("#8ee7bd"));
        statusDot.getStyleClass().add("status-dot");
        statusText = new Label("SYSTEM READY");
        statusText.getStyleClass().add("status-text");
        taskStats = new Label("0 TASKS  ·  0 DONE");
        taskStats.getStyleClass().add("task-stats");
        GridPane status = new GridPane();
        status.setHgap(8);
        status.setVgap(3);
        status.setAlignment(Pos.CENTER_RIGHT);
        status.add(statusDot, 0, 0);
        status.add(statusText, 1, 0);
        status.add(taskStats, 1, 1);
        return status;
    }

    /**
     * Creates the central conversation panel.
     */
    private Node createChatPanel() {
        VBox chatPanel = new VBox(12);
        chatPanel.getStyleClass().add("chat-panel");
        chatPanel.setPadding(new Insets(14, 24, 8, 24));

        HBox chatHeading = new HBox(8);
        chatHeading.setAlignment(Pos.CENTER_LEFT);
        Label heading = new Label("CHAT");
        heading.getStyleClass().add("section-kicker");
        Label divider = new Label("/");
        divider.getStyleClass().add("heading-divider");
        Label headingHint = new Label("One task at a time");
        headingHint.getStyleClass().add("heading-hint");
        Region headingSpacer = new Region();
        HBox.setHgrow(headingSpacer, Priority.ALWAYS);
        Label liveBadge = new Label("●  LIVE");
        liveBadge.getStyleClass().add("live-badge");
        chatHeading.getChildren().addAll(heading, divider, headingHint, headingSpacer, liveBadge);

        messageList = new VBox(12);
        messageList.setId("messages");
        messageList.setMaxWidth(Double.MAX_VALUE);
        messageList.setPadding(new Insets(8, 0, 12, 0));
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

        commandField = createCommandField();
        HBox.setHgrow(commandField, Priority.ALWAYS);
        composerRow.getChildren().addAll(commandField, createSendButton());
        return composerRow;
    }

    /**
     * Creates the command field with submission and history keyboard actions.
     */
    private TextField createCommandField() {
        TextField field = new TextField();
        field.setPromptText("Type a command, then press Enter");
        field.setAccessibleText("Wangsa command input");
        field.setMinWidth(0);
        field.setMinHeight(48);
        field.setPrefHeight(48);
        field.setMaxHeight(48);
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
        sendButton.setMinWidth(92);
        sendButton.setMinHeight(48);
        sendButton.setPrefHeight(48);
        sendButton.setMaxHeight(48);
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
        GridPane cheatsheet = new GridPane();
        cheatsheet.setHgap(10);
        cheatsheet.setVgap(3);
        cheatsheet.getStyleClass().add("cheatsheet");
        ColumnConstraints category = new ColumnConstraints(48);
        ColumnConstraints examples = new ColumnConstraints();
        examples.setMinWidth(0);
        examples.setHgrow(Priority.ALWAYS);
        cheatsheet.getColumnConstraints().addAll(category, examples);
        addCheatsheetRow(cheatsheet, 0, "ADD", "todo <description>  ·  deadline <description> /by <yyyy-MM-dd>");
        addCheatsheetRow(cheatsheet, 1, "EVENT", "event <description> /from <start> /to <end>");
        addCheatsheetRow(cheatsheet, 2, "TASKS",
                "list  ·  find <keyword>  ·  sort  ·  mark <#>  ·  unmark <#>  ·  delete <#>");
        addCheatsheetRow(cheatsheet, 3, "HELP", "help  ·  @ai <question>  ·  bye");
        return cheatsheet;
    }

    /**
     * Aligns command groups in real columns so wrapped text keeps its indentation.
     */
    private void addCheatsheetRow(GridPane grid, int row, String category, String examples) {
        Label heading = new Label(category);
        heading.getStyleClass().add("cheatsheet-title");
        Label commands = new Label(examples);
        commands.setMinWidth(0);
        commands.setMaxWidth(Double.MAX_VALUE);
        commands.setWrapText(true);
        commands.getStyleClass().add("cheatsheet-text");
        grid.addRow(row, heading, commands);
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
     * Loads persisted tasks and renders the initial conversation.
     */
    private void loadTasks() {
        try {
            taskService = new TaskService(createRepository(), parser);
            appendAssistant("Hi! I'm Wangsa. Let's make room for what matters.\n\n"
                    + "• Add a task: todo read a book\n"
                    + "• See command examples: help\n"
                    + "• Your changes save automatically.");
            appendTasks(TaskView.tasks(taskService.getTasks()));
        } catch (StorageException | WangsaException exception) {
            appendError("I couldn't load your saved tasks. Nothing has been changed.\n"
                    + exception.getMessage()
                    + "\nType `help` for commands or `bye` to close.");
        }
        refreshHeader();
    }

    /**
     * Creates the default repository shared by the JavaFX workflows.
     */
    private TaskRepository createRepository() {
        return new Storage(SaveLocation.getDefaultFile());
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
            appendError(exception.getMessage());
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
        if (taskService == null && parsedCommand.type() != Parser.CommandType.BYE
                && parsedCommand.type() != Parser.CommandType.HELP) {
            throw new StorageException("Your saved task list is unavailable. Fix the startup error and restart "
                    + "Wangsa before using task commands.");
        }
        switch (parsedCommand.type()) {
            case BYE -> {
                appendAssistant("See you next time!");
                Platform.exit();
            }
            case LIST -> appendTasks(TaskView.tasks(taskService.getTasks()));
            case HELP -> {
                messageList.getChildren().add(ChatMessage.help());
                scrollToLatestMessage();
            }
            case AI -> askAi(((Command.AiQuestion) parsedCommand).question());
            case FIND -> appendTasks(TaskView.matches(
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
        appendTasks(TaskView.sorted(taskService.getTasks()));
    }

    /**
     * Removes a task and shows the remaining progress after the save succeeds.
     */
    private void deleteTask(Command.TaskNumber command) throws WangsaException, StorageException {
        Task removedTask = taskService.delete(command.taskNumber());
        appendTasks(TaskView.confirmation("Deleted:", removedTask,
                TaskFormatter.formatProgressSummary(taskService.getTasks())));
    }

    /**
     * Adds a task and confirms it only after the service saves it.
     */
    private void addTask(Command.AddTask command) throws WangsaException, StorageException {
        Task addedTask = taskService.add(command.task());
        appendTasks(TaskView.confirmation("Added:", addedTask,
                TaskFormatter.formatProgressSummary(taskService.getTasks())));
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
        appendTasks(TaskView.confirmation(isMarked ? "Nice work! Completed:" : "Ready to work on again:",
                updatedTask, TaskFormatter.formatProgressSummary(taskService.getTasks())));
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

    /** Adds a structured task snapshot using the same scrolling behavior as other replies. */
    private void appendTasks(TaskView tasks) {
        messageList.getChildren().add(ChatMessage.tasks(tasks));
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
     * Makes failures distinguishable by both text and color, not color alone.
     */
    private void appendError(String message) {
        messageList.getChildren().add(new ChatMessage(message, ChatMessage.Role.ERROR));
        scrollToLatestMessage();
    }

    /**
     * Creates a left-aligned assistant or right-aligned user message bubble.
     */
    private Node createMessage(String message, boolean isUser) {
        return new ChatMessage(message, isUser ? ChatMessage.Role.USER : ChatMessage.Role.ASSISTANT);
    }

    /**
     * Reveals the start of a long reply so users can read it from the beginning.
     */
    private void scrollToLatestMessage() {
        Platform.runLater(() -> {
            chatScrollPane.getScene().getRoot().applyCss();
            chatScrollPane.getScene().getRoot().layout();
            double scrollableHeight = chatScrollPane.getContent().getBoundsInLocal().getHeight()
                    - chatScrollPane.getViewportBounds().getHeight();
            double lastMessageTop = messageList.getChildren().getLast().getBoundsInParent().getMinY();
            chatScrollPane.setVvalue(scrollableHeight <= 0 ? 0 : Math.min(1, lastMessageTop / scrollableHeight));
        });
    }

}
