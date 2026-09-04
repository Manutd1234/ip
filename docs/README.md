# Wangsa User Guide

Wangsa is a Pokémon-inspired personal assistant for keeping a small task list. It
supports a command-line interface and a JavaFX desktop interface backed by the same
task service and save file.

## Adding tasks

Use one of these commands to create a task:

```text
todo read the project brief
deadline submit report /by 2026-09-20
event project meeting /from Monday 2pm /to Monday 4pm
```

Wangsa validates deadline dates in `yyyy-MM-dd` format and displays them as a
friendlier month-day-year value. Events retain the start and end text entered by the
user.

## Managing tasks

```text
list
find project
sort
mark 2
unmark 2
delete 2
```

The `find` command performs a case-insensitive search of task descriptions. The
`sort` extension orders deadline tasks from earliest to latest and places tasks
without deadlines after them, preserving the order of tasks with equal sort dates.
Task numbers are one-based and refer to the order shown by `list`.

## Saving tasks

Wangsa stores task data in `data/wangsa.txt` after every successful mutation. The
file is created automatically when the first task is added. Task type, description,
details, and completion status are restored on the next launch.

## Building and running

Run these commands from the project root:

```shell
./gradlew clean build
./gradlew run
./gradlew jar
java -jar build/libs/Wangsa.jar
```

The JAR bundles the platform-specific JavaFX runtime selected by Gradle, so it can
run on a matching Java 25 environment without a separate JavaFX installation.
