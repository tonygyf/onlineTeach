package com.example.onlineteach.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
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
                    .navigate(R.id.action_homeFragment_to_courseListFragment);
        }
        // 其他菜单项的点击处理可以在这里添加
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}