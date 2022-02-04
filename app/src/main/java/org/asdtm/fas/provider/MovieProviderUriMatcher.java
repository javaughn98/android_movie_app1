package org.asdtm.fas.provider;

import android.content.UriMatcher;
import android.net.Uri;
import android.util.Log;
import android.util.SparseArray;

public class MovieProviderUriMatcher {

    private UriMatcher mUriMatcher;

    private SparseArray<MovieUriEnum> mEnumSparseArray = new SparseArray<>();

    /* MovieProviderUriMatcher constructor */
    public MovieProviderUriMatcher() {
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        buildUriMatcher();
    }

    /* Builds the uri matcher from the uri enum */
    private void buildUriMatcher() {
        final String authority = MovieContract.CONTENT_AUTHORITY;

        MovieUriEnum[] uris = MovieUriEnum.values();
        for (MovieUriEnum uri : uris) {
            mUriMatcher.addURI(authority, uri.path, uri.code);
        }

        buildEnumsSparseArray();
    }

    /* Adds the uri code and uri to the enumSparseArray */
    private void buildEnumsSparseArray() {
        MovieUriEnum[] uris = MovieUriEnum.values();
        for (MovieUriEnum uri : uris) {
            mEnumSparseArray.put(uri.code, uri);
        }
    }

    /* Returns the MovieUriEnum if the uri is matched */
    public MovieUriEnum matchUri(Uri uri) {
        final int code = mUriMatcher.match(uri);
        try {
            return matchCode(code);
        } catch (UnsupportedOperationException e) {
            throw new UnsupportedOperationException("Unknown URI: " + uri);
        }
    }

    /* Returns the MovieUriEnum if the uri code is matched */
    private MovieUriEnum matchCode(int code) {
        MovieUriEnum movieUriEnum = mEnumSparseArray.get(code);
        if (movieUriEnum != null) {
            return movieUriEnum;
        } else {
            throw new UnsupportedOperationException("Unknown URI with code: " + code);
        }
    }
}
