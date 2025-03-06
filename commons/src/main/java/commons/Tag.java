package commons;

import jakarta.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.HashSet;
import java.util.Set;

@Entity
public class Tag {
    @Id
    public String title;

    public Set<Long> noteIDs;

    /**
     * Constructs a new Tag instance with the specified details.
     *
     * @param title the title of the tag
     */
    public Tag(String title) {
        this.title = title;
        noteIDs = new HashSet<>();
    }

    /**
     * Parameterless constructor.
     */
    public Tag() {
        noteIDs = new HashSet<>();
    }

    /**
     * Getter for the title of a Tag.
     *
     * @return the title of the Tag
     */
    public String getTitle() {
        return title;
    }

    /**
     * Compares this Tag to another object for equality.
     *
     * @param obj the object to compare to this note
     * @return true if the specified object is equal to this note; false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    /**
     * Computes the hash code for this Tag.
     *
     * @return the hash code of this tag
     */
    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    /**
     * Returns the name of the Tag, used to display in ComboBox when filtering by Tags.
     *
     * @return the name of the tag
     */
    @Override
    public String toString() {
        return title;
    }

}
