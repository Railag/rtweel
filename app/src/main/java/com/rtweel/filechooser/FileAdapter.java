package com.rtweel.filechooser;

import java.util.List;

import com.rtweel.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class FileAdapter extends ArrayAdapter<Line> {
	private Context mContext;
	private int mId;
	private List<Line> mList;

	public FileAdapter(Context context, int textViewId, List<Line> list) {
		super(context, textViewId, list);
		mContext = context;
		mId = textViewId;
		mList = list;
	}

	public Line getItem(int number) {
		return mList.get(number);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (convertView == null) {
			LayoutInflater layoutInflater = LayoutInflater.from(mContext);
			convertView = layoutInflater.inflate(mId, null);
		}

		final Line line = mList.get(position);
		if (line != null) {
			TextView fileName = (TextView) convertView.findViewById(R.id.file_name);
			TextView fileClass = (TextView) convertView.findViewById(R.id.file_class);

			if (fileName != null)
				fileName.setText(line.getName());
			if (fileClass != null)
				fileClass.setText(line.getData());

		}
		return convertView;
	}

}
