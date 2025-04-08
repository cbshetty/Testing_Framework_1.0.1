package com.dbUtils;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private static DatabaseManager instance;
    private Connection connection;

    private String url = "jdbc:mysql://amx-test-automation-prod-db-cluster.cluster-c0iswfmnnzar.ap-south-1.rds.amazonaws.com:3306/Functional_Automation";
    private String username = "admin";
    private String password = System.getProperty("GrafanaDBPwd");

    // Private constructor to prevent instantiation
    private DatabaseManager() {
        try {
            // Load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Establish the connection
            connection = DriverManager.getConnection(url, username, password);
            System.out.println("Database connected successfully.");
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Get the instance of DatabaseManager (Singleton pattern)
    public static DatabaseManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseManager.class) {
                if (instance == null) {
                    instance = new DatabaseManager();
                }
            }
        }
        return instance;
    }

    // Get the connection object
    public Connection getConnection() {
        return connection;
    }

    // Close the connection when done
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println(DatabaseManager.getInstance().getConnection()  );
        DatabaseManager.getInstance().closeConnection();
    }
}

