# Wangsa

## A small quest log for a busy day

Wangsa is a Pokémon-inspired task manager for people who want a quick, friendly
way to capture and review their work. It supports todo tasks, dated deadlines,
events, search, stable deadline sorting, completion tracking, and deletion.

The JavaFX desktop app presents these actions in a focused conversation view with
clear status counts, command suggestions, keyboard history, and an always-visible
command cheatsheet. A command-line interface is available for terminal users and
automated checks.

## Start using Wangsa

```shell
./gradlew run
```

Then try:

```text
todo read the project brief
deadline submit report /by 2026-09-20
list
mark 1
```

All successful changes are saved automatically to a local SQLite database and
restored on the next launch. Read the complete [User Guide](README.md) for every
command, GUI interaction, validation rule, and recovery detail.

## Why Wangsa is dependable

- The CLI and GUI share the same parser, task service, domain model, and persistence
  contract.
- Deadline and event data are retained, not flattened into display-only strings.
- SQLite transactions protect task order and saved state during updates.
- Input and saved-data errors are reported with actionable messages.

For contributors, [ARCHITECTURE.md](ARCHITECTURE.md) explains the design and
[../tests/test-plan.md](../tests/test-plan.md) describes the automated and manual
acceptance checks.
