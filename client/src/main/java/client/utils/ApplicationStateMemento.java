package client.utils;

import commons.Collection;
import commons.Note;

import java.util.ArrayList;
import java.util.List;

public record ApplicationStateMemento(List<Note> notes, List<Collection> collections) {
    /**
     * Constructs the ApplicationStateMemento class.
     *
     * @param notes       the list of all notes
     * @param collections the list of all collections
     */
    public ApplicationStateMemento(List<Note> notes, List<Collection> collections) {
        this.notes = new ArrayList<>(notes);
        this.collections = new ArrayList<>(collections);
    }

    /**
     * Returns the list of notes.
     *
     * @return the list of notes
     */
    @Override
    public List<Note> notes() {
        return new ArrayList<>(notes);
    }

    /**
     * Returns the list of collections.
     *
     * @return the list of collections
     */
    @Override
    public List<Collection> collections() {
        return new ArrayList<>(collections);
    }

}
