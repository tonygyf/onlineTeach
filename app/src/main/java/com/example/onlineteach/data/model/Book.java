package com.example.onlineteach.data.model;

import java.util.Date;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.TypeConverters;
import androidx.room.Ignore;
import com.example.onlineteach.data.converter.DateConverter;

@Entity(tableName = "books")
@TypeConverters(DateConverter.class)
public class Book {
    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "file_path")
    private String filePath;

    @ColumnInfo(name = "file_type")
    private String fileType;

    @ColumnInfo(name = "file_size")
    private long fileSize;

    @ColumnInfo(name = "added_date")
    private Date addedDate;

    @ColumnInfo(name = "cover_path")
    private String coverPath; // 可选的封面路径
    
    public Book() {
    }
    
    @Ignore
    public Book(String title, String filePath, String fileType, long fileSize) {
        this.title = title;
        this.filePath = filePath;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.addedDate = new Date();
    }
    
    // Getters and Setters
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public String getFileType() {
        return fileType;
    }
    
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
    
    public long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
    
    public Date getAddedDate() {
        return addedDate;
    }
    
    public void setAddedDate(Date addedDate) {
        this.addedDate = addedDate;
    }
    
    public String getCoverPath() {
        return coverPath;
    }
    
    public void setCoverPath(String coverPath) {
        this.coverPath = coverPath;
    }
}