package server.services;

import commons.Tag;

import java.util.List;

public interface TagService {
    /**
     * Retrieves all tags from the database.
     *
     * @return a list of all tags
     */
    List<Tag> findAllTags();

    /**
     * Searches for a Tag in the database, or creates it if not existent.
     *
     * @param tagName the title of the tag to search for
     * @return the tag, if found, or a new one with the TagName title
     */
    Tag findOrCreateTag(String tagName);

    /**
     * Saves a Tag to the database.
     *
     * @param tag the tag to save
     * @return the saved tag
     */
    Tag saveTag(Tag tag);

    /**
     * Finds a Tag by its ID.
     *
     * @param id the ID to search
     * @return the found tag, or null if not existent
     */
    Tag findTagById(String id);

    /**
     * Deletes a tag by its ID.
     *
     * @param id the ID to search
     * @return true if deletion was successful, false if no tag with searched ID
     */
    boolean deleteTagById(String id);

    /**
     * Removes a NoteID from a Tag (if the Note gets deleted or updated).
     *
     * @param noteID the ID of the deleted note
     */
    void removeNoteIDFromTags(Long noteID);
}
