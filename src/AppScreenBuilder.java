import java.security.InvalidParameterException;

public class AppScreenBuilder {

    private AppScreen buildMainScreen(){
        AppScreen mainScreen = new AppScreenMain();
        return mainScreen;
    }

    private AppScreen buildSettingsScreen(){
        AppScreen settingsScreen = new AppScreenSettings();
        return settingsScreen;
    }

    private AppScreen buildExitScreen(){
        AppScreen exitScreen = new AppScreenExit();
        return exitScreen;
    }

    public AppScreen buildScreen(String type){
        switch (type) {
            case "main":
                return buildMainScreen();
            case "settings":
                return buildSettingsScreen();
            case "exit":
                return buildExitScreen();
            default:
                throw new InvalidParameterException("No AppScreen type found for: " + type);
        }
    }
}
