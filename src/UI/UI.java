package UI;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.DirectoryChooser;
import javafx.util.Duration;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class UI extends JFrame {
    private final JButton playButton;
    private final JButton nextButton;
    private final JButton prevButton;
    private final JButton changePlaylistButton;
    private final JButton repeatButton;
    private final JButton shuffleButton;

    private final JLabel songLabel;
    private final JSlider scrubBar;
    private final JLabel currentTimeLabel;
    private final JLabel totalTimeLabel;
    private MediaPlayer mediaPlayer;
    private List<File> songFiles;
    private int currentSongIndex = -1;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("mm:ss");
    private JList<String> playlist;
    private DefaultListModel<String> playlistModel;
    private enum PlaybackMode {NORMAL, REPEAT, SHUFFLE}
    private PlaybackMode currentMode = PlaybackMode.NORMAL;

    private ImageIcon resizeImage(String filePath, int width, int height) {
        /// Resize the image to the specified width and height to fit the button.
        try {
            ImageIcon originalIcon = new ImageIcon(getClass().getResource(filePath));
            Image originalImage = originalIcon.getImage();
            Image resizedImage = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(resizedImage);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public UI() {
        // Initialize the JavaFX environment
        new JFXPanel();


        // Swing components
        playButton = new JButton("▶");
        nextButton = new JButton("Next");
        prevButton = new JButton("Previous");
        changePlaylistButton = new JButton("Change Playlist");
        repeatButton = new JButton("Repeat");
        shuffleButton = new JButton("Shuffle");

        songLabel = new JLabel("No song playing", SwingConstants.CENTER);
        scrubBar = new JSlider();
        currentTimeLabel = new JLabel("--:--");
        totalTimeLabel = new JLabel("--:--");

        // Set up the icons for the buttons

        try {
            int iconSize = 30;

            ImageIcon playIcon = resizeImage("play.png", iconSize, iconSize);
            playButton.setIcon(playIcon);

            ImageIcon nextIcon = resizeImage("next.png", iconSize, iconSize);
            nextButton.setIcon(nextIcon);

            ImageIcon prevIcon = resizeImage("prev.png", iconSize, iconSize);
            prevButton.setIcon(prevIcon);

            ImageIcon changePlaylistIcon = resizeImage("changePlaylist.png", iconSize, iconSize);
            changePlaylistButton.setIcon(changePlaylistIcon);

            ImageIcon repeatIcon = resizeImage("repeat.png", iconSize, iconSize);
            repeatButton.setIcon(repeatIcon);

            ImageIcon shuffleIcon = resizeImage("shuffle.png", iconSize, iconSize);
            shuffleButton.setIcon(shuffleIcon);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // String fullPath = "/Users/longnguyen/Desktop/COMP730SoftDev/MP3-Player/src/UI";
        // ImageIcon playIcon = new ImageIcon(getClass().getResource("play.png"));
        // ImageIcon nextIcon = new ImageIcon(getClass().getResource("next.png"));
        // ImageIcon prevIcon = new ImageIcon(getClass().getResource("prev.png"));
        // ImageIcon changePlaylistIcon = new ImageIcon(getClass().getResource("changePlaylist.png"));
        // ImageIcon repeatIcon = new ImageIcon(getClass().getResource("repeat.png"));
        // ImageIcon shuffleIcon = new ImageIcon(getClass().getResource("shuffle.png"));

        
        // playButton.setIcon(playIcon);
        // nextButton.setIcon(nextIcon);
        // prevButton.setIcon(prevIcon);
        // changePlaylistButton.setIcon(changePlaylistIcon);
        // repeatButton.setIcon(repeatIcon);
        // shuffleButton.setIcon(shuffleIcon);

        // Set up the JFrame
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        setLayout(new BorderLayout(5, 5));

        // Set up the UI components
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(currentTimeLabel, BorderLayout.WEST);
        topPanel.add(scrubBar, BorderLayout.CENTER);
        topPanel.add(totalTimeLabel, BorderLayout.EAST);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(topPanel, BorderLayout.NORTH);
        centerPanel.add(songLabel, BorderLayout.CENTER);

        // Set up the UI layout for each button in the bottom panel
        JPanel bottomPanel = new JPanel(new BorderLayout());
        FlowLayout flowLayout = new FlowLayout(FlowLayout.CENTER);
        bottomPanel.setLayout(flowLayout);
        bottomPanel.add(playButton);
        bottomPanel.add(nextButton);
        bottomPanel.add(prevButton);
        bottomPanel.add(changePlaylistButton);
        

        JPanel bottomPanel2 = new JPanel(flowLayout);
        FlowLayout flowLayout2 = new FlowLayout(FlowLayout.CENTER);
        bottomPanel2.setLayout(flowLayout2);
        bottomPanel2.add(repeatButton);
        bottomPanel2.add(shuffleButton);

        JPanel combinedBottomPanel = new JPanel(new BorderLayout());
        combinedBottomPanel.add(bottomPanel, BorderLayout.NORTH);
        combinedBottomPanel.add(bottomPanel2, BorderLayout.SOUTH);

        // Initialize playlist components
        playlistModel = new DefaultListModel<>();
        playlist = new JList<>(playlistModel);
        JScrollPane playlistScrollPane = new JScrollPane(playlist);

        // Add components to the UI
        add(playlistScrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.SOUTH);
        add(combinedBottomPanel, BorderLayout.NORTH);
        // add(bottomPanel2, BorderLayout.NORTH);


        // Action listeners for buttons
        playButton.addActionListener(this::playPause);
        nextButton.addActionListener(e -> playNextSong());
        prevButton.addActionListener(e -> playPreviousSong());
        changePlaylistButton.addActionListener(this::changePlaylist);
        repeatButton.addActionListener(this::toggleRepeatMode);
        shuffleButton.addActionListener(this::toggleShuffleMode);



        scrubBar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (mediaPlayer != null && mediaPlayer.getTotalDuration().greaterThan(Duration.ZERO)) {
                    int mouseX = evt.getX();
                    double scrubValue = (double) mouseX / (double) scrubBar.getWidth() * scrubBar.getMaximum();
                    Duration seekTo = mediaPlayer.getTotalDuration().multiply(scrubValue / 100.0);
                    mediaPlayer.seek(seekTo);
                }
            }
        });

        // Set up the JavaFX thread
        Platform.runLater(this::initializeMediaPlayer);

        playlist.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedSongIndex = playlist.getSelectedIndex();
                if (selectedSongIndex >= 0) {
                    currentSongIndex = selectedSongIndex;
                    playSong(currentSongIndex);
                }
            }
        });
    }

    

    private void initializeMediaPlayer() {
        SwingUtilities.invokeLater(() -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File selectedDirectory = directoryChooser.showDialog(null);
    
            if (selectedDirectory != null) {
                songFiles = new ArrayList<>(List.of(selectedDirectory.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp3"))));
    
                if (!songFiles.isEmpty()) {
                    playNextSong();
                } else {
                    JOptionPane.showMessageDialog(this, "No MP3 files found in the selected directory.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "No directory selected.");
            }
    
            for (File songFile : songFiles) {
                playlistModel.addElement(songFile.getName());
            }
        });
    }

    private void playPause(ActionEvent event) {
        if (mediaPlayer != null) {
            MediaPlayer.Status status = mediaPlayer.getStatus();
            if (status == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
                playButton.setText("Play");
                updatePlayPauseIcon(false);
            } else {
                mediaPlayer.play();
                playButton.setText("Pause");
                updatePlayPauseIcon(true);

            }
        }
    }

    private void updatePlayPauseIcon(boolean isPlaying) {
        // Set the appropriate icon based on the playback status
        try {
            int iconSize = 30;
            String iconPath = isPlaying ? "pause.png" : "play.png";
            ImageIcon playPauseIcon = resizeImage(iconPath, iconSize, iconSize);
            playButton.setIcon(playPauseIcon);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playNextSong() {
        if (songFiles != null && !songFiles.isEmpty()) {
            currentSongIndex = (currentSongIndex + 1) % songFiles.size();
            playSong(currentSongIndex);
        }
    }

    private void playPreviousSong() {
        if (songFiles != null && !songFiles.isEmpty()) {
            currentSongIndex = (currentSongIndex - 1 + songFiles.size()) % songFiles.size();
            playSong(currentSongIndex);
        }
    }

    private void playSong(int index) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    
        int[] currentIndex = {index}; // Array to store the current index
    
        Media media;
        if (currentMode == PlaybackMode.SHUFFLE) {
            Collections.shuffle(songFiles); // Shuffle the playlist
            currentIndex[0] = 0; // Start playing from the first song after shuffling
            media = new Media(songFiles.get(currentIndex[0]).toURI().toString());
        } else {
            media = new Media(songFiles.get(currentIndex[0]).toURI().toString());
        }
    
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setOnReady(() -> {
            mediaPlayer.play();
            songLabel.setText(songFiles.get(currentIndex[0]).getName());
            playButton.setText("Pause");
            Duration totalDuration = mediaPlayer.getMedia().getDuration();
            totalTimeLabel.setText(timeFormat.format(new Date((long) totalDuration.toMillis())));
        });
    
        mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> updateScrubBar(newValue));
        });
    
        mediaPlayer.setOnEndOfMedia(() -> {
            switch (currentMode) {
                case REPEAT:
                    mediaPlayer.seek(Duration.ZERO); // Repeat the song
                    break;
                case SHUFFLE:
                    currentIndex[0] = (currentIndex[0] + 1) % songFiles.size();
                    playSong(currentIndex[0]);
                    break;
                case NORMAL:
                    playNextSong(); // Play the next song in normal mode
                    break;
            }
        });
    
        playlist.setSelectedIndex(currentIndex[0]);
        playlist.ensureIndexIsVisible(currentIndex[0]);
    }

    private void updateScrubBar(Duration currentTime) {
        double progress = currentTime.toMillis() / mediaPlayer.getTotalDuration().toMillis();
        scrubBar.setValue((int) (progress * 100));
        currentTimeLabel.setText(timeFormat.format(new Date((long) currentTime.toMillis())));
    }

    private void changePlaylist(ActionEvent event) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedDirectory = fileChooser.getSelectedFile();
            if (selectedDirectory != null) {
                // Update the playlist with the new directory
                updatePlaylist(selectedDirectory);
            }
        }
    }

    private void updatePlaylist(File directory) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }

        songFiles = new ArrayList<>(List.of(directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp3"))));

        if (!songFiles.isEmpty()) {
            currentSongIndex = -1; // Reset current song index
            playNextSong();
        } else {
            JOptionPane.showMessageDialog(this, "No MP3 files found in the selected directory.");
        }

        // Clear and update the playlist model
        playlistModel.clear();
        for (File songFile : songFiles) {
            playlistModel.addElement(songFile.getName().replace(".mp3", ""));
        }
    }

    private void toggleRepeatMode(ActionEvent event) {
        switch (currentMode) {
            case NORMAL:
                currentMode = PlaybackMode.REPEAT;
                repeatButton.setText("Repeat (ON)");
                break;
            case REPEAT:
                currentMode = PlaybackMode.NORMAL;
                repeatButton.setText("Repeat");
                break;
            case SHUFFLE:
                // If in shuffle mode, switch to repeat mode
                currentMode = PlaybackMode.REPEAT;
                repeatButton.setText("Repeat (ON)");
                shuffleButton.setText("Shuffle");
                break;
        }
    }

    private void updatePlaylistUI() {
        playlistModel.clear();
        for (File songFile : songFiles) {
            playlistModel.addElement(songFile.getName().replace(".mp3", ""));
        }
    }

    private void toggleShuffleMode(ActionEvent event) {
        switch (currentMode) {
            case NORMAL:
                currentMode = PlaybackMode.SHUFFLE;
                shuffleButton.setText("Shuffle (ON)");
                break;
            case REPEAT:
                // If in repeat mode, switch to shuffle mode
                currentMode = PlaybackMode.SHUFFLE;
                shuffleButton.setText("Shuffle (ON)");
                repeatButton.setText("Repeat");
                break;
            case SHUFFLE:
                currentMode = PlaybackMode.NORMAL;
                shuffleButton.setText("Shuffle");
                break;
        }
    
        // When the shuffle mode is on,
        // create a temporary list for shuffling, excluding the currently playing song
        if (currentMode == PlaybackMode.SHUFFLE && mediaPlayer != null) {
            List<File> tempList = new ArrayList<>(songFiles);
            tempList.remove(currentSongIndex);
            Collections.shuffle(tempList);
            tempList.add(0, songFiles.get(currentSongIndex)); // Add the currently playing song at the top
            songFiles = tempList;
            updatePlaylistUI();
        }
    }


    public static void main(String[] args) {
        new UI().setVisible(true);
        System.out.println("Starting!");
    }
}
