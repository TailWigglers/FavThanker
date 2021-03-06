package net.tailwigglers.favthanker.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import net.tailwigglers.favthanker.util.Constants;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.logging.Level;

public class View extends Application implements ChangeListener<Boolean> {

    private Model model;
    private Controller controller;

    private Label userLabel;

    private Button startButton;
    private Button stopButton;
    private Button selectUserButton;

    private ProgressBar progressBar;
    private Label progressLabel;

    private TextArea textArea;
    private Label faStatusLabel;

    private Region veil;
    private Stage stage;

    private boolean verifiedLogin = false;
    private boolean faOnline = false;

    @Override
    public void init() {
        java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);
        model = new Model();
        controller = new Controller(model, this);
    }

    @Override
    public void start(Stage primaryStage) {
        // Set up GUI components

        userLabel = new Label(Constants.SELECT_USER_PROMPT);
        userLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        userLabel.setAlignment(Pos.CENTER);
        userLabel.setStyle("-fx-font-size: 12pt;");

        startButton = new Button(Constants.START);
        startButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        startButton.setDisable(true);
        startButton.focusedProperty().addListener(this);
        startButton.addEventHandler(ActionEvent.ANY, controller);

        stopButton = new Button(Constants.STOP);
        stopButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        stopButton.setDisable(true);
        stopButton.focusedProperty().addListener(this);
        stopButton.addEventHandler(ActionEvent.ANY, controller);

        selectUserButton = new Button(Constants.SELECT_USER);
        selectUserButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        selectUserButton.focusedProperty().addListener(this);
        selectUserButton.addEventHandler(ActionEvent.ANY, controller);

        progressBar = new ProgressBar();
        progressBar.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        progressLabel = new Label(Constants.STOPPED);
        progressLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        progressLabel.setAlignment(Pos.CENTER);

        textArea = new TextArea();
        textArea.textProperty().addListener((observable, oldValue, newValue) -> textArea.setScrollTop(Double.MAX_VALUE));
        textArea.setEditable(false);
        textArea.focusedProperty().addListener(this);

        Label faStatusPrefixLabel = new Label(Constants.FA_STATUS);
        faStatusPrefixLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        faStatusPrefixLabel.setAlignment(Pos.CENTER_LEFT);

        faStatusLabel = new Label(Constants.FA_OFFLINE);
        faStatusLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        faStatusLabel.setAlignment(Pos.CENTER_LEFT);
        faStatusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");

        HBox faStatusBox = new HBox(faStatusPrefixLabel, faStatusLabel);

        Label copyrightLabel = new Label(Constants.COPYRIGHT);
        copyrightLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        copyrightLabel.setAlignment(Pos.CENTER);

        Image logoImage = new Image(Constants.LOGO_FILEPATH);
        ImageView logoImageView = new ImageView();
        logoImageView.setFitWidth(20);
        logoImageView.setFitHeight(20);
        logoImageView.setImage(logoImage);

        Label versionLabel = new Label(Constants.VERSION);
        versionLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        versionLabel.setAlignment(Pos.CENTER_RIGHT);

        // Create grid layout
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 25, 5, 25));

        int numCols = 3;
        for (int i = 0; i < numCols; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(100.0 / (double) numCols);
            grid.getColumnConstraints().add(col);
        }

        RowConstraints row0 = new RowConstraints();
        RowConstraints row1 = new RowConstraints();
        RowConstraints row2 = new RowConstraints();
        RowConstraints row3 = new RowConstraints();
        RowConstraints row4 = new RowConstraints();
        RowConstraints row5 = new RowConstraints();

        row0.setPercentHeight(10.0);
        row1.setPercentHeight(10.0);
        row2.setPercentHeight(10.0);
        row3.setPercentHeight(30.0);
        row4.setPercentHeight(30.0);
        row5.setPercentHeight(10.0);

        grid.getRowConstraints().addAll(row0, row1, row2, row3, row4, row5);

        // Add GUI components to grid

        grid.add(logoImageView, 1, 5);
        GridPane.setHalignment(logoImageView, HPos.RIGHT);
        GridPane.setMargin(logoImageView, new Insets(0,15,0,0) );

        grid.add(userLabel, 0, 0, 3, 1);
        GridPane.setFillWidth(userLabel, true);
        GridPane.setFillHeight(userLabel, true);

        grid.add(startButton, 0, 1);
        GridPane.setFillWidth(startButton, true);
        GridPane.setFillHeight(startButton, true);

        grid.add(stopButton, 1, 1);
        GridPane.setFillWidth(stopButton, true);
        GridPane.setFillHeight(stopButton, true);

        grid.add(selectUserButton, 2, 1);
        GridPane.setFillWidth(selectUserButton, true);
        GridPane.setFillHeight(selectUserButton, true);

        grid.add(progressBar, 0, 2, 2, 1);
        GridPane.setFillWidth(progressBar, true);
        GridPane.setFillHeight(progressBar, true);

        grid.add(progressLabel, 2, 2, 1, 1);
        GridPane.setFillWidth(progressLabel, true);
        GridPane.setFillHeight(progressLabel, true);

        grid.add(textArea, 0, 3, 3, 2);
        GridPane.setFillWidth(textArea, true);
        GridPane.setFillHeight(textArea, true);

        grid.add(faStatusBox,0,5);
        GridPane.setFillWidth(faStatusBox, true);
        GridPane.setFillHeight(faStatusBox, true);

        grid.add(copyrightLabel,1, 5);
        GridPane.setFillWidth(copyrightLabel, true);
        GridPane.setFillHeight(copyrightLabel, true);

        grid.add(versionLabel,2, 5);
        GridPane.setFillWidth(versionLabel, true);
        GridPane.setFillHeight(versionLabel, true);

        // Create veil to overlay GUI while loading
        veil = new Region();
        veil.setStyle("-fx-background-color: rgba(0, 0, 0, 0.3)");
        veil.setVisible(true);

        // Put grid and veil together in stack pane
        StackPane root = new StackPane();
        root.getChildren().addAll(grid, veil);

        // Create scene
        Scene scene = new Scene(root, 600, 300);

        // Set up stage
        stage = primaryStage;
        stage.setMinWidth(600);
        stage.setMinHeight(300);
        stage.setResizable(false);
        stage.setTitle(Constants.TITLE);
        stage.setScene(scene);
        stage.getIcons().add(logoImage);
        stage.show();

        // Login if previously logged in
        if (model.getFoundConfig() &&
                model.getProps().getProperty(Constants.USERNAME) != null &&
                !model.getProps().getProperty(Constants.USERNAME).equals("")) {
            String jsonFilename = model.getProps().getProperty(Constants.USERNAME) + ".json";
            controller.login(new File(jsonFilename), true);
        } else {
            veil.setVisible(false);
        }

        controller.checkFaOnline();
    }

    @Override
    public void stop() {
        try {
            model.setStopFlag(true);
            model.persist();
        } catch (Exception e) {
            createExceptionDialog(e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    void setStateInProgress() {
        // Update progress bar
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        progressLabel.setText(Constants.STARTING);

        // Update buttons
        startButton.setDisable(true);
        stopButton.setDisable(false);
        selectUserButton.setDisable(true);
    }

    public void setStateProgressError(Throwable e) {
        Platform.runLater(() -> {
            createExceptionDialog(e);

            startButton.setDisable(false);
            selectUserButton.setDisable(false);
            stopButton.setDisable(true);

            progressBar.progressProperty().unbind();
            progressBar.progressProperty().setValue(ProgressBar.INDETERMINATE_PROGRESS);
            progressLabel.setText(Constants.STOPPED);
        });
    }

    public void setStateProgressSuccess(final int favCount) {
        Platform.runLater(() -> {
            progressBar.progressProperty().unbind();
            startButton.setDisable(false);
            stopButton.setDisable(true);
            selectUserButton.setDisable(false);

            if (model.getStopFlag()) {
                progressLabel.setText(Constants.STOPPED);
            } else {
                progressLabel.setText(favCount + "/" + favCount);
            }
        });
    }

    public void setStateLoadJsonTaskError(final File userFile) {
        Platform.runLater(() -> {
            Alert alert = new Alert(
                    Alert.AlertType.ERROR,
                    "Could not load " +
                            userFile.getName() +
                            ". Make sure you have correctly created your user file!"
            );
            alert.showAndWait();
            veil.setVisible(false);
        });
    }

    public void setStateError(Throwable e) {
        Platform.runLater(() -> createExceptionDialog(e));
    }

    File openFile() {
        // Open file chooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open User File");
        fileChooser.setInitialDirectory(new File("."));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
        return fileChooser.showOpenDialog(stage);
    }

    public void updateProgress(final double current, final double max) {
        Platform.runLater(() -> progressLabel.setText((int) current + "/" + (int) max));
    }

    public void setVeilVisible(boolean visible) {
        Platform.runLater(() -> veil.setVisible(visible));
    }

    public void welcomeUser() {
        Platform.runLater(() -> {
            userLabel.setText("Welcome " + model.getUsername() + "!");
            veil.setVisible(false);
            verifiedLogin = true;

            if (faOnline) {
               startButton.setDisable(false);
            }
        });
    }

    public void print(String text) {
        Platform.runLater(() -> textArea.appendText(text + "\n"));
    }

    private void createExceptionDialog(Throwable e) {
        e.printStackTrace();

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("An error has occurred.");
        alert.setContentText(e.getMessage());
        alert.setResizable(true);
        alert.initOwner(stage);

        // Create expandable Exception.
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("The exception stacktrace was:");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        // Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent(expContent);

        alert.showAndWait();
        veil.setVisible(false);
    }

    public Optional<String[]> showAddCookiesDialog() {
        // Create the custom dialog.
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Add Cookies");
        dialog.setHeaderText("Enter FA cookies from your web browser.");
        dialog.setResizable(true);
        dialog.initOwner(stage);

        // Set the button types.
        ButtonType enterButtonType = new ButtonType("Enter", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(enterButtonType, ButtonType.CANCEL);

        // Create the captcha field
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField cookieAField = new TextField();
        cookieAField.setPromptText("Cookie A");

        TextField cookieBField = new TextField();
        cookieBField.setPromptText("Cookie B");

        grid.add(new Label("Cookie A:"), 0, 0);
        grid.add(cookieBField, 1, 0);
        grid.add(new Label("Cookie B:"), 0, 1);
        grid.add(cookieAField, 1, 1);

        // Enable/Disable enter button depending on whether a cookie was entered.
        Node enterButton = dialog.getDialogPane().lookupButton(enterButtonType);
        enterButton.setDisable(true);

        // Do some validation
        cookieAField.textProperty().addListener((observable, oldValue, newValue) ->
                enterButton.setDisable(newValue.trim().isEmpty()));

        dialog.getDialogPane().setContent(grid);

        // Convert the result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == enterButtonType) {
                String[] result = new String[2];
                result[0] = cookieBField.getText();
                result[1] = cookieAField.getText();
                return result;
            }
            return null;
        });

        return dialog.showAndWait();
    }

    Button getStartButton() {
        return startButton;
    }

    Button getStopButton() {
        return stopButton;
    }

    Button getSelectUserButton() {
        return selectUserButton;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    @Override
    public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {
        Platform.runLater(() -> userLabel.requestFocus());
    }

    public void setFaStatusLabel(boolean isOnline) {
        Platform.runLater(() -> {
            faOnline = isOnline;

            if (faOnline) {
                faStatusLabel.setText(Constants.FA_ONLINE);
                faStatusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                if (verifiedLogin) {
                    startButton.setDisable(false);
                }
            } else {
                faStatusLabel.setText(Constants.FA_OFFLINE);
                faStatusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                startButton.setDisable(true);
            }
        });
    }
}
