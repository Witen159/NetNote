package server.api;

import commons.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.services.TagService;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {
    private final TagService tagService;

    /**
     * Constructs a new TagController with the specified tag service.
     *
     * @param tagService the service used for managing tags
     */
    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    /**
     * Retrieves all tags.
     *
     * @return a response entity containing the list of all tags
     */
    @GetMapping
    public ResponseEntity<List<Tag>> getAllTags() {
        List<Tag> tags = tagService.findAllTags();
        if (tags.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(tags);
    }

    /**
     * Creates a new tag.
     *
     * @param tag the tag to be created
     * @return a response entity containing the created tag
     */
    @PostMapping
    public ResponseEntity<Tag> createTag(@RequestBody Tag tag) {
        Tag createdTag = tagService.saveTag(tag);
        return ResponseEntity.ok(createdTag);
    }

    /**
     * Retrieves a specific tag by its ID.
     *
     * @param id the ID of the tag to retrieve
     * @return a response entity containing the tag if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Tag> getTagById(@PathVariable String id) {
        Tag tag = tagService.findTagById(id);
        if (tag == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(tag);
    }

    /**
     * Deletes a tag by its ID.
     *
     * @param id the ID of the tag to delete
     * @return a response entity indicating the result of the operation
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable String id) {
        boolean deleted = tagService.deleteTagById(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
