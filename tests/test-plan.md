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

## C-Sort acceptance checks

1. Add deadlines with different dates, a todo, and an event.
2. Run `sort`.
3. Confirm deadlines appear from earliest to latest.
4. Confirm undated tasks appear after deadlines and retain their relative order.
5. Restart Wangsa and confirm the sorted order is restored from `data/wangsa.db`.
