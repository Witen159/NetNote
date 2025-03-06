/*
 * Copyright 2021 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package client.scenes;

import client.utils.ApplicationState;
import client.utils.UndoRedoManager;
import com.google.inject.Inject;
import commons.Collection;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class EditCollectionsCtrl implements Initializable {
    private final ApplicationState state;
    private Scene scene;
    private final UndoRedoManager undoRedoManager;
    private ResourceBundle resourceBundle;

    @FXML
    private ListView<String> collectionListView;

    @FXML
    private Label titleLabel;

    @FXML
    private Label collectionLabel;

    @FXML
    private TextField titleField;

    @FXML
    private Label serverField;

    @FXML
    private Label collectionField;

    @FXML
    private Label statusLabelField;

    @FXML
    private Label statusLabel;

    @FXML
    private Button addCollectionButton;

    @FXML
    private Button removeCollectionButton;

    @FXML
    private Button makeDefaultButton;

    @FXML
    private Button saveButton;


    /**
     * Constructor method for the class.
     *
     * @param state           The application state
     * @param undoRedoManager The undo redo manager
     */
    @Inject
    public EditCollectionsCtrl(ApplicationState state, UndoRedoManager undoRedoManager) {
        this.state = state;
        this.undoRedoManager = undoRedoManager;
    }

    /**
     * Initializes the EditCollectionsCtrl scene by setting up default values
     * for UI elements and pre-populating fields with sample data.
     * This method is automatically invoked when the FXML file is loaded.
     *
     * @param location  the location used to resolve relative paths for the root object
     * @param resources the resources used to localize the root object
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        collectionListView.setItems(state.getObservableCollections());

        // Intercept the ESC key when the ListView is focused
        collectionListView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                changeInputFocus();
                event.consume(); // Prevent the ListView from handling the event
            }
        });

        ChangeListener<Number> onCollectionSelectionChange = (_, _, selectedCollectionIndexNumber)
                -> {
            int selectedCollectionIndex = selectedCollectionIndexNumber.intValue();

            if (selectedCollectionIndex < 0) {
                titleField.setText("");
                serverField.setText("");
                collectionField.setText("");
                statusLabelField.setText("");

                titleField.setDisable(true);
                serverField.setDisable(true);
                collectionField.setDisable(true);
                makeDefaultButton.setDisable(true);
                saveButton.setDisable(true);
            } else {
                Collection selectedCollection =
                        state.getCollectionFromIndex(selectedCollectionIndex);

                titleField.setText(selectedCollection.title);
                serverField.setText(selectedCollection.serverUrl);
                collectionField.setText(String.valueOf(selectedCollection.id));
                statusLabelField.setText("");

                titleField.setDisable(false);
                serverField.setDisable(false);
                collectionField.setDisable(false);
                makeDefaultButton.setDisable(false);
                saveButton.setDisable(false);
            }
        };

        collectionListView.getSelectionModel().selectedIndexProperty()
                .addListener(onCollectionSelectionChange);

        titleField.setDisable(true);
        serverField.setDisable(true);
        collectionField.setDisable(true);
        makeDefaultButton.setDisable(true);
        saveButton.setDisable(true);

        makeDefaultButton.setText(resources.getString("EditCollection.defaultButton"));
        saveButton.setText(resources.getString("EditCollection.saveButton"));
    }

    /**
     * Hardcoded fields of the addCollection scene.
     */
    @FXML
    private void addCollection() {
        undoRedoManager.saveState(state);
        state.addCollection();

        collectionListView.getSelectionModel().selectLast();
    }

    /**
     * Removes the selected collection if there exists one.
     */
    @FXML
    private void removeCollection() {
        if (!state.isServerAvailable()) {
            showServerAlertWarning("Alert.unableToDeleteCollection");
            return;
        }

        undoRedoManager.saveState(state);

        int selectedCollectionIndex = collectionListView.getSelectionModel().getSelectedIndex();

        if (selectedCollectionIndex < 0) {
            showServerAlertWarning("EditCollection.noSelected");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(resourceBundle.getString("Alert.confirmationDialog"));
        alert.setHeaderText(null);
        alert.setContentText(
                state.getResourceBundle().getString("EditCollection.confirmationAlert")
        );

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            state.deleteCollectionFromIndex(selectedCollectionIndex);
        }

        int newSelectedCollectionIndex = Math.max(0, selectedCollectionIndex - 1);
        if (state.getObservableCollections().isEmpty()) {
            collectionListView.getSelectionModel().clearSelection();
        } else {
            collectionListView.getSelectionModel().select(newSelectedCollectionIndex);
        }
    }

    /**
     * Sets one collection as the default.
     */
    @FXML
    private void makeDefault() {
        undoRedoManager.saveState(state);

        int selectedCollectionIndex = collectionListView.getSelectionModel().getSelectedIndex();
        state.setDefaultCollectionFromIndex(selectedCollectionIndex);

        if (state.isServerAvailable()) {
            // Manually select the same collection in the list after setting it to the default
            collectionListView.getSelectionModel().select(selectedCollectionIndex);
            statusLabelField.setText(this.resourceBundle.getString(
                    "EditCollection.collectionMadeDefault"));
        } else {
            statusLabelField.setText(this.resourceBundle.getString(
                    "EditCollection.unableToMakeDefault"));
        }
    }

    /**
     * Handles the saving of a collection. Retrieves user inputs from the fields,
     * checks if the collection title is already in use, and either updates the status
     * or adds the collection to the list.
     */
    @FXML
    private void save() {
        undoRedoManager.saveState(state);

        int selectedCollectionIndex = collectionListView.getSelectionModel().getSelectedIndex();

        state.updateCollectionFromIndex(selectedCollectionIndex,
                titleField.getText(), serverField.getText());

        // Manually select the same collection in the list after setting it to the default
        collectionListView.getSelectionModel().select(selectedCollectionIndex);

        if (!state.isServerAvailable()) {
            statusLabelField.setText(this.resourceBundle.getString(
                    "EditCollection.statusNotUpdated"));
        } else {
            statusLabelField.setText(this.resourceBundle.getString(
                    "EditCollection.statusUpdated"));
        }
    }

    /**
     * Sets the resourceBundle to the class.
     * This can be used for application components that needs to be initialized.
     *
     * @param resourceBundle the ResourceBundle that needs to be changed to
     */
    public void setResourceBundle(ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
    }

    /**
     * Updates the language for this scene.
     *
     * @param newLanguage the new language that needs to be updated
     */
    public void updateLanguage(ResourceBundle newLanguage) {
        titleLabel.setText(newLanguage.getString("EditCollection.title"));
        collectionLabel.setText(newLanguage.getString("EditCollection.collection"));
        statusLabel.setText(newLanguage.getString("EditCollection.status"));

        makeDefaultButton.setText(newLanguage.getString("EditCollection.defaultButton"));
        saveButton.setText(newLanguage.getString("EditCollection.saveButton"));

        if (!state.isServerAvailable()) {
            statusLabelField.setText(newLanguage.getString("EditCollection.serverNotAvailable"));
        }
    }

    /**
     * Show server unavailability alert of warning type.
     *
     * @param message the message to show
     */
    private void showServerAlertWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setTitle(resourceBundle.getString("Alert.warningDialog"));
        alert.setContentText(resourceBundle.getString(message));
        alert.showAndWait();
    }

    /**
     * Configures the scene by registering keyboard shortcuts
     * for various actions in the Edit Collections interface.
     * The following key combinations are registered:
     * - ESC: Cycles through input fields.
     * - CTRL + N: Adds a new collection.
     * - CTRL + D: Deletes the selected collection.
     * - ALT + DOWN: Selects the next collection.
     * - ALT + UP: Selects the previous collection.
     * - CTRL + S: Saves changes to the selected collection.
     * - CTRL + M: Sets the selected collection as the default.
     * - CTRL + W: Closes the Edit Collections interface.
     * - CTRL + Z: Undo
     * - CTRL + Y: Redo
     *
     * @param scene The scene to be configured with keyboard shortcuts
     */
    public void setScene(Scene scene) {
        this.scene = scene;

        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.ESCAPE),
                this::changeInputFocus);

        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN),
                this::addNewCollection);

        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN),
                this::deleteCollection);

        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.DOWN, KeyCombination.ALT_DOWN),
                this::nextCollection);

        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.UP, KeyCombination.ALT_DOWN),
                this::previousCollection);

        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN),
                this::saveCollection);

        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.M, KeyCombination.CONTROL_DOWN),
                this::callMakeDefault);

        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN),
                this::closeCollection);

        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN),
                this::undo);

        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN),
                this::redo);

    }

    /**
     * Cycles through input fields when the ESC key is pressed.
     * If none of the input fields are focused, or if the ListView is focused,
     * this method sets the focus to the Title field. It then cycles through
     * the Server and Collection fields in order.
     */
    private void changeInputFocus() {
        System.out.println("ESCAPE key pressed");
        titleField.requestFocus();
    }

    /**
     * Handles the creation of a new collection when CTRL + N is pressed.
     * This method fires the action of the "Add Collection" button.
     */
    private void addNewCollection() {
        System.out.println("CTRL + N detected");
        addCollectionButton.fire();
    }

    /**
     * Deletes the selected collection when CTRL + D is pressed.
     * This method fires the action of the "Remove Collection" button.
     */
    private void deleteCollection() {
        System.out.println("CTRL + D detected");
        removeCollectionButton.fire();
    }

    /**
     * Selects the next collection in the list when ALT + DOWN is pressed.
     * This method moves the selection to the next item in the ListView and
     * scrolls to the newly selected item, if necessary.
     */
    private void nextCollection() {
        int currentIndex = collectionListView.getSelectionModel().getSelectedIndex();
        if (currentIndex < collectionListView.getItems().size() - 1) {
            collectionListView.getSelectionModel().select(currentIndex + 1);
            collectionListView.scrollTo(currentIndex + 1);
            System.out.println("Moved to next item: "
                    + collectionListView.getSelectionModel().getSelectedItem());
        }
    }

    /**
     * Selects the previous collection in the list when ALT + UP is pressed.
     * This method moves the selection to the previous item in the ListView and
     * scrolls to the newly selected item, if necessary.
     */
    private void previousCollection() {
        int currentIndex = collectionListView.getSelectionModel().getSelectedIndex();
        if (currentIndex > 0) {
            collectionListView.getSelectionModel().select(currentIndex - 1);
            collectionListView.scrollTo(currentIndex - 1);
            System.out.println("Moved to previous item: "
                    + collectionListView.getSelectionModel().getSelectedItem());
        }
    }

    /**
     * Saves the currently selected collection when CTRL + S is pressed.
     * This method fires the action of the "Save" button.
     */
    private void saveCollection() {
        saveButton.fire();
    }

    /**
     * Sets the currently selected collection as the default when CTRL + M is pressed.
     * This method fires the action of the "Make Default" button.
     */
    private void callMakeDefault() {
        makeDefaultButton.fire();
    }

    /**
     * Closes the Edit Collections interface by hiding the secondary stage.
     */
    public void closeCollection() {
        if (scene != null && scene.getWindow() != null) {
            scene.getWindow().hide();
        }
    }

    /**
     * Returns back to 1 previous state.
     */
    private void undo() {
        if (undoRedoManager.undo()) {
            System.out.println("Undo successful");
        } else {
            System.out.println("No more actions to undo");
        }
    }

    /**
     * Returns to 1 state ahead of the state when undo() was executed.
     */
    private void redo() {
        if (undoRedoManager.redo()) {
            System.out.println("Redo successful");
        } else {
            System.out.println("No more actions to redo");
        }
    }
}
