# Wangsa User Guide

Wangsa helps you keep track of todos, deadlines, and events. Type a short command
to add a task, find it, or mark it done. Changes are saved automatically.

[Quick start](#quick-start) · [Commands](#features) ·
[AI help](#optional-ai-command-help-ai) · [Saving tasks](#saving-tasks) ·
[Troubleshooting](#troubleshooting) · [Command summary](#command-summary)

## Quick start

1. Install **[Java 25](https://adoptium.net/temurin/releases/?version=25)** if needed.
   Run `java -version` in a terminal to check.
2. Download `Wangsa.zip` from the [latest release](https://github.com/Manutd1234/ip/releases/latest)
   and extract it into a folder you own, such as **Documents/Wangsa**.
   On Windows, right-click the ZIP and choose **Extract All**.
3. Open the extracted folder. On Windows, double-click **Start-Wangsa.bat**.
   On Mac, double-click **Start-Wangsa.command**. On Linux, open a terminal in
   that folder and run `java -jar Wangsa.jar`.
4. Click the command box at the bottom. Type one complete command, then press
   **Enter** or click **SEND**. Do not type commands into the conversation area.

Prefer just the JAR? Download `Wangsa.jar`, put it in a folder you own, and run:

```shell
java -jar Wangsa.jar
```

This terminal command also works if a start script does not open. Do not launch
from inside the ZIP. No database setup, account, or API key is needed.

The JAR supports Windows x64, Linux x64, and Intel/Apple Silicon Macs.
Linux needs a graphical desktop with GTK 3. You do not need to install JavaFX
separately or set up AI to manage tasks.

![Wangsa desktop with its task list and command box](Ui.png)

Messages are labelled **YOU** and **WANGSA**, with small arrow symbols to distinguish
the two sides. The app uses no external artwork or audio.

Try these commands one at a time, starting with an empty list:

```text
todo read the project brief
deadline submit report /by 2026-09-20
list
mark 1
```

You should have two tasks, with the first marked done.

To run from source, use `./gradlew run` for the desktop or `./gradlew runCli`
for the terminal interface. On Windows, use `gradlew.bat` instead of `./gradlew`.

## Features

### Before you start

- Use lowercase command names and markers. Type `list`, not `/list`.
  Your task description can use capitals, such as `ST2334`.
- Replace placeholders with your own text: `todo DESCRIPTION` becomes
  `todo read a book`. Do not type the angle brackets shown in the desktop hints.
- Put spaces around `/by`, `/from`, and `/to`. Use each required marker once,
  in the order shown. These markers are reserved in deadline and event commands.
- Extra spaces around a command are ignored. Spaces or tabs can separate its
  parts; spaces inside descriptions are kept. Required values cannot be blank.
- Task numbers start at 1. `list` and `find` use the same numbers.
  Sorting or deleting can change them, so check the current list.
- The limit is **100 tasks**, including completed ones. Duplicate tasks are allowed.

The desktop shows **[To do]** or **[Done]** beside each task. Due dates and event
times appear underneath, aligned with the task text. Use the number at the start of the task for
`mark`, `unmark`, or `delete`.

The optional terminal interface uses compact markers: `[T]` for todo, `[D]` for
deadline, `[E]` for event, `[ ]` for incomplete, and `[X]` for complete.

### Adding a todo: `todo`

**Format:** `todo DESCRIPTION`

Example: `todo read a book`

Adds a task without a date to the end of the list: `[To do] read a book`.

### Adding a deadline: `deadline`

**Format:** `deadline DESCRIPTION /by YYYY-MM-DD`

Example: `deadline submit report /by 2026-09-20`

Adds `[To do] submit report`, with **Due: 20 Sep 2026** underneath.

- Keep the slash in `/by`, with a space on each side. Plain `by` does not work.
- Use `YYYY-MM-DD`: a four-digit year, two-digit month, and two-digit day.
- The date must exist. September has 30 days, so `2026-09-31` is invalid.
- Past dates are allowed. Times are not supported.

For example, type `deadline ST2334 /by 2026-09-30` to add ST2334 due on 30 September.
If you see an error, no task was added. Enter the corrected command in the box
at the bottom; you do not need to delete anything first.

### Adding an event: `event`

**Format:** `event DESCRIPTION /from START /to END`

Example: `event project meeting /from Monday 2pm /to Monday 4pm`

Adds `[To do] project meeting`, with **From: Monday 2pm** and **To: Monday 4pm**
on separate lines.
Both start and end are required. They are stored as text: Wangsa does not check
their order or send reminders.

### Listing all tasks: `list`

`list` shows every task, including completed tasks, with its current number.

### Finding tasks: `find`

**Format:** `find KEYWORD`

Example: `find book` matches `read book`, `BOOK flight`, and `booking tickets`.

Search ignores case and checks descriptions only, not dates or event times.
Several words form one phrase: `find read book` looks for that exact phrase.
Wangsa tells you if nothing matches.

Results keep their full-list numbers. If the only result is numbered 4, use
`mark 4`, not `mark 1`.

### Sorting by deadline: `sort`

`sort` puts deadlines first, earliest to latest, then todos and events.
Tasks with equal dates keep their relative order, as do todos and events.
Completed tasks follow the same rule. The new order is saved and displayed.

### Completing a task: `mark`

**Format:** `mark NUMBER`

Example: `mark 2` changes task 2 from **[To do]** to **[Done]**.
The task stays in the list. Marking it again has no further effect.

### Reopening a task: `unmark`

**Format:** `unmark NUMBER`

Example: `unmark 2` changes task 2 from **[Done]** to **[To do]**.
Unmarking an incomplete task has no further effect.

### Deleting a task: `delete`

**Format:** `delete NUMBER`

Example: `delete 2` removes task 2. Later tasks move up one number.

**Deletion is immediate and has no undo.** Run `list` first if you are unsure.

### Exiting Wangsa: `bye`

`bye` closes the app. Successful changes are already saved.
Closing the window also keeps those changes.

### Built-in command help: `help`

`help` shows four groups: **Add tasks**, **View and find**, **Update tasks**, and
**Help and exit**. The desktop gives a short explanation and an example you can
type for each command. The terminal shows the command formats as bullet points.
Both work offline, without an API key.

### Optional AI command help: `@ai`

**Format:** `@ai QUESTION`

Example: `@ai How do I add a deadline for my report?`

The AI explains commands and suggests examples. It **cannot change tasks or run
commands**. Answers can be wrong, so check suggestions against `help`.
Questions must be 1–1000 characters long.

#### Setup

1. Create a Groq API key in the [Groq console](https://console.groq.com/keys).
2. Set `LLM_API_KEY` in the terminal used to launch Wangsa.

   macOS/Linux:

   ```shell
   export LLM_API_KEY='your-groq-api-key'
   java -jar Wangsa.jar
   ```

   Windows PowerShell:

   ```powershell
   $env:LLM_API_KEY='your-groq-api-key'
   java -jar Wangsa.jar
   ```

   In IntelliJ, add the variable under your run configuration's
   **Environment variables**. When running from source, launch Gradle from the
   same terminal. Keep your real key out of source code and shared files.
3. Restart Wangsa and ask an `@ai` question.

The default model is `openai/gpt-oss-20b`, hosted by Groq. You can set `LLM_MODEL`
to another [supported Groq model](https://console.groq.com/docs/models).
Access, usage limits, and charges depend on your Groq account.

#### Privacy and offline use

Only your question and the command reference are sent to Groq—not saved tasks
or previous messages. Each question is independent. Avoid putting sensitive
information in a question.

Without a key, or if the service fails, Wangsa shows **Offline help**.
Normal task commands still work. Requests have a 20-second timeout and no retries.

You can use task commands while the desktop is waiting for an answer, but only
one AI question can run at a time. The terminal waits for the answer before
processing another command. Closing the desktop cancels the request.

### Desktop shortcuts

- **list** shows your tasks immediately.
- **new todo**, **find**, **mark #**, and **ask AI** fill in a command for you to finish.
- **Up/Down** in the command box recall successful commands from this session.
- Resize the window or scroll to read longer conversations.
- Wangsa replies stay on the left and your messages stay on the right, even when maximized.
- Long replies open at their beginning. Scroll down to read the rest.
- Help groups stack into one column when there is less room.

The header shows total and completed task counts. Errors have a red border and
an **ACTION NEEDED** label. Tasks are saved, but the chat and command history
reset when the app closes.

## Saving tasks

Tasks save automatically to **`data/wangsa.txt` beside `Wangsa.jar`**.
The folder and file are created after your first change. There is no database
to install or set up. Open the folder containing the JAR to find your `data` folder.
The desktop and terminal share this file; run one instance at a time.

When running from source, the save file is in the project's `data` folder.

To back up your tasks, close Wangsa and copy the entire `data` folder.
To restore them, close Wangsa and replace its `data` folder with the backup.
Keep the JAR and `data` folder together when moving the app. To update Wangsa,
close it and replace only the JAR, keeping your existing `data` folder.

Earlier database versions used `wangsa.db`. This version leaves that file
untouched and does not import it. Keep a backup if it contains tasks you need
to transfer. Existing valid `wangsa.txt` files still work.

If saving fails, the change is cancelled. If loading fails, the desktop shows
**STORAGE UNAVAILABLE** and blocks task commands to protect the saved data.
`help` and `bye` still work. The message gives the file path and, for malformed
data, the line to check. Back up the file before correcting it, or restore a
backup, then restart. The terminal exits after a storage error.

## Troubleshooting

| Problem | What to try |
| --- | --- |
| Unknown command or missing value | Use lowercase commands without a leading slash. Follow the formats above. `list`, `sort`, `help`, and `bye` take no extra values. |
| Deadline needs a `/by` date | Include the slash: `deadline ST2334 /by 2026-09-30`, not `deadline ST2334 by 2026-09-30`. |
| Invalid date | Check the calendar as well as the format. `2026-09-31` does not exist; use `2026-09-30` if you mean the last day of September. Re-enter the whole corrected command. |
| Invalid task number | Run `list` and use a number shown there. Add a task first if the list is empty. |
| Task list is full | Delete a task before adding another. Completed tasks count towards the limit. |
| Tasks seem missing | Check the folder containing the JAR you opened. Keep your usual `data` folder beside that JAR. |
| Cannot load or save | Close other instances and check folder access. Back up the data before repairing it. |
| App will not open | Extract the ZIP first and check Java 25 with `java -version`. Run `java -jar Wangsa.jar` in a terminal to see the error. Linux also needs GTK 3 and a graphical desktop. |
| AI shows offline help | Check the Groq key, connection, model access, and usage limits. Restart after changing settings. `help` works without AI. |

## Command summary

| Action | Format | Example |
| --- | --- | --- |
| Add todo | `todo DESCRIPTION` | `todo read a book` |
| Add deadline | `deadline DESCRIPTION /by YYYY-MM-DD` | `deadline return book /by 2026-09-20` |
| Add event | `event DESCRIPTION /from START /to END` | `event lunch /from 12pm /to 1pm` |
| List | `list` | `list` |
| Find | `find KEYWORD` | `find book` |
| Sort | `sort` | `sort` |
| Complete | `mark NUMBER` | `mark 2` |
| Reopen | `unmark NUMBER` | `unmark 2` |
| Delete | `delete NUMBER` | `delete 2` |
| Built-in help | `help` | `help` |
| AI help | `@ai QUESTION` | `@ai How do I add a deadline?` |
| Exit | `bye` | `bye` |

## Credits

The guide structure draws on the
[AddressBook Level 3 User Guide](https://se-education.org/addressbook-level3/UserGuide.html).
Development and documentation were assisted by OpenAI Codex with GPT-6 Astra.
See [Credits](https://manutd1234.github.io/ip/CREDITS.html) for AI usage, code sources,
and libraries. The interface symbols are drawn in code, without external media.
