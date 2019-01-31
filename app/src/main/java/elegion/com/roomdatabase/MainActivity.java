package elegion.com.roomdatabase;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import elegion.com.roomdatabase.database.Album;
import elegion.com.roomdatabase.database.AlbumSong;
import elegion.com.roomdatabase.database.MusicDao;
import elegion.com.roomdatabase.database.Song;

public class MainActivity extends AppCompatActivity {
    private Button mAddBtn;
    private Button mGetBtn;

    // добавить базу данных Room ----
    // вставить данные / извлечь данные ---
    // добавить контент провайдер над Room ---

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final MusicDao musicDao = ((AppDelegate) getApplicationContext()).getMusicDatabase().getMusicDao();

        mAddBtn = (findViewById(R.id.add));
        mAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicDao.insertAlbums(createAlbums());
                musicDao.insertSongs(createSongs());
                musicDao.setLinksAlbumSongs(createAlbumSongs());
            }
        });

        mGetBtn = findViewById(R.id.get);
        mGetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast(musicDao);
            }
        });

    }

    private List<Album> createAlbums() {
        int count = 3;
        List<Album> albums = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            albums.add(new Album(i, "album " + i, "release " + System.currentTimeMillis()));
        }

        return albums;
    }

    private List<Song> createSongs() {
        int count = 3;
        List<Song> songs = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            songs.add(new Song(i, "song " + i, "duration " + System.currentTimeMillis()));
        }

        return songs;
    }

    private List<AlbumSong> createAlbumSongs() {
        int count = 3;
        List<AlbumSong> albumSongs = new ArrayList<>(count);

        albumSongs.add(new AlbumSong(0, 0, 0));
        albumSongs.add(new AlbumSong(1, 0, 1));
        albumSongs.add(new AlbumSong(2, 1, 2));

        return albumSongs;
    }

    private void showToast(MusicDao musicDao) {
        List<Album> albums = musicDao.getAlbums();
        List<Song> songs = musicDao.getSongs();
        List<AlbumSong> albumSongs = musicDao.getAlbumSongs();

        StringBuilder builder = new StringBuilder();

        for (int i = 0, size = albums.size(); i < size; i++) {
            builder.append(albums.get(i).toString()).append("\n");
        }

        for (int i = 0, size = songs.size(); i < size; i++) {
            builder.append(songs.get(i).toString()).append("\n");
        }

        for (int i = 0, size = albumSongs.size(); i < size; i++) {
            builder.append(albumSongs.get(i).toString()).append("\n");
        }

        Toast.makeText(this, builder.toString(), Toast.LENGTH_LONG).show();

    }
}
