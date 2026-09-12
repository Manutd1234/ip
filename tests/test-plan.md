# Wangsa Test Plan

## Automated checks

- `./gradlew clean test` runs the JUnit tests with Java assertions and Java 25 native access enabled.
- `./gradlew checkstyleMain checkstyleTest` checks production and test source style.
- `./gradlew clean check` runs the complete local verification used by CI.
- `./gradlew runCli` launches the terminal interface directly from Gradle.

## Persistence acceptance checks

1. Start Wangsa with no database and add a task; confirm `data/wangsa.db` is created.
2. If `data/wangsa.txt` exists, start Wangsa once and confirm its tasks appear in the database-backed list.
3. Delete all migrated tasks, restart Wangsa, and confirm the legacy file is not imported again.
4. Stop or interrupt a write and confirm the database contains either the old snapshot or the complete new snapshot.
5. Open an existing database and confirm task order remains stable and malformed task-specific fields are reported.
6. Try migrating a malformed legacy file; confirm no database is created. Correct
   the reported line, restart, and confirm the legacy tasks are imported.
7. Insert at the front and middle of a saved list through the repository, then
   delete earlier positions. Confirm ordering survives without unique-index errors.

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
   task commands are rejected, the original data remains intact, and `bye` exits.

## User guide acceptance checks

1. Follow the quick-start example in `docs/README.md` using an empty launch folder.
2. Confirm the full-window `docs/Ui.png` exists and both Markdown tables render.
3. Check that the Pages index includes the guide from `README.md` without copying it.
4. After merging and pushing, open the public Pages site and verify the guide,
   screenshot, links, and tables there. Local rendering does not replace this check.

## C-Sort acceptance checks

1. Add deadlines with different dates, a todo, and an event.
2. Run `sort`.
3. Confirm deadlines appear from earliest to latest.
4. Confirm undated tasks appear after deadlines and retain their relative order.
5. Restart Wangsa and confirm the sorted order is restored from `data/wangsa.db`.
