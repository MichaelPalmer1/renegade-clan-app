package com.renegade.rc;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.view.Gravity.CENTER;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static android.widget.TextView.BufferType.SPANNABLE;

public class StatusFragment_Tabs_Cards extends Fragment {
    private TableRow rowNextMap;
    private TextView serverName, mapName, numPlayers, mod, nextMap;
    private ImageView mapImage;
    private SwipeRefreshLayout refreshStatus;
    public ArrayList<Player>
            axisPlayers     = new ArrayList<>(),
            alliedPlayers   = new ArrayList<>(),
            specPlayers     = new ArrayList<>(),
            connPlayers     = new ArrayList<>();
    public ListView listAxis, listAllied, listSpec, listConn;
    private Pattern patternSettings = Pattern.compile("\\\\([A-Za-z_]+)\\\\([^\\\\]+)");
    private Pattern patternPlayers = Pattern.compile("(\\d+) (\\d+) \"(.*)\"");
    public MenuItem btn_refresh;
    private ServerStatusTask statusTask = null;
    private DownloadJSONTask jsonTask = null;
    private static final int AXIS = 0, ALLIES = 1, SPECTATOR = 2, CONNECTING = 3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (statusTask != null && statusTask.getStatus() != ServerStatusTask.Status.FINISHED) {
            statusTask.cancel(true);
            statusTask = null;
        }
        if (jsonTask != null && jsonTask.getStatus() != DownloadJSONTask.Status.FINISHED) {
            jsonTask.cancel(true);
            jsonTask = null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_status_tabs, container, false);

        // Setup variables
        rowNextMap      = (TableRow)            rootView.findViewById(R.id.rowNextMap       );
        mapImage        = (ImageView)           rootView.findViewById(R.id.imgMap           );
        serverName      = (TextView)            rootView.findViewById(R.id.serverName       );
        mapName         = (TextView)            rootView.findViewById(R.id.mapName          );
        nextMap         = (TextView)            rootView.findViewById(R.id.nextMap          );
        numPlayers      = (TextView)            rootView.findViewById(R.id.numPlayers       );
        mod             = (TextView)            rootView.findViewById(R.id.mod              );
        listAxis        = (ListView)            rootView.findViewById(R.id.axisListView     );
        listAllied      = (ListView)            rootView.findViewById(R.id.alliedListView   );
        listSpec        = (ListView)            rootView.findViewById(R.id.specListView     );
        listConn        = (ListView)            rootView.findViewById(R.id.connListView     );
        refreshStatus   = (SwipeRefreshLayout)  rootView.findViewById(R.id.refreshStatus    );

        // Set on refresh listener for swipe refresh
        refreshStatus.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getServerStatus();
            }
        });
        refreshStatus.setColorSchemeColors(Color.RED, Color.BLUE, Color.GREEN);

        // Query the server
        getServerStatus();

        return rootView;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        btn_refresh = menu.findItem(R.id.action_refresh);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            item.setEnabled(false);
            refreshStatus.setRefreshing(true);
            getServerStatus();

            new Handler().postDelayed(new Runnable() {
                public void run() {
                    item.setEnabled(true);
                }
            }, 1500);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
     * Custom Methods
     */

    public static void setListViewHeight(ListView listview){
        ListAdapter listAdapter = listview.getAdapter();
        if(listAdapter == null)
            return;

        int totalHeight = 0;
        for(int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listview);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listview.getLayoutParams();
        params.height = totalHeight + (listview.getDividerHeight()*(listAdapter.getCount() - 1));
        listview.setLayoutParams(params);
    }

    private void getServerStatus() {
        if (statusTask != null && statusTask.getStatus() != ServerStatusTask.Status.FINISHED)
            statusTask.cancel(true);

        statusTask = (ServerStatusTask) new ServerStatusTask().execute();
    }

    private void processData(String inData) {
        // Split by new line
        String[] data = inData.split("\n");

        // Clear previous data
        Settings.clear();
        Teams.clear();

        // Search for matches
        int teamPos = 0;
        String teamInfo = "";
        for (int i = 1; i < data.length - 1; i++) {
            // Split by new lines
            String line = data[i];

            if (i == 1) {
                // Search for settings
                Matcher matchSettings = patternSettings.matcher(line);
                while (matchSettings.find())
                    Settings.set(matchSettings.group(1), matchSettings.group(2));

                // Save team information for processing
                if (Settings.get("P") != null)
                    teamInfo = Settings.get("P").replace("-", "");

            } else {
                // Create players matcher
                Matcher matchPlayers = patternPlayers.matcher(line);
                while (matchPlayers.find()) {
                    // Save player information
                    String name = matchPlayers.group(3);
                    String xp = matchPlayers.group(1);
                    String ping = matchPlayers.group(2);

                    // Assign player to a team
                    switch (teamInfo.charAt(teamPos++)) {
                        case '0':
                            Teams.Connecting.add(name, "", "");
                            break;
                        case '1':
                            Teams.Axis.add(name, xp, ping);
                            break;
                        case '2':
                            Teams.Allies.add(name, xp, ping);
                            break;
                        case '3':
                            Teams.Spectators.add(name, "", ping);
                            break;
                    }
                }
            }
        }

        // Fill server info heading
        fillInfo();

        // Fill player list
        fillPlayers();

        // Axis Adapter
        PlayerAdapter axisAdapter = new PlayerAdapter(getActivity(), R.layout.row_player, axisPlayers);
        listAxis.setAdapter(axisAdapter);
        setListViewHeight(listAxis);

        // Allied Adapter
        PlayerAdapter alliedAdapter = new PlayerAdapter(getActivity(), R.layout.row_player, alliedPlayers);
        listAllied.setAdapter(alliedAdapter);
        setListViewHeight(listAllied);

        // Spectator Adapter
        PlayerAdapter specAdapter = new PlayerAdapter(getActivity(), R.layout.row_player, specPlayers);
        listSpec.setAdapter(specAdapter);
        setListViewHeight(listSpec);

        // Connecting Adapter
        PlayerAdapter connAdapter = new PlayerAdapter(getActivity(), R.layout.row_player, connPlayers);
        listConn.setAdapter(connAdapter);
        setListViewHeight(listConn);

        // Close swipe refresh
        refreshStatus.setRefreshing(false);
    }

    private void fillInfo() {
        // Cancel any tasks that are already running
        if (jsonTask != null && jsonTask.getStatus() != DownloadJSONTask.Status.FINISHED)
            jsonTask.cancel(true);

        // Download map details
        jsonTask = (DownloadJSONTask) new DownloadJSONTask().execute
                ("http://www.therenegadeclan.org/getMapInfo.php?map=" + Settings.get("mapname"));

        // Map image
        Picasso.with(getActivity())
                .load("http://www.therenegadeclan.org/images/maps/"
                        + Settings.get("mapname").toLowerCase() + ".png")
                .placeholder(R.drawable.unknown_map)
                .into(mapImage);

        // Server name
        serverName.setText(Settings.get("sv_hostname"));
        RC.color(serverName);

        // Mod
        mod.setText(String.format("%s %s", Settings.get("gamename"), Settings.get("mod_version")));

        // Players
        numPlayers.setText(String.format("%d / %s", Teams.clients, Settings.get("sv_maxclients")));
    }

    private void fillPlayers() {
        TextView serverEmpty = (TextView) getActivity().findViewById(R.id.serverEmpty);
        ScrollView playerData = (ScrollView) getActivity().findViewById(R.id.scrollPlayerData);

        // Clear players
        for(int team = 0; team < 4; team++) {
            int startIndex = getStartIndex(team), numTeamRows = getTeamRows(team);
            if(numTeamRows > 0)
                getTeamTable(team).removeViews(startIndex, numTeamRows);

        }

        axisPlayers.clear();
        alliedPlayers.clear();
        specPlayers.clear();
        connPlayers.clear();

        // Check if clients are connected
        if (Teams.clients > 0) {
            // Hide server empty message and show player data row
            serverEmpty.setVisibility(GONE);
            playerData.setVisibility(VISIBLE);

            // Loop through the teams
            for (int team = 0; team < 4; team++) {
                int players = 0;
                String teamName = "";
                TextView textTeam   = new TextView(getActivity());
                TableRow rowTeam    = new TableRow(getActivity()),
                         rowHeader  = new TableRow(getActivity()),
                         rowList    = new TableRow(getActivity());

                switch (team) {
                    case AXIS:
                        teamName    = "Axis";
                        players     = Teams.Axis.players;
                        textTeam    = (TextView) getActivity().findViewById(R.id.textAxisTitle);
                        rowTeam     = (TableRow) getActivity().findViewById(R.id.rowAxisTitle);
                        rowHeader   = (TableRow) getActivity().findViewById(R.id.rowAxisHeader);
                        rowList     = (TableRow) getActivity().findViewById(R.id.axisListRow);
                        break;
                    case ALLIES:
                        teamName    = "Allies";
                        players     = Teams.Allies.players;
                        textTeam    = (TextView) getActivity().findViewById(R.id.textAlliesTitle);
                        rowTeam     = (TableRow) getActivity().findViewById(R.id.rowAlliesTitle);
                        rowHeader   = (TableRow) getActivity().findViewById(R.id.rowAlliesHeader);
                        rowList     = (TableRow) getActivity().findViewById(R.id.alliedListRow);
                        break;
                    case SPECTATOR:
                        teamName    = "Spectators";
                        players     = Teams.Spectators.players;
                        textTeam    = (TextView) getActivity().findViewById(R.id.textSpectatorsTitle);
                        rowTeam     = (TableRow) getActivity().findViewById(R.id.rowSpectatorsTitle);
                        rowHeader   = (TableRow) getActivity().findViewById(R.id.rowSpectatorsHeader);
                        rowList     = (TableRow) getActivity().findViewById(R.id.specListRow);
                        break;
                    case CONNECTING:
                        teamName    = "Connecting";
                        players     = Teams.Connecting.players;
                        textTeam    = (TextView) getActivity().findViewById(R.id.textConnectingTitle);
                        rowTeam     = (TableRow) getActivity().findViewById(R.id.rowConnectingTitle);
                        rowHeader   = (TableRow) getActivity().findViewById(R.id.rowConnectingHeader);
                        rowList     = (TableRow) getActivity().findViewById(R.id.connListRow);
                        break;
                }

                if (players > 0) {
                    rowTeam.setVisibility(VISIBLE);
                    rowHeader.setVisibility(VISIBLE);
                    rowList.setVisibility(VISIBLE);
                    textTeam.setText( String.format("%s (%d)", teamName, players) );
                } else {
                    if (team == AXIS || team == ALLIES) {
                        textTeam.setText(teamName);
                        rowHeader.setVisibility(GONE);
                        rowList.setVisibility(GONE);
                    } else {
                        rowTeam.setVisibility(GONE);
                        rowHeader.setVisibility(GONE);
                        rowList.setVisibility(GONE);
                    }
                }
                if (players > 0) {
                    for (int j = 0; j < players; j++) {
                        Player player = null;
                        switch (team) {
                            case AXIS:          player = Teams.Axis.get(j);         break;
                            case ALLIES:        player = Teams.Allies.get(j);       break;
                            case SPECTATOR:     player = Teams.Spectators.get(j);   break;
                            case CONNECTING:    player = Teams.Connecting.get(j);   break;
                        }

                        if (player != null)
                            addPlayer(player, team);
                    }

                } else {
                    if (team == AXIS || team == ALLIES) {
                        addCenteredText(team, "Team is empty", getStartIndex(team));
                    }
                }
            }
        } else {
            serverEmpty.setVisibility(VISIBLE);
            playerData.setVisibility(GONE);
        }
    }

    private TableLayout getTeamTable(int team) {
        switch(team) {
            case AXIS:      return (TableLayout) getActivity().findViewById(R.id.tableAxis);
            case ALLIES:    return (TableLayout) getActivity().findViewById(R.id.tableAllies);
            case SPECTATOR: return (TableLayout) getActivity().findViewById(R.id.tableSpectators);
            case CONNECTING:return (TableLayout) getActivity().findViewById(R.id.tableConnecting);
        }
        return (TableLayout) getActivity().findViewById(R.id.tableAxis);
    }

    private int getStartIndex(int team) {
        int rowIndex = 0;
        switch (team) {
            case AXIS:          rowIndex = getRowIndex(team, R.id.axisListRow);   break;
            case ALLIES:        rowIndex = getRowIndex(team, R.id.alliedListRow); break;
            case SPECTATOR:     rowIndex = getRowIndex(team, R.id.specListRow);   break;
            case CONNECTING:    rowIndex = getRowIndex(team, R.id.connListRow);   break;
        }
        return (rowIndex > -1) ? rowIndex + 1 : 0;
    }

    private int getTeamRows(int team) {
        int rowIndex = 0, startIndex = getStartIndex(team);
        switch (team) {
            case AXIS:          rowIndex = getRowIndex(team, R.id.axisSpacer);   break;
            case ALLIES:        rowIndex = getRowIndex(team, R.id.alliedSpacer); break;
            case SPECTATOR:     rowIndex = getRowIndex(team, R.id.specSpacer);   break;
            case CONNECTING:    rowIndex = getRowIndex(team, R.id.connSpacer);   break;
        }
        return (rowIndex - startIndex > -1) ? rowIndex - startIndex : 0;
    }

    private int getRowIndex(int team, int resourceId) {
        TableLayout teamTable = getTeamTable(team);
        for (int i = 0; i < teamTable.getChildCount(); i++) {
            TableRow row = (TableRow) teamTable.getChildAt(i);
            if (row.getId() == resourceId)
                return i;
        }
        return 0;
    }

    private void addCenteredText(int team, String text, int startIndex) {
        TableRow tableRow = new TableRow(getActivity());
        TextView textView = new TextView(getActivity());
        TableRow.LayoutParams params = new TableRow.LayoutParams(0, WRAP_CONTENT);
        params.column = 0;
        params.span = 3;
        params.weight = 0.0f;
        params.gravity = CENTER;
        tableRow.setGravity(CENTER);
        textView.setText(text);
        tableRow.addView(textView, params);
        addRow(team, tableRow, startIndex);
    }

    private void addPlayer(Player player, int team) {
        int nameSpan = 1;
        boolean showXP = true, showPing = true;
        float nameWeight = 0.65f;

        switch(team) {
            case SPECTATOR:
                showXP = false;
                break;
            case CONNECTING:
                showXP = false;
                showPing = false;
                break;
        }

        if (!showXP) {
            nameSpan++;
            nameWeight += 0.25f;
        }

        if (!showPing) {
            nameSpan++;
            nameWeight = 0.0f;
        }

        TableRow tableRow = new TableRow(getActivity());
        for (int col = 0; col < 3; col++) {
            TableRow.LayoutParams params = new TableRow.LayoutParams(0, WRAP_CONTENT);
            TextView textView = new TextView(getActivity());
            textView.setSingleLine(true);

            switch (col) {
                case 0: // Name
                    textView.setText(player.name, SPANNABLE);
                    RC.color(textView);
                    params.column = 0;
                    params.span = nameSpan;
                    params.weight = nameWeight;
                    tableRow.addView(textView, params);
                    break;
                case 1: // XP
                    textView.setText(player.xp);
                    params.column = 1;
                    params.span = 1;
                    params.weight = 0.25f;
                    if(showXP) tableRow.addView(textView, params);
                    break;
                case 2: // Ping
                    textView.setText(player.ping);
                    params.column = 2;
                    params.span = 1;
                    params.weight = 0.10f;
                    if(showPing) tableRow.addView(textView, params);
                    break;
            }
        }
        switch(team) {
            case AXIS:
                axisPlayers.add(player);
                break;
            case ALLIES:
                alliedPlayers.add(player);
                break;
            case SPECTATOR:
                specPlayers.add(player);
                break;
            case CONNECTING:
                connPlayers.add(player);
                break;
        }
    }

    private void addRow(int team, TableRow tableRow, int startIndex) {
        TableLayout.LayoutParams params = new TableLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        tableRow.setBackgroundColor(
                getResources().getColor(R.color.primary_text_default_material_dark));
        getTeamTable(team).addView(tableRow, startIndex, params);
    }

    private void processMapDetails(String data) {
        try {
            // Create JSON object
            JSONObject json = new JSONObject(data);

            // Set map name
            if (json.has("map")) {
                mapName.setText(json.getString("map"));
                RC.color(mapName);
            } else {
                mapName.setText(Settings.get("mapname"));
            }

            // Set next map
            if (Settings.get("g_gametype").equals("2") && json.has("next")) {
                rowNextMap.setVisibility(View.VISIBLE);
                nextMap.setText(json.getString("next"));
                RC.color(nextMap);
            } else {
                rowNextMap.setVisibility(View.GONE);
            }

        } catch (Exception e) {
            Log.e("StatusFragment_Tabs", "processMapDetails(): " + e.getClass().getSimpleName() + " - " + e.toString());
            e.printStackTrace();
        }
    }

    private void statusError(String message) {
        statusTask.cancel(true);
        refreshStatus.setRefreshing(false);
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    private class DownloadJSONTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

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
                Log.e("DownloadJSON_Status", "Error encountered while downloading JSON");
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
            if (result != null && result.length() > 0) {
                processMapDetails(result);
            } else {
                mapName.setText(Settings.get("mapname") );
            }
        }
    }

    private class ServerStatusTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            refreshStatus.setRefreshing(true);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                // Generate the command
                byte[] bufferOut = "xxxxgetstatus".getBytes();
                for (int i = 0; i <= 3; i++) {
                    bufferOut[i] = (byte) 0xff;
                }
                // Create socket
                InetAddress serverAddress = InetAddress.getByName(RC.SERVER_IP);
                DatagramSocket socket = new DatagramSocket();

                // Send outbound packet
                DatagramPacket packetOut = new DatagramPacket(bufferOut, bufferOut.length, serverAddress, RC.SERVER_PORT);
                socket.send(packetOut);

                // Receive inbound packet
                byte[] bufferIn = new byte[65507];
                DatagramPacket packetIn = new DatagramPacket(bufferIn, bufferIn.length);
                socket.receive(packetIn);

                // Return server info
                return new String(packetIn.getData());

            } catch (UnknownHostException e) {
                statusError("No network connection");
            } catch (IOException e) {
                statusError("Could not receive data from the RC server");
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            // Check if the action was canceled
            if (isCancelled()) {
                result = null;
            }

            // Check the result
            if (result != null) {
                processData(result);
            } else {
                statusError("Could not contact the RC server");
            }
        }
    }

    private class PlayerAdapter extends ArrayAdapter<Player> {
        private final Context context;
        private final int resourceID;
        private final ArrayList<Player> player_details;

        public PlayerAdapter(Context context, int resource, ArrayList<Player> list) {
            super(context, resource, list);

            this.player_details = list;
            this.context = context;
            this.resourceID = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(resourceID, parent, false);
            TextView playerName = (TextView) rowView.findViewById(R.id.playerName);
            TextView playerXP = (TextView) rowView.findViewById(R.id.playerXP);
            TextView playerPing = (TextView) rowView.findViewById(R.id.playerPing);
            Player player = player_details.get(position);

            // Name
            playerName.setText(player.name);
            RC.color(playerName);

            // XP
            playerXP.setText(player.xp);

            // Ping
            playerPing.setText(player.ping);

            return rowView;
        }
    }

}