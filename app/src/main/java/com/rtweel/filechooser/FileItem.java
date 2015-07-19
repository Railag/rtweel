package com.rtweel.filechooser;

import android.support.annotation.NonNull;

import java.util.Locale;

public class FileItem implements Comparable<FileItem> {
	private String mName;
	private String mDetails;
	private String mPath;

	public FileItem(String name, String details, String path) {
		mName = name;
		mDetails = details;
		mPath = path;
	}

	public String getName() {
		return mName;
	}

	public String getDetails() {
		return mDetails;
	}

	public String getPath() {
		return mPath;
	}

	@Override
	public int compareTo(@NonNull FileItem item) {
		if (this.mName != null)
			return this.mName.toLowerCase(Locale.getDefault()).compareTo(
					item.getName().toLowerCase(Locale.getDefault()));
		else
			throw new IllegalArgumentException();
	}
}
