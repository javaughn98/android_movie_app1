package org.asdtm.fas.activity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.asdtm.fas.InventumContextWrapper;
import org.asdtm.fas.R;
import org.asdtm.fas.model.Track;
import org.asdtm.fas.model.Artist;
import org.asdtm.fas.model.Album;
import org.asdtm.fas.provider.MovieContract;
import org.asdtm.fas.util.AppUtils;
import org.asdtm.fas.util.Constants;
import org.asdtm.fas.util.ContentValuesUtils;
import org.asdtm.fas.util.PrefUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

public class MusicDetailsActivity extends AppCompatActivity {

    private static final String TAG = MusicDetailsActivity.class.getSimpleName();

    private static final String TRACK_ID = "org.asdtm.fas.details.track_id";

    private String mId;
    private Track track;
    private Track favTrack;
    private Artist artist;
    private Album album;

    private Cursor mCursor;
    private Cursor favCursor;
    private Cursor allFavCursor;
    private ContentResolver mResolver;
    private ContentResolver favResolver;

    private boolean hasBeenFavourited = false;

    //bind a drawable background image
    @BindDrawable(R.drawable.background_reel) Drawable placeholderImage;

    //bind various ui elements to resource ids
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.track_cover)
    ImageView trackCover;
    @BindView(R.id.track_name)
    TextView trackName;
    @BindView(R.id.track_artist_name)
    TextView artistName;
    @BindView(R.id.track_duration)
    TextView trackDuration;
    @BindView(R.id.track_album_cover)
    ImageView albumCover;
    @BindView(R.id.track_album_title)
    TextView albumTitle;
    @BindView(R.id.track_artist_photo)
    ImageView artistPhoto;
    @BindView(R.id.track_artist_view_name)
    TextView artistViewName;
    @BindView(R.id.progressBar) SmoothProgressBar progressBar;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(InventumContextWrapper.wrap(base));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_details);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //get the track id from the intent that started this activity
        Bundle args = getIntent().getExtras();
        mId = args.getString(TRACK_ID);

        track = new Track();
        album = new Album();
        artist = new Artist();

        mResolver = getContentResolver();
        favResolver = getContentResolver();
        favCursor = favResolver.query(MovieContract.FavMusic.CONTENT_URI,
                null,
                MovieContract.FavMusic.MUSIC_ID + "=?",
                new String []{String.valueOf(mId)},
                null);
        allFavCursor = favResolver.query(MovieContract.FavMusic.CONTENT_URI,
                null,
                null,
                null,
                null);

        loadFromInternet();

        // set the status bar color to black
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.BLACK);
    }

    private void loadFromInternet() {
        if (!AppUtils.isNetworkAvailableAndConnected(this)) {
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.network_error), Snackbar.LENGTH_LONG).show();
        }

        updateProgressBar(true);
        String lang = PrefUtils.getFormatLocale(MusicDetailsActivity.this);
        RequestQueue queue = Volley.newRequestQueue(MusicDetailsActivity.this);
        String url = "https://api.deezer.com/track/" + mId;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new com.android.volley.Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    setDataFromJSON(response);
                    favTrack = track;
                    // set the track cover image
                    Picasso.with(MusicDetailsActivity.this)
                            .load(Constants.DEEZER_IMAGE_URL + track.getCoverPath() + Constants.COVER_MEDIUM)
                            .placeholder(placeholderImage)
                            .fit().centerCrop()
                            .noFade()
                            .into(trackCover);

                    // set the artist image
                    Picasso.with(MusicDetailsActivity.this)
                            .load(artist.getArtistImage())
                            .placeholder(placeholderImage)
                            .fit().centerCrop()
                            .error(placeholderImage)
                            .into(artistPhoto);

                    // set the album cover image
                    Picasso.with(MusicDetailsActivity.this)
                            .load(album.getAlbumImage())
                            .placeholder(placeholderImage)
                            .fit().centerCrop()
                            .error(placeholderImage)
                            .into(albumCover);

                    // Update the data on the page with the data from the response
                    setTitle(track.getSongTitle());
                    trackName.setText(track.getSongTitle());
                    trackDuration.setText(track.durationToString());
                    artistName.setText(artist.getArtistName());
                    artistViewName.setText(artist.getArtistName());
                    albumTitle.setText(album.getAlbumName());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        queue.add(jsonObjectRequest);
        updateProgressBar(false);
    }

    private void setDataFromJSON(JSONObject data) throws JSONException {
        if (data != null) {
            JSONObject artistData = data.getJSONObject("artist");
            JSONObject albumData = data.getJSONObject("album");

            track.setSongId(String.valueOf(data.getInt("id")));
            track.setSongTitle(data.getString("title"));
            track.setDuration(data.getInt("duration"));
            track.setExplicit(data.getBoolean("explicit_lyrics"));
            track.setCoverPath(data.getString("md5_image"));
            artist.setArtistId(String.valueOf(artistData.getInt("id")));
            artist.setArtistName(artistData.getString("name"));
            artist.setArtistImage(artistData.getString("picture_medium"));
            album.setAlbumId(String.valueOf(albumData.getInt("id")));
            album.setAlbumName(albumData.getString("title"));
            album.setAlbumImage(albumData.getString("cover_medium"));
        }
    }

    public void updateProgressBar(boolean visible) {
        if (progressBar != null) {
            progressBar.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);

            if (visible) {
                progressBar.progressiveStart();
            } else {
                progressBar.progressiveStop();
            }
        }
    }

    public static Intent newIntent(Context context, String id) {
        Intent intent = new Intent(context, MusicDetailsActivity.class);
        Bundle args = new Bundle();
        args.putString(TRACK_ID, id);
        intent.putExtras(args);
        return intent;
    }

    private void addMusicToFavourites(Track track, Artist artist, Album album) {
        ContentValues values = ContentValuesUtils.setFavMusicValues(track, artist, album);
        if (favCursor != null) {
            String id = MovieContract.FavMusic.getMusicId(MovieContract.FavMusic.buildMusicUri(mId));
            if (favCursor.getCount() == 0) {
                favResolver.insert(MovieContract.FavMusic.CONTENT_URI, values);
            } else {
                Uri uri = MovieContract.FavMusic.buildMusicUri(values.getAsString(MovieContract.FavMusic.MUSIC_ID));
                favResolver.update(uri, values, null, null);
            }
        }
    }

    private void deleteMusicFromFavourites() {
        String[] args = { mId };
        Uri uri = MovieContract.FavMusic.buildMusicUri(mId);
        mResolver.delete(uri, null, null);
    }

    private boolean isInFavouritesList(String id) {
        ArrayList<String> favIDs = new ArrayList<>();
        if (allFavCursor != null) {
            while (allFavCursor.moveToNext()) {
                favIDs.add(allFavCursor.getString(allFavCursor.getColumnIndex(MovieContract.FavMusic.MUSIC_ID)));
            }
            return favIDs.contains(id);
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);

        if (isInFavouritesList(mId)) {
            MenuItem favouriteButton = mToolbar.getMenu().findItem(R.id.menu_favourite);
            favouriteButton.setIcon(android.R.drawable.btn_star_big_on);
            hasBeenFavourited = true;
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_update:
                loadFromInternet();
                return true;
            case R.id.menu_favourite:
                hasBeenFavourited = !hasBeenFavourited;
                MenuItem favouriteButton = mToolbar.getMenu().findItem(R.id.menu_favourite);
                if (favouriteButton != null) {
                    if (hasBeenFavourited) {
                        favouriteButton.setIcon(android.R.drawable.btn_star_big_on);
                        if (favTrack != null) {
                            addMusicToFavourites(favTrack, artist, album);
                        }
                    } else {
                        favouriteButton.setIcon(android.R.drawable.btn_star_big_off);
                        deleteMusicFromFavourites();
                    }
                }

        }

        return super.onOptionsItemSelected(item);
    }
}
