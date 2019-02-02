package elegion.com.roomdatabase;

import android.arch.persistence.room.Room;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;

import elegion.com.roomdatabase.database.Album;
import elegion.com.roomdatabase.database.MusicDao;
import elegion.com.roomdatabase.database.MusicDatabase;
import elegion.com.roomdatabase.database.Song;

public class MusicProvider extends ContentProvider {

    private static final String TAG = MusicProvider.class.getSimpleName();

    private static final String AUTHORITY = "com.elegion.roomdatabase.musicprovider";
    private static final String TABLE_ALBUM = "album";
    private static final String TABLE_SONG = "song";

    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    private static final int ALBUM_TABLE_CODE = 100;
    private static final int ALBUM_ROW_CODE = 101;
    private static final int SONG_TABLE_CODE = 102;
    private static final int SONG_ROW_CODE = 103;

    static {
        URI_MATCHER.addURI(AUTHORITY, TABLE_ALBUM, ALBUM_TABLE_CODE);
        URI_MATCHER.addURI(AUTHORITY, TABLE_ALBUM + "/*", ALBUM_ROW_CODE);
        URI_MATCHER.addURI(AUTHORITY, TABLE_SONG, SONG_TABLE_CODE);
        URI_MATCHER.addURI(AUTHORITY, TABLE_SONG + "/*", SONG_ROW_CODE);
    }

    private MusicDao mMusicDao;

    public MusicProvider() {
    }

    @Override
    public boolean onCreate() {
        if (getContext() != null) {
            mMusicDao = Room.databaseBuilder(getContext().getApplicationContext(), MusicDatabase.class, "music_database")
                    .build()
                    .getMusicDao();
            return true;
        }

        return false;
    }

    @Override
    public String getType(Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case ALBUM_TABLE_CODE:
                return "vnd.android.cursor.dir/" + AUTHORITY + "." + TABLE_ALBUM;
            case ALBUM_ROW_CODE:
                return "vnd.android.cursor.item/" + AUTHORITY + "." + TABLE_ALBUM;
            case SONG_TABLE_CODE:
                return "vnd.android.cursor.dir/" + AUTHORITY + "." + TABLE_SONG;
            case SONG_ROW_CODE:
                return "vnd.android.cursor.item/" + AUTHORITY + "." + TABLE_SONG;
            default:
                throw new UnsupportedOperationException("not yet implemented");
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        int code = URI_MATCHER.match(uri);

        if (code != ALBUM_ROW_CODE && code != ALBUM_TABLE_CODE) return null;

        int id = (int) ContentUris.parseId(uri);

        switch (code) {
            case ALBUM_TABLE_CODE:
                return mMusicDao.getAlbumsCursor();
            case ALBUM_ROW_CODE:
                return mMusicDao.getAlbumWithIdCursor(id);
            case SONG_TABLE_CODE:
                return mMusicDao.getSongsCursor();
            case SONG_ROW_CODE:
                return mMusicDao.getSongWithIdCursor(id);
        }

        return null;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        switch (URI_MATCHER.match(uri)) {
            case ALBUM_TABLE_CODE:
                return insertAlbum(uri, values);
            case SONG_TABLE_CODE:
                return insertSong(uri, values);
            default:
                throwIllegalArgumentException();
        }

        return null;
    }

    protected Uri insertAlbum(@NonNull Uri uri, ContentValues values) {
        if (!isAlbumValuesValid(values)) {
            throwIllegalArgumentException();
            return null;
        }

        Album album = new Album();
        Integer id = values.getAsInteger("id");
        album.setId(id);
        album.setName(values.getAsString("name"));
        album.setReleaseDate(values.getAsString("release"));
        mMusicDao.insertAlbum(album);

        return ContentUris.withAppendedId(uri, id);
    }

    protected Uri insertSong(@NonNull Uri uri, ContentValues values) {
        if (!isSongValuesValid(values)) {
            throwIllegalArgumentException();
            return null;
        }

        Song song = new Song();
        Integer id = values.getAsInteger("id");
        song.setId(id);
        song.setName(values.getAsString("name"));
        song.setDuration(values.getAsString("duration"));
        mMusicDao.insertSong(song);

        return ContentUris.withAppendedId(uri, id);
    }

    protected void throwIllegalArgumentException() {
        throw new IllegalArgumentException("cant add multiple items");
    }

    protected boolean isAlbumValuesValid(ContentValues values) {
        return values.containsKey("id") && values.containsKey("name") && values.containsKey("release");
    }

    protected boolean isSongValuesValid(ContentValues values) {
        return values.containsKey("id") && values.containsKey("name") && values.containsKey("duration");
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (URI_MATCHER.match(uri) == ALBUM_ROW_CODE && isAlbumValuesValid(values)) {
            Album album = new Album();
            int id = (int) ContentUris.parseId(uri);
            album.setId(id);
            album.setName(values.getAsString("name"));
            album.setReleaseDate(values.getAsString("release"));
            int updatedRows = mMusicDao.updateAlbumInfo(album);
            return updatedRows;
        } else {
            throw new IllegalArgumentException("cant add multiple items");
        }

    }

    protected int updateAlbum(@NonNull Uri uri, ContentValues values) {
        Album album = new Album();
        int id = (int) ContentUris.parseId(uri);
        album.setId(id);
        album.setName(values.getAsString("name"));
        album.setReleaseDate(values.getAsString("release"));
        int updatedRows = mMusicDao.updateAlbumInfo(album);
        return updatedRows;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if (URI_MATCHER.match(uri) == ALBUM_ROW_CODE) {
            int id = (int) ContentUris.parseId(uri);
            return mMusicDao.deleteAlbumById(id);
        } else {
            throw new IllegalArgumentException("cant add multiple items");
        }

    }
}
