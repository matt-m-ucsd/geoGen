import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class GeoGen extends Application {

    private AppScreenBuilder appScreenBuilder = new AppScreenBuilder();
    private Group root = new Group();
    private AppScreen mainView = appScreenBuilder.buildScreen("main");
    private AppScreen settingsView = appScreenBuilder.buildScreen("settings");

    private void initRoot(){
        root.getChildren().add(mainView.getViewNode());
        root.getChildren().add(settingsView.getViewNode());
    }

    @Override
    public void start(Stage stage) {
        Scene primaryScene = new Scene(root, Constants.WIN_WDT, Constants.WIN_HEIGHT);
        primaryScene.getStylesheets().add(GeoGen.class.getResource("skin.css").toExternalForm());

        stage.setTitle("Random Pattern Generator");
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setScene(primaryScene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
