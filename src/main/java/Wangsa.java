import java.nio.file.Path;
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
        Parser parser = new Parser();
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

                try {
                    Parser.CommandType commandType = parser.parseCommandType(command);
                    if (commandType == Parser.CommandType.BYE) {
                        System.out.println("Bye. Hope to see you again soon!");
                        System.out.println(SEPARATOR);
                        return;
                    } else if (commandType == Parser.CommandType.LIST) {
                        printTaskList(tasks);
                    } else if (commandType == Parser.CommandType.MARK
                            || commandType == Parser.CommandType.UNMARK) {
                        int taskNumber = parser.parseTaskNumber(command);
                        boolean isMarked = commandType == Parser.CommandType.MARK;
                        Task updatedTask = isMarked ? tasks.mark(taskNumber) : tasks.unmark(taskNumber);
                        storage.saveTasks(tasks.getTasks());
                        printStatusUpdate(isMarked, updatedTask);
                    } else if (commandType == Parser.CommandType.DELETE) {
                        Task removedTask = tasks.delete(parser.parseTaskNumber(command));
                        storage.saveTasks(tasks.getTasks());
                        printDeletion(removedTask, tasks.size());
                    } else {
                        Task task = parser.parseTask(command);
                        tasks.add(task);
                        storage.saveTasks(tasks.getTasks());
                        System.out.println("Got it. I've added this task:");
                        System.out.println("  " + task);
                        System.out.println("Now you have " + tasks.size() + " tasks in the list.");
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

    /** Prints all stored tasks in their current order and status. */
    private static void printTaskList(TaskList taskList) {
        List<Task> tasks = taskList.getTasks();
        System.out.println("Here are the tasks in your list:");
        for (int i = 0; i < tasks.size(); i++) {
            System.out.println((i + 1) + "." + tasks.get(i));
        }
    }

    /** Confirms a successful task-status update after it has been saved. */
    private static void printStatusUpdate(boolean isMarked, Task task) {
        if (isMarked) {
            System.out.println("Nice! I've marked this task as done:");
        } else {
            System.out.println("OK, I've marked this task as not done yet:");
        }
        System.out.println("  " + task);
    }

    /** Confirms a successful deletion after the updated task list has been saved. */
    private static void printDeletion(Task removedTask, int taskCount) {
        System.out.println("Noted. I've removed this task:");
        System.out.println("  " + removedTask);
        System.out.println("Now you have " + taskCount + " tasks in the list.");
    }
}
