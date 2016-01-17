package com.renegade.rc;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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

public class WarningsFragment extends Fragment {
    public ArrayList<Warnings> warningList = new ArrayList<Warnings>();
    public ListView listWarnings;
    private ProgressDialog progressDialog;
    private DownloadJSONTask dlTask = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_warnings, container, false);
        listWarnings = (ListView) rootView.findViewById(R.id.listWarnings);
        progressDialog = new ProgressDialog(getActivity());

        if (dlTask != null && dlTask.getStatus() != DownloadJSONTask.Status.FINISHED) {
            dlTask.cancel(true);
        }

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
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(MainActivity.WARNINGS);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dlTask != null && dlTask.getStatus() != DownloadJSONTask.Status.FINISHED) {
            dlTask.cancel(true);
            dlTask = null;
        }
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

			WarningsAdapter adapter = new WarningsAdapter(getActivity(), R.layout.warningdetail, warningList);
			listWarnings.setAdapter(adapter);

		} catch (JSONException e) {
			Log.e("Warnings_Tabs", "JSON Exception: " + e.toString());
			e.printStackTrace();
		}
	}

    private class DownloadJSONTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            progressDialog.setTitle("Please wait");
            progressDialog.setMessage("Fetching warnings...");
            progressDialog.show();
            super.onPreExecute();
        }

        protected String doInBackground(String... urls) {
            String s, response = "";
            try {
                // Create connection
                HttpURLConnection conn = (HttpURLConnection) new URL( RC.generateAPI(urls[0]) )
                        .openConnection();
                conn.setRequestMethod("GET");

                // Check response code
                if(conn.getResponseCode() != 200) {
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
            if(isCancelled()) {
                result = null;
            }

            // Check result
            if(result != null) {
                processWarningData(result);
            }

            // Close progress dialog
            progressDialog.dismiss();
        }
    }

    class WarningsAdapter extends ArrayAdapter<Warnings> {
        private final Context context;
        private final int resourceID;
        private final ArrayList<Warnings> warnings;

        public WarningsAdapter(Context context, int resource, ArrayList<Warnings> list) {
            super(context, resource, list);
            this.warnings = list;
            this.context = context;
            this.resourceID = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(resourceID, parent, false);
			TextView warnedPlayer = (TextView) rowView.findViewById(R.id.warnedPlayer);
			TextView warningCreated = (TextView) rowView.findViewById(R.id.warningCreated);
			TextView warning = (TextView) rowView.findViewById(R.id.warning);
			TextView warner = (TextView) rowView.findViewById(R.id.warner);

            // Player
            warnedPlayer.setText(warnings.get(position).getName());
            RC.color(warnedPlayer);

            // Created
            warningCreated.setText(warnings.get(position).getCreated());

            // Warning
            warning.setText(warnings.get(position).getWarning());
            RC.color(warning);

            // Warner
            warner.setText(warnings.get(position).getWarner());
            RC.color(warner);

            return rowView;
        }
    }

}