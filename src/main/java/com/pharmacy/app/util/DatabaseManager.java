package com.pharmacy.app.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static String dbFile = "pharmacy.db";
    private static Connection connection;

    public static String getDbFilePath() {
        return dbFile;
    }

    public static void setDbFileForTesting(String testFilePath) {
        resetConnection();
        dbFile = testFilePath;
    }

    public static Connection getConnection() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("PRAGMA foreign_keys = ON;");
                }
                initializeSchema(connection);
                runMigrations(connection);
            } catch (SQLException e) {
                throw new RuntimeException("Failed to connect to database: " + e.getMessage(), e);
            }
        }
        return connection;
    }

    public static void resetConnection() {
        closeConnection();
        connection = null;
    }

    private static void initializeSchema(Connection conn) throws SQLException {
        String[] createStatements = {

                """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE,
                password_hash TEXT NOT NULL,
                full_name TEXT NOT NULL,
                role TEXT NOT NULL CHECK (role IN ('ADMIN', 'PHARMACIST', 'CASHIER')),
                is_active INTEGER NOT NULL DEFAULT 1,
                created_at TEXT NOT NULL DEFAULT (datetime('now'))
            );
            """,

                """
            CREATE TABLE IF NOT EXISTS suppliers (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                contact_person TEXT,
                phone TEXT,
                email TEXT,
                address TEXT,
                created_at TEXT NOT NULL DEFAULT (datetime('now'))
            );
            """,

                """
            CREATE TABLE IF NOT EXISTS medicines (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                generic_name TEXT,
                category TEXT,
                manufacturer TEXT,
                unit TEXT NOT NULL DEFAULT 'strip',
                unit_price REAL NOT NULL DEFAULT 0,
                pack_size INTEGER NOT NULL DEFAULT 1,
                tax_pct REAL NOT NULL DEFAULT 0,
                is_controlled INTEGER NOT NULL DEFAULT 0,
                barcode TEXT UNIQUE,
                low_stock_threshold INTEGER NOT NULL DEFAULT 10,
                is_active INTEGER NOT NULL DEFAULT 1,
                created_at TEXT NOT NULL DEFAULT (datetime('now'))
            );
            """,

                """
            CREATE TABLE IF NOT EXISTS batches (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                medicine_id INTEGER NOT NULL,
                batch_no TEXT NOT NULL,
                expiry_date TEXT NOT NULL,
                quantity INTEGER NOT NULL DEFAULT 0,
                cost_price REAL NOT NULL DEFAULT 0,
                received_date TEXT NOT NULL DEFAULT (datetime('now')),
                FOREIGN KEY (medicine_id) REFERENCES medicines(id) ON DELETE CASCADE
            );
            """,

                """
            CREATE TABLE IF NOT EXISTS patients (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                phone TEXT,
                dob TEXT,
                allergies TEXT,
                notes TEXT,
                created_at TEXT NOT NULL DEFAULT (datetime('now'))
            );
            """,

                """
            CREATE TABLE IF NOT EXISTS doctors (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                specialization TEXT,
                phone TEXT,
                created_at TEXT NOT NULL DEFAULT (datetime('now'))
            );
            """,

                """
            CREATE TABLE IF NOT EXISTS prescriptions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                patient_id INTEGER NOT NULL,
                doctor_name TEXT,
                prescription_date TEXT NOT NULL DEFAULT (datetime('now')),
                image_path TEXT,
                created_by INTEGER,
                FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
                FOREIGN KEY (created_by) REFERENCES users(id)
            );
            """,

                """
            CREATE TABLE IF NOT EXISTS prescription_items (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                prescription_id INTEGER NOT NULL,
                medicine_id INTEGER NOT NULL,
                dosage TEXT,
                quantity INTEGER NOT NULL DEFAULT 1,
                FOREIGN KEY (prescription_id) REFERENCES prescriptions(id) ON DELETE CASCADE,
                FOREIGN KEY (medicine_id) REFERENCES medicines(id)
            );
            """,

                """
            CREATE TABLE IF NOT EXISTS sales (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                patient_id INTEGER,
                prescription_id INTEGER,
                subtotal REAL NOT NULL DEFAULT 0,
                discount REAL NOT NULL DEFAULT 0,
                tax_amount REAL NOT NULL DEFAULT 0,
                total REAL NOT NULL DEFAULT 0,
                payment_mode TEXT NOT NULL CHECK (payment_mode IN ('CASH', 'CARD', 'UPI', 'OTHER')),
                created_by INTEGER,
                created_at TEXT NOT NULL DEFAULT (datetime('now')),
                FOREIGN KEY (patient_id) REFERENCES patients(id),
                FOREIGN KEY (prescription_id) REFERENCES prescriptions(id),
                FOREIGN KEY (created_by) REFERENCES users(id)
            );
            """,

                """
            CREATE TABLE IF NOT EXISTS sale_items (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                sale_id INTEGER NOT NULL,
                medicine_id INTEGER NOT NULL,
                batch_id INTEGER NOT NULL,
                quantity INTEGER NOT NULL,
                unit_price REAL NOT NULL,
                line_total REAL NOT NULL,
                FOREIGN KEY (sale_id) REFERENCES sales(id) ON DELETE CASCADE,
                FOREIGN KEY (medicine_id) REFERENCES medicines(id),
                FOREIGN KEY (batch_id) REFERENCES batches(id)
            );
            """,

                """
            CREATE TABLE IF NOT EXISTS purchase_orders (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                supplier_id INTEGER NOT NULL,
                order_date TEXT NOT NULL DEFAULT (datetime('now')),
                status TEXT NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'RECEIVED', 'CANCELLED')),
                FOREIGN KEY (supplier_id) REFERENCES suppliers(id)
            );
            """,

                """
            CREATE TABLE IF NOT EXISTS purchase_items (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                purchase_order_id INTEGER NOT NULL,
                medicine_id INTEGER NOT NULL,
                quantity INTEGER NOT NULL,
                cost_price REAL NOT NULL,
                FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(id) ON DELETE CASCADE,
                FOREIGN KEY (medicine_id) REFERENCES medicines(id)
            );
            """,

                """
            CREATE TABLE IF NOT EXISTS audit_logs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                action TEXT NOT NULL,
                entity TEXT,
                entity_id INTEGER,
                details TEXT,
                created_at TEXT NOT NULL DEFAULT (datetime('now')),
                FOREIGN KEY (user_id) REFERENCES users(id)
            );
            """
        };

        try (Statement stmt = conn.createStatement()) {
            for (String sql : createStatements) {
                stmt.execute(sql);
            }
        }
    }

    private static void runMigrations(Connection conn) {
        tryAddColumn(conn, "medicines", "pack_size", "INTEGER NOT NULL DEFAULT 1");
        tryAddColumn(conn, "medicines", "is_active", "INTEGER NOT NULL DEFAULT 1");
    }

    private static void tryAddColumn(Connection conn, String table, String column, String definition) {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
        } catch (SQLException e) {
            // Already exists - expected after the first run
        }
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }
}