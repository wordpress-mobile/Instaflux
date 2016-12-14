package org.wordpress.android.fluxc.instaflux;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.wordpress.android.fluxc.model.PostModel;

import java.util.ArrayList;
import java.util.List;

class PostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<PostModel> postList;
    private Context mContext;

    PostAdapter(Context context, List<PostModel> postList) {
        this.postList = new ArrayList<>();
        for (PostModel postModel : postList) {
            if (ImageUtils.hasImageInContent(postModel.getContent())) {
                this.postList.add(postModel);
            }
        }
        this.mContext = context;
    }

    @Override
    public PostViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.post_list_row, null);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        PostViewHolder postViewHolder = (PostViewHolder) holder;
        PostModel postModel = postList.get(position);

        postViewHolder.titleTextView.setText(postModel.getTitle());
        String imageUrl = ImageUtils.getLargestImage(postModel.getContent());
        if (imageUrl != null) {
            Picasso.with(mContext).load(imageUrl).into(postViewHolder.imageView);
        }
    }

    @Override
    public int getItemCount() {
        return (postList != null ? postList.size() : 0);
    }

    private class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleTextView;

        PostViewHolder(View view) {
            super(view);
            this.imageView = (ImageView) view.findViewById(R.id.image_view);
            this.titleTextView = (TextView) view.findViewById(R.id.title);
        }
    }
}
