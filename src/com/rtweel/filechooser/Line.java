package com.rtweel.filechooser;

import java.util.Locale;

public class Line implements Comparable<Line> {
	private String mName;
	private String mData;
	private String mPath;

	public Line(String name, String data, String path) {
		mName = name;
		mData = data;
		mPath = path;
	}

	public String getName() {
		return mName;
	}

	public String getData() {
		return mData;
	}

	public String getPath() {
		return mPath;
	}

	@Override
	public int compareTo(Line line) {
		if (this.mName != null)
			return this.mName.toLowerCase(Locale.getDefault()).compareTo(
					line.getName().toLowerCase(Locale.getDefault()));
		else
			throw new IllegalArgumentException();
	}
}
