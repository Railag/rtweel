package com.rtweel.tweet;

import java.util.List;
import java.util.concurrent.ExecutionException;

import twitter4j.Status;

import com.rtweel.R;
import com.rtweel.asynctasks.LogoTask;
import com.rtweel.cache.App;
import com.rtweel.cache.DiskCache;
import com.rtweel.parsers.DateParser;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TweetAdapter extends BaseAdapter {

	private final List<Status> mData;
	private final Context mContext;

	public TweetAdapter(List<Status> data, Context context) {
		this.mData = data;
		this.mContext = context;
	}

	public TweetAdapter(Timeline timeline, Context context) {
		this.mData = timeline.getTweets();
		this.mContext = context;
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public Object getItem(int position) {
		return mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (convertView == null) {

			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.list_item, parent, false);

			TextView text = (TextView) convertView
					.findViewById(R.id.tweet_text);
			TextView author = (TextView) convertView
					.findViewById(R.id.tweet_author);
			TextView date = (TextView) convertView
					.findViewById(R.id.tweet_date);
			ImageView picture = (ImageView) convertView
					.findViewById(R.id.tweet_author_picture);

			ViewHolder vh = new ViewHolder(author, text, date, picture);

			convertView.setTag(vh);

		}

		ViewHolder vh = (ViewHolder) convertView.getTag();

		Status tweet = mData.get(position);

		String imageUri = tweet.getUser().getMiniProfileImageURL();

		String cacheName = tweet.getUser().getName().replace(' ', '_')
				+ "_mini";
		// LogoTask task = new LogoTask();

		// DiskCache cache = //new DiskCache(mContext,"bitmap", 10*1024*1024,
		// CompressFormat.JPEG, 70);
		// mDiskLruCache = cache;

		App app = (App) mContext;
		DiskCache cache = app.getDiskCache();
		// Bitmap bitmap = getBitmapFromDiskCache(tweet.getUser().getName());
		Bitmap bitmap = cache.getBitmap(cacheName);

		// Bitmap bitmap = MemoryCache.getBitmap(tweet.getUser().getName());
		if (bitmap == null) {
			if (!app.isOnline()) {
				Log.i("DEBUG", "picture task tweet adapter NO NETWORK");
				Options opts = new Options();
				opts.outHeight = 24;
				opts.outWidth = 24;
				opts.inScaled = true;
				// TODO: valid picture size

				bitmap = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.ic_launcher, opts); 
			} else {
				try {
					bitmap = new LogoTask().execute(imageUri).get();
					cache.put(cacheName, bitmap);
					// MemoryCache.addBitmap(tweet.getUser().getName(), bitmap);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				} catch (NullPointerException e) {
					e.printStackTrace();
					Options opts = new Options();
					opts.outHeight = 24;
					opts.outWidth = 24;
					opts.inScaled = true;

					bitmap = BitmapFactory.decodeResource(
							mContext.getResources(), R.drawable.ic_launcher,
							opts);
				}
			}
		}

		vh.getPictureView().setImageBitmap(bitmap);

		// Log.i("DEBUG",
		// "Width: "+vh.getPictureView().getDrawable().getMinimumWidth()
		// + " Height: " +vh.getPictureView().getDrawable().getMinimumHeight());

		vh.getAuthorView().setText(tweet.getUser().getName());

		vh.getTextView().setText(tweet.getText().replace('\n', ' '));

		String date = DateParser.parse(tweet.getCreatedAt().toString());

		vh.getDaTextView().setText(date);

		return convertView;
	}

	private class ViewHolder {
		private final TextView mAuthorView;
		private final TextView mTextView;
		private final TextView mDateView;
		private final ImageView mPictureView;

		public ViewHolder(TextView user, TextView text, TextView date,
				ImageView picture) {
			this.mAuthorView = user;
			this.mTextView = text;
			this.mDateView = date;
			this.mPictureView = picture;
		}

		public TextView getAuthorView() {
			return mAuthorView;
		}

		public TextView getTextView() {
			return mTextView;
		}

		public TextView getDaTextView() {
			return mDateView;
		}

		public ImageView getPictureView() {
			return mPictureView;
		}

	}

}
