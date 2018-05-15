package DigipuffLearnsJava;

public class IntPoint {

    //IVARS
    private int x;
    private int y;

    //CONSTRUCTORS
    public IntPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    //GETTERS
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

} //END OF CLASS
