package server.services;

import commons.Files;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ImageService {
    /**
     * Saves an image with the given name and data.
     *
     * @param imageFile the image file that needs to be saved
     * @return the saved image
     */
    public Files uploadImage(MultipartFile imageFile) throws IOException;

    /**
     * Retrieves the image with the given name.
     *
     * @param id the id of the image
     * @return the image
     */
    public Files getImage(Long id);

    /**
     * Returns all the images.
     *
     * @return a list of all the images
     */
    public List<Files> getAllImages();

    /**
     * Deletes the image with the given id.
     *
     * @param id the id of the image
     * @return a string message indicating whether the image was successfully deleted or not
     */
    public String deleteImage(Long id);

    /**
     * Renames the image with the given id.
     *
     * @param id    the id of the image
     * @param name  the new name of the image
     * @return a string message indicating whether the image was successfully renamed or not
     */
    public String renameImage(Long id, String name);

    /**
     * Returns all images with same name.
     *
     * @param name the name of the image that needs to be found
     * @return a list of all images with the same name
     */
    List<Files> getIdByName(String name);
}
