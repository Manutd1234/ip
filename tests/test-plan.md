# Wangsa test plan

Run manual checks in a disposable folder, not against your own saved tasks.
Use the [manual checklist](peer-smoke-test.md) for a quick test of the JAR.

## Automated checks

- `./gradlew clean test` runs the JUnit tests with Java assertions and Java 25 native access enabled.
- `./gradlew checkstyleMain checkstyleTest` checks production and test source style.
- Checkstyle also checks four-space switch indentation, consistent import order,
  and Javadoc formatting against the SE-EDU conventions.
- `./gradlew clean check` runs the complete local verification used by CI.
- `./gradlew shadowJar portableZip` builds the portable JAR and easy-start ZIP. Run
  `python3 tests/release_smoke.py build/libs/Wangsa.jar` to test it outside the source tree.
- `./gradlew runCli` launches the terminal interface directly from Gradle.

## Persistence acceptance checks

1. Start with no save file and add a task; confirm `data/wangsa.txt` is created beside the JAR.
2. Restart the JAR from a different working folder. Confirm the same tasks appear.
3. Delete all tasks and restart. Confirm the list stays empty.
4. Add todos, deadlines, and events containing Unicode, pipes, and backslashes.
   Confirm descriptions, times, order, and completion survive a restart.
5. Try an invalid status, date, escape sequence, or a file with 101 tasks.
   Confirm loading fails with a clear error and the original file is unchanged.
6. Make the save destination unwritable. Confirm an attempted change fails without
   changing the displayed list. Restore access and try again.
7. Confirm temporary save files are removed after successful and failed saves.
8. Place an old `wangsa.db` beside the app. Confirm it is not opened or changed.

## Download checks

1. Extract `Wangsa.zip` into a folder with spaces in its name.
2. On Windows, open `Start-Wangsa.bat`. On Mac, open `Start-Wangsa.command`.
   Confirm the app opens with Java 25 and no database setup.
3. Without Java on the path, confirm the script points to the quick-start instructions.
4. Confirm the ZIP contains the current JAR, instructions, and scripts, but no personal data.

## GUI acceptance checks

1. Run `./gradlew run` and confirm the window opens with the Wangsa header, task
   counts, conversation panel, command cheatsheet, and command input.
2. Add a todo, deadline, and event through the input field; confirm each response
   appears in the conversation and the header count updates.
3. Use `list`, `find`, `sort`, `mark`, `unmark`, and `delete`; confirm the GUI
   reflects the same behavior as the CLI.
4. Click the suggestion chips and use Up/Down history navigation; confirm commands
   are inserted or executed as described in the user guide.
5. Enter invalid commands and confirm an explanatory message appears without
   changing the task count.
6. Find a task that is not first in the full list. Confirm its displayed number
   marks that same task, and a search with no matches says so explicitly.
7. Start with malformed saved data. Confirm the header says STORAGE UNAVAILABLE,
   task commands are rejected, the original data remains intact, `help` works, and `bye` exits.
8. At narrow and normal window sizes, check avatar/bubble alignment, long-message
   wrapping, equal input/button heights, and the status dot beside its heading.

## User guide acceptance checks

1. Follow the quick-start example in `docs/README.md` using an empty launch folder.
2. Confirm the full-window `docs/Ui.png` exists and both Markdown tables render.
3. Check that the Pages index includes the guide from `README.md` without copying it.
4. After merging and pushing, open the public Pages site and verify the guide,
   screenshot, links, and tables there. Local rendering does not replace this check.

## Optional AI help acceptance checks

1. Unset `LLM_API_KEY`, start each interface, and enter `help` and then
   `@ai How do I add a deadline?`. Confirm the command reference appears and the
   latter explains setup with an Offline help label. Add and mark a task normally.
2. Enter `@ai` alone, an over-1000-character question, and `help extra`. Confirm
   validation messages appear and tasks remain unchanged.
3. With a Groq key configured, ask about deadlines, reopening tasks, and priorities.
   Confirm replies use the actual command syntax and describe priorities as unsupported.
   Responses vary, so review these examples manually before publishing a release.
4. Ask `@ai delete my tasks`. Confirm the response is only text and task counts
   and persisted records are unchanged.
5. While a desktop request is pending, enter `list`, then another `@ai` question.
   Confirm the window stays responsive, `list` works, and the second question is
   rejected until the first finishes. The answer should replace its own loading bubble.
6. Use an invalid key or disconnect from the network. Confirm failure produces
   offline help without showing request details, and normal commands still work.
7. Close the desktop during a request. Confirm the application exits promptly.
8. Check empty lists and single-task confirmations. Messages should suggest a useful
   next step and use "1 task" or "1 quest" rather than a plural.

Automated tests use fake models to check replies, failures, empty replies,
independent requests, offline fallback, and that AI output is not executed.
They do not check live answer quality or require a Groq key.

## C-Sort acceptance checks

1. Add deadlines with different dates, a todo, and an event.
2. Run `sort`.
3. Confirm deadlines appear from earliest to latest.
4. Confirm undated tasks appear after deadlines and retain their relative order.
5. Restart Wangsa and confirm the sorted order is restored from `data/wangsa.txt`.
