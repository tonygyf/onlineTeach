package com.example.onlineteach.data.dao;

import com.example.onlineteach.data.model.Book;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import java.util.List;

@Dao
public interface BookDao {
    /**
     * 添加新书籍
     * @param book 要添加的书籍
     * @return 新添加书籍的ID
     */
    @Insert
    long insertBook(Book book);

    /**
     * 获取所有书籍
     * @return 书籍列表
     */
    @Query("SELECT * FROM books")
    List<Book> getAllBooks();

    /**
     * 根据ID获取书籍
     * @param id 书籍ID
     * @return 书籍对象，如果不存在返回null
     */
    @Query("SELECT * FROM books WHERE id = :id")
    Book getBookById(long id);

    /**
     * 更新书籍信息
     * @param book 要更新的书籍
     * @return 更新的行数
     */
    @Update
    int updateBook(Book book);

    /**
     * 删除书籍
     * @param book 要删除的书籍
     */
    @Delete
    void deleteBook(Book book);
}