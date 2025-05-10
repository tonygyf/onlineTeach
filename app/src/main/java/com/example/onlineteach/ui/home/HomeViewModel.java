package com.example.onlineteach.ui.home;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;


import com.example.onlineteach.R;
import com.example.onlineteach.data.repository.MemoRepository;

import java.util.ArrayList;
import java.util.List;


import androidx.lifecycle.AndroidViewModel;


public class HomeViewModel extends AndroidViewModel {
    private final MutableLiveData<List<MenuItem>> menuItems;
    private final MemoRepository memoRepository;
    private final LiveData<Integer> uncompletedMemoCount;

    public HomeViewModel(Application application) {
        super(application);
        menuItems = new MutableLiveData<>();
        List<MenuItem> items = new ArrayList<>();
        items.add(new MenuItem(R.drawable.ic_course, "课程"));
        items.add(new MenuItem(R.drawable.ic_notebook, "待办"));
        items.add(new MenuItem(R.drawable.ic_cloud, "云盘"));
        items.add(new MenuItem(R.drawable.ic_group, "分组"));
        items.add(new MenuItem(R.drawable.ic_bookshelf, "书架"));
        items.add(new MenuItem(R.drawable.ic_settings, "设置"));
        menuItems.setValue(items);

        // 初始化备忘录仓库并监听未完成待办数量
        memoRepository = new MemoRepository(application);
        uncompletedMemoCount = memoRepository.getUncompletedMemoCount();

        // 监听未完成待办数量变化，更新待办项的标记
        uncompletedMemoCount.observeForever(count -> {
            List<MenuItem> currentItems = menuItems.getValue();
            if (currentItems != null && currentItems.size() > 1) {
                MenuItem todoItem = currentItems.get(1);
                todoItem.setShowBadge(count > 0);
                menuItems.setValue(currentItems);
            }
        });
    }

    public LiveData<List<MenuItem>> getMenuItems() {
        return menuItems;
    }
}
