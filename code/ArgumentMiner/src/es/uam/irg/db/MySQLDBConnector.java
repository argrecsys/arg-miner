package es.uam.irg.db;

public class MySQLDBConnector extends DBConnector {
    
    // Public constants
    public static final String DB_TYPE = "MySQL";
    public static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";
    public static final String DB_PROTOCOL = "jdbc:mysql:";

    // ============
    // CONSTRUCTORS
    // ============
    /**
     * Creates an empty instance of MySQLConnector, registering the MySQL driver
     */
    public MySQLDBConnector() throws Exception {
        super(DB_DRIVER, new com.mysql.cj.jdbc.Driver());
    }

    // ===========================
    // DATABASE MANAGEMENT METHODS
    // ===========================
    /**
     * Acquires a connection to a database with the given user name and user
     * password
     *
     * @param dbServer the URL of the database server in which the connection is
     * going to be established
     * @param dbName the name of the database in which the connection is going
     * to be established
     * @param userName the name of the user who wants to open the connection
     * @param userPassword the password of the user who wants to open the
     * connection
     */
    public void connect(String dbServer, String dbName, String userName, String userPassword) throws Exception {
        super.connect(DB_PROTOCOL + "//" + dbServer + "/" + dbName + "?user=" + userName + "&password=" + userPassword + "&useUnicode=true&characterEncoding=UTF-8&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC");
    }
}
