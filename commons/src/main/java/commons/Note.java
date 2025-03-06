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
package commons;

import jakarta.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

import static org.apache.commons.lang3.builder.ToStringStyle.MULTI_LINE_STYLE;

@Entity
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public long id;

    public String title;
    private String content;

    @ManyToOne()
    public Collection collection;

    private Set<String> tags;

    /**
     * Default constructor for the Note class.
     */
    @SuppressWarnings("unused")
    public Note() {
        // for object mappers
        tags = new HashSet<>(); // tags field must never be null, only an empty Set
    }

    /**
     * Constructs a new Note instance with the specified details.
     *
     * @param title      the title of the note
     * @param content    the content of the note
     * @param collection the Collection to which the note belongs
     */
    public Note(String title, String content, Collection collection) {
        this.title = title;
        this.content = content;
        this.collection = collection;
        tags = new HashSet<>();
        parseTags();
    }

    /**
     * Getter for the title of a Note.
     *
     * @return the title of the Note
     */
    public String getTitle() {
        return title;
    }

    /**
     * Getter for the content of a Note.
     *
     * @return the text (content) of the note
     */
    public String getContent() {
        return content;
    }

    /**
     * Getter for the ID of a Note.
     *
     * @return the ID of the note
     */
    public long getId() {
        return id;
    }

    /**
     * Getter for the list of Tags of a Note.
     *
     * @return the set of tags of the note
     */
    public Set<String> getTags() {
        return tags;
    }

    /**
     * Setter and tag parser for a Note's content.
     *
     * @param newContent the new content to set
     */
    public void setContent(String newContent) {
        this.content = newContent;
        tags.clear();
        parseTags();
    }

    /**
     * Setter for the list of Tags of a note.
     *
     * @param newTags the list of tags to set
     */
    public void setTags(Set<String> newTags) {
        if (newTags == null)
            tags.clear();
        else
            this.tags = newTags;
    }

    /**
     * Automatically parses and sets Tags from the content of a Note.
     */
    public void parseTags() {

        try (BufferedReader reader = new BufferedReader(new StringReader(content))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] words = line.split("\\s+");
                for (String word : words) {
                    if (word.startsWith("#") && word.length() > 1) {
                        String tag = word.substring(1)
                                .replaceAll("[^a-zA-Z0-9]", "");
                        if (!tag.isEmpty()) {
                            tags.add(tag);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading text: " + e.getMessage());
        }
    }

    /**
     * Compares this Note to another object for equality.
     *
     * @param obj the object to compare to this note
     * @return true if the specified object is equal to this note; false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    /**
     * Computes the hash code for this Note.
     *
     * @return the hash code of this note
     */
    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    /**
     * Returns a string representation of this Note.
     *
     * @return a string representation of this note
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, MULTI_LINE_STYLE);
    }
}