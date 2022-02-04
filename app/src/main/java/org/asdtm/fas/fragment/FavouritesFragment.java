package org.asdtm.fas.fragment;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.asdtm.fas.R;
import org.asdtm.fas.adapter.TrackAdapter;
import org.asdtm.fas.adapter.TvAdapter;
import org.asdtm.fas.model.Genre;
import org.asdtm.fas.model.Network;
import org.asdtm.fas.model.ProductionCountry;
import org.asdtm.fas.model.TV;
import org.asdtm.fas.model.TVResults;
import org.asdtm.fas.model.Track;
import org.asdtm.fas.provider.MovieContract;
import org.asdtm.fas.service.ServiceGenerator;
import org.asdtm.fas.adapter.MoviesAdapter;
import org.asdtm.fas.model.Movie;
import org.asdtm.fas.model.MovieResults;
import org.asdtm.fas.service.DiscoverService;
import org.asdtm.fas.service.MovieService;
import org.asdtm.fas.service.TvService;
import org.asdtm.fas.util.PrefUtils;
import org.asdtm.fas.util.StringUtils;
import org.asdtm.fas.view.CustomErrorView;
import org.asdtm.fas.view.EndlessRecyclerOnScrollListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavouritesFragment extends Fragment {

    private static final String TAG = MoviesFragment.class.getSimpleName();

    private int mType;
    private static final String GET_MOVIES_TYPE = "org.asdtm.fas.movie.get_movies_type";
    public static final int MOVIES = 0;
    public static final int TV_SHOWS = 1;
    public static final int TRACKS = 2;

    //database accessors
    private Cursor movieCursor;
    private Cursor tvCursor;
    private Cursor trackCursor;

    private MoviesAdapter moviesAdapter;
    private List<Movie> removedMovies = new ArrayList<>();
    private List<Movie> mMovies;

    private TvAdapter tvAdapter;
    private List<TV> removedTVs = new ArrayList<>();
    private List<TV> mTVs;

    private TrackAdapter trackAdapter;
    private List<Track> mTracks;

    private int mPage = 1;
    private String mLang;

    private Unbinder unbinder;
    //bind ui elements to resource ids
    @BindView(R.id.recycler_view) RecyclerView recyclerView;
    @BindView(R.id.progressBar) CircularProgressBar progressBar;
    @BindView(R.id.error) CustomErrorView mCustomErrorView;

    //essentially a constructor
    public FavouritesFragment newInstance(int type) {
        FavouritesFragment fragment = new FavouritesFragment();
        Bundle args = new Bundle();
        args.putInt(GET_MOVIES_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mType = getArguments().getInt(GET_MOVIES_TYPE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_recycler_view, container, false);
        unbinder = ButterKnife.bind(this, v);

        mLang = PrefUtils.getFormatLocale(getActivity());

        mMovies = new ArrayList<>();
        moviesAdapter = new MoviesAdapter(mMovies);
        mTVs = new ArrayList<>();
        tvAdapter = new TvAdapter(mTVs);
        mTracks = new ArrayList<>();
        trackAdapter = new TrackAdapter(mTracks);

        setupCursors();

        //set up ui elements
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        if (mType == MOVIES) {
            recyclerView.setAdapter(moviesAdapter);
        } else if (mType == TV_SHOWS) {
            recyclerView.setAdapter(tvAdapter);
        } else if (mType == TRACKS) {
            recyclerView.setAdapter(trackAdapter);
        }
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        //load more movies when scrolled all the way
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page) {
                mPage = page;
                loadMovies();
            }
        });
        updateProgressBar(true);
        loadMovies();

        if (movieCursor != null) {
            movieCursor.close();
        }
        if (tvCursor != null) {
            tvCursor.close();
        }

        if (mMovies.size() == 0 && mType == MOVIES) {
            v = inflater.inflate(R.layout.favourite_movie_empty, container, false);
        }

        if (mTVs.size() == 0 && mType == TV_SHOWS) {
            v = inflater.inflate(R.layout.favourite_tv_empty, container, false);
        }

        if (mTracks.size() == 0 && mType == TRACKS) {
            v = inflater.inflate(R.layout.favourite_tracks_empty, container, false);
        }

        return v;
    }

    private void setupCursors() {
        ContentResolver resolver = getActivity().getContentResolver();
        movieCursor = resolver.query(MovieContract.FavMovies.CONTENT_URI,
                null,
                null, //get all rows
                null,
                null);
        tvCursor = resolver.query(MovieContract.FavTVs.CONTENT_URI,
                null,
                null,
                null,
                null);
        trackCursor = resolver.query(MovieContract.FavMusic.CONTENT_URI,
                null,
                null,
                null,
                null);
    }

    //methods to convert the comma-separated strings returned from the database
    //into list objects
    private List<Genre> genreStringToList(String genres, String mID) {
        List<Genre> genreList = new ArrayList<>();
        String[] genreStr = genres.split(", ");
        for (String s: genreStr) {
            genreList.add(new Genre(mID, s));
        }
        return genreList;
    }

    private List<ProductionCountry> countriesStringToList(String countries) {
        List<ProductionCountry> countryList = new ArrayList<>();
        String[] countriesStr = countries.split(", ");
        for (String s: countriesStr) {
            countryList.add(new ProductionCountry(s));
        }
        return countryList;
    }

    private void loadMoviesFromDB() {
        List<Movie> movies = new ArrayList<>();
        while (movieCursor.moveToNext()) {
            Movie m = new Movie();
            m.setId(movieCursor.getString(movieCursor.getColumnIndex(MovieContract.FavMovies.MOVIE_ID)));
            m.setTitle(movieCursor.getString(movieCursor.getColumnIndex(MovieContract.FavMovies.MOVIE_TITLE)));
            System.out.println(m.getTitle());
            m.setOriginalTitle(movieCursor.getString(movieCursor.getColumnIndex(MovieContract.FavMovies.MOVIE_ORIGINAL_TITLE)));
            m.setReleaseDate(movieCursor.getString(movieCursor.getColumnIndex(MovieContract.FavMovies.MOVIE_RELEASE_DATE)));
            m.setGenres(genreStringToList(movieCursor.getString(movieCursor.getColumnIndex(MovieContract.FavMovies.MOVIE_GENRES)), m.getId()));
            m.setProductionCountries(countriesStringToList(movieCursor.getString(movieCursor.getColumnIndex(MovieContract.FavMovies.MOVIE_PRODUCTION_COUNTRIES))));
            m.setRuntime(Integer.parseInt(movieCursor.getString(movieCursor.getColumnIndex(MovieContract.FavMovies.MOVIE_RUNTIME))));
            m.setTagline(movieCursor.getString(movieCursor.getColumnIndex(MovieContract.FavMovies.MOVIE_TAGLINE)));
            m.setOverview(movieCursor.getString(movieCursor.getColumnIndex(MovieContract.FavMovies.MOVIE_OVERVIEW)));
            m.setBudget(movieCursor.getLong(movieCursor.getColumnIndex(MovieContract.FavMovies.MOVIE_BUDGET)));
            m.setRevenue(movieCursor.getLong(movieCursor.getColumnIndex(MovieContract.FavMovies.MOVIE_REVENUE)));
            m.setStatus(movieCursor.getString(movieCursor.getColumnIndex(MovieContract.FavMovies.MOVIE_STATUS)));
            m.setImdbId(movieCursor.getString(movieCursor.getColumnIndex(MovieContract.FavMovies.MOVIE_IMDB_ID)));
            m.setHomepage(movieCursor.getString(movieCursor.getColumnIndex(MovieContract.FavMovies.MOVIE_HOMEPAGE)));
            m.setVoteAverage(movieCursor.getFloat(movieCursor.getColumnIndex(MovieContract.FavMovies.MOVIE_VOTE_AVERAGE)));
            m.setVoteCount(movieCursor.getInt(movieCursor.getColumnIndex(MovieContract.FavMovies.MOVIE_VOTE_COUNT)));
            m.setBackdropPath(movieCursor.getString(movieCursor.getColumnIndex(MovieContract.FavMovies.MOVIE_BACKDROP_PATH)));
            m.setPosterPath(movieCursor.getString(movieCursor.getColumnIndex(MovieContract.FavMovies.MOVIE_POSTER_PATH)));
            m.setPopularity(movieCursor.getInt(movieCursor.getColumnIndex(MovieContract.FavMovies.MOVIE_POPULARITY)));
            movies.add(m);
        }
        addMovies(movies);
    }

    private List<Network> networkStringToList(String networks, String id) {
        List<Network> networkList = new ArrayList<>();
        String[] networksStr = networks.split(", ");
        for (String s: networksStr) {
            networkList.add(new Network(s, id));
        }
        return networkList;
    }

    private void loadTVFromDB() {
        List<TV> shows = new ArrayList<>();
        while (tvCursor.moveToNext()) {
            TV show = new TV();
            show.setId(tvCursor.getString(tvCursor.getColumnIndex(MovieContract.FavTVs.TV_ID)));
            show.setName(tvCursor.getString(tvCursor.getColumnIndex(MovieContract.FavTVs.TV_NAME)));
            show.setOriginalName(tvCursor.getString(tvCursor.getColumnIndex(MovieContract.FavTVs.TV_ORIGINAL_NAME)));
            show.setFirstAirDate(tvCursor.getString(tvCursor.getColumnIndex(MovieContract.FavTVs.TV_FIRST_AIR_DATE)));
            show.setLastAirDate(tvCursor.getString(tvCursor.getColumnIndex(MovieContract.FavTVs.TV_LAST_AIR_DATE)));
            show.setGenres(genreStringToList(tvCursor.getString(tvCursor.getColumnIndex(MovieContract.FavTVs.TV_GENRES)), show.getId()));
            //field types arent consistent with how they are implemented in Movie
            //changing the field types in TV to be consistent breaks TVActivity
            //so this is fine for now
            List<String> countries = new ArrayList<>();
            Collections.addAll(countries, tvCursor.getString(tvCursor.getColumnIndex(MovieContract.FavTVs.TV_ORIGINAL_COUNTRY)).split(", "));
            show.setOriginCountry(countries);
            List<Integer> runtime = new ArrayList<>();
            runtime.add(tvCursor.getInt(tvCursor.getColumnIndex(MovieContract.FavTVs.TV_EPISODE_RUNTIME)));
            show.setEpisodeRunTime(runtime);
            show.setOverview(tvCursor.getString(tvCursor.getColumnIndex(MovieContract.FavTVs.TV_OVERVIEW)));
            show.setInProduction(tvCursor.getInt(tvCursor.getColumnIndex(MovieContract.FavTVs.TV_IN_PRODUCTION)) > 0);
            show.setStatus(tvCursor.getString(tvCursor.getColumnIndex(MovieContract.FavTVs.TV_STATUS)));
            show.setNetworks(networkStringToList(tvCursor.getString(tvCursor.getColumnIndex(MovieContract.FavTVs.TV_NETWORKS)), show.getId()));
            show.setHomepage(tvCursor.getString(tvCursor.getColumnIndex(MovieContract.FavTVs.TV_HOMEPAGE)));
            show.setVoteAverage(tvCursor.getFloat(tvCursor.getColumnIndex(MovieContract.FavTVs.TV_VOTE_AVERAGE)));
            show.setVoteCount(tvCursor.getInt(tvCursor.getColumnIndex(MovieContract.FavTVs.TV_VOTE_COUNT)));
            show.setBackdropPath(tvCursor.getString(tvCursor.getColumnIndex(MovieContract.FavTVs.TV_BACKDROP_PATH)));
            show.setPosterPath(tvCursor.getString(tvCursor.getColumnIndex(MovieContract.FavTVs.TV_POSTER_PATH)));
            show.setPopularity(tvCursor.getInt(tvCursor.getColumnIndex(MovieContract.FavTVs.TV_POPULARITY)));

            shows.add(show);
        }
        addTv(shows);
    }

    private void loadTracksFromDB() {
        List<Track> tracks = new ArrayList<>();
        while (trackCursor.moveToNext()) {
            Track track = new Track();
            track.setSongId(trackCursor.getString(trackCursor.getColumnIndex(MovieContract.FavMusic.MUSIC_ID)));
            track.setSongTitle(trackCursor.getString(trackCursor.getColumnIndex(MovieContract.FavMusic.MUSIC_NAME)));
            track.setDuration(trackCursor.getInt(trackCursor.getColumnIndex(MovieContract.FavMusic.MUSIC_DUR)));
            track.setCoverPath(trackCursor.getString(trackCursor.getColumnIndex(MovieContract.FavMusic.MUSIC_POSTER_PATH)));
            tracks.add(track);
        }
        addTrack(tracks);
    }

    private void loadMovies() {
        switch (mType) {
            case MOVIES:
                loadMoviesFromDB();
                break;
            case TV_SHOWS:
                loadTVFromDB();
                break;
            case TRACKS:
                loadTracksFromDB();
                break;
        }
    }

    //add movies returned from api to a local array
    private void addMovies(List<Movie> movies) {
        if (movies != null) {
            mMovies.addAll(movies);
            moviesAdapter.notifyDataSetChanged();
        }
        updateProgressBar(false);
    }

    private void addTrack(List<Track> tracks) {
        if (tracks != null) {
            mTracks.addAll(tracks);
            trackAdapter.notifyDataSetChanged();
        }
        updateProgressBar(false);
    }

    public void sortMovieList(String category) throws ParseException {
        if (mMovies.size() == 0 || mMovies.size() == 1) {
            return;
        }
        switch (category) {
            case "Popularity":
                if (removedMovies.size() != 0) {
                    mMovies.addAll(removedMovies);
                    moviesAdapter.notifyDataSetChanged();
                    removedMovies.clear();
                }
                for (int i = 0; i < (mMovies.size() - 1); i++) {
                    for (int j = i + 1; j < mMovies.size(); j++) {
                        if (mMovies.get(i).getPopularity() < mMovies.get(j).getPopularity()) {
                            Movie temp = mMovies.get(i);
                            mMovies.set(i, mMovies.get(j));
                            mMovies.set(j, temp);
                        }
                    }
                }
                moviesAdapter.notifyDataSetChanged();

                break;

            case "Rating":
                if (removedMovies.size() != 0) {
                    mMovies.addAll(removedMovies);
                    moviesAdapter.notifyDataSetChanged();
                    removedMovies.clear();
                }
                for (int i = 0; i < (mMovies.size() - 1); i++) {
                    for (int j = i + 1; j < mMovies.size(); j++) {
                        if (mMovies.get(i).getVoteAverage() < mMovies.get(j).getVoteAverage()) {
                            Movie temp = mMovies.get(i);
                            mMovies.set(i, mMovies.get(j));
                            mMovies.set(j, temp);
                        }
                    }
                }
                moviesAdapter.notifyDataSetChanged();
                break;

            case "Now Playing":
                if (removedMovies.size() != 0) {
                    mMovies.addAll(removedMovies);
                    moviesAdapter.notifyDataSetChanged();
                    removedMovies.clear();
                }
                List<Movie> removed = new ArrayList<>();
                String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                System.out.println(date);
                for (int i = 0; i < mMovies.size(); i++) {
                    System.out.println("date = " + date);
                    System.out.println("Release date = " + mMovies.get(i).getReleaseDate());
                    if (date.compareTo(mMovies.get(i).getReleaseDate()) < 0) {
                        System.out.println(mMovies.get(i).getTitle());
                        removed.add(mMovies.remove(i));
                        i--;
                    }
                }

                for (int i = 0; i < (mMovies.size() - 1); i++) {
                    for (int j = i + 1; j < mMovies.size(); j++) {
                        if (mMovies.get(i).getReleaseDate().compareTo(mMovies.get(j).getReleaseDate()) < 0) {
                            Movie temp2 = mMovies.get(i);
                            mMovies.set(i, mMovies.get(j));
                            mMovies.set(j, temp2);
                        }
                    }
                }
                moviesAdapter.notifyDataSetChanged();
                removedMovies = removed;
                break;

            case "Upcoming":
                if (removedMovies.size() != 0) {
                    mMovies.addAll(removedMovies);
                    moviesAdapter.notifyDataSetChanged();
                    removedMovies.clear();
                }
                List<Movie> removed1 = new ArrayList<>();
                String date1 = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

                System.out.println(date1);
                for (int i = 0; i < mMovies.size(); i++) {
                    if (date1.compareTo(mMovies.get(i).getReleaseDate()) > 0) {
                        System.out.println("yes yes");
                        System.out.println(mMovies.get(i).getTitle());
                        removed1.add(mMovies.remove(i));
                        i--;
                    }
                }

                for (int i = 0; i < (mMovies.size() - 1); i++) {
                    for (int j = i + 1; j < mMovies.size(); j++) {
                        if (mMovies.get(i).getReleaseDate().compareTo(mMovies.get(j).getReleaseDate()) > 0) {
                            Movie temp2 = mMovies.get(i);
                            mMovies.set(i, mMovies.get(j));
                            mMovies.set(j, temp2);
                        }
                    }
                }
                moviesAdapter.notifyDataSetChanged();
                removedMovies = removed1;
                break;

            case "AtoZ":
                System.out.println("");
                if (removedMovies.size() != 0) {
                    mMovies.addAll(removedMovies);
                    moviesAdapter.notifyDataSetChanged();
                    removedMovies.clear();
                }
                for (int i = 0; i < (mMovies.size() - 1); i++) {
                    for (int j = i + 1; j < mMovies.size(); j++) {
                        if (mMovies.get(i).getTitle().compareTo(mMovies.get(j).getTitle()) > 0) {
                            Movie temp = mMovies.get(i);
                            mMovies.set(i, mMovies.get(j));
                            mMovies.set(j, temp);
                        }
                    }
                }
                moviesAdapter.notifyDataSetChanged();
                break;


        }
    }


    public void sortTVs(String category) {
        if (mTVs.size() == 0 || mTVs.size() == 1) {
            return;
        }

        switch (category) {
            case "Popularity":
                if(removedTVs.size() != 0) {
                    mTVs.addAll(removedTVs);
                    tvAdapter.notifyDataSetChanged();
                    removedTVs.clear();
                }
                for(int i = 0; i < (mTVs.size() - 1); i++) {
                    for (int j = i + 1; j < mTVs.size(); j++) {
                        if(mTVs.get(i).getPopularity() < mTVs.get(j).getPopularity()) {
                            TV temp = mTVs.get(i);
                            mTVs.set(i, mTVs.get(j));
                            mTVs.set(j, temp);
                        }
                    }
                }
                tvAdapter.notifyDataSetChanged();
                break;

            case "Rating":
                if(removedTVs.size() != 0) {
                    mTVs.addAll(removedTVs);
                    tvAdapter.notifyDataSetChanged();
                    removedTVs.clear();
                }
                for(int i = 0; i < (mTVs.size() - 1); i++) {
                    for (int j = i + 1; j < mTVs.size(); j++) {
                        if (mTVs.get(i).getVoteAverage() < mTVs.get(j).getVoteAverage()) {
                            TV temp = mTVs.get(i);
                            mTVs.set(i, mTVs.get(j));
                            mTVs.set(j, temp);
                        }
                    }
                }
                tvAdapter.notifyDataSetChanged();
                break;

            case "AtoZ":
                if(removedTVs.size() != 0) {
                    mTVs.addAll(removedTVs);
                    tvAdapter.notifyDataSetChanged();
                    removedTVs.clear();
                }
                for(int i = 0; i < (mTVs.size() - 1); i++) {
                    for (int j = i + 1; j < mTVs.size(); j++) {
                        if (mTVs.get(i).getName().compareTo(mTVs.get(j).getName()) > 0) {
                            TV temp = mTVs.get(i);
                            mTVs.set(i, mTVs.get(j));
                            mTVs.set(j, temp);
                        }
                    }
                }
                tvAdapter.notifyDataSetChanged();
                break;

            case "Now Playing":
                if(removedTVs.size() != 0) {
                    mTVs.addAll(removedTVs);
                    tvAdapter.notifyDataSetChanged();
                    removedTVs.clear();
                }
                List<TV> removed = new ArrayList<>();
                for(int i = 0; i < mTVs.size(); i++) {
                    if (!mTVs.get(i).isInProduction()) {
                        removed.add(mTVs.remove(i));
                        i--;
                    }
                }
                tvAdapter.notifyDataSetChanged();
                removedTVs = removed;
                break;

            case "Airing Today":
                if(removedTVs.size() != 0) {
                    mTVs.addAll(removedTVs);
                    tvAdapter.notifyDataSetChanged();
                    removedTVs.clear();
                }
                String date = "2021-02-01";
                String date1 = "2021-04-30";


                List<TV> removed1 = new ArrayList<>();
                for(int i = 0; i < mTVs.size(); i++) {
                    if(date.compareTo(mTVs.get(i).getLastAirDate()) > 0 || date1.compareTo(mTVs.get(i).getLastAirDate()) < 0) {
                        removed1.add(mTVs.remove(i));
                        i--;
                    }
                }
                tvAdapter.notifyDataSetChanged();
                removedTVs = removed1;
                break;
        }
    }

    private void addTv(List<TV> tvs) {
        if (tvs != null) {
            mTVs.addAll(tvs);
            tvAdapter.notifyDataSetChanged();
        }
        updateProgressBar(false);
    }

    //reload movies/tvs and update adapters when coming back to favourites menu from a movie/show
    //this makes it so if you unfavourite something from the favourites list, it disappears
    @Override
    public void onResume() {
        setupCursors();
        mMovies.clear();
        mTVs.clear();
        mTracks.clear();
        loadMovies();
        moviesAdapter.notifyDataSetChanged();
        tvAdapter.notifyDataSetChanged();
        trackAdapter.notifyDataSetChanged();
        super.onResume();
    }

    private void updateProgressBar(boolean visibility) {
        if (progressBar != null) {
            progressBar.setVisibility(visibility ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
