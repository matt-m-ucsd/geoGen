public class AppScreenSettings extends AppScreen {
    private String SavePath;

    public AppScreenSettings() {
        SavePath = System.getProperty("user.dir");
        buildScreen();
    }

    public void UpdateSavePath(String newPath) {
        SavePath = newPath;
    }

    private void buildScreen() {

    }

}
