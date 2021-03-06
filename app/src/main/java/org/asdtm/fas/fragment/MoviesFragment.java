package org.asdtm.fas.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.asdtm.fas.R;
import org.asdtm.fas.service.ServiceGenerator;
import org.asdtm.fas.adapter.MoviesAdapter;
import org.asdtm.fas.model.Movie;
import org.asdtm.fas.model.MovieResults;
import org.asdtm.fas.service.DiscoverService;
import org.asdtm.fas.service.MovieService;
import org.asdtm.fas.util.PrefUtils;
import org.asdtm.fas.util.StringUtils;
import org.asdtm.fas.view.CustomErrorView;
import org.asdtm.fas.view.EndlessRecyclerOnScrollListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MoviesFragment extends Fragment {

    private static final String TAG = MoviesFragment.class.getSimpleName();

    private int mType;
    private static final String GET_MOVIES_TYPE = "org.asdtm.fas.movie.get_movies_type";
    public static final int POPULAR = 0;
    public static final int TOP_RATED = 1;
    public static final int UPCOMING = 2;
    public static final int NOW_PLAYING = 3;

    private MoviesAdapter mAdapter;
    private List<Movie> mMovies;
    private int mPage = 1;
    private String mLang;

    private Unbinder unbinder;
    //bind ui elements to resource ids
    @BindView(R.id.recycler_view) RecyclerView recyclerView;
    @BindView(R.id.progressBar) CircularProgressBar progressBar;
    @BindView(R.id.error) CustomErrorView mCustomErrorView;

    //essentially a constructor
    public MoviesFragment newInstance(int type) {
        MoviesFragment fragment = new MoviesFragment();
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
        mAdapter = new MoviesAdapter(mMovies);

        //set up ui elements
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setAdapter(mAdapter);
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

        return v;
    }

    private void loadMovies() {
        switch (mType) {
            case POPULAR:
                loadPopular();
                break;
            case TOP_RATED:
                loadTopRated();
                break;
            case UPCOMING:
                loadUpcoming();
                break;
            case NOW_PLAYING:
                loadNowPlaying();
                break;
        }
    }

    //query api for popular movies, then update data accordingly
    private void loadPopular() {
        MovieService popularService = ServiceGenerator.createService(MovieService.class);
        Call<MovieResults> popularCall = popularService.popular(ServiceGenerator.API_KEY, mLang, mPage);
        popularCall.enqueue(new Callback<MovieResults>() {
            @Override
            public void onResponse(Call<MovieResults> call, Response<MovieResults> response) {
                if (response.isSuccessful()) {
                    List<Movie> movies = response.body().getMovies();
                    addMovies(movies);
                } else {
                    Log.i("TAG", "Res: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<MovieResults> call, Throwable t) {
                onLoadFailed(t);
            }
        });
    }

    //query api for top rated movies
    private void loadTopRated() {
        MovieService topRatedService = ServiceGenerator.createService(MovieService.class);
        Call<MovieResults> topRatedCall = topRatedService.topRated(ServiceGenerator.API_KEY, mLang, mPage);
        topRatedCall.enqueue(new Callback<MovieResults>() {
            @Override
            public void onResponse(Call<MovieResults> call, Response<MovieResults> response) {
                if (response.isSuccessful()) {
                    List<Movie> movies = response.body().getMovies();
                    addMovies(movies);
                } else {
                    Log.i("TAG", "Res: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<MovieResults> call, Throwable t) {
                onLoadFailed(t);
            }
        });
    }

    //query api for upcoming movies
    private void loadUpcoming() {
        MovieService upcomingService = ServiceGenerator.createService(MovieService.class);
        Call<MovieResults> upcomingCall = upcomingService.upcoming(ServiceGenerator.API_KEY, mLang, mPage);
        upcomingCall.enqueue(new Callback<MovieResults>() {
            @Override
            public void onResponse(Call<MovieResults> call, Response<MovieResults> response) {
                if (response.isSuccessful()) {
                    List<Movie> movies = response.body().getMovies();
                    addMovies(movies);
                } else {
                    Log.i("TAG", "Res: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<MovieResults> call, Throwable t) {
                onLoadFailed(t);
            }
        });
    }

    //query api for movies currently playing, sorted in descending order of popularity
    private void loadNowPlaying() {
        DiscoverService nowPlayingService = ServiceGenerator.createService(DiscoverService.class);
        Call<MovieResults> nowPlayingCall = nowPlayingService.inTheaters(ServiceGenerator.API_KEY, mLang, mPage, "popularity.desc", StringUtils.inTheatersLte(), StringUtils.inTheatersGte());
        nowPlayingCall.enqueue(new Callback<MovieResults>() {
            @Override
            public void onResponse(Call<MovieResults> call, Response<MovieResults> response) {
                List<Movie> movies = response.body().getMovies();
                addMovies(movies);
            }

            @Override
            public void onFailure(Call<MovieResults> call, Throwable t) {
                onLoadFailed(t);
            }
        });
    }

    //add movies returned from api to a local array
    private void addMovies(List<Movie> movies) {
        if (movies != null) {
            mMovies.addAll(movies);
            mAdapter.notifyDataSetChanged();
        }
        updateProgressBar(false);
    }

    private void updateProgressBar(boolean visibility) {
        if (progressBar != null) {
            progressBar.setVisibility(visibility ? View.VISIBLE : View.GONE);
        }
    }

    private void onLoadFailed(Throwable t) {
        mCustomErrorView.setError(t);
        mCustomErrorView.setVisibility(View.VISIBLE);
        updateProgressBar(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
