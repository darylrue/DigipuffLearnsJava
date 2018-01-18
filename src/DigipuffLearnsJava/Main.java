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
    public static final int INFINITE = -1;  //used for initial number of haikus Digipuff has

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
        return "worlds/demo.world";
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
        Hub.getMainStage().setTitle("R2 Learns Java");
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
        R2 darth = new Darth("Darth", 0, 1, Dir.EAST, INFINITE);
        darth.spawn();
        R2 r2 = new R2("R2", 0, 0, Dir.EAST, 0);
        r2.spawn();
        for(int i = 0; i < 6; i++) {
            r2.move();
            darth.move();
        }
        r2.move();
        darth.turnLeft();
        r2.turnLeft();
        darth.move();
        r2.move();
        darth.move();
        r2.move();
        darth.pickUpFlashlight();
        r2.move();
        darth.turnRight();
        r2.turnLeft();
        darth.turnRight();
        for(int i = 0; i < 5; i++) {
            darth.move();
            r2.move();
            darth.turnLeft();
            r2.turnLeft();
        }
        darth.placeFlashlight();
        darth.move();
        r2.move();
        r2.pickUpFlashlight();
        r2.turnRight();
        darth.turnLeft();
        r2.turnRight();
        darth.move();
        r2.move();
        darth.move();
        r2.move();
        r2.turnLeft();
        darth.turnLeft();
        for(int i = 0; i < 3; i++) {
            r2.move();
            darth.move();
        }
        r2.turnLeft();
        r2.move();
        darth.move();
        r2.turnLeft();
    }

} //END OF CLASS
