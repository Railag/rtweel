package com.rtweel;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by firrael on 12.6.15.
 */
public class NavAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<NavItem> items;

    public NavAdapter() {
        items = new ArrayList<>();
    }

    public NavAdapter(Context context, ArrayList<NavItem> items) {
        mContext = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.drawer_item, parent, false);
            TextView text = (TextView) convertView.findViewById(R.id.drawer_text);
            ImageView icon = (ImageView) convertView.findViewById(R.id.drawer_icon);
            ViewHolder vh = new ViewHolder(text, icon);
            convertView.setTag(vh);
        }

        ViewHolder vh = (ViewHolder) convertView.getTag();

        NavItem item = items.get(position);

        vh.text.setText(item.text);
        vh.icon.setImageDrawable(item.icon);

        return convertView;
    }

    private static class ViewHolder {
        public final TextView text;
        public final ImageView icon;

        public ViewHolder(TextView text, ImageView icon) {
            this.text = text;
            this.icon = icon;
        }
    }
}
