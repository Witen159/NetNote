package server.api;

import commons.Tag;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import server.database.TagRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class TagRepositoryTest {
    @Autowired
    private TagRepository tagRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private Tag tag1;
    private Tag tag2;

    @BeforeEach
    public void setUp() {
        tag1 = new Tag();
        tag1.title = "testTitle";

        tag2 = new Tag();
        tag2.title = "testTitle2";

        tagRepository.saveAll(List.of(tag1, tag2));
    }

    @AfterEach
    public void tearDown() {
        tagRepository.deleteAll();
    }

    @Test
    public void testFindAll() {
        List<Tag> tags = tagRepository.findAll();
        assertEquals(2, tags.size());
        Tag tag = tags.getFirst();
        assertEquals("testTitle", tag.title);
    }

    @Test
    public void testFindAllWithSort() {
        Sort sort = Sort.by(Sort.Direction.ASC, "title");
        List<Tag> sortedTags = tagRepository.findAll(sort);
        assertEquals("testTitle", sortedTags.get(0).title);
        assertEquals("testTitle2", sortedTags.get(1).title);
    }

    @Test
    public void testFindAllWithPagination() {
        Pageable pageable = PageRequest.of(0,1, Sort.by(Sort.Direction.ASC, "title"));
        Page<Tag> page = tagRepository.findAll(pageable);
        assertEquals(1, page.getContent().size());
        assertEquals("testTitle", page.getContent().getFirst().title);
    }

    @Test
    public void testFindById() {
        Tag savedTag = tagRepository.findAll().getFirst();
        Optional<Tag> tag = tagRepository.findById(savedTag.title);
        assertTrue(tag.isPresent());
        Tag testNote = tag.get();
        assertNotNull(testNote);
        assertEquals("testTitle", testNote.title);
    }

    @Test
    public void testSaveAll() {
        List<Tag> savedTags = tagRepository.findAll();
        assertEquals(2, savedTags.size());
        assertTrue(savedTags.contains(tag1));
        assertTrue(savedTags.contains(tag2));
    }

    @Test
    public void testFlush() {
        tag1.title = "updatedTitle";
        tagRepository.save(tag1);
        tagRepository.flush();
        Tag updatedTag = tagRepository.findById(tag1.title)
                .orElseThrow(() -> new RuntimeException("Tag not found"));
        assertEquals("updatedTitle", updatedTag.title);
    }

    @Test
    public void testSaveAndFlush() {
        Tag newTag = new Tag();
        newTag.title = "testTitle";
        Tag savedTag = tagRepository.saveAndFlush(newTag);
        Tag retrievedTag = tagRepository.findById(savedTag.title)
                .orElseThrow(() -> new RuntimeException("Tag not found"));
        assertNotNull(retrievedTag);
        assertEquals("testTitle", retrievedTag.title);
    }

    @Test
    public void testDelete() {
        assertTrue(tagRepository.existsById(tag1.title));
        assertTrue(tagRepository.existsById(tag2.title));
        tagRepository.delete(tag1);
        assertFalse(tagRepository.existsById(tag1.title));
        assertTrue(tagRepository.existsById(tag2.title));
    }

    @Test
    public void testDeleteById() {
        assertTrue(tagRepository.existsById(tag1.title));
        tagRepository.deleteById(tag1.title);
        assertFalse(tagRepository.existsById(tag1.title));
        assertTrue(tagRepository.existsById(tag2.title));
    }

    @Test
    public void deleteAllInBatch() {
        assertEquals(2, tagRepository.count());
        tagRepository.deleteAllInBatch();
        assertEquals(0, tagRepository.count());
    }

    @Test
    public void deleteAllByIdInBatch() {
        assertEquals(2, tagRepository.count());
        List<String> idsToDelete = List.of(tag1.title, tag2.title);
        tagRepository.deleteAllByIdInBatch(idsToDelete);
        assertEquals(0, tagRepository.count());
        assertFalse(tagRepository.existsById(tag1.title));
        assertFalse(tagRepository.existsById(tag2.title));
    }

    @Test
    public void testGetReferenceById() {
        Tag tagReference = tagRepository.getReferenceById(tag1.title);
        assertNotNull(tagReference);
        assertTrue(entityManager.contains(tagReference));
        String title = tagReference.title;
        assertEquals("testTitle", title);
    }

    @Test
    public void testCount() {
        long initialCount = tagRepository.count();
        assertEquals(2, tagRepository.count());
        Tag newTag = new Tag();
        newTag.title = "newTitle";
        tagRepository.save(newTag);
        long updatedCount = tagRepository.count();
        assertEquals(initialCount + 1, updatedCount);
    }

}
