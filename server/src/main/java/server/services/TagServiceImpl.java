package server.services;

import commons.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import server.database.TagRepository;

import java.util.List;

@Service
public class TagServiceImpl implements TagService {
    private final TagRepository tagRepository;

    /**
     * Constructs a new TagServiceImpl with the specified repository.
     *
     * @param repo the repository used for managing tags
     */
    @Autowired
    public TagServiceImpl(TagRepository repo) {
        this.tagRepository = repo;
    }

    @Override
    public List<Tag> findAllTags() {
        return tagRepository.findAll();
    }

    @Override
    public Tag findOrCreateTag(String tagName) {
        if (tagName == null || tagName.trim().isEmpty()) {
            throw new IllegalArgumentException("Tag name cannot be null or empty.");
        }

        List<Tag> allTags = tagRepository.findAll();

        for (Tag tag : allTags) {
            if (tag.getTitle().equals(tagName)) {
                return tag;
            }
        }

        Tag newTag = new Tag(tagName);
        return tagRepository.save(newTag);
    }

    @Override
    public Tag saveTag(Tag tag) {
        if (tag.getTitle() == null) {
            return null;
        }
        return tagRepository.save(tag);
    }

    @Override
    public Tag findTagById(String id) {
        return tagRepository.findById(id).orElse(null);
    }

    @Override
    public boolean deleteTagById(String id) {
        if (!tagRepository.existsById(id)) {
            return false;
        }
        tagRepository.deleteById(id);
        return true;
    }

    @Override
    public void removeNoteIDFromTags(Long noteID) {
        for (Tag tag : findAllTags()) {
            tag.noteIDs.remove(noteID);
            if (tag.noteIDs.isEmpty())
                tagRepository.delete(tag);
        }
    }
}
