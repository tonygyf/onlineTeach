package com.example.onlineteach.ui.memo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.onlineteach.R;
import com.example.onlineteach.databinding.FragmentMemoListBinding;
import com.example.onlineteach.data.model.Memo;

public class MemoListFragment extends Fragment implements MemoAdapter.OnMemoClickListener {
    private FragmentMemoListBinding binding;
    private MemoListViewModel memoListViewModel;
    private MemoAdapter memoAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMemoListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        memoListViewModel = new ViewModelProvider(this).get(MemoListViewModel.class);

        setupRecyclerView();
        setupFab();
        observeMemos();
    }

    private void setupRecyclerView() {
        memoAdapter = new MemoAdapter(this);
        binding.memoList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.memoList.setAdapter(memoAdapter);
    }

    private void setupFab() {
        binding.fabAddMemo.setOnClickListener(v ->
            Navigation.findNavController(v).navigate(R.id.action_navigation_memo_list_to_memo_edit)
        );
    }

    private void observeMemos() {
        memoListViewModel.getAllMemos().observe(getViewLifecycleOwner(), memos -> {
            memoAdapter.submitList(memos);
            binding.emptyView.setVisibility(memos.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public void onMemoClick(Memo memo) {
        Bundle bundle = new Bundle();
        bundle.putInt("memoId", memo.getId());
        Navigation.findNavController(requireView())
                .navigate(R.id.action_navigation_memo_list_to_memo_edit, bundle);
    }

    @Override
    public void onMemoCheckChanged(Memo memo, boolean isChecked) {
        memo.setCompleted(isChecked);
        memoListViewModel.updateMemo(memo);
        if (isChecked) {
            // 显示完成动画
            showCompletionAnimation();
        }
    }

    private void showCompletionAnimation() {
        // 实现烟花动画效果
        binding.completionAnimationView.setVisibility(View.VISIBLE);
        binding.completionAnimationView.playAnimation();
        binding.completionAnimationView.addAnimatorListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                binding.completionAnimationView.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}