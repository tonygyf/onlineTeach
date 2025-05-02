package com.example.onlineteach.ui.bookshelf;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;


import com.example.onlineteach.data.model.Book;
import com.example.onlineteach.data.repository.BookRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BookshelfViewModel extends AndroidViewModel {
    private final BookRepository bookRepository;
    private final MutableLiveData<List<Book>> books = new MutableLiveData<>();
    private final ExecutorService executorService;

    public BookshelfViewModel(@NonNull Application application) {
        super(application);
        bookRepository = new BookRepository(application);
        executorService = Executors.newSingleThreadExecutor();
        loadBooks();
    }

    public LiveData<List<Book>> getBooks() {
        return books;
    }

    private void loadBooks() {
        executorService.execute(() -> {
            List<Book> bookList = bookRepository.getAllBooks();
            books.postValue(bookList);
        });
    }

    public void addBook(Book book) {
        executorService.execute(() -> {
            long id = bookRepository.addBook(book);
            book.setId(id);
            loadBooks(); // 重新加载数据
        });
    }

    public void deleteBook(Book book) {
        executorService.execute(() -> {
            bookRepository.deleteBook(book);
            loadBooks(); // 重新加载数据
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}