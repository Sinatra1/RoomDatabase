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

public class MusicProvider extends ContentProvider {

    private static final String TAG = MusicProvider.class.getSimpleName();

    private static final String AUTHORITY = "com.elegion.roomdatabase.musicprovider";
    private static final String TABLE_ALBUM = "album";

    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    private static final int ALBUM_TABLE_CODE = 100;
    private static final int ALBUM_ROW_CODE = 101;

    static {
        URI_MATCHER.addURI(AUTHORITY, TABLE_ALBUM, ALBUM_TABLE_CODE);
        URI_MATCHER.addURI(AUTHORITY, TABLE_ALBUM + "/*", ALBUM_ROW_CODE);
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
            default:
                throw new UnsupportedOperationException("not yet implemented");
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        int code = URI_MATCHER.match(uri);

        if (code != ALBUM_ROW_CODE && code != ALBUM_TABLE_CODE) return null;

        Cursor cursor;

        if (code == ALBUM_TABLE_CODE) {
            cursor = mMusicDao.getAlbumsCursor();
        } else {
            cursor = mMusicDao.getAlbumWithIdCursor((int) ContentUris.parseId(uri));
        }
        return cursor;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        if (URI_MATCHER.match(uri) == ALBUM_TABLE_CODE && isValuesValid(values)) {
            Album album = new Album();
            Integer id = values.getAsInteger("id");
            album.setId(id);
            album.setName(values.getAsString("name"));
            album.setReleaseDate(values.getAsString("release"));
            mMusicDao.insertAlbum(album);
            return ContentUris.withAppendedId(uri, id);
        } else {
            throw new IllegalArgumentException("cant add multiple items");
        }
    }

    private boolean isValuesValid(ContentValues values) {
        return values.containsKey("id") && values.containsKey("name") && values.containsKey("release");
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (URI_MATCHER.match(uri) == ALBUM_ROW_CODE && isValuesValid(values)) {
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
