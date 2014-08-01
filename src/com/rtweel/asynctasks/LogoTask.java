package com.rtweel.asynctasks;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

public class LogoTask extends AsyncTask<String, Void, Bitmap> {

	@Override
	protected Bitmap doInBackground(String... params) {
		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(params[0]);
		Bitmap bitmap = null;

		try {
			HttpResponse response = client.execute(request);

			HttpEntity entity = response.getEntity();

			InputStream stream = entity.getContent();

			bitmap = BitmapFactory.decodeStream(stream);

			entity.consumeContent();

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bitmap;
	}

	@Override
	protected void onPostExecute(Bitmap result) {
		super.onPostExecute(result);
	}
}
