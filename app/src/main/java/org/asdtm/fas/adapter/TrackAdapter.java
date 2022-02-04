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

public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.TrackHolder> {

    private List<Track> mTracks;

    public TrackAdapter(List<Track> tracks) {
        mTracks = tracks;
    }

    @Override
    public TrackHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.item_track, parent, false);

        return new TrackHolder(v);
    }

    @Override
    public void onBindViewHolder(TrackHolder holder, int position) {
        Track t = mTracks.get(position);
        holder.bindTrack(t, position);
    }

    @Override
    public int getItemCount() {
        return (mTracks != null) ? mTracks.size() : 0;
    }

    public class TrackHolder extends RecyclerView.ViewHolder {

        private Context mContext;
        private Track mTrack;

        @BindView(R.id.track_position) TextView positionView;
        @BindView(R.id.track_poster) ImageView posterView;
        @BindView(R.id.track_name) TextView nameView;
        //@BindView(R.id.track_original_name) TextView originalNameView;
        @BindView(R.id.track_duration) TextView durationView;

        public TrackHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            mContext = itemView.getContext();
        }

        public void bindTrack(Track t, int position) {
            mTrack = t;

            positionView.setText(String.valueOf(position + 1));
            Drawable placeholder = ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.background_reel, null);
            Picasso.with(mContext)
                    .load(Constants.DEEZER_IMAGE_URL + t.getCoverPath() + Constants.COVER_MEDIUM)
                    .placeholder(placeholder)
                    .fit().centerCrop()
                    .noFade()
                    .into(posterView);
            nameView.setText(t.getSongTitle());
            durationView.setText(mTrack.durationToString());
        }

        @OnClick(R.id.track_root)
        public void startMusicActivity() {
            Intent intent = MusicDetailsActivity.newIntent(mContext, mTrack.getSongId());
            mContext.startActivity(intent);
        }
    }
}
