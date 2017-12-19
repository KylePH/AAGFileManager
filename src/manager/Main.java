package manager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.Stage;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        Parent init = FXMLLoader.load(getClass().getResource("form.fxml"));

        // Have the scene accept files being dragged over top.
        init.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            } else {
                event.consume();
            }
        });

        primaryStage.setTitle("AAG Filer");
        primaryStage.setScene(new Scene(init, 400, 300));
        primaryStage.show();
        primaryStage.setResizable(false);

    }

    public static void main(String[] args) {

        launch(args);
    }

}
