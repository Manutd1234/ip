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
1. After that, locate the `src/main/java/Wangsa.java` file, right-click it, and choose `Run Wangsa.main()` (if the code editor is showing compile errors, try restarting the IDE). If the setup is correct, you should see the following output, followed by an interactive prompt:
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

Tasks can be added with `todo DESCRIPTION`, `deadline DESCRIPTION /by DATE_OR_TIME`, or `event DESCRIPTION /from START /to END`. Use `list` to display tasks, `mark N` or `unmark N` to change completion status, `delete N` to remove a task, and `bye` to exit.

Wangsa automatically saves task-list changes to `data/wangsa.txt` and restores them the next time it starts.

On the first run, Wangsa creates the `data` folder and save file when the
first task is added. The path is relative to the project folder, so the same
commands work across operating systems. The saved records retain each task's
type, description, details, and completion status.

**Warning:** Keep the `src\main\java` folder as the root folder for Java files (i.e., don't rename those folders or move Java files to another folder outside of this folder path), as this is the default location some tools (e.g., Gradle) expect to find Java files.
