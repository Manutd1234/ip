# Wangsa

Wangsa is a Pokémon-inspired task manager with desktop and terminal interfaces.
Read the [User Guide](docs/README.md) for setup, commands, and recovery.
The sections below describe development setup.

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
   You now have 1 task in your list.
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

The [product website](https://manutd1234.github.io/ip/) renders the user guide
from `docs/README.md`. GitHub Pages publishes the `master` branch and `/docs`
folder, so branch changes appear there after merging and pushing.

## Building and running with Gradle

Use the Gradle wrapper from the project root:

```shell
./gradlew build
./gradlew run
./gradlew runCli
./gradlew jar
```

The wrapper uses Gradle 9.1.0, which is configured for the project's Java 25 toolchain.
The executable JAR is written to `build/libs/Wangsa.jar`; copy it to an empty
folder and run it with `java --enable-native-access=ALL-UNNAMED -jar Wangsa.jar`.

`./gradlew run` launches the JavaFX desktop interface. The original text
interface remains available through `./gradlew runCli` for command-line use and
automated tests.

## Optional AI command help

Use `help` for the built-in command reference, or `@ai How do I add a deadline?`
for an AI explanation. AI answers are read-only: suggested commands are never
executed automatically. The app works without AI configuration and falls back to
offline help if the service is unavailable.

To enable AI, set `LLM_API_KEY` to a Groq API key in your launch environment and
restart Wangsa. `LLM_MODEL` optionally overrides the default Groq-hosted
`openai/gpt-oss-20b` model. See the [AI setup instructions](docs/README.md#optional-ai-command-help-ai)
for terminal and IntelliJ setup, usage, and data sent to the provider.

This feature follows the read-only help approach in the
[SE-EDU AI integration tutorial](https://se-education.org/guides/tutorials/addingAiToJavaApp.html),
using LangChain4j's model adapter without agents, tools, or conversation memory.

## Persistence details

On the first run, Wangsa creates the `data` folder and SQLite database. Existing
`data/wangsa.txt` files are migrated once, then the database becomes the source of
truth. SQLite stores one indexed row per task and commits each update transactionally,
using WAL mode and immediate write transactions for predictable writer contention.
Unique task positions and task-type/detail constraints keep the saved records
consistent while retaining each task's type, description, details, and completion
status.

**Warning:** Keep the `src\main\java` folder as the root folder for Java files (i.e., don't rename those folders or move Java files to another folder outside of this folder path), as this is the default location some tools (e.g., Gradle) expect to find Java files.
