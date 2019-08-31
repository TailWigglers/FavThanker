package com.seledrex.gui;

import com.seledrex.util.Constants;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

/**
 * View contains all methods and properties for the GUI.
 */
public class View extends Application {

    private Model model;
    private Controller controller;

    private Label userLabel;

    private Button startButton;
    private Button stopButton;
    private Button selectUserButton;

    private ProgressBar progressBar;
    private Label progressLabel;

    private TextArea textArea;

    private Region veil;
    private Stage stage;

    /**
     * Initializes the application with the model and controller.
     */
    @Override
    public void init() {
        model = new Model();
        controller = new Controller(model, this);
    }

    /**
     * Sets up the GUI components and registers with the controller.
     * @param primaryStage Stage.
     */
    @Override
    public void start(Stage primaryStage) {
        // Set up GUI components
        userLabel = new Label(Constants.SELECT_USER_PROMPT);
        userLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        userLabel.setAlignment(Pos.CENTER);

        startButton = new Button(Constants.START);
        startButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        startButton.setDisable(true);
        startButton.addEventHandler(ActionEvent.ANY, controller);

        stopButton = new Button(Constants.STOP);
        stopButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        stopButton.setDisable(true);
        stopButton.addEventHandler(ActionEvent.ANY, controller);

        selectUserButton = new Button(Constants.SELECT_USER);
        selectUserButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        selectUserButton.addEventHandler(ActionEvent.ANY, controller);

        progressBar = new ProgressBar();
        progressBar.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        progressLabel = new Label(Constants.STOPPED);
        progressLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        progressLabel.setAlignment(Pos.CENTER);

        textArea = new TextArea();
        textArea.textProperty().addListener((observable, oldValue, newValue) -> textArea.setScrollTop(Double.MAX_VALUE));
        textArea.setEditable(false);

        // Create grid layout
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        int numCols = 3;
        for (int i = 0; i < numCols; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(100.0 / (double) numCols);
            grid.getColumnConstraints().add(col);
        }

        RowConstraints row1 = new RowConstraints();
        RowConstraints row2 = new RowConstraints();
        RowConstraints row3 = new RowConstraints();
        RowConstraints row4 = new RowConstraints();
        RowConstraints row5 = new RowConstraints();

        row1.setPercentHeight(10.0);
        row2.setPercentHeight(10.0);
        row3.setPercentHeight(10.0);
        row4.setPercentHeight(35.0);
        row5.setPercentHeight(35.0);

        grid.getRowConstraints().addAll(row1, row2, row3, row4, row5);

        // Add GUI components to grid
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

        grid.add(textArea, 0, 3, 3, 3);
        GridPane.setFillWidth(textArea, true);
        GridPane.setFillHeight(textArea, true);

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
        stage.setTitle(Constants.TITLE);
        stage.setScene(scene);
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
    }

    /**
     * Handles closing the application.
     */
    @Override
    public void stop() {
        try {
            model.persist();
        } catch (Exception e) {
            createExceptionDialog(e);
        }
    }

    /**
     * Sets GUI to the in progress state when the faving task is in process.
     */
    void setStateInProgress() {
        // Update progress bar
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        progressLabel.setText(Constants.STARTING);

        // Update buttons
        startButton.setDisable(true);
        stopButton.setDisable(false);
        selectUserButton.setDisable(true);
    }

    /**
     * Sets GUI to the progress error state when the faving task fails.
     * @param e Exception.
     */
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

    /**
     * Sets GUI to the progress success state when the faving task succeeds.
     * @param favCount Total favs.
     */
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

    /**
     * Sets GUI to the load Json task error state when the load Json task fails.
     * @param userFile File attempted to be loaded.
     */
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

    /**
     * Creates a general error message.
     * @param e Exception.
     */
    public void setStateError(Throwable e) {
        Platform.runLater(() -> createExceptionDialog(e));
    }

    /**
     * Opens a file using a file chooser.
     * @return chosen file.
     */
    File openFile() {
        // Open file chooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open User File");
        fileChooser.setInitialDirectory(new File("."));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
        return fileChooser.showOpenDialog(stage);
    }

    /**
     * Updates the progress label with current progress.
     * @param current Current progress.
     * @param max Max progress.
     */
    public void updateProgress(final double current, final double max) {
        Platform.runLater(() -> progressLabel.setText((int) current + "/" + (int) max));
    }

    /**
     * Toggles veil visibility.
     * @param visible True for visible, false otherwise.
     */
    public void setVeilVisible(boolean visible) {
        Platform.runLater(() -> veil.setVisible(visible));
    }

    /**
     * Helper method used to welcome a new user.
     */
    public void welcomeUser() {
        Platform.runLater(() -> {
            userLabel.setText("Welcome " + model.getUsername() + "!");
            startButton.setDisable(false);
            veil.setVisible(false);
        });
    }

    /**
     * Helper method for printing to text area.
     * @param text Text to print.
     */
    public void print(String text) {
        Platform.runLater(() -> textArea.appendText(text + "\n"));
    }

    /**
     * Creates and shows a dialog that displays an exception.
     * @param e Exception.
     */
    private void createExceptionDialog(Throwable e)
    {
        e.printStackTrace();

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("An error has occurred.");
        alert.setContentText(e.getMessage());

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

    /**
     * Shows the login dialog and returns the typed in captcha message.
     * @return captcha message.
     */
    public Optional<String> showLoginDialog() {
        // Create the custom dialog.
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Captcha Dialog");
        dialog.setHeaderText("Enter the captcha code.");

        // Set the captcha
        dialog.setGraphic(new ImageView(new File("captcha.jpg").toURI().toString()));

        // Set the button types.
        ButtonType enterButtonType = new ButtonType("Enter", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(enterButtonType, ButtonType.CANCEL);

        // Create the captcha field
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField captchaField = new TextField();
        captchaField.setPromptText("Captcha");

        grid.add(new Label("Captcha:"), 0, 0);
        grid.add(captchaField, 1, 0);

        // Enable/Disable enter button depending on whether a captcha was entered.
        Node enterButton = dialog.getDialogPane().lookupButton(enterButtonType);
        enterButton.setDisable(true);

        // Do some validation
        captchaField.textProperty().addListener((observable, oldValue, newValue) ->
                enterButton.setDisable(newValue.trim().isEmpty()));

        dialog.getDialogPane().setContent(grid);

        // Convert the result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == enterButtonType) {
                return captchaField.getText();
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
}