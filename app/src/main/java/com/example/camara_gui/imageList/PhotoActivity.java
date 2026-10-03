package com.example.camara_gui.imageList;

import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.camara_gui.R;
import com.example.camara_gui.data.AppDatabase;
import com.example.camara_gui.data.Photo;
import com.example.camara_gui.data.PhotoDao;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PhotoActivity extends AppCompatActivity {
    private PhotoDao photoDao;
    private PhotoAdapter adapter;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_photo_list);

        RecyclerView recyclerView = findViewById(R.id.recyclerPhotos);

        recyclerView.setLayoutManager( new LinearLayoutManager(this)
        );

        AppDatabase database = AppDatabase.getDatabase(this);

        photoDao = database.photoDao();

        adapter = new PhotoAdapter( new ArrayList<>(), this::deletePhoto);

        recyclerView.setAdapter(adapter);

        loadPhotos();
    }
    private void loadPhotos() {

        executor.execute(() -> {

            List<Photo> photos = photoDao.getAll();

            runOnUiThread(() -> adapter.setPhotos(photos)
            );
        });
    }
    private void deletePhoto(Photo photo) {

        executor.execute(() -> {

            Uri uri = Uri.parse(photo.getUri());

            getContentResolver().delete(
                    uri,
                    null,
                    null
            );

            photoDao.delete(photo);

            loadPhotos();
        });
    }
}