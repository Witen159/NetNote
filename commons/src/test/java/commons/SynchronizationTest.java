package commons;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SynchronizationTest {
    private Synchronization synchronization1;
    private Synchronization synchronization2;
    private Synchronization synchronization3;

    @BeforeEach
    void setUp() {
        synchronization1 = new Synchronization(SynchronizationType.DELETE, 1, "title", "content");
        synchronization2 = new Synchronization(SynchronizationType.DELETE, 1, "title", "content");
        synchronization3 = new Synchronization(SynchronizationType.CREATE, 2, "title", "content");
    }

    @AfterEach
    void tearDown() {
        synchronization1 = null;
        synchronization2 = null;
        synchronization3 = null;
    }

    @Test
    void testEqual_null() {
        boolean test = synchronization1.equals(null);
        assertNotNull(synchronization1);
        assertFalse(test);
    }

    @Test
    void testEqual_same() {
        boolean test = synchronization1.equals(synchronization1);
        assertNotNull(synchronization1);
        assertTrue(test);
    }

    @Test
    void testEqual_other() {
        assertNotNull(synchronization1);
        assertNotNull(synchronization2);
        assertTrue(synchronization1 != synchronization2);
        boolean test = synchronization1.equals(synchronization2);
        assertTrue(test);
    }

    @Test
    void testNotEqual_other() {
        assertNotNull(synchronization1);
        assertNotNull(synchronization3);
        assertTrue(synchronization1 != synchronization3);
        boolean test = synchronization1.equals(synchronization3);
        assertFalse(test);
    }

    @Test
    void testHashCodeEquals() {
        assertNotNull(synchronization1);
        assertNotNull(synchronization2);

        int hashCode1 = synchronization1.hashCode();
        int hashCode2 = synchronization2.hashCode();
        assertEquals(synchronization1, synchronization2);
        assertEquals(hashCode1, hashCode2);
    }

    @Test
    void testHashCodeNotEqual() {
        assertNotNull(synchronization1);
        assertNotNull(synchronization3);
        int hashCode1 = synchronization1.hashCode();
        int hashCode2 = synchronization3.hashCode();
        assertNotEquals(synchronization1, synchronization3);
        assertNotEquals(hashCode1, hashCode2);
    }

    @Test
    void testToStringNotNull() {
        assertNotNull(synchronization1);
        String string = synchronization1.toString();
        assertNotNull(string);
    }

    @Test
    void testToString() {
        assertNotNull(synchronization1);
        String string = synchronization1.toString();
        assertTrue(string.contains("title"));
        assertTrue(string.contains("content"));
        assertTrue(string.contains("DELETE"));

    }
}
