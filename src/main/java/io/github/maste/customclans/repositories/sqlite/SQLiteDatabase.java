package io.github.maste.customclans.repositories.sqlite;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public final class SQLiteDatabase implements AutoCloseable {

    private final Path databasePath;
    private final Logger logger;
    private final ExecutorService executorService;

    public SQLiteDatabase(Path databasePath, Logger logger) {
        this.databasePath = databasePath;
        this.logger = logger;
        this.executorService = Executors.newSingleThreadExecutor(new DatabaseThreadFactory());
    }

    public void initialize() throws SQLException {
        try {
            Files.createDirectories(databasePath.getParent());
        } catch (Exception exception) {
            throw new SQLException("Unable to create database directory", exception);
        }

        try (Connection connection = openConnection(); Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS clans (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL,
                        tag TEXT NOT NULL,
                        tag_color TEXT NOT NULL,
                        president_uuid TEXT NOT NULL,
                        created_at INTEGER NOT NULL
                    )
                    """);
            statement.execute("CREATE UNIQUE INDEX IF NOT EXISTS idx_clans_name_unique ON clans(name COLLATE NOCASE)");

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS clan_members (
                        clan_id INTEGER NOT NULL,
                        player_uuid TEXT NOT NULL UNIQUE,
                        last_known_name TEXT NOT NULL,
                        role TEXT NOT NULL,
                        joined_at INTEGER NOT NULL,
                        FOREIGN KEY (clan_id) REFERENCES clans(id) ON DELETE CASCADE,
                        UNIQUE (clan_id, player_uuid)
                    )
                    """);
            statement.execute("CREATE INDEX IF NOT EXISTS idx_clan_members_clan_id ON clan_members(clan_id)");

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS clan_invites (
                        clan_id INTEGER NOT NULL,
                        invited_player_uuid TEXT NOT NULL UNIQUE,
                        invited_by_uuid TEXT NOT NULL,
                        expires_at INTEGER NOT NULL,
                        FOREIGN KEY (clan_id) REFERENCES clans(id) ON DELETE CASCADE
                    )
                    """);
            statement.execute("CREATE INDEX IF NOT EXISTS idx_clan_invites_expires_at ON clan_invites(expires_at)");
        }
    }

    public <T> CompletableFuture<T> supplyAsync(SQLSupplier<T> supplier) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return supplier.get();
            } catch (Exception exception) {
                throw new CompletionException(exception);
            }
        }, executorService);
    }

    public CompletableFuture<Void> runAsync(SQLRunnable runnable) {
        return supplyAsync(() -> {
            runnable.run();
            return null;
        });
    }

    public <T> CompletableFuture<T> transactionAsync(SQLTransaction<T> transaction) {
        return supplyAsync(() -> {
            try (Connection connection = openConnection()) {
                boolean previousAutoCommit = connection.getAutoCommit();
                connection.setAutoCommit(false);
                try {
                    T result = transaction.apply(connection);
                    connection.commit();
                    connection.setAutoCommit(previousAutoCommit);
                    return result;
                } catch (Exception exception) {
                    connection.rollback();
                    connection.setAutoCommit(previousAutoCommit);
                    throw exception;
                }
            }
        });
    }

    public Connection openConnection() throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath.toAbsolutePath());
        configureConnection(connection);
        return connection;
    }

    private void configureConnection(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys=ON");
            statement.execute("PRAGMA busy_timeout=5000");
            statement.execute("PRAGMA journal_mode=WAL");
        }
    }

    @Override
    public void close() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(Duration.ofSeconds(5).toMillis(), TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            executorService.shutdownNow();
        }
    }

    @FunctionalInterface
    public interface SQLSupplier<T> {
        T get() throws Exception;
    }

    @FunctionalInterface
    public interface SQLRunnable {
        void run() throws Exception;
    }

    @FunctionalInterface
    public interface SQLTransaction<T> {
        T apply(Connection connection) throws Exception;
    }

    private final class DatabaseThreadFactory implements ThreadFactory {

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "customclans-sqlite");
            thread.setDaemon(true);
            thread.setUncaughtExceptionHandler((unused, throwable) ->
                    logger.severe("Uncaught SQLite worker exception: " + throwable.getMessage()));
            return thread;
        }
    }
}
