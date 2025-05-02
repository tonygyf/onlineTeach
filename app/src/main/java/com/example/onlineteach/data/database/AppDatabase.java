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
    // 创建一个ExecutorService用于后台数据库操作
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "app_database"
                            )
                            // 添加回调，在数据库创建或打开时执行
                            .addCallback(sRoomDatabaseCallback)
                            // 如果在开发阶段频繁修改 schema，可以使用 fallbackToDestructiveMigration()
                            // 发布应用时应移除或使用migrations
                            // .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // 数据库创建/打开的回调
    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            // 在数据库创建时在新线程中执行数据插入
            databaseWriteExecutor.execute(() -> {
                // Populate the database in a separate thread
                CourseDao courseDao = INSTANCE.courseDao();
                BookDao bookDao = INSTANCE.bookDao();

                // 清空现有数据（可选，如果您希望每次应用启动都重新填充）
                // courseDao.deleteAllCourses();

                // 添加您的测试数据
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

                // 添加示例TXT书籍
                Book sampleBook = new Book();
                sampleBook.setTitle("示例教学资料.txt");
                sampleBook.setFilePath("/storage/emulated/0/Download/示例教学资料.txt");
                sampleBook.setFileType("txt");
                sampleBook.setFileSize(1024L);
                sampleBook.setAddedDate(new Date());
                sampleBook.setCoverPath("@drawable/ic_book_cover");
                
                bookDao.insertBook(sampleBook);
            });
        }
        // 如果需要，还可以覆盖 onOpen() 方法
    };

    // 关闭 ExecutorService 的方法 (可选，但推荐)
    public static void shutdownExecutorService() {
        databaseWriteExecutor.shutdown();
    }
}