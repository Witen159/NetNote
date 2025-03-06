package commons;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class CollectionTest {
    private Collection c1;

    @BeforeEach
    void setUp() {
        c1 = new Collection("CollectionTitleTest", "https://urlTest");
    }

    @AfterEach
    void tearDown() {
        c1 = null;
    }

    @Test
    void testEquals() {
        Collection c2 = new Collection("CollectionTitleTest", "https://urlTest");
        assertEquals(c1, c2);
    }

    @Test
    void testNotEquals() {
        Collection differentCollection = new Collection("DifferentTitleTest", "https://urlTest");
        assertNotEquals(c1, differentCollection);
    }

    @Test
    void testHashCodeTrue() {
        Collection c2 = new Collection("CollectionTitleTest", "https://urlTest");
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    void testHashCodeFalse() {
        Collection differentCollection = new Collection("DifferentTitleTest", "https://urlTest");
        assertNotEquals(c1.hashCode(), differentCollection.hashCode());
    }

    @Test
    void testToString() {
        String expectedString = "Collection CollectionTitleTest, connect by URL: https://urlTest. Unique id: " + c1.getId();
        assertEquals(expectedString, c1.toString());
    }

}