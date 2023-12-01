package ca.footeware.javafx.squeaker;

import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

/**
 * Maintains the list of selected items.
 */
public class SelectionManager {

    private static final ObservableSet<FileTreeItem> selectedSet = FXCollections.observableSet();

    /**
     * Hidden constructor.
     */
    private SelectionManager() {
    }

    public static void addSelectedItem(FileTreeItem item) {
        selectedSet.add(item);
    }

    public static void removeSelectedItem(FileTreeItem item) {
        selectedSet.remove(item);
    }

    public static ObservableSet<FileTreeItem> getSelected() {
        return selectedSet;
    }

    public static void clear() {
        selectedSet.clear();
    }
}
