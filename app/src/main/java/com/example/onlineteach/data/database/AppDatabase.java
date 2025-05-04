package com.example.onlineteach.data.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.onlineteach.data.dao.BookDao;
import com.example.onlineteach.data.dao.UserDao;
import com.example.onlineteach.data.dao.GroupDao;
import com.example.onlineteach.data.dao.CourseDao;

import com.example.onlineteach.data.model.Book;
import com.example.onlineteach.data.model.User;
import com.example.onlineteach.data.model.Group;
import com.example.onlineteach.data.model.GroupMember;
import com.example.onlineteach.data.model.GroupMessage;
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

@Database(entities = {User.class, Course.class, Book.class, Group.class, GroupMember.class, GroupMessage.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserDao userDao();
    public abstract CourseDao courseDao();
    public abstract BookDao bookDao();
    public abstract GroupDao groupDao();

    private static volatile AppDatabase INSTANCE;
    private static Context appContext;

    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
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
                UserDao userDao = INSTANCE.userDao();
                CourseDao courseDao = INSTANCE.courseDao();
                BookDao bookDao = INSTANCE.bookDao();
                GroupDao groupDao = INSTANCE.groupDao();

                // ✅ 初始化默认用户
//                User defaultUser = new User();
//                defaultUser.setUsername("admin");
//                defaultUser.setEmail("admin@example.com");
//                defaultUser.setPassword("password"); // 明文仅限测试
//                userDao.insertUser(defaultUser); // 该用户将获得 ID = 1

                // ✅ 初始化课程
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

                // ✅ 初始化图书
                try {
                    String fileName = "sample_book.txt";
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

                // ✅ 初始化群组
                Group group = new Group();
                group.setName("教学交流群");
                group.setDescription("用于教学经验交流");
                group.setCreatorId(1); // 设置创建者ID为默认用户
                group.setCreateTime(System.currentTimeMillis()); // 设置创建时间
                group.setMemberCount(1); // 设置成员数量
                long groupId = groupDao.insertGroup(group);

                // ✅ 初始化群成员
                GroupMember member = new GroupMember();
                member.setGroupId((int)groupId);
                member.setUserId(1); // 假设用户ID 1
                member.setJoinTime(System.currentTimeMillis()); // 设置加入时间
                member.setAdmin(true); // 设置为管理员
                groupDao.insertGroupMember(member);

                // ✅ 初始化群消息
                GroupMessage message = new GroupMessage();
                message.setGroupId((int)groupId);
                message.setSenderId(1); // 同样假设用户ID 1
                message.setContent("欢迎加入教学交流群！");
                message.setSendTime(System.currentTimeMillis()); // 设置发送时间
                message.setMessageType(0); // 设置消息类型为文本
                groupDao.insertGroupMessage(message);
            });
        }
    };

    public static void shutdownExecutorService() {
        databaseWriteExecutor.shutdown();
    }
}
