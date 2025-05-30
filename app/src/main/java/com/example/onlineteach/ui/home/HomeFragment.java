package com.example.onlineteach.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.onlineteach.R;
import com.example.onlineteach.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment implements MenuAdapter.OnItemClickListener {

    private FragmentHomeBinding binding;
    private HomeViewModel homeViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                            ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.recyclerViewMenu.setLayoutManager(new LinearLayoutManager(requireContext()));
        homeViewModel.getMenuItems().observe(getViewLifecycleOwner(), menuItems -> {
            MenuAdapter adapter = new MenuAdapter(menuItems, this);
            binding.recyclerViewMenu.setAdapter(adapter);
        });

        return root;
    }

    @Override
    public void onItemClick(int position) {
        if (position == 0) { // 课程菜单项
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_navigation_home_to_navigation_course_list);
        } else if (position == 1) { // 待办菜单项
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_navigation_home_to_navigation_memo_list);
        } else if (position == 3) { // 分组菜单项
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_navigation_home_to_navigation_group_list);
        } else if (position == 4) { // 书架菜单项
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_navigation_home_to_navigation_bookshelf);
        } else if (position == 5) { // 设置菜单项
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_navigation_home_to_navigation_settings);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        // 确保在恢复到此Fragment时，ActionBar显示正确的标题
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (activity != null) {
                androidx.appcompat.app.ActionBar actionBar = activity.getSupportActionBar();
                if (actionBar != null) {
                    actionBar.setTitle(R.string.title_home);
                    actionBar.show();
                }
            }
        }
    }
}