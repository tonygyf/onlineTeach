package com.example.onlineteach.data.repository;

import android.content.Context;

import com.example.onlineteach.data.dao.BookDao;
import com.example.onlineteach.data.dao.BookDaoImpl;
import com.example.onlineteach.data.model.Book;

import java.util.List;

/**
 * 书籍仓库类，作为数据访问的统一接口
 */
public class BookRepository {
    private final BookDao bookDao;

    public BookRepository(Context context) {
        this.bookDao = new BookDaoImpl(context);
    }

    /**
     * 添加新书籍
     * @param book 要添加的书籍
     * @return 新添加书籍的ID
     */
    public long addBook(Book book) {
        return bookDao.insertBook(book);
    }

    /**
     * 获取所有书籍
     * @return 书籍列表
     */
    public List<Book> getAllBooks() {
        return bookDao.getAllBooks();
    }

    /**
     * 根据ID获取书籍
     * @param id 书籍ID
     * @return 书籍对象
     */
    public Book getBook(long id) {
        return bookDao.getBookById(id);
    }

    /**
     * 更新书籍信息
     * @param book 要更新的书籍
     * @return 是否更新成功
     */
    public boolean updateBook(Book book) {
        return bookDao.updateBook(book) > 0;
    }


    public void deleteBook(Book book) {
        bookDao.deleteBook(book);
    }
}