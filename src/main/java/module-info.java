module com.haw.chatapplication {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.media;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires javafx.graphics;
    requires com.google.gson;

    opens com.haw.chatapplication to javafx.fxml;
    opens com.haw.chatapplication.model to com.google.gson;
    exports com.haw.chatapplication;
    exports com.haw.chatapplication.model to com.google.gson;
}