package org.asdtm.fas.activity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
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
import org.asdtm.fas.adapter.MoviesAdapter;
import org.asdtm.fas.fragment.FavouritesFragment;
import org.asdtm.fas.fragment.MoviesFragment;
import org.asdtm.fas.model.Movie;
import org.asdtm.fas.model.Video;
import org.asdtm.fas.model.VideoResults;
import org.asdtm.fas.provider.MovieContract;
import org.asdtm.fas.service.MovieService;
import org.asdtm.fas.service.ServiceGenerator;
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

public class MovieDetailsActivity extends AppCompatActivity {

    private static final String TAG = MovieDetailsActivity.class.getSimpleName();

    private static final String MOVIE_ID = "org.asdtm.fas.details.movie_id";

    //database accessor
    private Cursor mCursor;
    private Cursor favCursor;
    private Cursor allFavCursor;
    private ContentResolver mResolver;
    private ContentResolver favResolver;
    private String mId;
    private Movie mMovie;
    private Movie favMovie;

    //used for switching the favourite button icon from empty to filled / vice versa
    private boolean hasBeenFavourited = false;

    //bind a drawable background image
    @BindDrawable(R.drawable.background_reel) Drawable placeholderImage;

    //bind various ui elements to resource ids
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.page_movie_details) NestedScrollView contentView;
    @BindView(R.id.movie_details_backdrop) ImageView backdrop;
    @BindView(R.id.movie_details_poster) ImageView poster;
    @BindView(R.id.movie_details_name) TextView name;
    @BindView(R.id.movie_details_original_name) TextView originalName;
    @BindView(R.id.movie_details_genres) TextView genres;
    @BindView(R.id.movie_details_countries) TextView productionCountries;
    @BindView(R.id.movie_details_runtime) TextView runtime;
    @BindView(R.id.movie_details_tagline) TextView tagline;
    @BindView(R.id.movie_details_overview) TextView overview;
    @BindView(R.id.movie_details_release_date) TextView releaseDate;
    @BindView(R.id.movie_details_status) TextView status;
    @BindView(R.id.movie_details_imdb_page) TextView imdbPage;
    @BindView(R.id.movie_details_homepage) TextView homepage;
    @BindView(R.id.movie_details_budget) TextView budget;
    @BindView(R.id.movie_details_revenue) TextView revenue;
    @BindView(R.id.rating_rating_bar) SimpleRatingBar ratingBar;
    @BindView(R.id.rating_vote_average) TextView voteAverageView;
    @BindView(R.id.rating_vote_count) TextView voteCountView;
    @BindView(R.id.progressBar) SmoothProgressBar progressBar;
    @BindView(R.id.movie_details_video_layout_root) LinearLayout videoLayout;
    @BindView(R.id.movie_details_video_preview) ImageView videoPreview;
    @BindView(R.id.movie_details_video_title) TextView videoTitle;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(InventumContextWrapper.wrap(base));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //get the movie id from the intent that started this activity
        Bundle args = getIntent().getExtras();
        mId = args.getString(MOVIE_ID);

        //query the content resolver for the movie id
        //then load data from database or internet according to
        //whether the database has it or not
        mResolver = getContentResolver();
        mCursor = mResolver.query(MovieContract.Movies.CONTENT_URI,
                null,
                MovieContract.Movies.MOVIE_ID + "=?",
                new String[]{String.valueOf(mId)},
                null);
        favResolver = getContentResolver();
        favCursor = favResolver.query(MovieContract.FavMovies.CONTENT_URI,
                null,
                MovieContract.FavMovies.MOVIE_ID + "=?",
                new String []{String.valueOf(mId)},
                null);
        allFavCursor = favResolver.query(MovieContract.FavMovies.CONTENT_URI,
                null,
                null, //get all rows
                null,
                null);
        if (mCursor != null) {
//            if (mCursor.getCount() != 0) {
//                loadFromDb();
//                System.out.println("called here form DB");
//            } else {
                loadFromInternet();
//                System.out.println("called here");
//            }
        }

        if (mCursor != null) {
            mCursor.close();
        }

        if (favCursor != null) {
            favCursor.close();
        }
    }

    //load data from database and assign it to corresponding ui elements
    private void loadFromDb() {
        if (mCursor.moveToNext()) {
            String backdropPath = mCursor.getString(mCursor.getColumnIndex(MovieContract.Movies.MOVIE_BACKDROP_PATH));
            String posterPath = mCursor.getString(mCursor.getColumnIndex(MovieContract.Movies.MOVIE_POSTER_PATH));
            //draw backdrop/poster for the movie
            if (backdropPath != null) {
                Picasso.with(MovieDetailsActivity.this)
                        .load(Constants.TMDB_IMAGE_URL + Constants.BACKDROP_SIZE_W780 + backdropPath)
                        .placeholder(placeholderImage)
                        .fit().centerCrop()
                        .error(placeholderImage)
                        .into(backdrop);
            } else {
                Picasso.with(MovieDetailsActivity.this)
                        .load(Constants.TMDB_IMAGE_URL + Constants.POSTER_SIZE_W500 + posterPath)
                        .placeholder(placeholderImage)
                        .fit().centerCrop()
                        .error(placeholderImage)
                        .into(backdrop);
            }
            Picasso.with(MovieDetailsActivity.this)
                    .load(Constants.TMDB_IMAGE_URL + Constants.POSTER_SIZE_W342 + posterPath)
                    .placeholder(placeholderImage)
                    .fit().centerCrop()
                    .error(placeholderImage)
                    .into(poster);

            String nameStr = mCursor.getString(mCursor.getColumnIndex(MovieContract.Movies.MOVIE_TITLE));
            String originalNameStr = mCursor.getString(mCursor.getColumnIndex(MovieContract.Movies.MOVIE_ORIGINAL_TITLE));
            String releaseDateStr = mCursor.getString(mCursor.getColumnIndex(MovieContract.Movies.MOVIE_RELEASE_DATE));
            String genresStr = mCursor.getString(mCursor.getColumnIndex(MovieContract.Movies.MOVIE_GENRES));
            String countriesStr = mCursor.getString(mCursor.getColumnIndex(MovieContract.Movies.MOVIE_PRODUCTION_COUNTRIES));
            int runtimeInt = mCursor.getInt(mCursor.getColumnIndex(MovieContract.Movies.MOVIE_RUNTIME));
            String taglineStr = mCursor.getString(mCursor.getColumnIndex(MovieContract.Movies.MOVIE_TAGLINE));
            String overviewStr = mCursor.getString(mCursor.getColumnIndex(MovieContract.Movies.MOVIE_OVERVIEW));
            long budgetLong = mCursor.getLong(mCursor.getColumnIndex(MovieContract.Movies.MOVIE_BUDGET));
            long revenueLong = mCursor.getLong(mCursor.getColumnIndex(MovieContract.Movies.MOVIE_REVENUE));
            String statusStr = mCursor.getString(mCursor.getColumnIndex(MovieContract.Movies.MOVIE_STATUS));
            String imdbId = mCursor.getString(mCursor.getColumnIndex(MovieContract.Movies.MOVIE_IMDB_ID));
            String homepageUrl = mCursor.getString(mCursor.getColumnIndex(MovieContract.Movies.MOVIE_HOMEPAGE));
            float voteAverageFlt = mCursor.getFloat(mCursor.getColumnIndex(MovieContract.Movies.MOVIE_VOTE_AVERAGE));
            int voteCountInt = mCursor.getInt(mCursor.getColumnIndex(MovieContract.Movies.MOVIE_VOTE_COUNT));

            setTitle(nameStr);
            name.setText(nameStr);
            originalName.setText(getString(R.string.details_movie_name, originalNameStr, StringUtils.getYear(releaseDateStr)));
            genres.setText(genresStr);
            productionCountries.setText(countriesStr);
            runtime.setText(StringUtils.formatRuntime(this, runtimeInt));
            tagline.setText(getString(R.string.details_tagline, taglineStr));
            overview.setText(overviewStr);
            releaseDate.setText(StringUtils.formatReleaseDate(releaseDateStr));
            status.setText(statusStr);
            imdbPage.setText(String.format(Constants.IMDB_MOVIE_URL, imdbId));
            homepage.setText(homepageUrl);
            budget.setText(getString(R.string.details_budget_revenue, budgetLong));
            revenue.setText(getString(R.string.details_budget_revenue, revenueLong));
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
        String lang = PrefUtils.getFormatLocale(MovieDetailsActivity.this);
        MovieService service = ServiceGenerator.createService(MovieService.class);
        Call<Movie> call = service.movieDetails(String.valueOf(mId), ServiceGenerator.API_KEY, lang);
        //async request to api
        call.enqueue(new Callback<Movie>() {
            @Override
            public void onResponse(Call<Movie> call, Response<Movie> response) {
                if (response.isSuccessful()) {
                    Movie movie = response.body();
                    addOrUpdate(movie);
                    favMovie = movie;

                    //draw backdrop/poster for the movie
                    if (movie.getBackdropPath() != null) {
                        Picasso.with(MovieDetailsActivity.this)
                                .load(Constants.TMDB_IMAGE_URL + Constants.BACKDROP_SIZE_W780 + movie.getBackdropPath())
                                .placeholder(placeholderImage)
                                .fit().centerCrop()
                                .error(placeholderImage)
                                .into(backdrop);
                    } else {
                        Picasso.with(MovieDetailsActivity.this)
                                .load(Constants.TMDB_IMAGE_URL + Constants.POSTER_SIZE_W500 + movie.getPosterPath())
                                .placeholder(placeholderImage)
                                .fit().centerCrop()
                                .error(placeholderImage)
                                .into(backdrop);
                    }
                    Picasso.with(MovieDetailsActivity.this)
                            .load(Constants.TMDB_IMAGE_URL + Constants.POSTER_SIZE_W342 + movie.getPosterPath())
                            .placeholder(placeholderImage)
                            .fit().centerCrop()
                            .error(placeholderImage)
                            .into(poster);
                    //populate ui elements with data
                    setTitle(movie.getTitle());
                    name.setText(movie.getTitle());
                    originalName.setText(getString(R.string.details_movie_name, movie.getOriginalTitle(), StringUtils.getYear(movie.getReleaseDate())));
                    genres.setText(StringUtils.getGenres(movie.getGenres()));
                    productionCountries.setText(StringUtils.getProductCountries(movie.getProductionCountries()));
                    runtime.setText(StringUtils.formatRuntime(MovieDetailsActivity.this, movie.getRuntime()));
                    tagline.setText(getString(R.string.details_tagline, movie.getTagline()));
                    overview.setText(movie.getOverview());
                    releaseDate.setText(StringUtils.formatReleaseDate(movie.getReleaseDate()));
                    status.setText(movie.getStatus());
                    imdbPage.setText(String.format(Constants.IMDB_MOVIE_URL, movie.getImdbId()));
                    homepage.setText(movie.getHomepage());
                    budget.setText(getString(R.string.details_budget_revenue, movie.getBudget()));
                    revenue.setText(getString(R.string.details_budget_revenue, movie.getRevenue()));
                    ratingBar.setRating(movie.getVoteAverage());
                    voteAverageView.setText(String.valueOf(movie.getVoteAverage()));
                    voteCountView.setText(String.valueOf(movie.getVoteCount()));
                    loadVideoPreview();

                    updateProgressBar(false);
                }
            }

            @Override
            public void onFailure(Call<Movie> call, Throwable t) {
                Log.e(TAG, "Error", t);
                updateProgressBar(false);
            }
        });
    }

    private void loadVideoPreview() {
        String lang = PrefUtils.getFormatLocale(MovieDetailsActivity.this);
        MovieService service = ServiceGenerator.createService(MovieService.class);
        Call<VideoResults> call = service.movieVideos(String.valueOf(mId), ServiceGenerator.API_KEY, lang);
        call.enqueue(new Callback<VideoResults>() {
            @Override
            public void onResponse(Call<VideoResults> call, Response<VideoResults> response) {
                if (response.isSuccessful()) {
                    List<Video> videos = response.body().getVideos();
                    if (!videos.isEmpty()) {
                        Picasso.with(MovieDetailsActivity.this)
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

    private void addOrUpdate(Movie movie) {
        ContentValues values = ContentValuesUtils.setMovieValues(movie);
        if (mCursor != null) {
            if (mCursor.getCount() == 0) {
                mResolver.insert(MovieContract.Movies.CONTENT_URI, values);
            } else {
                Uri uri = MovieContract.Movies.buildMovieUri(values.getAsString(MovieContract.Movies.MOVIE_ID));
                mResolver.update(uri, values, null, null);
            }
        }
    }

    private boolean isInFavouritesList(String id) {
        ArrayList<String> favIDs = new ArrayList<>();
        if (allFavCursor != null) {
            while (allFavCursor.moveToNext()) {
                favIDs.add(allFavCursor.getString(allFavCursor.getColumnIndex(MovieContract.FavMovies.MOVIE_ID)));
            }
            return favIDs.contains(id);
        }
        return false;
    }

    private void addMovieToFavourites(Movie movie) {
        ContentValues values = ContentValuesUtils.setFavMovieValues(movie);
        if (favCursor != null) {
            String id = MovieContract.FavMovies.getMovieId(MovieContract.FavMovies.buildMovieUri(mId));
            if (favCursor.getCount() == 0) {
                favResolver.insert(MovieContract.FavMovies.CONTENT_URI, values);
            } else {
                Uri uri = MovieContract.FavMovies.buildMovieUri(values.getAsString(MovieContract.FavMovies.MOVIE_ID));
                favResolver.update(uri, values, null, null);
            }
        }
    }
    private void deleteMovieFromFavourites() {
        String[] args = { mId };
        Uri uri = MovieContract.FavMovies.buildMovieUri(mId);
        mResolver.delete(uri, null, null);
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
        Intent intent = new Intent(context, MovieDetailsActivity.class);
        Bundle args = new Bundle();
        args.putString(MOVIE_ID, id);
        intent.putExtras(args);
        return intent;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);

        //if this movie is already favourited, fill in the icon
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
                        if (favMovie != null) {
                            addMovieToFavourites(favMovie);
                        }
                    } else {
                        favouriteButton.setIcon(android.R.drawable.btn_star_big_off);
                        deleteMovieFromFavourites();
                    }
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.movie_details_video_layout_root)
    void openVideosActivity() {
        Intent intent = VideosActivity.newIntent(MovieDetailsActivity.this, VideosActivity.TYPE_MOVIES, mId);
        startActivity(intent);
    }
}
