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
import java.util.Date;
import java.util.List;

public class UI extends JFrame {
    private final JButton playButton;
    private final JButton nextButton;
    private final JButton prevButton;
    private final JLabel songLabel;
    private final JSlider scrubBar;
    private final JLabel currentTimeLabel;
    private final JLabel totalTimeLabel;
    private MediaPlayer mediaPlayer;
    private List<File> songFiles;
    private int currentSongIndex = -1;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("mm:ss");

    public UI() {
        // Initialize the JavaFX environment
        new JFXPanel();

        // Swing components
        playButton = new JButton("Play");
        nextButton = new JButton("Next");
        prevButton = new JButton("Previous");
        songLabel = new JLabel("No song playing", SwingConstants.CENTER);
        scrubBar = new JSlider();
        currentTimeLabel = new JLabel("--:--");
        totalTimeLabel = new JLabel("--:--");

        // Set up the JFrame
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 200);
        setLayout(new BorderLayout(5, 5));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(songLabel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(playButton, BorderLayout.WEST);
        bottomPanel.add(nextButton, BorderLayout.EAST);
        bottomPanel.add(prevButton, BorderLayout.CENTER);

        JPanel scrubPanel = new JPanel(new BorderLayout(5, 0));
        scrubPanel.add(currentTimeLabel, BorderLayout.WEST);
        scrubPanel.add(scrubBar, BorderLayout.CENTER);
        scrubPanel.add(totalTimeLabel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);
        add(scrubPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // Action listeners for buttons
        playButton.addActionListener(this::playPause);
        nextButton.addActionListener(e -> playNextSong());
        prevButton.addActionListener(e -> playPreviousSong());

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
    }

    private void initializeMediaPlayer() {
        // Show directory chooser on the JavaFX Application Thread
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
    }

    private void playPause(ActionEvent event) {
        if (mediaPlayer != null) {
            MediaPlayer.Status status = mediaPlayer.getStatus();
            if (status == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
                playButton.setText("Play");
            } else {
                mediaPlayer.play();
                playButton.setText("Pause");
            }
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
        Media media = new Media(songFiles.get(index).toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setOnReady(() -> {
            mediaPlayer.play();
            songLabel.setText(songFiles.get(index).getName());
            playButton.setText("Pause");
            Duration totalDuration = mediaPlayer.getMedia().getDuration();
            totalTimeLabel.setText(timeFormat.format(new Date((long) totalDuration.toMillis())));
        });

        mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> updateScrubBar(newValue));
        });

        mediaPlayer.setOnEndOfMedia(this::playNextSong);
    }

    private void updateScrubBar(Duration currentTime) {
        double progress = currentTime.toMillis() / mediaPlayer.getTotalDuration().toMillis();
        scrubBar.setValue((int) (progress * 100));
        currentTimeLabel.setText(timeFormat.format(new Date((long) currentTime.toMillis())));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new UI().setVisible(true);
        });
    }
}
