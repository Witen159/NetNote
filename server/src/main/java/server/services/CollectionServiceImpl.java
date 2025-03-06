package server.services;

import commons.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import server.database.CollectionRepository;

import java.util.List;

@Service
public class CollectionServiceImpl implements CollectionService {
    private final CollectionRepository repo;

    /**
     * Constructs a new CollectionServiceImpl with the specified repository.
     *
     * @param collectionRepository the repository used for managing collections
     */
    @Autowired
    public CollectionServiceImpl(CollectionRepository collectionRepository) {
        this.repo = collectionRepository;
    }

    @Override
    public Collection findCollection(long id) {
        if (id < 0 || !repo.existsById(id)) {
            return null;
        }
        return repo.findById(id).orElse(null);
    }

    @Override
    public Collection saveCollection(Collection collection) {
        if (collection == null || collection.title == null
                || collection.serverUrl == null) {
            return null;
        }
        return repo.save(collection);
    }

    @Override
    public Collection deleteCollection(Collection collection) {
        if (collection == null || !repo.existsById(collection.id)) {
            return null;
        }
        repo.delete(collection);
        return collection;
    }

    @Override
    public List<Collection> findAllCollections() {
        return repo.findAll();
    }
}
