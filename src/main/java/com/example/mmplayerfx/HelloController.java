package com.example.mmplayerfx;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import javafx.scene.control.*;
import javafx.scene.control.ToggleButton;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;

public class HelloController extends Component implements Initializable {
    @FXML
    private Label songLabel, volumeLabel, loopLabel;
    @FXML
    private Button playButton, pauseButton, resetButton, previousButton, nextButton, chooseFile, chooseFiles, removeFiles;
    @FXML
    private ComboBox<String> speedBox;
    @FXML
    private Slider volumeSlider;
    @FXML
    private ProgressBar songProgressBar;
    @FXML
    private ToggleButton loopButton;
    @FXML
    private ListView<String> listPlaylist = new ListView<>();

    private Media media;
    private MediaPlayer mediaPlayer;
    public File directory;
    private File[] files;
    private ArrayList<File> songs;
    private int songNumber;
    private int[] speeds = {25, 50, 75, 100, 125, 150, 175, 200};
    private Timer timer;
    private TimerTask task;
    private boolean running;
    private JFileChooser fileChooser;
    public JTextField filePathField;
    public String filePath = "files";
    private Boolean isLooping;
    private ArrayList<File> playlistPosition;
    private Boolean toggled;
    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        songs = new ArrayList<>();
        directory = new File(filePath);
        files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                songs.add(file);
                listPlaylist.getItems().add(file.getName());
            }
            showTextView();
        }
        media = new Media(songs.get(songNumber).toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        songLabel.setText(songs.get(songNumber).getName());
        for (int i = 0; i < speeds.length; i++) {
            speedBox.getItems().add((speeds[i]) + "%");
        }
        speedBox.setOnAction(this::changeSpeed);
        volumeSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
                mediaPlayer.setVolume(volumeSlider.getValue() * 0.01);
            }
        });
    }
    public void playMedia() { //plays the track
        beginTimer();
        changeSpeed(null);
        mediaPlayer.setVolume(volumeSlider.getValue() * 0.01);
        mediaPlayer.play();
    }
    public void stopMedia() { //stops playing the track
        cancelTimer();
        mediaPlayer.pause();
    }
    public void repeatMedia() { //restarts playing the track
        songProgressBar.setProgress(0);
        mediaPlayer.seek(Duration.seconds(0));
        beginTimer();
        mediaPlayer.play();
    }
    public void changeSpeed(ActionEvent event) { //changes speed
        if (speedBox.getValue() == null) {
            mediaPlayer.setRate(1);
        } else {
            mediaPlayer.setRate(Integer.parseInt(speedBox.getValue().substring(0, speedBox.getValue().length() - 1)) * 0.01);
        }
    }
    public void changeVolume() {
    }
    public void nextMedia() {
        if (songNumber < songs.size() - 1) { //if the number of tracks playing is smaller, than number of songs - 1, it adds 1 to it, getting to next song
            songNumber++;
            mediaPlayer.stop();
            media = new Media(songs.get(songNumber).toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            songLabel.setText(songs.get(songNumber).getName());
            playMedia();
            toggled = false;
            loopLabel.setText("Loop OFF");
        } else {
            songNumber = 0;
            mediaPlayer.stop();
            media = new Media(songs.get(songNumber).toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            songLabel.setText(songs.get(songNumber).getName());
            playMedia();
            toggled = false;
            loopLabel.setText("Loop OFF");
        }
    }
    public void previousMedia() {
        if (songNumber > 0) { //if the number of song playing is bigger, than 0, it retracts 1 from it, getting to previous song
            songNumber--;
            mediaPlayer.stop();
            media = new Media(songs.get(songNumber).toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            songLabel.setText(songs.get(songNumber).getName());
            playMedia();
            toggled = false;
            loopLabel.setText("Loop OFF");
        } else {
            songNumber = songs.size() - 1;
            mediaPlayer.stop();
            media = new Media(songs.get(songNumber).toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            songLabel.setText(songs.get(songNumber).getName());
            playMedia();
            toggled = false;
            loopLabel.setText("Loop OFF");
        }
    }
    public void beginTimer() {
        timer = new Timer();
        task = new TimerTask() {
            public void run() {
                running = true;
                double current = mediaPlayer.getCurrentTime().toSeconds();
                double end = media.getDuration().toSeconds();
                songProgressBar.setProgress(current / end);
                if (current / end == 1) { //when media player reaches end, it turns off
                    cancelTimer();
                }
            }
        };
        timer.scheduleAtFixedRate(task, 0, 1000);
    }
    public void cancelTimer() {
        if (timer != null) {
            running = false;
            timer.cancel();
            timer = null;  // Sets timer to null after stopping music
        }
    }
    private String newPath = filePath;
    public void chooseMedia() {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File("."));
        chooser.setDialogTitle("Select Folder");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        FileNameExtensionFilter filter = new FileNameExtensionFilter("Music Files", "mp3", "wav");
        chooser.setFileFilter(filter);

        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            newPath = chooser.getSelectedFile().getAbsolutePath().replace("\\", "\\\\");
            System.out.println(newPath);
            directory = new File(newPath);

            songs.clear();
            listPlaylist.getItems().clear();

            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && (file.getName().toLowerCase().endsWith(".mp3") || file.getName().toLowerCase().endsWith(".wav"))) {
                        songs.add(file);
                        listPlaylist.getItems().add(file.getName());
                        loopLabel.setText("Loop OFF");
                    }
                }
            }
        } else {
            System.out.println("No Selection");
        }
    }
    public void toggleLoop(){ //when loop button is selected, it starts checking, if the loop is at the end. When it reaches end, song duration sets on zero
        boolean toggled = loopButton.isSelected();

        if (toggled) {
            mediaPlayer.setOnEndOfMedia(() -> mediaPlayer.seek(Duration.ZERO));
            loopLabel.setText("Loop ON");
        } else {
            mediaPlayer.setOnEndOfMedia(null);
            loopButton.setSelected(false);
            loopLabel.setText("Loop OFF");
        }
    }
    public void showTextView() {
        listPlaylist.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
                if (newValue != null) {
                    int selectedIndex = listPlaylist.getSelectionModel().getSelectedIndex();
                    File selectedSong = songs.get(selectedIndex);
                    mediaPlayer.setVolume(volumeSlider.getValue() * 0.01);
                    String selectedSongPath = selectedSong.getAbsolutePath();
                    if (mediaPlayer != null) {
                        mediaPlayer.stop();
                        mediaPlayer.dispose();  // Dispose the old media player
                        cancelTimer();          // Stop old media player's timer
                    }

                    media = new Media(new File(selectedSongPath).toURI().toString());
                    mediaPlayer = new MediaPlayer(media);
                    mediaPlayer.play();
                    songLabel.setText(selectedSong.getName());
                    songNumber = selectedIndex;
                    System.out.println("Selected Song: " + selectedSong.getName());
                    beginTimer();// Start a new timer for the current media player
                    toggled = false;
                    loopLabel.setText("Loop OFF");
                }
            }
        });
    }
    String newMusic = "";
    public void chooseFile(){
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File("."));
        chooser.setDialogTitle("select files");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(true);

        FileNameExtensionFilter filter = new FileNameExtensionFilter("Music Files", "mp3", "wav"); //setting filter for mp3 and wav type files
        chooser.setFileFilter(filter);

        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = chooser.getSelectedFiles();

            if (selectedFiles != null) {//selecting files and then concerting their path to windows type path
                for (File file : selectedFiles) {
                    String path = chooser.getSelectedFile().toString().replace("\\", "\\\\");//converting here
                    Path filePath = Paths.get(file.getAbsolutePath());
                    songs.add(new File(path));//adding song to playlist
                    listPlaylist.getItems().add(filePath.getFileName().toString());
                }
            }
        } else {//if nothing was selected, No Selection gets printed
            System.out.println("No Selection ");
        }
    }
    public void removeFile() {//when button is pressed, currently pressed song gets stoppes, removed from ListView and removed from playlist
        int selectedIndex = listPlaylist.getSelectionModel().getSelectedIndex();
        if (selectedIndex != -1) {
            mediaPlayer.stop();
            listPlaylist.getItems().remove(selectedIndex);
            songs.remove(selectedIndex);
        } else {
            songLabel.setText("Nothing else can be removed");
        }
        showTextView();
    }
}