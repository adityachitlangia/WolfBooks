package src.main.java.WolfBooks.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static DatabaseConnection instance;
    private static Connection connection;

    private DatabaseConnection() {

    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            String dbUrl = "jdbc:mysql://localhost:3306/";
            String dbSchema = "WolfBooks";
            String dbUser = "root"; //sc.nextLine(); // Most likely 'root'
            String dbPass = ""; //sc.nextLine(); // Most likely ''

            try {
                connection = DriverManager.getConnection(dbUrl + dbSchema, dbUser, dbPass);
            } catch (SQLException e) {
                connection = DriverManager.getConnection(dbUrl, dbUser, dbPass);
                InitializeDatabase init = new InitializeDatabase();
                init.createSchema(connection);
                connection.close();
                connection = DriverManager.getConnection(dbUrl + dbSchema, dbUser, dbPass);
            }
        }
        return connection;
    }
}