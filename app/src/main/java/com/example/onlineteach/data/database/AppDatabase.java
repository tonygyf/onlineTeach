package com.example.onlineteach.data.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.onlineteach.data.dao.BookDao;
import com.example.onlineteach.data.dao.UserDao;
import com.example.onlineteach.data.model.Book;
import com.example.onlineteach.data.model.User;
import com.example.onlineteach.data.dao.CourseDao;
import com.example.onlineteach.data.model.Course;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {User.class, Course.class, Book.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserDao userDao();
    public abstract CourseDao courseDao();
    public abstract BookDao bookDao();

    private static volatile AppDatabase INSTANCE;

    // ✅ 添加静态 context
    private static Context appContext;

    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    // ✅ 保存传入的 context
                    appContext = context.getApplicationContext();

                    INSTANCE = Room.databaseBuilder(
                                    appContext,
                                    AppDatabase.class,
                                    "app_database"
                            )
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            databaseWriteExecutor.execute(() -> {
                CourseDao courseDao = INSTANCE.courseDao();
                BookDao bookDao = INSTANCE.bookDao();

                List<Course> courseList = new ArrayList<>();

                Course course1 = new Course();
                course1.setTitle("大学教学的语言技能");
                course1.setCredits(1.0f);
                course1.setTeacher("教发老师");
                course1.setImageUrl("@drawable/courselist");

                Course course2 = new Course();
                course2.setTitle("教学准备五件事");
                course2.setCredits(1.0f);
                course2.setTeacher("教发老师");
                course2.setImageUrl("@drawable/courselist");

                Course course3 = new Course();
                course3.setTitle("在线教学设计与实施");
                course3.setCredits(1.5f);
                course3.setTeacher("技术支持中心");
                course3.setImageUrl("@drawable/courselist");

                courseList.add(course1);
                courseList.add(course2);
                courseList.add(course3);

                courseDao.insertAll(courseList);

                try {
                    String fileName = "sample_book.txt";

                    // ✅ 使用 appContext 替代 context
                    InputStream inputStream = appContext.getAssets().open(fileName);
                    File privateDir = new File(appContext.getFilesDir(), "books");
                    if (!privateDir.exists()) {
                        privateDir.mkdirs();
                    }
                    File outputFile = new File(privateDir, fileName);

                    FileOutputStream outputStream = new FileOutputStream(outputFile);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }
                    outputStream.close();
                    inputStream.close();

                    Book sampleBook = new Book();
                    sampleBook.setTitle("示例教学资料");
                    sampleBook.setFilePath(outputFile.getAbsolutePath());
                    sampleBook.setFileType("txt");
                    sampleBook.setFileSize(outputFile.length());
                    sampleBook.setAddedDate(new Date());
                    sampleBook.setCoverPath("@drawable/ic_book_cover");

                    bookDao.insertBook(sampleBook);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    };

    public static void shutdownExecutorService() {
        databaseWriteExecutor.shutdown();
    }
}
