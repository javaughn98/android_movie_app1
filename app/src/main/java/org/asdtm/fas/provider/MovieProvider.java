package org.asdtm.fas.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import org.asdtm.fas.provider.MovieContract.FavMoviesColumns;
import org.asdtm.fas.provider.MovieContract.Movies;
import org.asdtm.fas.provider.MovieContract.FavMovies;
import org.asdtm.fas.provider.MovieContract.MoviesColumns;
import org.asdtm.fas.provider.MovieContract.Persons;
import org.asdtm.fas.provider.MovieContract.PersonsColumns;
import org.asdtm.fas.provider.MovieContract.TVs;
import org.asdtm.fas.provider.MovieContract.TVsColumns;
import org.asdtm.fas.provider.MovieContract.FavTVs;
import org.asdtm.fas.provider.MovieContract.FavTVsColumns;
import org.asdtm.fas.provider.MovieContract.FavMusic;
import org.asdtm.fas.provider.MovieContract.FavMusicColumns;

public class MovieProvider extends ContentProvider {

    private static final String TAG = MovieProvider.class.getSimpleName();

    private MovieDatabase mOpenHelper;

    private MovieProviderUriMatcher mUriMatcher;

    @Override
    public boolean onCreate() {
        mOpenHelper = new MovieDatabase(getContext());
        mUriMatcher = new MovieProviderUriMatcher();
        return true;
    }

    //return a cursor with data from the database
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        MovieUriEnum matchingUriEnum = mUriMatcher.matchUri(uri);

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(matchingUriEnum.table);
        switch (matchingUriEnum) {
            case MOVIES_ID:
                qb.appendWhere(MoviesColumns.MOVIE_ID + "=" + Movies.getMovieId(uri));
                break;
            case TVS_ID:
                qb.appendWhere(TVsColumns.TV_ID + "=" + TVs.getTVId(uri));
                break;
            case PERSONS_ID:
                qb.appendWhere(PersonsColumns.PERSON_ID + "=" + Persons.getPersonId(uri));
                break;
            case FAVMOVIES_ID:
                qb.appendWhere(FavMoviesColumns.MOVIE_ID + "=" + FavMovies.getMovieId(uri));
                break;
            case FAVTVS_ID:
                qb.appendWhere(FavTVsColumns.TV_ID + "=" + FavTVs.getTVId(uri));
                break;
            case FAVMUSIC_ID:
                qb.appendWhere(FavMusicColumns.MUSIC_ID + "=" + FavMusic.getMusicId(uri));
                break;
        }

        Cursor cursor = db.query(matchingUriEnum.table,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);

        if (getContext() != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        MovieUriEnum matchingUriEnum = mUriMatcher.matchUri(uri);
        return matchingUriEnum.contentType;
    }

    //insert new data into the database
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        MovieUriEnum matchingUriEnum = mUriMatcher.matchUri(uri);

        if (matchingUriEnum.table != null) {
            try {
                db.insertOrThrow(matchingUriEnum.table, null, values);
            } catch (SQLiteConstraintException e) {
                throw e;
            }
        }

        switch (matchingUriEnum) {
            case MOVIES:
                return Movies.buildMovieUri(values.getAsString(Movies.MOVIE_ID));
            case TVS:
                return TVs.buildTVUri(values.getAsString(TVs.TV_ID));
            case PERSONS:
                return Persons.buildPersonUri(values.getAsString(Persons.PERSON_ID));
            case FAVMOVIES:
                return FavMovies.buildMovieUri(values.getAsString(FavMovies.MOVIE_ID));
            case FAVTVS:
                return FavTVs.buildTVUri(values.getAsString(FavTVs.TV_ID));
            case FAVMUSIC:
                return FavMusic.buildMusicUri(values.getAsString(FavMusic.MUSIC_ID));
            default:
                throw new UnsupportedOperationException("Unknown insert URI: " + uri);
        }
    }

    //delete data from database
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        MovieUriEnum matchingUriEnum = mUriMatcher.matchUri(uri);

        int count = 0;
        switch (matchingUriEnum) {
            case MOVIES_ID:
                String movieId = Movies.getMovieId(uri);
                if (TextUtils.isEmpty(selection)) {
                    selection = MoviesColumns.MOVIE_ID + " = " + movieId;
                } else {
                    selection = selection + " AND " + MoviesColumns.MOVIE_ID + " = " + movieId;
                }
                break;
            case TVS_ID:
                String tvID = TVs.getTVId(uri);
                if (TextUtils.isEmpty(selection)) {
                    selection = TVsColumns.TV_ID + " = " + tvID;
                } else {
                    selection = selection + " AND " + TVsColumns.TV_ID + " = " + tvID;
                }
                break;
            case PERSONS_ID:
                String personID = Persons.getPersonId(uri);
                if (TextUtils.isEmpty(selection)) {
                    selection = PersonsColumns.PERSON_ID + "=?";
                    selectionArgs = new String[]{personID};
                } else {
                    selection = selection + " AND " + PersonsColumns.PERSON_ID + "=" + personID;
                    selectionArgs = new String[]{personID};
                }
                break;
            case FAVMOVIES_ID:
                String favMoviesId = FavMovies.getMovieId(uri);
                if (TextUtils.isEmpty(selection)) {
                    selection = FavMoviesColumns.MOVIE_ID + " = " + favMoviesId;
                } else {
                    selection = selection + " AND " + FavMoviesColumns.MOVIE_ID + " = " + favMoviesId;
                }
                break;
            case FAVTVS_ID:
                String favTVsId = FavTVs.getTVId(uri);
                if (TextUtils.isEmpty(selection)) {
                    selection = FavTVsColumns.TV_ID + " = " + favTVsId;
                } else {
                    selection = selection + " AND " + FavTVsColumns.TV_ID + " = " + favTVsId;
                }
                break;
            case FAVMUSIC_ID:
                String favMusicId = FavMusic.getMusicId(uri);
                if (TextUtils.isEmpty(selection)) {
                    selection = FavMusicColumns.MUSIC_ID + " = " + favMusicId;
                } else {
                    selection = selection + " AND " + FavMusicColumns.MUSIC_ID + " = " + favMusicId;
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }

        count = db.delete(matchingUriEnum.table, selection, selectionArgs);
        notifyChanged(uri);
        return count;
    }

    //update data in the database
    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        MovieUriEnum matchingUriEnum = mUriMatcher.matchUri(uri);

        int count = 0;
        switch (matchingUriEnum) {
            case MOVIES_ID:
                String movieId = Movies.getMovieId(uri);
                if (TextUtils.isEmpty(selection)) {
//                    selection = MoviesColumns.MOVIE_ID + " = " + movieId;
                    selection = MoviesColumns.MOVIE_ID + "=?";
                    selectionArgs = new String[]{movieId};
                } else {
//                    selection = selection + " AND " + MoviesColumns.MOVIE_ID + " = " + movieId;
                    selection = selection + " AND " + MoviesColumns.MOVIE_ID + "=?";
                    selectionArgs = new String[]{movieId};
                }
                break;
            case FAVMOVIES_ID:
                String favMoviesId = FavMovies.getMovieId(uri);
                if (TextUtils.isEmpty(selection)) {
  //                  selection = MoviesColumns.MOVIE_ID + " = " + favMoviesId;
                    selection = FavMoviesColumns.MOVIE_ID + "=?";
                    selectionArgs = new String[]{favMoviesId};
                }
                break;
            case TVS_ID:
                String tvId = TVs.getTVId(uri);
                if (TextUtils.isEmpty(selection)) {
                    selection = TVsColumns.TV_ID + "=" + tvId;
                } else {
                    selection = selection + " AND " + TVsColumns.TV_ID + "=" + tvId;
                }
                break;
            case FAVTVS_ID:
                String favTVId = FavTVs.getTVId(uri);
                if (TextUtils.isEmpty(selection)) {
                    selection = FavTVsColumns.TV_ID + "=?" + favTVId;
                } else {
                    selection = selection + " AND " + FavTVsColumns.TV_ID + "=" + favTVId;
                    selectionArgs = new String[]{favTVId};
                }
                break;
            case FAVMUSIC_ID:
                String favMusicId = FavMusic.getMusicId(uri);
                if (TextUtils.isEmpty(selection)) {
                    selection = FavMusicColumns.MUSIC_ID + "=?";
                    selectionArgs = new String[]{favMusicId};
                } else {
                    selection = selection + " AND " + FavMusicColumns.MUSIC_ID + "=" + favMusicId;
                    selectionArgs = new String[]{favMusicId};
                }
                break;
            case PERSONS_ID:
                String person_id = Persons.getPersonId(uri);
                if (TextUtils.isEmpty(selection)) {
                    selection = PersonsColumns.PERSON_ID + "=?";
                    selectionArgs = new String[]{person_id};
                } else {
                    selection = selection + " AND " + PersonsColumns.PERSON_ID + "=?";
                    selectionArgs = new String[]{person_id};
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }

        count = db.update(matchingUriEnum.table, values, selection, selectionArgs);
        notifyChanged(uri);
        return count;
    }

    private void notifyChanged(Uri uri) {
        Context context = getContext();
        if (context != null) {
            context.getContentResolver().notifyChange(uri, null);
        }
    }
}
