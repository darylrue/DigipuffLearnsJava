package DigipuffLearnsJava;

public class Fido extends Digipuff {

    //CONSTANTS
    private static final String EAST_IMAGE_URL = "images/fido.png";
    private static final String CRASH_IMAGE_URL = "images/fido.png";

    //IVARS

    //CONSTRUCTORS
    public Fido(String name, int x, int y, Dir initDirection, int numHaikus) {
        super(name, x, y, initDirection, numHaikus);
    }

    //GETTERS

    //SETTERS

    //OTHER METHODS
    @Override
    public String getEastImageUrl() {
        return EAST_IMAGE_URL;
    }

    @Override
    public String getCrashImageUrl() {
        return CRASH_IMAGE_URL;
    }

} //END OF CLASS
