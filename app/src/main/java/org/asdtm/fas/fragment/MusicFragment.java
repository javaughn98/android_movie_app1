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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.asdtm.fas.R;
import org.asdtm.fas.adapter.AlbumAdapter;
import org.asdtm.fas.adapter.TrackAdapter;
import org.asdtm.fas.model.Album;
import org.asdtm.fas.model.Track;
import org.asdtm.fas.util.PrefUtils;
import org.asdtm.fas.util.StringUtils;
import org.asdtm.fas.view.CustomErrorView;
import org.asdtm.fas.view.EndlessRecyclerOnScrollListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MusicFragment extends Fragment {
    private int mType;
    private static final String GET_MUSIC_TYPE = "org.asdtm.fas.movie.get_music_type";
    public static final int TOP_TRACKS = 0;
    public static final int TOP_ALBUMS = 1;

    private int mPage = 1;
    private String mLang;
    private List<Track> mTracks;
    private List<Album> mAlbums;
    private TrackAdapter trackAdapter;
    private AlbumAdapter albumAdapter;

    private Unbinder unbinder;
    @BindView(R.id.recycler_view) RecyclerView recyclerView;
    @BindView(R.id.progressBar) CircularProgressBar progressBar;
    @BindView(R.id.error) CustomErrorView mCustomErrorView;

    public MusicFragment newInstance(int type) {
        MusicFragment fragment = new MusicFragment();
        Bundle args = new Bundle();
        args.putInt(GET_MUSIC_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mType = getArguments().getInt(GET_MUSIC_TYPE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_recycler_view, container, false);
        unbinder = ButterKnife.bind(this, v);

        mLang = PrefUtils.getFormatLocale(getActivity());
        mTracks = new ArrayList<>();
        mAlbums = new ArrayList<>();

        trackAdapter = new TrackAdapter(mTracks);
        albumAdapter = new AlbumAdapter(mAlbums);

        if (mType == TOP_TRACKS) {
            recyclerView.setAdapter(trackAdapter);
        } else if (mType == TOP_ALBUMS) {
            recyclerView.setAdapter(albumAdapter);
        }

        //set up ui elements
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        //load more movies when scrolled all the way
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page) {
                mPage = page;
                loadMusic();
            }
        });
        updateProgressBar(true);
        loadMusic();

        return v;
    }

    private void loadMusic() {
        switch (mType) {
            case TOP_TRACKS:
                loadTopTracks();
                break;
            case TOP_ALBUMS:
                loadTopAlbums();
                break;
        }
    }

    // This is where the data will be loaded into the fragment
    private void loadTopTracks() {
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        String url = "https://api.deezer.com/chart/0/tracks?limit=100";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                JSONArray data = null;
                try {
                    data = response.getJSONArray("data");
                    List<Track> trackList = setTracksFromJSON(data);
                    addTracks(trackList);
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
    }

    // This is where the data will be loaded into the fragment
    private void loadTopAlbums() {

        RequestQueue queue = Volley.newRequestQueue(getActivity());
        String url = "https://api.deezer.com/chart/0/albums?limit=100";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                JSONArray data = null;
                try {
                    data = response.getJSONArray("data");
                    List<Album> albumList = setAlbumsFromJSON(data);
                    addAlbums(albumList);
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

    }

    private List<Album> setAlbumsFromJSON(JSONArray data) throws JSONException {
        if (data != null) {
            List<Album> albumList = new ArrayList<Album>();
            for (int i = 0; i < data.length(); i++) {
                Album album = new Album();
                JSONObject album_json = data.getJSONObject(i);
                album.setAlbumId(String.valueOf(album_json.getInt("id")));
                album.setAlbumName(album_json.getString("title"));
                album.setAlbumImage(album_json.getString("cover"));
                album.setArtistName(album_json.getJSONObject("artist").getString("name"));
                albumList.add(album);
            }
            return albumList;
        }
        return null;
    }

    private List<Track> setTracksFromJSON(JSONArray data) throws JSONException {
        if (data != null) {
            List<Track> trackList = new ArrayList<Track>();
            for (int i = 0; i < data.length(); i++) {
                Track track = new Track();
                JSONObject track_json = data.getJSONObject(i);
                track.setSongId(String.valueOf(track_json.getInt("id")));
                track.setSongTitle(track_json.getString("title"));
                track.setDuration(track_json.getInt("duration"));
                track.setExplicit(track_json.getBoolean("explicit_lyrics"));
                track.setCoverPath(track_json.getString("md5_image"));
                trackList.add(track);
            }
            return trackList;
        }
        return null;
    }

    private void addTracks(List<Track> trackList) {
        if (trackList != null) {
            mTracks.addAll(trackList);
            //commented out because MusicAdapter not created yet
            trackAdapter.notifyDataSetChanged();
        }
        updateProgressBar(false);
    }

    private void addAlbums(List<Album> albumList) {
        if (albumList != null) {
            mAlbums.addAll(albumList);
            //commented out because MusicAdapter not created yet
            albumAdapter.notifyDataSetChanged();
        }
        updateProgressBar(false);
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
