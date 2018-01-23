package DigipuffLearnsJava;

import Ddialog.*;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;

public class World {

    //CONSTANTS
    private static final double DEFAULT_GRID_LINE_WIDTH = 10;
    public static final double BORDER_WIDTH = 5;
    private static final String SS_TAG = "ss=";
    private static final String NHS_TAG = "nhs=";
    private static final String NVS_TAG = "nvs=";
    private static final String WALLS_TAG = "walls=";
    private static final String HAIKUS_TAG = "haikus=";
    private static final String GOAL_TAG = "goal=";
    private static final String HAIKU_ID = "haiku";
    private static final String GOAL_ID = "g";
    private static final String SOUTH_WALL_ID = "southWall";
    private static final String NORTH_WALL_ID = "northWall";
    private static final String EAST_WALL_ID = "eastWall";
    private static final String WEST_WALL_ID = "westWall";
    private static final String TOP_LEFT_ID = "topLeft";
    private static final String TOP_RIGHT_ID = "topRight";
    private static final String BOTTOM_LEFT_ID = "bottomLeft";
    private static final String BOTTOM_RIGHT_ID = "bottomRight";
    private static final String NUM_BOX_ID = "numBox";
    private static final String DEFAULT_HAIKU_URL = "images/haiku.png";
    private static final String ZERO_URL = "images/0.png";
    private static final String LEADING_ONE_URL = "images/l1.png";
    private static final String TRAILING_ONE_URL = "images/t1.png";
    private static final String TWO_URL = "images/2.png";
    private static final String THREE_URL = "images/3.png";
    private static final String FOUR_URL = "images/4.png";
    private static final String FIVE_URL = "images/5.png";
    private static final String SIX_URL = "images/6.png";
    private static final String SEVEN_URL = "images/7.png";
    private static final String EIGHT_URL = "images/8.png";
    private static final String NINE_URL = "images/9.png";
    private static final String PLUS_URL = "images/plus.png";
    private static final int MAX_NUM_MOVES = 10000;

    //IVARS
    private int spaceSize;
    private int numHorizSpaces;
    private int numVertSpaces;
    private GridPane grid;
    private Pane pane;
    private List<Wall> walls;
    private int[][] haikus;
    private Goal goal;
    private double gridLineWidth;
    private String haikuUrl;
    private WorldObjectList[][] map;
    private List<Digipuff> digipuffList;
    private ArrayList<DigiAction> actions;
    private boolean actionSequenceStarted = false;
    private InitState initState;

    //CONSTRUCTORS
    public World(int spaceSize, int numHorizSpaces, int numVertSpaces) {
        this.spaceSize = spaceSize;
        this.numHorizSpaces = numHorizSpaces;
        this.numVertSpaces = numVertSpaces;
        initHaikus();
        buildGrid();
        map = new WorldObjectList[numHorizSpaces][numVertSpaces];
        this.pane = new Pane(this.grid);
    }

    public World(String worldStr) throws Exception {
        worldStr = worldStr.replaceAll("\\s", "");
        errorCheckWorldStr(worldStr);
        this.spaceSize = parseSS(worldStr);
        this.numHorizSpaces = parseNHS(worldStr);
        this.numVertSpaces = parseNVS(worldStr);
        parseGoal(worldStr);
        List<Wall> wallList = parseWalls(worldStr);
        List<HaikuPoint> flList = parseHaikus(worldStr);
        initHaikus();
        buildGrid();
        setWalls(wallList);
        drawWalls();
        setHaikus(flList);
        drawGoal();
        map = new WorldObjectList[getNumHorizSpaces()][getNumVertSpaces()];
        this.pane = new Pane(this.grid);
    }

    //GETTERS
    public int getWorldWidth() {
        return spaceSize * numHorizSpaces;
    }

    public int getWorldHeight() {
        return spaceSize * numVertSpaces;
    }

    public String getWorldStr() {
        return SS_TAG + String.valueOf(spaceSize) +
                NHS_TAG + String.valueOf(numHorizSpaces) +
                NVS_TAG + String.valueOf(numVertSpaces) +
                WALLS_TAG + wallsToString() +
                HAIKUS_TAG + haikusToString() +
                GOAL_TAG + goalToString();
    }

    public int getSpaceSize() {
        return spaceSize;
    }

    public int getNumHorizSpaces() {
        return numHorizSpaces;
    }

    public int getNumVertSpaces() {
        return numVertSpaces;
    }

    public GridPane getGrid() {
        return grid;
    }

    public Pane getPane() {
        return this.pane;
    }

    private List<Wall> getWalls() {
        if(walls == null) walls = new ArrayList<>();
        return walls;
    }

    public double getGridLineWidth() {
        if(gridLineWidth == 0)
            gridLineWidth = DEFAULT_GRID_LINE_WIDTH;
        return gridLineWidth;
    }

    public Goal getGoal() {
        if(goal == null) goal = new Goal();
        return goal;
    }

    public String getHaikuUrl() {
        if(haikuUrl == null)
            haikuUrl = DEFAULT_HAIKU_URL;
        return haikuUrl;
    }

    public ArrayList<WorldObject> getWObjList(int x, int y) {
        if(map[x][y] != null) return map[x][y].list;
        return null;
    }

    public List<Digipuff> r2List() {
        if(digipuffList == null) digipuffList = new ArrayList<Digipuff>();
        return digipuffList;
    }

    private List<DigiAction> getActions() {
        if(actions == null) actions = new ArrayList<>();
        return actions;
    }

    public List<DigiAction> getActionsAndClear() {
        List<DigiAction> actionList = getActions();
        this.actions = new ArrayList<>();
        return actionList;
    }

    public boolean isWall(int x1, int y1, int x2, int y2) {
        if(x1 < 0 || x2 < 0 || x1 >= numHorizSpaces || x2 >= numHorizSpaces)
            return true;
        if(y1 < 0 || y2 < 0 || y1 >= numVertSpaces || y2 >= numVertSpaces)
            return true;
        Wall tempWall = new Wall(new IntPoint(x1, y1), new IntPoint(x2, y2));
        for(Wall wall: getWalls()) {
            if(wall.equals(tempWall)) return true;
        }
        return false;
    }

    public boolean isWall(IntPoint p1, IntPoint p2) {
        if(p1.getX() < 0 || p2.getX() < 0 || p1.getX() >= numHorizSpaces || p2.getX() >= numHorizSpaces)
            return true;
        if(p1.getY() < 0 || p2.getY() < 0 || p1.getY() >= numVertSpaces || p2.getY() >= numVertSpaces)
            return true;
        Wall tempWall = new Wall(p1, p2);
        for(Wall wall: getWalls()) {
            if (wall.equals(tempWall)) return true;
        }
        return false;
    }

    private boolean isWallNorth(IntPoint space) {
        return isWall(space.getX(), space.getY(), space.getX(), space.getY() + 1);
    }

    private boolean isWallSouth(IntPoint space) {
        return isWall(space.getX(), space.getY(), space.getX(), space.getY() - 1);
    }

    private boolean isWallEast(IntPoint space) {
        return isWall(space.getX(), space.getY(), space.getX() + 1, space.getY());
    }

    private boolean isWallWest(IntPoint space) {
        return isWall(space.getX(), space.getY(), space.getX() - 1, space.getY());
    }

    public boolean isOutOfBounds(int x, int y) {
        return x >= numHorizSpaces || x < 0 || y >= numVertSpaces || y < 0;
    }

    public boolean hasSolidObject(int x, int y) {
        if(map.length <= x || map[0].length <= y) return false;  //the point is off the map
        if(map[x][y] == null) return false;  //no object list has been created at this location
        for(WorldObject obj: map[x][y].list) {
            if(obj instanceof SolidObject) return true;
        }
        return false;
    }

    public int[][] getHaikus() {
        return haikus;
    }

    public boolean isHaikuAt(int x, int y) {
        return haikus[x][y] > 0;
    }

    public boolean isGoal(int x, int y) {
        return goal != null && goal.getX() != null && goal.getX() == x &&
                goal.getY() != null && goal.getY() == y;
    }

     //SETTERS
    public void setSpaceSize(int size) {
        spaceSize = size;
    }

    public void setNumHorizSpaces(int numSpaces) {
        numHorizSpaces = numSpaces;
    }

    public void setNumVertSpaces(int numSpaces) {
        numVertSpaces = numSpaces;
    }

    public void setWalls(List<Wall> wallList) {
        walls = wallList;
    }

    public void addWall(IntPoint p1, IntPoint p2) {

        Wall wall = new Wall(p1, p2);
        getWalls().add(wall);
        drawSingleWall(wall);
        fillInCornersIfNecessary(wall);
    }

    public void removeWall(IntPoint p1, IntPoint p2) {
        Wall wallToRem = new Wall(p1, p2);
        List<Wall> wallList = getWalls();
        for(int i = 0; i < wallList.size(); i++) {
            Wall wall = wallList.get(i);
            if(wall.equals(wallToRem)) {
                wallList.remove(wall);
                eraseWall(wall);
                eraseCornersIfNecessary(wall);
                break;
            }
        }
    }

    public void setHaikus(List<HaikuPoint> haikuPointList) {
        if(haikuPointList == null) return;
        for(HaikuPoint flPoint: haikuPointList) {
            int multiplicity = flPoint.getMultiplicity();
            for(int i = 0; i < multiplicity; i++) {
                haikus[flPoint.getIntPoint().getX()][flPoint.getIntPoint().getY()]++;
                drawHaiku(flPoint.getIntPoint().getX(), flPoint.getIntPoint().getY());
            }
        }
    }

    public void setHaikuUrl(String url) {
        haikuUrl = url;
    }

    public void setGridLineWidth(double width) {
        gridLineWidth = width;
    }

    public void setGoal(Goal goal) {
        eraseGoal();
        this.goal = goal;
        drawGoal();
    }

    public void addWorldObject(WorldObject o, int x, int y) {
        if(map[x][y] == null) map[x][y] = new WorldObjectList();
        map[x][y].list.add(o);
    }

    public void removeWorldObject(WorldObject o) {
        for(int i = 0; i < map.length; i++) {
            for(int j = 0; j < map[0].length; j++) {
                if(map[i][j] != null && map[i][j].list.contains(o)) {
                    map[i][j].list.remove(o);
                    return;
                }
            }
        }
    }

    public void addHaiku(int x, int y) {
        haikus[x][y]++;
        if(!Hub.isRecording()) drawHaiku(x, y);
    }

    public void removeHaiku(int x, int y) {
        haikus[x][y]--;
        if(!Hub.isRecording()) {
        removeHaikuImage(x, y);
        }
    }

    private void removeHaikuImage(int x, int y) {
        List<Node> nodeList = getNodeListAt(x, y);
        for (int i = 0; i < nodeList.size(); i++) {
            Node node = nodeList.get(i);
            //change number if more than 1 haiku
            if (haikus[x][y] > 0) {
                //change or remove the number
                if (node.getId() != null && node.getId().equals(NUM_BOX_ID)) {
                    nodeList.remove(node);
                    if (haikus[x][y] > 1) drawNumber(x, y);
                }
            } else { //remove the haiku image
                if (node.getId() != null && node.getId().equals(HAIKU_ID)) {
                    nodeList.remove(node);
                }
            }
        }
    }

    public void addAction(DigiAction action) {
        if(!actionSequenceStarted) {
            saveInitialState();
            actionSequenceStarted = true;
        }
        getActions().add(action);
        if(getActions().size() >= MAX_NUM_MOVES) {
            Hub.setMaxMovesReached(true);
            Platform.runLater(()->Hub.replay());
            Hub.pauseRecordingThread();
        }
    }

    //OTHER METHODS
    public void prepareForReplay() {
        haikus = initState.haikus;
        redrawHaikus();
        map = initState.map;
    }

    private void saveInitialState() {
        initState = new InitState();
        initState.haikus = clone(haikus);
        initState.map = clone(map);
    }

    private int[][] clone(int[][] oldArray) {
        int[][] newArray = new int[oldArray.length][oldArray[0].length];
        for(int i = 0; i < oldArray.length; i++) {
            for(int j = 0; j < oldArray[0].length; j++) {
                newArray[i][j] = oldArray[i][j];
            }
        }
        return newArray;
    }

    private WorldObjectList[][] clone(WorldObjectList[][] oldArray) {
        WorldObjectList[][] newArray = new WorldObjectList[oldArray.length][oldArray[0].length];
        for(int i = 0; i < oldArray.length; i++) {
            for(int j = 0; j < oldArray[0].length; j++) {
                newArray[i][j] = oldArray[i][j];
            }
        }
        return newArray;
    }

    private void buildGrid() {
        grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setStyle("-fx-border-width: " + BORDER_WIDTH + "; -fx-border-color: red");
        //iterate through each column
        for(int i = 0; i < numHorizSpaces; i++) {
            //iterate through each row
            for(int j = 0; j < numVertSpaces; j++) {
                grid.add(gridCell(), i, j);
            }
        }
    }

    private StackPane gridCell() {
        double borderWidth = getGridLineWidth() / 2;
        Rectangle rect = new Rectangle(getSpaceSize(), getSpaceSize());
        rect.setStroke(Color.GHOSTWHITE);
        rect.setStrokeWidth(borderWidth);
        rect.setFill(new ImagePattern(new Image("images/white-marble.jpg")));
        StackPane gridCell = new StackPane();
        gridCell.setAlignment(Pos.CENTER);
        gridCell.getChildren().add(rect);
        modifyGridCell(gridCell);
        return gridCell;
    }

    public void modifyGridCell(StackPane gridCell) {
        //to be overridden
    }

    public int getRow(int worldY) {
        return getNumVertSpaces() - 1 - worldY;
    }

    public int getYFromRow(int row) {
        return getNumVertSpaces() - 1 - row;
    }

    private void initHaikus() {
        haikus = new int[numHorizSpaces][numVertSpaces];
        for(int i = 0; i < numHorizSpaces; i++) {
            for(int j = 0; j < numVertSpaces; j++) {
                haikus[i][j] = 0;
            }
        }
    }

    public void errorCheckWorldStr(String worldStr) throws Exception {
        if(!worldStr.contains(SS_TAG) ||
                !worldStr.contains(NHS_TAG) ||
                !worldStr.contains(NVS_TAG) ||
                !worldStr.contains(WALLS_TAG) ||
                !worldStr.contains(HAIKUS_TAG) ||
                !worldStr.contains(GOAL_TAG) ||
                !worldStr.contains(Goal.LOC_TAG) ||
                !worldStr.contains(Goal.DIR_TAG) ||
                !worldStr.contains(Goal.NUM_HAIKUS_TAG) ||
                !worldStr.contains(Goal.NUM_MOVES_TAG))
            throw new RuntimeException("Error in world string. One or more tags not found.");
    }

    private int parseSS(String worldStr) throws Exception {
        int beginIndex = worldStr.indexOf(SS_TAG) + SS_TAG.length();
        int endIndex = nextNonDigitIndex(worldStr, beginIndex + 1);
        if(endIndex == -1)
            throw new RuntimeException("End index of " + SS_TAG + " not found in world string.");
        String spaceSizeStr = worldStr.substring(beginIndex, endIndex);
        return Integer.valueOf(spaceSizeStr);
    }

    public int nextNonDigitIndex(String str, int beginIndex) {
        for(int i = beginIndex; i < str.length(); i++) {
            if(!Character.isDigit(str.charAt(i)))
                return i;
        }
        return -1;
    }

    private int nextDigitIndex(String str, int beginIndex) {
        for(int i = beginIndex; i < str.length(); i++) {
            if(Character.isDigit(str.charAt(i)))
                return i;
        }
        return -1;
    }

    private int nextOpenBracketIndex(String str, int beginIndex) {
        for(int i = beginIndex; i < str.length(); i++) {
            if(str.charAt(i) == '[') {
                return i;
            }
        }
        return -1;
    }

    private int nextOpenParenIndex(String str, int beginIndex) {
        for(int i = beginIndex; i < str.length(); i++) {
            if(str.charAt(i) == '(') {
                return i;
            }
        }
        return -1;
    }

    private int parseNHS(String worldStr) throws Exception {
        int beginIndex = worldStr.indexOf(NHS_TAG) + NHS_TAG.length();
        int endIndex = nextNonDigitIndex(worldStr, beginIndex + 1);
        if(endIndex == -1)
            throw new RuntimeException("End index of " + NHS_TAG + " not found in world string.");
        String numHorizSpacesStr = worldStr.substring(beginIndex, endIndex);
        return Integer.valueOf(numHorizSpacesStr);
    }

    private int parseNVS(String worldStr) throws Exception {
        int beginIndex = worldStr.indexOf(NVS_TAG) + NVS_TAG.length();
        int endIndex = nextNonDigitIndex(worldStr, beginIndex + 1);
        if(endIndex == -1) endIndex = worldStr.length();
//            throw new RuntimeException("End index of " + NVS_TAG + " not found in world string.");
        String numVertSpacesStr = worldStr.substring(beginIndex, endIndex);
        return Integer.valueOf(numVertSpacesStr);
    }

    private List<Wall> parseWalls(String worldStr) throws Exception {
        int i = worldStr.indexOf(WALLS_TAG) + WALLS_TAG.length();
        if(Character.isLetter(worldStr.charAt(i))) return null; //there are no walls
        List<Wall> wallList = new ArrayList<>();
        int nextWallIndex = nextOpenBracketIndex(worldStr, i);
        while(nextWallIndex >= 0) {
            i = nextOpenParenIndex(worldStr, nextWallIndex);
            if(i >= 0) {
                IntPoint p1 = parseSinglePoint(worldStr, i);
                i = nextOpenParenIndex(worldStr, i + 1);
                IntPoint p2 = parseSinglePoint(worldStr, i);
                wallList.add(new Wall(p1, p2));
                nextWallIndex = nextOpenBracketIndex(worldStr, i);
            }
        }
        return wallList;
    }

    private List<HaikuPoint> parseHaikus(String worldStr) throws Exception {
        int i = worldStr.indexOf(HAIKUS_TAG) + HAIKUS_TAG.length();
        if(Character.isLetter(worldStr.charAt(i))) return null; //there are no haikus
        return parseHaikuPoints(worldStr, i);
    }

    private void parseGoal(String worldStr) throws Exception {
        int i;
        //Location
        i = worldStr.indexOf(Goal.LOC_TAG) + Goal.LOC_TAG.length();
        int end = worldStr.indexOf(Goal.DIR_TAG);
        String locStr = worldStr.substring(i, end);
        parseGoalLocation(locStr);
        //Direction
        i = worldStr.indexOf(Goal.DIR_TAG) + Goal.DIR_TAG.length();
        end = worldStr.indexOf(Goal.NUM_HAIKUS_TAG);
        String dirStr = worldStr.substring(i, end);
        parseGoalDir(dirStr);
        //Number of haikus
        i = worldStr.indexOf(Goal.NUM_HAIKUS_TAG) + Goal.NUM_HAIKUS_TAG.length();
        end = worldStr.indexOf(Goal.NUM_MOVES_TAG);
        String numHaikusStr = worldStr.substring(i, end);
        parseGoalNumHaikus(numHaikusStr);
        //Number of Moves
        i = worldStr.indexOf(Goal.NUM_MOVES_TAG) + Goal.NUM_MOVES_TAG.length();
        String numMovesStr = worldStr.substring(i);
        parseGoalNumMoves(numMovesStr);
    }

    private void parseGoalLocation(String locStr) throws Exception {
        //x value
        int i = locStr.indexOf('(') + 1;
        int end = locStr.indexOf(',');
        String xStr = locStr.substring(i, end);
        if(Character.isDigit(xStr.charAt(0))) getGoal().setX(Integer.valueOf(xStr));
        //otherwise leave it null
        //y value
        i = end + 1;
        end = locStr.indexOf(')');
        String yStr = locStr.substring(i, end);
        if(Character.isDigit(yStr.charAt(0))) getGoal().setY(Integer.valueOf(yStr));
        //otherwise leave it null
    }

    private void parseGoalDir(String dirStr) throws Exception {
        if(dirStr.equals(Hub.ANY)) return; //leave direction null
        getGoal().setDirection(Dir.valueOf(dirStr));
    }

    private void parseGoalNumHaikus(String numHaikusStr) throws Exception {
        //numHaikusStr contains symbol and number
        if(numHaikusStr.equals(Hub.ANY)) return; //leave numHaikusSym null
        int end = nextDigitIndex(numHaikusStr, 1);
        getGoal().setNumHaikusSym(CompSym.fromValue(numHaikusStr.substring(0, end)));
        getGoal().setNumHaikus(Integer.valueOf(numHaikusStr.substring(end)));

    }

    private void parseGoalNumMoves(String numMovesStr) throws Exception{
        //numMovesStr contains symbol and number
        if(numMovesStr.equals(Hub.ANY)) return; //leave numMovesSym null
        int end = nextDigitIndex(numMovesStr, 1);
        getGoal().setNumMovesSym(CompSym.fromValue(numMovesStr.substring(0, end)));
        getGoal().setNumMoves(Integer.valueOf(numMovesStr.substring(end)));
    }

    private List<HaikuPoint> parseHaikuPoints(String str, int i) throws Exception {
        List<HaikuPoint> pointList = new ArrayList<>();
        int nextPointIndex = str.indexOf('(', i);
        while(nextPointIndex >= 0) {
            int xStrStart = nextDigitIndex(str, nextPointIndex);
            int xStrEnd = nextNonDigitIndex(str, xStrStart);
            String xStr = str.substring(xStrStart, xStrEnd);
            int yStrStart = nextDigitIndex(str, xStrEnd);
            int yStrEnd = nextNonDigitIndex(str, yStrStart);
            String yStr = str.substring(yStrStart, yStrEnd);
            i = yStrEnd;
            int multiplicity = 1;
            if(str.charAt(yStrEnd + 1) == 'x') {  //more than one haiku at this point
                int multiplicityStart = yStrEnd + 2;
                int multiplicityEnd = nextNonDigitIndex(str, multiplicityStart);
                String multiplicityStr = str.substring(multiplicityStart, multiplicityEnd);
                multiplicity = Integer.valueOf(multiplicityStr);
                i = multiplicityEnd;
            }
            IntPoint point = new IntPoint(Integer.valueOf(xStr), Integer.valueOf(yStr));
            pointList.add(new HaikuPoint(point, multiplicity));
            //find start of next point (or stop at next tag)

            while(i < str.length()) {
                if(Character.isLetter(str.charAt(i))) {
                    nextPointIndex = -1;
                    break;
                } else if(str.charAt(i) == '(') {
                    nextPointIndex = i;
                    break;
                }
                i++;
            }
        }
        return pointList;
    }

    private IntPoint parseSinglePoint(String str, int i) throws Exception {
        int nextPointIndex = str.indexOf('(', i);
        if(nextPointIndex >= 0) {
            int xStrStart = nextDigitIndex(str, nextPointIndex);
            int xStrEnd = nextNonDigitIndex(str, xStrStart);
            String xStr = str.substring(xStrStart, xStrEnd);
            int yStrStart = nextDigitIndex(str, xStrEnd);
            int yStrEnd = nextNonDigitIndex(str, yStrStart);
            String yStr = str.substring(yStrStart, yStrEnd);
            return new IntPoint(Integer.valueOf(xStr), Integer.valueOf(yStr));
        }
        return null;
    }

    private void drawHaiku(int x, int y) {
        StackPane stackPane = getPaneAt(x, y);
        if(haikus[x][y] == 1) {
            ImageView haikuIV = new ImageView(getHaikuUrl());
            haikuIV.setFitHeight(getSpaceSize() - getGridLineWidth());
            haikuIV.setFitWidth(getSpaceSize() - getGridLineWidth());
            haikuIV.setId(HAIKU_ID);
            stackPane.getChildren().add(haikuIV);
            StackPane.setAlignment(haikuIV, Pos.CENTER);
        } else drawNumber(x, y);
        //check if Digipuff is on this space. If so, move him to the front
        for(int i = 0; i < stackPane.getChildren().size(); i++) {
            Node node = stackPane.getChildren().get(i);
            if(node.getId() != null && node.getId().equals(Hub.R2_ID)) {
                node.toFront();
            }
        }
    }

    private void drawNumber(int x, int y) {
        HBox numBox = new HBox();
        String numStr = String.valueOf(haikus[x][y]);
        if(haikus[x][y] > 99) numStr = "99";
        ImageView tens;
        ImageView ones;
        if(numStr.length() > 1) {
            tens = getNumImage(numStr.charAt(0), DecPlace.TENS);
            ones = getNumImage(numStr.charAt(1), DecPlace.ONES);
            numBox.getChildren().addAll(tens, ones);
        } else {
            ones = getNumImage(numStr.charAt(0), DecPlace.ONES);
            numBox.getChildren().add(ones);
        }
        if(haikus[x][y] > 99) numBox.getChildren().add(new ImageView(PLUS_URL));
        numBox.setAlignment(Pos.CENTER);
        numBox.setId(NUM_BOX_ID);
        if(haikus[x][y] > 2) {
            //remove existing numBox
            removeNumBox(x, y);
        }
        getPaneAt(x, y).getChildren().add(numBox);
    }

    private void removeNumBox(int x, int y) {
        List<Node> nodeList = getPaneAt(x, y).getChildren();
        for(int i = 0; i < nodeList.size(); i++) {
            Node node = nodeList.get(i);
            if(node.getId() != null && node.getId().equals(NUM_BOX_ID)) {
                nodeList.remove(node);
            }
        }
    }

    private void redrawHaikus() {
        boolean recordingValue = Hub.isRecording();
        if(Hub.isRecording())
            Hub.setRecording(false);  //this is necessary so that the add and removeHaiku methods will update graphics
        for(int i = 0; i < haikus.length; i++) {
            for(int j = 0; j < haikus[0].length; j++) {
                int num = haikus[i][j];
                if(num >0) {
                    for(int k = 0; k < num; k++) {
                        removeHaiku(i, j);
                    }
                    for(int m = 0; m < num; m++) {
                        addHaiku(i, j);
                    }
                }
            }
        }
        if(recordingValue)
            Hub.setRecording(true);
    }

    public void eraseGoal() {
        if(getGoal().getX() == null || getGoal().getY() == null) return;
        List<Node> nodeList = getNodeListAt(getGoal().getX(), getGoal().getY());
        for(Node n: nodeList) {
            if(n.getId() != null && n.getId().equals(GOAL_ID)) {
                nodeList.remove(n);
                break;
            }
        }
    }

    public void drawGoal() {
        if(getGoal().getX() == null || getGoal().getY() == null) return;
        StackPane goalPane = getPaneAt(getGoal().getX(), getGoal().getY());
        ImageView goalIV = getGoal().getImage();
        goalIV.setFitHeight(getSpaceSize() - getGridLineWidth());
        goalIV.setFitWidth(getSpaceSize() - getGridLineWidth());
        goalIV.setId(GOAL_ID);
        goalPane.getChildren().add(goalIV);
    }

    private void drawWalls() {
        for(Wall wall: getWalls()) {
            drawSingleWall(wall);
        }
        fillInWallCorners();
    }

    private void drawSingleWall(Wall wall) {
        if(wall.isHoriz()) {
            //line at bottom of upper space
            int upperY = Math.max(wall.getP1().getY(), wall.getP2().getY());
            Line upperSpaceLine = new Line(0, getSpaceSize(), getSpaceSize(), getSpaceSize());
            upperSpaceLine.setStrokeWidth(getGridLineWidth() / 2);
            upperSpaceLine.setId(SOUTH_WALL_ID);
            StackPane.setAlignment(upperSpaceLine, Pos.BOTTOM_CENTER);
            getPaneAt(wall.getP1().getX(), upperY).getChildren().add(upperSpaceLine);
            //line at top of lower space
            int lowerY = Math.min(wall.getP1().getY(), wall.getP2().getY());
            Line lowerSpaceLine = new Line(0 , 0, getSpaceSize(), 0);
            lowerSpaceLine.setStrokeWidth(getGridLineWidth() / 2);
            lowerSpaceLine.setId(NORTH_WALL_ID);
            StackPane.setAlignment(lowerSpaceLine, Pos.TOP_CENTER);
            getPaneAt(wall.getP1().getX(), lowerY).getChildren().add(lowerSpaceLine);
        } else {
            //wall is vertical
            int leftSpaceX = Math.min(wall.getP1().getX(), wall.getP2().getX());
            Line leftSpaceLine = new Line(getSpaceSize(), 0, getSpaceSize(), getSpaceSize());
            leftSpaceLine.setStrokeWidth(getGridLineWidth() / 2);
            leftSpaceLine.setId(EAST_WALL_ID);
            StackPane.setAlignment(leftSpaceLine, Pos.CENTER_RIGHT);
            getPaneAt(leftSpaceX, wall.getP1().getY()).getChildren().add(leftSpaceLine);
            int rightSpaceX = Math.max(wall.getP1().getX(), wall.getP2().getX());
            Line rightSpaceLine = new Line(0, 0, 0, getSpaceSize());
            rightSpaceLine.setStrokeWidth(getGridLineWidth() / 2);
            rightSpaceLine.setId(WEST_WALL_ID);
            StackPane.setAlignment(rightSpaceLine, Pos.CENTER_LEFT);
            getPaneAt(rightSpaceX, wall.getP1().getY()).getChildren().add(rightSpaceLine);
        }
    }

    private void eraseWall(Wall wall) {
        if(wall.isVert()) {
            IntPoint leftSpace = wall.getP1();
            IntPoint rightSpace = wall.getP2();
            if(rightSpace.getX() < leftSpace.getX()) {
                rightSpace = wall.getP1();
                leftSpace = wall.getP2();
            }
            //remove the east wall in leftSpace
            List<Node> leftSpaceNodeList = getNodeListAt(leftSpace.getX(), leftSpace.getY());
            removeNodeById(leftSpaceNodeList, EAST_WALL_ID);
            //remove the west wall rightSpace
            List<Node> rightSpaceNodeList = getNodeListAt(rightSpace.getX(), rightSpace.getY());
            removeNodeById(rightSpaceNodeList, WEST_WALL_ID);
        } else {
            //wall is horizontal
            IntPoint upperSpace = wall.getP1();
            IntPoint lowerSpace = wall.getP2();
            if(upperSpace.getY() < lowerSpace.getY()) {
                lowerSpace = wall.getP1();
                upperSpace = wall.getP2();
            }
            //remove the south wall from upperSpace
            List<Node> upperSpaceNodeList = getNodeListAt(upperSpace.getX(), upperSpace.getY());
            removeNodeById(upperSpaceNodeList, SOUTH_WALL_ID);
            //remove the north wall from lowerSpace
            List<Node> lowerSpaceNodeList = getNodeListAt(lowerSpace.getX(), lowerSpace.getY());
            removeNodeById(lowerSpaceNodeList, NORTH_WALL_ID);
        }
    }

    private void removeNodeById(List<Node> nodeList, String id) {
        for(int i = 0; i < nodeList.size(); i++) {
            Node node = nodeList.get(i);
            if(node.getId() != null && node.getId().equals(id)) {
                nodeList.remove(node);
                break;
            }
        }
    }

    public StackPane getPaneAt(int x, int y) {
        for(Node n: grid.getChildren()) {
            if(GridPane.getColumnIndex(n) == x && GridPane.getRowIndex(n) == getRow(y) &&
                    n instanceof StackPane) {
                return (StackPane)n;
            }
        }
        return null;
    }

    public List<Node> getNodeListAt(int x, int y) {
        return getPaneAt(x, y).getChildren();
    }

    private int getNumWallsAt(IntPoint point) {
        int count = 0;
        for(Wall wall: getWalls()) {
            if(wall.contains(point.getX(), point.getY())) {
                count++;
            }
        }
        return count;
    }

    public int getNumWallsAt(int x, int y) {
        int count = 0;
        for(Wall wall: getWalls()) {
            if(wall.contains(x, y)) {
                count++;
            }
        }
        return count;
    }

    private void fillInWallCorners() {
        //get list of points with walls
        List<IntPoint> wallPointList = new ArrayList<IntPoint>() {
            @Override
            public boolean contains(Object o) {
                for(IntPoint point: this) {
                    if(point.equals(o)) return true;
                }
                return false;
            }
        };
        for(Wall wall: getWalls()) {
            if(!wallPointList.contains(wall.getP1())) wallPointList.add(wall.getP1());
            if(!wallPointList.contains(wall.getP2())) wallPointList.add(wall.getP2());
        }
        //get list of points with corners
        List<IntPoint> cornerList = new ArrayList<>();
        for(IntPoint wallPoint: wallPointList) {
            if(getNumWallsAt(wallPoint) > 1) cornerList.add(wallPoint);
        }

        //determine which corners need to be filled in for each corner space
        for(IntPoint space: cornerList) {
            if(isWallNorth(space)) {
                if(isWallEast(space)) drawCorner(space.getX() + 1, space.getY() + 1, Corner.BOTTOM_LEFT);
                if(isWallWest(space)) drawCorner(space.getX() - 1, space.getY() + 1, Corner.BOTTOM_RIGHT);
            }
            if(isWallSouth(space)) {
                if(isWallEast(space)) drawCorner(space.getX() + 1, space.getY() - 1, Corner.TOP_LEFT);
                if(isWallWest(space)) drawCorner(space.getX() - 1, space.getY() - 1, Corner.TOP_RIGHT);
            }
        }
    }

    public void fillInCornersIfNecessary(Wall wall) {
        //there are 4 possibilities for corners that could need to be filled in - the spaces on either side
        //of the 2 spaces that define the wall
        if(wall.isHoriz()) {
            //potential corner spaces are to the left and right
            IntPoint upperPoint = wall.getP1();
            IntPoint lowerPoint = wall.getP2();
            if(wall.getP1().getY() < wall.getP2().getY()) {
                upperPoint = wall.getP2();
                lowerPoint = wall.getP1();
            }
            //check to the left of upperPoint and lowerPoint
            if(!isOutOfBounds(upperPoint.getX() - 1, upperPoint.getY())) {
                if(isWallWest(lowerPoint)) drawCorner(upperPoint.getX() - 1, upperPoint.getY(),
                        Corner.BOTTOM_RIGHT);
                if(isWallWest(upperPoint)) drawCorner(lowerPoint.getX() - 1, lowerPoint.getY(),
                        Corner.TOP_RIGHT);
            }
            //check to the right of upperPoint and lowerPoint
            if(!isOutOfBounds(upperPoint.getX() + 1, upperPoint.getY())) {
                if(isWallEast(lowerPoint)) drawCorner(upperPoint.getX() + 1, upperPoint.getY(),
                        Corner.BOTTOM_LEFT);
                if(isWallEast(upperPoint)) drawCorner(lowerPoint.getX() + 1, lowerPoint.getY(),
                        Corner.TOP_LEFT);
            }
        } else {
            //wall is vertical
            //potential corner spaces are above and below
            IntPoint leftPoint = wall.getP1();
            IntPoint rightPoint = wall.getP2();
            if(rightPoint.getX() < leftPoint.getX()) {
                leftPoint = wall.getP2();
                rightPoint = wall.getP1();
            }
            //check above leftPoint and rightPoint
            if(!isOutOfBounds(leftPoint.getX(), leftPoint.getY() + 1)) {
                if(isWallNorth(rightPoint)) drawCorner(leftPoint.getX(), leftPoint.getY() + 1,
                        Corner.BOTTOM_RIGHT);
                if(isWallNorth(leftPoint)) drawCorner(rightPoint.getX(), rightPoint.getY() + 1,
                        Corner.BOTTOM_LEFT);
            }
            //check below leftPoint and rightPoint
            if(!isOutOfBounds(leftPoint.getX(), leftPoint.getY() - 1)) {
                if(isWallSouth(rightPoint)) drawCorner(leftPoint.getX(), leftPoint.getY() - 1,
                        Corner.TOP_RIGHT);
                if(isWallSouth(leftPoint)) drawCorner(rightPoint.getX(), rightPoint.getY() - 1,
                        Corner.TOP_LEFT);
            }
        }
    }

    private void eraseCornersIfNecessary(Wall wall) {
        //there are 4 possibilities for corners that could need to be erased - the spaces on either side
        //of the 2 spaces that define the wall
        if(wall.isHoriz()) {
            //potential corner spaces are to the left and right
            IntPoint upperPoint = wall.getP1();
            IntPoint lowerPoint = wall.getP2();
            if(wall.getP1().getY() < wall.getP2().getY()) {
                upperPoint = wall.getP2();
                lowerPoint = wall.getP1();
            }
            //check to the left of upperPoint and lowerPoint
            if(!isOutOfBounds(upperPoint.getX() - 1, upperPoint.getY())) {
                if(isWallWest(lowerPoint)) eraseCorner(upperPoint.getX() - 1, upperPoint.getY(),
                        Corner.BOTTOM_RIGHT);
                if(isWallWest(upperPoint)) eraseCorner(lowerPoint.getX() - 1, lowerPoint.getY(),
                        Corner.TOP_RIGHT);
            }
            //check to the right of upperPoint and lowerPoint
            if(!isOutOfBounds(upperPoint.getX() + 1, upperPoint.getY())) {
                if(isWallEast(lowerPoint)) eraseCorner(upperPoint.getX() + 1, upperPoint.getY(),
                        Corner.BOTTOM_LEFT);
                if(isWallEast(upperPoint)) eraseCorner(lowerPoint.getX() + 1, lowerPoint.getY(),
                        Corner.TOP_LEFT);
            }
        } else {
            //wall is vertical
            //potential corner spaces are above and below
            IntPoint leftPoint = wall.getP1();
            IntPoint rightPoint = wall.getP2();
            if(rightPoint.getX() < leftPoint.getX()) {
                leftPoint = wall.getP2();
                rightPoint = wall.getP1();
            }
            //check above leftPoint and rightPoint
            if(!isOutOfBounds(leftPoint.getX(), leftPoint.getY() + 1)) {
                if(isWallNorth(rightPoint)) eraseCorner(leftPoint.getX(), leftPoint.getY() + 1,
                        Corner.BOTTOM_RIGHT);
                if(isWallNorth(leftPoint)) eraseCorner(rightPoint.getX(), rightPoint.getY() + 1,
                        Corner.BOTTOM_LEFT);
            }
            //check below leftPoint and rightPoint
            if(!isOutOfBounds(leftPoint.getX(), leftPoint.getY() - 1)) {
                if(isWallSouth(rightPoint)) eraseCorner(leftPoint.getX(), leftPoint.getY() - 1,
                        Corner.TOP_RIGHT);
                if(isWallSouth(leftPoint)) eraseCorner(rightPoint.getX(), rightPoint.getY() - 1,
                        Corner.TOP_LEFT);
            }
        }
    }

    private void drawCorner(int x, int y, Corner corner) {
        Rectangle rect = new Rectangle(0, 0, getGridLineWidth() / 2, getGridLineWidth() / 2);
        rect.setStrokeWidth(0);
        rect.setStroke(Color.BLACK);
        rect.setFill(Color.BLACK);
        switch(corner) {
            case TOP_LEFT:
                StackPane.setAlignment(rect, Pos.TOP_LEFT);
                rect.setId(TOP_LEFT_ID);
                break;
            case TOP_RIGHT:
                StackPane.setAlignment(rect, Pos.TOP_RIGHT);
                rect.setId(TOP_RIGHT_ID);
                break;
            case BOTTOM_LEFT:
                StackPane.setAlignment(rect, Pos.BOTTOM_LEFT);
                rect.setId(BOTTOM_LEFT_ID);
                break;
            case BOTTOM_RIGHT:
                StackPane.setAlignment(rect, Pos.BOTTOM_RIGHT);
                rect.setId(BOTTOM_RIGHT_ID);
                break;
        }
        StackPane pane = getPaneAt(x, y);
        if(pane != null) pane.getChildren().add(rect);
    }

    private void eraseCorner(int x, int y, Corner corner) {
        String id = "";
        switch(corner) {
            case TOP_LEFT:
                id = TOP_LEFT_ID;
                break;
            case TOP_RIGHT:
                id = TOP_RIGHT_ID;
                break;
            case BOTTOM_LEFT:
                id = BOTTOM_LEFT_ID;
                break;
            case BOTTOM_RIGHT:
                id = BOTTOM_RIGHT_ID;
                break;
        }
        List<Node> nodeList = getNodeListAt(x, y);
        for(int i = 0; i < nodeList.size(); i++) {
            Node node = nodeList.get(i);
            if(node.getId() != null && node.getId().equals(id)) {
                nodeList.remove(node);
                break;
            }
        }
    }

    private ImageView getNumImage(char digit, DecPlace decPlace) {
        switch(digit) {
            case '0':
                return new ImageView(ZERO_URL);
            case '1':
                if(decPlace == DecPlace.TENS) return new ImageView(LEADING_ONE_URL);
                return new ImageView(TRAILING_ONE_URL);
            case '2':
                return new ImageView(TWO_URL);
            case '3':
                return new ImageView(THREE_URL);
            case '4':
                return new ImageView(FOUR_URL);
            case '5':
                return new ImageView(FIVE_URL);
            case '6':
                return new ImageView(SIX_URL);
            case '7':
                return new ImageView(SEVEN_URL);
            case '8':
                return new ImageView(EIGHT_URL);
            case '9':
                return new ImageView(NINE_URL);

        }
        return null;
    }

    private String wallsToString() {
        StringBuilder wallStr = new StringBuilder();
        for(Wall wall: getWalls()) {
            wallStr.append("[(").append(wall.getP1().getX()).append(",")
                    .append(wall.getP1().getY()).append(")(").append(wall.getP2().getX())
                    .append(",").append(wall.getP2().getY()).append(")]");
        }
        return wallStr.toString();
    }

    private String haikusToString() {
        StringBuilder haikuStr = new StringBuilder();
        for(int i = 0; i < getHaikus().length; i++) {
            for(int j = 0; j < getHaikus()[0].length; j++) {
                int numHaikus = getHaikus()[i][j];
                if(numHaikus > 0) haikuStr.append("(").append(i).append(",")
                        .append(j).append(")");
                if(numHaikus > 1) haikuStr.append("x").append(numHaikus);
            }
        }
        return haikuStr.toString();
    }

    private String goalToString() {
        return getGoal().toString();
    }

    public void checkGoal() {
        if(getGoal().isEmpty()) return;
        for(Digipuff digipuff : r2List()) {
            if(getGoal().getX() == null || digipuff.getX() == getGoal().getX()
            && getGoal().getY() == null || digipuff.getY() == getGoal().getY()
            && getGoal().getDirection() == null || getGoal().getDirection() == digipuff.getDirection()
            && goalNumHaikusSuccess(digipuff)
            && goalNumMovesSuccess(digipuff))
                successExitDialog("You did it!");
        }
    }

    private boolean goalNumHaikusSuccess(Digipuff digipuff) {
        if(getGoal().getNumHaikusSym() == null) return true;
        return goalSymbolFieldSuccess(digipuff.getNumHaikus(), getGoal().getNumHaikusSym(),
                getGoal().getNumHaikus());
    }

    private boolean goalNumMovesSuccess(Digipuff digipuff) {
        if(getGoal().getNumMovesSym() == null) return true;
        return goalSymbolFieldSuccess(digipuff.getTotalMoves(), getGoal().getNumMovesSym(),
                getGoal().getNumMoves());
    }

    private boolean goalSymbolFieldSuccess(int r2Amt, CompSym symbol, int goalAmt) {
        switch(symbol) {
            case EQUAL_TO:
                return r2Amt == goalAmt;
            case LESS_THAN:
                return r2Amt < goalAmt;
            case GREATER_THAN:
                return r2Amt > goalAmt;
            case LESS_THAN_OR_EQUAL_TO:
                return r2Amt <= goalAmt;
            case GREATER_THAN_OR_EQUAL_TO:
                return r2Amt >= goalAmt;
        }
        throw new RuntimeException("Comparator symbol in goal not recognized.");
    }

    public static void successExitDialog(String message) {
        Ddialog dialog = new Ddialog(DialogType.INFO, Hub.getMainStage(), message);
        dialog.setFontSize(Hub.DIALOG_FONT_SIZE);
        dialog.setOKButtonText("I am awesome");
        dialog.setOKEvent(e -> System.exit(0));
        dialog.setOnExit(e -> System.exit(0));
        dialog.show();
    }

    private class WorldObjectList {

        public ArrayList<WorldObject> list = new ArrayList<>();

    } //END OF WorldObjectList CLASS

    private class HaikuPoint {

        private IntPoint intPoint;
        private int multiplicity;

        public HaikuPoint(IntPoint intPoint, int multiplicity) {
            this.intPoint = intPoint;
            this.multiplicity = multiplicity;
        }

        public IntPoint getIntPoint() {
            return intPoint;
        }

        public int getMultiplicity() {
            return multiplicity;
        }

    } //END OF HaikuPoint CLASS

    private class InitState {
        public WorldObjectList[][] map;
        public int[][] haikus;
    }

} //END OF World CLASS
