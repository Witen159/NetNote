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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class NoteTest {

    private Note note1;
    private Note note2;
    private Note note3;
    private Collection collection;

    @BeforeEach
    void setUp() {
        collection = new Collection("collection title", "server URL");
        note1 = new Note("title", "content", collection);
        note2 = new Note("title", "content", collection);
        note3 = new Note("Title", "Content", collection);
    }

    @AfterEach
    void tearDown() {
        collection = null;
        note1 = null;
        note2 = null;
    }

    @Test
    void testEqual() {
        assertNotNull(note1);
        assertNotNull(note2);
        assertEquals(note1, note2);
    }

    @Test
    void testNotEqual() {
        assertNotNull(note1);
        assertNotNull(note3);
        assertNotEquals(note1, note3);
    }

    @Test
    void testHashCodeEquals() {
        assertNotNull(note1);
        assertNotNull(note2);

        int hashCode1 = note1.hashCode();
        int hashCode2 = note2.hashCode();
        assertEquals(note1, note2);
        assertEquals(hashCode1, hashCode2);
    }

    @Test
    void testHashCodeNotEqual() {
        assertNotNull(note1);
        assertNotNull(note3);

        int hashCode1 = note1.hashCode();
        int hashCode2 = note3.hashCode();
        assertNotEquals(note1, note3);
        assertNotEquals(hashCode1, hashCode2);
    }

    @Test
    void testToStringNotNull() {
        assertNotNull(note1);

        String string = note1.toString();
        assertNotNull(string);
    }

    @Test
    void testToString() {
        assertNotNull(note1);

        String expected = note1.toString();
        assertEquals(expected, note1.toString());
    }
}