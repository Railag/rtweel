package com.rtweel.filechooser;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.rtweel.R;

import java.util.List;

public class FileAdapter extends BaseAdapter {
    private Context mContext;
    private List<FileItem> mList;

    public FileAdapter(Context context, List<FileItem> list) {
        mContext = context;
        mList = list;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    public FileItem getItem(int number) {
        return mList.get(number);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final FileItem item = mList.get(position);

        if (convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            convertView = layoutInflater.inflate(R.layout.file_item, parent, false);

            TextView name = (TextView) convertView.findViewById(R.id.file_item_name);
            TextView details = (TextView) convertView.findViewById(R.id.file_item_details);

            ViewHolder vh = new ViewHolder(name, details);
            convertView.setTag(vh);
        }

        ViewHolder vh = (ViewHolder) convertView.getTag();

        vh.getNameView().setText(item.getName());
        vh.getDetailsView().setText(item.getDetails());

        return convertView;
    }


    static class ViewHolder {
        private TextView name;
        private TextView details;

        ViewHolder(TextView name, TextView details) {
            this.name = name;
            this.details = details;
        }

        public TextView getNameView() {
            return name;
        }

        public TextView getDetailsView() {
            return details;
        }
    }

}
