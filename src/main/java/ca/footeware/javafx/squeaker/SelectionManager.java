package ca.footeware.javafx.squeaker;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Maintains the list of selected items.
 */
public class SelectionManager {

    private static final ObservableList<FileTreeItem> SELECTED = FXCollections.observableArrayList();

    /**
     * Hidden constructor.
     */
    private SelectionManager() {
    }

    public static void addSelectedItem(FileTreeItem item) {
        SELECTED.add(item);
    }

    public static void removeSelectedItem(FileTreeItem item) {
        SELECTED.remove(item);
    }

    public static ObservableList<FileTreeItem> getSelected() {
        return SELECTED;
    }

    public static void clear() {
        SELECTED.clear();
    }
}
