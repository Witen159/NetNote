/*
 * Copyright 2021 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package server.api;

import commons.Collection;
import commons.Note;
import commons.Synchronization;
import commons.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.bind.annotation.*;
import server.WebSocketEventListener;
import server.services.NoteService;
import server.services.TagService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/notes")
public class NoteController {
    private final NoteService service;
    private final TagService tagService;

    private final SimpMessageSendingOperations messagingTemplate;

    @Autowired
    private WebSocketEventListener connectionsEventListener;

    /**
     * Constructs a new NoteController with the specified service.
     *
     * @param service           the service used for managing notes
     * @param tagService        the service used for managing tags
     * @param messagingTemplate the messaging template
     */
    public NoteController(NoteService service, TagService tagService,
                          SimpMessageSendingOperations messagingTemplate) {
        this.service = service;
        this.tagService = tagService;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Retrieves all notes.
     *
     * @return a list of all notes, or null if no notes exist
     */
    @GetMapping(path = {"", "/"})
    public List<Note> getAll() {
        return service.findAllNotes();
    }

    /**
     * Retrieves the collection associated with a specific note by its ID.
     *
     * @param id the ID of the note whose associated collection is to be retrieved
     * @return a response entity containing the collection if found
     */
    @GetMapping("/collection/{id}")
    public ResponseEntity<Collection> getCollectionByNoteId(@PathVariable("id") long id) {
        var collection = service.findNotesCollection(id);
        if (collection == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(collection);
    }

    /**
     * Retrieves a specific note by its ID.
     *
     * @param id the ID of the note to retrieve, provided in the path
     * @return a response entity containing the note if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Note> getById(@PathVariable("id") long id) {
        var note = service.findNote(id);
        if (note == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(note);
    }

    /**
     * Saves a new note.
     *
     * @param note the note to be saved, provided in the request body
     * @return a response entity containing the saved note
     */
    @PostMapping(path = {"", "/"})
    public ResponseEntity<Note> save(@RequestBody Note note) {
        var savedNote = service.saveNote(note);
        if (savedNote == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(savedNote);
    }

    /**
     * Updates an existing note by its ID.
     *
     * @param id   the ID of the note to update, provided in the path
     * @param note the updated note data, provided in the request body
     * @return a response entity containing the updated note
     */
    @PutMapping("/{id}")
    public ResponseEntity<Note> update(@PathVariable("id") long id, @RequestBody Note note) {
        var updatedNote = service.findNote(id);
        if (note == null || note.title == null || note.getContent() == null
                || updatedNote == null) {
            return ResponseEntity.badRequest().build();
        }

        updatedNote.title = note.title;
        updatedNote.setContent(note.getContent());
        updatedNote.collection = note.collection;

        return ResponseEntity.ok(service.saveNote(updatedNote));
    }

    /**
     * Deletes a note based on the provided note object.
     *
     * @param note the note to be deleted, provided in the request body
     * @return a response entity containing the deleted note
     */
    @DeleteMapping(path = {"", "/"})
    public ResponseEntity<Note> delete(@RequestBody Note note) {
        var deletedNote = service.deleteNote(note);
        if (deletedNote == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(deletedNote);
    }

    /**
     * Deletes a note based on its ID.
     *
     * @param id the ID of the note to delete, provided in the path
     * @return a response entity containing the deleted note
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Note> delete(@PathVariable("id") long id) {
        var note = service.findNote(id);
        if (note == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(service.deleteNote(note));
    }

    /**
     * Removes a Tag from a Note.
     *
     * @param noteId  the ID of the note
     * @param tagName the name of the tag to be removed
     * @return a response entity containing the note with the removed tag
     */
    @DeleteMapping("/{noteId}/tags/{tagId}")
    public ResponseEntity<Note> removeTagFromNote(@PathVariable Long noteId,
                                                  @PathVariable String tagName) {
        Note updatedNote = service.removeTagFromNote(noteId, tagName);
        return ResponseEntity.ok(updatedNote);
    }

    /**
     * Gets all Tags of a Note.
     *
     * @param noteId the ID of the note
     * @return a response entity containing the tag list
     */
    @GetMapping("/{noteId}/tags")
    public ResponseEntity<Set<String>> getTagsForNote(@PathVariable Long noteId) {
        Set<String> tags = service.getTagsForNote(noteId);
        return ResponseEntity.ok(tags);
    }

    /**
     * Gets all Tags that exist.
     *
     * @return a list of tags
     */
    @GetMapping("/tags")
    public ResponseEntity<List<Tag>> getAllTags() {
        List<Tag> tags = tagService.findAllTags();
        return ResponseEntity.ok(tags);
    }

    /**
     * Publish synchronizations to all other websocket subscribers.
     *
     * @param synchronization Synchronization object
     * @param headers         The websocket headers
     */
    @MessageMapping("/synchronize")
    public void publishChangesToWebsocket(Synchronization synchronization,
                                          StompHeaderAccessor headers) {
        Set<String> ids = new HashSet<>(connectionsEventListener.sessionIds);
        ids.remove(headers.getSessionId());

        for (String id : ids) {
            messagingTemplate.convertAndSendToUser(id,
                    "/synchronization", synchronization, createHeaders(id));
        }
    }

    // https://stackoverflow.com/q/34929578

    /**
     * Generates headers from sessionId.
     *
     * @param sessionId The session id
     * @return The created headers
     */
    private MessageHeaders createHeaders(String sessionId) {
        SimpMessageHeaderAccessor headerAccessor =
                SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);
        return headerAccessor.getMessageHeaders();
    }
}