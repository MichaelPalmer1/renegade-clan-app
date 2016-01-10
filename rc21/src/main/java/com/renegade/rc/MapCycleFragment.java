package com.renegade.rc;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.io.InputStream;

import android.app.ProgressDialog;
import android.content.Context;
import android.app.Activity;
import android.app.Fragment;
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
import android.widget.TextView;
import android.widget.ListView;

//import com.google.android.gms.analytics.HitBuilders;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

public class MapCycleFragment extends Fragment {
    public ArrayList<JSONObject> mapList = new ArrayList<JSONObject>();
    public ListView listMapCycle;
    private ProgressDialog progressDialog;
    private DownloadJSONTask dlTask = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

//    @Override
//    public void onStart() {
//        super.onStart();
//        ((RCApp) getActivity().getApplication())
//                .getTracker(RCApp.TrackerName.APP_TRACKER)
//                .setScreenName("com.renegade.rc.MapCycleFragment");
//
//        ((RCApp) getActivity().getApplication())
//                .getTracker(RCApp.TrackerName.APP_TRACKER)
//                .send(new HitBuilders.AppViewBuilder().build());
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_mapcycle, container, false);
        listMapCycle = (ListView) rootView.findViewById(R.id.listMapCycle);
        progressDialog = new ProgressDialog(getActivity());

        if (dlTask != null && dlTask.getStatus() != DownloadJSONTask.Status.FINISHED) {
            dlTask.cancel(true);
        }
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
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(MainActivity.CYCLE);
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
            Log.d("MapData", data);

            JSONObject json = new JSONObject(data);
            JSONObject maps = json.getJSONObject("maps");
            JSONArray cycle = json.getJSONArray("cycle");

            for(int i = 0; i < cycle.length(); i++) {
                mapList.add( maps.getJSONObject( cycle.getString(i) ) );
            }

            CustomAdapter adapter = new CustomAdapter(getActivity(), R.layout.mapdetail, mapList);
            listMapCycle.setAdapter(adapter);

        } catch (JSONException e) {
            Log.e("MapCycle", "JSON Exception: " + e.toString());
            e.printStackTrace();
        }
    }

    private class DownloadJSONTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            progressDialog.setTitle("Please wait");
            progressDialog.setMessage("Fetching map cycle...");
            progressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... urls) {
            /*
            String response = "";
            DefaultHttpClient client = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(urls[0]);
            try {
                HttpResponse execute = client.execute(httpGet);
                InputStream content = execute.getEntity().getContent();

                BufferedReader buffer = new BufferedReader( new InputStreamReader(content) );
                String s;
                while ( (s = buffer.readLine()) != null ) {
                    response += s;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return response;
            */
            return "";
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
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(resourceID, parent, false);
            TextView mapName = (TextView) rowView.findViewById(R.id.mapName);
//            TextView mapNumber = (TextView) rowView.findViewById(R.id.mapNumber);
            TextView mapDesc = (TextView) rowView.findViewById(R.id.mapDesc);
            ImageView mapImage = (ImageView) rowView.findViewById(R.id.mapImage);
//            ListView listAxisObjs = (ListView) rowView.findViewById(R.id.listAxisObjs);
//            ListView listAlliedObjs = (ListView) rowView.findViewById(R.id.listAlliedObjs);
            try {
                JSONObject json = map_details.get(position);
                // Name
                mapName.setText(json.getString("map"));
                RC.color(mapName);

                // Image
                Picasso.with(getActivity())
                        .load("http://www.therenegadeclan.org/images/maps/"
                                + json.getString("bsp") + ".png")
                        .placeholder(R.drawable.unknown_map)
                        .into(mapImage);

                // Number
//                mapNumber.setText(position + 1 + ".");

                // Description
                if (json.has("mapDesc")) {
                    mapDesc.setText(json.getString("mapDesc"));
                    RC.color(mapDesc);
                } else {
                    rowView.findViewById(R.id.lblDesc).setVisibility(View.GONE);
                    mapDesc.setVisibility(View.GONE);
                    rowView.findViewById(R.id.spacer).setVisibility(View.GONE);
                }

                // Time limit
                if (json.has("length") && Integer.parseInt(json.getString("length")) > 0) {
                    ((TextView) rowView.findViewById(R.id.mapTimelimit)).setText(
                            String.format("%s minutes", json.getString("length")));
                } else {
                    rowView.findViewById(R.id.lblTimelimit).setVisibility(View.GONE);
                    rowView.findViewById(R.id.mapTimelimit).setVisibility(View.GONE);
                }
                /*
                // Axis objectives
                if(json.has("objAxis")) {
                    ArrayList<String> axisObjs = RC.JSONFunctions.toArrayList( json.getJSONArray("objAxis") );
                    ArrayAdapter<String> axisAdapter = new ArrayAdapter<>(
                            getActivity(), R.layout.row, R.id.listEntry, axisObjs);
                    listAxisObjs.setAdapter(axisAdapter);
                }

                // Allied objectives
                if(json.has("objAllied")) {
                    ArrayList<String> alliedObjs = RC.JSONFunctions.toArrayList( json.getJSONArray("objAllied") );
                    ArrayAdapter<String> alliedAdapter = new ArrayAdapter<>(
                            getActivity(), R.layout.row, R.id.listEntry, alliedObjs);
                    listAlliedObjs.setAdapter(alliedAdapter);
                }
                */
            } catch (JSONException e) {
                Log.e("MapCycle_CustomAdapter", "getView(): JSONException - " + e.toString());
                e.printStackTrace();
            }

            return rowView;
        }
    }

}