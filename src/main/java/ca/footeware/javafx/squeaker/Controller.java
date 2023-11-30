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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;

public class Controller implements Initializable {

    @FXML
    private Button playButton;
    @FXML
    private Button stopButton;
    @FXML
    private Button addButton;
    @FXML
    private TreeView tree;
    @FXML
    private TableView table;
    @FXML
    private TableColumn fileColumn;
    @FXML
    private TableColumn trackColumn;
    @FXML
    private TableColumn artistColumn;
    @FXML
    private TableColumn titleColumn;
    @FXML
    private TableColumn albumColumn;
    @FXML
    private TableColumn yearColumn;
    @FXML
    private TableColumn genreColumn;
    private TableViewSelectionModel<AudioFile> selectionModel;
    private MediaPlayer mediaPlayer;

    @FXML
    private void onAddButtonAction(ActionEvent event) {
        var selectedList = SelectionManager.getSelected();
        for (FileTreeItem fileTreeItem : selectedList) {
            File file = fileTreeItem.getValue().getFile();
            AudioFile audioFile = new AudioFile(file.toURI());
            fillTags(audioFile);
            table.getItems().add(audioFile);
        }
    }

    private File mp3ToWav(File mp3Data) throws UnsupportedAudioFileException, IOException {
        // open stream
        AudioInputStream mp3Stream = AudioSystem.getAudioInputStream(mp3Data);
        AudioFormat sourceFormat = mp3Stream.getFormat();
        // create audio format object for the desired stream/audio format
        // this is *not* the same as the file format (wav)
        AudioFormat convertFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                sourceFormat.getSampleRate(),
                16,
                sourceFormat.getChannels(),
                sourceFormat.getChannels() * 2,
                sourceFormat.getSampleRate(),
                false);
        // create stream that delivers the desired format
        AudioInputStream converted = AudioSystem.getAudioInputStream(convertFormat, mp3Stream);
        // write stream into a file with file format wav
        String tempFolder = System.getProperty("java.io.tmpdir");
        var newFile = new File(tempFolder + "/squeaker.wav");
        System.out.println("ca.footeware.javafx.squeaker.Controller.mp3ToWav(), newFile="+newFile);
        AudioSystem.write(converted, AudioFileFormat.Type.WAVE, newFile);
        return newFile;
    }

    @FXML
    private void onPlayButtonAction(ActionEvent event) {
        try {
            AudioFile selectedItem = selectionModel.getSelectedItem();
            File wav = mp3ToWav(selectedItem);
            URI uri = wav.toURI();
            Media media = new Media(uri.toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setAutoPlay(true);
        } catch (UnsupportedAudioFileException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void onStopButtonAction(ActionEvent event) {
        mediaPlayer.stop();
    }

    @FXML
    private void onTableSortAction(ActionEvent event) {
        System.out.println("ca.footeware.javafx.squeaker.Controller.onTableSortAction()");
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
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
        artistColumn.setCellValueFactory(new PropertyValueFactory<>("artist"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        albumColumn.setCellValueFactory(new PropertyValueFactory<>("album"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));
        genreColumn.setCellValueFactory(new PropertyValueFactory<>("genre"));
        selectionModel = table.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.SINGLE.SINGLE);
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
}
