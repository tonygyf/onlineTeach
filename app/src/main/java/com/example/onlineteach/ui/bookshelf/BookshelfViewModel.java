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
    public BookshelfViewModel(@NonNull Application application) {
        super(application);
        bookRepository = new BookRepository(application);
        loadBooks();
    }

    public LiveData<List<Book>> getBooks() {
        return books;
    }

    private void loadBooks() {
        bookRepository.getAllBooks(new BookRepository.BookListCallback() {
            @Override
            public void onBooksLoaded(List<Book> bookList) {
                books.postValue(bookList);
            }

            @Override
            public void onError(String errorMessage) {
                // 可以在这里处理错误，例如通知UI显示错误信息
            }
        });
    }

    public void addBook(Book book) {
        bookRepository.addBook(book, new BookRepository.BookOperationCallback() {
            @Override
            public void onSuccess() {
                loadBooks(); // 成功后重新加载数据
            }

            @Override
            public void onError(String errorMessage) {
                // 可以在这里处理错误，例如通知UI显示错误信息
            }
        });
    }

    public void deleteBook(Book book) {
        bookRepository.deleteBook(book, new BookRepository.BookOperationCallback() {
            @Override
            public void onSuccess() {
                loadBooks(); // 成功后重新加载数据
            }

            @Override
            public void onError(String errorMessage) {
                // 可以在这里处理错误，例如通知UI显示错误信息
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        bookRepository.shutdownExecutor();
    }
}