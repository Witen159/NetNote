package server.api;

import commons.Note;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import server.database.NoteRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class NoteRepositoryTest {

    @Autowired
    private NoteRepository noteRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private Note note1;
    private Note note2;

    @BeforeEach
    public void setUp() {
        note1 = new Note();
        note1.title = "testTitle";
        note1.setContent("testContent");

        note2 = new Note();
        note2.title = "testTitle2";
        note2.setContent("testContent2");

        noteRepository.saveAll(List.of(note1, note2));
    }

    @AfterEach
    public void tearDown() {
        noteRepository.deleteAll();
    }

    @Test
    public void testFindAll() {
        List<Note> notes = noteRepository.findAll();
        assertEquals(2, notes.size());
        Note note = notes.getFirst();
        assertEquals("testTitle", note.title);
        assertEquals("testContent", note.getContent());
    }

    @Test
    public void testFindAllWithSort() {
        Sort sort = Sort.by(Sort.Direction.ASC, "title");
        List<Note> sortedNotes = noteRepository.findAll(sort);
        assertEquals("testTitle", sortedNotes.get(0).title);
        assertEquals("testTitle2", sortedNotes.get(1).title);
    }

    @Test
    public void testFindAllWithPagination() {
        Pageable pageable = PageRequest.of(0,1, Sort.by(Sort.Direction.ASC, "title"));
        Page<Note> page = noteRepository.findAll(pageable);
        assertEquals(1, page.getContent().size());
        assertEquals("testTitle", page.getContent().getFirst().title);
    }

    @Test
    public void testFindById() {
        Note savedNote = noteRepository.findAll().getFirst();
        Optional<Note> note = noteRepository.findById(savedNote.id);
        assertTrue(note.isPresent());
        Note testNote = note.get();
        assertNotNull(testNote);
        assertEquals("testTitle", testNote.title);
        assertEquals("testContent", testNote.getContent());
    }

    @Test
    public void testSaveAll() {
        List<Note> savedNotes = noteRepository.findAll();
        assertEquals(2, savedNotes.size());
        assertTrue(savedNotes.contains(note1));
        assertTrue(savedNotes.contains(note2));
    }

    @Test
    public void testFlush() {
        note1.setContent("updatedContent");
        noteRepository.save(note1);
        noteRepository.flush();
        Note updatedNote = noteRepository.findById(note1.id)
                .orElseThrow(() -> new RuntimeException("Note not found"));
        assertEquals("updatedContent", updatedNote.getContent());
    }

    @Test
    public void testSaveAndFlush() {
        Note newNote = new Note();
        newNote.title = "testTitle";
        newNote.setContent("testContent");
        Note savedNote = noteRepository.saveAndFlush(newNote);
        Note retrievedNote = noteRepository.findById(savedNote.id)
                .orElseThrow(() -> new RuntimeException("Note not found"));
        assertNotNull(retrievedNote);
        assertEquals("testTitle", retrievedNote.title);
        assertEquals("testContent", retrievedNote.getContent());
    }

    @Test
    public void testDelete() {
        assertTrue(noteRepository.existsById(note1.id));
        assertTrue(noteRepository.existsById(note2.id));
        noteRepository.delete(note1);
        assertFalse(noteRepository.existsById(note1.id));
        assertTrue(noteRepository.existsById(note2.id));
    }

    @Test
    public void testDeleteById() {
        assertTrue(noteRepository.existsById(note1.id));
        noteRepository.deleteById(note1.id);
        assertFalse(noteRepository.existsById(note1.id));
        assertTrue(noteRepository.existsById(note2.id));
    }

    @Test
    public void deleteAllInBatch() {
        assertEquals(2, noteRepository.count());
        noteRepository.deleteAllInBatch();
        assertEquals(0, noteRepository.count());
    }

    @Test
    public void deleteAllByIdInBatch() {
        assertEquals(2, noteRepository.count());
        List<Long> idsToDelete = List.of(note1.id, note2.id);
        noteRepository.deleteAllByIdInBatch(idsToDelete);
        assertEquals(0, noteRepository.count());
        assertFalse(noteRepository.existsById(note1.id));
        assertFalse(noteRepository.existsById(note2.id));
    }

    @Test
    public void testGetReferenceById() {
        Note noteReference = noteRepository.getReferenceById(note1.id);
        assertNotNull(noteReference);
        assertTrue(entityManager.contains(noteReference));
        String title = noteReference.title;
        String content = noteReference.getContent();
        assertEquals("testTitle", title);
        assertEquals("testContent", content);
    }

    @Test
    public void testCount() {
        long initialCount = noteRepository.count();
        assertEquals(2, noteRepository.count());
        Note newNote = new Note();
        newNote.title = "newTitle";
        newNote.setContent("newContent");
        noteRepository.save(newNote);
        long updatedCount = noteRepository.count();
        assertEquals(initialCount + 1, updatedCount);
    }

    @Test
    public void testExists() {
        assertTrue(noteRepository.existsById(note1.id));
        assertTrue(noteRepository.existsById(note2.id));
        Long nonExistentId = 9999L;
        assertFalse(noteRepository.existsById(nonExistentId));
    }
}
