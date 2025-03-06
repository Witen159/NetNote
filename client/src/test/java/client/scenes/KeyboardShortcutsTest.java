package client.scenes;

import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KeyboardShortcutsTest extends ApplicationTest {

    static {
        System.setProperty("testfx.robot", "glass");
        System.setProperty("glass.platform", "Monocle");
        System.setProperty("monocle.platform", "Headless");
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.text", "t2k");
    }

    private TextField searchField;
    private Button addButton;
    private Button removeButton;
    private Button refreshButton;
    private ListView<String> myNotesList;
    private ChoiceBox<String> myCollections;

    @Override
    public void start(Stage stage) {
        searchField = new TextField();
        addButton = new Button("Add");
        addButton.setOnAction(_ -> System.out.println("Add button clicked"));

        removeButton = new Button("Remove");
        removeButton.setOnAction(_ -> System.out.println("Remove button clicked"));

        refreshButton = new Button("Refresh");
        refreshButton.setOnAction(_ -> System.out.println("Refresh button clicked"));

        myNotesList = new ListView<>();
        myNotesList.setItems(FXCollections.observableArrayList("Note 1", "Note 2", "Note 3", "Note 4"));

        myCollections = new ChoiceBox<>();
        myCollections.setItems(FXCollections.observableArrayList("Collection 1", "Collection 2", "Collection 3", "Collection 4"));
        myCollections.getSelectionModel().select(1);

        VBox root = new VBox();
        root.getChildren().addAll(searchField, addButton, removeButton, refreshButton, myNotesList, myCollections);

        Scene scene = new Scene(root, 400, 400);
        stage.setScene(scene);
        stage.show();

        setScene(scene);
    }

    private void setScene(Scene scene) {
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.ESCAPE),
                () -> {
                    System.out.println("ESCAPE key pressed");
                    searchField.requestFocus();
                }
        );

        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN),
                () -> {
                    System.out.println("CTRL + N detected");
                    addButton.fire();
                }
        );

        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN),
                () -> {
                    System.out.println("CTRL + D detected");
                    removeButton.fire();
                }
        );

        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN),
                () -> {
                    System.out.println("CTRL + R detected");
                    refreshButton.fire();
                }
        );

        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.DOWN, KeyCombination.ALT_DOWN),
                () -> {
                    int currentIndex = myNotesList.getSelectionModel().getSelectedIndex();
                    if (currentIndex < myNotesList.getItems().size() - 1) {
                        myNotesList.getSelectionModel().select(currentIndex + 1);
                        myNotesList.scrollTo(currentIndex + 1);
                        System.out.println("Moved to next item: " + myNotesList.getSelectionModel().getSelectedItem());
                    }
                }
        );

        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.UP, KeyCombination.ALT_DOWN),
                () -> {
                    int currentIndex = myNotesList.getSelectionModel().getSelectedIndex();
                    if (currentIndex > 0) {
                        myNotesList.getSelectionModel().select(currentIndex - 1);
                        myNotesList.scrollTo(currentIndex - 1);
                        System.out.println("Moved to previous item: " + myNotesList.getSelectionModel().getSelectedItem());
                    }
                }
        );

        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.RIGHT, KeyCombination.ALT_DOWN),
                () -> {
                    int currentIndex = myCollections.getSelectionModel().getSelectedIndex();
                    if (currentIndex < myCollections.getItems().size() - 1) {
                        myCollections.getSelectionModel().select(currentIndex + 1);
                        System.out.println("Moved to next collection: " + myCollections.getSelectionModel().getSelectedItem());
                    }
                }
        );

        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.LEFT, KeyCombination.ALT_DOWN),
                () -> {
                    int currentIndex = myCollections.getSelectionModel().getSelectedIndex();
                    if (currentIndex > 0) {
                        myCollections.getSelectionModel().select(currentIndex - 1);
                        System.out.println("Moved to previous collection: " + myCollections.getSelectionModel().getSelectedItem());
                    }
                }
        );

        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.ENTER),
                () -> {
                    System.out.println("ENTER key pressed");
                    addButton.fire();
                }
        );

        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN),
                () -> System.out.println("CTRL + L detected")
        );

        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN),
                () -> System.out.println("CTRL + Q detected")
        );
    }

    @Test
    public void testEscapeShortcut() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            interact(() -> press(KeyCode.ESCAPE).release(KeyCode.ESCAPE));

            String consoleOutput = outContent.toString();
            assertTrue(consoleOutput.contains("ESCAPE key pressed"), "Expected 'ESCAPE key pressed' to be printed to the console.");

            interact(() -> assertTrue(searchField.isFocused(), "Expected searchField to gain focus."));
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    public void testCtrlNShortcut() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            interact(() -> press(KeyCode.CONTROL).press(KeyCode.N).release(KeyCode.N).release(KeyCode.CONTROL));

            String consoleOutput = outContent.toString();
            assertTrue(consoleOutput.contains("CTRL + N detected"),
                    "Expected 'CTRL + N detected' to be printed to the console.");
            assertTrue(consoleOutput.contains("Add button clicked"),
                    "Expected 'Add button clicked' to be printed to the console.");
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    public void testCtrlDShortcut() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            interact(() -> press(KeyCode.CONTROL).press(KeyCode.D).release(KeyCode.D).release(KeyCode.CONTROL));

            String consoleOutput = outContent.toString();
            assertTrue(consoleOutput.contains("CTRL + D detected"), "Expected 'CTRL + D detected' to be printed to the console.");
            assertTrue(consoleOutput.contains("Remove button clicked"), "Expected 'Remove button clicked' to be printed to the console.");
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    public void testCtrlRShortcut() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            interact(() -> press(KeyCode.CONTROL).press(KeyCode.R).release(KeyCode.R).release(KeyCode.CONTROL));

            String consoleOutput = outContent.toString();
            assertTrue(consoleOutput.contains("CTRL + R detected"), "Expected 'CTRL + R detected' to be printed to the console.");
            assertTrue(consoleOutput.contains("Refresh button clicked"), "Expected 'Refresh button clicked' to be printed to the console.");
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    public void testAltDownShortcut() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            interact(() -> {
                myNotesList.requestFocus();
                myNotesList.getSelectionModel().select(0);
            });

            assertEquals("Note 1", myNotesList.getSelectionModel().getSelectedItem(), "Initially, the first note should be selected.");

            interact(() -> press(KeyCode.ALT).press(KeyCode.DOWN).release(KeyCode.DOWN).release(KeyCode.ALT));

            interact(() -> assertEquals("Note 2", myNotesList.getSelectionModel().getSelectedItem(), "Expected the second note to be selected."));

            String consoleOutput = outContent.toString().trim();
            assertTrue(consoleOutput.contains("Moved to next item: Note 2"), "Expected the console output to indicate the next item was selected.");
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    public void testAltUpShortcut() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            interact(() -> {
                myNotesList.requestFocus();
                myNotesList.getSelectionModel().select(1);
            });

            assertEquals("Note 2", myNotesList.getSelectionModel().getSelectedItem(), "Initially, the second note should be selected.");

            interact(() -> press(KeyCode.ALT).press(KeyCode.UP).release(KeyCode.UP).release(KeyCode.ALT));

            interact(() -> assertEquals("Note 1", myNotesList.getSelectionModel().getSelectedItem(), "Expected the first note to be selected."));

            String consoleOutput = outContent.toString().trim();
            assertTrue(consoleOutput.contains("Moved to previous item: Note 1"), "Expected the console output to indicate the previous item was selected.");
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    public void testAltLeftShortcut() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            interact(() -> myCollections.requestFocus());

            assertEquals("Collection 2", myCollections.getSelectionModel().getSelectedItem(), "Initially, the second collection should be selected.");

            interact(() -> press(KeyCode.ALT).press(KeyCode.LEFT).release(KeyCode.LEFT).release(KeyCode.ALT));

            interact(() -> assertEquals("Collection 1", myCollections.getSelectionModel().getSelectedItem(), "Expected the first collection to be selected."));

            String consoleOutput = outContent.toString().trim();
            assertTrue(consoleOutput.contains("Moved to previous collection: Collection 1"), "Expected the console output to indicate the previous collection was selected.");
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    public void testAltRightShortcut() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            interact(() -> {
                myCollections.requestFocus();
                myCollections.getSelectionModel().select(0);
            });

            assertEquals("Collection 1", myCollections.getSelectionModel().getSelectedItem(), "Initially, the first collection should be selected.");

            interact(() -> press(KeyCode.ALT).press(KeyCode.RIGHT).release(KeyCode.RIGHT).release(KeyCode.ALT));

            interact(() -> assertEquals("Collection 2", myCollections.getSelectionModel().getSelectedItem(), "Expected the second collection to be selected."));

            String consoleOutput = outContent.toString().trim();
            assertTrue(consoleOutput.contains("Moved to next collection: Collection 2"), "Expected the console output to indicate the next collection was selected.");
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    public void testEnterShortcut() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            interact(() -> press(KeyCode.ENTER).release(KeyCode.ENTER));

            String consoleOutput = outContent.toString();
            assertTrue(consoleOutput.contains("ENTER key pressed"), "Expected 'ENTER key pressed' to be printed to the console.");
            assertTrue(consoleOutput.contains("Add button clicked"), "Expected 'Add button clicked' to be printed to the console.");
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    public void testCtrlLShortcut() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            interact(() -> press(KeyCode.CONTROL).press(KeyCode.L).release(KeyCode.L).release(KeyCode.CONTROL));

            String consoleOutput = outContent.toString();
            assertTrue(consoleOutput.contains("CTRL + L detected"), "Expected 'CTRL + L detected' to be printed to the console.");
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    public void testCtrlQShortcut() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            interact(() -> press(KeyCode.CONTROL).press(KeyCode.Q).release(KeyCode.Q).release(KeyCode.CONTROL));

            String consoleOutput = outContent.toString();
            assertTrue(consoleOutput.contains("CTRL + Q detected"), "Expected 'CTRL + Q detected' to be printed to the console.");
        } finally {
            System.setOut(originalOut);
        }
    }
}