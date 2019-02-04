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
import elegion.com.roomdatabase.database.AlbumSong;
import elegion.com.roomdatabase.database.MusicDao;
import elegion.com.roomdatabase.database.MusicDatabase;
import elegion.com.roomdatabase.database.Song;

public class MusicProvider extends ContentProvider {

    private static final String TAG = MusicProvider.class.getSimpleName();

    private static final String AUTHORITY = "com.elegion.roomdatabase.musicprovider";
    private static final String TABLE_ALBUM = "album";
    private static final String TABLE_SONG = "song";
    private static final String TABLE_ALBUMSONG = "albumsong";

    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    private static final int ALBUM_TABLE_CODE = 100;
    private static final int ALBUM_ROW_CODE = 101;
    private static final int SONG_TABLE_CODE = 102;
    private static final int SONG_ROW_CODE = 103;
    private static final int ALBUMSONG_TABLE_CODE = 104;
    private static final int ALBUMSONG_ROW_CODE = 105;

    static {
        URI_MATCHER.addURI(AUTHORITY, TABLE_ALBUM, ALBUM_TABLE_CODE);
        URI_MATCHER.addURI(AUTHORITY, TABLE_ALBUM + "/*", ALBUM_ROW_CODE);
        URI_MATCHER.addURI(AUTHORITY, TABLE_SONG, SONG_TABLE_CODE);
        URI_MATCHER.addURI(AUTHORITY, TABLE_SONG + "/*", SONG_ROW_CODE);
        URI_MATCHER.addURI(AUTHORITY, TABLE_ALBUMSONG, ALBUMSONG_TABLE_CODE);
        URI_MATCHER.addURI(AUTHORITY, TABLE_ALBUMSONG + "/*", ALBUMSONG_ROW_CODE);
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
            case ALBUMSONG_TABLE_CODE:
                return "vnd.android.cursor.dir/" + AUTHORITY + "." + TABLE_ALBUMSONG;
            case ALBUMSONG_ROW_CODE:
                return "vnd.android.cursor.item/" + AUTHORITY + "." + TABLE_ALBUMSONG;
            default:
                throw new UnsupportedOperationException("not yet implemented");
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        int code = URI_MATCHER.match(uri);

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
            case ALBUMSONG_TABLE_CODE:
                return mMusicDao.getAlbumSongsCursor();
            case ALBUMSONG_ROW_CODE:
                return mMusicDao.getAlbumSongWithIdCursor(id);
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
            case ALBUMSONG_TABLE_CODE:
                return insertAlbumSong(uri, values);
            default:
                throwIllegalArgumentException();
        }

        return null;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        switch (URI_MATCHER.match(uri)) {
            case ALBUM_ROW_CODE:
                return updateAlbum(uri, values);
            case SONG_ROW_CODE:
                return updateSong(uri, values);
            case ALBUMSONG_ROW_CODE:
                return updateAlbumSong(uri, values);
            default:
                throwIllegalArgumentException();
        }

        return 0;
    }

    protected Uri insertAlbum(@NonNull Uri uri, ContentValues values) {
        if (!isAlbumValuesValid(values)) {
            throwIllegalArgumentException();
            return null;
        }

        Album album = prepareAlbum(values);

        mMusicDao.insertAlbum(album);

        return ContentUris.withAppendedId(uri, values.getAsInteger("id"));
    }

    protected Uri insertSong(@NonNull Uri uri, ContentValues values) {
        if (!isSongValuesValid(values)) {
            throwIllegalArgumentException();
            return null;
        }

        Song song = prepareSong(values);
        mMusicDao.insertSong(song);

        return ContentUris.withAppendedId(uri, values.getAsInteger("id"));
    }

    protected Uri insertAlbumSong(@NonNull Uri uri, ContentValues values) {
        if (!isAlbumSongValuesValid(values)) {
            throwIllegalArgumentException();
            return null;
        }

        AlbumSong albumSong = prepareAlbumSong(values);
        mMusicDao.setLinkAlbumSong(albumSong);

        return ContentUris.withAppendedId(uri, values.getAsInteger("id"));
    }

    protected boolean isAlbumValuesValid(ContentValues values) {
        return values.containsKey("id") && values.containsKey("name") && values.containsKey("release");
    }

    protected boolean isSongValuesValid(ContentValues values) {
        return values.containsKey("id") && values.containsKey("name") && values.containsKey("duration");
    }

    protected boolean isAlbumSongValuesValid(ContentValues values) {
        return values.containsKey("id") && values.containsKey("album_id") && values.containsKey("song_id");
    }

    protected Integer updateAlbum(@NonNull Uri uri, ContentValues values) {
        if (!isAlbumValuesValid(values)) {
            throwIllegalArgumentException();
            return null;
        }

        Album album = prepareAlbum(values);
        int updatedRows = mMusicDao.updateAlbumInfo(album);
        return updatedRows;
    }

    protected Integer updateSong(@NonNull Uri uri, ContentValues values) {
        if (!isSongValuesValid(values)) {
            throwIllegalArgumentException();
            return null;
        }

        Song song = prepareSong(values);
        int updatedRows = mMusicDao.updateSongInfo(song);
        return updatedRows;
    }

    protected Integer updateAlbumSong(@NonNull Uri uri, ContentValues values) {
        if (!isAlbumSongValuesValid(values)) {
            throwIllegalArgumentException();
            return null;
        }

        AlbumSong albumSong = prepareAlbumSong(values);
        int updatedRows = mMusicDao.updateAlbumSongInfo(albumSong);
        return updatedRows;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int id = (int) ContentUris.parseId(uri);

        switch (URI_MATCHER.match(uri)) {
            case ALBUM_ROW_CODE:
                return mMusicDao.deleteAlbumById(id);
            case SONG_ROW_CODE:
                return mMusicDao.deleteSongById(id);
            case ALBUMSONG_ROW_CODE:
                return mMusicDao.deleteAlbumSongById(id);
            default:
                throwIllegalArgumentException();
        }

        return 0;
    }

    protected Album prepareAlbum(ContentValues values) {
        Album album = new Album();
        Integer id = values.getAsInteger("id");
        album.setId(id);
        album.setName(values.getAsString("name"));
        album.setReleaseDate(values.getAsString("release"));

        return album;
    }

    protected Song prepareSong(ContentValues values) {
        Song song = new Song();
        Integer id = values.getAsInteger("id");
        song.setId(id);
        song.setName(values.getAsString("name"));
        song.setDuration(values.getAsString("duration"));

        return song;
    }

    protected AlbumSong prepareAlbumSong(ContentValues values) {
        AlbumSong albumSong = new AlbumSong();
        Integer id = values.getAsInteger("id");
        albumSong.setId(id);
        albumSong.setAlbumId(values.getAsInteger("album_id"));
        albumSong.setSongId(values.getAsInteger("song_id"));

        return albumSong;
    }

    protected void throwIllegalArgumentException() {
        throw new IllegalArgumentException("cant add multiple items");
    }
}
