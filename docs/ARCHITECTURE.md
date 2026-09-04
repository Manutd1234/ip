# Wangsa Architecture

Wangsa keeps user-interface code separate from task-management code so the CLI and JavaFX interfaces can share the same behavior.

## Responsibilities

```text
User input
    |
    +--> duke.Wangsa      (CLI presentation and command loop)
    |
    +--> duke.gui.Main    (JavaFX controls and rendering)
             |
             v
       duke.Parser        (command syntax and validation)
             |
             v
       duke.TaskService   (application workflows)
          |          |
          v          v
  duke.TaskList    duke.TaskRepository
  (domain state)   (persistence contract)
                         |
                         v
             duke.SqliteTaskRepository
             (transactional SQLite database)
```

- `Task`, `Todo`, `Deadline`, `Event`, and `TaskType` model task data and display behavior.
- `TaskList` owns ordering, capacity, search, and task mutations.
- `Parser` translates command text into validated command types and task values.
- `TaskService` coordinates a complete use case, such as adding a task and saving it.
- `TaskRepository` defines persistence without committing the application to a storage format.
- `SqliteTaskRepository` implements `TaskRepository` using indexed, transactional SQLite rows.
- SQLite uses WAL mode for reader/writer concurrency and immediate write transactions so position checks and
  updates acquire the writer lock together. Unique position indexes and task-type checks protect ordering and
  task-specific fields at the database boundary, including for databases created by earlier versions.
- `Storage` remains the legacy text-file reader used for one-time migration from `data/wangsa.txt`.
- `Ui` and `duke.gui.Main` format output for their respective interfaces.

The `C-Sort` extension follows the same flow as other commands: `Parser` recognizes
`sort`, `TaskService` persists the reordered snapshot, and each interface renders
the resulting task order. `TaskList` keeps the sorting rule close to the task data
structure so both interfaces behave identically.

## Adding a feature

1. Add or update the domain model in the task classes when the feature introduces new task data or behavior.
2. Add command syntax and validation in `Parser`.
3. Add the state-changing workflow in `TaskService`; save mutations through `TaskRepository`.
4. Add focused unit tests beside the affected class. Prefer testing `TaskService` and the domain classes instead of JavaFX controls.
5. Add CLI output in `Ui` and JavaFX output in `duke.gui.Main` only after the shared workflow works.
6. Update the user guide and this architecture guide when the public command set or a layer responsibility changes.

## Extension points

- A new persistence backend can implement `TaskRepository` without changing `TaskService`.
- The SQLite repository keeps schema creation and transaction handling in one adapter, so a future
  remote database can replace it without leaking JDBC details into the domain or interfaces.
- Schema initialization is idempotent: it adds missing indexes and validation triggers whenever an existing
  database is opened, while malformed legacy rows are reported instead of being silently changed.
- A new interface can construct a `TaskService` and reuse the existing parser and workflows.
- A new task type should extend `Task`, define its own details, and be handled by `Parser` and
  `SqliteTaskRepository` for creation and persistence.

Keep commits focused by separating domain changes, service changes, interface changes, tests, and documentation where practical.
