package com.example.onlineteach.ui.memo;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.onlineteach.R;
import com.example.onlineteach.databinding.FragmentMemoEditBinding;
import com.example.onlineteach.data.model.Memo;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MemoEditFragment extends Fragment {
    private FragmentMemoEditBinding binding;
    private MemoListViewModel memoListViewModel;
    private Calendar reminderCalendar;
    private int memoId = -1;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMemoEditBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        memoListViewModel = new ViewModelProvider(this).get(MemoListViewModel.class);
        reminderCalendar = Calendar.getInstance();

        // 获取传递的备忘录ID
        if (getArguments() != null) {
            memoId = getArguments().getInt("memoId", -1);
            if (memoId != -1) {
                loadMemo(memoId);
            }
        }

        setupDateTimePicker();
        setupSaveButton();
    }

    private void loadMemo(int memoId) {
        memoListViewModel.getMemoById(memoId).observe(getViewLifecycleOwner(), memo -> {
            if (memo != null) {
                binding.editMemoTitle.setText(memo.getTitle());
                binding.editMemoContent.setText(memo.getContent());
                binding.switchReminder.setChecked(memo.getReminderTime() > 0);
                if (memo.getReminderTime() > 0) {
                    reminderCalendar.setTimeInMillis(memo.getReminderTime());
                    updateDateTimeDisplay();
                }
            }
        });
    }

    private void setupDateTimePicker() {
        binding.switchReminder.setOnClickListener(v -> showDateTimePicker());
        updateDateTimeDisplay();
    }

    private void showDateTimePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    reminderCalendar.set(Calendar.YEAR, year);
                    reminderCalendar.set(Calendar.MONTH, month);
                    reminderCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    showTimePicker();
                },
                reminderCalendar.get(Calendar.YEAR),
                reminderCalendar.get(Calendar.MONTH),
                reminderCalendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    reminderCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    reminderCalendar.set(Calendar.MINUTE, minute);
                    updateDateTimeDisplay();
                },
                reminderCalendar.get(Calendar.HOUR_OF_DAY),
                reminderCalendar.get(Calendar.MINUTE),
                true
        );
        timePickerDialog.show();
    }

    private void updateDateTimeDisplay() {
        binding.textReminderTime.setText(dateFormat.format(reminderCalendar.getTime()));
    }

    private void setupSaveButton() {
        binding.buttonSave.setOnClickListener(v -> saveMemo());
    }

    private void saveMemo() {
        String title = binding.editMemoTitle.getText().toString().trim();
        String content = binding.editMemoContent.getText().toString().trim();

        if (title.isEmpty()) {
            binding.editMemoTitle.setError("请输入标题");
            return;
        }

        long reminderTime = binding.switchReminder.isChecked() ? reminderCalendar.getTimeInMillis() : 0;
        Memo memo = new Memo(title, content, reminderTime);
        if (memoId != -1) {
            memo.setId(memoId);
            memoListViewModel.updateMemo(memo);
        } else {
            memoListViewModel.addMemo(memo);
        }

        Navigation.findNavController(requireView()).navigateUp();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}