package client.utils;

import commons.Collection;
import commons.Note;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class UndoRedoManager {
    private final Stack<ApplicationStateMemento> undoStack = new Stack<>();
    private final Stack<ApplicationStateMemento> redoStack = new Stack<>();
    private final ApplicationState state;

    /**
     * Constructs the UndoRedoManager class.
     *
     * @param state the application state
     */
    @Inject
    public UndoRedoManager(ApplicationState state) {
        this.state = state;
    }

    /**
     * Saves the state.
     *
     * @param state the application state
     */
    public void saveState(ApplicationState state) {
        List<Note> notesCopy = new ArrayList<>();
        if (state.getNotes() != null) {
            for (Note note : state.getNotes()) {
                Note copyNote = new Note();
                copyNote.id = note.id;
                copyNote.title = note.title;
                copyNote.setContent(note.getContent());
                copyNote.collection = note.collection;
                notesCopy.add(copyNote);
            }
        }

        List<Collection> collectionsCopy = new ArrayList<>();
        if (state.getCollections() != null) {
            for (Collection collection : state.getCollections()) {
                Collection copyCollection = new Collection();
                copyCollection.id = collection.id;
                copyCollection.title = collection.title;
                copyCollection.serverUrl = collection.serverUrl;
                collectionsCopy.add(copyCollection);
            }
        }

        undoStack.push(new ApplicationStateMemento(notesCopy, collectionsCopy));
        redoStack.clear();
    }

    /**
     * Saves the current state only if the undo stack does not have the exact same state.
     *
     * @param state the application state
     */
    public void maybeSaveState(ApplicationState state) {
        if (undoStack.isEmpty() || !undoStack.peek().equals(
                new ApplicationStateMemento(state.getNotes(), state.getCollections()))) {
            saveState(state);
        }
    }

    /**
     * Returns the application state to 1 previous state.
     *
     * @return false if the undo stack is empty, otherwise true
     */
    public boolean undo() {
        redoStack.push(new ApplicationStateMemento(state.getNotes(), state.getCollections()));
        if (undoStack.isEmpty()) {
            return false;
        }
        state.restoreState(undoStack.peek().notes(), undoStack.peek().collections());
        undoStack.pop();
        return true;
    }

    /**
     * Returns the application state to 1 state ahead of the state when undo() was executed.
     *
     * @return false if the redo stack is empty, otherwise true
     */
    public boolean redo() {
        undoStack.push(new ApplicationStateMemento(state.getNotes(), state.getCollections()));
        if (redoStack.isEmpty()) {
            return false;
        }
        state.restoreState(redoStack.peek().notes(), redoStack.peek().collections());
        redoStack.pop();
        return true;
    }
}