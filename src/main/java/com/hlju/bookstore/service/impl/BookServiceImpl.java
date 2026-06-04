package com.hlju.bookstore.service.impl;

import com.hlju.bookstore.entity.Book;
import com.hlju.bookstore.entity.PageResult;
import com.hlju.bookstore.mapper.BookMapper;
import com.hlju.bookstore.repository.BookRepository;
import com.hlju.bookstore.service.BookService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    public BookServiceImpl(BookRepository bookRepository, BookMapper bookMapper) {
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
    }

    @Override
    public List<Book> findAllBooks() {
        return bookRepository.findAll();
    }

    @Override
    public PageResult<Book> findBooksPage(Integer pageNum, Integer pageSize, String keyword, String category) {
        int safePageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int safePageSize = pageSize == null || pageSize < 1 ? 8 : Math.min(pageSize, 50);
        PageHelper.startPage(safePageNum, safePageSize);
        List<Book> books = bookMapper.selectBooks(trimToNull(keyword), trimToNull(category));
        return PageResult.from(new PageInfo<>(books));
    }

    @Override
    public List<String> findCategories() {
        return bookMapper.selectCategories();
    }

    @Override
    public Book findBookById(Integer id) {
        return bookRepository.findById(id);
    }

    @Override
    public boolean saveBook(Book book) {
        return bookRepository.save(book);
    }

    @Override
    public boolean deleteBookById(Integer id) {
        return bookRepository.deleteById(id);
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
