package com.example.onlineteach.utils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {

    /**
     * 获取文件的真实路径
     *
     * @param context 上下文
     * @param uri     文件URI
     * @return 文件的真实路径，如果无法获取则返回null
     */
    public static String getPathFromUri(Context context, Uri uri) {
        String path = null;
        if ("file".equalsIgnoreCase(uri.getScheme())) {
            path = uri.getPath();
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            try {
                final String[] projection = {"_data"};
                try (Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null)) {
                    if (cursor != null && cursor.moveToFirst()) {
                        final int columnIndex = cursor.getColumnIndexOrThrow("_data");
                        path = cursor.getString(columnIndex);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return path;
    }

    /**
     * 获取文件的MIME类型
     *
     * @param context 上下文
     * @param uri     文件URI
     * @return 文件的MIME类型
     */
    public static String getMimeType(Context context, Uri uri) {
        return context.getContentResolver().getType(uri);
    }

    /**
     * 获取文件大小
     *
     * @param context 上下文
     * @param uri     文件URI
     * @return 文件大小（字节）
     */
    public static long getFileSize(Context context, Uri uri) {
        try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) {
                    return cursor.getLong(sizeIndex);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 为文档URI获取持久化访问权限
     *
     * @param context 上下文
     * @param uri     文档URI
     */
    public static void takePersistableUriPermission(Context context, Uri uri) {
        final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
        context.getContentResolver().takePersistableUriPermission(uri, takeFlags);
    }

    /**
     * 将选择的文件复制到应用私有存储空间
     *
     * @param context  上下文
     * @param sourceUri 源文件URI
     * @param fileName 文件名
     * @return 复制后的本地文件
     * @throws IOException 如果复制过程中发生错误
     */
    public static File copyFileToLocalStorage(Context context, Uri sourceUri, String fileName) throws IOException {
        // 创建应用私有目录
        File booksDir = new File(context.getFilesDir(), "books");
        if (!booksDir.exists()) {
            booksDir.mkdirs();
        }

        // 创建目标文件
        File destFile = new File(booksDir, fileName);

        // 复制文件内容
        try (InputStream in = context.getContentResolver().openInputStream(sourceUri);
             OutputStream out = new FileOutputStream(destFile)) {

            if (in == null) {
                throw new IOException("无法打开输入流");
            }

            byte[] buffer = new byte[4096];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            out.flush();
            return destFile;
        }
    }

    /**
     * 根据文件类型打开相应的阅读器
     *
     * @param context  上下文
     * @param filePath 文件路径
     * @param mimeType 文件MIME类型
     */
    public static void openFile(Context context, String filePath, String mimeType) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                Toast.makeText(context, "文件不存在", Toast.LENGTH_SHORT).show();
                return;
            }

            // 使用FileProvider获取文件URI
            Uri fileUri = FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".fileprovider",
                    file);

            // 创建打开文件的Intent
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(fileUri, mimeType);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // 检查是否有应用可以处理此Intent
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "没有找到可以打开此类型文件的应用", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "打开文件失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}