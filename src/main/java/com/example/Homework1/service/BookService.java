package com.example.Homework1.service;

import com.example.Homework1.entity.Book;
import java.util.List;
import java.util.Optional;
public interface BookService {
    Book saveBook(Book book); // 新增書籍
    List<Book> getAllBooks(); // 取得所有書籍
    Optional<Book> getBookById(Long id); // 透過 ID 取得書籍
    Book updateBook(Long id, Book bookDetails); // 更新書籍
    void deleteBook(Long id); // 刪除書籍
}
