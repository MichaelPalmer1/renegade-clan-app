package com.renegade.rc;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TimeZone;

public class BansFragment_Tabs_Cards extends RCFragment {
	public ArrayList<Bans> banList = new ArrayList<>();
	public RecyclerView bansRecycler;
	private DownloadJSONTask dlTask = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_bans_cards, container, false);
		bansRecycler = (RecyclerView) rootView.findViewById(R.id.bansRecycler);

		LinearLayoutManager llm = new LinearLayoutManager(getContext());
		bansRecycler.setLayoutManager(llm);

		if (dlTask != null && dlTask.getStatus() != DownloadJSONTask.Status.FINISHED)
			dlTask.cancel(true);

		dlTask = (DownloadJSONTask) new DownloadJSONTask().execute("Server/Bans");

		return rootView;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.bans, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (dlTask != null && dlTask.getStatus() != DownloadJSONTask.Status.FINISHED) {
			dlTask.cancel(true);
			dlTask = null;
		}
	}

	@Override
	public void tabSelected() {
		bansRecycler.smoothScrollToPosition(0);
	}

	public void processBanData(String data) {
		try {
			JSONArray json = new JSONArray(data);
			banList.clear();

			SimpleDateFormat formatIn = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
			SimpleDateFormat formatOut = new SimpleDateFormat("MM/dd/yy hh:mma", Locale.US);
			formatIn.setTimeZone(TimeZone.getTimeZone("UTC"));

			for (int i = 0; i < json.length(); i++) {
				JSONObject banData = json.getJSONObject(i);
				String made = banData.getString("made"), expires = banData.getString("expires");
				try {
					made = formatOut.format(formatIn.parse(made));
					made = made.substring(0, made.length() - 2)
							+ made.substring(made.length() - 2, made.length() - 1)
							      .toLowerCase();
					if (!expires.equals("9999-12-31 23:59:59")) {
						expires = formatOut.format(formatIn.parse(expires));
						expires = expires.substring(0, expires.length() - 2)
								+ expires.substring(expires.length() - 2, expires.length() - 1)
								         .toLowerCase();
					} else {
						expires = "Permanent";
					}
				} catch (ParseException e) {
					Log.e("ProcessBanData", "Unable to parse date");
					e.printStackTrace();
				}
				banList.add(new Bans(
						banData.getString("name"),
						made,
						banData.getString("reason"),
						banData.getString("banner"),
						expires
				));
			}
			BansAdapter adapter = new BansAdapter(banList);
			bansRecycler.setAdapter(adapter);

		} catch (JSONException e) {
			Log.e("Bans_Tabs", "JSON Exception: " + e.toString());
			e.printStackTrace();
		}
	}

	private class DownloadJSONTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {
			String s, response = "";
			try {
				// Create connection
				HttpURLConnection conn = (HttpURLConnection) new URL( RC.generateAPI(urls[0]) )
						.openConnection();
				conn.setRequestMethod("GET");

				// Check response code
				if (conn.getResponseCode() != 200) {
					throw new Exception(
							String.format(
									"Could not get data from remote source. HTTP response: %s (%d)",
									conn.getResponseMessage(), conn.getResponseCode()
							)
					);
				}

				// Save response to a string
				InputStream in = new BufferedInputStream(conn.getInputStream());
				BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
				while ((s = buffer.readLine()) != null)
					response += s;

			} catch (Exception e) {
				Log.e("DownloadJSON_Bans", "Error encountered while downloading JSON");
				e.printStackTrace();
			}
			return response;
		}

		@Override
		protected void onPostExecute(String result) {
			// Check for cancellations
			if (isCancelled())
				result = null;

			// Check result
			if (result != null)
				processBanData(result);
		}
	}

	class BansAdapter extends RecyclerView.Adapter<BansAdapter.BanViewHolder> {

		private ArrayList<Bans> bans;

		class BanViewHolder extends RecyclerView.ViewHolder {
			CardView cardView;
			TextView bannedPlayer, banCreated, banReason, banner, banExpires;

			public BanViewHolder(View view) {
				super(view);
				cardView = (CardView) view.findViewById(R.id.card_view_ban);
				bannedPlayer = (TextView) view.findViewById(R.id.bannedPlayer);
				banCreated = (TextView) view.findViewById(R.id.banCreated);
				banReason = (TextView) view.findViewById(R.id.banReason);
				banner = (TextView) view.findViewById(R.id.banner);
				banExpires = (TextView) view.findViewById(R.id.banExpires);
			}
		}

		public BansAdapter(ArrayList<Bans> bans) {
			this.bans = bans;
		}

		@Override
		public int getItemCount() {
			return bans.size();
		}

		@Override
		public BanViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bandetail_cards, parent, false);
			return new BanViewHolder(view);
		}

		@Override
		public void onBindViewHolder(BanViewHolder holder, int position) {
			holder.bannedPlayer.setText(bans.get(position).getPlayer());
			RC.color(holder.bannedPlayer);

			holder.banCreated.setText(bans.get(position).getCreated());

			holder.banReason.setText(bans.get(position).getReason());
			RC.color(holder.banReason);

			holder.banner.setText(bans.get(position).getBanner());
			RC.color(holder.banner);

			holder.banExpires.setText(bans.get(position).getExpires());
		}

		@Override
		public void onAttachedToRecyclerView(RecyclerView recyclerView) {
			super.onAttachedToRecyclerView(recyclerView);
		}
	}

}