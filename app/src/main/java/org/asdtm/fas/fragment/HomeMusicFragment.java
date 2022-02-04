package org.asdtm.fas.fragment;

import android.content.Context;
import android.os.Bundle;
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
import org.asdtm.fas.model.Track;
import org.asdtm.fas.adapter.HomeMusicAdapter;
import org.asdtm.fas.util.PrefUtils;
import org.asdtm.fas.util.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import fr.castorflex.android.circularprogressbar.CircularProgressBar;

public class HomeMusicFragment extends Fragment {

    private static final String TAG = HomeMusicFragment.class.getSimpleName();

    @BindView(R.id.home_recycler_view) RecyclerView mRecyclerView;
    @BindView(R.id.progressBar) CircularProgressBar progressBar;

    private HomeMusicAdapter mAdapter;
    private List<Track> mTracks;
    private int mPage = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.home_recycler_view, container, false);
        ButterKnife.bind(this, v);

        String lang = PrefUtils.getFormatLocale(getActivity());

        mTracks = new ArrayList<>();
        mAdapter = new HomeMusicAdapter(mTracks);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        mRecyclerView.setAdapter(mAdapter);

        updateProgressBar(true);
        loadTopTracks();

        return v;
    }

    // This is where the data will be loaded into the fragment
    private void loadTopTracks() {
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        String url = "https://api.deezer.com/chart/0/tracks?limit=10";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                JSONArray data = null;
                try {
                    data = response.getJSONArray("data");
                    List<Track> trackList = setTracksFromJSON(data);
                    addTracks(trackList);
                    for (int i = 0; i < trackList.size(); i++) {
                        Log.i("Music Fragment TRACKS", trackList.get(i).getSongTitle());
                    }
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
            mAdapter.notifyDataSetChanged();
        }
        updateProgressBar(false);
    }


    private void updateProgressBar(boolean visibility) {
        progressBar.setVisibility(visibility ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }
}
