package com.renegade.rc;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
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

public class MapCycleFragment_Tabs extends Fragment {
    public ArrayList<JSONObject> mapList = new ArrayList<>();
    public ListView listMapCycle;
    private DownloadJSONTask dlTask = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_mapcycle, container, false);
        listMapCycle = (ListView) rootView.findViewById(R.id.listMapCycle);

        if (dlTask != null && dlTask.getStatus() != DownloadJSONTask.Status.FINISHED)
            dlTask.cancel(true);

        dlTask = (DownloadJSONTask) new DownloadJSONTask()
                .execute("http://www.therenegadeclan.org/getMapCycle.php");

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

    public void processMapData(String data) {
        try {
            JSONObject json = new JSONObject(data);
            JSONObject maps = json.getJSONObject("maps");
            JSONArray cycle = json.getJSONArray("cycle");

            mapList.clear();

            for(int i = 0; i < cycle.length(); i++)
                mapList.add( maps.getJSONObject( cycle.getString(i) ) );

            CustomAdapter adapter = new CustomAdapter(getActivity(), R.layout.mapdetail, mapList);
            listMapCycle.setAdapter(adapter);

        } catch (JSONException e) {
            Log.e("MapCycle_Tabs", "JSON Exception: " + e.toString());
            e.printStackTrace();
        }
    }

    private class DownloadJSONTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String s = "", response = "";
            try {
                // Create connection
                HttpURLConnection conn = (HttpURLConnection) new URL(urls[0]).openConnection();
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

    class CustomAdapter extends ArrayAdapter<JSONObject> {
        private final Context context;
        private final int resourceID;
        private final ArrayList<JSONObject> map_details;

        public CustomAdapter(Context context, int resource, ArrayList<JSONObject> list) {
            super(context, resource, list);

            this.map_details = list;
            this.context = context;
            this.resourceID = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(resourceID, parent, false);
            TextView mapName = (TextView) rowView.findViewById(R.id.mapName1);
            TextView mapDesc = (TextView) rowView.findViewById(R.id.mapDesc1);
            ImageView mapImage = (ImageView) rowView.findViewById(R.id.mapImage1);
            try {
                JSONObject json = map_details.get(position);
                // Name
                mapName.setText(json.getString("map"));
                RC.color(mapName);

                Picasso.with(getActivity())
                        .load("http://www.therenegadeclan.org/images/maps/"
                                + json.getString("bsp") + ".png")
                        .placeholder(R.drawable.unknown_map)
                        .into(mapImage);

                // Description
                if (json.has("mapDesc")) {
                    mapDesc.setText(json.getString("mapDesc"));
                    RC.color(mapDesc);
                } else {
                    rowView.findViewById(R.id.lblDesc1).setVisibility(View.GONE);
                    mapDesc.setVisibility(View.GONE);
                    rowView.findViewById(R.id.spacer1).setVisibility(View.GONE);
                }

                // Time limit
                if (json.has("length") && Integer.parseInt(json.getString("length")) > 0) {
                    ((TextView) rowView.findViewById(R.id.mapTimelimit1))
                            .setText(String.format("%s minutes", json.getString("length")));
                } else {
                    rowView.findViewById(R.id.lblTimelimit1).setVisibility(View.GONE);
                    rowView.findViewById(R.id.mapTimelimit1).setVisibility(View.GONE);
                }

            } catch (JSONException e) {
                Log.e("CycleFragment_Adapter", "getView(): JSONException - " + e.toString());
                e.printStackTrace();
            }

            return rowView;
        }
    }

}