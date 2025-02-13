package com.example.Homework1.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.Homework1.entity.Book;
import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {
    
    // 透過書籍分類查找
    List<Book> findByCategory(String category);
}
