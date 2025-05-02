package com.example.onlineteach.ui.bookshelf;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.onlineteach.databinding.FragmentBookshelfBinding;
import com.example.onlineteach.data.model.Book;
import com.example.onlineteach.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class BookshelfFragment extends Fragment {

    private FragmentBookshelfBinding binding;
    private BookshelfViewModel viewModel;
    private BookAdapter adapter;

    // ✅ 修改类型为 <String>，而不是 <String[]>
    private ActivityResultLauncher<String> filePickerLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ 使用 GetContent（更兼容）
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                this::handleSelectedFile
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBookshelfBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(BookshelfViewModel.class);

        // 设置RecyclerView
        binding.recyclerViewBooks.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        adapter = new BookAdapter(new ArrayList<>(), this::onBookClick);
        binding.recyclerViewBooks.setAdapter(adapter);

        // 观察书籍数据变化
        viewModel.getBooks().observe(getViewLifecycleOwner(), books -> {
            adapter.updateBooks(books);
            binding.emptyView.setVisibility(books.isEmpty() ? View.VISIBLE : View.GONE);
        });

        // 添加书籍按钮点击事件
        binding.fabAddBook.setOnClickListener(v -> openFilePicker());
    }

    private void openFilePicker() {
        // ✅ GetContent 只支持单个 MIME 类型，这里使用 */* 表示所有类型
        filePickerLauncher.launch("*/*");
    }

    private void handleSelectedFile(Uri uri) {
        if (uri == null) return;

        try {
            // 获取文件信息
            String fileName = getFileNameFromUri(uri);
            String fileType = requireContext().getContentResolver().getType(uri);
            long fileSize = getFileSizeFromUri(uri);

            // 复制文件到本地存储
            File localFile = FileUtils.copyFileToLocalStorage(requireContext(), uri, fileName);

            // 创建书籍对象并保存到数据库
            Book book = new Book(fileName, localFile.getAbsolutePath(), fileType, fileSize);
            viewModel.addBook(book);

            Toast.makeText(requireContext(), "已添加书籍: " + fileName, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "添加书籍失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private long getFileSizeFromUri(Uri uri) {
        long size = 0;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                    if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) {
                        size = cursor.getLong(sizeIndex);
                    }
                }
            }
        }
        return size;
    }

    private void onBookClick(Book book) {
        FileUtils.openFile(requireContext(), book.getFilePath(), book.getFileType());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
