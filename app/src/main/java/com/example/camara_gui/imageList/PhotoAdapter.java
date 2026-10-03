package com.example.camara_gui.imageList;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.camara_gui.R;
import com.example.camara_gui.data.Photo;
import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {
    private List<Photo> photos;
    private final OnDeleteClickListener deleteListener;

    public interface OnDeleteClickListener {
        void onDelete(Photo photo);
    }
    public PhotoAdapter(List<Photo> photos, OnDeleteClickListener deleteListener) {
        this.photos = photos;
        this.deleteListener = deleteListener;
    }
    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(
                        R.layout.item_photo,
                        parent,
                        false
                );

        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position ) {

        Photo photo = photos.get(position);
        holder.imagePhoto.setImageURI(Uri.parse(photo.getUri())        );

        holder.buttonDelete.setOnClickListener(v -> deleteListener.onDelete(photo));
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    public void setPhotos(List<Photo> photos) {
        this.photos = photos;
        notifyDataSetChanged();
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {

        ImageView imagePhoto;
        Button buttonDelete;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);

            imagePhoto = itemView.findViewById(R.id.imagePhoto);

            buttonDelete =itemView.findViewById(R.id.buttonDelete);
        }
    }
}