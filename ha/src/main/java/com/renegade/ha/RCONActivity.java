package com.renegade.ha;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class RCONActivity extends ActionBarActivity {

    private EditText editText1, editText2, editText3, editText4;
//    private Button rconSend, rconClear;
    private final String RCON_PW = "3z3k!AL374";
    public InputMethodManager imm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rcon);
        editText1 = (EditText) this.findViewById(R.id.editText1);
        editText2 = (EditText) this.findViewById(R.id.editText2);
        editText3 = (EditText) this.findViewById(R.id.editText3);
        editText4 = (EditText) this.findViewById(R.id.editText4);
        imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_launcher);
        getSupportActionBar().setTitle("RCON");
//        rconSend.setOnClickListener(this);
//        rconClear.setOnClickListener(this);
//        String[] cmds = getResources().getStringArray(R.array.commands_array);
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, cmds);
//        rcon_command.setAdapter(adapter);
//        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_rcon, menu);
        return true;
    }

    static int numPass = 0;
    static String pass = "";

    public void passKeyClicked(final View view) {
        switch(numPass) {
            case 0:
                editText1.setText("1");
                pass += ((Button) view).getText();
                numPass++;
                break;
            case 1:
                editText2.setText("1");
                pass += ((Button) view).getText();
                numPass++;
                break;
            case 2:
                editText3.setText("1");
                pass += ((Button) view).getText();
                numPass++;
                break;
            case 3:
                editText4.setText("1");
                pass += ((Button) view).getText();
                numPass++;
                Toast.makeText(this, "Finished: " + pass, Toast.LENGTH_LONG).show();
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        clearPass(view);
                    }
                }, 1000);
                break;
        }
    }

    public void clearPass(View view) {
        pass = "";
        numPass = 0;
        editText1.setText("");
        editText2.setText("");
        editText3.setText("");
        editText4.setText("");
    }

    class RCONTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                // Generate the command
                String command = params[0];
                byte[] bufferOut = ("xxxxrcon \"" + RCON_PW + "\" " + command).getBytes();
                for (int i = 0; i <= 3; i++) {
                    bufferOut[i] = (byte) 0xff;
                }
                // Create socket
                InetAddress serverAddress = InetAddress.getByName("et.therenegadeclan.org");
                DatagramSocket socket = new DatagramSocket();

                // Send outbound packet
                DatagramPacket packetOut = new DatagramPacket(bufferOut, bufferOut.length, serverAddress, 27960);
                socket.send(packetOut);

                // Receive inbound packet
                byte[] bufferIn = new byte[65507];
                DatagramPacket packetIn = new DatagramPacket(bufferIn, bufferIn.length);
                socket.receive(packetIn);

                // Return response
                return new String(packetIn.getData());

            } catch (UnknownHostException e) {
                Log.e("RCONTask", "doInBackground(): UnknownHostException - " + e.toString());
                e.printStackTrace();
            } catch (IOException e) {
                Log.e("RCONTask", "doInBackground(): IOException - " + e.toString());
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }

    public void onClick(View view) {
        switch ( view.getId() ) {
            case R.id.rconSend:
                // Send RCON command
                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                break;
            case R.id.rconClear:
                // Clear RCON command
                imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
                break;
        }
    }


}
