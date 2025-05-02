package com.example.onlineteach.ui.bookshelf;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlineteach.R;
import com.example.onlineteach.data.model.Book;

import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {
    
    private List<Book> books;
    private final OnBookClickListener listener;
    
    public interface OnBookClickListener {
        void onBookClick(Book book);
    }
    
    public BookAdapter(List<Book> books, OnBookClickListener listener) {
        this.books = books;
        this.listener = listener;
    }
    
    public void updateBooks(List<Book> newBooks) {
        this.books = newBooks;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = books.get(position);
        holder.titleTextView.setText(book.getTitle());
        
        // 根据文件类型设置不同的图标
        String fileType = book.getFileType();
        if (fileType != null) {
            if (fileType.contains("pdf")) {
                holder.iconImageView.setImageResource(R.drawable.ic_pdf);
            } else if (fileType.contains("powerpoint") || fileType.contains("presentation")) {
                holder.iconImageView.setImageResource(R.drawable.ic_ppt);
            } else if (fileType.contains("text/plain")) {
                holder.iconImageView.setImageResource(R.drawable.ic_txt);
            } else if (fileType.contains("epub")) {
                holder.iconImageView.setImageResource(R.drawable.ic_epub);
            } else {
                holder.iconImageView.setImageResource(R.drawable.ic_bookshelf);
            }
        } else {
            holder.iconImageView.setImageResource(R.drawable.ic_bookshelf);
        }
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBookClick(book);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return books.size();
    }
    
    static class BookViewHolder extends RecyclerView.ViewHolder {
        ImageView iconImageView;
        TextView titleTextView;
        
        BookViewHolder(View itemView) {
            super(itemView);
            iconImageView = itemView.findViewById(R.id.image_book_cover);
            titleTextView = itemView.findViewById(R.id.text_book_title);
        }
    }
}