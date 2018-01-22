package DigipuffLearnsJava;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class ControlBar extends ScrollPane {

    //CONSTANTS
    private static final String INFINITE = "INFINITE";

    //IVARS
    private VBox mainVBox;
    private Button startBtn;
    private Slider speedSlider;
    private boolean userPaused;
    private boolean readyForReplay = false;

    //CONSTRUCTORS
    public ControlBar() {
        initLayout();
    }

    //GETTERS
    public VBox getMainVBox() {
        return mainVBox;
    }

    public boolean isUserPaused() { return userPaused; }

    //SETTERS
    private void setUserPaused(boolean userPaused) {
        this.userPaused = userPaused;
    }

    public void setReadyForReplay(boolean value) {
        readyForReplay = value;
        if(readyForReplay) {
            startBtn.getStyleClass().remove("start-button-disabled");
            startBtn.getStyleClass().add("start-button");
            startBtn.applyCss();
            //TODO - cancel loading animation
        }
    }

    //OTHER METHODS
    private void initLayout() {
        this.getStyleClass().add("bg");
        //Start button
        startBtn = new Button("Start");
        startBtn.getStyleClass().add("start-button-disabled");
        startBtn.setOnAction(e -> {
            if(!readyForReplay) return;
            Hub.setStarted(true);
            if(Hub.isPaused()) {
                Hub.getCurrentAnimation().play();
                Hub.setPaused(false);
                setUserPaused(false);
                Platform.runLater(() -> {
                    startBtn.getStyleClass().remove("start-button");
                    startBtn.getStyleClass().add("pause-button");
                    startBtn.setText("Pause");
                });
            } else {
                Hub.getCurrentAnimation().pause();
                Hub.setPaused(true);
                setUserPaused(true);
                Platform.runLater(() -> {
                    startBtn.getStyleClass().remove("pause-button");
                    startBtn.getStyleClass().add("start-button");
                    startBtn.setText("Start");
                });
            }

        });
        //Speed slider
        buildSpeedSlider();
        VBox sliderVBox = new VBox(5);
        sliderVBox.setAlignment(Pos.CENTER);
        Label sliderLabel = new Label("Speed:");
        sliderLabel.getStyleClass().add("styled-text");
        sliderVBox.getChildren().addAll(sliderLabel, speedSlider);
        //Goal
        VBox goalVBox = new VBox(10);
        Text goalLabel = new Text("GOAL");
        goalLabel.getStyleClass().add("header-text");
        HBox goalLabelHBox = new HBox();
        goalLabelHBox.setAlignment(Pos.CENTER);
        goalLabelHBox.getChildren().add(goalLabel);
        Label locLbl = new Label();
        Insets insets = new Insets(0,0,0,25);
        locLbl.setPadding(insets);
        locLbl.getStyleClass().add("styled-text");

        Label dirLbl = new Label();
        dirLbl.setPadding(insets);
        dirLbl.getStyleClass().add("styled-text");
        Label flLbl = new Label();
        flLbl.setPadding(insets);
        flLbl.getStyleClass().add("styled-text");
        Label numMovesLbl = new Label();
        numMovesLbl.setPadding(insets);
        numMovesLbl.getStyleClass().add("styled-text");
        locLbl.setText("Location: " + Hub.getWorld().getGoal().locationStr());
        dirLbl.setText("Direction: " + Hub.getWorld().getGoal().directionStr());
        flLbl.setText("Haikus: " + Hub.getWorld().getGoal().numHaikusStr());
        numMovesLbl.setText("No. of Moves: " + Hub.getWorld().getGoal().numMovesStr());
        goalVBox.getChildren().addAll(goalLabelHBox, locLbl, dirLbl, flLbl, numMovesLbl);
        mainVBox = new VBox(25);
        mainVBox.setAlignment(Pos.TOP_CENTER);
        mainVBox.setPadding(new Insets(20, 5, 10, 5));
        mainVBox.setPrefWidth(190);
        mainVBox.getChildren().addAll(startBtn, sliderVBox, goalVBox);
        mainVBox.getStyleClass().add("text");
        this.setContent(mainVBox);
    }

    private void buildSpeedSlider() {
        speedSlider = new Slider(1, 10, Hub.getSpeed());
        speedSlider.setShowTickMarks(true);
        speedSlider.setShowTickLabels(true);
        speedSlider.setMajorTickUnit(1);
        speedSlider.setBlockIncrement(1);
        speedSlider.setSnapToTicks(true);
        speedSlider.setPadding(new Insets(0,15,0,15));
        speedSlider.setStyle("-fx-font-size: 12px");
        speedSlider.valueProperty().addListener(((observable, oldValue, newValue) -> {
            if(newValue != null && !newValue.equals(oldValue)) {
                Hub.setSpeed(newValue.intValue());
            }
        }));
    }
}
