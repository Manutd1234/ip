# Wangsa User Guide

Wangsa is a Pokémon-inspired task manager for keeping track of your daily quests.
Type short commands to add tasks, find work, and track what you have finished.
Your tasks are saved automatically on this computer.

![The Wangsa desktop window, with a quest list and command box](Ui.png)

## Quick start

1. Install **Java 25**. Run `java -version` in a terminal to check your version.
2. Download the JAR from the [Wangsa releases page](https://github.com/Manutd1234/ip/releases).
   Use a build for your operating system and processor. The current build bundles
   platform-specific JavaFX libraries; a JAR built on another platform may not open.
3. Put `Wangsa.jar` in a folder where you can save files. Open a terminal in that
   folder and run:

   ```shell
   java --enable-native-access=ALL-UNNAMED -jar Wangsa.jar
   ```

4. Click the command box at the bottom of the window. Type a command and press
   **Enter**, or click **SEND**.

If you have the source code, run `./gradlew run` from the project folder instead
(`gradlew.bat run` on Windows). This builds with the JavaFX libraries for your
computer. The terminal interface is available with `./gradlew runCli`.

Try this sequence with an empty task list:

```text
todo read the project brief
deadline submit report /by 2026-09-20
event project meeting /from Monday 2pm /to Monday 4pm
list
mark 1
find report
```

The list now has three tasks. The first is complete, and `find report` shows task
**2**, which you can complete with `mark 2`.

## Reading and entering commands

- Commands and markers are **case-sensitive**: use `todo` and `/by`, not `TODO` or
  `/BY`. Search text is case-insensitive.
- Replace words such as `DESCRIPTION` and `NUMBER` with your own values. Do not
  type the capitalized placeholders or angle brackets from the on-screen hints.
- Leading and trailing spaces are ignored. Spaces or tabs can separate command
  keywords, markers, and values. Spaces inside descriptions are kept.
- `/by`, `/from`, and `/to` must be separate tokens, with whitespace before their
  values. Use each required marker once, in the order shown below. These tokens
  are reserved in deadline and event commands.
- Task numbers start at **1**. `list` and `find` use the same task numbers. After
  deleting or sorting, use the latest results because task numbers may change.

In a task such as `2.[D][X] submit report (by: Sep 20 2026)`, `2` is its task number,
`D` means deadline, and `X` means complete. `T` means todo, `E` means event, and a
blank status `[ ]` means incomplete. The GUI adds a space after the task number.

## Adding tasks

### Todo: `todo DESCRIPTION`

A todo is a task without a date.

```text
todo read the project brief
```

Wangsa adds `[T][ ] read the project brief` to the end of your list and confirms
the addition. Descriptions cannot be empty. Duplicate tasks are allowed.

### Deadline: `deadline DESCRIPTION /by YYYY-MM-DD`

Use a deadline for work due on a date.

```text
deadline submit report /by 2026-09-20
```

Wangsa adds `[D][ ] submit report (by: Sep 20 2026)`. Use a real calendar date, with
a four-digit year, two-digit month, and two-digit day; `2026-02-30` is rejected.
Past deadlines are allowed. Deadlines contain a date, without a time of day.

### Event: `event DESCRIPTION /from START /to END`

Use an event for an activity with a start and end.

```text
event project meeting /from Monday 2pm /to Monday 4pm
```

Wangsa adds `[E][ ] project meeting (from: Monday 2pm to: Monday 4pm)`.
The description, start, and end must all be non-empty. Start and end are **text**,
so Wangsa accepts phrases such as `after lunch` and does not check whether the
end comes after the start. Events do not trigger notifications or reminders.

## Viewing and finding tasks

### List: `list`

Shows all tasks, including completed tasks, in their current order. An empty GUI
list displays “Your quest log is clear.” Adding tasks appends them to this order.

### Find: `find KEYWORD`

```text
find report
```

Finds descriptions containing the supplied text, ignoring case. `find BOOK`
matches both `read book` and `return book`. Multiple words form one search phrase:
`find project meeting` looks for that phrase, not either word separately.
Dates and event start/end values are not searched.

Results keep their numbers from the full list. For example, if a matching task
is shown as `4`, use `mark 4` to complete it. Finding tasks does not reorder or
remove anything. A search with no matches displays a no-match message.

### Sort: `sort`

Puts deadlines first, from earliest to latest, followed by todos and events.
Deadlines on the same date retain their relative order; todos and events also
retain their relative order. Completed and incomplete tasks use the same rule.
The new order is saved, and the response shows the new task numbers.

## Completing and removing tasks

### Complete: `mark NUMBER`

```text
mark 2
```

Marks task 2 as complete, changing `[ ]` to `[X]`. The task stays in your list.
Marking an already completed task is allowed and keeps it complete.

### Reopen: `unmark NUMBER`

```text
unmark 2
```

Changes task 2 back to incomplete. You can also unmark an incomplete task.

### Delete: `delete NUMBER`

```text
delete 2
```

Permanently removes task 2 and saves the change immediately. There is no undo or
confirmation prompt. Later tasks move up one number. Run `list` first if you are
unsure which task a number refers to.

### Exit: `bye`

Closes Wangsa. Successful changes have already been saved; you do not need a
separate save command. Closing the desktop window also retains saved tasks.

## Desktop shortcuts

The **TRY** buttons sit below the command box. **list** runs immediately; **new
todo**, **find**, and **mark #** insert a prefix for you to finish. Press **Up** or
**Down** in the command box to browse successful commands from this session.
Consecutive identical commands appear only once in this history.

Your commands appear on the right and Wangsa's responses on the left. The header
counts total and completed tasks. The window can be resized; scroll the
conversation to see older replies. The transcript and command history are not
saved between launches.

## Saved tasks and recovery

Wangsa creates `data/wangsa.db` inside the folder **from which you launch it**.
Always launch from the same folder to use the same task list. The GUI and CLI
share that list when launched from the same folder; use one instance at a time.
Moving only the JAR does not move your existing tasks.

Every successful add, mark, unmark, delete, and sort is saved automatically.
If a save fails, the operation is not confirmed and the in-memory change is
rolled back. The CLI exits after a storage error; the GUI displays the error.

To back up your tasks, close Wangsa and copy the entire `data` folder somewhere
safe. To restore, close Wangsa and replace the `data` folder with your backup.
Avoid editing the SQLite database by hand.

If `data/wangsa.txt` from an older version exists before the first database is
created, Wangsa imports it once. The text file remains as a backup; later changes
use the database. An invalid legacy file reports a line number and can be
corrected before retrying the import.

If saved tasks cannot be loaded, the GUI displays **STORAGE UNAVAILABLE** and
blocks task commands to protect existing data. Fix the reported problem or
restore a backup, then restart. `bye` still works. The CLI reports the problem
and exits.

## Troubleshooting

| Problem | What to do |
| --- | --- |
| Unknown command | Use the exact lowercase command. `list`, `sort`, and `bye` take no extra arguments. |
| Missing description or event value | Supply a description and every required marker/value. |
| Invalid deadline date | Use a real date such as `2026-09-20`; include two digits for month and day. |
| Duplicate or misplaced markers | Use `/by` once for a deadline, or `/from` then `/to` once each for an event. |
| Invalid task number | Run `list`, then use one whole number from the displayed list. On an empty list, add a task first. |
| Task list is full | The limit is **100 tasks**, including completed tasks. Delete one before adding another. |
| Empty input | The GUI ignores blank submissions. The CLI asks you to enter a command. |
| Tasks seem to have disappeared | Check the launch folder and its `data` folder; avoid creating another list in a different folder. |
| Cannot read or save data | Close other Wangsa instances and check that the launch folder is writable. Back up existing data before recovery. |
| JAR will not start | Check `java -version` reports Java 25. Use a build matching your OS and processor, or run from source as described above. |

## Command summary

| Action | Format |
| --- | --- |
| Add todo | `todo DESCRIPTION` |
| Add deadline | `deadline DESCRIPTION /by YYYY-MM-DD` |
| Add event | `event DESCRIPTION /from START /to END` |
| List all tasks | `list` |
| Find descriptions | `find KEYWORD` |
| Sort by deadline | `sort` |
| Complete a task | `mark NUMBER` |
| Reopen a task | `unmark NUMBER` |
| Delete a task | `delete NUMBER` |
| Exit | `bye` |
