package ca.footeware.javafx.squeaker;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Maintains the list of selected items.
 */
public class SelectionManager {

    private static final ObservableList<FileTreeItem> selectedList = FXCollections.observableArrayList();

    /**
     * Hidden constructor.
     */
    private SelectionManager() {
    }

    public static void addSelectedItem(FileTreeItem item) {
        selectedList.add(item);
    }

    public static void removeSelectedItem(FileTreeItem item) {
        selectedList.remove(item);
    }

    public static ObservableList<FileTreeItem> getSelected() {
        return selectedList;
    }

    public static void clear() {
        selectedList.clear();
    }
}
