package MainView;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseManager {
    private static DatabaseManager instance = null;
    private Connection connection;

    private DatabaseManager(String dbName) {
        try {
            // Load the SQLite JDBC driver (if necessary)
            Class.forName("org.sqlite.JDBC");
            // Establish a connection to the database using the provided name
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbName);
            initializeDatabase();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public static DatabaseManager getInstance(String dbName) {
        if (instance == null) {
            instance = new DatabaseManager(dbName);
        }
        return instance;
    }

    protected void initializeDatabase() {
        try (Statement statement = connection.createStatement()) {
            // Create Songs table
            statement.execute("CREATE TABLE IF NOT EXISTS Songs (" +
                              "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                              "path TEXT NOT NULL UNIQUE);");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void addSong(String path) {
        String sql = "INSERT INTO Songs (path) VALUES (?) ON CONFLICT(path) DO NOTHING;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, path);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public List<String> getAllSongs() {
        List<String> songs = new ArrayList<>();
        String sql = "SELECT path FROM Songs;";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                songs.add(rs.getString("path"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return songs;
    }
    
    public void clearDatabase() {
        try (Statement statement = connection.createStatement()) {
            // Clear the Songs table
            statement.execute("DELETE FROM Songs;");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
