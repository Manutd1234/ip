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

        String[] tasks = new String[MAX_TASKS];
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
                    System.out.println("Here are the tasks in your list:");
                    for (int i = 0; i < taskCount; i++) {
                        System.out.println((i + 1) + ". " + tasks[i]);
                    }
                } else if (taskCount < MAX_TASKS) {
                    tasks[taskCount] = command;
                    taskCount++;
                    System.out.println("added: " + command);
                } else {
                    System.out.println("Sorry, your task list is full.");
                }

                System.out.println(SEPARATOR);
            }
        }
    }
}
