package com.renegade.rc;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.RemoteViews;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Implementation of App Widget functionality.
 */
public class StatusWidget extends AppWidgetProvider {

    private ServerInfoTask infoTask = null;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        if ( infoTask != null && infoTask.getStatus() != ServerInfoTask.Status.FINISHED )
            infoTask.cancel(true);

        infoTask = (ServerInfoTask) new ServerInfoTask().execute();

        long startTime = System.currentTimeMillis();
        while(infoTask.getStatus() == ServerInfoTask.Status.RUNNING) {
            // break if stuck for 3 seconds
            if( System.currentTimeMillis() - startTime > 3000) {
				Log.e("StatusWidget", "Task stuck for 3+ seconds");
				break;
			}
        }

        // There may be multiple widgets active, so update all of them
        for ( int appWidgetId : appWidgetIds )
            updateAppWidget(context, appWidgetManager, appWidgetId);

    }

	@Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
		super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        if (infoTask != null && infoTask.getStatus() != ServerInfoTask.Status.FINISHED) {
            infoTask.cancel(true);
            infoTask = null;
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.status_widget);

        String mapText = Info.get("mapname") != null ? "Map: " + Info.get("mapname") : "Map: ---";
        String playerText = Info.get("clients") != null ?
                "Players: " + Info.get("clients") + " / " + Info.get("sv_maxclients")
						+ " (" + System.currentTimeMillis() + ")"
                : "Players: ---";

		Intent intent = new Intent(context, StatusFragment_Tabs_Cards.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        views.setTextViewText(R.id.widgetMap, mapText);
        views.setTextViewText(R.id.widgetPlayers, playerText);
		views.setOnClickPendingIntent(R.id.button, pendingIntent);

		// Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private class ServerInfoTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            try {
                // Generate the command
                byte[] bufferOut = "xxxxgetinfo".getBytes();
                for (int i = 0; i <= 3; i++)
                    bufferOut[i] = (byte) 0xff;

                // Create socket
                InetAddress serverAddress = InetAddress.getByName(RC.SERVER_IP);
                DatagramSocket socket = new DatagramSocket();

                // Send outbound packet
                DatagramPacket packetOut = new DatagramPacket(bufferOut, bufferOut.length,
						serverAddress, RC.SERVER_PORT);
                socket.send(packetOut);

                // Receive inbound packet
                byte[] bufferIn = new byte[1041];
                DatagramPacket packetIn = new DatagramPacket(bufferIn, bufferIn.length);
                socket.receive(packetIn);

                // Return server info
                return new String(packetIn.getData());

            } catch (UnknownHostException e) {
                Log.e("StatusWidget", "doInBackground(): UnknownHostException - " + e.toString());

            } catch (IOException e) {
                Log.e("StatusWidget", "doInBackground(): IOException - " + e.toString());
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            // Check if the action was canceled
            if ( isCancelled() )
                result = null;

            // Check the result
            if ( result != null ) {
                // Remove response header
                String data = result.split("\n")[1];

                // Search for info
                Matcher matchInfo = Pattern.compile("\\\\([A-Za-z_]+)\\\\([^\\\\]+)").matcher(data);
                while ( matchInfo.find() )
                    Info.set(matchInfo.group(1), matchInfo.group(2));

            } else {
                Log.e("StatusWidget", "onPostExecute(): Error querying server - result == null");
            }
        }
    }

}

