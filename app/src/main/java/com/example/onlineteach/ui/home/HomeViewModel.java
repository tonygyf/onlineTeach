package com.example.onlineteach.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.onlineteach.R;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel {
    private final MutableLiveData<List<MenuItem>> menuItems;

    public HomeViewModel() {
        menuItems = new MutableLiveData<>();
        List<MenuItem> items = new ArrayList<>();
        items.add(new MenuItem(R.drawable.ic_course, "课程"));
        items.add(new MenuItem(R.drawable.ic_notebook, "待办"));
        items.add(new MenuItem(R.drawable.ic_cloud, "云盘"));
        items.add(new MenuItem(R.drawable.ic_group, "分组"));
        items.add(new MenuItem(R.drawable.ic_bookshelf, "书架"));
        items.add(new MenuItem(R.drawable.ic_settings, "设置"));
        menuItems.setValue(items);
    }

    public LiveData<List<MenuItem>> getMenuItems() {
        return menuItems;
    }
}