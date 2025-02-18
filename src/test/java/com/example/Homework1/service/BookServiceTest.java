package com.example.Homework1.service;

import com.example.Homework1.dao.BookRepository;
import com.example.Homework1.entity.Book;
import com.example.Homework1.serviceImpl.BookServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BookServiceTest {

    @Mock
    private BookRepository bookRepository;  // 🔥 Mock `BookRepository`

    @InjectMocks
    private BookServiceImpl bookService; // 🔥 測試的目標

    private Book testBook;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // ✅ 設定測試書籍
        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("Mocking in Java");
        testBook.setAuthor("Mockito Expert");
        testBook.setCategory("Programming");
        testBook.setPrice(BigDecimal.valueOf(500.0));
        testBook.setSaleprice(BigDecimal.valueOf(450.0));
        testBook.setDescription("An advanced guide to Mockito.");
    }

    @Test
    void testSaveBook() {
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        Book savedBook = bookService.saveBook(testBook);

        assertNotNull(savedBook);
        assertEquals("Mocking in Java", savedBook.getTitle());
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void testGetBookById() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

        Optional<Book> foundBook = bookService.getBookById(1L);

        assertTrue(foundBook.isPresent());
        assertEquals("Mocking in Java", foundBook.get().getTitle());
        verify(bookRepository, times(1)).findById(1L);
    }

    @Test
    void testGetAllBooks() {
        when(bookRepository.findAll()).thenReturn(List.of(testBook));

        List<Book> books = bookService.getAllBooks();

        assertFalse(books.isEmpty());
        assertEquals(1, books.size());
        verify(bookRepository, times(1)).findAll();
    }

    @Test
    void testUpdateBook() {
        Book updatedDetails = new Book();
        updatedDetails.setTitle("Updated Title");

        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        Book updatedBook = bookService.updateBook(1L, updatedDetails);

        assertNotNull(updatedBook);
        assertEquals("Updated Title", updatedBook.getTitle());
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void testDeleteBook() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        doNothing().when(bookRepository).deleteById(1L);

        bookService.deleteBook(1L);

        verify(bookRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteBook_NotFound() {
        // **Mock `findById()` 返回 `Optional.empty()`，表示書籍不存在**
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        // **確保 `deleteBook(99L)` 會拋出 `IllegalArgumentException`**
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookService.deleteBook(99L);
        });

        // **驗證異常訊息是否正確**
        assertEquals("Book not found", exception.getMessage());

        // **確保 `deleteById()` 不會被呼叫，因為書籍不存在**
        verify(bookRepository, never()).deleteById(99L);
    }
}
