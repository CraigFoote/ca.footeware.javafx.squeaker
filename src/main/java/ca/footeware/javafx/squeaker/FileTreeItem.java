package ca.footeware.javafx.squeaker;

import java.io.File;
import java.nio.file.Files;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;

public class FileTreeItem extends CheckBoxTreeItem<File> {

    private ObservableList<TreeItem<File>> children;

    /**
     * Constructor.
     *
     * @param file {@link File}
     */
    public FileTreeItem(File file) {
        super(file);
        this.selectedProperty().addListener(
                (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                    // ignore folder selection and use children's selection instead
                    if (isLeaf()) {
                        // if newValue is true, this FileTreeItem was selected
                        if (newValue) {
                            SelectionManager.addSelectedItem(this);
                        } else {
                            // else it was deselected
                            SelectionManager.removeSelectedItem(this);
                        }
                    }
                });
    }

    @Override
    public boolean isLeaf() {
        return !Files.isDirectory(getValue().toPath());
    }

    @Override
    public ObservableList<TreeItem<File>> getChildren() {
        if (children == null) {
            children = super.getChildren();
            final File file = getValue();
            final File[] childFiles = file.listFiles((File child) -> {
                if (Files.isDirectory(child.toPath())) {
                    // folder
                    return true;
                } else {
                    String name = child.getName().toLowerCase();
// TODO                    return name.endsWith(".mp3") || name.endsWith(".flac");
                    return name.endsWith(".mp3");
                }
            });
            if (childFiles != null) {
                for (File childFile : childFiles) {
                    final FileTreeItem treeItem = new FileTreeItem(childFile);
                    children.add(treeItem);
                }
            }
            children.sort(new FileTreeItemComparator());
        }
        return children;
    }
}
