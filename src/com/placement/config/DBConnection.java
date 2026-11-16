package com.placement.config;

import com.placement.exception.DatabaseOperationException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Singleton JDBC Connection Manager.
 * Reads connection credentials and parameters from db.properties
 * and supplies fresh, isolated database connections for try-with-resources blocks.
 */
public class DBConnection {

    private static volatile DBConnection instance;
    private final Properties properties = new Properties();
    private String url;
    private String user;
    private String password;
    private String driver;

    private DBConnection() {
        loadProperties();
        initDriver();
    }

    /**
     * Retrieves the thread-safe Singleton instance of DBConnection.
     * Uses double-checked locking for high performance.
     */
    public static DBConnection getInstance() {
        if (instance == null) {
            synchronized (DBConnection.class) {
                if (instance == null) {
                    instance = new DBConnection();
                }
            }
        }
        return instance;
    }

    /**
     * Attempts to locate and load db.properties from multiple standard locations:
     * 1. Classpath root (/db.properties)
     * 2. Classpath resources folder (/resources/db.properties)
     * 3. Filesystem relative path ("resources/db.properties")
     * 4. Filesystem working directory ("db.properties")
     */
    private void loadProperties() {
        boolean loaded = false;

        // Candidate 1 & 2: Classpath lookups
        String[] classpathPaths = {"/db.properties", "/resources/db.properties", "db.properties", "resources/db.properties"};
        for (String path : classpathPaths) {
            try (InputStream in = getClass().getResourceAsStream(path.startsWith("/") ? path : "/" + path)) {
                if (in != null) {
                    properties.load(in);
                    loaded = true;
                    break;
                }
            } catch (IOException ignored) {
            }
        }

        // Candidate 3 & 4: Filesystem lookups
        if (!loaded) {
            String[] filePaths = {"resources/db.properties", "db.properties", "../resources/db.properties"};
            for (String filePath : filePaths) {
                File file = new File(filePath);
                if (file.exists() && file.isFile()) {
                    try (FileInputStream fis = new FileInputStream(file)) {
                        properties.load(fis);
                        loaded = true;
                        break;
                    } catch (IOException ignored) {
                    }
                }
            }
        }

        if (!loaded) {
            throw new DatabaseOperationException(
                "Unable to load 'db.properties'. Ensure it exists in 'resources/' or the application classpath."
            );
        }

        this.url = properties.getProperty("db.url");
        this.user = properties.getProperty("db.user");
        this.password = properties.getProperty("db.password");
        this.driver = properties.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");

        if (this.url == null || this.user == null) {
            throw new DatabaseOperationException("Missing mandatory database properties: 'db.url' or 'db.user'.");
        }
    }

    private void initDriver() {
        try {
            Class.forName(this.driver);
        } catch (ClassNotFoundException e) {
            throw new DatabaseOperationException(
                "MySQL JDBC Driver class '" + driver + "' not found. Ensure mysql-connector-j jar is on classpath.", e
            );
        }
    }

    /**
     * Creates and returns a fresh java.sql.Connection from DriverManager.
     * Callers must wrap this call inside a try-with-resources block.
     *
     * @return Connection active database connection
     * @throws DatabaseOperationException if connection cannot be established
     */
    public Connection getConnection() throws DatabaseOperationException {
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new DatabaseOperationException(
                "Failed to establish connection to MySQL database at [" + url + "]. Verify MySQL service is running and credentials are valid.",
                e
            );
        }
    }

    /**
     * Diagnostic probe to test database connectivity.
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed() && conn.isValid(3);
        } catch (Exception e) {
            return false;
        }
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }
}
