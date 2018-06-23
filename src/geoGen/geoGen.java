package geoGen;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.controlsfx.control.RangeSlider;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class geoGen extends Application implements EventHandler<ActionEvent> {
    private double WIN_HEIGHT = 600.0;
    private double WIN_WDT = 600.0;
    private double GEN_HEIGHT = 500.0;
    private double GEN_WDT = 300.0;
    private int ITERATION_CONST = 8;
    private int ITERATION_BUFFER = 3;
    private double strokeWidth;
    private Color strokeColor, bgColor;
    private double xScalar, yScalar;

    private Line[] lineArr;
    private int numLines;
    private int counter;
    private double xOut;
    private double yOut;
    private String datePattern;

    private Random rand;
    private Pane genPane, wallPaperPane;
    private BorderPane primaryPane;
    private GridPane grid;
    private Button saveButton, genButton, advButton, wallPaperButton;
    private RangeSlider iterationSlider;
    private Label iterLab;
    private Scene currScene, scene;
    private Stage stage;
    private String savePath;

    /* Settings Elements */
    private RadioButton prevSel1, prevSel2;
    private GridPane settingsPane;
    private Scene settingsScene;
    private Button exitButton = new Button("Exit Settings");
    private Button chooserButton = new Button("Choose Save Directory");
    private ToggleGroup lineTGroup = new ToggleGroup();
    private ToggleGroup bgTGroup = new ToggleGroup();
    private RadioButton red = new RadioButton("Red");
    private RadioButton green = new RadioButton("Green");
    private RadioButton blue = new RadioButton("Blue");
    private RadioButton black = new RadioButton("Black");
    private RadioButton white = new RadioButton("White");
    private RadioButton darkGray = new RadioButton("Dark Gray");
    private RadioButton red2 = new RadioButton("Red");
    private RadioButton green2 = new RadioButton("Green");
    private RadioButton blue2 = new RadioButton("Blue");
    private RadioButton black2 = new RadioButton("Black");
    private RadioButton white2 = new RadioButton("White");
    private RadioButton darkGray2 = new RadioButton("Dark Gray");
    private Label lineColors = new Label("Line Color:");
    private Label bgColors = new Label("Background Color:");
    private Label currDir;
    /* -------------- */

    /**
     * Calculates the distance between two given points
     * @param startX starting x coordinate
     * @param startY starting y coordinate
     * @param endX ending x coordinate
     * @param endY ending y coordinate
     * @return the distance between the two points
     */
    private double pointDist(double startX, double startY, double endX, double endY) {
        return Math.sqrt(((endY - startY)*(endY - startY)) + ((endX - startX) * (endX - startX)));
    }

    /**
     * finds the intersection point if it exists in quarter of canvas
     * @param line1 input line 1
     * @param line2 input line 2
     * @return true if point found in quarter of canvas, false if not
     */
    private Boolean foundIntersection(Line line1, Line line2) {
        double slope1, slope2, b1, b2, interX, interY, yDiff, xDiff;

        yDiff = line1.getEndY() - line1.getStartY();
        xDiff = line1.getEndX() - line1.getStartX();
        slope1 = yDiff/xDiff;
        yDiff = line2.getEndY() - line2.getStartY();
        xDiff = line2.getEndX() - line2.getStartX();
        slope2 = yDiff/xDiff;

        if (slope1 == slope2) {
            return false;
        }

        b1 = line1.getStartY() - (slope1 * line1.getStartX());
        b2 = line2.getStartY() - (slope2 * line2.getStartX());

        interX = (b2 - b1)/(slope1 - slope2);
        interY = (slope1 * interX) + b1;

        if ((interX <= line2.getEndX()) && (interX > 0) && (interY > 0)) {
            xOut = interX;
            yOut = interY;
            return true;
        }

        xOut = -1;
        yOut = -1;

        return false;
    }

    /**
     *
     * @param inPoints array of input points, [0] = startX, [1] = startY, [2] = endX, [3] = endY
     * @param paneWidth width of pane, to determine mirror points
     * @param paneHeight height of pane, to determine mirror points
     * @return array of doubles, [0] = mirror startX1, [1] = mirror startY1, [2] = mirror endX1, [3] = mirror endY1, etc
     */
    private double[] genMirrorPoints(double[] inPoints, double paneWidth, double paneHeight) {
        double[] outpoints = new double[12];

        outpoints[0] = paneWidth - inPoints[0];
        outpoints[1] = inPoints[1];
        outpoints[2] = paneWidth - inPoints[2];
        outpoints[3] = inPoints[3];

        outpoints[4] = paneWidth - inPoints[0];
        outpoints[5] = paneHeight - inPoints[1];
        outpoints[6] = paneWidth - inPoints[2];
        outpoints[7] = paneHeight - inPoints[3];

        outpoints[8] = inPoints[0];
        outpoints[9] = paneHeight - inPoints[1];
        outpoints[10] = inPoints[2];
        outpoints[11] = paneHeight - inPoints[3];

        return outpoints;
    }

    private void constructLines(Line[] inLines, double[] ptArr, double widthOverHeight) {
        inLines[0] = new Line(ptArr[0], ptArr[1], ptArr[2], ptArr[3]);
        inLines[0].setStrokeWidth(strokeWidth * widthOverHeight);
        inLines[0].setStroke(strokeColor);

        inLines[1] = new Line(ptArr[4], ptArr[5], ptArr[6], ptArr[7]);
        inLines[1].setStrokeWidth(strokeWidth * widthOverHeight);
        inLines[1].setStroke(strokeColor);

        inLines[2] = new Line(ptArr[8], ptArr[9], ptArr[10], ptArr[11]);
        inLines[2].setStrokeWidth(strokeWidth * widthOverHeight);
        inLines[2].setStroke(strokeColor);
    }

    /**
     * Fills the input line array with a generated line and its mirrors
     * @param inLines input line array that will be filled with a generated line and the line's mirrors
     */
    private void genLineAndMirrors(Line[] inLines) {
        Line line;
        Line[] mLines = new Line[3];
        double[] ptArr = new double[4], ptArrM;

        ptArr[0] = 0;
        ptArr[1] = rand.nextDouble() * (GEN_HEIGHT /2);
        ptArr[2] = (GEN_WDT /2);
        ptArr[3] = rand.nextDouble() * (GEN_HEIGHT /2);

        line = new Line(ptArr[0], ptArr[1], ptArr[2], ptArr[3]);
        line.setStrokeWidth(strokeWidth);
        line.setStroke(strokeColor);
        lineArr[counter] = line;

        for (int i = 0; i < counter; i++) {
            if(foundIntersection(line, lineArr[i])) {
                if ((pointDist(ptArr[0], ptArr[1], ptArr[2], ptArr[3]) > pointDist(ptArr[0], ptArr[1], xOut, yOut)) && (xOut > -1)) {
                    ptArr[2] = xOut;
                    ptArr[3] = yOut;
                    line.setEndX(ptArr[2]);
                    line.setEndY(ptArr[3]);
                }
            }
        }

        this.counter += 1;
        ptArrM = genMirrorPoints(ptArr, GEN_WDT, GEN_HEIGHT);
        constructLines(mLines, ptArrM, 1);

        inLines[0] = line;
        for (int i = 0; i < 3; i++)
            inLines[i + 1] = mLines[i];
    }

    /**
     * draws the line, single line function to make code easier to read
     * @param inputLine the input line to draw
     */
    private void drawLine(Line inputLine) { genPane.getChildren().add(inputLine); }

    /**
     * Generates numLines amount of lines and their mirrors
     */
    private void genLines() {
        Line[] lineAndMirrors = new Line[4];

        for (int i = 0; i < numLines; i++) {
            genLineAndMirrors(lineAndMirrors);
            for (int j = 0; j < 4; j++) {
                drawLine(lineAndMirrors[j]);
            }
        }
    }

    /**
     * initializes the toolbar as an HBox, styles it and gives it events
     */
    private void initToolBar() {
        HBox topBar = new HBox();
        HBox titleBox = new HBox();
        HBox xBox = new HBox();

        Label title = new Label("Random Pattern Generator");
        Label xBtn = new Label("  close  ");

        titleBox.setAlignment(Pos.CENTER);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        xBox.setAlignment(Pos.CENTER);
        xBox.setPrefWidth(60);

        titleBox.getChildren().add(title);
        xBox.getChildren().add(xBtn);

        topBar.setStyle("-fx-padding: 3px; -fx-background-color: #373737; -fx-text-fill: white;");
        title.setStyle("-fx-font: 11pt \"Helvetica\"; -fx-text-fill: #c5c5c5");
        xBtn.setStyle("-fx-font: 11pt \"Helvetica\"; -fx-text-fill: #c5c5c5; -fx-padding: 1px; " +
                "-fx-border-color: #c5c5c5; -fx-border-radius: 2px");

        topBar.getChildren().addAll(titleBox, xBox);
        xBtn.setOnMouseClicked(e -> closeStage());

        class Point { private double xCoordinate, yCoordinate; }
        final Point point = new Point();

        topBar.setOnMousePressed(me -> {
            point.xCoordinate = stage.getX() - me.getScreenX();
            point.yCoordinate = stage.getY() - me.getScreenY();
        });
        topBar.setOnMouseDragged(me -> {
            stage.setX(me.getScreenX() + point.xCoordinate);
            stage.setY(me.getScreenY() + point.yCoordinate);
        });

        primaryPane.setTop(topBar);
    }

    /**
     * initializes member variables
     */
    private void initVars() {
        strokeWidth = 2.0;
        strokeColor = Color.WHITE;
        datePattern = "kk_mm_ss_MM-dd-yyyy";
        rand = new Random();
        numLines = rand.nextInt(ITERATION_BUFFER) + ITERATION_CONST;//8-10 iterations
        lineArr = new Line[numLines];
        counter = 0;
        savePath = System.getProperty("user.dir");
        xScalar = 1920.0D/500.0D;
        yScalar = 1080.0D/300.0D;
        bgColor = Color.web("#111111");
    }
    /**
     * initializes member buttons
     */
    private void initButtons() {
        genButton = new Button("Gen Lines");
        genButton.setOnAction(this);
        genButton.setId("custom-button");

        saveButton = new Button("Save");
        saveButton.setOnAction(this);
        saveButton.setId("custom-button");

        wallPaperButton = new Button("Save as Wallpaper (1920x1080)");
        wallPaperButton.setId("custom-button");
        wallPaperButton.setOnAction(this);

        advButton = new Button("Advanced settings");
        advButton.setId("adv-button");
        advButton.setOnAction(this);

    }

    /**
     * initializes member panes
     */
    private void initPanes() {
        genPane = new Pane();
        wallPaperPane = new Pane();

        primaryPane.setStyle("-fx-background-color: #0b0b0b;-fx-padding:0px;");
        primaryPane.setMinSize(WIN_WDT, WIN_HEIGHT);

        grid.setMinSize(GEN_WDT/2, WIN_HEIGHT);
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);

        genPane.setMaxSize(GEN_WDT, GEN_HEIGHT);
        genPane.setStyle("-fx-background-color: #111111;");

        wallPaperPane.setMinSize(1920, 1080);
        wallPaperPane.setStyle("-fx-background-color: #111111;");

        grid.add(genButton,0, 20);
        grid.add(saveButton, 0, 21);
        grid.add(wallPaperButton, 0, 24);
        grid.add(advButton, 0, 25);

        primaryPane.setLeft(grid);
        primaryPane.setCenter(genPane);
    }

    /**
     * initializes member sliders
     */
    private void initSliders() {
        iterationSlider = new RangeSlider(3, 18, 8, 10);
        iterationSlider.setBlockIncrement(1.0);
        iterationSlider.setMajorTickUnit(5);
        iterationSlider.setMinorTickCount(4);
        iterationSlider.setOrientation(Orientation.HORIZONTAL);
        iterationSlider.setSnapToTicks(true);
        iterationSlider.setShowTickLabels(true);
        iterationSlider.setShowTickMarks(true);
        grid.add(iterationSlider, 0,22);

        iterationSlider.highValueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                ITERATION_BUFFER = (int) (iterationSlider.getHighValue() - iterationSlider.getLowValue() + 1);
                ITERATION_CONST = (int) iterationSlider.getLowValue();
                String s = ITERATION_CONST +" - " + (ITERATION_CONST + ITERATION_BUFFER - 1) + " lines to generate";
                iterLab.setText(s);
                numLines = rand.nextInt(ITERATION_BUFFER) + ITERATION_CONST;
            }
        });

        iterationSlider.lowValueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                ITERATION_CONST = (int) iterationSlider.getLowValue();
                ITERATION_BUFFER = (int) iterationSlider.getHighValue() - ITERATION_CONST + 1;
                String s = ITERATION_CONST + " - " + (int) iterationSlider.getHighValue();
                s = s + " lines to generate";
                iterLab.setText(s);
                numLines = rand.nextInt(ITERATION_BUFFER) + ITERATION_CONST;
            }
        });
    }

    /**
     * initializes primary pane's labels
     */
    private void initLabels() {
        iterLab = new Label();
        iterLab.setText(ITERATION_CONST + " - " + (ITERATION_BUFFER + ITERATION_CONST - 1) + " lines to generate");
        iterLab.setTextFill(Color.WHITE);
        grid.add(iterLab, 0,23);
    }

    /**
     * confirms if user wants to close the program
     */
    private void closeStage() {
        FlowPane exitPane = new FlowPane(10, 10);
        exitPane.setAlignment(Pos.CENTER);
        exitPane.setPadding(new Insets(180, 250, 200, 250));
        exitPane.setStyle("-fx-background-color: #0b0b0b;");

        Button yes = new Button("Close Window");
        Button no = new Button("Do Not Close Window");
        yes.setId("custom-button");
        no.setId("custom-button");
        exitPane.getChildren().addAll(yes, no);

        Scene exitScene = new Scene(exitPane, WIN_WDT, WIN_HEIGHT);
        stage.setScene(exitScene);

        exitScene.getStylesheets().add(geoGen.class.getResource("skin.css").toExternalForm());

        yes.setOnAction(e -> Platform.exit());
        no.setOnAction(e -> stage.setScene(currScene));
    }

    /**
     * creates button events for settings pane's buttons
     */
    private void createButtonEvents() {
        chooserButton.setOnAction(a -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Save to...");
            File defaultDirectory = new File(".");
            chooser.setInitialDirectory(defaultDirectory);
            File selectedDirectory = chooser.showDialog(stage);
            if (selectedDirectory == null){
                currDir.setText("Saving to: " + savePath);
            } else {
                savePath = selectedDirectory.getAbsolutePath();
                currDir.setText("Saving to: " + savePath);
            }
        });

        exitButton.setOnAction(b -> {
            currScene = scene;
            stage.setScene(scene);
        });
    }

    /**
     * adds radios and labels to settings pane
     */
    private void addElementsToSettingsPane() {
        settingsPane.add(lineColors, 0 ,0);
        settingsPane.add(red, 0, 1);
        settingsPane.add(green, 0, 2);
        settingsPane.add(blue, 0, 3);
        settingsPane.add(black, 0, 4);
        settingsPane.add(white, 0, 5);
        settingsPane.add(darkGray, 0, 6);
        settingsPane.add(exitButton, 0, 12);

        settingsPane.add(bgColors, 1 ,0);
        settingsPane.add(red2, 1, 1);
        settingsPane.add(green2, 1, 2);
        settingsPane.add(blue2, 1, 3);
        settingsPane.add(black2, 1, 4);
        settingsPane.add(white2, 1, 5);
        settingsPane.add(darkGray2, 1, 6);
    }

    /**
     * styles radio buttons for choosing line colors
     */
    private void styleRadioElements() {
        red.setToggleGroup(lineTGroup);
        red.setId("small-text");
        green.setToggleGroup(lineTGroup);
        green.setId("small-text");
        blue.setToggleGroup(lineTGroup);
        blue.setId("small-text");
        black.setToggleGroup(lineTGroup);
        black.setId("small-text");
        white.setToggleGroup(lineTGroup);
        white.setId("small-text");
        darkGray.setToggleGroup(lineTGroup);
        darkGray.setId("small-text");

        red2.setToggleGroup(bgTGroup);
        red2.setId("small-text");
        green2.setToggleGroup(bgTGroup);
        green2.setId("small-text");
        blue2.setToggleGroup(bgTGroup);
        blue2.setId("small-text");
        black2.setToggleGroup(bgTGroup);
        black2.setId("small-text");
        white2.setToggleGroup(bgTGroup);
        white2.setId("small-text");
        darkGray2.setToggleGroup(bgTGroup);
        darkGray2.setId("small-text");
    }

    /**
     * styles radio buttons for choosing background color
     */
    private void styleLabelElements() {
        lineColors.setId("list-header");
        bgColors.setId("list-header");
        currDir.setId("small-text");
        GridPane.setConstraints(bgColors, 1, 0, 2, 1);
        exitButton.setId("adv-button");
        chooserButton.setId("custom-button");
        settingsPane.add(chooserButton, 0, 7);
        GridPane.setConstraints(chooserButton,0, 7, 2, 1);
        settingsPane.add(currDir, 0, 8);
        GridPane.setConstraints(currDir, 0, 8, 3, 1 );
    }

    /**
     * styles and groups all settings pane elements, uses two helper methods above
     */
    private void styleAndGroupElements() {
        styleRadioElements();
        styleLabelElements();
    }

    /**
     * gives events to the radio buttons for choosing line colors
     */
    private void createRadioEvents1() {
        red.setOnMousePressed(me -> {
            Line[] lines = new Line[genPane.getChildren().size()];
            genPane.getChildren().toArray(lines);
            for (Line l:lines)
                l.setStroke(Color.RED);
            lines = new Line[wallPaperPane.getChildren().size()];
            wallPaperPane.getChildren().toArray(lines);
            for (Line l:lines)
                l.setStroke(Color.RED);
            strokeColor = Color.RED;
            red.setSelected(true);
            prevSel1.setSelected(false);
            prevSel1 = red;
        });
        green.setOnMousePressed(me -> {
            Line[] lines = new Line[genPane.getChildren().size()];
            genPane.getChildren().toArray(lines);
            for (Line l:lines)
                l.setStroke(Color.SPRINGGREEN);
            lines = new Line[wallPaperPane.getChildren().size()];
            wallPaperPane.getChildren().toArray(lines);
            for (Line l:lines)
                l.setStroke(Color.SPRINGGREEN);
            strokeColor = Color.SPRINGGREEN;
            green.setSelected(true);
            prevSel1.setSelected(false);
            prevSel1 = green;
        });
        blue.setOnMousePressed(me -> {
            Line[] lines = new Line[genPane.getChildren().size()];
            genPane.getChildren().toArray(lines);
            for (Line l:lines)
                l.setStroke(Color.STEELBLUE);
            lines = new Line[wallPaperPane.getChildren().size()];
            wallPaperPane.getChildren().toArray(lines);
            for (Line l:lines)
                l.setStroke(Color.STEELBLUE);
            strokeColor = Color.STEELBLUE;
            blue.setSelected(true);
            prevSel1.setSelected(false);
            prevSel1 = blue;
        });
        black.setOnMousePressed(me -> {
            Line[] lines = new Line[genPane.getChildren().size()];
            genPane.getChildren().toArray(lines);
            for (Line l:lines)
                l.setStroke(Color.BLACK);
            lines = new Line[wallPaperPane.getChildren().size()];
            wallPaperPane.getChildren().toArray(lines);
            for (Line l:lines)
                l.setStroke(Color.BLACK);
            strokeColor = Color.BLACK;
            black.setSelected(true);
            prevSel1.setSelected(false);
            prevSel1 = black;
        });
        white.setOnMousePressed(me -> {
            Line[] lines = new Line[genPane.getChildren().size()];
            genPane.getChildren().toArray(lines);
            for (Line l:lines)
                l.setStroke(Color.WHITE);
            lines = new Line[wallPaperPane.getChildren().size()];
            wallPaperPane.getChildren().toArray(lines);
            for (Line l:lines)
                l.setStroke(Color.WHITE);
            strokeColor = Color.WHITE;
            white.setSelected(true);
            prevSel1.setSelected(false);
            prevSel1 = white;
        });
        darkGray.setOnMousePressed(me -> {
            Line[] lines = new Line[genPane.getChildren().size()];
            genPane.getChildren().toArray(lines);
            for (Line l:lines)
                l.setStroke(Color.web("#111111"));
            lines = new Line[wallPaperPane.getChildren().size()];
            wallPaperPane.getChildren().toArray(lines);
            for (Line l:lines)
                l.setStroke(Color.web("#111111"));
            strokeColor = Color.web("#111111");
            darkGray.setSelected(true);
            prevSel1.setSelected(false);
            prevSel1 = darkGray;
        });
    }

    /**
     * gives events to the radio buttons for choosing background color
     */
    private void createRadioEvents2() {
        red2.setOnMousePressed(me -> {
            genPane.setStyle("-fx-background-color: red");
            bgColor = Color.RED;
            red2.setSelected(true);
            prevSel2.setSelected(false);
            prevSel2 = red2;
        });
        green2.setOnMousePressed(me -> {
            genPane.setStyle("-fx-background-color: springgreen");
            bgColor = Color.SPRINGGREEN;
            prevSel2.setSelected(false);
            green2.setSelected(true);
            prevSel2 = green2;
        });
        blue2.setOnMousePressed(me -> {
            genPane.setStyle("-fx-background-color: steelblue");
            bgColor = Color.STEELBLUE;
            blue2.setSelected(true);
            prevSel2.setSelected(false);
            prevSel2 = blue2;
        });
        black2.setOnMousePressed(me -> {
            genPane.setStyle("-fx-background-color: black");
            bgColor = Color.BLACK;
            black2.setSelected(true);
            prevSel2.setSelected(false);
            prevSel2 = black2;
        });
        white2.setOnMousePressed(me -> {
            genPane.setStyle("-fx-background-color: white");
            bgColor = Color.WHITE;
            white2.setSelected(true);
            prevSel2.setSelected(false);
            prevSel2 = white2;
        });
        darkGray2.setOnMousePressed(me -> {
            genPane.setStyle("-fx-background-color: #111111");
            bgColor = Color.web("#111111");
            darkGray2.setSelected(true);
            prevSel2.setSelected(false);
            prevSel2 = darkGray2;
        });
    }

    /**
     * initializes settings pane
     */
    private void initSettingsPane() {
        currDir = new Label("Saving to: " + savePath);
        settingsPane = new GridPane();
        settingsScene = new Scene(settingsPane, WIN_WDT, WIN_HEIGHT);
        settingsScene.getStylesheets().add(geoGen.class.getResource("skin.css").toExternalForm());

        settingsPane.setVgap(5);
        settingsPane.setHgap(10);
        settingsPane.setAlignment(Pos.CENTER);
        settingsPane.setPadding(new Insets(0, 200, 0, 30));
        settingsPane.setStyle("-fx-background-color: #0b0b0b;");

        //creates 3 columns in settings pane, each 100px wide
        for (int i = 0; i < 3; i++) {
            ColumnConstraints column = new ColumnConstraints(100);
            settingsPane.getColumnConstraints().add(column);
        }

        createButtonEvents();
        addElementsToSettingsPane();
        styleAndGroupElements();
        createRadioEvents1();
        createRadioEvents2();

        prevSel2 = darkGray2;
        prevSel1 = white;
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        stage.initStyle(StageStyle.UNDECORATED);
        grid = new GridPane();
        primaryPane = new BorderPane();

        initToolBar();
        initVars();
        initButtons();
        initPanes();
        initSliders();
        initLabels();
        initSettingsPane();

        scene = new Scene(primaryPane, WIN_WDT, WIN_HEIGHT);
        currScene = scene;
        scene.getStylesheets().add(geoGen.class.getResource("skin.css").toExternalForm());

        stage.setTitle("TEST");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * shows settings pane and changes relevant member variables
     */
    private void showSettings() {
        prevSel1.setSelected(true);
        prevSel2.setSelected(true);
        currScene = settingsScene;
        stage.setScene(settingsScene);
    }

    /**
     *
     * @param inPane
     */
    private void savePane(Pane inPane) {
        File file;
        Date date = new Date();
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(bgColor);
        WritableImage snapshot = inPane.snapshot(params, null);
        SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
        String dateStr = sdf.format(date);
        if (inPane == wallPaperPane) {
            file = new File(savePath + "/" + dateStr + "_" + "1920x1080" + ".png");
        }
        else {
            file = new File(savePath + "/" + dateStr + ".png");
        }
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", file);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void handle(ActionEvent event) {
        if (event.getSource() == genButton) {
            genPane.getChildren().clear();
            lineArr = new Line[numLines];
            counter = 0;

            numLines = rand.nextInt(ITERATION_BUFFER) + ITERATION_CONST;
            lineArr = new Line[numLines];
            genLines();

        } else if (event.getSource() == saveButton) {
            savePane(genPane);
        } else if (event.getSource() == advButton) {
            showSettings();
        } else if (event.getSource() == wallPaperButton) {
            saveAsWallPaper(1920, 1080);
        }
    }

    /**
     *
     * @param wallWidth passed wallpaper width desired
     * @param wallHeight passed wallpaper height desired
     */
    private void saveAsWallPaper(int wallWidth, int wallHeight) {
        Line wLine;
        Line[] mLines = new Line[3];
        double[] ptArr = new double[4], ptArrM;
        wallPaperPane.setStyle(genPane.getStyle());

        for (int i = 0; i < lineArr.length; i++) {
            ptArr[0] = lineArr[i].getStartY() * xScalar;
            ptArr[1] = 0;
            ptArr[2] = lineArr[i].getEndY() * xScalar;
            ptArr[3] = lineArr[i].getEndX() * yScalar;

            ptArrM = genMirrorPoints(ptArr, wallWidth, wallHeight);
            constructLines(mLines, ptArrM, (double) wallWidth/wallHeight);

            wLine = new Line(ptArr[0], ptArr[1], ptArr[2], ptArr[3]);
            wLine.setStroke(strokeColor);
            wLine.setStrokeWidth(2.0 * wallWidth / wallHeight);

            wallPaperPane.getChildren().add(wLine);
            wallPaperPane.getChildren().add(mLines[0]);
            wallPaperPane.getChildren().add(mLines[1]);
            wallPaperPane.getChildren().add(mLines[2]);
        }

        savePane(wallPaperPane);
    }

    public static void main(String[] args) {
        launch(args);
    }

}
