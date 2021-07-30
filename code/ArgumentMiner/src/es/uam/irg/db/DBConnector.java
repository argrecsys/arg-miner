package es.uam.irg.db;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Nico
 */
public class DBConnector {
    // Private attributes

    private Connection connection;

    // ============
    // CONSTRUCTORS
    // ============
    /**
     * Creates an empty instance of DBConnector, recording a specific JDBC driver
     *
     * @param driverClassName the class name of the driver
     * @param driver an instance of the driver
     */
    public DBConnector(String driverClassName, Driver driver) throws Exception {
        this.connection = null;
        try {
            DriverManager.registerDriver(driver);
            Class.forName(driverClassName);
        } catch (Exception e) {
            throw e;
        }
    }

    public Connection getConnection() {
        return this.connection;
    }
    
    // ===========================
    // DATABASE MANAGEMENT METHODS
    // ===========================
    /**
     * Acquires a connection to a database according to the parameters of the given URL
     *
     * @param url the URL with the information of the server, database name, user name and user password
     */
    public void connect(String url) throws Exception {
        try {
            this.disconnect();
            this.connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            throw e;
        }
    }

    /**
     * Closes the connection to the database
     */
    public void disconnect() {
        try {
            if (this.connection != null) {
                this.connection.close();
                this.connection = null;
            }
        } catch (SQLException e) {
        }
    }

    /**
     * TRUE if connection is established, FALSE if not
     * @return 
     */
    public boolean isConnected() {
        return this.connection != null;
    }

    /**
     * Prepares the database connection for batch queries.
     * Note that a connection has to be previously opened (connect method)
     */
    public Statement openBatch() throws Exception {
        try {
            Statement stm = this.connection.createStatement();
            this.connection.setAutoCommit(false);
            return stm;
        } catch (SQLException e) {
            throw e;
        }
    }

    /**
     * Executes the current query batch.
     * Note that a query batch has to be previously created (openBatch method)
     */
    public void closeBatch(Statement stm) throws Exception {
        try {
            if (stm != null) {
                stm.executeBatch();
                this.connection.setAutoCommit(true);
                stm.close();
            }
        } catch (SQLException e) {
            throw e;
        }
    }

    /**
     * Closes the connection to the database. Method invoked by the 'garbage collection' system
     */
    public void finalice() {
        this.disconnect();
    }

    /**
     * Selects several records of the database.
     * Note that a connection has to be previously opened (connect method)
     *
     * @param query Select SQL query
     *
     * @return records returned by the database
     */
    public ResultSet executeSelect(String query) throws Exception {
        ResultSet rs = null;
        try {
            Statement stm = this.connection.createStatement();
            rs = stm.executeQuery(query);
        } catch (SQLException e) {
            throw e;
        }
        return rs;
    }

    /**
     * Inserts a record in the database.
     * Note that a connection has to be previously opened (connect method)
     *
     * @param query the Insert SQL query to execute
     * @param batchMode a boolean indicating whether the insert has to be executed in batch mode
     *
     * @return number of inserted rows
     */
    public int executeInsert(String query, boolean batchMode, Statement stm) throws Exception {
        int numRows = 0;
        try {
            if (batchMode) {
                stm.addBatch(query);
            } else {
                stm = this.connection.createStatement();
                numRows = stm.executeUpdate(query);
                stm.close();
            }
        } catch (SQLException e) {
            throw e;
        }
        return numRows;
    }

    /**
     * Makes an update in the database.
     * Note that a connection has to be previously opened (connect method)
     *
     * @param query the Update SQL query to execute
     * @param batchMode a boolean indicating whether the update has to be executed in batch mode
     *
     * @return number of updated records
     */
    public int executeUpdate(String query, boolean batchMode, Statement stm) throws Exception {
        int numRows = 0;
        try {
            if (batchMode) {
                stm.addBatch(query);
            } else {
                stm = this.connection.createStatement();
                numRows = stm.executeUpdate(query);
                stm.close();
            }
        } catch (SQLException e) {
            throw e;
        }
        return numRows;
    }

    /**
     * Erases records from the database.
     * Note that a connection has to be previously opened (connect method)
     *
     * @param query the Delete SQL query to execute
     * @param batchMode a boolean indicating whether the delete has to be executed in batch mode
     *
     * @return number of deleted records
     */
    public int executeDelete(String query, boolean batchMode, Statement stm) throws Exception {
        int numRows = 0;
        try {
            if (batchMode) {
                stm.addBatch(query);
            } else {
                stm = this.connection.createStatement();
                numRows = stm.executeUpdate(query);
                stm.close();
            }
        } catch (SQLException e) {
            throw e;
        }
        return numRows;
    }
}
