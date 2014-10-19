package com.example.tagged;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class MyMainActivity extends ActionBarActivity {

	// Q2. Photo browser
	// Your app calls a flickr API to get recent photos and show them one by one
	// when swiping right to left. You will need to get developer keys from
	// flickr for this.
	// http://api.flickr.com/services/rest/?method=flickr.photos.getrecent

	// Key:
	// 76b1f48b66bf401ee4a2e53f1f49a0b6
	// Secret:
	// 4f1c04517fd201d0
	// https://api.flickr.com/services/rest/?method=flickr.photos.getrecent&api_key=76b1f48b66bf401ee4a2e53f1f49a0b6&extras=url_sq

	static ImageLoader imgLoader;
	static List<Entry> entries;
	int currentPosition;
	static ImageView currentView;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	MyPagerAdapter myPagerAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		imgLoader = new ImageLoader(getApplicationContext());

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		myPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(myPagerAdapter);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageSelected(int arg0) {
				currentPosition = arg0;
				Log.e("onPageSelected", "" + arg0);

				if (entries != null && !entries.isEmpty()) {
					Log.e("url", entries.get(currentPosition).url);
					imgLoader.DisplayImage(entries.get(currentPosition).url,
							R.drawable.loader, currentView);
				}

			}

		});
		new FetchPhotoListTask().execute(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";

		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static PlaceholderFragment newInstance(int sectionNumber) {
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public PlaceholderFragment() {
		}

		@Override
		public void onResume() {
			super.onResume();
			int position = getArguments().getInt(ARG_SECTION_NUMBER);
			Log.e("onResume", "" + position);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			int position = getArguments().getInt(ARG_SECTION_NUMBER);
			Log.e("onCreateView", "" + position);

			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);

			currentView = (ImageView) rootView.findViewById(R.id.imageView1);

			if (entries != null && !entries.isEmpty()) {
				imgLoader
						.DisplayImage(
								entries.get(getArguments().getInt(
										ARG_SECTION_NUMBER)).url,
								R.drawable.loader, currentView);
			}
			return rootView;
		}
	}

	private class FetchPhotoListTask extends
			AsyncTask<Boolean, Integer, Boolean> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (result && !entries.isEmpty()) {
				myPagerAdapter.notifyDataSetChanged();
			}
		}

		@Override
		protected Boolean doInBackground(Boolean... params) {
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet();
			try {
				request.setURI(new URI(
						"https://api.flickr.com/services/rest/?method=flickr.photos.getrecent&api_key=76b1f48b66bf401ee4a2e53f1f49a0b6&extras=url_sq"));
				HttpResponse response = client.execute(request);

				// receive response as inputStream
				InputStream inputStream = response.getEntity().getContent();
				String result = "";
				// convert inputstream to string
				if (inputStream != null) {
					// result = convertInputStreamToString(inputStream);
					// Log.e("tagged stream", result);
					// JSONObject json = new JSONObject(result);
					try {
						XmlPullParser parser = Xml.newPullParser();
						parser.setFeature(
								XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
						parser.setInput(inputStream, null);
						parser.nextTag();

						entries = new ArrayList<Entry>();

						parser.require(XmlPullParser.START_TAG,
								XmlPullParser.NO_NAMESPACE, "rsp");
						String name = parser.getName();

						while (parser.next() != XmlPullParser.END_TAG) {
							if (parser.getEventType() != XmlPullParser.START_TAG) {
								continue;
							}
							name = parser.getName();

							// Starts by looking for the entry tag
							if (name.equals("photo")) {
								entries.add(readEntry(parser));
								parser.nextTag();
							}
						}
						return true;
					} catch (XmlPullParserException e) {
						e.printStackTrace();
					} finally {
						inputStream.close();
					}
				}

			} catch (URISyntaxException e) {
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return false;
		}

	}

	private static String convertInputStreamToString(InputStream inputStream)
			throws IOException {
		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(inputStream));
		String line = "";
		String result = "";
		while ((line = bufferedReader.readLine()) != null)
			result += line;

		inputStream.close();
		return result;
	}

	public boolean isConnected() {
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected())
			return true;
		else
			return false;
	}

	public static class Entry {
		public final String url;
		public final int width;
		public final int height;

		private Entry(String url, int width, int height) {
			this.url = url;
			this.width = width;
			this.height = height;
		}
	}

	private Entry readEntry(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, XmlPullParser.NO_NAMESPACE,
				"photo");
		String url = null;
		int width = 0;
		int height = 0;

		String name = parser.getName();
		if (name.equals("photo")) {
			url = parser
					.getAttributeValue(XmlPullParser.NO_NAMESPACE, "url_sq");
			Log.e("tagged", url);
			Log.e("tagged", parser.getAttributeValue(
					XmlPullParser.NO_NAMESPACE, "width_sq"));
			Log.e("tagged", parser.getAttributeValue(
					XmlPullParser.NO_NAMESPACE, "height_sq"));

			width = Integer.valueOf(parser.getAttributeValue(
					XmlPullParser.NO_NAMESPACE, "width_sq"));
			height = Integer.valueOf(parser.getAttributeValue(
					XmlPullParser.NO_NAMESPACE, "height_sq"));
		}
		return new Entry(url, width, height);
	}

	public class MyPagerAdapter extends FragmentStatePagerAdapter {

		public MyPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			return PlaceholderFragment.newInstance(currentPosition);
		}

		@Override
		public int getCount() {
			return entries == null ? 0 : entries.size();
		}
	}
}
