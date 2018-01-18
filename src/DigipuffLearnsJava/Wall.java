package DigipuffLearnsJava;

public class Wall {

    //CONSTANTS

    //IVARS
    private IntPoint p1;
    private IntPoint p2;

    //CONSTRUCTORS
    public Wall(IntPoint p1, IntPoint p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    public Wall(int x1, int y1, int x2, int y2) {
        this.p1 = new IntPoint(x1, y1);
        this.p2 = new IntPoint(x2, y2);
    }

    //GETTERS
    public IntPoint getP1() {
        return p1;
    }

    public IntPoint getP2() {
        return p2;
    }

    public boolean contains(int x, int y) {
        if(getP1().getX() == x && getP1().getY() == y) return true;
        if(getP2().getX() == x && getP2().getY() == y) return true;
        return false;
    }

    public boolean isHoriz() { return p1.getX() == p2.getX(); }

    public boolean isVert() { return !isHoriz(); }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Wall)) return false;
        if(getP1().equals(((Wall)o).getP1()) && getP2().equals(((Wall)o).getP2()) ||
                getP2().equals(((Wall)o).getP1()) && getP1().equals(((Wall)o).getP2())) return true;
        return false;
    }

    //SETTERS
    public void setP1(IntPoint p1) {
        this.p1 = p1;
    }

    public void setP2(IntPoint p2) {
        this.p2 = p2;
    }

    //OTHER METHODS

}
