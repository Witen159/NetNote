package server.api;

import commons.Files;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import server.services.ImageService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/images")
public class ImageController {
    private final ImageService imageService;

    /**
     * Constructor that creates a new ImageController object with specified ImageService.
     *
     * @param imageService the ImageService to be used by the ImageController
     */
    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    /**
     * Uploads and saves the posted image to the database.
     *
     * @param imageData the image file that needs to be uploaded
     * @return a string with the status and name about the uploaded image
     */
    @PostMapping(path = "/upload")
    public ResponseEntity<String> uploadImage(@RequestParam("imageData") MultipartFile imageData) {
        try {
            Files savedFiles = imageService.uploadImage(imageData);
            //return ResponseEntity.ok(savedImage);
            return ResponseEntity.ok("Image uploaded successfully. " + savedFiles.getName());
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Returns the image with the specified id.
     *
     * @param id the id that is being searched for
     * @return the image with request status
     */
    @GetMapping(path = "/get/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable("id") Long id) {
        Files files = imageService.getImage(id);
        if (files == null) {
            return ResponseEntity.notFound().build();
        }
        MediaType mediaType = MediaType.valueOf(files.getType());
        return ResponseEntity.ok().contentType(mediaType).body(files.getImageByte());
    }

    /**
     * Returns all the images in the database.
     *
     * @return a string with information (name and id) about all the images in the database
     */
    @GetMapping(path = {"/get", "/get/all"})
    public ResponseEntity<String> getAllImages() {
        List<Files> files = imageService.getAllImages();
        if (files.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        StringBuilder response = new StringBuilder();
        for (Files file : files) {
            response.append(file.getName()).append("; id: ").append(file.getId()).append("\n");
        }
        return ResponseEntity.ok(response.toString());
    }

    /**
     * Returns all the images in the database.
     *
     * @return a list of all the images in the database
     */
    @GetMapping(path = "/get/allRawImages")
    public ResponseEntity<List<Files>> getAllRawImages() {
        List<Files> files = imageService.getAllImages();
        if (files.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(files);
    }

    /**
     * Deletes the image with the specified id.
     *
     * @param id the id of the image that needs to be deleted
     * @return a string with information about the deletion status
     */
    @DeleteMapping(path = "/delete/{id}")
    public ResponseEntity<String> deleteImage(@PathVariable("id") Long id) {
        String message = imageService.deleteImage(id);
        if (message == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(message);
    }

    /**
     * Renames the image with the specified id.
     *
     * @param id   the id of the image that needs to be renamed
     * @param name the new name of the image
     * @return a string with information about the renaming status
     */
    @PutMapping(path = "/rename/{id}")
    public ResponseEntity<String> renameImage(@PathVariable("id") Long id,
                                              @RequestParam("imageName") String name) {
        if (name == null || name.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        String message = imageService.renameImage(id, name);
        if (message == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(message);
    }

}
