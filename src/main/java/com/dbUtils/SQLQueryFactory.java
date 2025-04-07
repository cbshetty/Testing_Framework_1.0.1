package com.dbUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SQLQueryFactory {

    // Factory method for creating SELECT queries
    public static PreparedStatement createSelectQuery(String query) throws SQLException {
        Connection connection = DatabaseManager.getInstance().getConnection();
        return connection.prepareStatement(query);
    }

    // Factory method for creating INSERT queries
    public static PreparedStatement createInsertQuery(String query) throws SQLException {
        Connection connection = DatabaseManager.getInstance().getConnection();
        return connection.prepareStatement(query);
    }

    public static PreparedStatement createUpdateQuery(String query) throws SQLException {
        Connection connection = DatabaseManager.getInstance().getConnection();
        return connection.prepareStatement(query);
    }

    public static PreparedStatement createDeleteQuery(String query) throws SQLException {
        Connection connection = DatabaseManager.getInstance().getConnection();
        return connection.prepareStatement(query);
    }

}
