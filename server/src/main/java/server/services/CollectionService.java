package server.services;


import commons.Collection;

import java.util.List;

public interface CollectionService {
    /**
     * Finds a collection by its ID.
     *
     * @param id the ID of the collection to find
     * @return the collection if found,
     * or null if the collection does not exist or the ID is invalid
     */
    Collection findCollection(long id);

    /**
     * Saves a collection to the repository.
     *
     * @param collection the collection to save
     * @return the saved collection, or null if validation fails
     */
    Collection saveCollection(Collection collection);

    /**
     * Deletes a collection from the repository.
     *
     * @param collection the collection to be deleted
     * @return the deleted collection, or null if the collection is null or does not exist
     */
    Collection deleteCollection(Collection collection);

    /**
     * Retrieves all collections from the repository.
     *
     * @return a list of all collections in the repository
     */
    List<Collection> findAllCollections();
}
