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

    private DatabaseManager() {
        try {
            // Load the SQLite JDBC driver (if necessary)
            Class.forName("org.sqlite.JDBC");
            // Establish a connection to the database
            connection = DriverManager.getConnection("jdbc:sqlite:MusicPlayer.db");
            initializeDatabase();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private void initializeDatabase() {
        try (Statement statement = connection.createStatement()) {
            // Create Songs table
            statement.execute("CREATE TABLE IF NOT EXISTS Songs (" +
                              "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                              "path TEXT NOT NULL UNIQUE);");

            // Create Playlists table
            statement.execute("CREATE TABLE IF NOT EXISTS Playlists (" +
                              "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                              "name TEXT NOT NULL);");

            // Create PlaylistSongs table
            statement.execute("CREATE TABLE IF NOT EXISTS PlaylistSongs (" +
                              "playlist_id INTEGER NOT NULL, " +
                              "song_id INTEGER NOT NULL, " +
                              "FOREIGN KEY (playlist_id) REFERENCES Playlists(id), " +
                              "FOREIGN KEY (song_id) REFERENCES Songs(id));");
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

    public void createPlaylist(String name) {
        String sql = "INSERT INTO Playlists (name) VALUES (?);";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addSongToPlaylist(String songPath, String playlistName) {
        String sql = "INSERT INTO PlaylistSongs (playlist_id, song_id) " +
                     "SELECT p.id, s.id FROM Playlists p, Songs s " +
                     "WHERE p.name = ? AND s.path = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playlistName);
            pstmt.setString(2, songPath);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getSongsInPlaylist(String playlistName) {
        List<String> songs = new ArrayList<>();
        String sql = "SELECT s.path FROM Songs s " +
                     "JOIN PlaylistSongs ps ON s.id = ps.song_id " +
                     "JOIN Playlists p ON ps.playlist_id = p.id " +
                     "WHERE p.name = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playlistName);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                songs.add(rs.getString("path"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return songs;
    }

    public List<String> getAllPlaylists() {
        List<String> playlists = new ArrayList<>();
        String sql = "SELECT name FROM Playlists;";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                playlists.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return playlists;
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

}
