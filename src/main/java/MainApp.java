import control.MainWindow;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/mainWindow.fxml"));
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Scene sc = new Scene(root);

        MainWindow window = loader.getController();
        window.setScene(sc);

        stage.setScene(sc);
        stage.setTitle("Asteroide");
        stage.show();

    }
}
