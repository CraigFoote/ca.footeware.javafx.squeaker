module ca.footeware.javafx.squeaker {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires javafx.mediaEmpty;
    requires javafx.graphics;
    requires java.base;
    requires java.desktop;
    requires mp3agic;

    opens ca.footeware.javafx.squeaker to javafx.fxml;
    exports ca.footeware.javafx.squeaker;
}
