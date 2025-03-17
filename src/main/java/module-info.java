module com.dsa.dsa {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.bouncycastle.provider;
    requires java.desktop;


    opens com.dsa.dsa to javafx.fxml;
    exports com.dsa.dsa;
}