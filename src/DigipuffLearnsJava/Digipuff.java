package DigipuffLearnsJava;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

public class Digipuff implements SolidObject{

    //CONSTANTS
    private static final String EAST_IMAGE_URL ="images/digipuff.png";
    private static final String CRASH_IMAGE_URL ="images/digi-crash.gif";
    private static final String INFINITE = "INFINITE";

    private ImageView eastImage;
    private ImageView crashImage;
    private int x;
    private int y;
    private ImageView image;
    private Dir direction;
    private IntegerProperty numHaikus;
    private IntegerProperty totalMoves;
    private String name;
    private ImageView currentWorldImage;
    private boolean spawned = false;
    private InitState initState;
    private double gridUnit;

    //CONSTRUCTORS
    public Digipuff(String name, int x, int y, Dir initDirection, int numHaikus) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.direction = initDirection;
        this.numHaikus = new SimpleIntegerProperty(numHaikus);
        this.totalMoves = new SimpleIntegerProperty(0);
    }

    //GETTERS
    public InitState getInitState() { return this.initState; }

    public String getEastImageUrl() { return EAST_IMAGE_URL; }

    public String getCrashImageUrl() { return CRASH_IMAGE_URL; }

    public String getName() { return name; }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Dir getDirection() {
        return direction;
    }

    public int getNumHaikus() {
        return numHaikus.get();
    }

    public int getTotalMoves() {
        return totalMoves.get();
    }

    public boolean frontIsBlocked() {
        int targetX;
        int targetY;
        switch(getDirection()) {
            case EAST:
                targetX = getX() + 1;
                targetY = getY();
                break;
            case WEST:
                targetX = getX() - 1;
                targetY = getY();
                break;
            case NORTH:
                targetX = getX();
                targetY = getY() + 1;
                break;
            case SOUTH:
                targetX = getX();
                targetY = getY() - 1;
                break;
            default:
                throw new RuntimeException("Direction: " + getDirection() + " not supported.");
        }
        return (Hub.getWorld().isWall(getX(), getY(), targetX, targetY) ||
                Hub.getWorld().hasSolidObject(targetX, targetY) ||
                Hub.getWorld().isOutOfBounds(targetX, targetY));
    }

    public boolean frontIsClear() {
        return !frontIsBlocked();
    }

    public boolean spaceHasHaiku() { return Hub.getWorld().isHaikuAt(getX(), getY()); }

    private ImageView getImage() {
        return image;
    }

    private IntegerProperty numHaikusProperty() {
        return numHaikus;
    }

    private IntegerProperty totalMovesProperty() {
        return totalMoves;
    }

    private ImageView getEastImage() {
        return eastImage;
    }

    private ImageView getCrashImage() {
        return crashImage;
    }

    private void setX(int x) {
        this.x = x;
    }

    private void setY(int y) {
        this.y = y;
    }

    private void setDirection(Dir direction) {
        this.direction = direction;
    }

    private void setNumHaikus(int numHaikus) {
        this.numHaikus.set(numHaikus);
    }

    //rotates the east image into the proper orientation based on direction
    private void setImage(ImageView image, Dir direction) {
        this.image = image;
        switch(direction) {
            case EAST:
                image.setRotate(0);
                break;
            case WEST:
                image.setRotate(180);
                break;
            case NORTH:
                image.setRotate(-90);
                break;
            case SOUTH:
                image.setRotate(90);
                break;
        }
    }

    private void setTotalMoves(int value) {
        totalMoves.set(value);
    }


    //OTHER METHODS
    public void move() {
        if(!spawned) return;
        if(Hub.isRecording()) {
            Hub.getWorld().addAction(new DigiAction(this, Action.MOVE));
        }
        if(frontIsBlocked()) {
            if(Hub.isRecording()) {
                Platform.runLater(Hub::replay);
                Hub.pauseRecordingThread();
                return;
            }
            crash();
            return;
        }
        if(!Hub.isRecording()) {
            TranslateTransition tt = new TranslateTransition(Duration.millis(Hub.getMoveTime()), this.currentWorldImage);
            tt.setInterpolator(Interpolator.EASE_BOTH);
            PauseTransition pt = new PauseTransition(Duration.millis(Hub.getMoveTime()));
            SequentialTransition st = new SequentialTransition(tt, pt);
            switch(getDirection()) {
                case EAST:
                    tt.setByX(this.gridUnit);
                    tt.setByY(0);
                    break;
                case WEST:
                    tt.setByX(-this.gridUnit);
                    tt.setByY(0);
                    break;
                case NORTH:
                    tt.setByX(0);
                    tt.setByY(-this.gridUnit);
                    break;
                case SOUTH:
                    tt.setByX(0);
                    tt.setByY(this.gridUnit);
                    break;
            }
            st.setOnFinished(e -> {
                updateWorldAfterMove();
                setTotalMoves(getTotalMoves() + 1);
                Hub.nextAction();
            });
            Hub.setCurrentAnimation(st);
            if(!Hub.isPaused()) Hub.getCurrentAnimation().play();
            //if it is paused, this animation will begin when the start button is clicked
        } else {  //recording
            updateWorldAfterMove();
        }
    }

    private void updateWorldAfterMove() {
        switch(getDirection()) {
            case EAST:
                setX(getX() + 1);
                break;
            case WEST:
                setX(getX() - 1);
                break;
            case NORTH:
                setY(getY() + 1);
                break;
            case SOUTH:
                setY(getY() - 1);
                break;
        }
        Hub.getWorld().removeWorldObject(this);
        Hub.getWorld().addWorldObject(this, getX(), getY());
    }

    public void turnRight() {
        if(!spawned) return;
        if(Hub.isRecording()) {
            Hub.getWorld().addAction(new DigiAction(this, Action.TURN_RIGHT));
            updateDirAfterRightTurn();
        } else { //not recording
            //rotate 90 degrees cw
            RotateTransition rt = new RotateTransition(Duration.millis(Hub.getMoveTime()), this.currentWorldImage);
            rt.setByAngle(90);
            PauseTransition pt = new PauseTransition(Duration.millis(Hub.getMoveTime()));
            SequentialTransition st = new SequentialTransition(rt, pt);
            st.setOnFinished(e -> {
                updateDirAfterRightTurn();
                setTotalMoves(getTotalMoves() + 1);
                Hub.nextAction();
            });
            Hub.setCurrentAnimation(st);
            if(!Hub.isPaused()) Hub.getCurrentAnimation().play();
            //if it is paused, this animation will begin when the start button is clicked
        }
    }

    private void updateDirAfterRightTurn() {
        switch(getDirection()) {
            case EAST:
                setDirection(Dir.SOUTH);
                break;
            case WEST:
                setDirection(Dir.NORTH);
                break;
            case NORTH:
                setDirection(Dir.EAST);
                break;
            case SOUTH:
                setDirection(Dir.WEST);
                break;
        }
    }

    public void turnLeft() {
        if(!spawned) return;
        if(Hub.isRecording()) {
            Hub.getWorld().addAction(new DigiAction(this, Action.TURN_LEFT));
            updateDirAfterLeftTurn();
        } else { //not recording
            //rotate 90 degrees ccw
            RotateTransition rt = new RotateTransition(Duration.millis(Hub.getMoveTime()), this.currentWorldImage);
            rt.setByAngle(-90);
            PauseTransition pt = new PauseTransition(Duration.millis(Hub.getMoveTime()));
            SequentialTransition st = new SequentialTransition(rt, pt);
            st.setOnFinished(e -> {
                updateDirAfterLeftTurn();
                setTotalMoves(getTotalMoves() + 1);
                Hub.nextAction();
            });
            Hub.setCurrentAnimation(st);
            if(!Hub.isPaused()) Hub.getCurrentAnimation().play();
            //if it is paused, this animation will begin when the start button is clicked
        }
    }

    private void updateDirAfterLeftTurn() {
        switch(getDirection()) {
            case EAST:
                setDirection(Dir.NORTH);
                break;
            case WEST:
                setDirection(Dir.SOUTH);
                break;
            case NORTH:
                setDirection(Dir.WEST);
                break;
            case SOUTH:
                setDirection(Dir.EAST);
                break;
        }
    }

    private void crash() {
        replaceImageInWorld(getCrashImage());
        RotateTransition rt = new RotateTransition(Duration.millis(2000), this.currentWorldImage);
        rt.setByAngle(-360);
        rt.setCycleCount(-1);
        rt.setInterpolator(Interpolator.LINEAR);
        rt.play();
        Hub.exitDialog(getName() + ", you crashed!");
    }

    private void replaceImageInWorld(ImageView newImage) {
        newImage.setX(this.currentWorldImage.getX());
        newImage.setY(this.currentWorldImage.getY());
        newImage.setTranslateX(this.currentWorldImage.getTranslateX());
        newImage.setTranslateY(this.currentWorldImage.getTranslateY());
        newImage.setRotate(this.currentWorldImage.getRotate());
        Hub.getWorld().getPane().getChildren().remove(this.currentWorldImage);
        Hub.getWorld().getPane().getChildren().add(newImage);
        this.currentWorldImage = newImage;
    }

    public void placeHaiku() {
        if(!spawned) return;
        if(Hub.isRecording()) Hub.getWorld().addAction(new DigiAction(this, Action.PLACE_HAIKU));
        if(getNumHaikus() == 0) {
            if(!Hub.isRecording()) outOfHaikus();  //do this only if we are in the replay
            return;
        }
        if(Hub.isRecording()) {
            updateWorldAfterPlaceHaiku();
        } else { //not recording
            PauseTransition pt = new PauseTransition(Duration.millis(Hub.getMoveTime()));
            pt.setOnFinished(e -> {
                updateWorldAfterPlaceHaiku();
                setTotalMoves(getTotalMoves() + 1);
            });
            PauseTransition secondPause = new PauseTransition(Duration.millis(Hub.getMoveTime()));
            SequentialTransition st = new SequentialTransition(pt, secondPause);
            st.setOnFinished(e -> Hub.nextAction());
            Hub.setCurrentAnimation(st);
            if(!Hub.isPaused()) Hub.getCurrentAnimation().play();
            //if it is paused, this animation will begin when the start button is clicked
        }
    }

    private void updateWorldAfterPlaceHaiku() {
        if(getNumHaikus() > 0) {
            setNumHaikus(getNumHaikus() - 1);
        }
        //otherwise, numHaikus is infinite (-1)
        int x = getX();
        int y = getY();
        Hub.getWorld().addHaiku(x, y);
    }

    public void pickUpHaiku() {
        if(!spawned) return;
        if(Hub.isRecording()) {
            Hub.getWorld().addAction(new DigiAction(this, Action.PICK_UP_HAIKU));
        }
        int x = getX();
        int y = getY();
        if(!Hub.getWorld().isHaikuAt(x, y)) {
            if(!Hub.isRecording()) noHaikuToPickup();
            return;
        }
        if(Hub.isRecording()) {
            updateWorldAfterPickupHaiku(x, y);
        } else { //not recording
            PauseTransition pt = new PauseTransition(Duration.millis(Hub.getMoveTime()));
            pt.setOnFinished(e -> {
                updateWorldAfterPickupHaiku(x, y);
                setTotalMoves(getTotalMoves() + 1);
            });
            PauseTransition secondPause = new PauseTransition(Duration.millis(Hub.getMoveTime()));
            SequentialTransition st = new SequentialTransition(pt, secondPause);
            st.setOnFinished(e -> Hub.nextAction());
            Hub.setCurrentAnimation(st);
            if(!Hub.isPaused()) Hub.getCurrentAnimation().play();
            //if it is paused, this animation will begin when the start button is clicked
        }
    }

    private void updateWorldAfterPickupHaiku(int x, int y) {
        Hub.getWorld().removeHaiku(x, y);
        if(getNumHaikus() >= 0) {   //not infinite (-1)
            setNumHaikus(getNumHaikus() + 1);
        }
    }

    private void outOfHaikus() {
        Hub.pausedDialog(getName() + ", you don't have any haikus!");
    }

    private void noHaikuToPickup() {
        Hub.pausedDialog(getName() + ", there aren't any haikus to pick up!");
    }

    public void spawn() {
        if(Hub.isRecording()) Hub.getWorld().addAction(new DigiAction(this, Action.SPAWN));
        if(spawned) {
            if(!Hub.isRecording()) Hub.pausedDialog(getName() + ", you can't spawn yourself more than once!");
            return;
        }
        if(Hub.getWorld().isOutOfBounds(getX(), getY())) {
            if(Hub.isRecording()) {
                Platform.runLater(Hub::replay);
                Hub.pauseRecordingThread();
                return;
            }
            Hub.exitDialog(getName() + ", you spawned yourself out of bounds!");
            return;
        }
        if(Hub.isRecording()) {
            initImages();
            setImage(getEastImage(), getDirection());
            saveInitialState();
        } else {
            addInfoToControlBar(name); //this can't happen while recording because r2 is on a separate thread
        }
        if(Hub.getWorld().hasSolidObject(getX(), getY())) {
            if(Hub.isRecording()) {
                Platform.runLater(Hub::replay);
                Hub.pauseRecordingThread();
                return;
            }
            crash();
            return;
        }
        Hub.getWorld().addWorldObject(this, getX(), getY());  //register Digipuff as a world object
        if(Hub.isRecording()) Hub.getWorld().r2List().add(this); //register this Digipuff for goal checking
        addR2();
        spawned = true;
    }

    private void saveInitialState() {
        initState = new InitState();
        initState.name = getName();
        initState.x = getX();
        initState.y = getY();
        initState.initDirection = getDirection();
        initState.numHaikus = getNumHaikus();
    }

    private void reset() {
        removeR2();
        spawned = false;
        name = initState.name;
        setX(initState.x);
        setY(initState.y);
        setDirection(initState.initDirection);
        setNumHaikus(initState.numHaikus);
    }

    private void removeR2() {
        if(!Hub.isRecording()) Hub.getWorld().getPane().getChildren().remove(currentWorldImage);
    }

    private void addR2() {
        ImageView img = getImage();
        this.gridUnit = (Hub.getWorld().getPane().getWidth() - World.BORDER_WIDTH * 2)
                / Hub.getWorld().getNumHorizSpaces();

        double offset = Hub.getWorld().getGridLineWidth() / 2 + World.BORDER_WIDTH;
        img.setX(getX() * gridUnit + offset);
        img.setY(Hub.getWorld().getYFromRow(getY()) * gridUnit + offset);
        if(Hub.isRecording()) {
            currentWorldImage = img;
        } else { //not recording
            img.setOpacity(0);  //image shouldn't be visible until it fades in
            Hub.getWorld().getPane().getChildren().add(img);
            //fade Digipuff in
            FadeTransition ft = new FadeTransition(Duration.millis(Hub.getMoveTime() * 3), img);
            ft.setByValue(1);
            ft.setOnFinished(e -> {
                currentWorldImage = img;
                Hub.nextAction();
            });
            Hub.setCurrentAnimation(ft);
            if(!Hub.isPaused()) Hub.getCurrentAnimation().play();
            //if it is paused, this animation will begin when the start button is clicked
        }
    }

    private void addInfoToControlBar(String name) {
        //Name Label
        HBox nameHBox = new HBox();
        nameHBox.setAlignment(Pos.CENTER);
        Text nameLabel = new Text(name);
        nameLabel.setStyle("-fx-font-weight:bold");
        nameLabel.setWrappingWidth(150);
        nameLabel.setTextAlignment(TextAlignment.CENTER);
        nameLabel.getStyleClass().add("header-text");
        nameHBox.getChildren().add(nameLabel);

        //Number of haikus indicator
        HBox numHaikusHBox = new HBox(10);
        numHaikusHBox.setPadding(new Insets(0,0,0,25));
        Label numHaikusText = new Label("Haikus:");
        numHaikusText.getStyleClass().add("styled-text");
        Label cBarNumHaikusValue;
        if(getNumHaikus() == -1) {
            cBarNumHaikusValue = new Label(INFINITE);
        } else {
            cBarNumHaikusValue = new Label(String.valueOf(getNumHaikus()));
        }
        cBarNumHaikusValue.getStyleClass().add("styled-text");
        numHaikusProperty().addListener(((observable, oldValue, newValue) -> {
            if(newValue != null && !newValue.equals(oldValue)) {
                if(newValue.intValue() == -1) {
                    Platform.runLater(() -> cBarNumHaikusValue.setText(INFINITE));
                } else {
                    Platform.runLater(() ->
                            cBarNumHaikusValue.setText(String.valueOf(newValue)));
                }
            }
        }));
        numHaikusHBox.getChildren().addAll(numHaikusText, cBarNumHaikusValue);

        //Total number of moves indicator
        HBox totalMovesHBox = new HBox(10);
        totalMovesHBox.setPadding(new Insets(0,0,0,25));
        Label totalMovesText = new Label("Total Moves:");
        totalMovesText.getStyleClass().add("styled-text");
        Label totalMovesValue = new Label(String.valueOf(getTotalMoves()));
        totalMovesValue.getStyleClass().add("styled-text");
        totalMovesProperty().addListener(((observable, oldValue, newValue) -> {
            if(newValue != null && !newValue.equals(oldValue)) {
                Platform.runLater(() ->
                        totalMovesValue.setText(String.valueOf(newValue.intValue())));
            }
        }));
        totalMovesHBox.getChildren().addAll(totalMovesText, totalMovesValue);
        VBox indicatorBox = new VBox(10);
        indicatorBox.getChildren().addAll(nameHBox, numHaikusHBox, totalMovesHBox);
        Hub.getControlBar().getMainVBox().getChildren().addAll(indicatorBox);
    }

    private void initImages() {
        try {
            eastImage = new ImageView(getEastImageUrl());
        } catch(Exception e) {
            System.out.println("East Image failed to load.");
            e.printStackTrace();
            System.exit(1);
        }

        try {
            crashImage = new ImageView(getCrashImageUrl());
        } catch(Exception e) {
            System.out.println("Crash Image failed to load.");
            e.printStackTrace();
            System.exit(1);
        }
        sizeImageAndSetId(eastImage);
        sizeImageAndSetId(crashImage);
    }

    private void sizeImageAndSetId(ImageView iv) {
        iv.setFitWidth(Hub.getWorld().getSpaceSize() - Hub.getWorld().getGridLineWidth());
        iv.setFitHeight(Hub.getWorld().getSpaceSize() - Hub.getWorld().getGridLineWidth());
        iv.setId(Hub.DIGIPUFF_ID);
    }

    public class InitState{
        public String name;
        public int x;
        public int y;
        public Dir initDirection;
        public int numHaikus;

        public void restore() { reset(); }
    }

} //END OF Digipuff CLASS
