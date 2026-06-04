package com.hlju.bookstore.mapper;

import com.hlju.bookstore.entity.Book;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BookMapper {
    @Select({
            "<script>",
            "SELECT id, title, author, category, price, stock, cover_url, description",
            "FROM books",
            "<where>",
            "  <if test='keyword != null and keyword != \"\"'>",
            "    AND (title LIKE CONCAT('%', #{keyword}, '%')",
            "      OR author LIKE CONCAT('%', #{keyword}, '%')",
            "      OR category LIKE CONCAT('%', #{keyword}, '%'))",
            "  </if>",
            "  <if test='category != null and category != \"\" and category != \"全部\"'>",
            "    AND category = #{category}",
            "  </if>",
            "</where>",
            "ORDER BY id DESC",
            "</script>"
    })
    List<Book> selectBooks(@Param("keyword") String keyword, @Param("category") String category);

    @Select("SELECT DISTINCT category FROM books WHERE category IS NOT NULL AND category <> '' ORDER BY category")
    List<String> selectCategories();
}
