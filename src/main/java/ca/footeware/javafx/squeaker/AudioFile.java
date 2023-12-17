/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ca.footeware.javafx.squeaker;

import java.io.File;
import java.net.URI;
import javafx.scene.media.Media;

/**
 * Extends {@link File} to include audio tags.
 */
public class AudioFile extends File {

    private String filename;
    private String track;
    private String artist;
    private String title;
    private String album;
    private String year;
    private String genre;
    private long seconds;
    private Media media;

    /**
     * Constructor.
     *
     * @param uri {@link URI}
     */
    public AudioFile(URI uri) {
        super(uri);
        this.filename = super.getName();
    }

    public String getTrack() {
        return track;
    }

    public void setTrack(String track) {
        this.track = track;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setSeconds(long seconds) {
        this.seconds = seconds;
    }

    public long getSeconds() {
        return seconds;
    }

    public String getFormattedTime() {
        return Utils.formatTime(seconds);
    }

    public void setMedia(Media media) {
        this.media = media;
    }

    public Media getMedia() {
        return media;
    }
}
