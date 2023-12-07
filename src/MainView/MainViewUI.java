package MainView;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;
import java.util.function.Consumer;

public class MainViewUI {
    private final JFrame frame;
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
    private final JList<String> playlist;
    private final DefaultListModel<String> playlistModel;

    public MainViewUI() {
        frame = new JFrame();

        playButton = new JButton("");
        nextButton = new JButton("Next");
        prevButton = new JButton("Previous");
        changePlaylistButton = new JButton("Change Playlist");
        repeatButton = new JButton("Repeat");
        shuffleButton = new JButton("Shuffle");

        songLabel = new JLabel("No song playing", SwingConstants.CENTER);
        scrubBar = new JSlider();
        currentTimeLabel = new JLabel("--:--");
        totalTimeLabel = new JLabel("--:--");

        playlistModel = new DefaultListModel<>();
        playlist = new JList<>(playlistModel);

        initUI();
    }

    private void initUI() {
        // Set up the JFrame
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 500);
        frame.setLayout(new BorderLayout(5, 5));

        // Set up the UI components
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(currentTimeLabel, BorderLayout.WEST);
        topPanel.add(scrubBar, BorderLayout.CENTER);
        topPanel.add(totalTimeLabel, BorderLayout.EAST);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(topPanel, BorderLayout.NORTH);
        centerPanel.add(songLabel, BorderLayout.CENTER);

        // Set up the UI layout for each button in the bottom panel
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.add(playButton);
        bottomPanel.add(nextButton);
        bottomPanel.add(prevButton);
        bottomPanel.add(changePlaylistButton);

        JPanel bottomPanel2 = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel2.add(repeatButton);
        bottomPanel2.add(shuffleButton);

        JPanel combinedBottomPanel = new JPanel(new BorderLayout());
        combinedBottomPanel.add(bottomPanel, BorderLayout.NORTH);
        combinedBottomPanel.add(bottomPanel2, BorderLayout.SOUTH);

        // Initialize playlist components
        JScrollPane playlistScrollPane = new JScrollPane(playlist);

        // Add components to the UI
        frame.add(playlistScrollPane, BorderLayout.CENTER);
        frame.add(centerPanel, BorderLayout.SOUTH);
        frame.add(combinedBottomPanel, BorderLayout.NORTH);
    }

    public void setPlayButtonActionListener(Consumer<ActionEvent> actionListener) {
        playButton.addActionListener(actionListener::accept);
    }

    public void setNextButtonActionListener(Consumer<ActionEvent> actionListener) {
        nextButton.addActionListener(actionListener::accept);
    }

    public void setPrevButtonActionListener(Consumer<ActionEvent> actionListener) {
        prevButton.addActionListener(actionListener::accept);
    }

    public void setChangePlaylistButtonActionListener(Consumer<ActionEvent> actionListener) {
        changePlaylistButton.addActionListener(actionListener::accept);
    }

    public void setRepeatButtonActionListener(Consumer<ActionEvent> actionListener) {
        repeatButton.addActionListener(actionListener::accept);
    }

    public void setShuffleButtonActionListener(Consumer<ActionEvent> actionListener) {
        shuffleButton.addActionListener(actionListener::accept);
    }

    public void setScrubBarMouseListener(MouseAdapter mouseAdapter) {
        scrubBar.addMouseListener(mouseAdapter);
    }

    public void setPlaylistSelectionListener(Consumer<ListSelectionEvent> selectionListener) {
        playlist.addListSelectionListener(e -> selectionListener.accept(e));
    }

    public void setVisible(boolean visible) {
        frame.setVisible(visible);
    }

    public void updateSongLabel(String songName) {
        songLabel.setText(songName);
    }

    public void updateTotalTimeLabel(String totalTime) {
        totalTimeLabel.setText(totalTime);
    }

    public void updateCurrentTimeLabel(String currentTime) {
        currentTimeLabel.setText(currentTime);
    }

    public void updateScrubBarValue(int value) {
        scrubBar.setValue(value);
    }

    public void setRepeatButtonBackground(Color color) {
        repeatButton.setBackground(color);
    }

    public void setShuffleButtonBackground(Color color) {
        shuffleButton.setBackground(color);
    }

    public void setRepeatButtonTextForeground(Color color) {
        repeatButton.setForeground(color);
    }

    public void setShuffleButtonTextForeground(Color color) {
        shuffleButton.setForeground(color);
    }

    public int getPlaylistSelectedIndex() {
        return playlist.getSelectedIndex();
    }

    public void updatePlaylistModel(List<File> songFiles) {
        playlistModel.clear();
        songFiles.forEach(song -> playlistModel.addElement(song.getName().replace(".mp3", "")));
    }

    public void updatePlayPauseButtonIcon(ImageIcon playPauseIcon) {
        playButton.setIcon(playPauseIcon);
    }

    public int getScrubBarWidth() {
        return scrubBar.getWidth();
    }

    public int getScrubBarMaximum() {
        return scrubBar.getMaximum();
    }

    // Method to set the JFrame title (e.g., "Media Player")
    public void setTitle(String title) {
        frame.setTitle(title);
    }
    
    public void setButtonIcon(JButton button, ImageIcon icon) {
        button.setIcon(icon);
    }

    // Call this method from MainView after creating the icons for each button
    public void setButtonIcons(
            ImageIcon playIcon,
            ImageIcon nextIcon,
            ImageIcon prevIcon,
            ImageIcon changePlaylistIcon,
            ImageIcon repeatIcon,
            ImageIcon shuffleIcon) {
        setButtonIcon(playButton, playIcon);
        setButtonIcon(nextButton, nextIcon);
        setButtonIcon(prevButton, prevIcon);
        setButtonIcon(changePlaylistButton, changePlaylistIcon);
        setButtonIcon(repeatButton, repeatIcon);
        setButtonIcon(shuffleButton, shuffleIcon);
    }
}
