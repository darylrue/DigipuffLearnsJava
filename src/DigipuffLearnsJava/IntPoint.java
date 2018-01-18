package DigipuffLearnsJava;

public class IntPoint {

    //CONSTANTS

    //IVARS
    private int x;
    private int y;

    //CONSTRUCTORS

    /**
     Constructs an IntPoint instance
     @param x the x-coordinate
     @param y the y-coordinate
     */
    public IntPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    //GETTERS

    /**
     * Getter for the x-coordinate
     * @return the value of the x-coordinate
     */
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof IntPoint) return getX() == ((IntPoint)o).getX() && getY() == ((IntPoint)o).getY();
        return false;
    }

    //SETTERS

    //OTHER METHODS

} //END OF CLASS
