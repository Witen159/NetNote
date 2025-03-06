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

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Entity
public class Collection {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public long id;

    public String title;
    public String serverUrl;

    /**
     * Default constructor for the Collection class.
     */
    public Collection() {
    }

    /**
     * Constructs a new Collection instance with the specified details.
     *
     * @param title     the title of the collection
     * @param serverUrl the server URL associated with the collection
     */
    public Collection(String title, String serverUrl) {
        this.title = title;
        this.serverUrl = serverUrl;
    }

    /**
     * Getter for the title of a Collection.
     *
     * @return the title of the Collection
     */
    public String getTitle() {
        return title;
    }

    /**
     * Getter for the ID of a Collection.
     *
     * @return the ID of the Collection
     */
    public long getId() {
        return id;
    }

    /**
     * Compares this Collection to another object for equality.
     *
     * @param obj the object to compare to this collection
     * @return true if the specified object is equal to this collection; false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    /**
     * Computes the hash code for this Collection.
     *
     * @return the hash code of this collection
     */
    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    /**
     * Returns a string representation of this Collection.
     *
     * @return a string representation of this collection
     */
    @Override
    public String toString() {
        return "Collection " + this.title
                + ", connect by URL: " + serverUrl + ". Unique id: " + id;
    }
}