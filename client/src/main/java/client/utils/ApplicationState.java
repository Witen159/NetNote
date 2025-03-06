package client.utils;

import com.google.inject.Inject;
import commons.Collection;
import commons.*;
import jakarta.websocket.DeploymentException;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.stage.StageStyle;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.*;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ApplicationState {
    private final ServerUtils server;

    private List<Collection> collections; // all collections
    private final ObservableList<String> observableCollections; // all collection titles
    // all collection titles, including the "All" collection (which is not actually a collection)
    private final ObservableList<String> observableCollectionsWithAll;
    private Collection selectedCollection; // null means "All" is selected
    private Collection defaultCollection;

    private List<Note> notes; // all notes
    private final List<Note> filteredNotes; // filtered subset of `notes`
    private final ObservableList<String> observableNotes; // title of `filteredNotes`
    private Note selectedNote;
    private Note lastNonNullSelectedNote;
    private String searchQuery;

    private ResourceBundle resourceBundle;
    private final WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
    private WebSocketStompClient stompClient;
    private StompSession stompSession;
    private final Map<Long, Runnable> autoSyncQueue;


    private final Set<Tag> tags; // all tags
    private Set<Tag> filteredTags; // filtered subset of tags
    private Set<Tag> selectedTags; // selected tags to filter by

    private final String defaultCollectionIdKey = "defaultCollectionId";
    private final Preferences preferences = Preferences.userNodeForPackage(ApplicationState.class);

    /**
     * Instantiates a state storage for the application.
     *
     * @param server The server utils
     */
    @Inject
    public ApplicationState(ServerUtils server) {
        this.server = server;
        collections = new ArrayList<>();
        observableCollections = FXCollections.observableArrayList();
        observableCollectionsWithAll = FXCollections.observableArrayList();

        notes = new ArrayList<>();
        filteredNotes = new ArrayList<>();
        observableNotes = FXCollections.observableArrayList();

        tags = new HashSet<>();
        selectedTags = new HashSet<>();
        filteredTags = new HashSet<>();

        searchQuery = "";

        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                handleAutoContentSyncTimer();
            }
        }, 0, 3000);

        autoSyncQueue = new HashMap<>();
    }

    /**
     * Make sure all note content is saved every 3s.
     */
    public void handleAutoContentSyncTimer() {
        autoSyncQueue.forEach((_, sync) -> sync.run());

        autoSyncQueue.clear();
    }

    /**
     * Refreshes state data from the server.
     */
    public void refresh() {
        // make sure the most recent edits are saved before refreshing
        handleAutoContentSyncTimer();

        collections.clear();
        collections.addAll(server.getAllCollections());

        notes.clear();
        notes.addAll(server.getAllNotes());

        long defaultCollectionId = loadDefaultCollectionId();

        defaultCollection = collections.stream()
                .filter(c -> c.id == defaultCollectionId)
                .findFirst()
                .orElse(null);

        if (defaultCollection != null) {
            System.out.println("Default collection restored: " + defaultCollection.title);
        } else {
            System.out.println("No default collection set.");
        }

        tags.clear();
        List<Tag> serverTags = server.getAllTags();
        if (serverTags != null) {
            tags.addAll(serverTags);
        }

        refreshObservableCollections();
        refreshObservableNotes();
    }

    /**
     * Returns the list of all notes.
     *
     * @return notes
     */
    public List<Note> getNotes() {
        return notes;
    }

    /**
     * Returns the list of all collections.
     *
     * @return collections
     */
    public List<Collection> getCollections() {
        return collections;
    }

    /**
     * Returns observable list of collection titles, to be used for the edit collections window.
     *
     * @return Observable list of collection titles
     */
    public ObservableList<String> getObservableCollections() {
        return observableCollections;
    }

    /**
     * Returns observable list of collection titles, to be used for the choice boxes.
     * Includes "All".
     *
     * @return Observable list of collection titles
     */
    public ObservableList<String> getObservableCollectionsWithAll() {
        return observableCollectionsWithAll;
    }

    /**
     * Updates observable collection title list to match the collections list.
     *
     * @param collection The selected collection, or null if "All" is selected
     */
    public void refreshObservableCollections(Collection collection) {
        // return titles (+ "(Default)" if it is the default collection)
        List<String> collectionTitles = collections.stream()
                .map(c -> c == defaultCollection ? c.title + " ("
                        + resourceBundle.getString("EditCollection.default") + ")" : c.title)
                .toList();

        String collectionButtonText;
        if (collection == null) {
            // prepend "All", to be used in the choice boxes
            collectionButtonText = this.resourceBundle.getString("State.all");
        } else {
            collectionButtonText = collection.title;
        }
        List<String> collectionTitlesWithAll =
                Stream.concat(Stream.of(collectionButtonText), collectionTitles.stream()).toList();

        observableCollections.setAll(collectionTitles);
        observableCollectionsWithAll.setAll(collectionTitlesWithAll);
    }

    /**
     * Updates observable collection title list to match the collections list.
     * This method is overloaded to handle the "All" collection,
     * where other methods can call it without putting a parameter.
     */
    public void refreshObservableCollections() {
        refreshObservableCollections(null);
    }

    /**
     * Returns observable list of note titles, to be used for the list view.
     *
     * @return Observable list of note titles
     */
    public ObservableList<String> getObservableNotes() {
        return observableNotes;
    }

    /**
     * Updates observable note title list to match the notes list.
     * Also makes sure to filter based on current collection and on the search query.
     * Also makes sure to filter based on tags selected by user.
     */
    public void refreshObservableNotes() {
        if (selectedCollection != null) {
            List<Note> collectionFiltered = notes.stream()
                    .filter(n -> Objects.equals(n.collection, selectedCollection)).toList();

            filteredNotes.clear();
            filteredNotes.addAll(collectionFiltered);
        } else {
            filteredNotes.clear();
            filteredNotes.addAll(notes);
        }

        List<Note> searchFiltered = filteredNotes.stream()
                .filter(n -> n.title.toLowerCase().contains(searchQuery.toLowerCase())
                        || n.getContent().toLowerCase().contains(searchQuery.toLowerCase())
                ).toList();

        filteredNotes.clear();
        filteredNotes.addAll(searchFiltered);

        if (!selectedTags.isEmpty()) {
            List<Note> tagFilteredNotes = new ArrayList<>();
            for (Note note : filteredNotes) {
                boolean isInAllTags = true;
                for (Tag tag : selectedTags) {
                    if (!tag.noteIDs.contains(note.id)) {
                        isInAllTags = false;
                        break;
                    }
                }
                if (isInAllTags) {
                    tagFilteredNotes.add(note);
                }
            }

            filteredNotes.clear();
            filteredNotes.addAll(tagFilteredNotes);
        }

        List<String> noteTitles = filteredNotes.stream().map(n -> n.title).toList();
        observableNotes.setAll(noteTitles);
    }

    /**
     * Restores the application to 1 previous state.
     *
     * @param restoredNotes       the list of notes from 1 previous state
     * @param restoredCollections the list of collections from 1 previous state
     */
    public void restoreState(List<Note> restoredNotes, List<Collection> restoredCollections) {
        this.notes = restoredNotes;
        this.collections = restoredCollections;
        refreshObservableNotes();
        refreshObservableCollections();
    }

    /**
     * Gets note data at a filtered index.
     *
     * @param index The filtered index of the note
     * @return Note data
     */
    public Note getNoteFromFilteredIndex(int index) {
        if (index < 0 || index >= filteredNotes.size()) return null;

        return filteredNotes.get(index);
    }

    /**
     * Sets the current selected note from an index of a filtered note list.
     *
     * @param index The current selected note index
     */
    public void setSelectedNoteFromFilteredIndex(int index) {
        if (index >= filteredNotes.size()) return;
        if (index < 0) {
            selectedNote = null;
            return;
        }

        selectedNote = filteredNotes.get(index);
        lastNonNullSelectedNote = selectedNote;
    }

    /**
     * Get index from the last selected note.
     *
     * @return Filtered index
     */
    public int getFilteredIndexFromLastNonNullSelectedNote() {
        if (lastNonNullSelectedNote == null) return -1;

        // find matching note with the same id
        Note matchingNote = filteredNotes.stream()
                .filter(n -> n.id == lastNonNullSelectedNote.id).findFirst().orElse(null);

        return filteredNotes.indexOf(matchingNote);
    }

    /**
     * Last non-null selected note.
     *
     * @return Note
     */
    public Note getLastNonNullSelectedNote() {
        return lastNonNullSelectedNote;
    }

    /**
     * Adds a new note to the state with the correct collection and updates observable list.
     *
     * @param selectedCollectionIndex The current selected collection index
     */
    public void addNote(int selectedCollectionIndex) {
        if (!isServerAvailable()) {
            showServerAlertWarning("Alert.unableToAddNote");
            return;
        }

        if (defaultCollection == null) {
            addDefaultCollection();
        }
        Note newNote = getNote(selectedCollectionIndex);

        Note savedNote = server.addNote(newNote);

        notes.add(savedNote);
        refreshObservableNotes();

        showServerAlertInformation("State.noteAdded");

        if (stompSession != null) {
            stompSession.send("/app/synchronize",
                    new Synchronization(SynchronizationType.CREATE,
                            savedNote.id, savedNote.title, savedNote.getContent()));
        }
    }

    /**
     * Gets the note.
     *
     * @param selectedCollectionIndex the index of the collection
     * @return the note
     */
    private Note getNote(int selectedCollectionIndex) {
        Collection noteCollection = selectedCollectionIndex < 0 ? defaultCollection
                : collections.get(selectedCollectionIndex);

        String newTitle = "New note";
        int newTitleNumber = getNewTitleNumber(newTitle);

        if (newTitleNumber != 0) {
            newTitle = newTitle + " " + newTitleNumber;
        }

        return new Note(newTitle, "", noteCollection);
    }

    /**
     * Returns the next number so that collections are automatically numbered.
     *
     * @param newTitle the new title of the collection
     * @return the number
     */
    private int getNewTitleNumber(String newTitle) {
        boolean titleExists = false;
        int newTitleNumber = 0;

        for (Note note : notes) {
            if (note.title.equals(newTitle)) {
                titleExists = true;
                break;
            }
        }

        while (titleExists) {
            titleExists = false;
            newTitleNumber++;
            for (Note note : notes) {
                if (note.title.equals(newTitle + " " + newTitleNumber)) {
                    titleExists = true;
                    break;
                }
            }
        }
        return newTitleNumber;
    }

    /**
     * Updates note title of selected note in state and updates observable list.
     *
     * @param title The updated title of the note
     */
    public void updateSelectedNoteTitle(String title) {
        if (!isServerAvailable()) {
            showServerAlertWarning("Alert.unableToUpdateNote");
            return;
        }

        if (selectedNote == null) return;
        if (title == null || selectedNote.title.equals(title)) return;

        title = title.trim();
        for (Note note : notes) {
            if (note.title.equals(title) && !note.equals(selectedNote)
                    && note.collection.equals(selectedNote.collection)) {
                showServerAlertWarning("State.noteTitleExists");
                return;
            }
        }

        selectedNote.title = title;
        Note updatedNote = server.updateNote(selectedNote.id, selectedNote);
        if (updatedNote == null) {
            showServerAlertWarning("Alert.serverNotAvailable");
            return;
        }

        refreshObservableNotes();

        if (stompSession != null) {
            stompSession.send("/app/synchronize",
                    new Synchronization(SynchronizationType.UPDATE,
                            updatedNote.id, updatedNote.title, updatedNote.getContent()));
        }
    }

    /**
     * Updates note content of selected note in state.
     *
     * @param content The updated content of the note
     */
    public void updateSelectedNoteContent(String content) {
        if (selectedNote == null) return;
        if (content == null || selectedNote.getContent().equals(content)) return;

        selectedNote.setContent(content);

        // a copy needs to be made, otherwise the autosave will wait 2 seconds and then
        // use a more recent copy of this.selectedNote, which messes everything up
        Note copyNote = new Note();
        copyNote.id = selectedNote.id;
        copyNote.title = selectedNote.title;
        copyNote.setContent(selectedNote.getContent());
        copyNote.collection = selectedNote.collection;

        // put the update to the server in the queue
        autoSyncQueue.put(copyNote.id, () -> {
            Note updatedNote = server.updateNote(copyNote.id, copyNote);
            if (updatedNote == null) {
                showServerAlertWarning("Alert.serverNotAvailable");
                return;
            }

            if (stompSession != null) {
                stompSession.send("/app/synchronize",
                        new Synchronization(SynchronizationType.UPDATE,
                                copyNote.id, copyNote.title, copyNote.getContent()));
            }
        });
    }

    /**
     * Deletes selected note in state and updates observable list.
     */
    public void deleteSelectedNote() {
        if (!isServerAvailable()) {
            showServerAlertWarning("Alert.unableToDeleteNote");
            return;
        }

        if (selectedNote == null) return;

        // remove note from sync queue first
        autoSyncQueue.remove(selectedNote.id);

        Note deletedNote = server.deleteNoteById(selectedNote.id);
        if (deletedNote == null) {
            showServerAlertWarning("Alert.serverNotAvailable");
            return;
        }
        notes.remove(selectedNote);
        refreshObservableNotes();

        if (stompSession != null) {
            stompSession.send("/app/synchronize",
                    new Synchronization(SynchronizationType.DELETE,
                            deletedNote.id, deletedNote.title, deletedNote.getContent()));
        }
    }

    /**
     * Get index of collection in the list from a collection object.
     *
     * @param collection A collection object
     * @return The index of the collection in the list
     */
    public int getIndexFromCollection(Collection collection) {
        // the collection stored in Note objects isn't the same collection
        // as those stored in state, but it is a copy
        // so get the non-copied version based on the collection id first
        if (collection == null) return -1;
        Collection actualCollection = collections.stream()
                .filter(c -> c.id == collection.id).findFirst().orElse(null);

        return collections.indexOf(actualCollection);
    }

    /**
     * Get collection data from the index of the collection in the list.
     *
     * @param index The index of the collection in the list
     * @return Collection data
     */
    public Collection getCollectionFromIndex(int index) {
        if (index < 0) return null;

        return collections.get(index);
    }

    /**
     * Set collection filter from the index of the collection in the list.
     *
     * @param selectedCollectionIndex Index of collection
     */
    public void setSelectedCollectionFromIndex(int selectedCollectionIndex) {
        if (selectedCollectionIndex < 0) {
            selectedCollection = null;
        } else {
            selectedCollection = collections.get(selectedCollectionIndex);
        }

        refreshObservableNotes();
    }

    /**
     * Used to add a default collection.
     */
    private void addDefaultCollection() {
        String servers = server.getServer();
        Collection newCollection =
                new Collection("Default Collection", servers + "collections/default");
        Collection savedCollection = server.addCollection(newCollection);

        collections.add(savedCollection);
        setDefaultCollectionFromIndex(collections.size() - 1);
        refreshObservableCollections();
        refreshObservableNotes();
    }

    /**
     * Adds a new collection to the state and updates observable list.
     */
    public void addCollection() {
        if (!isServerAvailable()) {
            showServerAlertWarning("Alert.unableToAddCollection");
            return;
        }
        if (collections.isEmpty()) {
            addDefaultCollection();
            return;
        }

        String newTitle = "New Collection";
        String newNameId = "new-collection";
        int newTitleNumber = getTitleNumber(newTitle);

        if (newTitleNumber != 0) {
            newTitle = newTitle + " " + newTitleNumber;
            newNameId = newNameId + "-" + newTitleNumber;
        }

        String servers = server.getServer();
        Collection newCollection = new Collection(newTitle, servers + "collections/" + newNameId);
        Collection savedCollection = server.addCollection(newCollection);
        if (savedCollection == null) {
            showServerAlertWarning("Alert.serverNotAvailable");
            return;
        }

        collections.add(savedCollection);
        refreshObservableCollections();
        refreshObservableNotes();

        showServerAlertInformation("State.collectionAdded");
    }

    /**
     * Returns the next number so that notes can be automatically numbered.
     *
     * @param newTitle the new title of the note
     * @return the number
     */
    private int getTitleNumber(String newTitle) {
        boolean titleExists = false;
        int newTitleNumber = 0;

        for (Collection collection : collections) {
            if (collection.title.equals(newTitle)) {
                titleExists = true;
                break;
            }
        }

        while (titleExists) {
            titleExists = false;
            newTitleNumber++;
            for (Collection collection : collections) {
                if (collection.title.equals(newTitle + " " + newTitleNumber)) {
                    titleExists = true;
                    break;
                }
            }
        }
        return newTitleNumber;
    }

    /**
     * Update collection data from index.
     *
     * @param index     The index of the collection
     * @param title     The updated collection title
     * @param serverUrl The updated server URL
     */
    public void updateCollectionFromIndex(int index, String title,
                                          String serverUrl) {
        if (!isServerAvailable()) {
            showServerAlertWarning("Alert.unableToUpdateCollection");
            return;
        }

        if (index < 0) return;
        Collection c = collections.get(index);

        if (Objects.equals(c.title, title) && Objects.equals(c.serverUrl, serverUrl))
            return;

        title = title.trim();
        for (Collection collection : collections) {
            if (collection.title.equals(title) && !collection.equals(c)) {
                showServerAlertWarning("State.collectionTitleExists");
                return;
            }
        }

        c.title = title;
        c.serverUrl = server.getServer() + "collections/" +
                title.trim().toLowerCase().replaceAll("\\s+", "-");

        server.updateCollection(c.id, c);

        // all of the notes have a copy of the collection, not the same collections as in the state
        // this is a problem when renaming collections
        // and after that trying to filter based on collection,
        // because the copy in the note will still have the old collection
        // so here, I just update the collection in the notes that have this specific collection
        List<Note> problematicNotes = notes.stream().filter(n -> n.collection.id == c.id).toList();
        problematicNotes.forEach(n -> {
            n.collection = c;
        });

        refreshObservableCollections();
        refreshObservableNotes();

        showServerAlertInformation("State.collectionUpdated");
    }

    /**
     * Checks if collection of a note was changed.
     *
     * @param index the index of a collection
     * @return true if there was a change
     */
    public boolean hasNoteCollectionSelectionChanged(int index) {
        if (selectedNote == null) return false;
        if (getIndexFromCollection(selectedNote.collection) == index) return false;

        Collection c = index >= 0 ? collections.get(index) : null;

        return selectedNote.collection != c;
    }

    /**
     * Updates the collection of the selected note,
     * using the index of the collection in the collection list.
     *
     * @param index The index of the collection
     */
    public void updateSelectedNoteCollectionFromIndex(int index) {
        if (selectedNote == null) return;
        if (getIndexFromCollection(selectedNote.collection) == index) return;

        Collection c = index >= 0 ? collections.get(index) : null;

        if (selectedNote.collection == c) return;

        for (Note note : notes) {
            if (note.title.equals(selectedNote.title) && !note.equals(selectedNote)
                    && note.collection.equals(c)) {
                showServerAlertWarning("State.noteTitleExists");
                return;
            }
        }

        selectedNote.collection = c;
        Note updatedNote = server.updateNote(selectedNote.id, selectedNote);
        if (updatedNote == null) {
            showServerAlertWarning("Alert.unableToUpdateNote");
            return;
        }
        refreshObservableNotes();
    }

    /**
     * Deletes collection in the state from the collection index.
     *
     * @param index The collection index
     */
    public void deleteCollectionFromIndex(int index) {
        if (index < 0 || index >= collections.size()) {
            System.err.println("Invalid collection index: " + index);
            return;
        }

        Collection c = collections.get(index);
        boolean hasNotes = notes.stream()
                .anyMatch(note -> note.collection != null && note.collection.equals(c));

        if (hasNotes) {
            System.out.println("Collection must be empty to be deleted");
            showServerAlertWarning("State.collectionIsNotEmpty");
            return;
        }
        try {
            server.deleteCollectionById(c.id);
            collections.remove(index);

            showServerAlertInformation("EditCollection.deleted");

            if (c.equals(defaultCollection)) {
                if (!collections.isEmpty()) {
                    defaultCollection = collections.getFirst();
                    saveDefaultCollectionId(defaultCollection.id);
                    System.out.println("New default collection set: " + defaultCollection.title);
                } else {
                    defaultCollection = null;
                    saveDefaultCollectionId(-1);
                    System.out.println("No collections available. Default collection cleared.");
                }
            }
        } catch (Exception e) {
            collections.remove(c);
            Collection deletedCollection = server.deleteCollectionById(c.id);
            if (deletedCollection == null) {
                showServerAlertWarning("Alert.serverNotAvailable");
                return;
            }
        }
        refreshObservableCollections();
        refreshObservableNotes();
    }


    /**
     * Sets the current default collection to add new notes to, using the collection index.
     *
     * @param index The collection index
     */
    public void setDefaultCollectionFromIndex(int index) {
        if (!isServerAvailable()) {
            showServerAlertWarning("Alert.unableToUpdateCollection");
            return;
        }
        if (index >= 0 && index < collections.size()) {
            defaultCollection = collections.get(index);
            saveDefaultCollectionId(defaultCollection.id); // Save the ID locally
            System.out.println("Default collection set: " + defaultCollection.title);
        } else {
            defaultCollection = null;
            saveDefaultCollectionId(-1); // Clear the default collection
            System.out.println("Default collection cleared.");
        }

        refreshObservableCollections();
        refreshObservableNotes();
    }

    /**
     * Saves the default collection ID to local storage.
     *
     * @param collectionId The ID of the default collection
     */
    private void saveDefaultCollectionId(long collectionId) {
        preferences.putLong(defaultCollectionIdKey, collectionId);
        System.out.println("Default collection ID saved: " + collectionId);
    }

    /**
     * Loads the default collection ID from local storage.
     *
     * @return The ID of the default collection, or -1 if none is set
     */
    private long loadDefaultCollectionId() {
        return preferences.getLong(defaultCollectionIdKey, -1);
    }

    /**
     * Checks if the server is available.
     *
     * @return true if it is available
     */
    public boolean isServerAvailable() {
        return server.isServerAvailable();
    }

    /**
     * Show server unavailability alert of warning type.
     *
     * @param message the message to show
     */
    private void showServerAlertWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setTitle(resourceBundle.getString("Alert.warningDialog"));
        alert.setContentText(resourceBundle.getString(message));
        alert.showAndWait();
    }

    /**
     * Show server unavailability alert of information type.
     *
     * @param message the message to show
     */
    private void showServerAlertInformation(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initStyle(StageStyle.UTILITY);
        alert.setTitle(resourceBundle.getString("Alert.informationDialog"));
        alert.setHeaderText(null);
        alert.setContentText(resourceBundle.getString(message));
        alert.show();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(alert::close);
            }
        }, 2000);
    }

    /**
     * Updates the search query and refreshes note list.
     *
     * @param query The search query
     */
    public void updateSearchQuery(String query) {
        searchQuery = query;

        refreshObservableNotes();
    }

    /**
     * Returns the resourceBundle that was selected.
     *
     * @return ResourceBundle that is the previous-selected language.
     */
    public ResourceBundle getResourceBundle() {
        return this.resourceBundle;
    }

    /**
     * Sets the resourceBundle to the field.
     *
     * @param resourceBundle the new language that has been selected
     */
    public void setResourceBundle(ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
    }

    /**
     * Returns the selected collection.
     *
     * @return The selected collection
     */
    public Collection getSelectedCollection() {
        return selectedCollection;
    }

    /**
     * Returns the Set of selected Tags.
     *
     * @return the set of selected tags
     */
    public Set<Tag> getSelectedTags() {
        return selectedTags;
    }

    /**
     * Sets/updates the set of selected tags.
     *
     * @param tags the set of selected tags
     */
    public void setSelectedTags(Set<Tag> tags) {
        this.selectedTags = tags;
        updateAvailableTags();
    }

    /**
     * Updates the available Tags based on user selection.
     */
    public void updateAvailableTags() {
        if (selectedTags.isEmpty()) {
            this.filteredTags = new HashSet<>(tags);
        } else {
            Set<Long> filteredNoteIds = selectedTags.stream()
                    .flatMap(tag -> tag.noteIDs.stream())
                    .collect(Collectors.toSet());

            this.filteredTags = tags.stream()
                    .filter(tag -> tag.noteIDs.stream().anyMatch(filteredNoteIds::contains))
                    .collect(Collectors.toSet());
        }
        refreshObservableNotes();
    }

    /**
     * Gets all Tags from the server.
     *
     * @return the list of tags
     */
    public List<Tag> fetchAllTags() {
        return server.getAllTags();
    }

    /**
     * Handle synchronization object that was received through the websocket subscription.
     */
    public void handleSynchronization() {
        // https://stackoverflow.com/a/32489845
        Platform.runLater(this::refresh);
    }

    /**
     * Connects to the web socket.
     */
    public void connectWebsocket() {
        WebSocketClient webSocketClient = new StandardWebSocketClient();
        this.stompClient = new WebSocketStompClient(webSocketClient);
        this.stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        StompSessionHandler handler = new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(final StompSession session, StompHeaders connectedHeaders) {
                stompSession = session;

                session.subscribe("/user/synchronization", new StompFrameHandler() {
                    @Override
                    public Type getPayloadType(StompHeaders headers) {
                        return Synchronization.class;
                    }

                    @Override
                    public void handleFrame(StompHeaders headers, Object payload) {
                        handleSynchronization();
                    }
                });
            }

            @Override
            public void handleTransportError(StompSession session, Throwable exception) {
                if (exception.getCause().getClass() != DeploymentException.class) {
                    super.handleTransportError(session, exception);
                    return;
                }

                stompClient.stop();

                // retry connection every 5 seconds if it failed
                TimerTask action = new TimerTask() {
                    public void run() {
                        System.out.println("Websocket couldn't connect " +
                                "last time, trying again now");
                        connectWebsocket();
                    }
                };

                new Timer().schedule(action, 5000);
            }
        };

        this.stompClient.connectAsync("ws://localhost:8080/websocket", this.headers, handler);
    }

    /**
     * Gets all the images from the server.
     *
     * @return a list of all the images
     */
    public List<Files> getAllImages() {
        return server.getAllImages();
    }
}
