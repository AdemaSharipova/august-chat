module kz.timka {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.logging.log4j;

    opens kz.timka.client to javafx.fxml;
    exports kz.timka.client;
}