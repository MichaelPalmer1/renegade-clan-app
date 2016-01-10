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

public class WarningsFragment_Tabs_Cards extends Fragment {
	public ArrayList<Warnings> warningList = new ArrayList<>();
	public RecyclerView warningsRecycler;
	private DownloadJSONTask dlTask = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_warnings_cards, container, false);
		warningsRecycler = (RecyclerView) rootView.findViewById(R.id.warningsRecycler);

		LinearLayoutManager llm = new LinearLayoutManager(getContext());
		warningsRecycler.setLayoutManager(llm);

		if (dlTask != null && dlTask.getStatus() != DownloadJSONTask.Status.FINISHED)
			dlTask.cancel(true);

		dlTask = (DownloadJSONTask) new DownloadJSONTask().execute("Server/Warnings");

		return rootView;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.warnings, menu);
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

	public void tabSelected() {
		warningsRecycler.smoothScrollToPosition(0);
	}

	public void processWarningData(String data) {
		try {
			JSONArray json = new JSONArray(data);
			warningList.clear();

			SimpleDateFormat formatIn = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
			SimpleDateFormat formatOut = new SimpleDateFormat("MM/dd/yy hh:mma", Locale.US);
			formatIn.setTimeZone(TimeZone.getTimeZone("UTC"));

			for (int i = 0; i < json.length(); i++) {
				JSONObject warningData = json.getJSONObject(i);
				String made = warningData.getString("made");
				try {
					made = formatOut.format(formatIn.parse(made));
					made = made.substring(0, made.length() - 2)
							+ made.substring(made.length() - 2, made.length() - 1)
							      .toLowerCase();
				} catch (ParseException e) {
					Log.e("ProcessWarningData", "Unable to parse date");
					e.printStackTrace();
				}
				warningList.add(new Warnings(
						warningData.getString("name"),
						warningData.getString("warning"),
						made,
						warningData.getString("warner")
				));
			}
			WarningsAdapter adapter = new WarningsAdapter(warningList);
			warningsRecycler.setAdapter(adapter);

		} catch (JSONException e) {
			Log.e("Warnings_Tabs", "JSON Exception: " + e.toString());
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
				Log.e("DownloadJSON_Warnings", "Error encountered while downloading JSON");
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
				processWarningData(result);
		}
	}

	class WarningsAdapter extends RecyclerView.Adapter<WarningsAdapter.WarningViewHolder> {

		private ArrayList<Warnings> warnings;

		class WarningViewHolder extends RecyclerView.ViewHolder {
			CardView cardView;
			TextView warnedPlayer, warning, warningCreated, warner;

			public WarningViewHolder(View view) {
				super(view);
				cardView = (CardView) view.findViewById(R.id.card_view_warning);
				warnedPlayer = (TextView) view.findViewById(R.id.warnedPlayer);
				warningCreated = (TextView) view.findViewById(R.id.warningCreated);
				warning = (TextView) view.findViewById(R.id.warning);
				warner = (TextView) view.findViewById(R.id.warner);
			}
		}

		public WarningsAdapter(ArrayList<Warnings> warnings) {
			this.warnings = warnings;
		}

		@Override
		public int getItemCount() {
			return warnings.size();
		}

		@Override
		public WarningViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View view = LayoutInflater.from(parent.getContext())
									  .inflate(R.layout.warningdetail_cards, parent, false);
			return new WarningViewHolder(view);
		}

		@Override
		public void onBindViewHolder(WarningViewHolder holder, int position) {
			holder.warnedPlayer.setText(warnings.get(position).getName());
			RC.color(holder.warnedPlayer);

			holder.warningCreated.setText(warnings.get(position).getCreated());

			holder.warning.setText(warnings.get(position).getWarning());
			RC.color(holder.warning);

			holder.warner.setText(warnings.get(position).getWarner());
			RC.color(holder.warner);
		}

		@Override
		public void onAttachedToRecyclerView(RecyclerView recyclerView) {
			super.onAttachedToRecyclerView(recyclerView);
		}
	}

}