# Squeaker
### An audio player in JavaFX.
## Building
1. git clone repo
1. mvn clean install
1. jlink --module-path target/modules:target/classes:/home/craig/openjfx-22-ea+16/openjfx-22-ea+16_linux-x64_bin-jmods/javafx-jmods-22 --add-modules javafx.fxml,mp3agic,ca.footeware.javafx.squeaker --output ./target/squeaker
1. jpackage -n Squeaker -p target/modules:target/classes:/home/craig/openjfx-22-ea+16/openjfx-22-ea+16_linux-x64_bin-jmods/javafx-jmods-22 -m ca.footeware.javafx.squeaker/ca.footeware.javafx.squeaker.App --icon squeaker.png --dest ./target