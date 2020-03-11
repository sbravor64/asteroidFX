package control;

import javafx.fxml.Initializable;
import javafx.scene.Scene;

import java.net.URL;
import java.util.ResourceBundle;

public class MainWindow implements Initializable {
    private Scene scene;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void setScene(Scene sc){
        scene=sc;
    }
}
