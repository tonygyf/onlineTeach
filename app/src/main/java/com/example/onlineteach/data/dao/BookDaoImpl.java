package com.example.onlineteach.data.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.onlineteach.data.model.Book;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookDaoImpl implements BookDao {
    private static final String DATABASE_NAME = "bookshelf.db";
    private static final int DATABASE_VERSION = 1;
    
    // 表名
    private static final String TABLE_BOOKS = "books";
    
    // 列名常量
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_FILE_PATH = "file_path";
    private static final String COLUMN_FILE_TYPE = "file_type";
    private static final String COLUMN_FILE_SIZE = "file_size";
    private static final String COLUMN_ADDED_DATE = "added_date";
    private static final String COLUMN_COVER_PATH = "cover_path";
    
    // 日期格式
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    
    // 创建表的SQL语句
    private static final String CREATE_BOOKS_TABLE = "CREATE TABLE " + TABLE_BOOKS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_TITLE + " TEXT, "
            + COLUMN_FILE_PATH + " TEXT, "
            + COLUMN_FILE_TYPE + " TEXT, "
            + COLUMN_FILE_SIZE + " INTEGER, "
            + COLUMN_ADDED_DATE + " TEXT, "
            + COLUMN_COVER_PATH + " TEXT"
            + ")";
    
    private final SQLiteOpenHelper dbHelper;

    public BookDaoImpl(Context context) {
        this.dbHelper = new SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
            @Override
            public void onCreate(SQLiteDatabase db) {
                db.execSQL(CREATE_BOOKS_TABLE);
            }
            
            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                // 如果数据库版本升级，可以在这里处理数据迁移
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKS);
                onCreate(db);
            }
        };
    }

    @Override
    public long insertBook(Book book) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_TITLE, book.getTitle());
        values.put(COLUMN_FILE_PATH, book.getFilePath());
        values.put(COLUMN_FILE_TYPE, book.getFileType());
        values.put(COLUMN_FILE_SIZE, book.getFileSize());
        values.put(COLUMN_ADDED_DATE, dateFormat.format(book.getAddedDate()));
        values.put(COLUMN_COVER_PATH, book.getCoverPath());
        
        long id = db.insert(TABLE_BOOKS, null, values);
        db.close();
        return id;
    }

    @Override
    public List<Book> getAllBooks() {
        List<Book> bookList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_BOOKS + " ORDER BY " + COLUMN_ADDED_DATE + " DESC";
        
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        if (cursor.moveToFirst()) {
            do {
                bookList.add(createBookFromCursor(cursor));
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        return bookList;
    }

    @Override
    public Book getBookById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Book book = null;
        Cursor cursor = db.query(TABLE_BOOKS, null, COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
        
        try {
            if (cursor.moveToFirst()) {
                book = createBookFromCursor(cursor);
            }
        } finally {
            cursor.close();
            db.close();
        }
        return book;
    }

    @Override
    public int updateBook(Book book) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_TITLE, book.getTitle());
        values.put(COLUMN_FILE_PATH, book.getFilePath());
        values.put(COLUMN_FILE_TYPE, book.getFileType());
        values.put(COLUMN_FILE_SIZE, book.getFileSize());
        values.put(COLUMN_ADDED_DATE, dateFormat.format(book.getAddedDate()));
        values.put(COLUMN_COVER_PATH, book.getCoverPath());
        
        int result = db.update(TABLE_BOOKS, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(book.getId())});
        db.close();
        return result;
    }

    @Override
    public void deleteBook(Book book) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(TABLE_BOOKS, COLUMN_ID + " = ?",
                new String[]{String.valueOf(book.getId())});
        db.close();
    }

    private Book createBookFromCursor(Cursor cursor) {
        Book book = new Book();
        
        int idIndex = cursor.getColumnIndex(COLUMN_ID);
        int titleIndex = cursor.getColumnIndex(COLUMN_TITLE);
        int filePathIndex = cursor.getColumnIndex(COLUMN_FILE_PATH);
        int fileTypeIndex = cursor.getColumnIndex(COLUMN_FILE_TYPE);
        int fileSizeIndex = cursor.getColumnIndex(COLUMN_FILE_SIZE);
        int addedDateIndex = cursor.getColumnIndex(COLUMN_ADDED_DATE);
        int coverPathIndex = cursor.getColumnIndex(COLUMN_COVER_PATH);
        
        book.setId(idIndex != -1 ? cursor.getLong(idIndex) : 0);
        book.setTitle(titleIndex != -1 ? cursor.getString(titleIndex) : "");
        book.setFilePath(filePathIndex != -1 ? cursor.getString(filePathIndex) : "");
        book.setFileType(fileTypeIndex != -1 ? cursor.getString(fileTypeIndex) : "");
        book.setFileSize(fileSizeIndex != -1 ? cursor.getLong(fileSizeIndex) : 0);
        
        try {
            if (addedDateIndex != -1) {
                String dateStr = cursor.getString(addedDateIndex);
                Date date = dateFormat.parse(dateStr);
                book.setAddedDate(date);
            } else {
                book.setAddedDate(new Date());
            }
        } catch (ParseException e) {
            e.printStackTrace();
            book.setAddedDate(new Date());
        }
        
        book.setCoverPath(coverPathIndex != -1 ? cursor.getString(coverPathIndex) : "");
        return book;
    }
}