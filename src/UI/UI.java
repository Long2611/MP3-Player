package UI;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.DirectoryChooser;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.io.File;

public class UI extends Application {

    private String selectedDirectory;
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;
    private boolean isPaused = false;
    private Label lblNewLabel;
    private Button playButton;
    private int pausePosition = 0; // Track the pause position

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("MP3 Player");

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select a directory");
        File directory = directoryChooser.showDialog(primaryStage);
        if (directory != null) {
            selectedDirectory = directory.getAbsolutePath();
        }

        playButton = new Button("Play");
        playButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                if (!isPlaying) {
                    if (!isPaused) {
                        playFirstSong(selectedDirectory, 0); // Pass 0 as the initial frame position
                    } else {
                        // Resume from the pause position
                        resumeMusic(pausePosition);
                    }
                } else {
                    pauseMusic();
                }
            }
        });

        lblNewLabel = new Label("Current Song: ");
        BorderPane root = new BorderPane();
        root.setCenter(playButton);
        root.setTop(lblNewLabel);

        Scene scene = new Scene(root, 450, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void playFirstSong(String directory, int framePosition) {
        File dir = new File(directory);
        File[] files = dir.listFiles();

        if (files != null && files.length > 0) {
            for (File file : files) {
                if (file.getName().toLowerCase().endsWith(".mp3")) {
                    Media media = new Media(file.toURI().toString());
                    mediaPlayer = new MediaPlayer(media);

                    mediaPlayer.setOnEndOfMedia(() -> {
                        // Capture the current frame position
                        pausePosition = (int) mediaPlayer.getCurrentTime().toSeconds();
                        System.out.println(pausePosition);
                    });

                    isPlaying = true;
                    isPaused = false;
                    updateSongLabel(file.getName());
                    mediaPlayer.play();
                    playButton.setText("Pause");

                    break;
                }
            }
        }
    }

    private void pauseMusic() {
        if (isPlaying && mediaPlayer != null) {
            mediaPlayer.pause();
            isPlaying = false;
            isPaused = true;
            playButton.setText("Resume");
        }
    }

    private void resumeMusic(int framePosition) {
        if (isPaused) {
            isPaused = false;
            playButton.setText("Pause");
            playFirstSong(selectedDirectory, framePosition);
        }
    }

    private void updateSongLabel(String songName) {
        lblNewLabel.setText("Current Song: " + songName);
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }
}
