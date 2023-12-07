package MainView;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainView {
    private MainViewUI ui;
    private MediaPlayerController mediaPlayerController;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("mm:ss");

    public MainView() {
        // Initialize the JavaFX environment
        new JFXPanel();

        // Initialize UI components
        ui = new MainViewUI();

        // Initialize MediaPlayerController
        mediaPlayerController = new MediaPlayerController();

        // Initialize and start the timer for updating the scrub bar
        int delay = 100; // Update every second
        Timer timer = new Timer(delay, e -> updateScrubBar());
        timer.start();

        // Action listeners for buttons
        ui.setPlayButtonActionListener(e -> playPause());
        ui.setNextButtonActionListener(e -> mediaPlayerController.playNextSong());
        ui.setPrevButtonActionListener(e -> mediaPlayerController.playPreviousSong());
        ui.setChangePlaylistButtonActionListener(this::changePlaylist);
        ui.setRepeatButtonActionListener(e -> toggleRepeat());
        ui.setShuffleButtonActionListener(e -> toggleShuffle());

        // Scrub bar mouse listener
        ui.setScrubBarMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                scrubBarClicked(evt);
            }
        });

        // Playlist selection listener
        ui.setPlaylistSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                playSelectedSong();
            }
        });

        // Set song playback listener
        mediaPlayerController.setSongPlaybackListener(new MediaPlayerController.SongPlaybackListener() {
            @Override
            public void onSongChanged(String songName, Duration totalDuration) {
                ui.updateSongLabel(songName);
                ui.updateTotalTimeLabel(timeFormat.format(new Date((long) totalDuration.toMillis())));
                updatePlayPauseButton();
            }
        });
        
        try {
            int iconSize = 30;
            ImageIcon playIcon = resizeImage("pause.png", iconSize, iconSize);
            ImageIcon pauseIcon = resizeImage("pause.png", iconSize, iconSize);
            ImageIcon nextIcon = resizeImage("next.png", iconSize, iconSize);
            ImageIcon prevIcon = resizeImage("prev.png", iconSize, iconSize);
            ImageIcon changePlaylistIcon = resizeImage("changePlaylist.png", iconSize, iconSize);
            ImageIcon repeatIcon = resizeImage("repeat.png", iconSize, iconSize);
            ImageIcon shuffleIcon = resizeImage("shuffle.png", iconSize, iconSize);

            // Update play button icon depending on the playback status
            boolean isPlaying = mediaPlayerController.getPlaybackStatus() == MediaPlayer.Status.PLAYING;
            ImageIcon playPauseIcon = isPlaying ? pauseIcon : playIcon;
            ui.setButtonIcons(playPauseIcon, nextIcon, prevIcon, changePlaylistIcon, repeatIcon, shuffleIcon);
        } catch (Exception e) {
            e.printStackTrace();
        }

        updatePlaylistFromDatabase();
    }

    private void toggleShuffle() {
        mediaPlayerController.toggleShuffleMode();
        if (mediaPlayerController.getCurrentMode() == MediaPlayerController.PlaybackMode.SHUFFLE) {
            ui.setShuffleButtonBackground(Color.BLUE);
            ui.setShuffleButtonTextForeground(Color.WHITE);
            ui.setRepeatButtonBackground(null);
            ui.setRepeatButtonTextForeground(null);
        } else {
            ui.setShuffleButtonBackground(null);
            ui.setShuffleButtonTextForeground(null);
        }
    }

    private void toggleRepeat() {
        mediaPlayerController.toggleRepeatMode();
        if (mediaPlayerController.getCurrentMode() == MediaPlayerController.PlaybackMode.REPEAT) {
            ui.setRepeatButtonBackground(Color.BLUE);
            ui.setRepeatButtonTextForeground(Color.WHITE);
            ui.setShuffleButtonBackground(null);
            ui.setShuffleButtonTextForeground(null);
        } else {
            ui.setRepeatButtonBackground(null);
            ui.setRepeatButtonTextForeground(null);
        }
    }

    private void playPause() {
        mediaPlayerController.playPause();
        updatePlayPauseButton();
    }

    private void updatePlayPauseButton() {
        // Update the play/pause button based on the MediaPlayer's status
        boolean isPlaying = mediaPlayerController.getPlaybackStatus() == MediaPlayer.Status.PLAYING;
        int iconSize = 30;
        String iconPath = isPlaying ? "play.png" : "pause.png";
        ImageIcon playPauseIcon = resizeImage(iconPath, iconSize, iconSize);
        ui.updatePlayPauseButtonIcon(playPauseIcon);
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

    private void scrubBarClicked(MouseEvent evt) {
        int mouseX = evt.getX();
        double scrubValue = (double) mouseX / ui.getScrubBarWidth() * ui.getScrubBarMaximum();
        Duration seekTo = mediaPlayerController.getTotalDuration().multiply(scrubValue / 100.0);
        mediaPlayerController.seek(seekTo);
    }

    private void playSelectedSong() {
        int selectedSongIndex = ui.getPlaylistSelectedIndex();
        if (selectedSongIndex >= 0) {
            mediaPlayerController.playSong(selectedSongIndex);
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
        ui.updatePlaylistModel(songFiles);
    }

    private void updateScrubBar() {
        if (mediaPlayerController != null) {
            Duration currentTime = mediaPlayerController.getCurrentTime();
            Duration totalDuration = mediaPlayerController.getTotalDuration();

            if (!totalDuration.equals(Duration.ZERO) && !totalDuration.isUnknown()) {
                double progress = currentTime.toMillis() / totalDuration.toMillis();
                ui.updateScrubBarValue((int) (progress * 100));
                ui.updateCurrentTimeLabel(timeFormat.format(new Date((long) currentTime.toMillis())));
            }
        }
    }

    private void changePlaylist(ActionEvent event) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int result = fileChooser.showOpenDialog(null); // Use 'null' or pass a component from UI
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedDirectory = fileChooser.getSelectedFile();
            updateSongDatabase(selectedDirectory);
        }
    }
    
    private void updateSongDatabase(File selectedDirectory) {
        if (selectedDirectory != null) {
            List<File> files = collectMP3Files(selectedDirectory);
            if (!files.isEmpty()) {
                DatabaseManager dbManager = DatabaseManager.getInstance();
                for (File file : files) {
                    dbManager.addSong(file.getAbsolutePath());
                }
                updatePlaylistFromDatabase();
            } else {
                JOptionPane.showMessageDialog(null, "No MP3 files found in the selected directory.");
            }
        }
    }

    private List<File> collectMP3Files(File directory) {
        List<File> mp3Files = new ArrayList<>();
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    mp3Files.addAll(collectMP3Files(file)); // Recursive call
                } else if (file.getName().toLowerCase().endsWith(".mp3")) {
                    mp3Files.add(file);
                }
            }
        }

        return mp3Files;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainView().ui.setVisible(true)); // Adjusted to set visibility of UI
    }
}