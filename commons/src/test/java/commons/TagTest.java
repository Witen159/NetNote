package commons;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TagTest {
    private Tag tag1;
    private Tag tag2;
    private Tag tag3;

    @BeforeEach
    void setUp() {
        tag1 = new Tag("tagTitle1");
        tag2 = new Tag("tagTitle1");
        tag3 = new Tag("TagTitle0");
    }

    @AfterEach
    void tearDown() {
        tag1 = null;
        tag2 = null;
        tag3 = null;
    }

    @Test
    void testEqual() {
        assertEquals(tag1, tag2);
    }

    @Test
    void testNotEqual() {
        assertNotEquals(tag1, tag3);
    }

    @Test
    void testHashCodeEquals() {
        int hashCode1 = tag1.hashCode();
        int hashCode2 = tag2.hashCode();
        assertEquals(tag1, tag2);
        assertEquals(hashCode1, hashCode2);
    }

    @Test
    void testHashCodeNotEqual() {
        int hashCode1 = tag1.hashCode();
        int hashCode2 = tag3.hashCode();
        assertNotEquals(tag1, tag3);
        assertNotEquals(hashCode1, hashCode2);
    }
}
