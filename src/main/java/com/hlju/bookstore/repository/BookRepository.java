package com.hlju.bookstore.repository;

import com.hlju.bookstore.entity.Book;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Repository
public class BookRepository {
    private final JdbcTemplate jdbcTemplate;

    public BookRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Book> findAll() {
        String sql = "SELECT * FROM books ORDER BY id DESC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Book.class));
    }

    public Book findById(Integer id) {
        String sql = "SELECT * FROM books WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Book.class), id);
        } catch (Exception e) {
            return null;
        }
    }

    public List<Book> findAiCandidates(String question, int limit) {
        int safeLimit = limit <= 0 ? 6 : Math.min(limit, 12);
        List<String> terms = extractAiSearchTerms(question);
        if (terms.isEmpty()) {
            String sql = "SELECT * FROM books ORDER BY stock DESC, id DESC LIMIT ?";
            return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Book.class), safeLimit);
        }

        StringBuilder scoreSql = new StringBuilder();
        StringBuilder whereSql = new StringBuilder();
        List<Object> scoreArgs = new ArrayList<>();
        List<Object> whereArgs = new ArrayList<>();
        for (String term : terms) {
            String like = "%" + term.toLowerCase(Locale.ROOT) + "%";
            if (!scoreSql.isEmpty()) {
                scoreSql.append(" + ");
            }
            scoreSql.append("(CASE WHEN LOWER(title) LIKE ? THEN 8 ELSE 0 END")
                    .append(" + CASE WHEN LOWER(category) LIKE ? THEN 6 ELSE 0 END")
                    .append(" + CASE WHEN LOWER(author) LIKE ? THEN 3 ELSE 0 END")
                    .append(" + CASE WHEN LOWER(description) LIKE ? THEN 4 ELSE 0 END)");
            scoreArgs.add(like);
            scoreArgs.add(like);
            scoreArgs.add(like);
            scoreArgs.add(like);

            if (!whereSql.isEmpty()) {
                whereSql.append(" OR ");
            }
            whereSql.append("(LOWER(title) LIKE ? OR LOWER(category) LIKE ? OR LOWER(author) LIKE ? OR LOWER(description) LIKE ?)");
            whereArgs.add(like);
            whereArgs.add(like);
            whereArgs.add(like);
            whereArgs.add(like);
        }

        String sql = "SELECT id, title, author, category, price, stock, cover_url, description, (" + scoreSql + ") AS relevance_score " +
                "FROM books WHERE " + whereSql + " ORDER BY relevance_score DESC, stock DESC, id DESC LIMIT ?";
        List<Object> args = new ArrayList<>(scoreArgs);
        args.addAll(whereArgs);
        args.add(safeLimit);
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Book.class), args.toArray());
    }

    public boolean save(Book book) {
        String sql = "INSERT INTO books (title, author, category, price, stock, cover_url, description) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            int rows = jdbcTemplate.update(sql,
                    book.getTitle(),
                    book.getAuthor(),
                    book.getCategory(),
                    book.getPrice(),
                    book.getStock(),
                    book.getCoverUrl(),
                    book.getDescription());
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM books WHERE id = ?";
        try {
            int rows = jdbcTemplate.update(sql, id);
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean decreaseStock(Integer id, Integer quantity) {
        String sql = "UPDATE books SET stock = stock - ? WHERE id = ? AND stock >= ?";
        try {
            int rows = jdbcTemplate.update(sql, quantity, id, quantity);
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean increaseStock(Integer id, Integer quantity) {
        String sql = "UPDATE books SET stock = stock + ? WHERE id = ?";
        try {
            int rows = jdbcTemplate.update(sql, quantity, id);
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private List<String> extractAiSearchTerms(String question) {
        if (!StringUtils.hasText(question)) {
            return List.of();
        }
        String lowerQuestion = question.toLowerCase(Locale.ROOT);
        Set<String> terms = new LinkedHashSet<>();
        Matcher matcher = Pattern.compile("[a-z0-9+#.]{2,}").matcher(lowerQuestion);
        while (matcher.find()) {
            terms.add(matcher.group());
        }

        addIfContains(lowerQuestion, terms, List.of("java", "后端", "开发", "编程", "spring", "spring boot"),
                List.of("java", "spring", "后端", "编程开发", "mysql", "算法", "计算机"));
        addIfContains(lowerQuestion, terms, List.of("前端", "vue", "javascript", "js", "网页"),
                List.of("前端", "vue", "javascript", "编程开发"));
        addIfContains(lowerQuestion, terms, List.of("数据库", "mysql", "sql", "索引", "性能"),
                List.of("数据库", "mysql", "sql"));
        addIfContains(lowerQuestion, terms, List.of("算法", "数据结构", "面试", "刷题"),
                List.of("算法", "数据结构", "计算机"));
        addIfContains(lowerQuestion, terms, List.of("代码", "架构", "设计模式", "软件工程", "项目管理"),
                List.of("软件工程", "设计模式", "代码", "项目管理"));
        addIfContains(lowerQuestion, terms, List.of("小说", "文学", "故事", "名著"),
                List.of("文学小说", "小说", "文学"));
        addIfContains(lowerQuestion, terms, List.of("科幻", "宇宙", "未来"),
                List.of("科幻", "三体"));
        addIfContains(lowerQuestion, terms, List.of("心理", "自我", "习惯", "成长"),
                List.of("心理成长", "心理", "成长"));
        addIfContains(lowerQuestion, terms, List.of("历史", "社会", "经济", "商业"),
                List.of("历史", "社会科学", "经济商业"));

        return terms.stream().limit(10).toList();
    }

    private void addIfContains(String question, Set<String> terms, List<String> triggers, List<String> additions) {
        if (triggers.stream().anyMatch(question::contains)) {
            terms.addAll(additions);
        }
    }
}
