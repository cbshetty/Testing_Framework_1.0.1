package com.dbUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;
    private final String dbConnectionUrl = System.getProperty("CONNECTION_URL");
    private final String dbUserName = System.getProperty("DB_USERNAME");
    private final String dbPassword = System.getProperty("DB_PASSWORD");

    /**
     * Private constructor to load the JDBC driver and establish the database connection.
     *
     * @throws SQLException if there is an error connecting to the database.
     */
    private DatabaseConnection() throws SQLException{
        try {
            // Load the JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            //Establish the connection
            this.connection = DriverManager.getConnection(dbConnectionUrl, dbUserName, dbPassword);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            throw new SQLException("Unable to connect to the database.");
        }
    }

    /**
     * Returns the singleton instance of DatabaseConnection.
     *
     * @return the singleton instance of DatabaseConnection.
     * @throws SQLException if there is an error connecting to the database.
     */
    public static DatabaseConnection getInstance() throws SQLException {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    /**
     * Returns the current database connection. If the connection is closed or null, it will re-establish the connection.
     *
     * @return the current database connection.
     * @throws SQLException if there is an error connecting to the database.
     */
    public Connection getConnection() throws SQLException{
        if (connection == null || connection.isClosed()) {
            synchronized (DatabaseConnection.class) {
                if (connection == null || connection.isClosed()) {
                    connection = DriverManager.getConnection(dbConnectionUrl, dbUserName, dbPassword);
                }
            }
        }
        return connection;
    }

    /**
     * Closes the database connection if it is open.
     */
    public static void closeConnection() {
        if (instance != null) {
            try {
                if (instance.connection != null && !instance.connection.isClosed()) {
                    instance.connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
