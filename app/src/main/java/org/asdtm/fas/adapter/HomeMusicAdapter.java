package org.asdtm.fas.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.asdtm.fas.R;
import org.asdtm.fas.activity.MusicActivity;
import org.asdtm.fas.activity.MusicDetailsActivity;
import org.asdtm.fas.model.Track;
import org.asdtm.fas.util.Constants;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HomeMusicAdapter extends RecyclerView.Adapter<HomeMusicAdapter.MusicHolder> {

    private List<Track> mTracks;

    public HomeMusicAdapter(List<Track> tracks) {
        mTracks = tracks;
    }

    @Override
    public MusicHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.item_home_music, parent, false);

        return new MusicHolder(v);
    }

    @Override
    public void onBindViewHolder(MusicHolder holder, int position) {
        Track track = mTracks.get(position);
        holder.bindMusic(track);
    }

    @Override
    public int getItemCount() {
        return (mTracks != null) ? mTracks.size() : 0;
    }

    public class MusicHolder extends RecyclerView.ViewHolder {

        private Context mContext;
        private Track mTrack;

        @BindView(R.id.home_music_poster)
        ImageView trackPoster;
        @BindView(R.id.home_music_title)
        TextView trackTitle;

        public MusicHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            mContext = itemView.getContext();
        }

        void bindMusic(Track track) {
            mTrack = track;
            trackTitle.setText(track.getSongTitle());
            Drawable placeholder = ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.background_reel, null);
            Picasso.with(mContext)
                    .load(Constants.DEEZER_IMAGE_URL + track.getCoverPath() + Constants.COVER_MEDIUM)
                    .placeholder(placeholder)
                    .fit().centerCrop()
                    .noFade()
                    .into(trackPoster);
        }

        @OnClick(R.id.home_music_root)
        void startMusicDetailActivity() {
            Intent intent = MusicDetailsActivity.newIntent(mContext, mTrack.getSongId());
            mContext.startActivity(intent);
        }
    }
}
