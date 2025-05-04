package com.example.onlineteach.ui.group;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlineteach.R;
import com.example.onlineteach.data.model.Group;
import com.example.onlineteach.utils.ToastUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class GroupListFragment extends Fragment implements GroupListAdapter.OnGroupClickListener {

    private GroupListViewModel mViewModel;
    private RecyclerView recyclerView;
    private FloatingActionButton fabCreateGroup;

    public static GroupListFragment newInstance() {
        return new GroupListFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_group_list, container, false);
        recyclerView = root.findViewById(R.id.recycler_view_groups);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        fabCreateGroup = root.findViewById(R.id.fab_create_group);
        fabCreateGroup.setOnClickListener(v -> {
            // TODO: 实现创建群组的对话框
            ToastUtils.showShortToast(getContext(), "创建新群组");
        });
        
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 使用AndroidViewModel的Factory来创建ViewModel
        mViewModel = new ViewModelProvider(this, 
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
            .get(GroupListViewModel.class);

        // 初始化适配器
        GroupListAdapter adapter = new GroupListAdapter(this);
        recyclerView.setAdapter(adapter);

        // 观察群组列表数据变化
        mViewModel.getGroups().observe(getViewLifecycleOwner(), groups -> {
            Log.d("GroupListFragment", "群组数据更新，数量: " + (groups != null ? groups.size() : 0));
            if (groups != null && !groups.isEmpty()) {
                adapter.setGroups(groups);
                // 确保适配器更新后刷新UI
                adapter.notifyDataSetChanged();
            } else {
                Log.d("GroupListFragment", "群组列表为空");
                // 显示空列表
                adapter.setGroups(new ArrayList<>());
            }
        });
    }

    @Override
    public void onGroupClick(Group group) {
        // TODO: 导航到群组聊天页面
        ToastUtils.showShortToast(getContext(), "进入群组: " + group.getName());
    }
}