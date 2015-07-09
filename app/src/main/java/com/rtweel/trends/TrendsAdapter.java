package com.rtweel.trends;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rtweel.MainActivity;
import com.rtweel.R;
import com.rtweel.tag.TagFragment;

import java.util.List;

import twitter4j.Trend;

/**
 * Created by firrael on 9.7.15.
 */
public class TrendsAdapter extends RecyclerView.Adapter<TrendsAdapter.ViewHolder> {

    private final List<Trend> mTrends;
    private final Context mContext;

    public TrendsAdapter(List<Trend> data, Context context) {
        mTrends = data;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout v = (LinearLayout) LayoutInflater.from(mContext)
                .inflate(R.layout.trend_item, parent, false);

        TextView body = (TextView) v.findViewById(R.id.trend_item_body);

        return new ViewHolder(v, body);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Trend trend = mTrends.get(position);

        holder.getBodyView().setText(trend.getName());

    }

    @Override
    public int getItemCount() {
        return mTrends.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView mBodyView;

        public ViewHolder(final View main, TextView body) {
            super(main);
            main.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getPosition();

                    RecyclerView rv = (RecyclerView) main.getParent();
                    TrendsAdapter adapter = (TrendsAdapter) rv.getAdapter();

                    Trend trend = adapter.mTrends.get(position);

                    Bundle args = new Bundle();
                    args.putString(TagFragment.QUERY, trend.getQuery());

                    TagFragment fragment = new TagFragment();
                    fragment.setArguments(args);
                    ((MainActivity) adapter.mContext).setMainFragment(fragment);

                }
            });

            this.mBodyView = body;
        }

        public TextView getBodyView() {
            return mBodyView;
        }
    }
}
