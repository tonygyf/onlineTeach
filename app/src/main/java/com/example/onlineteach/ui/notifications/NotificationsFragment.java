package com.example.onlineteach.ui.notifications;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlineteach.R;
import com.example.onlineteach.data.model.Group;
import com.example.onlineteach.ui.group.GroupListAdapter;
import com.example.onlineteach.utils.ToastUtils;

import java.util.ArrayList;

public class NotificationsFragment extends Fragment implements GroupListAdapter.OnGroupClickListener {

    private NotificationsViewModel mViewModel;
    private RecyclerView recyclerView;
    private TextView emptyTextView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_groups, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_my_groups);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        emptyTextView = view.findViewById(R.id.text_empty_groups);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        mViewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(NotificationsViewModel.class);

        // 初始化适配器
        GroupListAdapter adapter = new GroupListAdapter(this);
        recyclerView.setAdapter(adapter);

        // 观察我的群组列表数据变化
        mViewModel.getMyGroups().observe(getViewLifecycleOwner(), groups -> {
            Log.d("NotificationsFragment", "我的群组数据更新，数量: " + (groups != null ? groups.size() : 0));
            if (groups != null && !groups.isEmpty()) {
                adapter.setGroups(groups);
                recyclerView.setVisibility(View.VISIBLE);
                emptyTextView.setVisibility(View.GONE);
            } else {
                Log.d("NotificationsFragment", "我的群组列表为空");
                adapter.setGroups(new ArrayList<>());
                recyclerView.setVisibility(View.GONE);
                emptyTextView.setVisibility(View.VISIBLE);
            }
        });

        // 观察 ViewModel 中的 toastMessage
        mViewModel.getToastMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                ToastUtils.showShortToast(getContext(), message);
                mViewModel.clearToastMessage();
            }
        });
    }

    @Override
    public void onGroupClick(Group group) {
        // 导航到群组聊天页面
        Bundle args = new Bundle();
        args.putInt("group_id", group.getGroupId());

        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
        navController.navigate(R.id.action_navigation_notifications_to_navigation_group_chat, args);

        Log.d("NotificationsFragment", "导航到群组聊天页面: " + group.getName() + ", ID: " + group.getGroupId());
    }
}