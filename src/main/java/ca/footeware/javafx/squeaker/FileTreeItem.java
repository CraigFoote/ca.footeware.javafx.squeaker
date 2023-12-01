package ca.footeware.javafx.squeaker;

import java.io.File;
import java.nio.file.Files;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;

public class FileTreeItem extends CheckBoxTreeItem<FileWrapper> {

    private ObservableList<TreeItem<FileWrapper>> children;

    /**
     * Constructor.
     *
     * @param fileWrapper {@link FileWrapper}
     */
    public FileTreeItem(FileWrapper fileWrapper) {
        super(fileWrapper);
        this.selectedProperty().addListener(
                (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                    if (isLeaf()) {
                        if (newValue) {
                            SelectionManager.addSelectedItem(this);
                        } else {
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
    public ObservableList<TreeItem<FileWrapper>> getChildren() {
        if (children == null) {
            children = super.getChildren();
            final File file = getValue();
            final File[] childFiles = file.listFiles((File child) -> {
                if (Files.isDirectory(child.toPath())) {
                    // folder
                    return true;
                } else {
                    String name = child.getName().toLowerCase();
//                    return name.endsWith(".mp3") || name.endsWith(".flac");
                    return name.endsWith(".mp3");
                }
            });
            for (File childFile : childFiles) {
                final FileTreeItem treeItem = new FileTreeItem(new FileWrapper(childFile));
                children.add(treeItem);
            }
            children.sort(new FileTreeItemComparator());
        }
        return children;
    }
}
