package server.services;

import commons.Collection;
import commons.Note;
import commons.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import server.database.NoteRepository;
import server.database.TagRepository;

import java.util.List;
import java.util.Set;


@Service
public class NoteServiceImpl implements NoteService {
    private final NoteRepository repo;
    private final TagService tagService;

    /**
     * Constructs a new NoteServiceImpl with the specified repository.
     *
     * @param noteRepository the repository used for managing notes
     * @param tagService     the tag service used to parse and save tags of a note
     */
    @Autowired
    public NoteServiceImpl(NoteRepository noteRepository, TagService tagService) {
        this.repo = noteRepository;
        this.tagService = tagService;
    }

    @Override
    public Note findNote(long id) {
        if (id < 0 || !repo.existsById(id)) {
            return null;
        }
        return repo.findById(id).orElse(null);
    }

    @Override
    public Note saveNote(Note note) {
        if (note.title == null || note.getContent() == null) {
            return null;
        }
        // refreshes tag list by removing previous tags from this note
        // then adding the new tags
        tagService.removeNoteIDFromTags(note.id);
        for (String tagName : note.getTags()) {
            Tag tag = tagService.findOrCreateTag(tagName);
            if (tag != null)
                tag.noteIDs.add(note.id);
        }
        return repo.save(note);
    }

    @Override
    public Note deleteNote(Note note) {
        if (note == null || !repo.existsById(note.id)) {
            return null;
        }
        tagService.removeNoteIDFromTags(note.id);
        repo.delete(note);
        return note;
    }

    @Override
    public List<Note> findAllNotes() {
        return repo.findAll();
    }

    @Override
    public Collection findNotesCollection(long id) {
        var note = findNote(id);
        if (note == null) {
            return null;
        }
        return note.collection;
    }

    @Autowired
    public NoteRepository noteRepository;

    @Autowired
    public TagRepository tagRepository;

    @Override
    public Note removeTagFromNote(Long noteId, String tagName) {
        Note note = noteRepository.findById(noteId)
                .orElse(null);

        Tag tag = tagRepository.findById(tagName)
                .orElse(null);

        assert note != null;
        note.getTags().remove(tag);
        return noteRepository.save(note);
    }

    @Override
    public Set<String> getTagsForNote(Long noteId) {
        Note note = noteRepository.findById(noteId)
                .orElse(null);

        assert note != null;
        return note.getTags();
    }
}
