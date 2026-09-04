# Wangsa project template

This is a project template for a greenfield Java project. The chatbot is named _Wangsa_. Given below are instructions on how to use it.

## Setting up in Intellij

Prerequisites: JDK 25, update Intellij to the most recent version.

1. Open Intellij (if you are not in the welcome screen, click `File` > `Close Project` to close the existing project first)
1. Open the project into Intellij as follows:
   1. Click `Open`.
   1. Select the project directory, and click `OK`.
   1. If there are any further prompts, accept the defaults.
1. Configure the project to use **JDK 25** (not other versions) as explained in [here](https://www.jetbrains.com/help/idea/sdk.html#set-up-jdk).<br>
   In the same dialog, set the **Project language level** field to the `SDK default` option.
1. After that, locate the `src/main/java/duke/Wangsa.java` file, right-click it, and choose `Run 'duke.Wangsa.main()'` (if the code editor is showing compile errors, try restarting the IDE). If the setup is correct, you should see the following output, followed by an interactive prompt:
   ```
   ____________________________________________________________
   Wangsa
   Hello! I'm Wangsa.
   What can I do for you?
   ____________________________________________________________
   todo read book
   ____________________________________________________________
   Got it. I've added this task:
     [T][ ] read book
   Now you have 1 tasks in the list.
   ____________________________________________________________
   list
   ____________________________________________________________
   Here are the tasks in your list:
   1.[T][ ] read book
   ____________________________________________________________
   bye
   ____________________________________________________________
   Bye. Hope to see you again soon!
   ____________________________________________________________
   ```

Tasks can be added with `todo DESCRIPTION`, `deadline DESCRIPTION /by YYYY-MM-DD`, or `event DESCRIPTION /from START /to END`. Deadline dates are displayed in a friendlier form; for example, `2019-10-15` is shown as `Oct 15 2019`. Use `list` to display tasks, `find KEYWORD` to search, `sort` to order deadlines chronologically, `mark N` or `unmark N` to change completion status, `delete N` to remove a task, and `bye` to exit.

Wangsa automatically saves task-list changes to the SQLite database at `data/wangsa.db` and restores them the next time it starts. Existing `data/wangsa.txt` files are imported automatically on the first database startup.

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for the responsibilities of each layer and guidance for adding features.

## Building and running with Gradle

Use the Gradle wrapper from the project root:

```shell
./gradlew build
./gradlew run
./gradlew jar
```

The wrapper uses Gradle 9.1.0, which is configured for the project's Java 25 toolchain.
The executable JAR is written to `build/libs/Wangsa.jar`; copy it to an empty
folder and run it with `java --enable-native-access=ALL-UNNAMED -jar Wangsa.jar`.

`./gradlew run` launches the JavaFX desktop interface. The original text
interface remains available through `duke.Wangsa` for command-line use and
automated tests.

On the first run, Wangsa creates the `data` folder and SQLite database. Existing
`data/wangsa.txt` files are migrated once, then the database becomes the source of
truth. SQLite stores one indexed row per task and commits each update transactionally,
while the saved records retain each task's type, description, details, and completion
status.

**Warning:** Keep the `src\main\java` folder as the root folder for Java files (i.e., don't rename those folders or move Java files to another folder outside of this folder path), as this is the default location some tools (e.g., Gradle) expect to find Java files.
