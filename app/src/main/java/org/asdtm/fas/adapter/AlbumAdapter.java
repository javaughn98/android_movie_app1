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
import org.asdtm.fas.model.Album;
import org.asdtm.fas.util.Constants;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumHolder> {

    private List<Album> mAlbums;

    public AlbumAdapter(List<Album> albums) {
        mAlbums = albums;
    }

    @Override
    public AlbumHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.item_album, parent, false);

        return new AlbumHolder(v);
    }

    @Override
    public void onBindViewHolder(AlbumHolder holder, int position) {
        Album a = mAlbums.get(position);
        holder.bindAlbum(a, position);
    }

    @Override
    public int getItemCount() {
        return (mAlbums != null) ? mAlbums.size() : 0;
    }

    public class AlbumHolder extends RecyclerView.ViewHolder {

        private Context mContext;
        private Album mAlbum;

        @BindView(R.id.album_position) TextView positionView;
        @BindView(R.id.album_poster) ImageView posterView;
        @BindView(R.id.album_name) TextView nameView;
        @BindView(R.id.album_artist_name) TextView artistNameView;
        //@BindView(R.id.album_duration) TextView durationView;

        public AlbumHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            mContext = itemView.getContext();
        }

        public void bindAlbum(Album a, int position) {
            mAlbum = a;

            positionView.setText(String.valueOf(position + 1));
            Drawable placeholder = ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.background_reel, null);
            Picasso.with(mContext)
                    .load(a.getAlbumImage())
                    .placeholder(placeholder)
                    .fit().centerCrop()
                    .noFade()
                    .into(posterView);
            nameView.setText(a.getAlbumName());
            artistNameView.setText(a.getArtistName());
        }

        //Implement after making a album details activity
//        @OnClick(R.id.track_root)
//        public void startMusicActivity() {
//            Intent intent = AlbumDetailsActivity.newIntent(mContext, mAlbum.getAlbumId());
//            mContext.startActivity(intent);
//        }
    }
}
