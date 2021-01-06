package com.example.voice_recorder.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voice_recorder.DateConversion;
import com.example.voice_recorder.R;

import java.io.File;

public class AudioList_Adapter extends RecyclerView.Adapter<AudioList_Adapter.AudioViewHolder> {

    private File[] allFiles;
    private DateConversion dateConversion;
    private onItemListClick onItemListClick;

    public AudioList_Adapter(File[] allFiles, onItemListClick onItemListClick) {
        this.allFiles = allFiles;
        this.onItemListClick = onItemListClick;
    }

    @NonNull
    @Override
    public AudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_list_item, parent, false);
        dateConversion = new DateConversion();
        return new AudioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AudioViewHolder holder, int position) {
        holder.list_title.setText(allFiles[position].getName());
        holder.list_date.setText(dateConversion.getDateConversion(allFiles[position].lastModified()));
    }

    @Override
    public int getItemCount() {
        return allFiles.length;
    }

    public class AudioViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView list_image, btn_delete;
        private TextView list_title;
        private TextView list_date;

        public AudioViewHolder(@NonNull View itemView) {
            super(itemView);

            list_image = itemView.findViewById(R.id.list_image_view);
            list_title = itemView.findViewById(R.id.list_title);
            list_date = itemView.findViewById(R.id.list_date);
            btn_delete = itemView.findViewById(R.id.btn_delete);

            list_image.setOnClickListener(this);
            btn_delete.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.list_image_view:
                    onItemListClick.onClickListener(allFiles[getAdapterPosition()], getAdapterPosition());
                    break;
                case R.id.btn_delete:
                    onItemListClick.onDeleteClickListener(allFiles[getAdapterPosition()], getAdapterPosition());
                    break;
            }

        }
    }

    public interface onItemListClick {
        void onClickListener(File file, int position);
        void onDeleteClickListener(File file, int position);
    }
}
