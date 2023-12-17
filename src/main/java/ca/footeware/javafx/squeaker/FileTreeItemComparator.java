package ca.footeware.javafx.squeaker;

import java.io.File;
import java.util.Comparator;
import javafx.scene.control.TreeItem;

/**
 * Sort folders before files then sort alphabetically.
 */
public class FileTreeItemComparator implements Comparator<TreeItem<FileWrapper>> {

    /**
     * Constructor.
     */
    public FileTreeItemComparator() {
        super();
    }

    @Override
    public int compare(TreeItem<FileWrapper> o1, TreeItem<FileWrapper> o2) {
        int result;
        if (o1.isLeaf() && !o2.isLeaf()) {
            result = 1;
        } else if (!o1.isLeaf() && o2.isLeaf()) {
            result = -1;
        } else {
            File file1 = o1.getValue();
            File file2 = o2.getValue();
            result = file1.getName().compareTo(file2.getName());
        }
        return result;
    }
}
