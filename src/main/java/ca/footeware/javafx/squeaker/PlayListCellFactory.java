package ca.footeware.javafx.squeaker;

import java.io.File;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class PlayListCellFactory implements Callback<ListView<PlayList>, ListCell<PlayList>> {

    @Override
    public ListCell<PlayList> call(ListView<PlayList> param) {
        return new ListCell<>() {
            @Override
            public void updateItem(PlayList playList, boolean empty) {
                super.updateItem(playList, empty);
                if (empty || playList == null) {
                    setText(null);
                } else {
                    setText(playList.getName().substring(
                            playList.getName().lastIndexOf(File.separator) + 1, playList.getName().lastIndexOf(".m3u")));
                }
            }
        };
    }
}
