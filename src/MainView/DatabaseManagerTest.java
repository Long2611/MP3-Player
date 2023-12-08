package MainView;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class DatabaseManagerTest {

    private DatabaseManager dbManager;

    @BeforeEach
    public void setUp() {
        // Initialize the database manager with the test database
        dbManager = DatabaseManager.getInstance("test.db");
        // Clean up the database before each test
        dbManager.clearDatabase();
    }

    @AfterEach
    public void tearDown() {
        // Clean up the database after each test
        dbManager.clearDatabase();
    }

    @Test
    public void testAddAndGetSong() {
        // Add a song
        String testSongPath = "/path/to/song.mp3";
        dbManager.addSong(testSongPath);

        // Retrieve songs and check if the added song is present
        List<String> songs = dbManager.getAllSongs();
        assertTrue(songs.contains(testSongPath));
    }
}
