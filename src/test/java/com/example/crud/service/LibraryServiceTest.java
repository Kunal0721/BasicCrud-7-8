package com.example.crud.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.crud.entity.Book;
import com.example.crud.entity.BookIssueRecord;
import com.example.crud.entity.IssueStatus;
import com.example.crud.entity.Student;
import com.example.crud.dto.IssueRequest;
import com.example.crud.dto.ReturnRequest;
import com.example.crud.repository.BookIssueRepository;
import com.example.crud.repository.BookRepository;
import com.example.crud.repository.StudentRepository;

@ExtendWith(MockitoExtension.class)
public class LibraryServiceTest {

    @Mock
    private BookIssueRepository issueRepo;

    @Mock
    private BookRepository bookRepo;

    @Mock
    private StudentRepository studentRepo;

    @InjectMocks
    private LibraryService libraryService;

    private Student student;
    private Book book;

    @BeforeEach
    void setUp() {
        student = new Student(1L, "Shivam", 20);
        book = new Book(1L, "Spring Boot", "Author A", "1234567890", 5, 5);
    }

    @Test
    void testIssueBook_Success() {
        IssueRequest request = new IssueRequest();
        request.setStudentId(1L);
        request.setBookId(1L);
        request.setDurationDays(14);

        when(studentRepo.findById(1L)).thenReturn(Optional.of(student));
        when(bookRepo.findById(1L)).thenReturn(Optional.of(book));
        when(issueRepo.findByStudentIdAndBookIdAndReturnDateIsNull(1L, 1L)).thenReturn(Optional.empty());
        when(issueRepo.save(any(BookIssueRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookIssueRecord record = libraryService.issueBook(request);

        assertNotNull(record);
        assertEquals(student, record.getStudent());
        assertEquals(book, record.getBook());
        assertEquals(4, book.getAvailableCopies());
        assertEquals(IssueStatus.ISSUED, record.getStatus());
        verify(bookRepo, times(1)).save(book);
        verify(issueRepo, times(1)).save(any(BookIssueRecord.class));
    }

    @Test
    void testIssueBook_NoCopiesAvailable() {
        book.setAvailableCopies(0);

        IssueRequest request = new IssueRequest();
        request.setStudentId(1L);
        request.setBookId(1L);

        when(studentRepo.findById(1L)).thenReturn(Optional.of(student));
        when(bookRepo.findById(1L)).thenReturn(Optional.of(book));
        when(issueRepo.findByStudentIdAndBookIdAndReturnDateIsNull(1L, 1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            libraryService.issueBook(request);
        });

        assertEquals("No available copies of this book left to issue!", exception.getMessage());
        verify(bookRepo, never()).save(any());
        verify(issueRepo, never()).save(any());
    }

    @Test
    void testIssueBook_AlreadyIssued() {
        IssueRequest request = new IssueRequest();
        request.setStudentId(1L);
        request.setBookId(1L);

        BookIssueRecord activeRecord = new BookIssueRecord();

        when(studentRepo.findById(1L)).thenReturn(Optional.of(student));
        when(bookRepo.findById(1L)).thenReturn(Optional.of(book));
        when(issueRepo.findByStudentIdAndBookIdAndReturnDateIsNull(1L, 1L)).thenReturn(Optional.of(activeRecord));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            libraryService.issueBook(request);
        });

        assertEquals("Student already has an active issue record for this book!", exception.getMessage());
        verify(bookRepo, never()).save(any());
        verify(issueRepo, never()).save(any());
    }

    @Test
    void testReturnBook_Success() {
        ReturnRequest request = new ReturnRequest();
        request.setStudentId(1L);
        request.setBookId(1L);

        book.setAvailableCopies(4);

        BookIssueRecord activeRecord = new BookIssueRecord();
        activeRecord.setId(10L);
        activeRecord.setStudent(student);
        activeRecord.setBook(book);
        activeRecord.setStatus(IssueStatus.ISSUED);

        when(studentRepo.findById(1L)).thenReturn(Optional.of(student));
        when(bookRepo.findById(1L)).thenReturn(Optional.of(book));
        when(issueRepo.findByStudentIdAndBookIdAndReturnDateIsNull(1L, 1L)).thenReturn(Optional.of(activeRecord));
        when(issueRepo.save(any(BookIssueRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookIssueRecord record = libraryService.returnBook(request);

        assertNotNull(record);
        assertEquals(IssueStatus.RETURNED, record.getStatus());
        assertNotNull(record.getReturnDate());
        assertEquals(5, book.getAvailableCopies());
        verify(bookRepo, times(1)).save(book);
        verify(issueRepo, times(1)).save(activeRecord);
    }
}
