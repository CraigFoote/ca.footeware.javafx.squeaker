/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ca.footeware.javafx.squeaker;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javafx.util.Duration;

/**
 * Utilities, e.g. water and sewer.
 *
 * @author Footeware.ca
 */
public class Utils {

    public static String formatTime(long seconds) {
        LocalTime time = LocalTime.ofSecondOfDay(seconds);
        DateTimeFormatter formatter;
        if (time.getHour() > 0) {
            formatter = DateTimeFormatter.ofPattern("hh:mm:ss");
        } else {
            formatter = DateTimeFormatter.ofPattern("mm:ss");
        }
        return time.format(formatter);
    }

    public static String formatTime(Duration duration) {
        double secondsDouble = duration.toSeconds();
        long seconds = Double.valueOf(secondsDouble).longValue();
        return formatTime(seconds);
    }

    /**
     * Hidden constructor.
     */
    private Utils() {
    }
}
