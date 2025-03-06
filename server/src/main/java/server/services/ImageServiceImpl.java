package server.services;

import commons.Files;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import server.database.ImageRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ImageServiceImpl implements ImageService {
    private final ImageRepository imageRepository;

    /**
     * Constructor that creates a new ImageService object,
     * with the appropriate ImageRepository object injected.
     *
     * @param imageRepository the repository object of all the images
     */
    @Autowired
    public ImageServiceImpl(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    @Override
    public Files uploadImage(MultipartFile imageFile) throws IOException {
        Files newFiles = imageRepository.save(new Files(
                imageFile.getOriginalFilename(),
                imageFile.getContentType(),
                imageFile.getBytes()
        ));
        return imageRepository.save(newFiles);
    }

    @Override
    public Files getImage(Long id) {
        if (id < 0 || !imageRepository.existsById(id)) {
            return null;
        }
        return imageRepository.findById(id).orElse(null);
    }

    @Override
    public List<Files> getAllImages() {
        return imageRepository.findAll();
    }

    @Override
    public String deleteImage(Long id) {
        if (id < 0 || !imageRepository.existsById(id)) {
            return null;
        }
        imageRepository.deleteById(id);
        return "Successfully deleted image with id: " + id;
    }

    @Override
    public String renameImage(Long id, String name) {
        if (id < 0 || !imageRepository.existsById(id)) {
            return null;
        }
        Files files = imageRepository.findById(id).orElse(null);
        assert files != null;
        files.setName(name);
        imageRepository.save(files);
        return "Successfully renamed image with id: " + id
                + " from '" + files.getName() + "' to '" + name + "'";
    }

    @Override
    public List<Files> getIdByName(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        List<Files> allFiles = imageRepository.findAll();
        List<Files> imagesFound = new ArrayList<>();
        for (Files files : allFiles) {
            if (files.getName().equals(name)) {
                imagesFound.add(files);
            }
        }
        return imagesFound;
    }
}