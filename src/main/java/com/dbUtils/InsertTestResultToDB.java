package com.dbUtils;

import com.api.reporting.ReportFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

public class InsertTestResultToDB {
    DatabaseConnection databaseConnection;
    static Connection connection;

    /**
     * Constructor to initialize the database connection.
     *
     * @throws SQLException if a database access error occurs.
     */
    public InsertTestResultToDB() throws SQLException {
        databaseConnection = databaseConnection.getInstance();
        connection = databaseConnection.getConnection();
    }

    /**
     * Inserts data into the specified table using the provided query and parameters.
     *
     * @param query      the SQL insert query.
     * @param parameters the parameters to be set in the query.
     * @throws SQLException if a database access error occurs.
     */
    public static void insertData(String query, Object... parameters) throws SQLException {
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            setParameters(preparedStatement, parameters);
            preparedStatement.executeUpdate();
        }
    }

    /**
     * Helper method to set parameters for the PreparedStatement.
     *
     * @param preparedStatement the PreparedStatement to set parameters for.
     * @param parameters        the parameters to be set.
     * @throws SQLException if a database access error occurs.
     */
    private static void setParameters(PreparedStatement preparedStatement, Object... parameters) throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            preparedStatement.setObject(i + 1, parameters[i]);
        }
    }

    /**
     * Inserts automation test results into the TestAutomationResult table.
     *
     * @throws SQLException if a database access error occurs.
     */
    public static void insertAutomationTestResults() throws SQLException {
        String workflowId = System.getProperty("WorkflowId");
        System.out.println(ReportFactory.calculateExecutionTime());
       // String reportLink = System.getProperty("ReportLink");
        String insertQuery = " INSERT INTO Test_Automation_Result.TestAutomationResult (`WorkflowId`,`Application Name`, `Report Name`, `Total Tests`, `Total Pass Tests`, `Total Fail Tests`,`POD`,`Execution Time`,`Timestamp`) VALUES (?,?,?,?,?,?,?,?,?)";
        System.out.println(insertQuery);
        System.out.println(ReportFactory.calculateExecutionTime());
        insertData(insertQuery, workflowId,ReportFactory.applicationName, ReportFactory.ReportName, ReportFactory.totalTests, ReportFactory.totalPassTests, ReportFactory.totalFailTests,"AROM",ReportFactory.calculateExecutionTime(),CurrentTimestamp());
        System.out.println(insertQuery);
    }

    /**
     * Alters the TestAutomationResult table by adding a new column.
     *
     * @param columnName the name of the new column.
     * @param dataType   the data type of the new column.
     * @throws SQLException if a database access error occurs.
     */
    public static void addColumnToTestAutomationResult(String columnName, String dataType) throws SQLException {
        String alterTableSQL = "ALTER TABLE Test_Automation_Result.TestAutomationResult ADD COLUMN `" + columnName + "` " + dataType;
        System.out.println(alterTableSQL);
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(alterTableSQL)) {
            preparedStatement.executeUpdate();
        }
    }


    public static void main(String[] args) throws SQLException {
        //addColumnToTestAutomationResult("POD","VARCHAR(255)");
        addColumnToTestAutomationResult("Execution Time","FLOAT");
    }

        public static String CurrentTimestamp() {
            // Get the current date and time
            Date now = new Date();

            // Define the format
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            // Format the current date and time
            return formatter.format(now);
        }

}
