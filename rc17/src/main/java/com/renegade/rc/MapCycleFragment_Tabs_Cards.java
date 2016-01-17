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

public class MapCycleFragment_Tabs_Cards extends Fragment {
    public ArrayList<Maps> mapList = new ArrayList<>();
    public RecyclerView cycleRecycler;
    private DownloadJSONTask dlTask = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_mapcycle_cards, container, false);
        cycleRecycler = (RecyclerView) rootView.findViewById(R.id.cycleRecycler);

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        cycleRecycler.setLayoutManager(llm);

        if (dlTask != null && dlTask.getStatus() != DownloadJSONTask.Status.FINISHED)
            dlTask.cancel(true);

        dlTask = (DownloadJSONTask) new DownloadJSONTask().execute("Map/Cycle");

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.mapcycle, menu);
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
        cycleRecycler.smoothScrollToPosition(0);
    }

    public void processMapData(String data) {
        try {
            JSONObject json = new JSONObject(data);
            JSONObject maps = json.getJSONObject("maps");
            JSONArray cycle = json.getJSONArray("cycle");

            mapList.clear();

            for(int i = 0; i < cycle.length(); i++) {
                JSONObject mapData = maps.getJSONObject( cycle.getString(i) );
                mapList.add( new Maps(
                        mapData.getString("bsp"),
                        mapData.getString("map"),
                        (mapData.has("length") && !mapData.isNull("length")) ? mapData.getInt("length") : 0,
                        mapData.has("mapDesc") ? mapData.getString("mapDesc") : null
                ));
            }
            RVAdapter adapter = new RVAdapter(mapList);
            cycleRecycler.setAdapter(adapter);

        } catch (JSONException e) {
            Log.e("MapCycle_Tabs", "JSON Exception: " + e.toString());
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
                Log.e("DownloadJSON_Maps", "Error encountered while downloading JSON");
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

    class RVAdapter extends RecyclerView.Adapter<RVAdapter.MapViewHolder> {

        class MapViewHolder extends RecyclerView.ViewHolder {
            CardView cardView;
            TextView mapName;
            ImageView mapImage;
            TextView lblMapDesc;
            TextView mapDesc;
            TextView spacer;
            TextView lblMapLength;
            TextView mapLength;

            public MapViewHolder(View view) {
                super(view);
                cardView = (CardView) view.findViewById(R.id.card_view);
                mapName = (TextView) view.findViewById(R.id.mapName);
                mapImage = (ImageView) view.findViewById(R.id.mapImage);
                lblMapDesc = (TextView) view.findViewById(R.id.lblDesc);
                mapDesc = (TextView) view.findViewById(R.id.mapDesc);
                spacer = (TextView) view.findViewById(R.id.spacer);
                lblMapLength = (TextView) view.findViewById(R.id.lblTimelimit);
                mapLength = (TextView) view.findViewById(R.id.mapTimelimit);
            }
        }

        List<Maps> maps;

        RVAdapter(List<Maps> maps) {
            this.maps = maps;
        }

        @Override
        public int getItemCount() {
            return maps.size();
        }

        @Override
        public MapViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mapdetail_cards, parent, false);
            return new MapViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MapViewHolder holder, int position) {
            holder.mapName.setText(maps.get(position).getMap());
            RC.color(holder.mapName);
            Picasso.with(getActivity())
                    .load(String.format("http://www.therenegadeclan.org/images/maps/%s.png",
                            maps.get(position).getBsp()))
                    .placeholder(R.drawable.unknown_map)
                    .into(holder.mapImage);

            // Description
            if(maps.get(position).getMapDesc() != null) {
                holder.mapDesc.setText( maps.get(position).getMapDesc() );
                RC.color(holder.mapDesc);
            } else {
                holder.lblMapDesc.setVisibility(View.GONE);
                holder.mapDesc.setVisibility(View.GONE);
                holder.spacer.setVisibility(View.GONE);
            }

            // Length
            if(maps.get(position).getLength() > 0) {
                holder.mapLength.setText(String.format("%d minutes",
                        maps.get(position).getLength()));
            } else {
                holder.lblMapDesc.setVisibility(View.GONE);
                holder.mapLength.setVisibility(View.GONE);
            }
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }
    }

}