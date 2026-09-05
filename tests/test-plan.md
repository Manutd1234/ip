# Wangsa Test Plan

## Automated checks

- `./gradlew clean test` runs the JUnit tests with Java assertions and Java 25 native access enabled.
- `./gradlew checkstyleMain checkstyleTest` checks production and test source style.
- `./gradlew clean check` runs the complete local verification used by CI.

## Persistence acceptance checks

1. Start Wangsa with no database and add a task; confirm `data/wangsa.db` is created.
2. If `data/wangsa.txt` exists, start Wangsa once and confirm its tasks appear in the database-backed list.
3. Delete all migrated tasks, restart Wangsa, and confirm the legacy file is not imported again.
4. Stop or interrupt a write and confirm the database contains either the old snapshot or the complete new snapshot.
5. Open an existing database and confirm task order remains stable and malformed task-specific fields are reported.

## C-Sort acceptance checks

1. Add deadlines with different dates, a todo, and an event.
2. Run `sort`.
3. Confirm deadlines appear from earliest to latest.
4. Confirm undated tasks appear after deadlines and retain their relative order.
5. Restart Wangsa and confirm the sorted order is restored from `data/wangsa.db`.
