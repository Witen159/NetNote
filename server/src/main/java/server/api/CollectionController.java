package server.api;

import commons.Collection;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.services.CollectionService;

import java.util.List;

@RestController
@RequestMapping("/api/collections")
public class CollectionController {
    private final CollectionService service;

    /**
     * Constructs a new CollectionController with the specified service.
     *
     * @param service the service used for managing collections
     */
    public CollectionController(CollectionService service) {
        this.service = service;
    }

    /**
     * Retrieves all collections.
     *
     * @return a list of all collections, or null if no collections exist
     */
    @GetMapping(path = {"", "/"})
    public List<Collection> getAll() {
        return service.findAllCollections();
    }

    /**
     * Retrieves a specific collection by its ID.
     *
     * @param id the ID of the collection to retrieve
     * @return a response entity containing the collection if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Collection> getById(@PathVariable("id") long id) {
        var collection = service.findCollection(id);
        if (collection == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(collection);
    }

    /**
     * Saves a new collection.
     *
     * @param collection the collection to be saved, provided in the request body
     * @return a response entity containing the saved collection
     */
    @PostMapping(path = {"", "/"})
    public ResponseEntity<Collection> save(@RequestBody Collection collection) {
        var saveCollection = service.saveCollection(collection);
        if (saveCollection == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(saveCollection);
    }

    /**
     * Updates an existing collection by its ID.
     *
     * @param id         the ID of the collection to update, provided in the path
     * @param collection the updated collection data, provided in the request body
     * @return a response entity containing the updated collection
     */
    @PutMapping("/{id}")
    public ResponseEntity<Collection> update(@PathVariable("id") long id,
                                             @RequestBody Collection collection) {
        var updatedCollection = service.findCollection(id);
        if (collection == null || collection.title == null
                || collection.serverUrl == null
                || updatedCollection == null) {
            return ResponseEntity.badRequest().build();
        }

        updatedCollection.title = collection.title;
        updatedCollection.serverUrl = collection.serverUrl;
        return ResponseEntity.ok(service.saveCollection(updatedCollection));
    }

    /**
     * Deletes a collection based on the provided collection object.
     *
     * @param collection the collection to be deleted, provided in the request body
     * @return a response entity containing the deleted collection
     */
    @DeleteMapping(path = {"", "/"})
    public ResponseEntity<Collection> delete(@RequestBody Collection collection) {
        var deletedCollection = service.deleteCollection(collection);
        if (deletedCollection == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(deletedCollection);
    }


    /**
     * Deletes a collection based on its ID.
     *
     * @param id the ID of the collection to be deleted, provided in the path
     * @return a response entity containing the deleted collection
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Collection> delete(@PathVariable("id") long id) {
        var collection = service.findCollection(id);
        if (collection == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(service.deleteCollection(collection));
    }
}
