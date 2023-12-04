package MainView;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.DirectoryChooser;
import javafx.util.Duration;

import javax.swing.*;

import MainView.Controller.MediaPlayerController;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainView extends JFrame {
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
    private MediaPlayerController mediaPlayerController;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("mm:ss");
    private JList<String> playlist;
    private DefaultListModel<String> playlistModel;

    public MainView() {
        // Initialize the JavaFX environment
        new JFXPanel();
        // Initialize and start the timer for updating the scrub bar
        int delay = 100; // Update every second
        Timer timer = new Timer(delay, e -> updateScrubBar());
        timer.start();

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

        // Initialize MediaPlayerController
        mediaPlayerController = new MediaPlayerController();

        // Initialize UI components
        initUI();

        // Action listeners for buttons
        playButton.addActionListener(this::playPause);
        nextButton.addActionListener(e -> mediaPlayerController.playNextSong());
        prevButton.addActionListener(e -> mediaPlayerController.playPreviousSong());
        changePlaylistButton.addActionListener(this::changePlaylist);
        repeatButton.addActionListener(e -> mediaPlayerController.toggleRepeatMode());
        shuffleButton.addActionListener(e -> mediaPlayerController.toggleShuffleMode());

        scrubBar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (mediaPlayerController.getPlaybackStatus() != MediaPlayer.Status.STOPPED && mediaPlayerController.getTotalDuration().greaterThan(Duration.ZERO)) {
                    int mouseX = evt.getX();
                    double scrubValue = (double) mouseX / (double) scrubBar.getWidth() * scrubBar.getMaximum();
                    Duration seekTo = mediaPlayerController.getTotalDuration().multiply(scrubValue / 100.0);
                    mediaPlayerController.seek(seekTo);
                }
            }
        });

        playlist.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedSongIndex = playlist.getSelectedIndex();
                if (selectedSongIndex >= 0) {
                    mediaPlayerController.playSong(selectedSongIndex);
                }
            }
        });
        
        mediaPlayerController.setSongPlaybackListener(new MediaPlayerController.SongPlaybackListener() {
            @Override
            public void onSongChanged(String songName, Duration totalDuration) {
                songLabel.setText(songName);
                totalTimeLabel.setText(timeFormat.format(new Date((long) totalDuration.toMillis())));
            }
        });
        
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
        
        updatePlaylistFromDatabase();
    }

    private void initUI() {
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
    }

    private void playPause(ActionEvent event) {
        mediaPlayerController.playPause();
        updatePlayPauseButton();
    }
    
    private ImageIcon resizeImage(String filePath, int width, int height) {
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

    private void updatePlayPauseButton() {
        // Update the play/pause button based on the MediaPlayer's status
        boolean isPlaying = mediaPlayerController.getPlaybackStatus() == MediaPlayer.Status.PLAYING;
        updatePlayPauseIcon(isPlaying);
    }


    private void updatePlayPauseIcon(boolean isPlaying) {
        // Set the appropriate icon based on the playback status
        try {
            int iconSize = 30;
            String iconPath = isPlaying ? "play.png" : "pause.png";
            ImageIcon playPauseIcon = resizeImage(iconPath, iconSize, iconSize);
            playButton.setIcon(playPauseIcon);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void changePlaylist(ActionEvent event) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedDirectory = fileChooser.getSelectedFile();
            if (selectedDirectory != null) {
                File[] files = selectedDirectory.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp3"));
                if (files != null && files.length > 0) {
                    DatabaseManager dbManager = DatabaseManager.getInstance();
                    for (File file : files) {
                        dbManager.addSong(file.getAbsolutePath());
                    }
                    updatePlaylistFromDatabase();
                } else {
                    JOptionPane.showMessageDialog(this, "No MP3 files found in the selected directory.");
                }
            }
        }
    }


    private void updatePlaylistFromDatabase() {
        DatabaseManager dbManager = DatabaseManager.getInstance();
        List<String> songPaths = dbManager.getAllSongs();
        List<File> songFiles = new ArrayList<>();

        for (String path : songPaths) {
            songFiles.add(new File(path));
        }

        mediaPlayerController.setSongFiles(songFiles);
        updatePlaylistUI(songFiles);
    }


    private void updatePlaylistUI(List<File> songFiles) {
        playlistModel.clear();
        for (File song : songFiles) {
            playlistModel.addElement(song.getName().replace(".mp3", ""));
        }
    }
    
    private void updateScrubBar() {
        if (mediaPlayerController != null) {
            Duration currentTime = mediaPlayerController.getCurrentTime();
            Duration totalDuration = mediaPlayerController.getTotalDuration();

            if (!totalDuration.equals(Duration.ZERO) && !totalDuration.isUnknown()) {
                double progress = currentTime.toMillis() / totalDuration.toMillis();
                scrubBar.setValue((int) (progress * 100));
                currentTimeLabel.setText(timeFormat.format(new Date((long) currentTime.toMillis())));
            }
        }
    }



    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainView().setVisible(true));
    }
    
    
}
