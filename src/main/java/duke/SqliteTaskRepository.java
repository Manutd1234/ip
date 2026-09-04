package duke;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists Wangsa tasks in a local SQLite database.
 *
 * <p>Each task is stored as one row with indexed ordering and deadline columns. Writes
 * use a transaction so an interrupted update cannot leave a partially written task
 * list. A legacy text file can be imported once when the database is first created.</p>
 */
public final class SqliteTaskRepository implements TaskRepository {
    private static final int QUERY_TIMEOUT_SECONDS = 30;

    private static final int BUSY_TIMEOUT_MILLISECONDS = 5_000;

    private static final String CREATE_TASKS_TABLE = "CREATE TABLE IF NOT EXISTS tasks ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "task_type TEXT NOT NULL CHECK (task_type IN ('T', 'D', 'E')), "
            + "is_done INTEGER NOT NULL CHECK (is_done IN (0, 1)), "
            + "description TEXT NOT NULL, "
            + "deadline TEXT, "
            + "event_from TEXT, "
            + "event_to TEXT, "
            + "position INTEGER NOT NULL CHECK (position >= 0)"
            + ")";

    private static final String CREATE_POSITION_INDEX = "CREATE INDEX IF NOT EXISTS idx_tasks_position "
            + "ON tasks(position)";

    private static final String CREATE_DEADLINE_INDEX = "CREATE INDEX IF NOT EXISTS idx_tasks_deadline "
            + "ON tasks(deadline, position)";

    private static final String COUNT_TASKS = "SELECT COUNT(*) FROM tasks";

    private static final String SELECT_TASKS = "SELECT task_type, is_done, description, deadline, "
            + "event_from, event_to FROM tasks ORDER BY position";

    private static final String DELETE_TASKS = "DELETE FROM tasks";

    private static final String INSERT_TASK = "INSERT INTO tasks "
            + "(task_type, is_done, description, deadline, event_from, event_to, position) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String SHIFT_POSITIONS_UP = "UPDATE tasks SET position = position + 1 "
            + "WHERE position >= ?";

    private static final String UPDATE_TASK = "UPDATE tasks SET task_type = ?, is_done = ?, description = ?, "
            + "deadline = ?, event_from = ?, event_to = ? WHERE position = ?";

    private static final String DELETE_TASK_AT_POSITION = "DELETE FROM tasks WHERE position = ?";

    private static final String SHIFT_POSITIONS_DOWN = "UPDATE tasks SET position = position - 1 "
            + "WHERE position > ?";

    private final Path databasePath;

    private final Path legacyFilePath;

    private boolean legacyMigrationPending;

    /**
     * Creates a repository backed by the supplied database path.
     *
     * <p>If the database is created beside a legacy {@code wangsa.txt} file, that file
     * is imported the first time the repository loads tasks.</p>
     *
     * @param databasePath SQLite database location
     */
    public SqliteTaskRepository(Path databasePath) {
        this(databasePath, databasePath.resolveSibling("wangsa.txt"));
    }

    /**
     * Creates a repository with an explicit legacy-file migration source.
     *
     * @param databasePath SQLite database location
     * @param legacyFilePath optional legacy text-file location
     */
    public SqliteTaskRepository(Path databasePath, Path legacyFilePath) {
        this.databasePath = databasePath;
        this.legacyFilePath = legacyFilePath;
        this.legacyMigrationPending = legacyFilePath != null
                && Files.notExists(databasePath)
                && Files.exists(legacyFilePath);
    }

    /**
     * Loads tasks in their saved order.
     *
     * @return saved tasks in their current order
     * @throws StorageException if the database cannot be opened or contains invalid data
     */
    @Override
    public List<Task> loadTasks() throws StorageException {
        try (Connection connection = openConnection()) {
            initializeSchema(connection);
            migrateLegacyTasksIfNeeded(connection);
            return readTasks(connection);
        } catch (SQLException exception) {
            throw databaseException("read", exception);
        }
    }

    /**
     * Replaces the saved task snapshot in one transaction.
     *
     * @param tasks tasks to persist in display order
     * @throws StorageException if the database cannot be updated
     */
    @Override
    public void saveTasks(List<Task> tasks) throws StorageException {
        try (Connection connection = openConnection()) {
            initializeSchema(connection);
            connection.setAutoCommit(false);
            try {
                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate(DELETE_TASKS);
                }
                insertTasks(connection, tasks);
                connection.commit();
                legacyMigrationPending = false;
            } catch (SQLException exception) {
                rollback(connection, exception);
                throw exception;
            }
        } catch (SQLException exception) {
            throw databaseException("save", exception);
        }
    }

    /**
     * Inserts one task without rewriting existing rows.
     *
     * @param task task to insert
     * @param position zero-based display position
     * @throws StorageException if the position or database update is invalid
     */
    @Override
    public void insertTask(Task task, int position) throws StorageException {
        try (Connection connection = openConnection()) {
            initializeSchema(connection);
            ensureInsertPosition(connection, position);
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement shift = connection.prepareStatement(SHIFT_POSITIONS_UP);
                        PreparedStatement insert = connection.prepareStatement(INSERT_TASK)) {
                    shift.setInt(1, position);
                    shift.executeUpdate();
                    bindTask(insert, task, position);
                    insert.executeUpdate();
                }
                connection.commit();
                legacyMigrationPending = false;
            } catch (SQLException exception) {
                rollback(connection, exception);
                throw exception;
            }
        } catch (SQLException exception) {
            throw databaseException("insert", exception);
        }
    }

    /**
     * Updates one task without rewriting unrelated rows.
     *
     * @param task replacement task data
     * @param position zero-based display position
     * @throws StorageException if the position or database update is invalid
     */
    @Override
    public void updateTask(Task task, int position) throws StorageException {
        try (Connection connection = openConnection()) {
            initializeSchema(connection);
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(UPDATE_TASK)) {
                bindTask(statement, task, position);
                if (statement.executeUpdate() != 1) {
                    throw invalidPosition(position);
                }
                connection.commit();
                legacyMigrationPending = false;
            } catch (SQLException | StorageException exception) {
                rollback(connection, exception);
                throw exception;
            }
        } catch (SQLException exception) {
            throw databaseException("update", exception);
        }
    }

    /**
     * Deletes one task and closes the resulting position gap in one transaction.
     *
     * @param position zero-based display position
     * @throws StorageException if the position or database update is invalid
     */
    @Override
    public void deleteTask(int position) throws StorageException {
        try (Connection connection = openConnection()) {
            initializeSchema(connection);
            connection.setAutoCommit(false);
            try (PreparedStatement delete = connection.prepareStatement(DELETE_TASK_AT_POSITION);
                    PreparedStatement shift = connection.prepareStatement(SHIFT_POSITIONS_DOWN)) {
                delete.setInt(1, position);
                if (delete.executeUpdate() != 1) {
                    throw invalidPosition(position);
                }
                shift.setInt(1, position);
                shift.executeUpdate();
                connection.commit();
                legacyMigrationPending = false;
            } catch (SQLException | StorageException exception) {
                rollback(connection, exception);
                throw exception;
            }
        } catch (SQLException exception) {
            throw databaseException("delete", exception);
        }
    }

    /** Opens a configured SQLite connection and creates its parent directory if needed. */
    private Connection openConnection() throws StorageException {
        Connection connection = null;
        try {
            Path absolutePath = databasePath.toAbsolutePath();
            Path parentDirectory = absolutePath.getParent();
            if (parentDirectory != null) {
                Files.createDirectories(parentDirectory);
            }
            connection = DriverManager.getConnection("jdbc:sqlite:" + absolutePath);
            configureConnection(connection);
            return connection;
        } catch (IOException | SQLException exception) {
            closeAfterFailedOpen(connection, exception);
            throw databaseException("open", exception);
        }
    }

    /** Configures SQLite for concurrent readers and short-lived writer contention. */
    private void configureConnection(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.setQueryTimeout(QUERY_TIMEOUT_SECONDS);
            statement.execute("PRAGMA busy_timeout = " + BUSY_TIMEOUT_MILLISECONDS);
            statement.execute("PRAGMA journal_mode = WAL");
            statement.execute("PRAGMA synchronous = NORMAL");
            statement.execute("PRAGMA foreign_keys = ON");
        }
    }

    /** Creates the current schema and indexes when the database is first opened. */
    private void initializeSchema(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.setQueryTimeout(QUERY_TIMEOUT_SECONDS);
            statement.executeUpdate(CREATE_TASKS_TABLE);
            statement.executeUpdate(CREATE_POSITION_INDEX);
            statement.executeUpdate(CREATE_DEADLINE_INDEX);
        }
    }

    /** Ensures that an insert position is within the current task range. */
    private void ensureInsertPosition(Connection connection, int position)
            throws SQLException, StorageException {
        try (Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(COUNT_TASKS)) {
            long taskCount = resultSet.next() ? resultSet.getLong(1) : 0;
            if (position < 0 || position > taskCount) {
                throw invalidPosition(position);
            }
        }
    }

    /** Creates a consistent error for an invalid zero-based task position. */
    private StorageException invalidPosition(int position) {
        return new StorageException("OOPS!!! Database task position " + position + " does not exist.");
    }

    /** Imports legacy text data only when this repository created a new database. */
    private void migrateLegacyTasksIfNeeded(Connection connection) throws SQLException, StorageException {
        if (!legacyMigrationPending || !isDatabaseEmpty(connection)) {
            return;
        }

        List<Task> legacyTasks = new Storage(legacyFilePath).loadTasks();
        connection.setAutoCommit(false);
        try {
            insertTasks(connection, legacyTasks);
            connection.commit();
            legacyMigrationPending = false;
        } catch (SQLException exception) {
            rollback(connection, exception);
            throw exception;
        }
    }

    /** Returns whether the database currently contains no task rows. */
    private boolean isDatabaseEmpty(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(COUNT_TASKS)) {
            return !resultSet.next() || resultSet.getLong(1) == 0;
        }
    }

    /** Reads and validates all rows in display order. */
    private List<Task> readTasks(Connection connection) throws SQLException, StorageException {
        List<Task> tasks = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(SELECT_TASKS);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                tasks.add(readTask(resultSet));
            }
        }
        return tasks;
    }

    /** Converts one database row back into its domain task. */
    private Task readTask(ResultSet resultSet) throws SQLException, StorageException {
        String type = resultSet.getString("task_type");
        String description = resultSet.getString("description");
        int status = resultSet.getInt("is_done");
        if (description == null || description.isEmpty() || (status != 0 && status != 1)) {
            throw invalidDatabaseRow("description cannot be empty and status must be 0 or 1");
        }

        Task task;
        switch (type) {
        case "T":
            task = new Todo(description);
            break;
        case "D":
            task = new Deadline(description, parseDeadline(resultSet.getString("deadline")));
            break;
        case "E":
            task = new Event(description, requireValue(resultSet.getString("event_from"), "event start"),
                    requireValue(resultSet.getString("event_to"), "event end"));
            break;
        default:
            throw invalidDatabaseRow("unknown task type");
        }

        if (status == 1) {
            task.markAsDone();
        }
        return task;
    }

    /** Parses the ISO date stored for a deadline. */
    private LocalDate parseDeadline(String value) throws StorageException {
        if (value == null || value.isEmpty()) {
            throw invalidDatabaseRow("deadline value cannot be empty");
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException exception) {
            throw new StorageException("OOPS!!! Database deadline must use yyyy-MM-dd format.", exception);
        }
    }

    /** Returns a required database text value or reports invalid data. */
    private String requireValue(String value, String fieldName) throws StorageException {
        if (value == null || value.isEmpty()) {
            throw invalidDatabaseRow(fieldName + " cannot be empty");
        }
        return value;
    }

    /** Inserts tasks in their current display order using a prepared batch. */
    private void insertTasks(Connection connection, List<Task> tasks) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_TASK)) {
            for (int position = 0; position < tasks.size(); position++) {
                bindTask(statement, tasks.get(position), position);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    /** Binds one domain task to the normalized task-row columns. */
    private void bindTask(PreparedStatement statement, Task task, int position) throws SQLException {
        statement.setString(1, task.getTypeIcon());
        statement.setInt(2, task.isDone() ? 1 : 0);
        statement.setString(3, task.getDescription());
        statement.setNull(4, Types.VARCHAR);
        statement.setNull(5, Types.VARCHAR);
        statement.setNull(6, Types.VARCHAR);
        if (task instanceof Deadline deadline) {
            statement.setString(4, deadline.getBy().toString());
        } else if (task instanceof Event event) {
            statement.setString(5, event.getFrom());
            statement.setString(6, event.getTo());
        }
        statement.setInt(7, position);
    }

    /** Rolls back a failed transaction while retaining the original exception. */
    private void rollback(Connection connection, Exception exception) {
        try {
            connection.rollback();
        } catch (SQLException rollbackException) {
            exception.addSuppressed(rollbackException);
        }
    }

    /** Closes a connection when configuration fails during opening. */
    private void closeAfterFailedOpen(Connection connection, Exception exception) {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (SQLException closeException) {
            exception.addSuppressed(closeException);
        }
    }

    /** Creates a consistent user-facing database error. */
    private StorageException databaseException(String operation, Exception exception) {
        return new StorageException("OOPS!!! I couldn't " + operation + " Wangsa's SQLite database at "
                + databasePath + ".", exception);
    }

    /** Creates a consistent invalid-row error. */
    private StorageException invalidDatabaseRow(String reason) {
        return new StorageException("OOPS!!! Database task data is invalid: " + reason + ".");
    }
}
