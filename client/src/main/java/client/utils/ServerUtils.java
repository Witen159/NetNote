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
package client.utils;

import com.google.inject.Inject;
import commons.Collection;
import commons.Files;
import commons.Note;
import commons.Tag;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import java.io.File;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.MULTIPART_FORM_DATA_TYPE;

public class ServerUtils {
    private final String noteMap = "api/notes";
    private final String collectionMap = "api/collections";
    private final String server;
    private final String imageMap = "api/images";

    /**
     * Sets default server.
     */
    @Inject
    public ServerUtils() {
        server = "http://localhost:8080/";
    }

    /**
     * Retrieves the server name.
     *
     * @return returns the server name string
     */
    public String getServer() {
        return server;
    }

    /**
     * Retrieves all notes.
     *
     * @return returns the List of Notes from Server
     */
    public List<Note> getAllNotes() {
        try {

            return ClientBuilder.newClient(new ClientConfig()) //
                    .target(server).path(noteMap) //
                    .request(APPLICATION_JSON) //
                    .get(new GenericType<>() {
                    });
        } catch (ProcessingException e) {
            System.out.println("Failed to connect to server");
            return new ArrayList<>();
        }
    }

    /**
     * Adds a note to the server.
     *
     * @param note Note to be added
     * @return Added Note
     */
    public Note addNote(Note note) {
        try {
            return ClientBuilder.newClient(new ClientConfig()) //
                    .target(server).path(noteMap) //
                    .request(APPLICATION_JSON) //
                    .post(Entity.entity(note, APPLICATION_JSON), Note.class);
        } catch (ProcessingException e) {
            System.out.println("Failed to connect to server");
            return null;
        }
    }

    /**
     * Updates Note by id.
     *
     * @param id          id of Note to be updated
     * @param updatedNote Note with updated parameters
     * @return Updated Note
     */
    public Note updateNote(long id, Note updatedNote) {
        try {
            return ClientBuilder.newClient(new ClientConfig()) //
                    .target(server).path(noteMap + "/" + id) //
                    .request(APPLICATION_JSON) //
                    .put(Entity.entity(updatedNote, APPLICATION_JSON), Note.class);
        } catch (ProcessingException e) {
            System.out.println("Failed to connect to server");
            return null;
        }
    }

    /**
     * Deletes Note from Server by id.
     *
     * @param id id of Note to be Deleted
     * @return Deleted Note
     */
    public Note deleteNoteById(long id) {
        try {
            return ClientBuilder.newClient(new ClientConfig()) //
                    .target(server).path(noteMap + "/" + id) //
                    .request(APPLICATION_JSON) //
                    .delete(Note.class);
        } catch (ProcessingException e) {
            System.out.println("Failed to connect to server");
            return null;
        }
    }

    /**
     * Gets all collections from the server.
     *
     * @return All collections from server
     */
    public List<Collection> getAllCollections() {
        try {
            return ClientBuilder.newClient(new ClientConfig()) //
                    .target(server).path(collectionMap) //
                    .request(APPLICATION_JSON) //
                    .get(new GenericType<>() {
                    });
        } catch (ProcessingException e) {
            System.out.println("Failed to connect to server");
            return new ArrayList<>();
        }
    }

    /**
     * Adds collection to the server.
     *
     * @param collection Collection to be added
     * @return Added collection
     */
    public Collection addCollection(Collection collection) {
        try {
            return ClientBuilder.newClient(new ClientConfig()) //
                    .target(server).path(collectionMap) //
                    .request(APPLICATION_JSON) //
                    .post(Entity.entity(collection, APPLICATION_JSON), Collection.class);
        } catch (ProcessingException e) {
            System.out.println("Failed to connect to server");
            return null;
        }
    }

    /**
     * Updates Collection by id.
     *
     * @param id                id of collection to be updated.
     * @param updatedCollection Collection with updated parameters.
     * @return Updated collection.
     */
    public Collection updateCollection(long id, Collection updatedCollection) {
        try {
            return ClientBuilder.newClient(new ClientConfig()) //
                    .target(server).path(collectionMap + "/" + id) //
                    .request(APPLICATION_JSON) //
                    .put(Entity.entity(updatedCollection, APPLICATION_JSON), Collection.class);
        } catch (ProcessingException e) {
            System.out.println("Failed to connect to server");
            return null;
        }
    }

    /**
     * Deleted Note from the server by id.
     *
     * @param id id of Note to be deleted
     * @return returns deleted Note
     */
    public Collection deleteCollectionById(long id) {
        try {
            return ClientBuilder.newClient(new ClientConfig()) //
                    .target(server).path(collectionMap + "/" + id) //
                    .request(APPLICATION_JSON) //
                    .delete(Collection.class);
        } catch (ProcessingException e) {
            System.out.println("Failed to connect to server");
            return null;
        }
    }

    /**
     * Gets all tags from the server.
     *
     * @return A list of tags fetched from the server
     */
    public List<Tag> getAllTags() {
        try {
            String tagMap = "api/tags";
            return ClientBuilder.newClient(new ClientConfig())
                    .target(server).path(tagMap)
                    .request(APPLICATION_JSON)
                    .get(new GenericType<>() {
                    });
        } catch (ProcessingException e) {
            System.out.println("Failed to connect to server");
            return new ArrayList<>();
        }
    }

    /**
     * Uploads new image to the server.
     *
     * @param file the file that needs to be uploaded
     * @return true if image is uploaded successfully, false otherwise
     */
    public boolean uploadImage(File file) {
        Client client = ClientBuilder.newBuilder().register(MultiPartFeature.class).build();
        FileDataBodyPart filePart = new FileDataBodyPart("imageData", file);
        try {
            FormDataMultiPart formData = (FormDataMultiPart) new FormDataMultiPart()
                    .bodyPart(filePart);

            Response response = client.target(server).path(imageMap + "/upload")
                    .request(MULTIPART_FORM_DATA_TYPE)
                    .post(Entity.entity(formData, formData.getMediaType()));

            formData.close();
            client.close();
            if (response.getStatus() == 200) {
                return true;
            }
            System.out.println("Server responded with status code: " + response.getStatus());
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes an image by the given id (string).
     *
     * @param idStr the id of the image in string format
     * @return true if image is deleted successfully, false otherwise
     */
    public boolean deleteImage(String idStr) {
        long id = Long.parseLong(idStr);
        try {
            Response response = ClientBuilder.newClient(new ClientConfig())
                    .target(server).path(imageMap + "/delete/" + id)
                    .request(APPLICATION_JSON)
                    .delete(Response.class);
            return response.getStatus() == 200 || response.getStatus() == 204;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gets all names of the images from the server.
     *
     * @return a string with all the names of the images
     */
    public String getAllImagesNames() {
        try {
            return ClientBuilder.newClient(new ClientConfig())
                    .target(server).path(imageMap + "/get/all")
                    .request(APPLICATION_JSON)
                    .get(String.class);
        } catch (ProcessingException e) {
            System.out.println("Failed to connect to server");
            return null;
        }
    }

    /**
     * Get all images from the server.
     *
     * @return a list of all the images from the server
     */
    public List<Files> getAllImages() {
        try {
            return ClientBuilder.newClient(new ClientConfig())
                    .target(server).path(imageMap + "/get/allRawImages")
                    .request(APPLICATION_JSON)
                    .get(new GenericType<>() {
                    });
        } catch (ProcessingException e) {
            System.out.println("Failed to connect to server");
            return null;
        }
    }

    /**
     * Checks the availability of the server.
     *
     * @return The availability of the server
     */
    public boolean isServerAvailable() {
        try {
            ClientBuilder.newClient(new ClientConfig()) //
                    .target(server) //
                    .request(APPLICATION_JSON) //
                    .get();
        } catch (ProcessingException e) {
            if (e.getCause() instanceof ConnectException) {
                return false;
            }
        }
        return true;
    }
}