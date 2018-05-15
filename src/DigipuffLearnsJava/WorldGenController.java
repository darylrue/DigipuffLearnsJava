package DigipuffLearnsJava;

import Ddialog.Ddialog;
import Ddialog.DialogType;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.converter.IntegerStringConverter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;

public class WorldGenController implements Initializable {

    private static final String DIR = "dir";
    private static final String PREFS_URL = System.getProperty("user.home") +
            "/.worldGenerator/properties.config";

    @FXML private ComboBox<String> dirCB;
    @FXML private ComboBox<String> numFLSymCB;
    @FXML private LimitedTextField numFLAmtTF;
    @FXML private ComboBox<String> numMovesSymCB;
    @FXML private LimitedTextField numMovesAmtTF;
    @FXML private ScrollPane worldPane;
    @FXML private ComboBox<Integer> numVertSpacesCB;
    @FXML private ComboBox<Integer> numHorizSpacesCB;
    @FXML private LimitedTextField spaceSizeTF;
    @FXML private ToggleButton wallBtn;
    @FXML private ToggleButton addHaikuBtn;
    @FXML private ToggleButton remHaikuBtn;
    @FXML private LimitedTextField haikuAddAmt;
    @FXML private LimitedTextField remHaikuAmt;
    @FXML private ToggleButton goalBtn;
    @FXML private MenuBar menuBar;
    @FXML private Menu fileMenu;

    private Path openWorldFile;
    private Properties prefs = new Properties();
    private Path prefsPath = Paths.get(PREFS_URL);
    private World world;
    private boolean loading = false;
    private String lastSavedWorldStr = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Hub.setRecording(false);  //otherwise haikus won't update on the screen
        loadPrefs();
        initComboBoxes();
        initTextFields();
        fileMenu.setOnShowing(e -> menuBar.requestFocus()); //so that text fields will be saved
        spaceSizeTF.setText(String.valueOf(Hub.DEFAULT_WORLD_SPACE_SIZE));
        getNewWorld();
        worldPane.setContent(world.getPane());
        setSaveWarning();
    }

    private void initComboBoxes() {
        //Number of Horizontal Spaces Combo Box
        for(int i = 1; i <= 20; i++) numHorizSpacesCB.getItems().add(i);
        numHorizSpacesCB.setOnAction(e -> numHorizSpacesCBOnAction());
        //Number of Vertical Spaces Combo Box
        for(int i = 1; i <= 20; i++) numVertSpacesCB.getItems().add(i);
        numVertSpacesCB.setOnAction(e -> numVertSpacesCBOnAction());
        //Goal Direction Combo Box
        dirCB.getItems().addAll(Hub.ANY, Dir.EAST.toString(), Dir.WEST.toString(),
                Dir.NORTH.toString(), Dir.SOUTH.toString());
        dirCB.setValue(Hub.ANY);
        //Goal numHaikus Symbol Combo Box
        populateSymCB(numFLSymCB);
        numFLAmtTF.setVisible(false);
        populateSymCB(numMovesSymCB);
        numMovesAmtTF.setVisible(false);
    }

    private void populateSymCB(ComboBox<String> cb) {
        cb.getItems().addAll(Hub.ANY, CompSym.EQUAL_TO.toString(),
                CompSym.GREATER_THAN.toString(), CompSym.LESS_THAN.toString(),
                CompSym.GREATER_THAN_OR_EQUAL_TO.toString(),
                CompSym.LESS_THAN_OR_EQUAL_TO.toString());
        cb.setValue(Hub.ANY);
    }

    private void numHorizSpacesCBOnAction() {
        if(loading) return;
        if(numHorizSpacesCB.getValue() == world.getNumHorizSpaces()) return;
        if(numHorizSpacesCB.getValue() < world.getNumHorizSpaces()) {
            if(horizShrinkCausesLostObjects()) {
                infoDialog("There are walls and/or objects preventing you " +
                        "from shrinking the world. Remove the objects, then resize.");
                Platform.runLater(() ->
                        numHorizSpacesCB.setValue(world.getNumHorizSpaces()));  //set it back
                return;
            }
        }
        world.setNumHorizSpaces(numHorizSpacesCB.getValue());
        getNewWorld(world.getWorldStr());
        redrawWorld();
    }

    private void numVertSpacesCBOnAction() {
        if(loading) return;
        if(numVertSpacesCB.getValue() == world.getNumVertSpaces()) return;
        if(numVertSpacesCB.getValue() < world.getNumVertSpaces()) {
            if(vertShrinkCausesLostObjects()) {
                infoDialog("There are walls and/or objects preventing you " +
                        "from shrinking the world. Remove the objects, then resize.");
                Platform.runLater(() ->
                        numVertSpacesCB.setValue(world.getNumVertSpaces()));  //set it back
                return;
            }
        }
        world.setNumVertSpaces(numVertSpacesCB.getValue());
        getNewWorld(world.getWorldStr());
        redrawWorld();
    }

    private void getNewWorld() {
        int spaceSize = Integer.valueOf(spaceSizeTF.getText());
        world = new World(spaceSize, numHorizSpacesCB.getValue(), numVertSpacesCB.getValue()) {
            @Override
            public void modifyGridCell(StackPane gridCell) {
                addMouseEvents(gridCell);
            }
        };
    }

    private void getNewWorld(String worldStr) {
        try {
            world = new World(worldStr) {
                @Override
                public void modifyGridCell(StackPane gridCell) {
                    addMouseEvents(gridCell);
                }
            };
        } catch (Exception e) {
            e.printStackTrace();
            Hub.infoDialog("Can't load world. The world file is corrupt.");
        }
    }

    private void redrawWorld() {
        worldPane.setContent(world.getPane());
    }

    @FXML
    private void changeSpaceSize() {
        if(spaceSizeTF.getText().equals("")) return;
        if(Integer.valueOf(spaceSizeTF.getText()) < 30) {
            Hub.infoDialog("Space size must be at least 30.");
            return;
        }
        int newSpaceSize = Integer.valueOf(spaceSizeTF.getText());
        if(newSpaceSize == world.getSpaceSize()) return;
        world.setSpaceSize(newSpaceSize);
        getNewWorld(world.getWorldStr());
        redrawWorld();
    }

    @FXML
    private void setGoalDir() {
        if(loading) return;
        if(dirCB.getValue().equals(Hub.ANY)) {
            world.getGoal().setDirection(null);
            return;
        }
        //value is a Direction
        world.getGoal().setDirection(Dir.valueOf(dirCB.getValue()));
    }

    @FXML
    private void setNumFLSym() {
        if(loading) return;
        if(numFLSymCB.getValue().equals(Hub.ANY)) {
            world.getGoal().setNumHaikusSym(null);
            numFLAmtTF.setVisible(false);
            return;
        }
        world.getGoal().setNumHaikusSym(CompSym.fromValue((numFLSymCB.getValue())));
        numFLAmtTF.setVisible(true);
    }

    @FXML
    private void setNumFLAmt() {
        if(world.getGoal().getNumHaikusSym() == null || numFLAmtTF.getText().equals("")) return;
        world.getGoal().setNumHaikus(Integer.valueOf(numFLAmtTF.getText()));
    }

    @FXML
    private void setNumMovesSym() {
        if(loading) return;
        if(numMovesSymCB.getValue().equals(Hub.ANY)) {
            world.getGoal().setNumMovesSym(null);
            numMovesAmtTF.setVisible(false);
            return;
        }
        world.getGoal().setNumMovesSym(CompSym.fromValue(numMovesSymCB.getValue()));
        numMovesAmtTF.setVisible(true);
    }

    @FXML
    private void setNumMovesAmt() {
        if(world.getGoal().getNumMovesSym() == null || numMovesAmtTF.getText().equals("")) return;
        world.getGoal().setNumMoves(Integer.valueOf(numMovesAmtTF.getText()));
    }

    @FXML
    private void openFile() {
        loading = true;
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open World File");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("World Files", "*.world"));
        String url = prefs.getProperty(DIR);
        if(url != null) {
            chooser.setInitialDirectory(new File(url));
        }
        File worldFile = chooser.showOpenDialog(WorldGenerator.getInstance().getStage());
        if(worldFile != null) {
            String worldStr;
            try {
                worldStr = new String(Files.readAllBytes(worldFile.toPath()));
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Could not open file.");
            }
            getNewWorld(worldStr);
            redrawWorld();
            openWorldFile = worldFile.toPath();
            spaceSizeTF.setText(String.valueOf(world.getSpaceSize()));
            numHorizSpacesCB.setValue(world.getNumHorizSpaces());
            numVertSpacesCB.setValue(world.getNumVertSpaces());
            System.out.println("direction: " + world.getGoal().directionStr());
            dirCB.setValue(world.getGoal().directionStr());
            numFLSymCB.setValue(world.getGoal().numHaikusSymStr());
            if(world.getGoal().getNumHaikusSym() != null)
                numFLAmtTF.setText(world.getGoal().numHaikusAmtStr());
            else numFLAmtTF.setText("0");
            if(numFLSymCB.getValue().equals(Hub.ANY))
                numFLAmtTF.setVisible(false);
            else numFLAmtTF.setVisible(true);
            System.out.println("NumMovesSym: " + world.getGoal().numMovesSymStr());
            numMovesSymCB.setValue(world.getGoal().numMovesSymStr());
            System.out.println("NumMoves: " + world.getGoal().numMovesAmtStr());
            if(world.getGoal().getNumMovesSym() != null)
                numMovesAmtTF.setText(world.getGoal().numMovesAmtStr());
            else numMovesAmtTF.setText("0");
            if(numMovesSymCB.getValue().equals(Hub.ANY))
                numMovesAmtTF.setVisible(false);
            else numMovesAmtTF.setVisible(true);
            WorldGenerator.getInstance().getStage().setTitle("World Generator - " + worldFile.getName());
        }
        loading = false;
    }

    @FXML
    private void saveFile() {
        if(openWorldFile == null) {
            saveFileAs();
            return;
        }
        String worldStr = world.getWorldStr();
        try {
            Files.write(openWorldFile, worldStr.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        lastSavedWorldStr = worldStr;
    }

    @FXML
    private void saveFileAs() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save file as:");
        FileChooser.ExtensionFilter filter =
                new FileChooser.ExtensionFilter("World Files", "*.world");
        chooser.getExtensionFilters().add(filter);
        String url = prefs.getProperty(DIR);
        if(url != null) {
            File urlFile = new File(url);
            if(Files.exists(urlFile.toPath())) {
                chooser.setInitialDirectory(urlFile);
            }
        }
        File newFile = chooser.showSaveDialog(WorldGenerator.getInstance().getStage());
        if(newFile != null) {
            String worldStr = world.getWorldStr();
            try {
                Files.write(newFile.toPath(), worldStr.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
            openWorldFile = newFile.toPath();
            WorldGenerator.getInstance().getStage().setTitle(
                    "World Generator - " + newFile.getName());
            lastSavedWorldStr = worldStr;
        }
    }

    @FXML
    private void setDirectory() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose your default directory:");
        Stage stage = WorldGenerator.getInstance().getStage();
        File dir = chooser.showDialog(stage);
        if(dir != null) {
            prefs.put(DIR, dir.getAbsolutePath());
            writePrefsToFile();
        }
    }

    private void setSaveWarning() {
        Stage stage = WorldGenerator.getInstance().getStage();
        stage.setOnCloseRequest(e -> {
            if(lastSavedWorldStr == null || !lastSavedWorldStr.equals(world.getWorldStr())) {
                onExitDialog(e,"Do you want to save changes?", yesEvent -> saveFile());
            }
        });
    }

    private void loadPrefs() {
        if(Files.exists(prefsPath)) {
            try {
                prefs.load(new FileReader(new File(PREFS_URL)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void writePrefsToFile() {
        if(!Files.exists(prefsPath)) {
            try {
                Files.createDirectories(prefsPath.getParent());
                Files.write(prefsPath, "".getBytes());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            prefs.store(new FileWriter(new File(PREFS_URL)), "");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean horizShrinkCausesLostObjects() {
        //horizontal shrink has just been attempted from the combo box
        for(int x = world.getNumHorizSpaces() - 1; x >= numHorizSpacesCB.getValue(); x--) {
            for(int y = 0; y < world.getNumVertSpaces(); y++) {
               if(isObjectAt(x, y)) return true;
            }
        }
        return false;
    }

    private boolean vertShrinkCausesLostObjects() {
        for(int y = world.getNumVertSpaces() - 1; y >= numVertSpacesCB.getValue(); y--) {
            for(int x = 0; x < world.getNumHorizSpaces(); x++) {
                if(isObjectAt(x, y)) return true;
            }
        }
        return false;
    }

    private boolean isObjectAt(int x, int y) {
        //solid object
        if(world.hasSolidObject(x, y)) return true;
        //goal
        if(world.isGoal(x, y)) return true;
        //wall
        if(world.getNumWallsAt(x, y) > 0) return true;
        //haikus
        if(world.isHaikuAt(x, y)) return true;
        return false;
    }

    private void infoDialog(String message) {
        Ddialog dialog = new Ddialog(DialogType.INFO, WorldGenerator.getInstance().getStage(), message);
        dialog.show();
    }

    private static void onExitDialog(WindowEvent exitEvent, String message, EventHandler<ActionEvent> onOKEvent) {
        Ddialog dialog = new Ddialog(DialogType.ONEXIT, WorldGenerator.getInstance()
                .getStage(), message);
        dialog.setOKEvent(onOKEvent);
        dialog.setCancelEvent(e -> exitEvent.consume());
        dialog.setOKButtonText("Yes");
        dialog.setNoButtonText("Don't save");
        dialog.showAndWait();
    }

    private void addMouseEvents(StackPane gridCell) {
        gridCell.setOnMouseClicked(e -> {
            double edgeDistance = world.getSpaceSize() * .15;
            double x = e.getX();
            double y = e.getY();
            if(y < edgeDistance) northClick(gridCell);
            else if(y > world.getSpaceSize() - edgeDistance) southClick(gridCell);
            else if(x < edgeDistance) westClick(gridCell);
            else if(x > world.getSpaceSize() - edgeDistance) eastClick(gridCell);
            else centerClick(gridCell);
        });
        //TODO - add gray wall / no wall on mouse hover
    }

    private void initTextFields() {
        //Text formatter for space size field
        spaceSizeTF.setTextFormatter(new TextFormatter<>(getSSIntegerConverter(),
                85, getIntegerFilter()));
        spaceSizeTF.focusedProperty().addListener(((observable, oldValue, newValue) -> {
            if(!newValue) {
                changeSpaceSize();
            }
        }));
        //Text formatter for goal num haikus field
        numFLAmtTF.setTextFormatter(new TextFormatter<>(getIntegerConverter(), 0,
                getIntegerFilter()));
        numFLAmtTF.focusedProperty().addListener(((observable, oldValue, newValue) -> {
            if (!newValue) {
                setNumFLAmt();
            }
        }));
        //Text formatter for goal num moves field
        numMovesAmtTF.setTextFormatter(new TextFormatter<>(getIntegerConverter(), 0,
                getIntegerFilter()));
        numMovesAmtTF.focusedProperty().addListener(((observable, oldValue, newValue) -> {
            if (!newValue) {
                setNumMovesAmt();
            }
        }));
        //Text formatter for add haiku field
        haikuAddAmt.setTextFormatter(new TextFormatter<>(getIntegerConverter(),
                1, getIntegerFilter()));
        //Text formatter for remove haiku field
        remHaikuAmt.setTextFormatter(new TextFormatter<>(getIntegerConverter(),
                1, getIntegerFilter()));

    }

    private UnaryOperator<TextFormatter.Change> getIntegerFilter() {
        return change -> {
            String newText = change.getControlNewText();
            if(newText.matches("([1-9][0-9]*)?")) return change;
            return null;
        };
    }

    private IntegerStringConverter getIntegerConverter() {
        return new IntegerStringConverter() {
            @Override
            public Integer fromString(String s) {
                if(s.isEmpty()) return 1;   //let a blank field be returned as 1
                return super.fromString(s);
            }
        };
    }

    private IntegerStringConverter getSSIntegerConverter() {
        return new IntegerStringConverter() {
            @Override
            public Integer fromString(String s) {
                if(s.isEmpty()) return Hub.DEFAULT_WORLD_SPACE_SIZE;
                return super.fromString(s);
            }
        };
    }

    private IntPoint getCoord(StackPane gridCell) {
        int worldX = GridPane.getColumnIndex(gridCell);
        int worldY = world.getYFromRow(GridPane.getRowIndex(gridCell));
        return new IntPoint(worldX, worldY);
    }

    private IntPoint oneSpaceNorthCoord(IntPoint coord) {
        return new IntPoint(coord.getX(), coord.getY() + 1);
    }

    private IntPoint oneSpaceSouthCoord(IntPoint coord) {
        return new IntPoint(coord.getX(), coord.getY() - 1);
    }

    private IntPoint oneSpaceEastCoord(IntPoint coord) {
        return new IntPoint(coord.getX() + 1, coord.getY());
    }

    private IntPoint oneSpaceWestCoord(IntPoint coord) {
        return new IntPoint(coord.getX() - 1, coord.getY());
    }

    private void northClick(StackPane gridCell) {
        if(addHaikuBtn.isSelected()) addHaikuClick(gridCell);
        if(remHaikuBtn.isSelected()) remHaikuClick(gridCell);
        if(goalBtn.isSelected()) goalClick(gridCell);
        if(wallBtn.isSelected()) {
            IntPoint p1 = getCoord(gridCell);
            //make sure gridCell is not on the top row
            if(p1.getY() == world.getNumVertSpaces() - 1) return;
            IntPoint p2 = oneSpaceNorthCoord(p1);
            if(world.isWall(p1, p2)) {
                world.removeWall(p1, p2);
            } else {
                //add the wall
                world.addWall(p1, p2);
            }
        }
    }

    private void southClick(StackPane gridCell) {
        if(addHaikuBtn.isSelected()) addHaikuClick(gridCell);
        if(remHaikuBtn.isSelected()) remHaikuClick(gridCell);
        if(goalBtn.isSelected()) goalClick(gridCell);
        if(wallBtn.isSelected()) {
            IntPoint p1 = getCoord(gridCell);
            //make sure gridCell is not on the bottom row
            if(p1.getY() == 0) return;
            IntPoint p2 = oneSpaceSouthCoord(p1);
            if(world.isWall(p1, p2)) {
                world.removeWall(p1, p2);
            } else {
                //add the wall
                world.addWall(p1, p2);
            }
        }
    }

    private void eastClick(StackPane gridCell) {
        if(addHaikuBtn.isSelected()) addHaikuClick(gridCell);
        if(remHaikuBtn.isSelected()) remHaikuClick(gridCell);
        if(goalBtn.isSelected()) goalClick(gridCell);
        if(wallBtn.isSelected()) {
            IntPoint p1 = getCoord(gridCell);
            //make sure gridCell is not on the rightmost column
            if(p1.getX() == world.getNumHorizSpaces() - 1) return;
            IntPoint p2 = oneSpaceEastCoord(p1);
            if(world.isWall(p1, p2)) {
                world.removeWall(p1, p2);
            } else {
                //add the wall
                world.addWall(p1, p2);
            }
        }
    }

    private void westClick(StackPane gridCell) {
        if(addHaikuBtn.isSelected()) addHaikuClick(gridCell);
        if(remHaikuBtn.isSelected()) remHaikuClick(gridCell);
        if(goalBtn.isSelected()) goalClick(gridCell);
        if(wallBtn.isSelected()) {
            IntPoint p1 = getCoord(gridCell);
            //make sure gridCell is not on the leftmost column
            if(p1.getX() == 0) return;
            IntPoint p2 = oneSpaceWestCoord(p1);
            if(world.isWall(p1, p2)) {
                world.removeWall(p1, p2);
            } else {
                //add the wall
                world.addWall(p1, p2);
            }
        }
    }

    private void centerClick(StackPane gridCell) {
        if(addHaikuBtn.isSelected()) addHaikuClick(gridCell);
        if(remHaikuBtn.isSelected()) remHaikuClick(gridCell);
        if(goalBtn.isSelected()) goalClick(gridCell);
    }

    private void addHaikuClick(StackPane gridCell) {
        IntPoint coord = getCoord(gridCell);
        String addAmtStr = haikuAddAmt.getText();
        if(addAmtStr.equals("")) addAmtStr = "1";
        int addAmt = Integer.valueOf(addAmtStr);
        for(int i = 0; i < addAmt; i++) {
            world.addHaiku(coord.getX(), coord.getY());
        }
    }

    private void remHaikuClick(StackPane gridCell) {
        IntPoint coord = getCoord(gridCell);
        if(!world.isHaikuAt(coord.getX(), coord.getY())) return;
        String remAmtStr = remHaikuAmt.getText();
        if(remAmtStr.equals("")) remAmtStr = "1";
        int remAmt = Integer.valueOf(remAmtStr);
        for(int i = 0; i < remAmt; i++) {
            if(world.isHaikuAt(coord.getX(), coord.getY()))
                world.removeHaiku(coord.getX(), coord.getY());
        }
    }

    private void goalClick(StackPane gridCell) {
        IntPoint coord = getCoord(gridCell);
        if(world.isGoal(coord.getX(), coord.getY())) {
            world.eraseGoal();
            world.getGoal().setX(null);
            world.getGoal().setY(null);
            return;
        }
        world.getGoal().setX(coord.getX());
        world.getGoal().setY(coord.getY());
        world.drawGoal();
    }

} //END OF CLASS


