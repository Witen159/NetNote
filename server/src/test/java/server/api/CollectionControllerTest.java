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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import server.services.CollectionService;
import server.services.TagService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CollectionController.class)
public class CollectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CollectionService collectionService;

    @MockBean
    private TagService tagService;

    private Collection collection1;
    private Collection collection2;

    @BeforeEach
    public void setUp() {
        collection1 = new Collection();
        collection1.title = "Collection 1";
        collection1.serverUrl = "test.com";

        collection2 = new Collection();
        collection2.title = "Collection 2";
        collection2.serverUrl = "test2.com";
    }

    @AfterEach
    public void tearDown() {
        collection1 = null;
        collection2 = null;
    }

    @Test
    void testGetAllCollections() throws Exception {
        when(collectionService.findAllCollections()).thenReturn(List.of(collection1, collection2));

        mockMvc.perform(get("/api/collections"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(collection1.id))
                .andExpect(jsonPath("$[0].title").value("Collection 1"))
                .andExpect(jsonPath("$[0].serverUrl").value("test.com"))
                .andExpect(jsonPath("$[1].id").value(collection2.id))
                .andExpect(jsonPath("$[1].title").value("Collection 2"))
                .andExpect(jsonPath("$[1].serverUrl").value("test2.com"));
    }

    @Test
    void testGetCollectionById_success() throws Exception {
        when(collectionService.findCollection(collection1.id)).thenReturn(collection1);

        mockMvc.perform(get("/api/collections/{id}", collection1.id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(collection1.id))
                .andExpect(jsonPath("$.title").value("Collection 1"))
                .andExpect(jsonPath("$.serverUrl").value("test.com"));
    }

    @Test
    void testGetCollectionById_null() throws Exception {
        when(collectionService.findCollection(collection1.id)).thenReturn(null);

        mockMvc.perform(get("/api/collections/{id}", collection1.id))
                .andExpect(status().isNotFound());
    }

    @Test
    void testSaveCollection() throws Exception {
        when(collectionService.saveCollection(any(Collection.class))).thenReturn(collection1);

        mockMvc.perform(post("/api/collections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Collection 1\", \"serverUrl\": \"test.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(collection1.id))
                .andExpect(jsonPath("$.title").value("Collection 1"))
                .andExpect(jsonPath("$.serverUrl").value("test.com"));
    }

    @Test
    void testUpdateCollection_success() throws Exception {
        when(collectionService.findCollection(collection1.id)).thenReturn(collection1);
        when(collectionService.saveCollection(any(Collection.class))).thenReturn(collection1);

        mockMvc.perform(put("/api/collections/{id}", collection1.id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Updated Collection 1\", \"serverUrl\": \"updatedtest.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Collection 1"))
                .andExpect(jsonPath("$.serverUrl").value("updatedtest.com"));
    }

    @Test
    void testUpdateCollection_badRequest() throws Exception {
        when(collectionService.findCollection(collection1.id)).thenReturn(null);

        mockMvc.perform(put("/api/collections/{id}", collection1.id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Updated Collection 1\", \"serverUrl\": \"updatedtest.com\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteCollectionById_success() throws Exception {
        when(collectionService.findCollection(collection1.id)).thenReturn(collection1);
        when(collectionService.deleteCollection(collection1)).thenReturn(collection1);

        mockMvc.perform(delete("/api/collections/{id}", collection1.id))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteCollectionById_null() throws Exception {
        when(collectionService.findCollection(collection1.id)).thenReturn(null);

        mockMvc.perform(delete("/api/collections/{id}", collection1.id))
                .andExpect(status().isNotFound());
    }


}