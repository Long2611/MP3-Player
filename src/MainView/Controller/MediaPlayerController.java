package MainView.Controller;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



public class MediaPlayerController {
    private MediaPlayer mediaPlayer;
    private List<File> songFiles;
    private int currentSongIndex = -1;
    private PlaybackMode currentMode = PlaybackMode.NORMAL;
    
    public enum PlaybackMode {NORMAL, REPEAT, SHUFFLE}
    
    public interface SongPlaybackListener {
        void onSongChanged(String songName, Duration totalDuration);
    }
    private SongPlaybackListener songPlaybackListener;
    public MediaPlayerController() {
        songFiles = new ArrayList<>();
    }
    
    public void setSongPlaybackListener(SongPlaybackListener listener) {
        this.songPlaybackListener = listener;
    }
    public void setSongFiles(List<File> files) {
        songFiles = files;
        currentSongIndex = -1;
        if (!songFiles.isEmpty()) {
            playNextSong();
        }
    }

    public void playPause() {
        if (mediaPlayer != null) {
            MediaPlayer.Status status = mediaPlayer.getStatus();
            if (status == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
            } else {
                mediaPlayer.play();
            }
        }
    }

    public void playNextSong() {
        if (!songFiles.isEmpty()) {
            if (currentMode == PlaybackMode.SHUFFLE) {
                Collections.shuffle(songFiles);
                currentSongIndex = 0;
            } else {
                currentSongIndex = (currentSongIndex + 1) % songFiles.size();
            }
            playSong(currentSongIndex);
        }
    }

    public void playPreviousSong() {
        if (!songFiles.isEmpty()) {
            currentSongIndex = (currentSongIndex - 1 + songFiles.size()) % songFiles.size();
            playSong(currentSongIndex);
        }
    }

    public void playSong(int index) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }

        Media media = new Media(songFiles.get(index).toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setOnReady(() -> mediaPlayer.play());

        mediaPlayer.setOnEndOfMedia(() -> {
            if (currentMode == PlaybackMode.REPEAT) {
                mediaPlayer.seek(Duration.ZERO);
            } else {
                playNextSong();
            }
        });
        
        // Notify listener
        if (songPlaybackListener != null) {
            String songName = songFiles.get(index).getName();
            songPlaybackListener.onSongChanged(songName, mediaPlayer.getTotalDuration());
        }
    }

    public void toggleShuffleMode() {
        if (currentMode != PlaybackMode.SHUFFLE) {
            currentMode = PlaybackMode.SHUFFLE;
        } else {
            currentMode = PlaybackMode.NORMAL;
        }
    }

    public void toggleRepeatMode() {
        if (currentMode != PlaybackMode.REPEAT) {
            currentMode = PlaybackMode.REPEAT;
        } else {
            currentMode = PlaybackMode.NORMAL;
        }
    }

    public MediaPlayer.Status getPlaybackStatus() {
        if (mediaPlayer != null) {
            return mediaPlayer.getStatus();
        }
        return MediaPlayer.Status.STOPPED;
    }

    public int getCurrentSongIndex() {
        return currentSongIndex;
    }

    public PlaybackMode getCurrentMode() {
        return currentMode;
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    public void seek(Duration duration) {
        if (mediaPlayer != null) {
            mediaPlayer.seek(duration);
        }
    }

    public Duration getCurrentTime() {
        return mediaPlayer != null ? mediaPlayer.getCurrentTime() : Duration.ZERO;
    }

    public Duration getTotalDuration() {
        return mediaPlayer != null ? mediaPlayer.getTotalDuration() : Duration.ZERO;
    }

    // Other necessary methods...
}
