package com.example.onlineteach.data.database; // 建议放在 data.database 包下

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.onlineteach.data.dao.UserDao; // 导入 UserDao
import com.example.onlineteach.data.model.User; // 导入 User 实体类

// 使用 @Database 注解标记这是一个 Room 数据库
@Database(entities = {User.class}, version = 1, exportSchema = false) // entities 包含所有实体类，version 是数据库版本
public abstract class AppDatabase extends RoomDatabase {

    // 抽象方法，返回对应的 DAO
    public abstract UserDao userDao();

    // 建议实现一个单例模式来获取数据库实例，避免创建多个数据库连接
    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final android.content.Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = androidx.room.Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "app_database" // 数据库文件名
                            )
                            // .addMigrations(MIGRATION_1_2) // 如果有数据库版本升级，需要添加迁移策略
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}