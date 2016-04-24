package com.example.mbax2zf2.myapplication;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public EditText ipText;
    public EditText portText;
    public String iptxt;
    public String  porttxt;
    public Socket sock;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ipText = (EditText) findViewById(R.id.ipView);
        portText = (EditText) findViewById(R.id.portView);
        ipText.setOnClickListener(this);
        portText.setOnClickListener(this);
        iptxt = ipText.getText().toString();
        porttxt = portText.getText().toString();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v)
    {
        System.out.println(ipText.getText());
        if(R.id.ipView == v.getId() && ipText.getText().toString().equals(iptxt))
            ipText.setText("");
        if(R.id.portView == v.getId() && portText.getText().toString().equals(porttxt))
            portText.setText("");
    }

    public void sendMsg(View v)
    {
        System.out.println("Button Clicked");
        String server_addr=ipText.getText().toString();
        int port = Integer.parseInt(portText.getText().toString());
        new clientSockets(server_addr,port).execute();

    }

    class clientSockets extends AsyncTask<Void, Void, String> {
        private String ip;
        private int portNo;

        public clientSockets(String ipAddr, int port)
        {
            super();
            this.ip=ipAddr;
            this.portNo=port;
        }

        @Override
        protected String doInBackground(Void... params){
            String response = "";
            try {
                DatagramSocket clientSocket = new DatagramSocket();
                InetAddress IPAddress = InetAddress.getByName(ip);
                byte[] sendData = new byte[1024];
                byte[] receiveData = new byte[1024];
                String sentence = "NACK1";
                sendData = sentence.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, portNo);
                clientSocket.send(sendPacket);
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                clientSocket.receive(receivePacket);
                response = new String(receivePacket.getData());
                System.out.println("FROM SERVER:" + response);
                clientSocket.close();
            }
            catch (Exception e){
                System.err.println(e);
            }
            return response;
        }

        @Override
        protected void onPostExecute(String resp){
            System.out.println("Task finished");
        }
    }
}
