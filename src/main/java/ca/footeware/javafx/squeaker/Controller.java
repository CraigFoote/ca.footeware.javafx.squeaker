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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.ObservableSet;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Border;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

public class Controller implements Initializable {

    @FXML
    private Button backButton;
    @FXML
    private Button playButton;
    @FXML
    private Button forwardButton;
    @FXML
    private MenuButton menuButton;
    @FXML
    private Label statusLabel;
    @FXML
    private Label timeLabel;
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
    private ImageView menuImageView;
    private boolean playing = false;
    private boolean paused = false;
    private int currentRowIndex = -1;

    @FXML
    private void onAddButtonAction(ActionEvent event) {
        final ObservableSet<FileTreeItem> selectedSet = SelectionManager.getSelected();
        final Service process = new Service() {
            @Override
            protected Task createTask() {
                return new Task() {
                    @Override
                    protected ObservableSet<FileTreeItem> call() throws Exception {
                        int cunt = 0;
                        Platform.runLater(() -> {
                            statusLabel.setText("0 of " + selectedSet.size() + " added");
                        });
                        for (FileTreeItem fileTreeItem : selectedSet) {
                            final File file = fileTreeItem.getValue().getFile();
                            final AudioFile audioFile = new AudioFile(file.toURI());
                            fillTags(audioFile);
                            final int finalCunt = ++cunt;
                            Platform.runLater(() -> {
                                table.getItems().add(audioFile);
                                statusLabel.setText(finalCunt + " of " + selectedSet.size() + " added");
                            });
                        }
                        return selectedSet;
                    }
                };
            }
        };
        process.setOnSucceeded(e -> {
            table.getSortOrder().add(fileColumn);
            table.sort();
            SelectionManager.clear();
        });
        process.start();
    }

    @FXML
    private void onPlayButtonAction(ActionEvent event) {
        AudioFile selectedItem = tableSelectionModel.getSelectedItem();
        currentRowIndex = tableSelectionModel.getSelectedIndex();
        System.out.println("ca.footeware.javafx.squeaker.Controller.onPlayButtonAction(), selected=" + selectedItem);
        if (selectedItem == null) {
            if (!table.getItems().isEmpty()) {
                tableSelectionModel.select(0);
                selectedItem = tableSelectionModel.getSelectedItem();
            }
        }
        if (selectedItem != null) {
            System.out.println("ca.footeware.javafx.squeaker.Controller.onPlayButtonAction(), selectedItem=" + selectedItem);
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
                    System.out.println("ca.footeware.javafx.squeaker.Controller.onPlayButtonAction(), playing...");
                    playNewFile(selectedItem);
                    playButton.setGraphic(pauseImageView);
                    playing = true;
                    paused = false;
                }
                statusLabel.setText(selectedItem.getFilename());
            }
        }
    }

    @FXML
    private void onAboutButtonAction(ActionEvent event) {
        final Stage dialog = new Stage();
        dialog.setResizable(false);
        dialog.initOwner(menuButton.getParent().getScene().getWindow());
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);
        final ImageView imageView = new ImageView(new Image("programmer.jpg"));
        final Hyperlink link = new Hyperlink("Another fine mess by Footeware.ca");
        link.setOnAction(e -> {
            try {
                new ProcessBuilder("x-www-browser", "http://Footeware.ca").start();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        link.setBorder(Border.EMPTY);
        link.setStyle("-fx-text-fill: #d8dee9;");
        final Button closeButton = new Button("Close");
        closeButton.setOnAction((e) -> dialog.close());
        closeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #d8dee9;");;
        final VBox vBox = new VBox(10, imageView, link, closeButton);
        vBox.setAlignment(Pos.BASELINE_CENTER);
        vBox.setStyle("-fx-background-color: #585c5f;");
        dialog.setScene(new Scene(vBox, 245, 325));
        dialog.showAndWait();
    }

    @FXML
    private void onBackButtonAction(ActionEvent e) {
        System.out.println("ca.footeware.javafx.squeaker.Controller.onBackButtonAction(), current=" + currentRowIndex);
        if (currentRowIndex > 0) {
            if (playing || paused) {
                mediaPlayer.stop();
            }
            currentRowIndex--;
            tableSelectionModel.select(currentRowIndex);
            final AudioFile selectedItem = tableSelectionModel.getSelectedItem();
            System.out.println("ca.footeware.javafx.squeaker.Controller.onBackButtonAction(), playing " + selectedItem);
            playNewFile(selectedItem);
            statusLabel.setText(selectedItem.getFilename());
            playing = true;
            paused = false;
        }
    }

    @FXML
    private void onForwardButtonAction(ActionEvent e) {
        if (currentRowIndex < table.getItems().size() - 1) {
            if (playing || paused) {
                mediaPlayer.stop();
            }
            currentRowIndex++;
            tableSelectionModel.select(currentRowIndex);
            final AudioFile selectedItem = tableSelectionModel.getSelectedItem();
            playNewFile(selectedItem);
            statusLabel.setText(selectedItem.getFilename());
            playing = true;
            paused = false;
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // back button
        backButton.setGraphic(new ImageView(new Image("backbutton.png")));

        // forward button
        forwardButton.setGraphic(new ImageView(new Image("forwardbutton.png")));

        // play/pause button
        final Image playImage = new Image("playbutton.png");
        final Image pauseImage = new Image("pausebutton.png");
        playImageView = new ImageView(playImage);
        pauseImageView = new ImageView(pauseImage);
        playButton.setGraphic(playImageView);

        // menu button
        final Image menuImage = new Image("menubutton.png");
        menuImageView = new ImageView(menuImage);
        menuButton.setGraphic(menuImageView);

        // tree
        final TreeItem<FileWrapper> root = new TreeItem<>(null);
        final Iterable<Path> rootFolderPaths = FileSystems.getDefault().getRootDirectories();
        for (Path rootFolderPath : rootFolderPaths) {
            final File file = rootFolderPath.toFile();
            final FileTreeItem rootTreeItem = new FileTreeItem(new FileWrapper(file));
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
        final ContextMenu contextMenu = new ContextMenu();
        // remove selected
        final MenuItem removeSelectedMenuItem = new MenuItem("Remove selected");
        removeSelectedMenuItem.setOnAction((event) -> {
            AudioFile selectedItem = tableSelectionModel.getSelectedItem();
            table.getItems().remove(selectedItem);
        });
        // remove all
        final MenuItem removeAllMenuItem = new MenuItem("Remove all");
        removeAllMenuItem.setOnAction((event) -> {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Confirm");
            alert.setHeaderText(null);
            alert.setContentText("Remove all items?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                table.getItems().clear();
            }
        });
        contextMenu.getItems().addAll(removeSelectedMenuItem, removeAllMenuItem);
        table.setContextMenu(contextMenu);
    }

    private void fillTags(AudioFile audioFile) {
        try {
            final Mp3File mp3File = new Mp3File(audioFile.getPath());
            if (mp3File.hasId3v2Tag()) {
                final ID3v2 tag = mp3File.getId3v2Tag();
//              byte[] imageData = id3v2tag.getAlbumImage();
//              BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageData));
                audioFile.setTrack(tag.getTrack());
                audioFile.setTitle(tag.getTitle());
                audioFile.setArtist(tag.getArtist());
                audioFile.setAlbum(tag.getAlbum());
                audioFile.setYear(tag.getYear());
                audioFile.setGenre(tag.getGenreDescription());
            }
            audioFile.setSeconds(mp3File.getLengthInSeconds());
        } catch (IOException | UnsupportedTagException | InvalidDataException ex) {
            ex.printStackTrace();
        }
    }

    private File mp3ToWav(File mp3) throws UnsupportedAudioFileException, IOException {
        System.out.println("ca.footeware.javafx.squeaker.Controller.mp3ToWav(), mp3="+mp3);
        final AudioInputStream mp3Stream = AudioSystem.getAudioInputStream(mp3);
        System.out.println("ca.footeware.javafx.squeaker.Controller.mp3ToWav(), mp3Stream="+mp3Stream);
        final AudioFormat sourceFormat = mp3Stream.getFormat();
        System.out.println("ca.footeware.javafx.squeaker.Controller.mp3ToWav(), format="+sourceFormat);
        final AudioFormat convertFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                sourceFormat.getSampleRate(),
                16,
                sourceFormat.getChannels(),
                sourceFormat.getChannels() * 2,
                sourceFormat.getSampleRate(),
                false);
        System.out.println("ca.footeware.javafx.squeaker.Controller.mp3ToWav(), converting to wav...");
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
                        System.out.println(".call()");
                        // convert to wav because mp3 doesn't play
                        final File wav = mp3ToWav(audioFile);
                        System.out.println(".call(), wav=" + wav.toURI());
                        return wav.toURI();
                    }
                };
            }
        };
        process.setOnSucceeded(e -> {
            // get value returned from process
            final URI uri = (URI) process.getValue();
            System.out.println("ca.footeware.javafx.squeaker.Controller.playNewFile(), uri=" + uri);
            final Media media = new Media(uri.toString());
            System.out.println("ca.footeware.javafx.squeaker.Controller.playNewFile(), media=" + media);
            mediaPlayer = new MediaPlayer(media);
            System.out.println("ca.footeware.javafx.squeaker.Controller.playNewFile(), mediaPlayer=" + mediaPlayer);
            mediaPlayer.setAutoPlay(true);
            statusLabel.setText(audioFile.getFilename());
            timeLabel.setText(audioFile.getFormattedTime());
            mediaPlayer.setOnEndOfMedia(new Runnable() {
                @Override
                public void run() {
                    if (table.getItems().size() > currentRowIndex + 1) {
                        currentRowIndex++;
                        tableSelectionModel.select(currentRowIndex);
                        final AudioFile selectedItem = tableSelectionModel.getSelectedItem();
                        playNewFile(selectedItem);
                    } else {
                        playButton.setGraphic(playImageView);
                        playing = false;
                    }
                }
            });
        });
        System.out.println("ca.footeware.javafx.squeaker.Controller.playNewFile(), process starting...");
        process.start();
    }
}
