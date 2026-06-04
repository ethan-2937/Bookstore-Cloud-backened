package com.hlju.bookstore.service;

import com.hlju.bookstore.entity.Book;
import com.hlju.bookstore.entity.PageResult;

import java.util.List;

public interface BookService {
    List<Book> findAllBooks();
    PageResult<Book> findBooksPage(Integer pageNum, Integer pageSize, String keyword, String category);
    List<String> findCategories();
    Book findBookById(Integer id);
    boolean saveBook(Book book);
    boolean deleteBookById(Integer id);
}
