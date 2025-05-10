package com.example.onlineteach.ui.memo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlineteach.R;
import com.example.onlineteach.data.model.Memo;
import com.example.onlineteach.databinding.ItemMemoBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MemoAdapter extends ListAdapter<Memo, MemoAdapter.MemoViewHolder> {
    private final OnMemoClickListener listener;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public interface OnMemoClickListener {
        void onMemoClick(Memo memo);
        void onMemoCheckChanged(Memo memo, boolean isChecked);
    }

    protected static final DiffUtil.ItemCallback<Memo> DIFF_CALLBACK = new DiffUtil.ItemCallback<Memo>() {
        @Override
        public boolean areItemsTheSame(@NonNull Memo oldItem, @NonNull Memo newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Memo oldItem, @NonNull Memo newItem) {
            return oldItem.getTitle().equals(newItem.getTitle()) &&
                   oldItem.getContent().equals(newItem.getContent()) &&
                   oldItem.getReminderTime() == newItem.getReminderTime() &&
                   oldItem.isCompleted() == newItem.isCompleted();
        }
    };

    public MemoAdapter(OnMemoClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public MemoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMemoBinding binding = ItemMemoBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new MemoViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MemoViewHolder holder, int position) {
        Memo memo = getItem(position);
        holder.bind(memo, listener);
    }

    static class MemoViewHolder extends RecyclerView.ViewHolder {
        private final ItemMemoBinding binding;

        public MemoViewHolder(ItemMemoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(final Memo memo, final OnMemoClickListener listener) {
            binding.memoTitle.setText(memo.getTitle());
            binding.memoContent.setText(memo.getContent());
            binding.memoDate.setText(dateFormat.format(new Date(memo.getReminderTime())));
            binding.reminderIcon.setVisibility(memo.getReminderTime() > 0 ? View.VISIBLE : View.GONE);
            binding.memoCheckbox.setChecked(memo.isCompleted());

            itemView.setOnClickListener(v -> listener.onMemoClick(memo));
            binding.getRoot().setOnClickListener(v -> listener.onMemoClick(memo));
            binding.memoCheckbox.setOnClickListener(v -> {
                boolean isChecked = binding.memoCheckbox.isChecked();
                listener.onMemoCheckChanged(memo, isChecked);
            });
            binding.reminderIcon.setOnClickListener(v -> {
                boolean newState = !memo.isCompleted();
                listener.onMemoCheckChanged(memo, newState);
            });}
        }
}
