package DigipuffLearnsJava;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class WorldGenerator extends Application {

    private static WorldGenerator instance;
    public static WorldGenerator getInstance() { return instance; }

    private Stage stage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;
        instance = this;
        Parent root = FXMLLoader.load(getClass().getResource("WorldGenLayout.fxml"));
        Scene scene = new Scene(root, 1052, 732);
        scene.getStylesheets().add(WorldGenerator.class.getResource
                ("style.css").toExternalForm());
        primaryStage.setTitle("World Generator");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public Stage getStage() {
        return stage;
    }

}
