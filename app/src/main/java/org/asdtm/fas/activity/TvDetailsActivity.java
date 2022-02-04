package org.asdtm.fas.activity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.media.tv.TvContract;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.iarcuschin.simpleratingbar.SimpleRatingBar;
import com.squareup.picasso.Picasso;

import org.asdtm.fas.InventumContextWrapper;
import org.asdtm.fas.R;
import org.asdtm.fas.model.TV;
import org.asdtm.fas.model.Video;
import org.asdtm.fas.model.VideoResults;
import org.asdtm.fas.provider.MovieContract;
import org.asdtm.fas.service.ServiceGenerator;
import org.asdtm.fas.service.TvService;
import org.asdtm.fas.util.AppUtils;
import org.asdtm.fas.util.Constants;
import org.asdtm.fas.util.ContentValuesUtils;
import org.asdtm.fas.util.PrefUtils;
import org.asdtm.fas.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class TvDetailsActivity extends AppCompatActivity {

    private static final String TAG = TvDetailsActivity.class.getSimpleName();

    private static final String TV_ID = "org.asdtm.fas.details.tv_id";

    private Cursor mCursor;
    private Cursor favCursor;
    private Cursor allFavCursor;
    private ContentResolver mResolver;
    private ContentResolver favResolver;
    private String mId;
    private TV favTv;

    private boolean hasBeenFavourited = false;

    @BindDrawable(R.drawable.background_reel) Drawable placeholderImage;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.tv_details_backdrop) ImageView backdrop;
    @BindView(R.id.tv_details_poster) ImageView poster;
    @BindView(R.id.tv_details_name) TextView name;
    @BindView(R.id.tv_details_original_name) TextView originalName;
    @BindView(R.id.tv_details_genres) TextView genres;
    @BindView(R.id.tv_details_country) TextView country;
    @BindView(R.id.tv_details_runtime) TextView runtime;
    @BindView(R.id.tv_details_overview) TextView overview;
    @BindView(R.id.tv_details_first_air_date) TextView firstAirDate;
    @BindView(R.id.tv_details_status) TextView status;
    @BindView(R.id.tv_details_networks) TextView networks;
    @BindView(R.id.tv_details_homepage) TextView homepage;
    @BindView(R.id.rating_rating_bar) SimpleRatingBar ratingBar;
    @BindView(R.id.rating_vote_average) TextView voteAverageView;
    @BindView(R.id.rating_vote_count) TextView voteCountView;
    @BindView(R.id.progressBar) SmoothProgressBar progressBar;
    @BindView(R.id.tv_details_video_layout_root) LinearLayout videoLayout;
    @BindView(R.id.tv_details_video_preview) ImageView videoPreview;
    @BindView(R.id.tv_details_video_title) TextView videoTitle;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(InventumContextWrapper.wrap(base));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tv_details);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Bundle args = getIntent().getExtras();
        mId = args.getString(TV_ID);

        mResolver = getContentResolver();
        mCursor = mResolver.query(MovieContract.TVs.CONTENT_URI,
                null,
                MovieContract.TVs.TV_ID + "=?",
                new String[]{String.valueOf(mId)},
                null);

        favResolver = getContentResolver();
        favCursor = favResolver.query(MovieContract.FavTVs.CONTENT_URI,
                null,
                MovieContract.FavTVs.TV_ID + "=?",
                new String[] {String.valueOf(mId)},
                null);
        allFavCursor = favResolver.query(MovieContract.FavTVs.CONTENT_URI,
                null,
                null,
                null,
                null);

        if (mCursor != null) {
            loadFromInternet();
        }

        if (mCursor != null) {
            mCursor.close();
        }
    }

    private void loadFromDb() {
        if (mCursor.moveToNext()) {
            String backdropPath = mCursor.getString(mCursor.getColumnIndex(MovieContract.TVs.TV_BACKDROP_PATH));
            String posterPath = mCursor.getString(mCursor.getColumnIndex(MovieContract.TVs.TV_POSTER_PATH));
            if (backdropPath != null) {
                Picasso.with(TvDetailsActivity.this)
                        .load(Constants.TMDB_IMAGE_URL + Constants.BACKDROP_SIZE_W780 + backdropPath)
                        .placeholder(placeholderImage)
                        .fit().centerCrop()
                        .error(placeholderImage)
                        .into(backdrop);
            } else {
                Picasso.with(TvDetailsActivity.this)
                        .load(Constants.TMDB_IMAGE_URL + Constants.POSTER_SIZE_W500 + posterPath)
                        .placeholder(placeholderImage)
                        .fit().centerCrop()
                        .error(placeholderImage)
                        .into(backdrop);
            }
            Picasso.with(TvDetailsActivity.this)
                    .load(Constants.TMDB_IMAGE_URL + Constants.POSTER_SIZE_W342 + posterPath)
                    .placeholder(placeholderImage)
                    .fit().centerCrop()
                    .error(placeholderImage)
                    .into(poster);

            String nameStr = mCursor.getString(mCursor.getColumnIndex(MovieContract.TVs.TV_NAME));
            String originalNameStr = mCursor.getString(mCursor.getColumnIndex(MovieContract.TVs.TV_ORIGINAL_NAME));
            String firstAirDateStr = mCursor.getString(mCursor.getColumnIndex(MovieContract.TVs.TV_FIRST_AIR_DATE));
            String lastAirDateStr = mCursor.getString(mCursor.getColumnIndex(MovieContract.TVs.TV_LAST_AIR_DATE));
            String genresStr = mCursor.getString(mCursor.getColumnIndex(MovieContract.TVs.TV_GENRES));
            String countryStr = mCursor.getString(mCursor.getColumnIndex(MovieContract.TVs.TV_ORIGINAL_COUNTRY));
            int runtimeInt = mCursor.getInt(mCursor.getColumnIndex(MovieContract.TVs.TV_EPISODE_RUNTIME));
            String overviewStr = mCursor.getString(mCursor.getColumnIndex(MovieContract.TVs.TV_OVERVIEW));
            int isInProduction = mCursor.getInt(mCursor.getColumnIndex(MovieContract.TVs.TV_IN_PRODUCTION));
            String statusStr = mCursor.getString(mCursor.getColumnIndex(MovieContract.TVs.TV_STATUS));
            String networksStr = mCursor.getString(mCursor.getColumnIndex(MovieContract.TVs.TV_NETWORKS));
            String homepageStr = mCursor.getString(mCursor.getColumnIndex(MovieContract.TVs.TV_HOMEPAGE));
            float voteAverageFlt = mCursor.getFloat(mCursor.getColumnIndex(MovieContract.TVs.TV_VOTE_AVERAGE));
            int voteCountInt = mCursor.getInt(mCursor.getColumnIndex(MovieContract.TVs.TV_VOTE_COUNT));

            setTitle(nameStr);
            name.setText(nameStr);
            if (!statusStr.equals(Constants.TV_STATUS_ENDED)) {
                originalName.setText(getString(R.string.details_tv_name,
                        originalNameStr,
                        StringUtils.getYear(firstAirDateStr),
                        getString(R.string.three_dots)));
            } else {
                originalName.setText(getString(R.string.details_tv_name,
                        originalNameStr,
                        StringUtils.getYear(firstAirDateStr),
                        StringUtils.getYear(lastAirDateStr)));
            }
            genres.setText(genresStr);
            country.setText(countryStr);
            runtime.setText(StringUtils.getEpisodeRuntime(this, runtimeInt));
            overview.setText(overviewStr);
            firstAirDate.setText(StringUtils.formatReleaseDate(firstAirDateStr));
            status.setText(statusStr);
            networks.setText(networksStr);
            homepage.setText(homepageStr);
            ratingBar.setRating(voteAverageFlt);
            voteAverageView.setText(String.valueOf(voteAverageFlt));
            voteCountView.setText(String.valueOf(voteCountInt));
            loadVideoPreview();
        }
    }

    private void loadFromInternet() {
        if (!AppUtils.isNetworkAvailableAndConnected(this)) {
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.network_error), Snackbar.LENGTH_LONG).show();
        }

        updateProgressBar(true);
        String lang = PrefUtils.getFormatLocale(TvDetailsActivity.this);
        TvService service = ServiceGenerator.createService(TvService.class);
        Call<TV> call = service.tvDetails(String.valueOf(mId), ServiceGenerator.API_KEY, lang);
        call.enqueue(new Callback<TV>() {
            @Override
            public void onResponse(Call<TV> call, Response<TV> response) {
                if (response.isSuccessful()) {
                    TV tv = response.body();
                    addOrUpdate(tv);
                    favTv = tv;

                    String countryStr = (!tv.getOriginCountry().isEmpty()) ? tv.getOriginCountry().get(0) : "-";
                    int runtimeInt = (!tv.getEpisodeRunTime().isEmpty()) ? tv.getEpisodeRunTime().get(0) : 0;
                    String networksStr = (!tv.getNetworks().isEmpty()) ? tv.getNetworks().get(0).getName() : "-";

                    if (tv.getBackdropPath() != null) {
                        Picasso.with(TvDetailsActivity.this)
                                .load(Constants.TMDB_IMAGE_URL + Constants.BACKDROP_SIZE_W780 + tv.getBackdropPath())
                                .placeholder(placeholderImage)
                                .fit().centerCrop()
                                .error(placeholderImage)
                                .into(backdrop);
                    } else {
                        Picasso.with(TvDetailsActivity.this)
                                .load(Constants.TMDB_IMAGE_URL + Constants.POSTER_SIZE_W500 + tv.getPosterPath())
                                .placeholder(placeholderImage)
                                .fit().centerCrop()
                                .error(placeholderImage)
                                .into(backdrop);
                    }
                    Picasso.with(TvDetailsActivity.this)
                            .load(Constants.TMDB_IMAGE_URL + Constants.POSTER_SIZE_W342 + tv.getPosterPath())
                            .placeholder(placeholderImage)
                            .fit().centerCrop()
                            .error(placeholderImage)
                            .into(poster);

                    setTitle(tv.getName());
                    name.setText(tv.getName());
                    if (!tv.getStatus().equals(Constants.TV_STATUS_ENDED)) {
                        originalName.setText(getString(R.string.details_tv_name,
                                tv.getOriginalName(),
                                StringUtils.getYear(tv.getFirstAirDate()),
                                getString(R.string.three_dots)));
                    } else {
                        originalName.setText(getString(R.string.details_tv_name,
                                tv.getOriginalName(),
                                StringUtils.getYear(tv.getFirstAirDate()),
                                StringUtils.getYear(tv.getLastAirDate())));
                    }
                    genres.setText(StringUtils.getGenres(tv.getGenres()));
                    country.setText(countryStr);
                    runtime.setText(StringUtils.getEpisodeRuntime(TvDetailsActivity.this, runtimeInt));
                    overview.setText(tv.getOverview());
                    firstAirDate.setText(StringUtils.formatReleaseDate(tv.getFirstAirDate()));
                    status.setText(tv.getStatus());
                    networks.setText(networksStr);
                    homepage.setText(tv.getHomepage());
                    ratingBar.setRating(tv.getVoteAverage());
                    voteAverageView.setText(String.valueOf(tv.getVoteAverage()));
                    voteCountView.setText(String.valueOf(tv.getVoteCount()));
                    loadVideoPreview();

                    updateProgressBar(false);
                }
            }

            @Override
            public void onFailure(Call<TV> call, Throwable t) {
                Log.e(TAG, "Error", t);
                updateProgressBar(false);
            }
        });
    }

    private void loadVideoPreview() {
        String lang = PrefUtils.getFormatLocale(TvDetailsActivity.this);
        TvService service = ServiceGenerator.createService(TvService.class);
        Call<VideoResults> call = service.tvVideos(String.valueOf(mId), ServiceGenerator.API_KEY, lang);
        call.enqueue(new Callback<VideoResults>() {
            @Override
            public void onResponse(Call<VideoResults> call, Response<VideoResults> response) {
                if (response.isSuccessful()) {
                    List<Video> videos = response.body().getVideos();
                    if (!videos.isEmpty()) {
                        Picasso.with(TvDetailsActivity.this)
                                .load(String.format(Constants.YOUTUBE_THUMBNAIL_URL, videos.get(0).getKey()))
                                .fit().centerCrop()
                                .into(videoPreview);
                        videoTitle.setText(getString(R.string.details_video_title, videos.size()));
                        videoLayout.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(Call<VideoResults> call, Throwable t) {
                Log.e(TAG, "Error loading video preview", t);
            }
        });
    }

    private void addTvToFavourites(TV tv) {
        ContentValues values = ContentValuesUtils.setFavTvValues(tv);
        if (favCursor != null) {
            String id = MovieContract.FavTVs.getTVId(MovieContract.FavTVs.buildTVUri(mId));
            if (favCursor.getCount() == 0) {
                favResolver.insert(MovieContract.FavTVs.CONTENT_URI, values);
            } else {
                Uri uri = MovieContract.FavTVs.buildTVUri(values.getAsString(MovieContract.FavTVs.TV_ID));
                favResolver.update(uri, values, null, null);
            }
        }
    }

    private void deleteTvFromFavourites() {
        String[] args = { mId };
        Uri uri = MovieContract.FavTVs.buildTVUri(mId);
        mResolver.delete(uri, null, null);
    }

    private boolean isInFavouritesList(String id) {
        ArrayList<String> favIDs = new ArrayList<>();
        if (allFavCursor != null) {
            while (allFavCursor.moveToNext()) {
                favIDs.add(allFavCursor.getString(allFavCursor.getColumnIndex(MovieContract.FavTVs.TV_ID)));
            }
            return favIDs.contains(id);
        }
        return false;
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

    private void addOrUpdate(TV tv) {
        ContentValues values = ContentValuesUtils.setTvValues(tv);
        if (mCursor != null) {
            if (mCursor.getCount() == 0) {
                mResolver.insert(MovieContract.TVs.CONTENT_URI, values);
            } else {
                Uri uri = MovieContract.TVs.buildTVUri(values.getAsString(MovieContract.TVs.TV_ID));
                mResolver.update(uri, values, null, null);
            }
        }
    }

    public static Intent newIntent(Context context, String id) {
        Intent intent = new Intent(context, TvDetailsActivity.class);
        Bundle args = new Bundle();
        args.putString(TV_ID, id);
        intent.putExtras(args);
        return intent;
    }

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
                        if (favTv != null) {
                            addTvToFavourites(favTv);
                        }
                    } else {
                        favouriteButton.setIcon(android.R.drawable.btn_star_big_off);
                        deleteTvFromFavourites();
                    }
                }
        }

        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.tv_details_video_layout_root)
    void openVideosActivity() {
        Intent intent = VideosActivity.newIntent(TvDetailsActivity.this, VideosActivity.TYPE_TV, mId);
        startActivity(intent);
    }
}
