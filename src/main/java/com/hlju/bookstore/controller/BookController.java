package com.hlju.bookstore.controller;

import com.hlju.bookstore.entity.Book;
import com.hlju.bookstore.entity.PageResult;
import com.hlju.bookstore.service.BookService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api")
public class BookController {
    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/books")
    public PageResult<Book> getBooks(@RequestParam(defaultValue = "1") Integer pageNum,
                                     @RequestParam(defaultValue = "8") Integer pageSize,
                                     @RequestParam(required = false) String keyword,
                                     @RequestParam(required = false) String category) {
        return bookService.findBooksPage(pageNum, pageSize, keyword, category);
    }

    @GetMapping("/books/categories")
    public List<String> getBookCategories() {
        return bookService.findCategories();
    }

    @GetMapping("/books/{id}")
    public Book getBook(@PathVariable Integer id) {
        Book book = bookService.findBookById(id);
        if (book == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found");
        }
        return book;
    }

    @PostMapping("/books")
    public boolean addBook(@RequestBody Book book,
                           @RequestParam String username,
                           @RequestParam String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No permission");
        }
        return bookService.saveBook(book);
    }

    @DeleteMapping("/books/{id}")
    public boolean deleteBook(@PathVariable Integer id,
                              @RequestParam String username,
                              @RequestParam String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No permission");
        }
        return bookService.deleteBookById(id);
    }
}
