package DigipuffLearnsJava;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HaikuDisplay {

    //CONSTANTS
    private static final double HAIKU_WIDTH = 450;
    private static final double HAIKU_HEIGHT = 651;
    private static final double WINDOW_WIDTH = 700;
    private static final double WINDOW_HEIGHT = 651;
    private static final String HAIKU_BEGIN_TAG = "<haiku>";
    private static final String HAIKU_END_TAG = "</haiku>";

    //IVARS
    private List<HaikuText> haikuList;

    //CONSTRUCTORS
    public HaikuDisplay() {
        this.haikuList = new ArrayList<>();
        loadHaikuList();
    }

    //GETTERS

    //SETTERS

    //OTHER METHODS
    private void loadHaikuList() {
        List<String> haikuFile= readHaikuFile();
        validateHaikuFile(haikuFile);
        Iterator<String> iterator = haikuFile.iterator();
        while(iterator.hasNext()) {
            iterator.next();  //begin tag
            HaikuText haikuText = new HaikuText(iterator.next(), iterator.next(), iterator.next());
            iterator.next();  //end tag
            this.haikuList.add(haikuText);
        }
    }

    private List<String> readHaikuFile() {
        List<String> haikuFile = new ArrayList<>();
        try {
            InputStream in = this.getClass().getResourceAsStream("/haikus.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line = reader.readLine();
            while(line != null) {
                haikuFile.add(line);
                line = reader.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Could not open haikus.txt.");
        }
        return haikuFile;
    }

    private void validateHaikuFile(List<String> haikuFile) {
        int lineOfHaiku = 0;
        for(String lineStr: haikuFile) {
            if((lineOfHaiku == 0 && !lineStr.equals(HAIKU_BEGIN_TAG))
                    || (lineOfHaiku == 1 && lineStr.isEmpty())
                    || (lineOfHaiku == 2 && lineStr.isEmpty())
                    || (lineOfHaiku == 3 && lineStr.isEmpty())
                    || (lineOfHaiku == 4 && !lineStr.equals(HAIKU_END_TAG))) {
                throw new RuntimeException("haikus.txt is corrupt.");
            } else {
                lineOfHaiku++;
                if(lineOfHaiku > 4) lineOfHaiku = 0;
            }
        }
        //make sure the last line of the file is the end tag
        if(lineOfHaiku != 0) {
            Hub.exitDialog("haikus.txt is corrupt");
            Hub.setExiting(true);
        }
    }

    public void showAndExit() {
        //Haiku
        //get random HaikuText from haikuList
        int i = (int)(Math.random() * this.haikuList.size());
        HaikuText haikuText = this.haikuList.get(i);
        Pane haikuPane = new Pane();
        haikuPane.setMinWidth(HAIKU_WIDTH);
        haikuPane.setMinHeight(HAIKU_HEIGHT);
        haikuPane.setMaxWidth(HAIKU_WIDTH);
        haikuPane.setMaxHeight(HAIKU_HEIGHT);
        haikuPane.setBackground(Background.EMPTY);
        haikuPane.setLayoutX(WINDOW_WIDTH - HAIKU_WIDTH);
        haikuPane.setLayoutY(WINDOW_HEIGHT - HAIKU_HEIGHT);
        loadHaikuImg(haikuPane);
        dispHaikuText(haikuPane, haikuText);
        //Fido
        int fidoY = 300;
        ImageView fidoIV = new ImageView("images/fido-front.png");
        fidoIV.setPreserveRatio(true);
        fidoIV.setFitWidth(300);
        fidoIV.setY(fidoY);
        //Earned Haiku Speech Bubble
        ImageView earnedHaikuIV = new ImageView("images/earned-haiku.png");
        earnedHaikuIV.setY(fidoY - 180);
        //Close Button
        int buttonWidth = 50;
        ImageView closeButtonIV = new ImageView("images/x-button.png");
        closeButtonIV.setPreserveRatio(true);
        closeButtonIV.setFitWidth(buttonWidth);
        closeButtonIV.setX(WINDOW_WIDTH - buttonWidth);
        ImageView closeButtonHoverIV = new ImageView("images/x-button-hover.png");
        closeButtonHoverIV.setPreserveRatio(true);
        closeButtonHoverIV.setFitWidth(buttonWidth);
        closeButtonHoverIV.setX(WINDOW_WIDTH - buttonWidth);
        closeButtonHoverIV.setOpacity(0);
        closeButtonHoverIV.setOnMousePressed(e -> System.exit(0));
        closeButtonHoverIV.setOnMouseEntered(e -> {
            closeButtonIV.setOpacity(0);
            closeButtonHoverIV.setOpacity(1);
        });
        closeButtonHoverIV.setOnMouseExited(e -> {
            closeButtonHoverIV.setOpacity(0);
            closeButtonIV.setOpacity(1);
        });
        Pane background = new Pane();
        background.setBackground(Background.EMPTY);
        background.getChildren().addAll(haikuPane, fidoIV, earnedHaikuIV, closeButtonIV, closeButtonHoverIV);
        Stage stage = new Stage();
        Scene scene = new Scene(background, WINDOW_WIDTH, WINDOW_HEIGHT);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.centerOnScreen();
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setOnCloseRequest(e -> System.exit(0));
        stage.show();
    }

    private void loadHaikuImg(Pane haikuPane) {
        ImageView haikuIV = new ImageView("images/paper.png");
        haikuPane.getChildren().add(haikuIV);
    }

    private void dispHaikuText(Pane background, HaikuText haikuText) {
        VBox haikuVBox = new VBox(10);
        haikuVBox.setMinWidth(HAIKU_WIDTH);
        haikuVBox.setMinHeight(HAIKU_HEIGHT);
        haikuVBox.setAlignment(Pos.CENTER);
        Text line1 = new Text(haikuText.getLine1());
        Text line2 = new Text(haikuText.getLine2());
        Text line3 = new Text(haikuText.getLine3());
        styleText(line1);
        styleText(line2);
        styleText(line3);
        haikuVBox.getChildren().addAll(line1, line2, line3);
        background.getChildren().add(haikuVBox);
    }

    private void styleText(Text text) {
        text.setStyle("-fx-font-family: 'Lucida Calligraphy'; -fx-font-size: 16px");
    }

    private class HaikuText {

        //IVARS
        private String line1;
        private String line2;
        private String line3;

        //GETTERS
        public String getLine1() { return this.line1; }
        public String getLine2() { return this.line2; }
        public String getLine3() { return this.line3; }

        //CONSTRUCTORS
        public HaikuText(String line1, String line2, String line3) {
            this.line1 = line1;
            this.line2 = line2;
            this.line3 = line3;
        }

    }

} //END OF CLASS
