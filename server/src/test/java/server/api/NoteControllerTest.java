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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import server.WebSocketConfig;
import server.WebSocketEventListener;
import server.services.NoteService;
import server.services.TagService;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NoteController.class)
@Import({WebSocketConfig.class, WebSocketEventListener.class})
public class NoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NoteService noteService;

    @MockBean
    private TagService tagService;

    private Note note1;
    private Collection defaultCollection;

    @BeforeEach
    public void setUp() {
        defaultCollection = new Collection();
        defaultCollection.title = "Default Collection";

        note1 = new Note();
        note1.title = "Note 1";
        note1.setContent("testContent");
        note1.collection = defaultCollection;
    }

    @AfterEach
    public void tearDown() {
        note1 = null;
        defaultCollection = null;
    }

    @Test
    void testGetAllNotes() throws Exception {
        when(noteService.findAllNotes()).thenReturn(List.of(note1));

        mockMvc.perform(get("/api/notes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(note1.getId()))
                .andExpect(jsonPath("$[0].title").value("Note 1"))
                .andExpect(jsonPath("$[0].content").value("testContent"))
                .andExpect(jsonPath("$[0].collection.title").value("Default Collection"))
                .andExpect(jsonPath("$[0].collection.id").value(defaultCollection.getId()));
    }

    @Test
    void testGetNoteById_success() throws Exception {
        when(noteService.findNote(note1.getId())).thenReturn(note1);

        mockMvc.perform(get("/api/notes/{id}", note1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(note1.getId()))
                .andExpect(jsonPath("$.title").value("Note 1"))
                .andExpect(jsonPath("$.content").value("testContent"));
    }

    @Test
    void testGetNoteById_null() throws Exception {
        when(noteService.findNote(note1.getId())).thenReturn(null);

        mockMvc.perform(get("/api/notes/{id}", note1.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testSaveNote() throws Exception {
        when(noteService.saveNote(any(Note.class))).thenReturn(note1);

        mockMvc.perform(post("/api/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Note 1\", \"content\": \"testContent\", \"collection\": {\"id\": 1, \"name\": \"Default Collection\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(note1.getId()))
                .andExpect(jsonPath("$.title").value("Note 1"))
                .andExpect(jsonPath("$.content").value("testContent"));
    }

    @Test
    void testUpdateNote_success() throws Exception {
        when(noteService.findNote(note1.getId())).thenReturn(note1);
        when(noteService.saveNote(any(Note.class))).thenReturn(note1);

        mockMvc.perform(put("/api/notes/{id}", note1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Updated Title\", \"content\": \"Updated Content\", \"collection\": {\"id\": 1, \"name\": \"Default Collection\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.content").value("Updated Content"));
    }

    @Test
    void testUpdateNote_badRequest() throws Exception {
        when(noteService.findNote(note1.getId())).thenReturn(null);

        mockMvc.perform(put("/api/notes/{id}", note1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Updated Title\", \"content\": \"Updated Content\", \"collection\": {\"id\": 1, \"name\": \"Default Collection\"}}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteNoteById_success() throws Exception {
        when(noteService.findNote(note1.getId())).thenReturn(note1);
        when(noteService.deleteNote(note1)).thenReturn(note1);

        mockMvc.perform(delete("/api/notes/{id}", note1.getId()))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteNoteById_null() throws Exception {
       when(noteService.findNote(note1.getId())).thenReturn(null);

        mockMvc.perform(delete("/api/notes/{id}", note1.getId()))
                .andExpect(status().isNotFound());
    }

}