package DigipuffLearnsJava;

import javafx.scene.image.ImageView;

public class Goal {

    //CONSTANTS
    private static final ImageView GOAL_IMG = new ImageView("images/goal.png");
    public static final String LOC_TAG = "Loc=";
    public static final String DIR_TAG = "Dir=";
    public static final String NUM_HAIKUS_TAG = "numHaikus=";
    public static final String NUM_MOVES_TAG = "Moves=";

    //IVARS
    private Integer x;
    private Integer y;
    private Dir direction;
    private CompSym numHaikusSym;
    private int numHaikus;
    private CompSym numMovesSym;
    private int numMoves;
    private ImageView image;
    private String nextLine = System.lineSeparator();

    //CONSTRUCTORS
    public Goal(){};

    public Goal(int x, int y) {
        this.x = x;
        this.y = y;
    }

    //GETTERS
    public Integer getX() {
        return x;
    }

    public Integer getY() {
        return y;
    }

    public Dir getDirection() { return direction; }

    public CompSym getNumMovesSym() { return numMovesSym; }

    public int getNumMoves() { return numMoves; }

    public CompSym getNumHaikusSym() { return numHaikusSym; }

    public int getNumHaikus() { return numHaikus; }

    public ImageView getImage() {
        if(image == null) {
            image = GOAL_IMG;
        }
        return image;
    }

    public boolean isEmpty() {
        return (getX() == null && getY() == null && getDirection() == null
            && getNumMovesSym() == null && getNumHaikusSym() == null);
    }

    //SETTERS
    public void setX(Integer x) {
        this.x = x;
    }

    public void setY(Integer y) {
        this.y = y;
    }

    public void setDirection(Dir direction) { this.direction = direction; }

    public void setNumMovesSym(CompSym symbol) { numMovesSym = symbol; }

    public void setNumMoves(int numMoves) { this.numMoves = numMoves; }

    public void setNumHaikusSym(CompSym symbol) { numHaikusSym = symbol; }

    public void setNumHaikus(int numHaikus) {
        this.numHaikus = numHaikus;
    }

    public void setImage(ImageView image) {
        this.image = image;
    }

    //OTHER METHODS
    @Override
    public String toString() {
        return LOC_TAG + locationStr()
                + DIR_TAG + directionStr()
                + NUM_HAIKUS_TAG + numHaikusStr()
                + NUM_MOVES_TAG + numMovesStr();
    }

    public String locationStr() {
        String xStr;
        String yStr;
        if(getX() == null) xStr = Hub.ANY;
        else xStr = getX().toString();
        if(getY() == null) yStr = Hub.ANY;
        else yStr = getY().toString();
        return "(" + xStr + "," + yStr + ")";
    }

    public String directionStr() {
        if(direction != null) {
            return direction.toString();
        }
        return Hub.ANY;
    }

    public String numHaikusSymStr() {
        if(numHaikusSym != null) return getNumHaikusSym().toString();
        return Hub.ANY;
    }

    public String numHaikusAmtStr() {
        if(numHaikusSym != null)
            return "" + getNumHaikus();
        return "";
    }

    public String numHaikusStr() {
        return numHaikusSymStr() + numHaikusAmtStr();
    }

    public String numMovesSymStr() {
        if(numMovesSym != null) return getNumMovesSym().toString();
        return Hub.ANY;
    }

    public String numMovesAmtStr() {
        if(numMovesSym != null) return "" + getNumMoves();
        return "";
    }

    public String numMovesStr() {
        return numMovesSymStr() + numMovesAmtStr();
    }

}
