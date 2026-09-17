package duke.gui;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import duke.SaveLocation;
import duke.Storage;
import duke.StorageException;
import duke.Task;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Exercises the release JAR's actual launcher, desktop commands, and text saving in a disposable folder.
 */
public final class ReleaseSmoke {
    private static volatile Throwable failure;

    private final Stage stage;

    private ReleaseSmoke(Stage stage) {
        this.stage = stage;
    }

    /**
     * Runs one release scenario. The Python runner creates a fresh working folder and enforces a timeout.
     *
     * @param args The scenario: normal, reload, or blocked.
     */
    public static void main(String[] args) {
        AtomicBoolean hasStarted = new AtomicBoolean();
        var watcher = Executors.newSingleThreadScheduledExecutor(Thread.ofPlatform().daemon().factory());
        watcher.scheduleAtFixedRate(() -> {
            try {
                Platform.runLater(() -> {
                    if (Window.getWindows().isEmpty() || hasStarted.getAndSet(true)) {
                        return;
                    }
                    watcher.shutdown();
                    try {
                        Stage window = (Stage) Window.getWindows().getFirst();
                        new ReleaseSmoke(window).verify(args[0]);
                        System.out.println("PASS: release GUI " + args[0]);
                    } catch (Throwable error) {
                        failure = error;
                    } finally {
                        Platform.exit();
                    }
                });
            } catch (IllegalStateException e) {
                // The launcher has not initialized JavaFX yet; the next scheduled check will try again.
            }
        }, 100, 100, TimeUnit.MILLISECONDS);
        Launcher.main(new String[0]);
        if (failure != null) {
            throw new AssertionError("Release smoke failed", failure);
        }
    }

    private void verify(String scenario) throws StorageException {
        check(stage.getTitle().contains("Wangsa"), "The window must use the product name");
        check(stage.isShowing(), "The release window must open");
        check(stage.getScene().lookup(".charizard-avatar") != null, "The Charizard artwork must be present");
        if (scenario.equals("blocked")) {
            check(lastReply().contains("couldn't load"), "Corrupt data must produce a startup error");
            send("todo must not overwrite data");
            check(lastReply().contains("unavailable"), "Task commands must be blocked after a load error");
            check(stage.getScene().lookup(".error-bubble") != null, "Storage errors must be visually distinct");
            send("help");
            check(lastReply().contains("todo DESCRIPTION"), "Offline help must remain available after a load error");
            verifyLayout(780, 550);
        } else if (scenario.equals("reload")) {
            check(lastReply().contains("[D][X] submit report"), "Saved completion must survive a restart");
            check(lastReply().contains("[E][ ] team meeting"), "Saved events must survive a restart");
            check(savedTasks().size() == 2, "Saved deletions must survive a restart");
        } else {
            verifyCommands();
        }
        send("bye");
        check(lastReply().contains("See you next time"), "The bye command must exit normally");
    }

    private void verifyCommands() throws StorageException {
        check(lastReply().contains("empty"), "A fresh release must start with an empty task list");
        send("  todo read book  ");
        check(stage.getScene().lookup(".ash-avatar") != null, "The Ash artwork must be present");
        send("deadline submit report /by 2026-09-20");
        send("event team meeting /from Monday 2pm /to Monday 4pm");
        check(savedTasks().size() == 3, "All three task types must save");
        send("find REPORT");
        check(lastReply().contains("2. [D][ ] submit report"), "Search must retain the full-list task number");
        send("mark 2");
        check(savedTasks().get(1).isDone(), "Mark must save completion");
        send("unmark 2");
        check(!savedTasks().get(1).isDone(), "Unmark must save the reopened task");
        send("mark 2");
        send("sort");
        check(savedTasks().getFirst().getDescription().equals("submit report"), "Sort must save deadline order");
        send("delete 2");
        check(savedTasks().size() == 2, "Delete must save the smaller task list");
        send("list");
        check(lastReply().contains("1. [D][X] submit report"), "List must show current numbering and status");
        check(lastReply().contains("2. [E][ ] team meeting"), "List must retain event details");
        send("find nothing-matches-this");
        check(lastReply().contains("No matching"), "An empty search must be explained");
        send("deadline impossible /by 2026-02-30");
        check(savedTasks().size() == 2, "Invalid dates must not add tasks");
        check(stage.getScene().lookup(".error-bubble") != null, "Command errors must be visually distinct");
        send("event repeated /from now /from later /to tomorrow");
        check(savedTasks().size() == 2, "Repeated markers must not add tasks");
        send("delete 999");
        check(savedTasks().size() == 2, "Invalid indexes must not delete tasks");
        send("help");
        check(lastReply().contains("todo DESCRIPTION"), "Built-in help must work without AI configuration");

        TextField field = commandField();
        field.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.UP, false, false, false, false));
        check(field.getText().equals("help"), "Up must recall the last successful command");
        field.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.DOWN, false, false, false, false));
        check(field.getText().isEmpty(), "Down must return to an empty composer");
        send("find " + "verylongword".repeat(30));
        verifyLayout(780, 550);
        verifyLayout(1120, 700);
    }

    /** Checks responsive geometry directly instead of assuming that a visible field is not clipped. */
    private void verifyLayout(double width, double height) {
        Region root = (Region) stage.getScene().getRoot();
        root.resize(width, height);
        root.applyCss();
        root.layout();
        verifyHeaderAlignment(root);
        Node field = commandField();
        Node sendButton = root.lookup(".send-button");
        check(field.getBoundsInParent().getWidth() >= 200, "The command field must retain usable width");
        check(Math.abs(field.getLayoutBounds().getHeight() - sendButton.getLayoutBounds().getHeight()) < 1,
                "The input and send button must have equal heights");
        Bounds inputBounds = field.localToScene(field.getLayoutBounds());
        check(inputBounds.getMaxY() <= height, "The command field must fit inside the resized window");
        ScrollPane chat = (ScrollPane) root.lookup(".chat-scroll");
        for (Node row : ((Parent) chat.getContent()).getChildrenUnmodifiable()) {
            Node avatar = row.lookup(".character-avatar");
            check(avatar.getLayoutBounds().getWidth() == 72, "Avatars must share the same column width");
            Node bubble = row.lookup(".assistant-bubble");
            bubble = bubble == null ? row.lookup(".user-bubble") : bubble;
            check(Math.abs(avatar.getLayoutY() - bubble.getLayoutY()) < 1,
                    "Avatar frames and message bubbles must align at the top");
            check(bubble.getBoundsInParent().getMaxX() <= row.getLayoutBounds().getWidth() + 1,
                    "Long messages must stay inside their row");
            Label body = (Label) row.lookup(".message-body");
            check(body.getBoundsInParent().getMaxX() <= bubble.getLayoutBounds().getWidth() + 1,
                    "Message text must wrap inside its bubble");
        }
    }

    /** Checks that the dot belongs to the first line and counts begin under the status text. */
    private void verifyHeaderAlignment(Parent root) {
        Node dot = root.lookup(".status-dot");
        Node status = root.lookup(".status-text");
        Node counts = root.lookup(".task-stats");
        Bounds dotBounds = dot.localToScene(dot.getLayoutBounds());
        Bounds statusBounds = status.localToScene(status.getLayoutBounds());
        Bounds countBounds = counts.localToScene(counts.getLayoutBounds());
        check(Math.abs(dotBounds.getCenterY() - statusBounds.getCenterY()) < 1,
                "The status dot must be vertically centered on the status heading");
        check(Math.abs(statusBounds.getMinX() - countBounds.getMinX()) < 1,
                "Status and quest counts must share a left edge");
        check(countBounds.getMinY() >= statusBounds.getMaxY() + 2,
                "Quest counts must have a clear gap below the status heading");
    }

    private void send(String command) {
        commandField().setText(command);
        ((Button) stage.getScene().lookup(".send-button")).fire();
    }

    private TextField commandField() {
        return (TextField) stage.getScene().lookup(".command-field");
    }

    private String lastReply() {
        ScrollPane chat = (ScrollPane) stage.getScene().lookup(".chat-scroll");
        List<Node> messages = ((Parent) chat.getContent()).getChildrenUnmodifiable();
        return ((Label) messages.getLast().lookup(".message-body")).getText();
    }

    private List<Task> savedTasks() throws StorageException {
        return new Storage(SaveLocation.getDefaultFile()).loadTasks();
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
