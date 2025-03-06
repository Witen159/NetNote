package server.services;

import commons.Collection;
import commons.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import server.database.NoteRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NoteServiceImplTest {

    private NoteServiceImpl noteService;

    @Mock
    private TagService tagService;

    @Mock
    private NoteRepository noteRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        noteService = new NoteServiceImpl(noteRepository, tagService);
    }

    @Test
    void findNote_ValidId_ReturnsNote() {
        Note mockNote = new Note();
        mockNote.id = 1L;
        when(noteRepository.existsById(1L)).thenReturn(true);
        when(noteRepository.findById(1L)).thenReturn(Optional.of(mockNote));

        Note result = noteService.findNote(1L);

        assertNotNull(result);
        assertEquals(1L, result.id);
    }

    @Test
    void findNote_InvalidId_ReturnsNull() {
        when(noteRepository.existsById(2L)).thenReturn(false);

        Note result = noteService.findNote(2L);

        assertNull(result);
    }

    @Test
    void saveNote_ValidNote_ReturnsSavedNote() {
        Note validNote = new Note();
        validNote.title = "Test Title";
        validNote.setContent("Test Content");
        validNote.collection = new Collection();
        when(noteRepository.save(validNote)).thenReturn(validNote);

        Note result = noteService.saveNote(validNote);

        assertNotNull(result);
        assertEquals("Test Title", result.title);
    }

    @Test
    void saveNote_InvalidNote_ReturnsNull() {
        Note invalidNote = new Note(); // Missing required fields
        invalidNote.title = null;
        invalidNote.setContent("Content");
        invalidNote.collection = new Collection();

        Note result = noteService.saveNote(invalidNote);

        assertNull(result);
        verify(noteRepository, never()).save(any(Note.class));
    }

    @Test
    void deleteNote_ExistingNote_DeletesAndReturnsNote() {
        Note existingNote = new Note();
        existingNote.id = 1L;
        when(noteRepository.existsById(1L)).thenReturn(true);

        Note result = noteService.deleteNote(existingNote);

        assertNotNull(result);
        verify(noteRepository, times(1)).delete(existingNote);
    }

    @Test
    void deleteNote_NonExistingNote_ReturnsNull() {
        Note nonExistingNote = new Note();
        nonExistingNote.id = 2L;
        when(noteRepository.existsById(2L)).thenReturn(false);

        Note result = noteService.deleteNote(nonExistingNote);

        assertNull(result);
        verify(noteRepository, never()).delete(any(Note.class));
    }

    @Test
    void findAllNotes_ReturnsListOfNotes() {
        List<Note> notes = new ArrayList<>();
        notes.add(new Note());
        notes.add(new Note());
        when(noteRepository.findAll()).thenReturn(notes);

        List<Note> result = noteService.findAllNotes();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void findNotesCollection_ExistingNote_ReturnsCollection() {
        Collection mockCollection = new Collection();
        Note noteWithCollection = new Note();
        noteWithCollection.id = 1L;
        noteWithCollection.collection = mockCollection;
        when(noteRepository.existsById(1L)).thenReturn(true);
        when(noteRepository.findById(1L)).thenReturn(Optional.of(noteWithCollection));

        Collection result = noteService.findNotesCollection(1L);

        assertNotNull(result);
        assertEquals(mockCollection, result);
    }

    @Test
    void findNotesCollection_NonExistingNote_ReturnsNull() {
        when(noteRepository.existsById(2L)).thenReturn(false);

        Collection result = noteService.findNotesCollection(2L);

        assertNull(result);
    }
}
