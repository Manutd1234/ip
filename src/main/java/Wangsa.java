import java.util.Scanner;

/**
 * Entry point for the Wangsa chatbot.
 */
public class Wangsa {
    private static final String SEPARATOR = "____________________________________________________________";
    private static final String BANNER = "Wangsa";

    public static void main(String[] args) {
        System.out.println(SEPARATOR);
        System.out.println(BANNER);
        System.out.println("Hello! I'm Wangsa.");
        System.out.println("What can I do for you?");
        System.out.println(SEPARATOR);

        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLine()) {
                String command = scanner.nextLine();
                System.out.println(SEPARATOR);

                if (command.equals("bye")) {
                    System.out.println("Bye. Hope to see you again soon!");
                    System.out.println(SEPARATOR);
                    break;
                }

                System.out.println(command);
                System.out.println(SEPARATOR);
            }
        }
    }
}
