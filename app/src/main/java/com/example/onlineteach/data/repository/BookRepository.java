package com.example.onlineteach.data.repository;

import android.app.Application;
import android.util.Log;

import com.example.onlineteach.data.dao.BookDao;
import com.example.onlineteach.data.database.AppDatabase;
import com.example.onlineteach.data.model.Book;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 书籍仓库类，作为数据访问的统一接口
 */
public class BookRepository {
    private static final String TAG = "BookRepository";
    private final BookDao bookDao;
    private final ExecutorService executorService;

    public BookRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        bookDao = db.bookDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * 添加新书籍
     * @param book 要添加的书籍
     * @param callback 添加结果回调
     */
    public void addBook(Book book, BookOperationCallback callback) {
        executorService.execute(() -> {
            try {
                long id = bookDao.insertBook(book);
                if (id > 0) {
                    Log.d(TAG, "Book inserted: " + book.getTitle());
                    callback.onSuccess();
                } else {
                    callback.onError("添加书籍失败");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error inserting book: " + e.getMessage());
                callback.onError("添加书籍时发生错误");
            }
        });
    }

    /**
     * 获取所有书籍
     * @param callback 查询结果回调
     */
    public void getAllBooks(BookListCallback callback) {
        executorService.execute(() -> {
            try {
                List<Book> books = bookDao.getAllBooks();
                callback.onBooksLoaded(books);
            } catch (Exception e) {
                Log.e(TAG, "Error getting all books: " + e.getMessage());
                callback.onError("获取书籍列表失败");
            }
        });
    }

    /**
     * 根据ID获取书籍
     * @param id 书籍ID
     * @param callback 查询结果回调
     */
    public void getBook(long id, BookCallback callback) {
        executorService.execute(() -> {
            try {
                Book book = bookDao.getBookById(id);
                if (book != null) {
                    callback.onBookLoaded(book);
                } else {
                    callback.onError("未找到指定书籍");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting book by id: " + e.getMessage());
                callback.onError("获取书籍信息失败");
            }
        });
    }

    /**
     * 更新书籍信息
     * @param book 要更新的书籍
     * @param callback 更新结果回调
     */
    public void updateBook(Book book, BookOperationCallback callback) {
        executorService.execute(() -> {
            try {
                int result = bookDao.updateBook(book);
                if (result > 0) {
                    Log.d(TAG, "Book updated: " + book.getTitle());
                    callback.onSuccess();
                } else {
                    callback.onError("更新书籍失败");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating book: " + e.getMessage());
                callback.onError("更新书籍时发生错误");
            }
        });
    }

    /**
     * 删除书籍
     * @param book 要删除的书籍
     * @param callback 删除结果回调
     */
    public void deleteBook(Book book, BookOperationCallback callback) {
        executorService.execute(() -> {
            try {
                bookDao.deleteBook(book);
                Log.d(TAG, "Book deleted: " + book.getTitle());
                callback.onSuccess();
            } catch (Exception e) {
                Log.e(TAG, "Error deleting book: " + e.getMessage());
                callback.onError("删除书籍时发生错误");
            }
        });
    }

    // 回调接口
    public interface BookOperationCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    public interface BookCallback {
        void onBookLoaded(Book book);
        void onError(String errorMessage);
    }

    public interface BookListCallback {
        void onBooksLoaded(List<Book> books);
        void onError(String errorMessage);
    }

    /**
     * 关闭执行器服务
     */
    public void shutdownExecutor() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            Log.d(TAG, "ExecutorService shut down.");
        }
    }
}