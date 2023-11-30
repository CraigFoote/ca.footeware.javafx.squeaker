/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ca.footeware.javafx.squeaker;

import java.io.File;

/**
 * Wraps a {@link File} to override its #toString method.
 */
public class FileWrapper extends File {

    private File file;

    /**
     * Constructor.
     *
     * @param file {@link File}
     */
    public FileWrapper(File file) {
        super(file.toURI());
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    @Override
    public String toString() {
        final String name = file.getName();
        final String path = file.getPath();
        return path.equals(File.separator) ? File.separator : file.getName();
    }
}
