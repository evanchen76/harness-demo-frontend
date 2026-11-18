package com.example.shop.product;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 前台商品查詢（唯讀）。可見條件：商品 ON_SHELF 且分類啟用。
 * orderBy 由 {@link StorefrontProductService} 從固定的排序清單選出，不是使用者輸入。
 */
@Mapper
public interface StorefrontProductMapper {

    String VISIBLE_FROM_WHERE = " FROM product p JOIN category c ON c.id = p.category_id"
            + " WHERE p.status = 'ON_SHELF' AND c.enabled = 1";

    String FILTERS = "<if test='categoryId != null'> AND p.category_id = #{categoryId}</if>"
            + "<if test='keyword != null'> AND LOWER(p.name) LIKE CONCAT('%', #{keyword}, '%') ESCAPE '\\\\'</if>";

    @Select("<script>SELECT p.id, p.name, p.price, p.main_image_url, c.name AS category_name, p.stock"
            + VISIBLE_FROM_WHERE + FILTERS
            + " ORDER BY ${orderBy} LIMIT #{size} OFFSET #{offset}</script>")
    List<StorefrontProductRow> selectPage(@Param("categoryId") Long categoryId, @Param("keyword") String keyword,
                                          @Param("orderBy") String orderBy, @Param("size") int size,
                                          @Param("offset") long offset);

    @Select("<script>SELECT COUNT(*)" + VISIBLE_FROM_WHERE + FILTERS + "</script>")
    long count(@Param("categoryId") Long categoryId, @Param("keyword") String keyword);

    @Select("SELECT p.id, p.name, p.price, p.main_image_url, c.name AS category_name, p.stock, p.description"
            + VISIBLE_FROM_WHERE + " AND p.id = #{id}")
    StorefrontProductRow selectVisibleById(@Param("id") long id);
}
