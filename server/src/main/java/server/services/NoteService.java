package server.services;

import commons.Collection;
import commons.Note;

import java.util.List;
import java.util.Set;

public interface NoteService {
    /**
     * Finds a note by its ID.
     *
     * @param id the ID of the note to find
     * @return the note if found, or null if the note does not exist or the ID is invalid
     */
    Note findNote(long id);

    /**
     * Saves a note to the repository.
     *
     * @param note the note to save
     * @return the saved note, or null if validation fails
     */
    Note saveNote(Note note);

    /**
     * Deletes a note from the repository.
     *
     * @param note the note to be deleted
     * @return the deleted note, or null if the note is null or does not exist
     */
    Note deleteNote(Note note);

    /**
     * Retrieves all notes from the repository.
     *
     * @return a list of all notes in the repository
     */
    List<Note> findAllNotes();

    /**
     * Retrieves the collection associated with a specific note by the note's ID.
     *
     * @param id the ID of the note whose collection is to be retrieved
     * @return the collection associated with the note, or null if the note does not exist
     */
    Collection findNotesCollection(long id);

    /**
     * Removes a Tag from a Note.
     *
     * @param noteId  the ID of the note
     * @param tagName the name of the tag
     * @return the note without the tag
     */
    Note removeTagFromNote(Long noteId, String tagName);

    /**
     * Getter for the Tags of a Note.
     *
     * @param noteId the ID of the note
     * @return the set of tags of the note
     */
    Set<String> getTagsForNote(Long noteId);
}
