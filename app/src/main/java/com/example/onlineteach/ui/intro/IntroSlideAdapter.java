package com.example.onlineteach.ui.intro;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.onlineteach.R;

import java.util.List;

public class IntroSlideAdapter extends RecyclerView.Adapter<IntroSlideAdapter.IntroSlideViewHolder> {

    private final List<IntroSlide> introSlides;

    public IntroSlideAdapter(List<IntroSlide> introSlides) {
        this.introSlides = introSlides;
    }

    @NonNull
    @Override
    public IntroSlideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new IntroSlideViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_intro_slide, parent, false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull IntroSlideViewHolder holder, int position) {
        holder.bind(introSlides.get(position));
    }

    @Override
    public int getItemCount() {
        return introSlides.size();
    }

    static class IntroSlideViewHolder extends RecyclerView.ViewHolder {

        private final LottieAnimationView lottieAnimationView;
        private final TextView titleTextView;
        private final TextView descriptionTextView;

        public IntroSlideViewHolder(@NonNull View itemView) {
            super(itemView);
            lottieAnimationView = itemView.findViewById(R.id.lottieAnimationView);
            titleTextView = itemView.findViewById(R.id.intro_title);
            descriptionTextView = itemView.findViewById(R.id.intro_description);
        }

        void bind(IntroSlide introSlide) {
            lottieAnimationView.setAnimation(introSlide.getLottieFileName());
            lottieAnimationView.playAnimation(); // 可选，触发播放
            titleTextView.setText(introSlide.getTitle());
            descriptionTextView.setText(introSlide.getDescription());
        }
    }
}