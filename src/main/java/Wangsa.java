import java.util.Scanner;

/**
 * Entry point for the Wangsa chatbot.
 */
public class Wangsa {
    private static final int MAX_TASKS = 100;
    private static final String SEPARATOR = "____________________________________________________________";
    private static final String BANNER = "Wangsa";

    public static void main(String[] args) {
        System.out.println(SEPARATOR);
        System.out.println(BANNER);
        System.out.println("Hello! I'm Wangsa.");
        System.out.println("What can I do for you?");
        System.out.println(SEPARATOR);

        Task[] tasks = new Task[MAX_TASKS];
        int taskCount = 0;

        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLine()) {
                String command = scanner.nextLine();
                System.out.println(SEPARATOR);

                if (command.equals("bye")) {
                    System.out.println("Bye. Hope to see you again soon!");
                    System.out.println(SEPARATOR);
                    break;
                }

                if (command.equals("list")) {
                    printTaskList(tasks, taskCount);
                } else if (command.startsWith("mark ") || command.startsWith("unmark ")) {
                    updateTaskStatus(command, tasks, taskCount);
                } else if (taskCount < MAX_TASKS) {
                    Task task = createTask(command);
                    if (task == null) {
                        System.out.println("Sorry, I couldn't understand that task.");
                    } else {
                        tasks[taskCount] = task;
                        taskCount++;
                        System.out.println("Got it. I've added this task:");
                        System.out.println("  " + task);
                        System.out.println("Now you have " + taskCount + " tasks in the list.");
                    }
                } else {
                    System.out.println("Sorry, your task list is full.");
                }

                System.out.println(SEPARATOR);
            }
        }
    }

    /** Creates the appropriate task subtype from a command. */
    private static Task createTask(String command) {
        if (command.startsWith("todo ")) {
            return new Todo(command.substring("todo ".length()));
        }

        if (command.startsWith("deadline ")) {
            String content = command.substring("deadline ".length());
            int byMarker = content.indexOf(" /by ");
            if (byMarker < 0) {
                return null;
            }
            String description = content.substring(0, byMarker);
            String by = content.substring(byMarker + " /by ".length());
            return new Deadline(description, by);
        }

        if (command.startsWith("event ")) {
            String content = command.substring("event ".length());
            int fromMarker = content.indexOf(" /from ");
            int toMarker = content.indexOf(" /to ", fromMarker + " /from ".length());
            if (fromMarker < 0 || toMarker < 0) {
                return null;
            }
            String description = content.substring(0, fromMarker);
            String from = content.substring(fromMarker + " /from ".length(), toMarker);
            String to = content.substring(toMarker + " /to ".length());
            return new Event(description, from, to);
        }

        // Preserve Level-2 behavior: unrecognized text is a todo task.
        return new Todo(command);
    }

    /** Prints all stored tasks in their current order and status. */
    private static void printTaskList(Task[] tasks, int taskCount) {
        System.out.println("Here are the tasks in your list:");
        for (int i = 0; i < taskCount; i++) {
            System.out.println((i + 1) + "." + tasks[i]);
        }
    }

    /** Updates a task's completion status for a mark or unmark command. */
    private static void updateTaskStatus(String command, Task[] tasks, int taskCount) {
        String[] parts = command.split(" ", 2);
        int taskNumber;
        try {
            taskNumber = Integer.parseInt(parts[1]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException exception) {
            System.out.println("Sorry, that task number is invalid.");
            return;
        }

        if (taskNumber < 1 || taskNumber > taskCount) {
            System.out.println("Sorry, that task number is invalid.");
            return;
        }

        Task task = tasks[taskNumber - 1];
        if (parts[0].equals("mark")) {
            task.markAsDone();
            System.out.println("Nice! I've marked this task as done:");
        } else {
            task.markAsNotDone();
            System.out.println("OK, I've marked this task as not done yet:");
        }
        System.out.println("  " + task);
    }
}
