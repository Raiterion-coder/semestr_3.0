module org.example.dem {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires org.slf4j;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    //requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;

    opens org.example.dem to javafx.fxml;
    exports org.example.dem;
}