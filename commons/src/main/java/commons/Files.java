package commons;

import jakarta.persistence.*;

@Entity
@Table(name = "image")
public class Files {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private String type;
    @Column(length = 50000000)
    private byte[] imageByte;

    /**
     * Default constructor.
     */
    public Files() {
    }

    /**
     * Constructor with all the fields.
     *
     * @param name      of the image
     * @param type      of the image (e.g. jpg, png, etc.)
     * @param imageByte the amount of bytes of the image
     */
    public Files(String name, String type, byte[] imageByte) {
        this.name = name;
        this.type = type;
        this.imageByte = imageByte;
    }

    /**
     * Gets the id of the image.
     *
     * @return the id of the image
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the id of the image.
     *
     * @param id the id of the image
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the name of the image.
     *
     * @return the name of the image
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the image.
     *
     * @param name the name of the image
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the type of the image.
     *
     * @return the type of the image
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the size in bytes of the image.
     *
     * @return the size in bytes of the image
     */
    public byte[] getImageByte() {
        return imageByte;
    }

}
