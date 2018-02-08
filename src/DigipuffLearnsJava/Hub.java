package DigipuffLearnsJava;

import Ddialog.Ddialog;
import Ddialog.DialogType;
import javafx.animation.PauseTransition;
import javafx.animation.Transition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public final class Hub {

    //CONSTANTS
    public static final int DEFAULT_WORLD_SPACE_SIZE = 85;
    private static final int DEFAULT_SPEED = 3;
    public static final String DIGIPUFF_ID = "digipuffId";
    public static final String ANY = "Any";
    public static final int DIALOG_FONT_SIZE = 15;
    private static final long PAUSE_TIME_CONVERSION_FACTOR = 1_000_000_000;

    //STATIC VARIABLES
    private static Stage mainStage;
    private static World world;
    private static ControlBar controlBar;
    private static boolean paused;
    private static int speed = DEFAULT_SPEED;
    private static long pauseTime = calculatePauseTime();
    private static boolean started = false;
    private static boolean exiting = false;
    private static boolean recording = true;
    private static boolean maxMovesReached = false;
    private static int actionIndex = 0;
    private static Thread recordingThread;
    private static Task recordingTask;
    private static BooleanProperty playbackFinished = new SimpleBooleanProperty(false);
    private static List<DigiAction> actionList;
    private static Transition currentAnimation;
    private static boolean animationFinished = true;


     //GETTERS
    public static Stage getMainStage() {
        return mainStage;
    }

    public static World getWorld() {
        return world;
    }

    public static ControlBar getControlBar() {
        return controlBar;
    }

    public static int getSpeed() {
        return speed;
    }

    public static boolean isPaused() { return paused; }

    public static boolean isStarted() { return started; }

    public static boolean isExiting() { return exiting; }

    public static boolean isRecording() { return recording; }

    public static Thread getRecordingThread() { return recordingThread; }

    public static Task getRecordingTask() { return recordingTask; }

    private static BooleanProperty playbackFinishedProperty() {
        return playbackFinished;
    }

    private static boolean getPlaybackFinished() {
        return playbackFinished.get();
    }

    public static double getMoveTime() { return 500 / Math.pow(getSpeed(), 1.5); }

    public static Transition getCurrentAnimation() { return currentAnimation; }


    //SETTERS
    public static void setMainStage(Stage stage) {
        mainStage = stage;
    }

    public static void setWorld(World myWorld) {
        /*this.*/world = myWorld;
    }

    public static void setWorld(String worldFileUrl) {

        StringBuilder worldStr = new StringBuilder();
        try {
            InputStream in = Hub.class.getResourceAsStream(worldFileUrl);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line = reader.readLine();
            while(line != null) {
                worldStr.append(line);
                line = reader.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
            exitDialogWait("Could not open file: " + worldFileUrl);
            exiting = true;
        }
        try {
            if(!exiting) setWorld(new World(worldStr.toString()));
        } catch (Exception e) {
            e.printStackTrace();
            exitDialogWait("Can't load world. The world file is corrupt.");
        }
    }

    public static void setControlBar(ControlBar cb) {
        controlBar = cb;
    }

    public static void setPaused(boolean psd) {
        paused = psd;
    }

    public static void setSpeed(int spd) {
        speed = spd;
        pauseTime = calculatePauseTime();
    }

    public static void setStarted(boolean value) {
        started = value;
    }

    public static void setRecording(boolean value) { recording = value; }

    public static void setMaxMovesReached(boolean value) { maxMovesReached = value; }

    public static void setRecordingThread(Thread thread) {
        recordingThread = thread;
    }

    public static void setRecordingTask(Task task) {
        recordingTask = task;
    }

    private static void setPlaybackFinished(boolean value) { playbackFinished.set(value); }

    public static void setAnimationFinished(boolean value) { animationFinished = value; }

    public static void setExiting(boolean value) { exiting = value; }

    public static void setCurrentAnimation(Transition animation) { currentAnimation = animation; }


    //OTHER METHODS
    private static long calculatePauseTime() {
        return PAUSE_TIME_CONVERSION_FACTOR / (long)Math.pow(speed, 1.8);
    }

    public static void pauseRecordingThread() {
        while(true) {
            try {
                recordingThread.sleep(1_000_000);
            } catch(InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }

    public static void exitDialog(String message) {
        Ddialog dialog = new Ddialog(DialogType.INFO, getMainStage(), message);
        dialog.setFontSize(Hub.DIALOG_FONT_SIZE);
        dialog.setOKEvent(e -> System.exit(0));
        dialog.setOnExit(e -> System.exit(0));
        dialog.show();
    }

    public static void exitDialogWait(String message) {
        Ddialog dialog = new Ddialog(DialogType.INFO, getMainStage(), message);
        dialog.setFontSize(Hub.DIALOG_FONT_SIZE);
        dialog.setOKEvent(e -> System.exit(0));
        dialog.setOnExit(e -> System.exit(0));
        dialog.showAndWait();
    }

    public static void infoDialog(String message) {
        Ddialog dialog = new Ddialog(DialogType.INFO, getMainStage(), message);
        dialog.setFontSize(Hub.DIALOG_FONT_SIZE);
        dialog.show();
    }

    public static void replay() {
        setRecording(false);
        //get the action list from World
        actionList = getWorld().getActionsAndClear();
        //reset the world
        getWorld().prepareForReplay();
        //reset all R2s
        for(Digipuff digipuff : getWorld().r2List()) {
            digipuff.getInitState().restore();
        }

        //set currentAnimation to a PauseTransition
        //NOTE: this will not happen until the user presses the start button
        PauseTransition openingAction = new PauseTransition(Duration.millis(1));
        openingAction.setOnFinished(e -> nextAction());
        currentAnimation = openingAction;  //this will now begin when the user clicks the start button

        //activate the start button
        getControlBar().setReadyForReplay(true);

    }

    public static void nextAction() {
        if(actionIndex < actionList.size()) {
            Digipuff digipuff = actionList.get(actionIndex).getDigipuff();
            Action action = actionList.get(actionIndex).getAction();
            doAction(digipuff, action);
            actionIndex++;
        } else { //there are no more actions to be performed
            checkMaxMovesReached();
            Hub.getWorld().checkGoal();
        }
    }

    private static void checkMaxMovesReached() {
        if(maxMovesReached) exitDialog("Maximum moves reached. Did you get stuck in an infinite loop?");
    }

    public static void pausedDialog(String message) {

        Ddialog dialog = new Ddialog(DialogType.INFO, Hub.getMainStage(), message);
        dialog.setFontSize(Hub.DIALOG_FONT_SIZE);
        dialog.setOKEvent(e -> {
            if(isStarted() && !getControlBar().isUserPaused()) {
                setPaused(false);
                Hub.getCurrentAnimation().play();
            }
        });
        dialog.setOnExit(e -> {
            if(isStarted() && !getControlBar().isUserPaused()) {
                setPaused(false);
                Hub.getCurrentAnimation().play();
            }
        });
        dialog.show();
        setPaused(true);
    }

    private static void doAction(Digipuff digipuff, Action action) {
        switch(action) {
            case SPAWN:
                digipuff.spawn();
                break;
            case MOVE:
                digipuff.move();
                break;
            case TURN_LEFT:
                digipuff.turnLeft();
                break;
            case TURN_RIGHT:
                digipuff.turnRight();
                break;
            case PICK_UP_HAIKU:
                digipuff.pickUpHaiku();
                break;
            case PLACE_HAIKU:
                digipuff.placeHaiku();
                break;
        }
    }

} //END OF CLASS
