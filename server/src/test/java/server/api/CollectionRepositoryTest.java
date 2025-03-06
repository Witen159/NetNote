package server.api;

import commons.Collection;
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
import server.database.CollectionRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class CollectionRepositoryTest {
    @Autowired
    private CollectionRepository collectionRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private Collection collection1;
    private Collection collection2;

    @BeforeEach
    public void setUp() {
        collection1 = new Collection();
        collection1.title = "testTitle";
        collection1.serverUrl = "testUrl";

        collection2 = new Collection();
        collection2.title = "testTitle2";
        collection2.serverUrl = "testUrl2";

        collectionRepository.saveAll(List.of(collection1, collection2));
    }

    @AfterEach
    public void tearDown() {
        collectionRepository.deleteAll();
    }

    @Test
    public void testFindAll() {
        List<Collection> collections = collectionRepository.findAll();
        assertEquals(2, collections.size());
        Collection collection = collections.getFirst();
        assertEquals("testTitle", collection.title);
        assertEquals("testUrl", collection.serverUrl);
    }

    @Test
    public void testFindAllWithSort() {
        Sort sort = Sort.by(Sort.Direction.ASC, "title");
        List<Collection> sortedCollections = collectionRepository.findAll(sort);
        assertEquals("testTitle", sortedCollections.get(0).title);
        assertEquals("testTitle2", sortedCollections.get(1).title);
    }

    @Test
    public void testFindAllWithPagination() {
        Pageable pageable = PageRequest.of(0,1, Sort.by(Sort.Direction.ASC, "title"));
        Page<Collection> page = collectionRepository.findAll(pageable);
        assertEquals(1, page.getContent().size());
        assertEquals("testTitle", page.getContent().getFirst().title);
    }

    @Test
    public void testFindById() {
        Collection savedCollection = collectionRepository.findAll().getFirst();
        Optional<Collection> collection = collectionRepository.findById(savedCollection.id);
        assertTrue(collection.isPresent());
        Collection testcollection = collection.get();
        assertNotNull(testcollection);
        assertEquals("testTitle", testcollection.title);
        assertEquals("testUrl", testcollection.serverUrl);
    }

    @Test
    public void testSaveAll() {
        List<Collection> savedCollections = collectionRepository.findAll();
        assertEquals(2, savedCollections.size());
        assertTrue(savedCollections.contains(collection1));
        assertTrue(savedCollections.contains(collection2));
    }

    @Test
    public void testFlush() {
        collection1.serverUrl = "updatedUrl";
        collectionRepository.save(collection1);
        collectionRepository.flush();
        Collection updatedCollection = collectionRepository.findById(collection1.id)
                .orElseThrow(() -> new RuntimeException("Note not found"));
        assertEquals("updatedUrl", updatedCollection.serverUrl);
    }

    @Test
    public void testSaveAndFlush() {
        Collection newCollection = new Collection();
        newCollection.title = "testTitle";
        newCollection.serverUrl = "testUrl";
        Collection savedCollection = collectionRepository.saveAndFlush(newCollection);
        Collection retrievedCollection = collectionRepository.findById(savedCollection.id)
                .orElseThrow(() -> new RuntimeException("Note not found"));
        assertNotNull(retrievedCollection);
        assertEquals("testTitle", retrievedCollection.title);
        assertEquals("testUrl", retrievedCollection.serverUrl);
    }

    @Test
    public void testDelete() {
        assertTrue(collectionRepository.existsById(collection1.id));
        assertTrue(collectionRepository.existsById(collection2.id));
        collectionRepository.delete(collection1);
        assertFalse(collectionRepository.existsById(collection1.id));
        assertTrue(collectionRepository.existsById(collection2.id));
    }

    @Test
    public void testDeleteById() {
        assertTrue(collectionRepository.existsById(collection1.id));
        collectionRepository.deleteById(collection1.id);
        assertFalse(collectionRepository.existsById(collection1.id));
        assertTrue(collectionRepository.existsById(collection2.id));
    }

    @Test
    public void deleteAllInBatch() {
        assertEquals(2, collectionRepository.count());
        collectionRepository.deleteAllInBatch();
        assertEquals(0, collectionRepository.count());
    }

    @Test
    public void deleteAllByIdInBatch() {
        assertEquals(2, collectionRepository.count());
        List<Long> idsToDelete = List.of(collection1.id, collection2.id);
        collectionRepository.deleteAllByIdInBatch(idsToDelete);
        assertEquals(0, collectionRepository.count());
        assertFalse(collectionRepository.existsById(collection1.id));
        assertFalse(collectionRepository.existsById(collection2.id));
    }

    @Test
    public void testGetReferenceById() {
        Collection collectionReference = collectionRepository.getReferenceById(collection1.id);
        assertNotNull(collectionReference);
        assertTrue(entityManager.contains(collectionReference));
        String title = collectionReference.title;
        String url = collectionReference.serverUrl;
        assertEquals("testTitle", title);
        assertEquals("testUrl", url);
    }

    @Test
    public void testCount() {
        long initialCount = collectionRepository.count();
        assertEquals(2, collectionRepository.count());
        Collection newCollection = new Collection();
        newCollection.title = "newTitle";
        newCollection.serverUrl = "newUrl";
        collectionRepository.save(newCollection);
        long updatedCount = collectionRepository.count();
        assertEquals(initialCount + 1, updatedCount);
    }

    @Test
    public void testExists() {
        assertTrue(collectionRepository.existsById(collection1.id));
        assertTrue(collectionRepository.existsById(collection2.id));
        Long nonExistentId = 9999L;
        assertFalse(collectionRepository.existsById(nonExistentId));
    }
}
