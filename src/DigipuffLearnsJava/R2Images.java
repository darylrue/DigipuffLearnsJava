package DigipuffLearnsJava;

public class R2Images implements R2Graphics {

    //IVARS
    private String eastImageUrl;
    private String crashImageUrl;

    //GETTERS
    @Override
    public String getEastImageUrl() {
        return eastImageUrl;
    }

    @Override
    public String getCrashImageUrl() {
        return crashImageUrl;
    }

    //SETTERS
    public void setEastImageUrl(String url) { eastImageUrl = url; }

    public void setCrashImageUrl(String url) { crashImageUrl = url; }

} //END OF CLASS
