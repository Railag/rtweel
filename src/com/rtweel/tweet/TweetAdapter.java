package com.rtweel.tweet;

import java.util.List;

import twitter4j.MediaEntity;
import twitter4j.Status;

import com.rtweel.R;
import com.rtweel.asynctasks.tweet.BitmapWorkerTaskMedia;
import com.rtweel.asynctasks.tweet.BitmapWorkerTaskProfile;
import com.rtweel.cache.App;
import com.rtweel.cache.AsyncDrawable;
import com.rtweel.cache.DiskCache;
import com.rtweel.parsers.DateParser;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TweetAdapter extends BaseAdapter {

	private final List<Status> mData;
	private final Context mContext;

	boolean mIsMediaAvailable;

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
			ImageView media = (ImageView) convertView
					.findViewById(R.id.tweet_media);

			ViewHolder vh = new ViewHolder(author, text, date, picture, media);

			convertView.setTag(vh);

		}

		ViewHolder vh = (ViewHolder) convertView.getTag();

		Status tweet = mData.get(position);
		
	//	Log.i("DEBUG", tweet.toString());

		String imageUri = tweet.getUser().getProfileImageURL();// getMiniProfileImageURL();

		MediaEntity[] entities = tweet.getMediaEntities();
		String url = null;

		if (entities.length > 0) {
			url = entities[0].getMediaURL();

			String cacheName = "entity_" + tweet.getId();
	//		LayoutParams params = vh.getMediaView().getLayoutParams();
	//		params.height = 175;
	//		params.width = 250;
	//		vh.getMediaView().setLayoutParams(params);
			vh.loadBitmapMedia(url, vh.getMediaView(), cacheName);
		} else {
			App app = (App) mContext;
			DiskCache cache = app.getDiskCache();
			// Drawable standart = mContext.getResources().getDrawable(
			// R.drawable.standart_image);
			// Canvas canvas = new Canvas();
			// standart.draw(canvas);
			if (!cache.containsKey("standart")) {
				Bitmap bitmap = Bitmap.createBitmap(2, 2, Config.ARGB_8888);
				bitmap.eraseColor(Color.WHITE);
				cache.put("standart_white", bitmap);
			}
	//		LayoutParams params = vh.getMediaView().getLayoutParams();
	//		params.height = 2;
	//		params.width = 2;
	//		vh.getMediaView().setLayoutParams(params);
			vh.loadBitmapMedia("standart", vh.getMediaView(), "standart_white");
		}

		String cacheName = tweet.getUser().getName().replace(' ', '_')
				+ "_mini";

		vh.loadBitmapProfile(imageUri, vh.getPictureView(), cacheName);

		vh.getAuthorView().setText(tweet.getUser().getName());

		vh.getTextView().setText(tweet.getText().replace('\n', ' '));

		String date = DateParser.parse(tweet.getCreatedAt().toString());

		vh.getDateView().setText(date);

		return convertView;
	}

	private static BitmapWorkerTaskMedia getBitmapWorkerTaskMedia(ImageView imageView) {
		if (imageView != null) {
			final Drawable drawable = imageView.getDrawable();
			if (drawable instanceof AsyncDrawable) {
				final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
				return (BitmapWorkerTaskMedia) asyncDrawable
						.getBitmapWorkerTask();
			}
		}
		return null;
	}

	private static BitmapWorkerTaskProfile getBitmapWorkerTaskProfile(
			ImageView imageView) {
		if (imageView != null) {
			final Drawable drawable = imageView.getDrawable();
			if (drawable instanceof AsyncDrawable) {
				final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
				return (BitmapWorkerTaskProfile) asyncDrawable
						.getBitmapWorkerTask();
			}
		}
		return null;
	}

	public static boolean cancelPotentialWorkMedia(String url,
			ImageView imageView) {
		final BitmapWorkerTaskMedia bitmapWorkerTask = getBitmapWorkerTaskMedia(imageView);

		if (bitmapWorkerTask != null) {
			final String bitmapData = bitmapWorkerTask.getUrl();
			// If bitmapData is not yet set or it differs from the new data
			if (bitmapData == null || bitmapData != url) {
				// Cancel previous task
				// String cache = bitmapWorkerTask.getCacheName();
				bitmapWorkerTask.cancel(true);
				/*
				 * Log.i("DEBUG", "cancel2" + cache); if
				 * (cache.startsWith("entity")) {
				 * imageView.setImageDrawable(null);
				 * imageView.setVisibility(View.GONE); Log.i("DEBUG", "cancel");
				 * }
				 */
				// if (url.contains("entity")) {
				// imageView.setVisibility(View.GONE);
				// }
			} else {
				// The same work is already in progress
				return false;
			}
		}
		// No task associated with the ImageView, or an existing task was
		// cancelled
		return true;
	}

	public static boolean cancelPotentialWorkProfile(String url,
			ImageView imageView) {
		final BitmapWorkerTaskProfile bitmapWorkerTask = getBitmapWorkerTaskProfile(imageView);

		if (bitmapWorkerTask != null) {
			final String bitmapData = bitmapWorkerTask.getUrl();
			// If bitmapData is not yet set or it differs from the new data
			if (bitmapData == null || bitmapData != url) {
				// Cancel previous task
				// String cache = bitmapWorkerTask.getCacheName();
				bitmapWorkerTask.cancel(true);
				/*
				 * Log.i("DEBUG", "cancel2" + cache); if
				 * (cache.startsWith("entity")) {
				 * imageView.setImageDrawable(null);
				 * imageView.setVisibility(View.GONE); Log.i("DEBUG", "cancel");
				 * }
				 */
				// if (url.contains("entity")) {
				// imageView.setVisibility(View.GONE);
				// }
			} else {
				// The same work is already in progress
				return false;
			}
		}
		// No task associated with the ImageView, or an existing task was
		// cancelled
		return true;
	}

	private class ViewHolder {
		private final TextView mAuthorView;
		private final TextView mTextView;
		private final TextView mDateView;
		private final ImageView mPictureView;
		private final ImageView mMediaView;

		public ViewHolder(TextView user, TextView text, TextView date,
				ImageView picture, ImageView media) {
			this.mAuthorView = user;
			this.mTextView = text;
			this.mDateView = date;
			this.mPictureView = picture;
			this.mMediaView = media;
		}

		public TextView getAuthorView() {
			return mAuthorView;
		}

		public TextView getTextView() {
			return mTextView;
		}

		public TextView getDateView() {
			return mDateView;
		}

		public ImageView getPictureView() {
			return mPictureView;
		}

		public ImageView getMediaView() {
			return mMediaView;
		}

		public void loadBitmapMedia(String url, ImageView image,
				String cacheName) {
			if (cancelPotentialWorkMedia(url, image)) {
				final BitmapWorkerTaskMedia task = new BitmapWorkerTaskMedia(
						image, mContext, cacheName);
				final AsyncDrawable asyncDrawable = new AsyncDrawable(
						mContext.getResources(), App.getBitmap(), task);
				image.setImageDrawable(asyncDrawable);
				task.execute(url);
			}
		}

		public void loadBitmapProfile(String url, ImageView image,
				String cacheName) {
			if (cancelPotentialWorkProfile(url, image)) {
				final BitmapWorkerTaskProfile task = new BitmapWorkerTaskProfile(
						image, mContext, cacheName);
				final AsyncDrawable asyncDrawable = new AsyncDrawable(
						mContext.getResources(), App.getBitmap(), task);
				image.setImageDrawable(asyncDrawable);
				task.execute(url);
			}
		}
	}

}
