package com.pharmacy.app.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles creating and restoring database backups.
 * Backups are stored as timestamped .db files in a local "backups" folder.
 */
public class BackupManager {

    private static final String BACKUP_FOLDER = "backups";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    /**
     * Creates a consistent snapshot backup of the live database using SQLite's
     * VACUUM INTO command (safe even while the app is running).
     * Returns the path to the created backup file.
     */
    public static String backupNow() {
        File folder = new File(BACKUP_FOLDER);
        if (!folder.exists()) folder.mkdirs();

        String filename = "pharmacy_backup_" + LocalDateTime.now().format(TIMESTAMP_FORMAT) + ".db";
        String fullPath = BACKUP_FOLDER + File.separator + filename;

        Connection conn = DatabaseManager.getConnection();
        String sql = "VACUUM INTO ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fullPath);
            ps.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Backup failed: " + e.getMessage(), e);
        }

        return fullPath;
    }

    /** Returns all backup files, most recent first. */
    public static List<String> listBackups() {
        File folder = new File(BACKUP_FOLDER);
        if (!folder.exists()) return List.of();

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".db"));
        if (files == null) return List.of();

        return Arrays.stream(files)
                .sorted(Comparator.comparingLong(File::lastModified).reversed())
                .map(File::getName)
                .collect(Collectors.toList());
    }

    /**
     * Restores the database from a chosen backup file.
     * Closes the live connection, replaces the database file, then resets
     * the connection so the next database call reconnects to the restored data.
     */
    public static void restoreFromBackup(String backupFileName) {
        Path backupPath = Path.of(BACKUP_FOLDER, backupFileName);
        Path liveDbPath = Path.of(DatabaseManager.getDbFilePath());

        if (!Files.exists(backupPath)) {
            throw new RuntimeException("Backup file not found: " + backupFileName);
        }

        // Close the current connection so the file isn't locked during replacement
        DatabaseManager.resetConnection();

        try {
            Files.copy(backupPath, liveDbPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Restore failed: " + e.getMessage(), e);
        }

        // Reconnect - this will now read the restored data
        DatabaseManager.getConnection();
    }
}