package ca.footeware.javafx.squeaker;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.ObservableSet;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

public class Controller implements Initializable {

    @FXML
    private Button playButton;
    @FXML
    private Label playingLabel;
    @FXML
    private Button addButton;
    @FXML
    private TreeView tree;
    @FXML
    private TableView<AudioFile> table;
    @FXML
    private TableColumn<AudioFile, String> fileColumn;
    @FXML
    private TableColumn<AudioFile, String> trackColumn;
    @FXML
    private TableColumn<AudioFile, String> artistColumn;
    @FXML
    private TableColumn<AudioFile, String> titleColumn;
    @FXML
    private TableColumn<AudioFile, String> albumColumn;
    @FXML
    private TableColumn<AudioFile, String> yearColumn;
    @FXML
    private TableColumn<AudioFile, String> genreColumn;

    private TableViewSelectionModel<AudioFile> tableSelectionModel;
    private MediaPlayer mediaPlayer;
    private ImageView playImageView;
    private ImageView pauseImageView;
    private boolean playing = false;
    private boolean paused = false;

    @FXML
    private void onAddButtonAction(ActionEvent event) {
        final ObservableSet<FileTreeItem> selectedSet = SelectionManager.getSelected();
        Service process = new Service() {
            @Override
            protected Task createTask() {
                return new Task() {
                    @Override
                    protected ObservableSet<FileTreeItem> call() throws Exception {
                        int cunt = 0;
                        Platform.runLater(() -> {
                            playingLabel.setText("0 of " + selectedSet.size() + " added");
                        });
                        for (FileTreeItem fileTreeItem : selectedSet) {
                            final File file = fileTreeItem.getValue().getFile();
                            final AudioFile audioFile = new AudioFile(file.toURI());
                            fillTags(audioFile);
                            final int finalCunt = ++cunt;
                            Platform.runLater(() -> {
                                table.getItems().add(audioFile);
                                playingLabel.setText(finalCunt + " of " + selectedSet.size() + " added");
                            });
                        }
                        return selectedSet;
                    }
                };
            }
        };
        process.setOnSucceeded(e -> {
            ObservableSet<FileTreeItem> list = (ObservableSet<FileTreeItem>) process.getValue();
            table.getSortOrder().add(fileColumn);
            table.sort();
            SelectionManager.clear();
        });
        process.start();
    }

    @FXML
    private void onPlayButtonAction(ActionEvent event) {
        final AudioFile selectedItem = tableSelectionModel.getSelectedItem();
        if (selectedItem != null) {
            if (playing) {
                // pause
                playing = false;
                paused = true;
                mediaPlayer.pause();
                playButton.setGraphic(playImageView);
            } else {
                // not playing, play if paused else play new
                if (paused) {
                    mediaPlayer.play();
                    paused = false;
                    playing = true;
                    playButton.setGraphic(pauseImageView);
                } else {
                    // play new file from start
                    playNewFile(selectedItem);
                    playButton.setGraphic(pauseImageView);
                    playingLabel.setText(selectedItem.getFilename());
                    playing = true;
                    paused = false;
                }
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // play/pause button
        Image playImage = new Image("playbutton.png");
        Image pauseImage = new Image("pausebutton.png");
        playImageView = new ImageView(playImage);
        pauseImageView = new ImageView(pauseImage);
        playButton.setGraphic(playImageView);

        // tree
        TreeItem<FileWrapper> root = new TreeItem<>(null);
        Iterable<Path> rootFolderPaths = FileSystems.getDefault().getRootDirectories();
        for (Path rootFolderPath : rootFolderPaths) {
            File file = rootFolderPath.toFile();
            FileTreeItem rootTreeItem = new FileTreeItem(new FileWrapper(file));
            root.getChildren().add(rootTreeItem);
        }
        root.setExpanded(true);
        tree.setRoot(root);
        tree.setShowRoot(false);
        tree.setCellFactory(CheckBoxTreeCell.<String>forTreeView());

        // table
        fileColumn.setCellValueFactory(new PropertyValueFactory<>("filename"));
        trackColumn.setCellValueFactory(new PropertyValueFactory<>("track"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        artistColumn.setCellValueFactory(new PropertyValueFactory<>("artist"));
        albumColumn.setCellValueFactory(new PropertyValueFactory<>("album"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));
        genreColumn.setCellValueFactory(new PropertyValueFactory<>("genre"));
        tableSelectionModel = table.getSelectionModel();
        tableSelectionModel.setSelectionMode(SelectionMode.SINGLE.SINGLE);
        // table context menu
        ContextMenu contextMenu = new ContextMenu();
        MenuItem menuItem = new MenuItem("Clear all");
        menuItem.setOnAction((event) -> table.getItems().clear());
        contextMenu.getItems().add(menuItem);
        table.setContextMenu(contextMenu);
    }

    private void fillTags(AudioFile audioFile) {
        try {
            Mp3File mp3File = new Mp3File(audioFile.getPath());
            if (mp3File.hasId3v2Tag()) {
                ID3v2 tag = mp3File.getId3v2Tag();
//              byte[] imageData = id3v2tag.getAlbumImage();
//              BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageData));
                audioFile.setTrack(tag.getTrack());
                audioFile.setTitle(tag.getTitle());
                audioFile.setArtist(tag.getArtist());
                audioFile.setAlbum(tag.getAlbum());
                audioFile.setYear(tag.getYear());
                audioFile.setGenre(tag.getGenreDescription());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (UnsupportedTagException ex) {
            ex.printStackTrace();
        } catch (InvalidDataException ex) {
            ex.printStackTrace();
        }
    }

    private File mp3ToWav(File mp3Data) throws UnsupportedAudioFileException, IOException {
        final AudioInputStream mp3Stream = AudioSystem.getAudioInputStream(mp3Data);
        final AudioFormat sourceFormat = mp3Stream.getFormat();
        final AudioFormat convertFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                sourceFormat.getSampleRate(),
                16,
                sourceFormat.getChannels(),
                sourceFormat.getChannels() * 2,
                sourceFormat.getSampleRate(),
                false);
        final AudioInputStream converted = AudioSystem.getAudioInputStream(convertFormat, mp3Stream);
        final String tempFolder = System.getProperty("java.io.tmpdir");
        final var newFile = new File(tempFolder + "/squeaker.wav");
        AudioSystem.write(converted, AudioFileFormat.Type.WAVE, newFile);
        return newFile;
    }

    private void playNewFile(AudioFile audioFile) {
        final Service process = new Service() {
            @Override
            protected Task createTask() {
                return new Task() {
                    @Override
                    protected URI call() throws Exception {
                        // convert to wav because mp3 doesn't play
                        final File wav = mp3ToWav(audioFile);
                        return wav.toURI();
                    }
                };
            }
        };
        process.setOnSucceeded(e -> {
            // get value returned from process
            final URI uri = (URI) process.getValue();
            final Media media = new Media(uri.toString());
            mediaPlayer = new MediaPlayer(media);
            // smack that ass!
            mediaPlayer.setAutoPlay(true);
        });
        process.start();
    }
}
