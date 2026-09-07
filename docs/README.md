# Wangsa User Guide

Wangsa is a Pokémon-inspired personal assistant for keeping a small task list. It
supports a JavaFX desktop interface and a command-line interface backed by the same
task service and SQLite database. Use the GUI for normal interaction; the CLI is
useful for terminal workflows and automated tests.

![Wangsa desktop interface](Ui.png)

## Quick start

Install Java 25, then run the desktop app from the project root:

```shell
./gradlew run
```

Click the command box, type a command, and press Enter or click **SEND**. The
bottom command cheatsheet and suggestion chips provide quick reminders. Press the
Up and Down arrow keys to reuse successful commands from the current session.

To run the terminal interface instead, use:

```shell
./gradlew runCli
```

For most users, `./gradlew run` is the recommended launch method. Gradle selects
the matching JavaFX libraries automatically for the desktop app and the SQLite
dependency automatically for the CLI.

## Commands

Task numbers are one-based and refer to the order currently shown by `list`. Text
inside angle brackets is a value that you replace; do not type the angle brackets.

| Command | Example | Purpose |
| --- | --- | --- |
| `todo <description>` | `todo read the project brief` | Add a task without a date. |
| `deadline <description> /by <date>` | `deadline submit report /by 2026-09-20` | Add a task with a deadline. |
| `event <description> /from <start> /to <end>` | `event project meeting /from Monday 2pm /to Monday 4pm` | Add a task with start and end text. |
| `list` | `list` | Show all tasks in their current order. |
| `find <keyword>` | `find project` | Find descriptions containing the keyword, ignoring case. |
| `sort` | `sort` | Put deadline tasks first in chronological order, then undated tasks. |
| `mark <number>` | `mark 2` | Mark a task as complete. |
| `unmark <number>` | `unmark 2` | Reopen a completed task. |
| `delete <number>` | `delete 2` | Remove a task permanently from the task list. |
| `bye` | `bye` | Save state and close the application. |

### Dates and task details

Deadline dates must be valid and use `yyyy-MM-dd`, such as `2026-09-20`. Wangsa
displays them in a friendlier form such as `Sep 20 2026`. Event start and end
values are kept as text, so values such as `Monday 2pm`, `09:00`, or `after lunch`
are valid as long as both `/from` and `/to` are supplied.

The `sort` command is stable: tasks with the same deadline keep their relative
order, and todo/event tasks keep their relative order after all deadlines.

## Using the desktop interface

The conversation area shows Wangsa's responses on the left and your commands on
the right. The header reports the number of saved tasks and completed tasks. The
interface supports the same commands as the CLI, so a task added in one interface
appears in the other after the next launch.

The **TRY** buttons insert common command prefixes into the input box. `list`
executes immediately; `new todo`, `find`, and `mark #` leave the remaining value
for you to complete. Invalid commands are shown as an explanatory response and do
not change the task list.

## Saving, migration, and recovery

Wangsa creates `data/wangsa.db` automatically and saves every successful add,
mark/unmark, delete, and sort operation. Task type, description, deadline or event
details, completion status, and display order are restored on the next launch.

If an old `data/wangsa.txt` file exists when the database is first created, Wangsa
imports it once. After that first database-backed load, SQLite is the source of
truth; deleting tasks later will not cause the legacy file to be imported again.

Writes use short SQLite transactions. If a write fails, Wangsa reports an error and
keeps the in-memory list consistent with the last successful save. Do not edit the
database manually. If saved data is malformed, Wangsa reports the problem instead
of silently discarding or guessing task details.

## Common errors

- **“Please enter a command.”** Enter one of the commands above.
- **Missing description.** Add text after `todo`, `deadline`, or `event`.
- **Invalid deadline.** Check that the date exists and uses `yyyy-MM-dd`.
- **Missing event values.** Supply both `/from <start>` and `/to <end>`.
- **Invalid task number.** Run `list` and use a number from the displayed range.
- **Task list is full.** Wangsa supports a maximum of 100 tasks; delete an old task
  before adding another.
- **Saved-data error.** Check that the application can read and write the `data`
  folder, then restart Wangsa. The original error text identifies the failing
  operation.

## Building and verifying

Run these commands from the project root:

```shell
./gradlew clean test
./gradlew checkstyleMain checkstyleTest
./gradlew clean check
./gradlew jar
java --enable-native-access=ALL-UNNAMED -jar build/libs/Wangsa.jar
```

The executable JAR bundles the platform-specific JavaFX runtime selected by Gradle.
Run it on a matching Java 25 environment; no separate JavaFX installation is
needed.

See [ARCHITECTURE.md](ARCHITECTURE.md) for the code structure and
[../tests/test-plan.md](../tests/test-plan.md) for the verification plan.
