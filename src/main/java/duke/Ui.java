package duke;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

/**
 * Handles all console input and output for Wangsa.
 */
public class Ui implements AutoCloseable {
    private static final String SEPARATOR = "____________________________________________________________";
    private static final String BANNER = "Wangsa";

    private final Scanner scanner;
    private final PrintStream output;

    /** Creates a UI connected to the process's standard input and output. */
    public Ui() {
        this(System.in, System.out);
    }

    /**
     * Creates a UI connected to the supplied streams.
     *
     * @param input source of user commands
     * @param output destination for chatbot messages
     */
    public Ui(InputStream input, PrintStream output) {
        this.scanner = new Scanner(input);
        this.output = output;
    }

    /** Displays the greeting shown when Wangsa starts. */
    public void showWelcome() {
        showLine();
        output.println(BANNER);
        output.println("Hello! I'm Wangsa.");
        output.println("What can I do for you?");
        showLine();
    }

    /** Returns whether another command can be read.
     * @return whether another command is available
     */
    public boolean hasNextCommand() {
        return scanner.hasNextLine();
    }

    /** Reads and returns the next complete user command.
     * @return the command text
     */
    public String readCommand() {
        return scanner.nextLine();
    }

    /** Displays the separator used around each chatbot response. */
    public void showLine() {
        output.println(SEPARATOR);
    }

    /** Displays Wangsa's farewell. */
    public void showGoodbye() {
        output.println("Bye. Hope to see you again soon!");
    }

    /**
     * Displays all tasks in their current order.
     * @param tasks tasks to display
     */
    public void showTaskList(List<Task> tasks) {
        output.println("Here are the tasks in your list:");
        for (int i = 0; i < tasks.size(); i++) {
            output.println((i + 1) + "." + tasks.get(i));
        }
    }

    /**
     * Displays confirmation that a task has been added and saved.
     * @param task added task
     * @param taskCount resulting task count
     */
    public void showTaskAdded(Task task, int taskCount) {
        output.println("Got it. I've added this task:");
        output.println("  " + task);
        output.println("Now you have " + taskCount + " tasks in the list.");
    }

    /**
     * Displays confirmation that a task's completion status has been saved.
     * @param task updated task
     * @param isMarked whether the task is now complete
     */
    public void showTaskStatusUpdate(Task task, boolean isMarked) {
        if (isMarked) {
            output.println("Nice! I've marked this task as done:");
        } else {
            output.println("OK, I've marked this task as not done yet:");
        }
        output.println("  " + task);
    }

    /**
     * Displays confirmation that a task has been deleted and the change saved.
     * @param task removed task
     * @param taskCount resulting task count
     */
    public void showTaskDeleted(Task task, int taskCount) {
        output.println("Noted. I've removed this task:");
        output.println("  " + task);
        output.println("Now you have " + taskCount + " tasks in the list.");
    }

    /**
     * Displays an error that Wangsa can explain to the user.
     * @param message user-facing error
     */
    public void showError(String message) {
        output.println(message);
    }

    /** Releases the scanner used to read commands. */
    @Override
    public void close() {
        scanner.close();
    }
}
