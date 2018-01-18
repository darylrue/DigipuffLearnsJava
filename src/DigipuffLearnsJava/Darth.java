package DigipuffLearnsJava;


public class Darth extends R2 {

    //CONSTANTS
    private static final String EAST_IMAGE_URL = "images/darth-east.png";
    private static final String CRASH_IMAGE_URL = "images/darth-crash.png";

    //IVARS

    //CONSTRUCTORS
    public Darth(String name, int x, int y, Dir initDirection, int numFlashlights) {
        super(name, x, y, initDirection, numFlashlights);
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

}
