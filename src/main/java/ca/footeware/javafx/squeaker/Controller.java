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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
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
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
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
    private TreeView fileTreeView;
    @FXML
    private TableView<Audio> tableView;
    @FXML
    private TableColumn<Audio, String> fileColumn;
    @FXML
    private TableColumn<Audio, String> trackColumn;
    @FXML
    private TableColumn<Audio, String> artistColumn;
    @FXML
    private TableColumn<Audio, String> titleColumn;
    @FXML
    private TableColumn<Audio, String> albumColumn;
    @FXML
    private TableColumn<Audio, String> yearColumn;
    @FXML
    private TableColumn<Audio, String> genreColumn;
    @FXML
    private TableColumn<Audio, String> durationColumn;
    @FXML
    private ListView<PlayList> playListView;

    private static final String APP_FOLDER = System.getProperty("user.home") + File.separator
            + ".local" + File.separator + "share" + File.separator + "squeaker" + File.separator;
    private ImageView playImageView;
    private ImageView pauseImageView;
    private final AtomicBoolean playing = new AtomicBoolean(false);
    private final AtomicBoolean paused = new AtomicBoolean(false);
    private final AtomicInteger currentRowIndex = new AtomicInteger(0);
    private Audio currentAudio;
    private MediaPlayer player;

    @FXML
    private void onLoadPlayListButtonAction(ActionEvent action) {
        PlayList selectedPlayList = playListView.getSelectionModel().getSelectedItem();
        tableView.getItems().addAll(selectedPlayList.getItems());
    }

    @FXML
    private void onSavePlayListButtonAction(ActionEvent event) {
        // prompt for playlist name
        final TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText(null);
        dialog.setTitle("Save Playlist");
        dialog.setContentText("Enter a name for this new playlist.");
        final Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(ActionEvent.ACTION, e -> {
            if (!playlistNameIsValid(dialog.getEditor().getText())) {
                e.consume();
                dialog.setContentText("That name already exists, please choose another.");
            }
        });
        dialog.showAndWait();
        final String filename = dialog.getEditor().getText().trim() + ".m3u";
        final String pathname = APP_FOLDER + "playlists" + File.separator + filename;
        // create and populate playlist
        PlayList playList = new PlayList(pathname);
        for (Audio audio : tableView.getItems()) {
            playList.getItems().add(audio);
        }
        writeToDisk(playList);
        playListView.getItems().add(playList);
    }

    @FXML
    private void onAddButtonAction(ActionEvent event) {
        final ObservableList<FileTreeItem> selectedSet = SelectionManager.getSelected();
        final Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                int cunt = 0;
                Platform.runLater(() -> statusLabel.setText("0 of " + selectedSet.size() + " added"));
                for (FileTreeItem fileTreeItem : selectedSet) {
                    final File file = fileTreeItem.getValue();
                    final Audio audio = new Audio(file.toPath());
                    fillTags(audio);
                    final int finalCunt = ++cunt;
                    Platform.runLater(() -> {
                        tableView.getItems().add(audio);
                        statusLabel.setText(finalCunt + " of " + selectedSet.size() + " added");
                    });
                }
                Platform.runLater(() -> {
                    tableView.getSortOrder().add(fileColumn);
                    tableView.sort();
                });
                return null;
            }
        };
        new Thread(task).start();
    }

    @FXML
    private void onPlayButtonAction(ActionEvent event) throws MalformedURLException {
        currentAudio = tableView.getSelectionModel().getSelectedItem();
        currentRowIndex.set(tableView.getSelectionModel().getSelectedIndex());
        if (currentAudio == null && !tableView.getItems().isEmpty()) {
            tableView.getSelectionModel().clearAndSelect(0);
            currentAudio = tableView.getSelectionModel().getSelectedItem();
            currentRowIndex.set(tableView.getSelectionModel().getSelectedIndex());
        }
        if (currentAudio != null) {
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
            tableView.getSelectionModel().clearAndSelect(currentRowIndex.get());
            currentAudio = tableView.getSelectionModel().getSelectedItem();
            play();
        }
    }

    @FXML
    private void onForwardButtonAction(ActionEvent e) {
        if (currentRowIndex.get() < tableView.getItems().size() - 1) {
            // playing or paused
            if (playing.get() || paused.get()) {
                stop();
            }
            // select next row
            currentRowIndex.set(currentRowIndex.get() + 1);
            tableView.getSelectionModel().clearAndSelect(currentRowIndex.get());
            currentAudio = tableView.getSelectionModel().getSelectedItem();
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

        // file tree
        final TreeItem<File> root = new TreeItem<>(null);
        final Iterable<Path> rootFolderPaths = FileSystems.getDefault().getRootDirectories();
        for (Path rootFolderPath : rootFolderPaths) {
            final File file = rootFolderPath.toFile();
            final FileTreeItem rootTreeItem = new FileTreeItem(file);
            root.getChildren().add(rootTreeItem);
            rootTreeItem.setExpanded(true);
        }
        root.setExpanded(true);
        fileTreeView.setRoot(root);
        fileTreeView.setShowRoot(false);
        // truncate file label to filename
        fileTreeView.setCellFactory(param -> new CheckBoxTreeCell<File>() {
            @Override
            public void updateItem(File item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty && item.exists() && item.canRead()) {
                    setText(item.getName());
                }
            }
        });

        // playlists
        final ContextMenu playListViewContextMenu = new ContextMenu();
        readPlayListsFromDisk();
        // context menu
        final MenuItem removeSelectedPlaylistMenuItem = new MenuItem("Delete");
        removeSelectedPlaylistMenuItem.setOnAction((event) -> {
            final PlayList selectedItem = playListView.getSelectionModel().getSelectedItem();
            final Alert alert = new Alert(AlertType.CONFIRMATION, "Delete playlist?", ButtonType.YES, ButtonType.NO);
            alert.showAndWait();
            if (alert.getResult() == ButtonType.YES) {
                try {
                    Files.delete(Path.of(APP_FOLDER, "playlists", selectedItem.getName()));
                    playListView.getItems().remove(selectedItem);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        playListViewContextMenu.getItems().add(removeSelectedPlaylistMenuItem);
        playListView.setContextMenu(playListViewContextMenu);
        // cell renderer
        playListView.setCellFactory(new PlayListCellFactory());

        // table
        fileColumn.setCellValueFactory(new PropertyValueFactory<>("filename"));
        trackColumn.setCellValueFactory(new PropertyValueFactory<>("track"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        artistColumn.setCellValueFactory(new PropertyValueFactory<>("artist"));
        albumColumn.setCellValueFactory(new PropertyValueFactory<>("album"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));
        genreColumn.setCellValueFactory(new PropertyValueFactory<>("genre"));
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("formattedTime"));
        tableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        // table context menu
        final ContextMenu tableViewContextMenu = new ContextMenu();
        // remove selected
        final MenuItem removeSelectedMenuItem = new MenuItem("Remove selected");
        removeSelectedMenuItem.setOnAction((event) -> {
            final Audio selectedItem = tableView.getSelectionModel().getSelectedItem();
            tableView.getItems().remove(selectedItem);
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
                tableView.getItems().clear();
            }
        });
        tableViewContextMenu.getItems().addAll(removeSelectedMenuItem, removeAllMenuItem);
        tableView.setContextMenu(tableViewContextMenu);
    }

    private void fillTags(Audio audioFile) {
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
            Path filename = audioFile.getPath().getName(audioFile.getPath().getNameCount() - 1);
            audioFile.setFilename(filename.toString());
        } catch (IOException | UnsupportedTagException | InvalidDataException ex) {
            ex.printStackTrace();
        }
    }

    private void sliderChanged(ObservableValue<? extends Number> property, Number oldValue, Number newValue) {
        Platform.runLater(() -> timeLabel.setText(Utils.formatTime(newValue.longValue())
                + " : " + Utils.formatTime(currentAudio.getSeconds())));
    }

    private void play() {
        final Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Media media = new Media(currentAudio.getPath().toUri().toURL().toString());
                player = new MediaPlayer(media);
                slider.setMin(0.0);
                slider.setMax(currentAudio.getSeconds());
                slider.setValue(0.0);
                player.currentTimeProperty().addListener(new ChangeListener<Duration>() {
                    @Override
                    public void changed(ObservableValue<? extends Duration> ov, Duration t, Duration t1) {
                        slider.setValue(t1.toSeconds());
                    }
                });
                Platform.runLater(() -> playButton.setGraphic(pauseImageView));
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

    private boolean playlistNameIsValid(String name) {
        File file = new File(APP_FOLDER + "playlists");
        String[] existingNamesArray = file.list((File dir, String name1) -> name1.endsWith(".m3u"));
        for (String existingName : existingNamesArray) {
            if (existingName.equals(name + ".m3u")) {
                return false;
            }
        }
        return true;
    }

    private void readPlayListsFromDisk() {
        final Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // load playlists from m3u files
                final File folder = new File(APP_FOLDER + "playlists");
                File[] m3uFiles = folder.listFiles((File dir, String name) -> name.endsWith(".m3u"));
                for (File m3uFile : m3uFiles) {
                    List<String> allLines = Files.readAllLines(Path.of(m3uFile.toURI()));
                    PlayList playList = new PlayList(m3uFile.getName());
                    for (String audioPath : allLines) {
                        final Audio audioFile = new Audio(Path.of(audioPath));
                        fillTags(audioFile);
                        playList.getItems().add(audioFile);
                    }
                    Platform.runLater(() -> {
                        playListView.getItems().add(playList);
                    });
                }
                return null;
            }
        };
        new Thread(task).start();
    }

    private void writeToDisk(PlayList playList) {
        try {
            final Path folderPath = Path.of(APP_FOLDER + "playlists");
            Files.createDirectories(folderPath);
            final Path playListFile = Files.createFile(folderPath.resolve(playList.getName()));
            for (Audio audio : playList.getItems()) {
                Files.writeString(playListFile, audio.getPath().toString() + System.lineSeparator(), StandardOpenOption.APPEND);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
