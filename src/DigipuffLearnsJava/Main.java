package DigipuffLearnsJava;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Main extends Application {

    //CONSTANTS
    public static final int INFINITE = -1;  //used for initial # of haikus Digipuff has (also available to subclasses)

    //IVARS
    private Thread recordingThread;

    //GETTERS

    //SETTERS
    private void setTitle(String title) {
        Hub.getMainStage().setTitle(title);
    }

    //OTHER METHODS
    @Override
    public void start(Stage primaryStage) throws Exception{
        Hub.setMainStage(primaryStage);
        preLayout();
        buildRecordingThread();
        layout();  //layout the main stage
        preRun();
        Hub.setPaused(true);
        //TODO - display loading animation
        recordingThread.start();
    }

    public String getWorldUrl() {
        return "demo.world";
    }

    private void preLayout() {

    }

    private void buildRecordingThread() {
        Task<Void> recordingTask = new Task<Void>() {
            @Override
            protected Void call() {
                runCommands();
                return null;
            }
        };
        recordingTask.setOnSucceeded(e -> Hub.replay());
        Hub.setRecordingTask(recordingTask);
        recordingThread = new Thread(recordingTask);
        Hub.setRecordingThread(recordingThread);
    }

    private void layout() {
        BorderPane root = new BorderPane();
        Hub.setWorld("/" + getWorldUrl());
        Pane worldPane = Hub.getWorld().getPane();
        ScrollPane sp = new ScrollPane(worldPane);
        ControlBar cb = new ControlBar();
        Hub.setControlBar(cb);
        root.setLeft(cb);
        root.setCenter(sp);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(Main.class.getResource
                ("style.css").toExternalForm());
        Hub.getMainStage().setTitle("Digipuff Learns Java");
        Hub.getMainStage().setScene(scene);
        Hub.getMainStage().setOnCloseRequest(e -> System.exit(0));
        if(!Hub.isExiting()) Hub.getMainStage().show();
    }

    public void preRun() {

    }

    public void runCommands() {
        demo();
    }

    private void demo() {
        Digipuff digipuff = new Digipuff("Digipuff", 0, 0, Dir.EAST, 0);
        digipuff.spawn();
        Fido fido = new Fido("Fido", 0, 1, Dir.EAST, INFINITE);
        fido.spawn();
        for(int i = 0; i < 6; i++) {
            digipuff.move();
            fido.move();
        }
        digipuff.move();
        fido.turnLeft();
        digipuff.turnLeft();
        fido.move();
        digipuff.move();
        fido.move();
        digipuff.move();
        fido.pickUpHaiku();
        digipuff.move();
        fido.turnRight();
        digipuff.turnLeft();
        fido.turnRight();
        for(int i = 0; i < 5; i++) {
            fido.move();
            digipuff.move();
            fido.turnLeft();
            digipuff.turnLeft();
        }
        fido.placeHaiku();
        fido.move();
        digipuff.move();
        digipuff.pickUpHaiku();
        fido.turnLeft();
        digipuff.turnRight();
        fido.turnLeft();
        digipuff.turnRight();
        digipuff.move();
        fido.move();
        digipuff.move();
        fido.turnRight();
        fido.move();
        digipuff.turnLeft();
        digipuff.move();
        fido.move();
        fido.turnLeft();
        for(int i = 0; i < 2; i++) {
            digipuff.move();
            fido.move();
        }
        digipuff.turnLeft();
        digipuff.move();
        fido.move();
        digipuff.turnLeft();
        fido.turnLeft();
    }

} //END OF CLASS
