package server.services;

import commons.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import server.database.CollectionRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CollectionServiceImplTest {

    private CollectionServiceImpl collectionService;

    @Mock
    private CollectionRepository collectionRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        collectionService = new CollectionServiceImpl(collectionRepository);
    }

    @Test
    void findCollection_ValidId_ReturnsCollection() {
        Collection mockCollection = new Collection();
        mockCollection.id = 1L;
        when(collectionRepository.existsById(1L)).thenReturn(true);
        when(collectionRepository.findById(1L)).thenReturn(Optional.of(mockCollection));

        Collection result = collectionService.findCollection(1L);

        assertNotNull(result);
        assertEquals(1L, result.id);
        verify(collectionRepository, times(1)).existsById(1L);
        verify(collectionRepository, times(1)).findById(1L);
    }

    @Test
    void findCollection_NegativeId_ReturnsNull() {
        Collection result = collectionService.findCollection(-1L);

        assertNull(result);
        verify(collectionRepository, never()).existsById(any(Long.class));
        verify(collectionRepository, never()).findById(any(Long.class));
    }

    @Test
    void findNote_InvalidId_ReturnsNull() {
        when(collectionRepository.existsById(1L)).thenReturn(false);

        Collection result = collectionService.findCollection(1L);

        assertNull(result);
        verify(collectionRepository, times(1)).existsById(1L);
        verify(collectionRepository, never()).findById(any(Long.class));
    }

    @Test
    void saveCollection_ValidCollection_ReturnsSavedCollection() {
        Collection validCollection = new Collection();
        validCollection.title = "Test Title";
        validCollection.serverUrl = "Test Server Url";
        when(collectionRepository.save(validCollection)).thenReturn(validCollection);

        Collection result = collectionService.saveCollection(validCollection);

        verify(collectionRepository, times(1)).save(validCollection);

        assertNotNull(result);
        assertEquals("Test Title", result.title);
        assertEquals("Test Server Url", result.serverUrl);
    }

    @Test
    void saveCollection_InvalidCollection_ReturnsNull() {
        Collection invalidCollection = new Collection();
        invalidCollection.title = null;
        invalidCollection.serverUrl = "Test Server Url";

        Collection result = collectionService.saveCollection(invalidCollection);

        assertNull(result);
        verify(collectionRepository, never()).save(any(Collection.class));
    }

    @Test
    void saveCollection_NullCollection_ReturnsNull() {
        Collection result = collectionService.saveCollection(null);

        assertNull(result);
        verify(collectionRepository, never()).save(any(Collection.class));
    }

    @Test
    void deleteCollection_ExistingCollection_DeletesAndReturnsCollection() {
        Collection existingCollection = new Collection();
        existingCollection.id = 1L;
        when(collectionRepository.existsById(1L)).thenReturn(true);

        Collection result = collectionService.deleteCollection(existingCollection);

        assertNotNull(result);
        assertEquals(1L, result.id);
        verify(collectionRepository, times(1)).existsById(1L);
        verify(collectionRepository, times(1)).delete(existingCollection);
    }

    @Test
    void deleteCollection_NonExistingCollection_ReturnsNull() {
        Collection nonExistingCollection = new Collection();
        nonExistingCollection.id = 2L;
        when(collectionRepository.existsById(2L)).thenReturn(false);

        Collection result = collectionService.deleteCollection(nonExistingCollection);

        assertNull(result);
        verify(collectionRepository, times(1)).existsById(2L);
        verify(collectionRepository, never()).delete(any(Collection.class));
    }

    @Test
    void deleteCollection_NullCollection_ReturnsNull() {
        Collection result = collectionService.deleteCollection(null);

        assertNull(result);
        verify(collectionRepository, never()).existsById(any(Long.class));
        verify(collectionRepository, never()).delete(any(Collection.class));
    }

    @Test
    void findAllCollections_ReturnsListOfCollections() {
        List<Collection> collections = new ArrayList<>();
        collections.add(new Collection());
        collections.add(new Collection());
        when(collectionRepository.findAll()).thenReturn(collections);

        List<Collection> result = collectionService.findAllCollections();

        verify(collectionRepository, times(1)).findAll();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void findAllCollections_NoCollections_ReturnsEmptyList() {
        List<Collection> result = collectionService.findAllCollections();

        verify(collectionRepository, times(1)).findAll();

        assertNotNull(result);
        assertEquals(0, result.size());
    }
}
