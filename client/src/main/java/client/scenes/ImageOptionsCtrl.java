package client.scenes;

import client.utils.ServerUtils;
import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.FileChooser;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.util.ResourceBundle;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class ImageOptionsCtrl implements Initializable {
    private Scene scene;
    private final ServerUtils server;
    private ResourceBundle resourceBundle;
    private File selectedImage;

    @FXML
    private Button addImageButton;
    @FXML
    private Button deleteImageButton;
    @FXML
    private ListView<String> imageViewList;


    /**
     * Constructor for ImageOptionsCtrl.
     *
     * @param server         the server object to communicate with the server
     */
    @Inject
    public ImageOptionsCtrl(ServerUtils server) {
        this.server = server;
        Locale defaultLocale = Locale.ENGLISH;
        Locale.setDefault(Locale.ENGLISH);

        try {
            this.resourceBundle = ResourceBundle
                    .getBundle("Internationalization.Text", defaultLocale);
        } catch (MissingResourceException e) {
            System.err.println("Missing ResourceBundle for the default locale. " +
                    "Falling back to base bundle.");
            this.resourceBundle = ResourceBundle
                    .getBundle("Internationalization.Text", Locale.ROOT);
        }
    }


    /**
     * Initializes the controller.
     *
     * @param location  the location of image options ctrl
     * @param resources the resources bundle of image options ctrl
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        imageViewList.getSelectionModel().selectedItemProperty()
                .addListener((_, _, newValue) -> {
                    if (newValue != null) {
                        selectedImage = new File(newValue);
                    }
                });

        if (server.getAllImagesNames() != null) {
            List<String> allImageNames = List.of(server.getAllImagesNames().split("\n"));
            imageViewList.getItems().addAll(allImageNames);
        }
    }

    /**
     * Sets the ResourceBundle for translations.
     *
     * @param resourceBundle the ResourceBundle to be used for translations
     */
    public void setResourceBundle(ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
    }

    /**
     * Sets the scene for the controller.
     *
     * @param scene the scene to set for the controller
     */
    public void setScene(Scene scene) {
        this.scene = scene;

        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN),
                this::addImage);

        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN),
                this::deleteImage);

        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.DOWN, KeyCombination.ALT_DOWN),
                this::nextFile);

        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.UP, KeyCombination.ALT_DOWN),
                this::previousFile);

        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN),
                this::closeWindow);
    }

    /**
     * Update the scene language.
     * @param newLanguage the new language
     */
    public void updateLanguage(ResourceBundle newLanguage) {
        this.resourceBundle = newLanguage;
        addImageButton.setText(resourceBundle.getString("Image.addImage"));
        deleteImageButton.setText(resourceBundle.getString("Image.deleteImage"));
    }

    /**
     * Adds an image to the server.
     */
    public void addImage() {
        if (!server.isServerAvailable()) {
            showWarning("Alert.unableToUploadImage");
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(resourceBundle.getString("Image.selectToAdd"));
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Image Files", "*.png", "*.jpg", "*.jpeg"
                ));
        Window window = scene.getWindow();
        File selectedFile = fileChooser.showOpenDialog(window);

        System.out.println(server.getAllImagesNames());
        String allImageNames = server.getAllImagesNames();
        if (selectedFile != null && selectedFile.exists()) {
            if (allImageNames.contains(selectedFile.getName())) {
                showWarning("Image.imageAlreadyExists");
                return;
            }
            try {
                boolean success = server.uploadImage(selectedFile);
                if (success) {
                    showInformation("Image.uploadSuccess");
                    refreshViewList();
                } else {
                    showWarning("Image.uploadFailure");
                }
            } catch (Exception e) {
                showWarning("Image.genericError");
            }
        } else {
            showInformation("Image.noImageSelected");
        }
    }

    /**
     * Deletes the selected image by the user.
     */
    public void deleteImage() {
        if (!server.isServerAvailable()) {
            showWarning("Alert.unableToDeleteImage");
            return;
        }
        AtomicReference<String> imageToDelete = new AtomicReference<>("");

        if (selectedImage != null) {
            System.out.println(selectedImage);
            imageToDelete.set(selectedImage.getName());
        } else {
            showWarning("Image.selectToDelete");
            return;
        }

        String[] parts = imageToDelete.get().split("; ");
        String id = "";
        for (String part : parts) {
            if (part.startsWith("id: ")) {
                id = part.split(": ")[1];
            }
        }
        boolean success = server.deleteImage(id);

        if (success) {
            imageViewList.getItems().remove(selectedImage);
            showInformation("Image.deleteSuccess");
            refreshViewList();
        } else {
            showWarning("Image.deleteFailure");
        }
    }


    /**
     * Refreshes the list of available images.
     */
    public void refreshViewList() {
        imageViewList.getItems().clear();
        List<String> allImageNames = List.of(server.getAllImagesNames().split("\n"));
        imageViewList.getItems().addAll(allImageNames);
    }

    /**
     * Shows an alert of alert type.
     *
     * @param message the message to show
     */
    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setTitle(resourceBundle.getString("Alert.warningDialog"));
        alert.setContentText(resourceBundle.getString(message));
        alert.showAndWait();
    }

    /**
     * Shows an alert of information type.
     *
     * @param message the message to show
     */
    private void showInformation(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initStyle(StageStyle.UTILITY);
        alert.setTitle(resourceBundle.getString("Alert.informationDialog"));
        alert.setHeaderText(null);
        alert.setContentText(resourceBundle.getString(message));
        alert.show();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(alert::close);
            }
        }, 2000);
    }

    /**
     * Handles the ALT + DOWN key combination event.
     * <p>
     * Moves the selection in the imageViewList to the next item if it exists.
     * Logs the action and ensures the list view scrolls to the selected item.
     * </p>
     */
    private void nextFile() {
        int currentIndex = imageViewList.getSelectionModel().getSelectedIndex();
        if (currentIndex < imageViewList.getItems().size() - 1) {
            imageViewList.getSelectionModel().select(currentIndex + 1);
            imageViewList.scrollTo(currentIndex + 1);
            System.out.println("Moved to next item: "
                    + imageViewList.getSelectionModel().getSelectedItem());
        }
    }

    /**
     * Handles the ALT + UP key combination event.
     * <p>
     * Moves the selection in the imageViewList to the previous item if it exists.
     * Logs the action and ensures the list view scrolls to the selected item.
     * </p>
     */
    private void previousFile() {
        int currentIndex = imageViewList.getSelectionModel().getSelectedIndex();
        if (currentIndex > 0) {
            imageViewList.getSelectionModel().select(currentIndex - 1);
            imageViewList.scrollTo(currentIndex - 1);
            System.out.println("Moved to previous item: "
                    + imageViewList.getSelectionModel().getSelectedItem());
        }
    }

    /**
     * Closes the window and stops the program.
     */
    private void closeWindow() {
        if (scene != null && scene.getWindow() != null) {
            scene.getWindow().hide();
        }
    }
}
