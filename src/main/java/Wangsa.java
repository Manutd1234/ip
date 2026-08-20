import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * Entry point for the Wangsa chatbot.
 */
public class Wangsa {
    private static final String SEPARATOR = "____________________________________________________________";
    private static final String BANNER = "Wangsa";
    private static final Path DATA_FILE_PATH = Path.of("data", "wangsa.txt");

    public static void main(String[] args) {
        System.out.println(SEPARATOR);
        System.out.println(BANNER);
        System.out.println("Hello! I'm Wangsa.");
        System.out.println("What can I do for you?");
        System.out.println(SEPARATOR);

        Storage storage = new Storage(DATA_FILE_PATH);
        TaskList tasks;
        try {
            tasks = new TaskList(storage.loadTasks());
        } catch (StorageException | WangsaException exception) {
            System.out.println(exception.getMessage());
            System.out.println(SEPARATOR);
            return;
        }

        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLine()) {
                String command = scanner.nextLine();
                System.out.println(SEPARATOR);

                if (command.equals("bye")) {
                    System.out.println("Bye. Hope to see you again soon!");
                    System.out.println(SEPARATOR);
                    break;
                }

                try {
                    if (command.equals("list")) {
                        printTaskList(tasks);
                    } else if (isStatusCommand(command)) {
                        Task updatedTask = updateTaskStatus(command, tasks);
                        storage.saveTasks(tasks.getTasks());
                        printStatusUpdate(command, updatedTask);
                    } else if (isDeleteCommand(command)) {
                        Task removedTask = deleteTask(command, tasks);
                        storage.saveTasks(tasks.getTasks());
                        printDeletion(removedTask, tasks.size());
                    } else if (isTaskCommand(command)) {
                        Task task = createTask(command);
                        tasks.add(task);
                        storage.saveTasks(tasks.getTasks());
                        System.out.println("Got it. I've added this task:");
                        System.out.println("  " + task);
                        System.out.println("Now you have " + tasks.size() + " tasks in the list.");
                    } else if (command.isEmpty()) {
                        throw new WangsaException("OOPS!!! Please enter a command.");
                    } else {
                        throw new WangsaException("OOPS!!! I'm sorry, but I don't know what that means :-(");
                    }
                } catch (WangsaException exception) {
                    System.out.println(exception.getMessage());
                } catch (StorageException exception) {
                    System.out.println(exception.getMessage());
                    System.out.println(SEPARATOR);
                    return;
                }

                System.out.println(SEPARATOR);
            }
        }
    }

    /** Returns whether the command is a valid task-status command prefix. */
    private static boolean isStatusCommand(String command) {
        return command.equals("mark") || command.startsWith("mark ")
                || command.equals("unmark") || command.startsWith("unmark ");
    }

    /** Returns whether the command is a delete command. */
    private static boolean isDeleteCommand(String command) {
        return command.equals("delete") || command.startsWith("delete ");
    }

    /** Returns whether the command is a supported task-creation command prefix. */
    private static boolean isTaskCommand(String command) {
        return command.equals("todo") || command.startsWith("todo ")
                || command.equals("deadline") || command.startsWith("deadline ")
                || command.equals("event") || command.startsWith("event ");
    }

    /** Creates the appropriate task subtype or reports malformed task input. */
    private static Task createTask(String command) throws WangsaException {
        if (command.equals("todo") || command.startsWith("todo ")) {
            String description = textAfterKeyword(command, "todo");
            if (description.isEmpty()) {
                throw new WangsaException("OOPS!!! The description of a todo cannot be empty.");
            }
            return new Todo(description);
        }

        if (command.equals("deadline") || command.startsWith("deadline ")) {
            String content = textAfterKeyword(command, "deadline");
            int byMarker = content.indexOf(" /by");
            if (byMarker < 0) {
                throw new WangsaException("OOPS!!! A deadline must include a description and a /by date.");
            }
            String description = content.substring(0, byMarker).trim();
            String by = content.substring(byMarker + " /by".length()).trim();
            if (description.isEmpty()) {
                throw new WangsaException("OOPS!!! The description of a deadline cannot be empty.");
            }
            if (by.isEmpty()) {
                throw new WangsaException("OOPS!!! A deadline needs a value after /by.");
            }
            try {
                return new Deadline(description, LocalDate.parse(by));
            } catch (DateTimeParseException exception) {
                throw new WangsaException("OOPS!!! Deadline date must be valid and use yyyy-MM-dd format "
                        + "(e.g., 2019-10-15).");
            }
        }

        String content = textAfterKeyword(command, "event");
        int fromMarker = content.indexOf(" /from");
        int toMarker = fromMarker < 0 ? -1 : content.indexOf(" /to", fromMarker + " /from".length());
        if (fromMarker < 0 || toMarker < 0) {
            throw new WangsaException("OOPS!!! An event must include a description, /from start, and /to end.");
        }
        String description = content.substring(0, fromMarker).trim();
        String from = content.substring(fromMarker + " /from".length(), toMarker).trim();
        String to = content.substring(toMarker + " /to".length()).trim();
        if (description.isEmpty()) {
            throw new WangsaException("OOPS!!! The description of an event cannot be empty.");
        }
        if (from.isEmpty() || to.isEmpty()) {
            throw new WangsaException("OOPS!!! An event needs values after /from and /to.");
        }
        return new Event(description, from, to);
    }

    /** Returns the trimmed text after a command keyword. */
    private static String textAfterKeyword(String command, String keyword) {
        return command.length() == keyword.length()
                ? ""
                : command.substring(keyword.length()).trim();
    }

    /** Prints all stored tasks in their current order and status. */
    private static void printTaskList(TaskList taskList) {
        List<Task> tasks = taskList.getTasks();
        System.out.println("Here are the tasks in your list:");
        for (int i = 0; i < tasks.size(); i++) {
            System.out.println((i + 1) + "." + tasks.get(i));
        }
    }

    /** Updates and returns a task, or reports an invalid task number. */
    private static Task updateTaskStatus(String command, TaskList tasks)
            throws WangsaException {
        String[] parts = command.split("\\s+");
        if (parts.length != 2) {
            throw new WangsaException("OOPS!!! " + parts[0] + " expects one task number.");
        }

        int taskNumber;
        try {
            taskNumber = Integer.parseInt(parts[1]);
        } catch (NumberFormatException exception) {
            throw new WangsaException("OOPS!!! Task number must be a whole number.");
        }

        if (parts[0].equals("mark")) {
            return tasks.mark(taskNumber);
        }
        return tasks.unmark(taskNumber);
    }

    /** Confirms a successful task-status update after it has been saved. */
    private static void printStatusUpdate(String command, Task task) {
        if (command.startsWith("mark")) {
            System.out.println("Nice! I've marked this task as done:");
        } else {
            System.out.println("OK, I've marked this task as not done yet:");
        }
        System.out.println("  " + task);
    }

    /** Deletes and returns a numbered task. */
    private static Task deleteTask(String command, TaskList tasks)
            throws WangsaException {
        String[] parts = command.split("\\s+");
        if (parts.length != 2) {
            throw new WangsaException("OOPS!!! delete expects one task number.");
        }

        int taskNumber;
        try {
            taskNumber = Integer.parseInt(parts[1]);
        } catch (NumberFormatException exception) {
            throw new WangsaException("OOPS!!! Task number must be a whole number.");
        }

        return tasks.delete(taskNumber);
    }

    /** Confirms a successful deletion after the updated task list has been saved. */
    private static void printDeletion(Task removedTask, int taskCount) {
        System.out.println("Noted. I've removed this task:");
        System.out.println("  " + removedTask);
        System.out.println("Now you have " + taskCount + " tasks in the list.");
    }
}
