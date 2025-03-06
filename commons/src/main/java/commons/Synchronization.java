package commons;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import static org.apache.commons.lang3.builder.ToStringStyle.MULTI_LINE_STYLE;

public class Synchronization {
    public SynchronizationType type;
    public long noteId;
    public String title;
    public String content;

    /**
     * Default no-arg constructor.
     */
    public Synchronization(){}

    /**
     * Constructs a new Synchronization instance with the specified details.
     *
     * @param type    The synchronization type
     * @param noteId  The note id
     * @param title   The note title
     * @param content The note content
     */
    public Synchronization(SynchronizationType type, long noteId, String title, String content) {
        this.type = type;
        this.noteId = noteId;
        this.title = title;
        this.content = content;
    }

    /**
     * Compares this Synchronization to another object for equality.
     *
     * @param obj the object to compare to this synchronization
     * @return true if the specified object is equal to this synchronization; false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    /**
     * Computes the hash code for this Synchronization.
     *
     * @return the hash code of this synchronization
     */
    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    /**
     * Returns a string representation of this Synchronization.
     *
     * @return a string representation of this synchronization
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, MULTI_LINE_STYLE);
    }
}