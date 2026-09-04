# Wangsa Test Plan

## Automated checks

- `./gradlew clean test` runs the JUnit tests with Java assertions enabled.
- `./gradlew checkstyleMain checkstyleTest` checks production and test source style.
- `./gradlew clean check` runs the complete local verification used by CI.

## C-Sort acceptance checks

1. Add deadlines with different dates, a todo, and an event.
2. Run `sort`.
3. Confirm deadlines appear from earliest to latest.
4. Confirm undated tasks appear after deadlines and retain their relative order.
5. Restart Wangsa and confirm the sorted order is restored from `data/wangsa.txt`.
