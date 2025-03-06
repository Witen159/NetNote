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

import client.Markdown;
import client.utils.*;
import client.utils.UndoRedoManager;
import com.google.inject.Inject;
import commons.AppConfig;
import commons.Files;
import commons.Note;
import commons.Tag;
import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Popup;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class NoteOverviewCtrl implements Initializable {
    private final ApplicationState state;
    private final MainCtrl mainCtrl;
    private Scene scene;
    private ResourceBundle resourceBundle;
    private final LinkedHashMap<String, Locale> languages = createLanguagesMap();
    private final UndoRedoManager undoRedoManager;
    private final Markdown markdown;
    private PauseTransition idlePause;

    @FXML
    public Label tagsFilterText;
    @FXML
    private Button saveButton;
    @FXML
    private Button editButton;
    @FXML
    private TextField searchField;
    @FXML
    private Label collectionText;
    @FXML
    private ListView<String> myNotesList;
    @FXML
    private TextField myNoteTitle;
    @FXML
    private TextArea myNoteContent;
    @FXML
    private ChoiceBox<String> myCollections;
    @FXML
    private ChoiceBox<String> mySelectedNoteCollection;
    @FXML
    private Button addButton;
    @FXML
    private Button removeButton;
    @FXML
    private Button refreshButton;
    @FXML
    private WebView myWebView;
    @FXML
    private Button syntaxButton;
    @FXML
    private ComboBox<String> myLanguages;
    @FXML
    private ComboBox<Tag> tagComboBox;
    @FXML
    private Button clearTagsButton;
    @FXML
    private Button imageOptionsButton;
    @FXML
    private Label selectedTagsLabel;

    /**
     * Instantiates a new note overview controller.
     *
     * @param state           The state of application
     * @param mainCtrl        The main controller
     * @param undoRedoManager The undo redo manager
     * @param markdown        The Markdown syntax checker
     */
    @Inject
    public NoteOverviewCtrl(ApplicationState state, MainCtrl mainCtrl,
                            UndoRedoManager undoRedoManager, Markdown markdown) {
        this.state = state;
        this.mainCtrl = mainCtrl;
        this.undoRedoManager = undoRedoManager;
        this.markdown = markdown;
        Locale initialLocale = mainCtrl.getAppConfig().getSelectedLanguage();
        ResourceBundle initialBundle = ResourceBundle
                .getBundle("Internationalization.Text", initialLocale);
        this.markdown.setResourceBundle(initialBundle);

    }

    /**
     * Initializes values and event listeners for the note overview.
     *
     * @param location  Location of the note overview controller
     * @param resources The resource bundle of the note overview controller
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ChangeListener<Number> onCollectionSelectionChange = (_, _, selectedCollectionIndexNumber)
                -> {
            // Make sure there's always a collection that is selected
            if (selectedCollectionIndexNumber.intValue() < 0) {
                myCollections.getSelectionModel().selectFirst();
            }
            // -1 because "All" doesn't count as a collection
            int selectedCollectionIndex = selectedCollectionIndexNumber.intValue() - 1;
            state.setSelectedCollectionFromIndex(selectedCollectionIndex);
        };

        myCollections.setItems(state.getObservableCollectionsWithAll());
        myCollections.getSelectionModel().selectedIndexProperty()
                .addListener(onCollectionSelectionChange);
        mySelectedNoteCollection.setItems(state.getObservableCollections());
        ChangeListener<String> onSearchQueryChange = (_, _, query) ->
                state.updateSearchQuery(query);
        searchField.textProperty().addListener(onSearchQueryChange);
        ChangeListener<Number> onNoteCollectionChange = (_, _, collectionIndexNumber) -> {
            int collectionIndex = collectionIndexNumber.intValue();

            // the "All" collection is selected
            boolean allCollections = myCollections.getSelectionModel().getSelectedIndex() <= 0;

            int selectedNoteIndex = myNotesList.getSelectionModel().getSelectedIndex();

            if (state.hasNoteCollectionSelectionChanged(collectionIndex)) {
                undoRedoManager.maybeSaveState(state);
            }

            if (collectionIndex >= 0) {
                state.updateSelectedNoteCollectionFromIndex(collectionIndex);
            } else if (state.getLastNonNullSelectedNote() != null) {
                // if the collections were edited, make sure to reselect the correct
                // collection for the note
                mySelectedNoteCollection.getSelectionModel().select(
                        state.getIndexFromCollection(state.getLastNonNullSelectedNote().collection)
                );
            }

            // if the "All" collection is selected and the note's collection is changed,
            // make sure the same note stays selected
            if (allCollections) {
                myNotesList.getSelectionModel().select(selectedNoteIndex);
            }
        };

        state.getObservableNotes().addListener((InvalidationListener) _ -> {
            int index = state.getFilteredIndexFromLastNonNullSelectedNote();

            Platform.runLater(() -> myNotesList.getSelectionModel().select(index));
        });

        mySelectedNoteCollection.getSelectionModel().selectedIndexProperty()
                .addListener(onNoteCollectionChange);

        setOnNoteSelectionChangeEventListener();

        ChangeListener<String> onNoteContentChange = (_, _, content) ->
                state.updateSelectedNoteContent(content);
        myNotesList.setItems(state.getObservableNotes());

        myNoteContent.textProperty().addListener((_, _, t1) -> {
            updateWebView(t1);
            if (!state.isServerAvailable()) {
                myNoteContent.setOnMouseClicked(_ ->
                        showServerAlertWarning("Alert.serverNotAvailableChanges"));
            }
        });

        myNoteContent.textProperty().addListener(onNoteContentChange);

        WebEngine webEngine = myWebView.getEngine();
        URL cssUrl = getClass().getClassLoader().getResource("style.css");
        assert cssUrl != null;
        webEngine.setUserStyleSheetLocation(cssUrl.toString());
        updateWebView(myNoteContent.getText());

        myNoteTitle.setOnMouseClicked(_ -> undoRedoManager.maybeSaveState(state));

        setDisableForAll(true);

        languageSetup();

        List<Tag> allTags = state.fetchAllTags();
        if (allTags == null || allTags.isEmpty()) {
            System.out.println("No tags fetched or the list is empty");
        } else {
            tagComboBox.getItems().addAll(allTags);
        }
    }

    /**
     * Sets up the language ComboBox and later use of language function.
     */
    private void languageSetup() {

        myLanguages.getItems().addAll(languages.keySet());
        myLanguages.setValue(getLanguageName(Locale.ENGLISH));
        myLanguages.setValue(getLanguageName(mainCtrl.getAppConfig().getSelectedLanguage()));
        myLanguages.getSelectionModel().selectedItemProperty()
                .addListener((_, _, newValue) -> switchLanguage(newValue));

        Map<String, String> flagPaths = createFlagPaths();

        myLanguages.setCellFactory(_ -> new ListCell<>() {
            private final ImageView imageView = new ImageView();
            private final Label label = new Label();
            private final HBox hBox =
                    new HBox(10, imageView, label); // Add spacing between the image and text

            {
                label.setStyle("-fx-text-fill: black;"); // Ensure the text color is black
            }

            @Override
            protected void updateItem(String language, boolean empty) {
                super.updateItem(language, empty);
                if (empty || language == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    String imagePath = flagPaths.get(language);
                    if (imagePath != null) {
                        imageView.setImage(new Image(imagePath));
                        imageView.setFitWidth(24);
                        imageView.setFitHeight(16);
                    } else {
                        imageView.setImage(null); // Clear the image if no path is found
                    }
                    label.setText(language); // Set the language name
                    setGraphic(hBox); // Use HBox to display image and text
                }
            }
        });

        myLanguages.setButtonCell(new ListCell<>() {
            private final ImageView imageView = new ImageView();
            private final Label label = new Label();
            private final HBox hBox =
                    new HBox(10, imageView, label); // Add spacing between the image and text

            {
                label.setStyle("-fx-text-fill: black;"); // Ensure the text color is black
            }

            @Override
            protected void updateItem(String language, boolean empty) {
                super.updateItem(language, empty);
                if (empty || language == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    String imagePath = flagPaths.get(language);
                    if (imagePath != null) {
                        imageView.setImage(new Image(imagePath));
                        imageView.setFitWidth(24);
                        imageView.setFitHeight(16);
                    } else {
                        imageView.setImage(null); // Clear the image if no path is found
                    }
                    label.setText(language); // Set the language name
                    setGraphic(hBox); // Use HBox to display image and text
                }
            }
        });

        myLanguages.setPrefWidth(250);


        myLanguages.getSelectionModel().selectedItemProperty()
                .addListener((_, _, newValue) -> {
                    if (newValue != null) {
                        switchLanguage(newValue);
                    }
                });
    }

    /**
     * Set event listener when note selection changes.
     */
    private void setOnNoteSelectionChangeEventListener() {
        ChangeListener<Number> onNoteSelectionChange = (_, _, selectedNoteIndexNumber) -> {
            // Clear and populate tags
            tagComboBox.getItems().clear();
            List<Tag> allTags = state.fetchAllTags();
            if (allTags != null && !allTags.isEmpty()) {
                tagComboBox.getItems().addAll(allTags);
            }

            int selectedNoteIndex = selectedNoteIndexNumber.intValue();
            state.setSelectedNoteFromFilteredIndex(selectedNoteIndex);

            // Handle cases based on the selected note index
            if (selectedNoteIndex < 0) {
                // No note selected
                myNoteTitle.setText("");
                myNoteContent.setText("");
                mySelectedNoteCollection.getSelectionModel().select(-1);
                mySelectedNoteCollection.setValue(null);
                setDisableForAll(true);

                updateWebView(null, resourceBundle);
            } else {
                Note selectedNote = state.getNoteFromFilteredIndex(selectedNoteIndex);

                myNoteTitle.setText(selectedNote.title);
                myNoteContent.setText(selectedNote.getContent());

                mySelectedNoteCollection.getSelectionModel().select(
                        state.getIndexFromCollection(selectedNote.collection));

                setDisableForAll(false);

                String noteContent = selectedNote.getContent();
                updateWebView(noteContent, resourceBundle);
            }
        };

        myNotesList.getSelectionModel().selectedIndexProperty().addListener(onNoteSelectionChange);
    }


    /**
     * Disable all note elements (or enable).
     *
     * @param disable Disable status
     */
    private void setDisableForAll(boolean disable) {
        myNoteTitle.setDisable(disable);
        myNoteContent.setDisable(disable);
        mySelectedNoteCollection.setDisable(disable);
        saveButton.setDisable(disable);
    }

    /**
     * Selects the correct collection when initializing.
     */
    public void initializeCollectionSelection() {
        myCollections.getSelectionModel().selectFirst();
    }

    /**
     * Initializes the available languages from the application.
     *
     * @return a LinkedHashMap<String, Locale> that stores the languages as Locales
     */
    private LinkedHashMap<String, Locale> createLanguagesMap() {
        LinkedHashMap<String, Locale> languages = new LinkedHashMap<>();
        languages.put("English", new Locale("En"));
        languages.put("Dutch", new Locale("Nl"));
        languages.put("Romanian", new Locale("Ro"));
        return languages;
    }

    /**
     * Creates paths from the language name to its respective flag.
     *
     * @return a map of the paths
     */
    private Map<String, String> createFlagPaths() {
        Map<String, String> flagPaths = new HashMap<>();
        flagPaths.put("English", "/flags/English.png");
        flagPaths.put("Dutch", "/flags/Dutch.png");
        flagPaths.put("Romanian", "/flags/Romanian.png");
        return flagPaths;
    }

    /**
     * Saves the updated title when clicking on the save button.
     *
     * @param actionEvent Information about the event
     */
    public void updateNoteTitle(ActionEvent actionEvent) {
        int selectedNoteIndex = myNotesList.getSelectionModel().getSelectedIndex();

        String newTitle = myNoteTitle.getText();

        if (newTitle.isEmpty()) {
            showServerAlertWarning("Note.emptyTitle");
        } else {
            undoRedoManager.maybeSaveState(state);
            state.updateSelectedNoteTitle(newTitle);
        }
        myNotesList.getSelectionModel().select(selectedNoteIndex);
    }

    /**
     * Adds a new note to the list.
     *
     * @param actionEvent Information about the event
     */
    public void addNote(ActionEvent actionEvent) {
        undoRedoManager.saveState(state);

        boolean wasListEmpty = myNotesList.getItems().isEmpty();

        // -1 because "All" doesn't count as a collection
        int selectedCollectionIndex = myCollections.getSelectionModel().getSelectedIndex() - 1;

        state.addNote(selectedCollectionIndex);

        Platform.runLater(() -> {
            // Select the newly added note
            myNotesList.getSelectionModel().selectLast();
        });

        // Update the WebView content
        if (wasListEmpty) {
            // If the list was empty before, show the empty note message for the new note
            updateWebView("");
        } else {
            // Otherwise, update for the new note's content
            String newText = myNoteContent.getText();
            updateWebView(newText);
        }
    }

    /**
     * Deletes a note from the list.
     *
     * @param actionEvent Information about the event
     */
    public void deleteNote(ActionEvent actionEvent) {
        if (!state.isServerAvailable()) {
            showServerAlertWarning("Alert.unableToDeleteNote");
            return;
        }

        undoRedoManager.saveState(state);

        int selectedNoteIndex = myNotesList.getSelectionModel().getSelectedIndex();
        if (selectedNoteIndex < 0) {
            myNotesList.getSelectionModel().selectLast();
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(resourceBundle.getString("Alert.confirmationDialog"));
        alert.setHeaderText(null);
        alert.setContentText(resourceBundle.getString("Note.confirmationAlert"));

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            state.deleteSelectedNote();

            if (state.isServerAvailable()) {
                Alert deletionAlert = new Alert(Alert.AlertType.INFORMATION);
                deletionAlert.initStyle(StageStyle.UTILITY);
                deletionAlert.setTitle(resourceBundle.getString("Alert.informationDialog"));
                deletionAlert.setHeaderText(null);
                deletionAlert.setContentText(resourceBundle.getString("Note.deleted"));
                deletionAlert.show();

                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(deletionAlert::close);
                    }
                }, 2000);
            }
        }


        // Check if the list is now empty after the deletion
        if (myNotesList.getItems().isEmpty()) {
            // If the list is empty, update the WebView to show "No Note Selected"
            updateWebView(null);
        } else {
            // Otherwise, manually select the correct note, i.e.,
            // the note above the deleted one in the list if possible
            int newSelectedNoteIndex = Math.max(0, selectedNoteIndex - 1);

            Platform.runLater(() -> myNotesList.getSelectionModel().select(newSelectedNoteIndex));

            // Update the WebView with the selected note's content
            String newText = myNoteContent.getText();
            updateWebView(newText);
        }
    }

    /**
     * Refreshes data from the server.
     *
     * @param actionEvent Information about the event
     */
    public void refresh(ActionEvent actionEvent) {
        if (!state.isServerAvailable()) {
            showServerAlertWarning("Alert.serverNotAvailableRefresh");
            return;
        }
        state.refresh();
        initializeCollectionSelection();
    }

    /**
     * Shows the scene to edit collections.
     *
     * @param actionEvent Information about the event
     */
    public void editCollections(ActionEvent actionEvent) {
        mainCtrl.showCollections();
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
     * Shows the scene to edit images.
     *
     * @param actionEvent Information about the event
     */
    @FXML
    public void imageOptions(ActionEvent actionEvent) {
        mainCtrl.showImageOptions();
    }

    /**
     * Updates the content of the WebView to display the rendered HTML of the given Markdown text.
     * <p>
     * The method validates the Markdown syntax,
     * parses it into HTML, and displays the content in the WebView.
     * If the Markdown content is invalid, an error message is logged to the console.
     * The method handles cases where no note is selected,
     * the note list is empty, or the note content is empty.
     * </p>
     *
     * @param text           the Markdown content to be rendered and displayed in the WebView.
     *                       If the content is null or empty, a default message is displayed.
     *                       If the Markdown content is invalid, an error message is logged.
     * @param resourceBundle the ResourceBundle to be used for the language-specific messages.
     */
    void updateWebView(String text, ResourceBundle resourceBundle) {
        WebEngine webEngine = myWebView.getEngine();

        if (myNotesList.getSelectionModel().getSelectedIndex() < 0
                || myNotesList.getItems().isEmpty()) {
            String defaultMessage = "<html><body><h2>"
                    + resourceBundle.getString("Note.noSelectedNote")
                    + "</h2><p>"
                    + resourceBundle.getString("Note.noSelectedNoteBody")
                    + "</p></body></html>";
            webEngine.loadContent(defaultMessage, "text/html");
            return;
        }

        if (text == null || text.trim().isEmpty()) {
            String emptyNoteMessage = "<html><body><h2>"
                    + resourceBundle.getString("Note.emptyNote")
                    + "</h2><p>"
                    + resourceBundle.getString("Note.emptyNoteBody")
                    + "</p></body></html>";
            webEngine.loadContent(emptyNoteMessage, "text/html");
            return;
        }

        try {
            Parser parser = Parser.builder().build();
            Node document = parser.parse(text);
            HtmlRenderer renderer = HtmlRenderer.builder().build();
            String htmlContent = renderer.render(document);

            webEngine.loadContent(htmlContent, "text/html");

            try {
                markdown.validateMarkdownSyntax(text);
            } catch (MarkdownSyntaxException e) {
                System.err.println("Markdown syntax issues:" + e.getMessage());
            }
        } catch (Exception e) {
            String fallbackContent = "<html><body><pre>"
                    + escapeHtml(text) + "</pre></body></html>";
            webEngine.loadContent(fallbackContent, "text/html");
            System.err.println("Error rendering Markdown: " + e.getMessage());
        }
    }

    /**
     * Escapes HTML characters in the text to prevent rendering issues.
     *
     * @param text the text the will be modified
     * @return a string with replaced text
     */
    private String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }

    /**
     * Returns the Markdown text with the correct image syntax to render.
     *
     * @param text the Markdown text to be modified for the image
     * @return the corrected Markdown text with the correct image syntax
     */
    private String markdownImage(String text) {
        Pattern pattern = Pattern.compile("!\\[.*?]\\(.*?\\)");
        Matcher matcher = pattern.matcher(text);
        List<String> markdownPlaceHolders = new ArrayList<>();
        while (matcher.find()) {
            markdownPlaceHolders.add(matcher.group());
        }

        List<Files> filesMap = state.getAllImages();

        for (String markdownPlaceHolder : markdownPlaceHolders) {
            String imageName = markdownPlaceHolder.substring(
                    markdownPlaceHolder.indexOf('[') + 1, markdownPlaceHolder.indexOf(']'));
            String imageType = markdownPlaceHolder.substring(
                    markdownPlaceHolder.lastIndexOf('.') + 1, markdownPlaceHolder.indexOf(')'));

            Files correspondingFiles = null;
            for (Files files : filesMap) {
                if (files.getName().equals(imageName) && files.getType().equals(imageType)) {
                    correspondingFiles = files;
                    break;
                }
            }
            if (correspondingFiles != null) {
                String base64Image = Base64.getEncoder()
                        .encodeToString(correspondingFiles.getImageByte());
                String imageSrc = "<img alt='" + imageName + "' src='data:image/" + imageType
                        + ";base64," + base64Image + "' />";
                text = text.replace(markdownPlaceHolder, imageSrc);
            }
        }
        return text;
    }


    /**
     * Overloaded method of updateWebView.
     * This method ensures that the FXML can initialize the class,
     * while new language changes can be handled.
     *
     * @param text same as the updateWebView's text parameter
     */
    void updateWebView(String text) {
        if (this.resourceBundle == null) {
            updateWebView(text,
                    ResourceBundle.getBundle("Internationalization.Text", Locale.ENGLISH));
        } else {
            updateWebView(text, this.resourceBundle);
        }
    }

    /**
     * Validates the syntax of the Markdown content
     * in the current note and displays the result in the WebView.
     * <p>
     * This method checks the Markdown syntax
     * for errors using validateMarkdownSyntax. If the syntax
     * is valid, it parses and renders the
     * Markdown content into HTML and displays a success message in the WebView.
     * If syntax errors are found, the errors
     * are displayed in the WebView. Any unexpected exceptions are also handled
     * and displayed in the WebView.
     * </p>
     */
    @FXML
    public void syntaxCheck() {
        String text = myNoteContent.getText();

        try {
            markdown.validateMarkdownSyntax(text);

            Parser parser = Parser.builder().build();
            Node document = parser.parse(text);

            HtmlRenderer renderer = HtmlRenderer.builder().build();
            renderer.render(document);

            myWebView.getEngine().loadContent("<html><body><h2>" +
                    resourceBundle.getString("Markdown.syntax") +
                    "</h2><p>" +
                    resourceBundle.getString("Markdown.noErrors") +
                    "</p></body></html>", "text/html");

        } catch (MarkdownSyntaxException e) {
            String title = resourceBundle.getString("Note.syntaxError");
            myWebView.getEngine().loadContent("<html><body><h2>" + title + "</h2><p>"
                    + e.getMessage().replaceAll("\n", "<br>") + "</p></body></html>", "text/html");
        } catch (Exception e) {
            myWebView.getEngine().loadContent("<html><body><h2>" +
                    resourceBundle.getString("Markdown.unexpectedError") +
                    "</h2><p>"
                    + e.getMessage() + "</p></body></html>", "text/html");
        }

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> updateWebView(text));
            }
        }, 2000);

    }

    /**
     * Sets the current scene and configures keyboard shortcuts for the scene.
     * This method assigns the given Scene to the current instance
     * and registers several key combinations as accelerators. Each accelerator
     * is mapped to a specific event handler method that executes when the
     * corresponding key combination is pressed.
     *
     * @param scene the Scene to be set and configured with accelerators
     */
    public void setScene(Scene scene) {
        this.scene = scene;

        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.ESCAPE),
                this::changeInputFocus);

        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN),
                this::addNewNote);

        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN),
                this::deleteNote);

        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN),
                this::refreshNotes);

        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN),
                this::checkSyntax);

        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN),
                this::cycleTags);

        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.I, KeyCombination.CONTROL_DOWN),
                this::imageOptionsFire);

        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.T,
                        KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN),
                this::clearTags);

        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.DOWN, KeyCombination.ALT_DOWN),
                this::nextNote);

        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.UP, KeyCombination.ALT_DOWN),
                this::previousNote);

        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.RIGHT, KeyCombination.ALT_DOWN),
                this::nextCollection);

        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.LEFT, KeyCombination.ALT_DOWN),
                this::previousCollection);

        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN),
                this::cycleSelectedCollection);

        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.ENTER),
                this::saveTitle);

        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN),
                this::cycleLanguage);

        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN),
                this::editCollection);

        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN),
                this::closeWindow);

        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN),
                this::undo);

        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN),
                this::redo);
    }

    /**
     * Handles the ESCAPE key press event.
     * <p>
     * Logs the event and shifts focus to other text areas.
     */
    private void changeInputFocus() {
        System.out.println("ESCAPE key pressed");
        if (!(scene.getFocusOwner() instanceof TextInputControl)) {
            myNoteContent.requestFocus();
        } else if (myNoteContent.isFocused()) {
            myNoteTitle.requestFocus();
        } else if (myNoteTitle.isFocused()) {
            searchField.requestFocus();
        } else if (searchField.isFocused()) {
            myNoteContent.requestFocus();
        }
    }

    /**
     * Handles the CTRL + N key combination event.
     * <p>
     * Logs the event and triggers the addButton.
     */
    private void addNewNote() {
        System.out.println("CTRL + N detected");
        addButton.fire();
    }

    /**
     * Handles the CTRL + D key combination event.
     * <p>
     * Logs the event and triggers the removeButton.
     */
    private void deleteNote() {
        System.out.println("CTRL + D detected");
        removeButton.fire();
    }


    /**
     * Handles the CTRL + R key combination event.
     * <p>
     * Logs the event and triggers the refreshButton.
     */
    private void refreshNotes() {
        System.out.println("CTRL + R detected");
        refreshButton.fire();
    }

    /**
     * Handles the CTRL + E key combination event.
     * <p>
     * Triggers the syntaxButton
     */
    private void checkSyntax() {
        System.out.println("CTRL + E detected");
        syntaxButton.fire();
    }

    /**
     * Saves the current note title when the title input field is focused.
     * This method checks if the note title input field has focus.
     * If it does,it triggers the save button's action by calling {@code fire()} on the save button.
     * This allows users to save
     * their changes to the note title without manually clicking the save button.
     */
    private void saveTitle() {
        if (myNoteTitle.isFocused()) {
            saveButton.fire();
        }
    }

    /**
     * Handles the ALT + DOWN key combination event.
     * <p>
     * Moves the selection in the myNotesList to the next item if it exists.
     * Logs the action and ensures the list view scrolls to the selected item.
     * </p>
     */
    private void nextNote() {
        int currentIndex = myNotesList.getSelectionModel().getSelectedIndex();
        if (currentIndex < myNotesList.getItems().size() - 1) {
            myNotesList.getSelectionModel().select(currentIndex + 1);
            myNotesList.scrollTo(currentIndex + 1);
            System.out.println("Moved to next item: "
                    + myNotesList.getSelectionModel().getSelectedItem());
        }
    }

    /**
     * Handles the ALT + UP key combination event.
     * <p>
     * Moves the selection in the myNotesList to the previous item if it exists.
     * Logs the action and ensures the list view scrolls to the selected item.
     * </p>
     */
    private void previousNote() {
        int currentIndex = myNotesList.getSelectionModel().getSelectedIndex();
        if (currentIndex > 0) {
            myNotesList.getSelectionModel().select(currentIndex - 1);
            myNotesList.scrollTo(currentIndex - 1);
            System.out.println("Moved to previous item: "
                    + myNotesList.getSelectionModel().getSelectedItem());
        }
    }

    /**
     * Handles the ALT + RIGHT key combination event.
     * <p>
     * Moves the selection in the myCollections list to the next item, if it exists.
     * Logs the action and ensures the selected collection is updated.
     * </p>
     */
    private void nextCollection() {
        int currentIndex = myCollections.getSelectionModel().getSelectedIndex();
        if (currentIndex < myCollections.getItems().size() - 1) {
            myCollections.getSelectionModel().select(currentIndex + 1);
            System.out.println("Moved to next collection: "
                    + myCollections.getSelectionModel().getSelectedItem());
        }
        showCustomPopup(myCollections);
    }

    /**
     * Handles the ALT + LEFT key combination event.
     * <p>
     * Moves the selection in the myCollections list to the previous item, if it exists.
     * Logs the action and ensures the selected collection is updated.
     * </p>
     */
    private void previousCollection() {
        int currentIndex = myCollections.getSelectionModel().getSelectedIndex();
        if (currentIndex > 0) {
            myCollections.getSelectionModel().select(currentIndex - 1);
            System.out.println("Moved to previous collection: "
                    + myCollections.getSelectionModel().getSelectedItem());
        }
        showCustomPopup(myCollections);
    }

    private int highlightedIndex = -1;
    private final Popup collectionPopup = new Popup();

    /**
     * Cycles through the selected collections and starts the idle timer.
     */
    private void cycleSelectedCollection() {
        if (myNotesList.getFocusModel().getFocusedItem() != null) {
            if (!collectionPopup.isShowing()) {
                setupPopup();
            }

            int currentIndex = (highlightedIndex >= 0)
                    ? highlightedIndex
                    : Math.max(0, mySelectedNoteCollection.getSelectionModel().getSelectedIndex());

            highlightedIndex = (currentIndex + 1) % mySelectedNoteCollection.getItems().size();
            ListView<String> listView = (ListView<String>) collectionPopup.getContent().getFirst();
            listView.getSelectionModel().clearAndSelect(highlightedIndex);

            resetIdlePause();
            System.out.println("Highlighted collection: "
                    + mySelectedNoteCollection.getItems().get(highlightedIndex));
        }
    }

    /**
     * Resets the idle pause for collection cycling.
     */
    private void resetIdlePause() {
        if (idlePause != null) {
            idlePause.stop();
        }

        idlePause = new PauseTransition(Duration.millis(1500));
        idlePause.setOnFinished(_ -> finalizeCollectionSelection());
        idlePause.play();
    }

    /**
     * Finalizes the selection procedure for collections.
     */
    private void finalizeCollectionSelection() {
        if (highlightedIndex >= 0) {
            mySelectedNoteCollection.getSelectionModel().select(highlightedIndex);
            state.updateSelectedNoteCollectionFromIndex(highlightedIndex);
            System.out.println("Finalized collection: "
                    + mySelectedNoteCollection.getSelectionModel().getSelectedItem());
        }

        highlightedIndex = -1;
        if (collectionPopup.isShowing()) {
            collectionPopup.hide();
        }

        if (idlePause != null) {
            idlePause.stop();
        }
    }

    /**
     * Custom mock popup for ChoiceBoxes.
     */
    private void setupPopup() {
        ListView<String> listView = new ListView<>(mySelectedNoteCollection.getItems());
        listView.getSelectionModel()
                .select(mySelectedNoteCollection.getSelectionModel().getSelectedIndex());

        int maxVisibleItems = mySelectedNoteCollection.getItems().size();
        int itemCount = Math.min(mySelectedNoteCollection.getItems().size(), maxVisibleItems);
        int additionalBottomPadding = 10;

        listView.setPrefHeight(itemCount * 24 + additionalBottomPadding);

        listView.setPrefWidth(mySelectedNoteCollection.getWidth() + 30);

        listView.setStyle("-fx-background-color: white; "
                + "-fx-border-color: lightgray; -fx-border-width: 1px; "
                + "-fx-padding: 0; -fx-background-insets: 0; "
                + "-fx-control-inner-background: white; "
                + "-fx-cell-size: 24px; "
                + "-fx-scroll-bar-policy: never; "
                + "-fx-scrollbar-thumb-horizontal: transparent; "
                + "-fx-scrollbar-track-horizontal: transparent; "
                + "-fx-scrollbar-thumb-vertical: transparent; "
                + "-fx-scrollbar-track-vertical: transparent;");

        listView.setMaxHeight(itemCount * 24 + additionalBottomPadding);
        listView.setMaxWidth(mySelectedNoteCollection.getWidth() + 30);

        collectionPopup.getContent().clear();
        collectionPopup.getContent().add(listView);

        collectionPopup.show(mySelectedNoteCollection,
                mySelectedNoteCollection
                        .localToScreen(mySelectedNoteCollection.getBoundsInLocal()).getMinX(),
                mySelectedNoteCollection
                        .localToScreen(mySelectedNoteCollection.getBoundsInLocal()).getMaxY());
    }

    /**
     * Cycles through the available languages in the language dropdown.
     * This method moves the selection to the next language in the list.
     * If the currently selected language is
     * the last one in the list, the selection wraps around to the first language.
     * It prints the newly selected language to the console.
     */
    private void cycleLanguage() {
        int currentIndex = myLanguages.getSelectionModel().getSelectedIndex();
        if (currentIndex == myLanguages.getItems().size() - 1) {
            myLanguages.getSelectionModel().select(0);
        } else {
            myLanguages.getSelectionModel().select(currentIndex + 1);
        }
        System.out.println("Moved to next item: "
                + myLanguages.getSelectionModel().getSelectedItem());
    }

    /**
     * Opens the collection editor interface.
     * This method calls the `showCollections()` method from the main controller to display
     * the collection editing interface.
     */
    private void editCollection() {
        mainCtrl.showCollections();
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

    /**
     * Displays a custom popup below the
     * ChoiceBox to mimic its dropdown behavior.
     *
     * @param choiceBox The ChoiceBox to expand.
     */
    private void showCustomPopup(ChoiceBox<String> choiceBox) {
        Popup popup = new Popup();

        ListView<String> listView = new ListView<>(choiceBox.getItems());
        listView.getSelectionModel().select(choiceBox.getSelectionModel().getSelectedIndex());

        double longestItemWidth = choiceBox.getItems().stream()
                .mapToDouble(this::computeTextWidth)
                .max()
                .orElse(0);
        double extraWidth = 50;
        int visibleItemCount = Math.min(choiceBox.getItems().size(), choiceBox.getItems().size());

        listView.setPrefWidth(Math.max(choiceBox.getWidth(), longestItemWidth) + extraWidth);
        listView.setPrefHeight(visibleItemCount * 24);

        listView.setStyle("-fx-padding: 0; -fx-background-insets: 0; -fx-background-color: white; "
                + "-fx-control-inner-background: white; "
                + "-fx-border-color: lightgray; -fx-border-width: 1px; "
                + "-fx-scrollbar-thumb-horizontal: transparent; "
                + "-fx-scrollbar-track-horizontal: transparent; "
                + "-fx-scrollbar-thumb-vertical: transparent; "
                + "-fx-scrollbar-track-vertical: transparent;");

        Platform.runLater(() -> listView.setStyle("-fx-scrollbar-thumb-horizontal: transparent; "
                + "-fx-scrollbar-track-horizontal: transparent; "
                + "-fx-scrollbar-thumb-vertical: transparent; "
                + "-fx-scrollbar-track-vertical: transparent;"));

        listView.setOnMouseClicked(_ -> {
            int selectedIndex = listView.getSelectionModel().getSelectedIndex();
            choiceBox.getSelectionModel().select(selectedIndex);
            popup.hide();
        });

        AtomicLong lastMouseMovement = new AtomicLong(System.currentTimeMillis());
        listView.setOnMouseMoved(_ -> lastMouseMovement.set(System.currentTimeMillis()));

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (System.currentTimeMillis() - lastMouseMovement.get() > 1500) {
                    popup.hide();
                    stop();
                }
            }
        }.start();

        popup.getContent().add(listView);

        popup.show(choiceBox,
                choiceBox.localToScreen(choiceBox.getBoundsInLocal()).getMinX(),
                choiceBox.localToScreen(choiceBox.getBoundsInLocal()).getMaxY());
    }

    /**
     * Sets up size of the custom popup and custom text.
     *
     * @param text the text that the popup is sized after
     * @return the helper text
     */
    private double computeTextWidth(String text) {
        Text helperText = new Text(text);
        new Scene(new Group(helperText));
        helperText.applyCss();
        return helperText.getLayoutBounds().getWidth();
    }

    /**
     * Sets the resourceBundle to the class.
     * This can be used for application components that needs to be initialized.
     *
     * @param resourceBundle the ResourceBundle that needs to be changed to.
     */
    public void setResourceBundle(ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
    }

    /**
     * Selects the available language that is chosen by the user.
     *
     * @param languageName is the selected language that the user has chosen.
     */
    private void switchLanguage(String languageName) {
        Locale currentLocale = languages.getOrDefault(languageName, Locale.ENGLISH);
        // Use appConfig instead of resourceBundle
        resourceBundle = ResourceBundle.getBundle("Internationalization.Text", currentLocale);
        markdown.setResourceBundle(resourceBundle);
        reloadScene(resourceBundle);
        updateWebView(null);
        mainCtrl.setResourceBundle(resourceBundle);
        state.setResourceBundle(resourceBundle);
    }

    /**
     * Updates language of the scene's components.
     *
     * @param newResourceBundle the new language bundle that updates the components
     */
    private void reloadScene(ResourceBundle newResourceBundle) {
        searchField.setPromptText(newResourceBundle.getString("Note.prompt"));
        editButton.setText(newResourceBundle.getString("Note.editBtn"));
        state.refreshObservableCollections();
        collectionText.setText(newResourceBundle.getString("Note.collection"));

        saveButton.setText(newResourceBundle.getString("Note.saveBtn"));
        tagsFilterText.setText(newResourceBundle.getString("Tags.filterText"));
        tagComboBox.setPromptText(newResourceBundle.getString("Tags.selectTags"));
        clearTagsButton.setText(newResourceBundle.getString("Tags.clearTags"));
        if (newTags == null || !newTags.isEmpty()) {
            selectedTagsLabel.setText(newResourceBundle.getString(
                    "Tags.selectedTags") + " " + newTags);
        } else {
            selectedTagsLabel.setText(newResourceBundle.getString("Tags.selectedTags"));
        }
        imageOptionsButton.setText(newResourceBundle.getString("Image.imageOptions"));
    }

    /**
     * Returns the correct and available language that the user chosen.
     * If it is not available it will return the standard (English) language.
     *
     * @param locale the language that is returned from the Map<String, Locale>
     * @return the language in String
     */
    private String getLanguageName(Locale locale) {
        for (Map.Entry<String, Locale> entry : languages.entrySet()) {
            if (entry.getValue().getLanguage().equals(locale.getLanguage())) {
                return entry.getKey();
            }
        }
        return "English";
    }

    /**
     * This method sets the application configs in the controller.
     * But this assumes the languages list has the same key value format as the getDisplayLanguage.
     *
     * @param appConfig the configs of the application.
     */
    public void setAppConfig(AppConfig appConfig) {
        int collectionIndex = state.getIndexFromCollection(appConfig.getSelectedCollection());
        myCollections.getSelectionModel().select(collectionIndex + 1);
    }

    /**
     * Filters the list of Notes based on the selected Tag.
     */
    @FXML
    private void handleTagSelection() {
        Tag selectedTag = tagComboBox.getSelectionModel().getSelectedItem();
        if (selectedTag == null)
            return;
        //simulate the tag selection process
        System.out.println("Selected tag: " + selectedTag.getTitle());
        updateComboBoxItems();
    }

    Set<Tag> newTags = new HashSet<>();

    /**
     * Updates the Tags in the ComboBox based on the selected Tag.
     */
    @FXML
    private void updateComboBoxItems() {
        Tag selectedTag = tagComboBox.getSelectionModel().getSelectedItem();
        newTags = new HashSet<>(state.getSelectedTags());
        if (newTags.contains(selectedTag)) {
            newTags.remove(selectedTag);
        } else {
            newTags.add(selectedTag);
        }
        if (!newTags.isEmpty())
            selectedTagsLabel.setText(resourceBundle.getString(
                    "Tags.selectedTags") + " " + newTags);
        state.setSelectedTags(newTags);
    }

    private final Popup tagPopup = new Popup();
    private int highlightedTagIndex = -1;

    /**
     * Method used to cycle tags by keyboard shortcuts.
     */
    @FXML
    private void cycleTags() {
        if (!tagComboBox.getItems().isEmpty())
            if (!tagPopup.isShowing()) {
                setupTagPopup();
            }

        int currentIndex = (highlightedTagIndex >= 0)
                ? highlightedTagIndex
                : tagComboBox.getSelectionModel().getSelectedIndex();

        highlightedTagIndex = (currentIndex + 1) % tagComboBox.getItems().size();

        ListView<String> listView = (ListView<String>) tagPopup.getContent().getFirst();
        listView.getSelectionModel().clearAndSelect(highlightedTagIndex);

        resetTagIdlePause();
        System.out.println("Highlighted tag: " + tagComboBox.getItems().get(highlightedTagIndex));
    }

    /**
     * Sets up the popup for cycling through tags.
     */
    private void setupTagPopup() {
        List<String> tagStrings = tagComboBox.getItems().stream()
                .map(Tag::toString)
                .collect(Collectors.toList());

        ListView<String> listView = new ListView<>(FXCollections.observableArrayList(tagStrings));
        listView.getSelectionModel().select(tagComboBox.getSelectionModel().getSelectedIndex());

        listView.setPrefHeight(Math.min(tagStrings.size(), tagComboBox.getItems().size()) * 24);
        listView.setPrefWidth(tagComboBox.getWidth() + 30);

        listView.setStyle("-fx-padding: 0; -fx-background-insets: 0; -fx-background-color: white; "
                + "-fx-control-inner-background: white; "
                + "-fx-border-color: lightgray; -fx-border-width: 1px; "
                + "-fx-scrollbar-thumb-horizontal: transparent; "
                + "-fx-scrollbar-track-horizontal: transparent; "
                + "-fx-scrollbar-thumb-vertical: transparent; "
                + "-fx-scrollbar-track-vertical: transparent;");

        Platform.runLater(() -> listView.setStyle("-fx-scrollbar-thumb-horizontal: transparent; "
                + "-fx-scrollbar-track-horizontal: transparent; "
                + "-fx-scrollbar-thumb-vertical: transparent; "
                + "-fx-scrollbar-track-vertical: transparent;"));

        tagPopup.getContent().clear();
        tagPopup.getContent().add(listView);

        tagPopup.show(tagComboBox,
                tagComboBox.localToScreen(tagComboBox.getBoundsInLocal()).getMinX(),
                tagComboBox.localToScreen(tagComboBox.getBoundsInLocal()).getMaxY());
    }

    /**
     * Resets the idle pause for tag cycling.
     */
    private void resetTagIdlePause() {
        if (idlePause != null) {
            idlePause.stop();
        }

        idlePause = new PauseTransition(Duration.millis(1500));
        idlePause.setOnFinished(_ -> finalizeTagSelection());
        idlePause.play();
    }

    /**
     * Finalizes the selection of the tag.
     */
    private void finalizeTagSelection() {
        if (highlightedTagIndex >= 0) {
            tagComboBox.getSelectionModel().select(highlightedTagIndex);
            System.out.println("Finalized tag: "
                    + tagComboBox.getSelectionModel().getSelectedItem());
        }

        highlightedTagIndex = -1;
        if (tagPopup.isShowing()) {
            tagPopup.hide();
        }

        if (idlePause != null) {
            idlePause.stop();
        }
    }

    /**
     * Clears all selected Tags and returns application to unfiltered state.
     */
    @FXML
    private void clearTags() {
        tagComboBox.getItems().clear();
        if (state.fetchAllTags() != null)
            tagComboBox.getItems().addAll(state.fetchAllTags());
        tagComboBox.setValue(null);
        state.setSelectedTags(new HashSet<>());
        newTags.clear();
        selectedTagsLabel.setText(resourceBundle.getString("Tags.selectedTags"));
    }

    /**
     * Fires the imageOptionsButton.
     */
    private void imageOptionsFire() {
        imageOptionsButton.fire();
    }

    /**
     * Closes the window and stops the program.
     */
    public void closeWindow() {
        if (scene != null && scene.getWindow() != null) {
            scene.getWindow().hide();
        }
    }
}
