package ca.footeware.javafx.squeaker;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.control.Slider;
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
import javafx.util.Duration;

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
    private Slider slider;
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
    @FXML
    private TableColumn<AudioFile, String> durationColumn;

    private TableViewSelectionModel<AudioFile> tableSelectionModel;
    private ImageView playImageView;
    private ImageView pauseImageView;
    private final AtomicBoolean playing = new AtomicBoolean(false);
    private final AtomicBoolean paused = new AtomicBoolean(false);
    private final AtomicInteger currentRowIndex = new AtomicInteger(0);
    private AudioFile currentAudioFile;
    private MediaPlayer player;

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
    private void onPlayButtonAction(ActionEvent event) throws MalformedURLException {
        currentAudioFile = tableSelectionModel.getSelectedItem();
        currentRowIndex.set(tableSelectionModel.getSelectedIndex());
        if (currentAudioFile == null) {
            if (!table.getItems().isEmpty()) {
                tableSelectionModel.clearAndSelect(0);
                currentAudioFile = tableSelectionModel.getSelectedItem();
                currentRowIndex.set(tableSelectionModel.getSelectedIndex());
            }
        }
        if (currentAudioFile != null) {
            // if playing pause
            if (playing.get()) {
                pause();
            } else {
                // not playing, unpause if paused else play new
                if (paused.get()) {
                    unpause();
                } else {
                    // play current file from start
                    play();
                }
            };
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
        if (currentRowIndex.get() > 0) {
            if (playing.get() || paused.get()) {
                // playing or paused
                stop();
            }
            // select previous row
            currentRowIndex.set(currentRowIndex.get() - 1);
            tableSelectionModel.clearAndSelect(currentRowIndex.get());
            currentAudioFile = tableSelectionModel.getSelectedItem();
            play();
        }
    }

    @FXML
    private void onForwardButtonAction(ActionEvent e) {
        if (currentRowIndex.get() < table.getItems().size() - 1) {
            // playing or paused
            if (playing.get() || paused.get()) {
                stop();
            }
            // select next row
            currentRowIndex.set(currentRowIndex.get() + 1);
            tableSelectionModel.clearAndSelect(currentRowIndex.get());
            currentAudioFile = tableSelectionModel.getSelectedItem();
            play();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // back button
        backButton.setGraphic(new ImageView(new Image("backbutton.png")));

        // play/pause button
        final Image playImage = new Image("playbutton.png");
        final Image pauseImage = new Image("pausebutton.png");
        playImageView = new ImageView(playImage);
        pauseImageView = new ImageView(pauseImage);
        playButton.setGraphic(playImageView);

        // forward button
        forwardButton.setGraphic(new ImageView(new Image("forwardbutton.png")));

        // slider
        slider.valueProperty().addListener(this::sliderChanged);
        slider.setSnapToTicks(true);

        // menu button
        final Image menuImage = new Image("menubutton.png");
        final ImageView menuImageView = new ImageView(menuImage);
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
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("formattedTime"));
        tableSelectionModel = table.getSelectionModel();
        tableSelectionModel.setSelectionMode(SelectionMode.SINGLE);
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

    private void sliderChanged(ObservableValue<? extends Number> property, Number oldValue, Number newValue) {
        Platform.runLater(() -> timeLabel.setText(Utils.formatTime(newValue.longValue())
                + " : " + Utils.formatTime(currentAudioFile.getSeconds())));
    }

    private void play() {
        final Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Media media = new Media(currentAudioFile.toURI().toURL().toExternalForm());
                player = new MediaPlayer(media);
                slider.setMin(0.0);
                slider.setMax(currentAudioFile.getSeconds());
                slider.setValue(0.0);
                player.currentTimeProperty().addListener(new ChangeListener<Duration>() {
                    @Override
                    public void changed(ObservableValue<? extends Duration> ov, Duration t, Duration t1) {
                        slider.setValue(t1.toSeconds());
                    }
                });
                player.play();
                playing.set(true);
                paused.set(false);
                return null;
            }
        };
        new Thread(task).start();
    }

    private void stop() {
        Platform.runLater(() -> playButton.setGraphic(playImageView));
        player.stop();
        paused.set(false);
        playing.set(false);
    }

    private void pause() {
        Platform.runLater(() -> playButton.setGraphic(playImageView));
        player.pause();
        playing.set(false);
        paused.set(true);
    }

    private void unpause() {
        Platform.runLater(() -> playButton.setGraphic(pauseImageView));
        player.play();
        playing.set(true);
        paused.set(false);
    }
}
