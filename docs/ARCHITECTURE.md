# Wangsa architecture

The desktop and terminal interfaces use the same task logic. A command is parsed
once, passed to the task service, saved if it changes data, and then shown to the user.

## Main classes

| Classes | Responsibility |
| --- | --- |
| `gui.Main`, `Wangsa`, `Ui` | Desktop controls and terminal input/output |
| `Parser`, `Command` | Check command syntax and carry the parsed arguments |
| `TaskService` | Carry out commands and coordinate saving |
| `TaskList`, `TaskMatch` | Manage order, capacity, search, and task numbers |
| `Task`, `Todo`, `Deadline`, `Event`, `TaskType` | Represent tasks and their details |
| `TaskRepository` | Define the storage operations |
| `Storage` | Read, validate, and save tasks in a plain text file |
| `SaveLocation` | Keep downloaded users' saves beside the JAR |
| `gui.ChatMessage`, `gui.CharacterAvatar`, `gui.QuestIcon`, `gui.TaskFormatter` | Render messages, artwork, and task lists |
| `CommandHelp`, `AiHelper` | Provide built-in help and optional AI explanations |
| `gui.Launcher`, `gui.NativeLibraries` | Select the JavaFX native libraries before opening the window |

## Task changes and saving

`TaskList` holds up to 100 valid tasks. Search results keep the numbers used by
the full list. Sorting puts deadlines first and keeps equal-date or undated
tasks in their previous relative order.

`TaskService` reports success only after saving. If an add, mark, unmark, or sort
cannot be saved, it restores the previous in-memory state. A delete is saved
before it is applied to memory.

`Storage` writes the complete list to a temporary UTF-8 file in the save folder,
then replaces `wangsa.txt`. It uses an atomic move where supported, with a regular
replacement as a fallback. Each line records the type, completion flag, description,
and any date or event times. Separators, backslashes, and line breaks are escaped.

Loading validates every line and the 100-task limit. Invalid data is reported
with its line number and file path; it is not silently discarded. The desktop
keeps `help` and `bye` available if loading fails. The terminal exits.

The packaged app saves under its own folder, regardless of the terminal's current
folder. Source-code runs save under the project folder. Old database files are
not read, changed, or deleted.

## Optional AI help

`CommandHelp` holds the command reference used by offline help and AI prompts.
`AiHelper` sends only that reference and the current question to Groq. It cannot
access saved tasks or execute commands, and it keeps no conversation history.

The desktop runs one request at a time in a background thread; task commands
remain usable. The terminal waits for the response. Requests have a 20-second
timeout and no retries. Missing configuration, empty replies, or provider errors
return offline help. Raw provider errors are not displayed because they may
contain request details.

Tests supply a fake model, so they need no API key or network connection.

## Adding a feature

1. Update the task model if new data is needed.
2. Add syntax and validation in `Parser`, with a matching `Command` type.
3. Put shared behaviour in `TaskService`; use `TaskRepository` for storage.
4. Add tests for valid input, invalid input, and failed saving.
5. Add desktop and terminal output, then update the user guide.

A different storage backend can implement `TaskRepository` without changing the
task service. A new task type should extend `Task` and be supported by the parser
and storage adapter.
