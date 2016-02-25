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
import android.widget.ImageView;
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
import java.util.List;

public class MapVoteFragment_Tabs_Cards extends Fragment {
    public ArrayList<Votes> voteList = new ArrayList<>();
    public RecyclerView voteRecycler;
    private DownloadJSONTask dlTask = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_mapvote_cards, container, false);
        voteRecycler = (RecyclerView) rootView.findViewById(R.id.voteRecycler);

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        voteRecycler.setLayoutManager(llm);

        if (dlTask != null && dlTask.getStatus() != DownloadJSONTask.Status.FINISHED)
            dlTask.cancel(true);

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
    public void onDestroy() {
        super.onDestroy();
        if (dlTask != null && dlTask.getStatus() != DownloadJSONTask.Status.FINISHED) {
            dlTask.cancel(true);
            dlTask = null;
        }
    }

    public void tabSelected() {
        voteRecycler.smoothScrollToPosition(0);
    }

    public void processMapData(String data) {
        try {
            JSONArray json = new JSONArray(data);

            voteList.clear();

            for(int i = 0; i < json.length(); i++) {
                JSONObject voteData = json.getJSONObject(i);
                voteList.add( new Votes(
                        voteData.getString("name"),
                        voteData.getString("mapname").equals("null") ?
                                voteData.getString("name") : voteData.getString("mapname"),
                        Integer.parseInt(voteData.getString("times_played")),
                        Integer.parseInt(voteData.getString("last_played")) ,
                        Integer.parseInt(voteData.getString("total_votes")),
                        Integer.parseInt(voteData.getString("vote_eligible")),
                        Double.parseDouble(voteData.getString("liking"))
                ));
            }
            RVAdapter adapter = new RVAdapter(voteList);
            voteRecycler.setAdapter(adapter);

        } catch (JSONException e) {
            Log.e("MapVotes_Tabs", "JSON Exception: " + e.toString());
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
            if ( isCancelled() )
                result = null;

            // Check result
            if ( result != null )
                processMapData(result);
        }
    }

    class RVAdapter extends RecyclerView.Adapter<RVAdapter.VoteViewHolder> {

        class VoteViewHolder extends RecyclerView.ViewHolder {
            CardView cardView;
            TextView mapVoteName;
            ImageView mapVoteImage;
            TextView timesPlayed;
            TextView lastPlayed;
            TextView votes;
            TextView mapUsage;

            public VoteViewHolder(View view) {
                super(view);
                cardView = (CardView) view.findViewById(R.id.card_view_map_vote);
                mapVoteName = (TextView) view.findViewById(R.id.mapVoteName);
                mapVoteImage = (ImageView) view.findViewById(R.id.mapVoteImage);
                timesPlayed = (TextView) view.findViewById(R.id.timesPlayed);
                lastPlayed = (TextView) view.findViewById(R.id.lastPlayed);
                votes = (TextView) view.findViewById(R.id.votes);
                mapUsage = (TextView) view.findViewById(R.id.mapUsage);
            }
        }

        List<Votes> votes;

        RVAdapter(List<Votes> votes) {
            this.votes = votes;
        }

        @Override
        public int getItemCount() {
            return votes.size();
        }

        @Override
        public VoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                                      .inflate(R.layout.votedetail_cards, parent, false);
            return new VoteViewHolder(view);
        }

        @Override
        public void onBindViewHolder(VoteViewHolder holder, int position) {
            // Map Name
            holder.mapVoteName.setText(votes.get(position).getMapName());
            RC.color(holder.mapVoteName);

            // Map Image
            Picasso.with(getActivity())
                    .load(String.format("http://www.therenegadeclan.org/images/maps/%s.png",
                            votes.get(position).getName()))
                    .placeholder(R.drawable.unknown_map)
                   .into(holder.mapVoteImage);

            // Times Played
            holder.timesPlayed.setText(String.valueOf(votes.get(position).getTimesPlayed()));

            // Last Played
            int lastPlayed = votes.get(position).getLastPlayed();
            if (lastPlayed == -1) {
                holder.lastPlayed.setText(getString(R.string.never));
            } else {
                holder.lastPlayed.setText(
                        getString(R.string.lastPlayed, lastPlayed, lastPlayed == 1 ? "" : "s")
                );
            }

            // Votes
            holder.votes.setText(
                    getString(R.string.mapVotes, votes.get(position).getTotalVotes(),
                            votes.get(position).getVoteEligible())
            );

            // Map Usage
            holder.mapUsage.setText(
                    getString(R.string.mapUsage, votes.get(position).getLiking())
            );

        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }
    }

}