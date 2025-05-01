package com.example.onlineteach.ui.personalinfo;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.onlineteach.R;
import com.example.onlineteach.data.model.User; // 导入 User
import com.example.onlineteach.data.repository.UserRepository; // 导入 UserRepository
import com.example.onlineteach.ui.home.MenuItem; // 导入 MenuItem

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
// 移除 ExecutorService 和 UserDao 导入，因为它们现在在 UserRepository 中管理

public class PersonalInfoViewModel extends AndroidViewModel {
    private static final String TAG = "PersonalInfoViewModel";
    private static final String PREFS_NAME = "UserPrefs";
    private static final String AVATAR_PATH_KEY = "avatar_path"; // 头像相关的SharedPreferences key

    private UserRepository userRepository; // 引用 UserRepository

    private MutableLiveData<String> mUserName = new MutableLiveData<>();
    private MutableLiveData<String> mStudentId = new MutableLiveData<>();
    private MutableLiveData<Uri> mAvatarUri = new MutableLiveData<>();
    private MutableLiveData<List<MenuItem>> menuItems = new MutableLiveData<>();

    public PersonalInfoViewModel(@NonNull Application application) {
        super(application);
        // 初始化 UserRepository
        userRepository = new UserRepository(application.getApplicationContext());

        // 在 ViewModel 初始化时加载登录用户信息
        loadLoggedInUser();
        // 加载保存的头像
        loadAvatarFromPrefs(application.getApplicationContext());
        // 初始化菜单项
        initMenuItems();
    }

    public LiveData<String> getUserName() {
        return mUserName;
    }

    public LiveData<String> getStudentId() {
        return mStudentId;
    }

    public LiveData<Uri> getAvatarUri() {
        return mAvatarUri;
    }

    public void setAvatarUri(Uri uri) {
        mAvatarUri.setValue(uri);
    }
    
    public LiveData<List<MenuItem>> getMenuItems() {
        return menuItems;
    }
    
    /**
     * 初始化个人信息页面的菜单项
     */
    private void initMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        items.add(new MenuItem(R.drawable.ic_edit, "修改个人信息"));
        items.add(new MenuItem(R.drawable.ic_history, "浏览记录"));
        items.add(new MenuItem(R.drawable.ic_logout, "退出账户"));
        menuItems.setValue(items);
    }
    
    /**
     * 处理退出账户操作
     */
    public void logout() {
        userRepository.logoutUser();
        // 更新UI状态
        mUserName.postValue("请登录");
        mStudentId.postValue("");
        // 清除头像
        mAvatarUri.setValue(null);
    }

    /**
     * 从 UserRepository 加载当前登录用户的信息
     */
    private void loadLoggedInUser() {
        userRepository.getLoggedInUser(new UserRepository.UserCallback() {
            @Override
            public void onUserLoaded(User user) {
                // 用户加载成功，更新 LiveData
                mUserName.postValue(user.getUserName());
                mStudentId.postValue(user.getStudentId());
                // TODO: 如果 User 实体类中有头像字段，在这里加载头像URI
            }

            @Override
            public void onUserNotFound() {
                // 没有找到登录用户或用户不存在，显示默认或未登录状态
                Log.i(TAG, "No logged in user found or user not in database.");
                mUserName.postValue("请登录");
                mStudentId.postValue("");
            }
        });
    }

    // getLoggedInUserId 方法可以从 PersonalInfoViewModel 中移除，因为 UserRepository 管理这个逻辑

    /**
     * 保存头像到应用内部存储
     * @param context 上下文
     * @param sourceUri 源图片URI
     * @return 保存后的图片URI
     */
    public Uri saveAvatarToInternalStorage(Context context, Uri sourceUri) {
        // ... 代码与之前相似，但可以考虑在 UserRepository 中管理头像保存，
        // 并利用 userRepository.getLoggedInUserId() 获取用户ID来命名文件
        if (sourceUri == null) {
            return null;
        }

        try {
            File avatarDir = new File(context.getFilesDir(), "avatars");
            if (!avatarDir.exists()) {
                avatarDir.mkdirs();
            }

            // 通过 UserRepository 获取当前登录用户ID来命名文件更安全
            int loggedInUserId = userRepository.getLoggedInUserId();
            String fileName = "avatar_" + (loggedInUserId != -1 ? loggedInUserId : "guest") + "_" + System.currentTimeMillis() + ".jpg";
            File avatarFile = new File(avatarDir, fileName);

            try (InputStream inputStream = context.getContentResolver().openInputStream(sourceUri);
                 FileOutputStream outputStream = new FileOutputStream(avatarFile)) {

                if (inputStream == null) {
                    return null;
                }

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();

                // 保存文件路径到SharedPreferences (可以考虑在 UserRepository 中管理)
                saveAvatarPathToPrefs(context, avatarFile.getAbsolutePath()); // TODO: 可以考虑按用户ID保存路径

                return Uri.fromFile(avatarFile);
            }
        } catch (IOException e) {
            Log.e(TAG, "保存头像失败", e);
            return null;
        }
    }

    /**
     * 保存头像路径到SharedPreferences
     */
    private void saveAvatarPathToPrefs(Context context, String path) {
        // 可以考虑将这个方法移到 UserRepository 中
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        // TODO: 考虑按登录用户ID保存不同的头像路径
        prefs.edit().putString(AVATAR_PATH_KEY, path).apply();
    }

    /**
     * 从SharedPreferences加载头像路径
     */
    public Uri loadAvatarFromPrefs(Context context) {
        // 可以考虑将这个方法移到 UserRepository 中
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        // TODO: 考虑按登录用户ID读取不同的头像路径
        String avatarPath = prefs.getString(AVATAR_PATH_KEY, null);

        if (avatarPath != null) {
            File avatarFile = new File(avatarPath);
            if (avatarFile.exists()) {
                Uri uri = Uri.fromFile(avatarFile);
                mAvatarUri.setValue(uri);
                return uri;
            }
        }
        mAvatarUri.setValue(null);
        return null;
    }


    @Override
    protected void onCleared() {
        super.onCleared();
        // 如果 UserRepository 需要清理资源，可以在这里调用，例如 userRepository.shutdownExecutor();
    }
}