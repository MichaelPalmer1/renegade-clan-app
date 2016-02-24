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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class MapVoteFragment extends Fragment {
    public ArrayList<Votes> voteList = new ArrayList<>();
    public ListView listMapVotes;
    private ProgressDialog progressDialog;
    private DownloadJSONTask dlTask = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_mapvotes, container, false);
        listMapVotes = (ListView) rootView.findViewById(R.id.listMapVotes);
        progressDialog = new ProgressDialog(getActivity());

        if (dlTask != null && dlTask.getStatus() != DownloadJSONTask.Status.FINISHED) {
            dlTask.cancel(true);
        }

        dlTask = (DownloadJSONTask) new DownloadJSONTask().execute("Map/Votes");

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.mapvotes, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(MainActivity.VOTES);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dlTask != null && dlTask.getStatus() != DownloadJSONTask.Status.FINISHED) {
            dlTask.cancel(true);
            dlTask = null;
        }
    }

    public void processMapData(String data) {
        try {
            JSONArray json = new JSONArray(data);
            voteList.clear();

            for(int i = 0; i < json.length(); i++) {
                JSONObject voteData = json.getJSONObject(i);
                voteList.add( new Votes(
                        voteData.getString("name"),
                        voteData.getString("mapname"),
                        Integer.parseInt(voteData.getString("times_played")),
                        Integer.parseInt(voteData.getString("last_played")) ,
                        Integer.parseInt(voteData.getString("total_votes")),
                        Integer.parseInt(voteData.getString("vote_eligible")),
                        Double.parseDouble(voteData.getString("liking"))
                ));
            }
            CustomAdapter adapter = new CustomAdapter(getActivity(), R.layout.votedetail, voteList);
            listMapVotes.setAdapter(adapter);

        } catch (JSONException e) {
            Log.e("MapVotes", "JSON Exception: " + e.toString());
            e.printStackTrace();
        }
    }

    private class DownloadJSONTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            progressDialog.setTitle("Please wait");
            progressDialog.setMessage("Fetching map votes...");
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
                Log.e("DownloadJSON_Votes", "Error encountered while downloading JSON");
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
                processMapData(result);
            }

            // Close progress dialog
            progressDialog.dismiss();
        }
    }

    class CustomAdapter extends ArrayAdapter<Votes> {
        private final Context context;
        private final int resourceID;
        private final ArrayList<Votes> vote_details;

        public CustomAdapter(Context context, int resource, ArrayList<Votes> list) {
            super(context, resource, list);

            this.vote_details = list;
            this.context = context;
            this.resourceID = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(resourceID, parent, false);
            TextView mapVoteName = (TextView) rowView.findViewById(R.id.mapVoteName);
            ImageView mapVoteImage = (ImageView) rowView.findViewById(R.id.mapVoteImage);
            TextView timesPlayed = (TextView) rowView.findViewById(R.id.timesPlayed);
            TextView lastPlayed = (TextView) rowView.findViewById(R.id.lastPlayed);
            TextView votes = (TextView) rowView.findViewById(R.id.votes);
            TextView mapUsage = (TextView) rowView.findViewById(R.id.mapUsage);

            // Map Name
            String mapName = vote_details.get(position).getMapName();
            if (mapName == null) {
                mapName = vote_details.get(position).getName();
            }
            mapVoteName.setText(mapName);
            RC.color(mapVoteName);

            // Map Image
            Picasso.with(getActivity())
                   .load(String.format("http://www.therenegadeclan.org/images/maps/%s.png",
                           vote_details.get(position).getName()))
                   .placeholder(R.drawable.unknown_map)
                   .into(mapVoteImage);

            // Times Played
            timesPlayed.setText(String.valueOf(vote_details.get(position).getTimesPlayed()));

            // Last Played
            int last = vote_details.get(position).getLastPlayed();
            if (last == -1) {
                lastPlayed.setText(getString(R.string.never));
            } else {
                lastPlayed.setText(
                        getString(R.string.lastPlayed, last, last == 1 ? "" : "s")
                );
            }

            // Votes
            votes.setText(
                    getString(R.string.mapVotes, vote_details.get(position).getTotalVotes(),
                            vote_details.get(position).getVoteEligible())
            );

            // Map Usage
            mapUsage.setText(
                    getString(R.string.mapUsage, vote_details.get(position).getLiking())
            );

            return rowView;
        }
    }

}