package org.asdtm.fas.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import org.asdtm.fas.provider.MovieContract.FavMoviesColumns;
import org.asdtm.fas.provider.MovieContract.MoviesColumns;
import org.asdtm.fas.provider.MovieContract.PersonsColumns;
import org.asdtm.fas.provider.MovieContract.TVsColumns;
import org.asdtm.fas.provider.MovieContract.FavTVsColumns;
import org.asdtm.fas.provider.MovieContract.FavMusicColumns;

public class MovieDatabase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "fas.db";
    private static final int DATABASE_VERSION = 1;

    private Context mContext;

    //Added new table name for favourite movies to Tables interface
    interface Tables {
        String FAVMOVIES = "favMovies";
        String MOVIES = "movies";
        String FAVTVS = "favTVs";
        String FAVMUSIC = "favMusic";
        String TVS = "tvs";
        String PERSONS = "persons";
    }

    public MovieDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        /* Creates a table for movies */
        sqLiteDatabase.execSQL("CREATE TABLE " + Tables.MOVIES + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + MoviesColumns.MOVIE_ID + " TEXT NOT NULL,"
                + MoviesColumns.MOVIE_IMDB_ID + " TEXT,"
                + MoviesColumns.MOVIE_ADULT + " TEXT,"
                + MoviesColumns.MOVIE_TITLE + " TEXT,"
                + MoviesColumns.MOVIE_ORIGINAL_TITLE + " TEXT,"
                + MoviesColumns.MOVIE_ORIGINAL_LANGUAGE + " TEXT,"
                + MoviesColumns.MOVIE_OVERVIEW + " TEXT,"
                + MoviesColumns.MOVIE_TAGLINE + " TEXT,"
                + MoviesColumns.MOVIE_VOTE_COUNT + " INTEGER,"
                + MoviesColumns.MOVIE_VOTE_AVERAGE + " REAL,"
                + MoviesColumns.MOVIE_POSTER_PATH + " TEXT,"
                + MoviesColumns.MOVIE_BACKDROP_PATH + " TEXT,"
                + MoviesColumns.MOVIE_BUDGET + " INTEGER,"
                + MoviesColumns.MOVIE_HOMEPAGE + " TEXT,"
                + MoviesColumns.MOVIE_POPULARITY + " REAL,"
                + MoviesColumns.MOVIE_REVENUE + " INTEGER,"
                + MoviesColumns.MOVIE_RUNTIME + " INTEGER,"
                + MoviesColumns.MOVIE_STATUS + " TEXT,"
                + MoviesColumns.MOVIE_GENRES + " TEXT,"
                + MoviesColumns.MOVIE_PRODUCTION_COUNTRIES + " TEXT,"
                + MoviesColumns.MOVIE_RELEASE_DATE + " TEXT)"
        );

        /* Creates a table for tv */
        sqLiteDatabase.execSQL("CREATE TABLE " + Tables.TVS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + TVsColumns.TV_ID + " TEXT NOT NULL,"
                + TVsColumns.TV_NAME + " TEXT,"
                + TVsColumns.TV_ORIGINAL_NAME + " TEXT,"
                + TVsColumns.TV_ORIGINAL_LANGUAGE + " TEXT,"
                + TVsColumns.TV_OVERVIEW + " TEXT,"
                + TVsColumns.TV_POSTER_PATH + " TEXT,"
                + TVsColumns.TV_BACKDROP_PATH + " TEXT,"
                + TVsColumns.TV_EPISODE_RUNTIME + " INTEGER,"
                + TVsColumns.TV_FIRST_AIR_DATE + " TEXT,"
                + TVsColumns.TV_GENRES + " TEXT,"
                + TVsColumns.TV_HOMEPAGE + " TEXT,"
                + TVsColumns.TV_IN_PRODUCTION + " TEXT,"
                + TVsColumns.TV_LAST_AIR_DATE + " TEXT,"
                + TVsColumns.TV_NETWORKS + " TEXT,"
                + TVsColumns.TV_NUMBER_OF_EPISODES + " INTEGER,"
                + TVsColumns.TV_NUMBER_OF_SEASONS + " INTEGER,"
                + TVsColumns.TV_ORIGINAL_COUNTRY + " TEXT,"
                + TVsColumns.TV_POPULARITY + " REAL,"
                + TVsColumns.TV_STATUS + " TEXT,"
                + TVsColumns.TV_VOTE_AVERAGE + " REAL,"
                + TVsColumns.TV_VOTE_COUNT + " INTEGER)"
        );

        /* Creates a table for persons (unused in current application) */
        sqLiteDatabase.execSQL("CREATE TABLE " + Tables.PERSONS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + PersonsColumns.PERSON_ID + " TEXT NOT NULL,"
                + PersonsColumns.PERSON_IMDB_ID + " TEXT,"
                + PersonsColumns.PERSON_NAME + " TEXT,"
                + PersonsColumns.PERSON_ADULT + " TEXT,"
                + PersonsColumns.PERSON_BIOGRAPHY + " TEXT,"
                + PersonsColumns.PERSON_BIRTHDAY + " TEXT,"
                + PersonsColumns.PERSON_DEATHDAY + " TEXT,"
                + PersonsColumns.PERSON_HOMEPAGE + " TEXT,"
                + PersonsColumns.PERSON_PLACE_OF_BIRTH + " TEXT,"
                + PersonsColumns.PERSON_POPULARITY + " REAL,"
                + PersonsColumns.PERSON_PROFILE_PATH + " TEXT)"
        );

        sqLiteDatabase.execSQL("CREATE TABLE " + Tables.FAVMOVIES + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY,"
                + FavMoviesColumns.MOVIE_ID + " TEXT NOT NULL,"
                + FavMoviesColumns.MOVIE_IMDB_ID + " TEXT,"
                + FavMoviesColumns.MOVIE_ADULT + " TEXT,"
                + FavMoviesColumns.MOVIE_TITLE + " TEXT,"
                + FavMoviesColumns.MOVIE_ORIGINAL_TITLE + " TEXT,"
                + FavMoviesColumns.MOVIE_ORIGINAL_LANGUAGE + " TEXT,"
                + FavMoviesColumns.MOVIE_OVERVIEW + " TEXT,"
                + FavMoviesColumns.MOVIE_TAGLINE + " TEXT,"
                + FavMoviesColumns.MOVIE_VOTE_COUNT + " INTEGER,"
                + FavMoviesColumns.MOVIE_VOTE_AVERAGE + " REAL,"
                + FavMoviesColumns.MOVIE_POSTER_PATH + " TEXT,"
                + FavMoviesColumns.MOVIE_BACKDROP_PATH + " TEXT,"
                + FavMoviesColumns.MOVIE_BUDGET + " INTEGER,"
                + FavMoviesColumns.MOVIE_HOMEPAGE + " TEXT,"
                + FavMoviesColumns.MOVIE_POPULARITY + " REAL,"
                + FavMoviesColumns.MOVIE_REVENUE + " INTEGER,"
                + FavMoviesColumns.MOVIE_RUNTIME + " INTEGER,"
                + FavMoviesColumns.MOVIE_STATUS + " TEXT,"
                + FavMoviesColumns.MOVIE_GENRES + " TEXT,"
                + FavMoviesColumns.MOVIE_PRODUCTION_COUNTRIES + " TEXT,"
                + FavMoviesColumns.MOVIE_RELEASE_DATE + " TEXT)"
        );

        sqLiteDatabase.execSQL("CREATE TABLE " + Tables.FAVTVS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY,"
                + FavTVsColumns.TV_ID + " TEXT NOT NULL,"
                + FavTVsColumns.TV_NAME + " TEXT,"
                + FavTVsColumns.TV_ORIGINAL_NAME + " TEXT,"
                + FavTVsColumns.TV_ORIGINAL_LANGUAGE + " TEXT,"
                + FavTVsColumns.TV_OVERVIEW + " TEXT,"
                + FavTVsColumns.TV_POSTER_PATH + " TEXT,"
                + FavTVsColumns.TV_BACKDROP_PATH + " TEXT,"
                + FavTVsColumns.TV_EPISODE_RUNTIME + " INTEGER,"
                + FavTVsColumns.TV_FIRST_AIR_DATE + " TEXT,"
                + FavTVsColumns.TV_GENRES + " TEXT,"
                + FavTVsColumns.TV_HOMEPAGE + " TEXT,"
                + FavTVsColumns.TV_IN_PRODUCTION + " TEXT,"
                + FavTVsColumns.TV_LAST_AIR_DATE + " TEXT,"
                + FavTVsColumns.TV_NETWORKS + " TEXT,"
                + FavTVsColumns.TV_NUMBER_OF_EPISODES + " INTEGER,"
                + FavTVsColumns.TV_NUMBER_OF_SEASONS + " INTEGER,"
                + FavTVsColumns.TV_ORIGINAL_COUNTRY + " TEXT,"
                + FavTVsColumns.TV_POPULARITY + " REAL,"
                + FavTVsColumns.TV_STATUS + " TEXT,"
                + FavTVsColumns.TV_VOTE_AVERAGE + " REAL,"
                + FavTVsColumns.TV_VOTE_COUNT + " INTEGER)"
        );

        sqLiteDatabase.execSQL("CREATE TABLE " + Tables.FAVMUSIC + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY,"
                + FavMusicColumns.MUSIC_ID + " TEXT NOT NULL,"
                + FavMusicColumns.MUSIC_NAME + " TEXT,"
                + FavMusicColumns.MUSIC_DUR + " INTEGER,"
                + FavMusicColumns.MUSIC_ARTIST + " TEXT,"
                + FavMusicColumns.MUSIC_ALBUM + " TEXT,"
                + FavMusicColumns.MUSIC_POSTER_PATH + " TEXT)"
        );

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Tables.MOVIES);
        onCreate(sqLiteDatabase);
    }
}
