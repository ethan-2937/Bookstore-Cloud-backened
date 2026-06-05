package com.hlju.bookstore.repository;

import com.hlju.bookstore.entity.FaqKnowledge;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Repository
public class FaqKnowledgeRepository {
    private final JdbcTemplate jdbcTemplate;

    public FaqKnowledgeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<FaqKnowledge> findAll(String keyword, String category, boolean includeDisabled) {
        StringBuilder sql = new StringBuilder("SELECT * FROM faq_knowledge WHERE 1 = 1");
        List<Object> args = new ArrayList<>();
        if (!includeDisabled) {
            sql.append(" AND enabled = TRUE");
        }
        if (StringUtils.hasText(category)) {
            sql.append(" AND category = ?");
            args.add(category.trim());
        }
        if (StringUtils.hasText(keyword)) {
            String like = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
            sql.append(" AND (LOWER(question) LIKE ? OR LOWER(answer) LIKE ? OR LOWER(category) LIKE ?)");
            args.add(like);
            args.add(like);
            args.add(like);
        }
        sql.append(" ORDER BY category ASC, id ASC");
        return jdbcTemplate.query(sql.toString(), new BeanPropertyRowMapper<>(FaqKnowledge.class), args.toArray());
    }

    public List<FaqKnowledge> searchRelevant(String question, int limit) {
        int safeLimit = limit <= 0 ? 5 : Math.min(limit, 10);
        if (!StringUtils.hasText(question)) {
            return jdbcTemplate.query(
                    "SELECT * FROM faq_knowledge WHERE enabled = TRUE ORDER BY id ASC LIMIT ?",
                    new BeanPropertyRowMapper<>(FaqKnowledge.class),
                    safeLimit
            );
        }
        List<String> terms = extractTerms(question);
        if (terms.isEmpty()) {
            return findAll(null, null, false).stream().limit(safeLimit).toList();
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
            scoreSql.append("(CASE WHEN LOWER(question) LIKE ? THEN 8 ELSE 0 END")
                    .append(" + CASE WHEN LOWER(answer) LIKE ? THEN 5 ELSE 0 END")
                    .append(" + CASE WHEN LOWER(category) LIKE ? THEN 6 ELSE 0 END)");
            scoreArgs.add(like);
            scoreArgs.add(like);
            scoreArgs.add(like);

            if (!whereSql.isEmpty()) {
                whereSql.append(" OR ");
            }
            whereSql.append("(LOWER(question) LIKE ? OR LOWER(answer) LIKE ? OR LOWER(category) LIKE ?)");
            whereArgs.add(like);
            whereArgs.add(like);
            whereArgs.add(like);
        }

        String sql = "SELECT id, category, question, answer, enabled, created_time, updated_time, (" + scoreSql + ") AS relevance_score " +
                "FROM faq_knowledge WHERE enabled = TRUE AND (" + whereSql + ") " +
                "ORDER BY relevance_score DESC, id ASC LIMIT ?";
        List<Object> args = new ArrayList<>(scoreArgs);
        args.addAll(whereArgs);
        args.add(safeLimit);
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(FaqKnowledge.class), args.toArray());
    }

    public List<String> findCategories() {
        String sql = "SELECT DISTINCT category FROM faq_knowledge WHERE category IS NOT NULL AND category <> '' ORDER BY category";
        return jdbcTemplate.queryForList(sql, String.class);
    }

    public boolean save(FaqKnowledge item) {
        String sql = "INSERT INTO faq_knowledge (category, question, answer, enabled) VALUES (?, ?, ?, ?)";
        int rows = jdbcTemplate.update(sql, item.getCategory(), item.getQuestion(), item.getAnswer(),
                item.getEnabled() == null || item.getEnabled());
        return rows > 0;
    }

    public boolean update(Integer id, FaqKnowledge item) {
        String sql = "UPDATE faq_knowledge SET category = ?, question = ?, answer = ?, enabled = ?, updated_time = NOW() WHERE id = ?";
        int rows = jdbcTemplate.update(sql, item.getCategory(), item.getQuestion(), item.getAnswer(),
                item.getEnabled() == null || item.getEnabled(), id);
        return rows > 0;
    }

    public boolean delete(Integer id) {
        String sql = "DELETE FROM faq_knowledge WHERE id = ?";
        int rows = jdbcTemplate.update(sql, id);
        return rows > 0;
    }

    private List<String> extractTerms(String question) {
        String lower = question.toLowerCase(Locale.ROOT);
        List<String> terms = new ArrayList<>();
        addIfContains(lower, terms, "退", "退换货");
        addIfContains(lower, terms, "换", "退换货");
        addIfContains(lower, terms, "配送", "配送");
        addIfContains(lower, terms, "物流", "配送");
        addIfContains(lower, terms, "支付", "支付");
        addIfContains(lower, terms, "付款", "支付");
        addIfContains(lower, terms, "发票", "发票");
        addIfContains(lower, terms, "评论", "评论");
        addIfContains(lower, terms, "评价", "评论");
        addIfContains(lower, terms, "订单", "订单");
        addIfContains(lower, terms, "客服", "客服");
        addIfContains(lower, terms, "账号", "账号");
        addIfContains(lower, terms, "登录", "账号");
        addIfContains(lower, terms, "库存", "库存");
        addIfContains(lower, terms, "缺货", "库存");

        for (String token : lower.split("[\\s,，。！？；;:：]+")) {
            if (token.length() >= 2 && terms.size() < 10) {
                terms.add(token);
            }
        }
        return terms.stream().distinct().limit(10).toList();
    }

    private void addIfContains(String question, List<String> terms, String trigger, String term) {
        if (question.contains(trigger)) {
            terms.add(term);
        }
    }
}
