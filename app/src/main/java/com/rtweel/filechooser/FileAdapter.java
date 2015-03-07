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
		View v = convertView;
		if (v == null) {
			
			LayoutInflater vi = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(mId, null);
		}
		final Line o = mList.get(position);
		if (o != null) {
			TextView t1 = (TextView) v.findViewById(R.id.TextView01);
			TextView t2 = (TextView) v.findViewById(R.id.TextView02);

			if (t1 != null)
				t1.setText(o.getName());
			if (t2 != null)
				t2.setText(o.getData());

		}
		return v;
	}

}
