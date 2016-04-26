package com.example.mbax2zf2.myapplication;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Spinner;

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
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        String msg = String.valueOf(spinner.getSelectedItem());
        new clientSockets(server_addr,port,msg).execute();


    }

    class clientSockets extends AsyncTask<Void, Void, String> {
        private String ip;
        private int portNo;
        private String sentence;

        public clientSockets(String ipAddr, int port, String msg)
        {
            super();
            this.ip=ipAddr;
            this.portNo=port;
            this.sentence=msg;
        }

        @Override
        protected String doInBackground(Void... params){
            String response = "";
            try {
                DatagramSocket clientSocket = new DatagramSocket();
                InetAddress IPAddress = InetAddress.getByName(ip);
                byte[] sendData;
                byte[] receiveData = new byte[1024];
                sendData = sentence.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, portNo);
                clientSocket.send(sendPacket);
                clientSocket.setSoTimeout(3000);
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                clientSocket.receive(receivePacket);
                byte[] data = new byte[receivePacket.getLength()];
                System.arraycopy(receivePacket.getData(), receivePacket.getOffset(), data, 0, receivePacket.getLength());
                response = new String(data);
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
            EditText opt = (EditText) findViewById(R.id.optText);
            byte[] data = resp.getBytes();
            byte crc = CRC8(data);
            int c = crc & 0xFF;
            opt.setText("");
            opt.append("ASCII string received: " + resp + "\n");
            opt.append("CRC of original string: " + data[data.length-1] + "\n");
            opt.append("CRC of received string without CRC: " + c + "\n");
        }

        protected byte CRC8(byte[] data){
            byte crc = data[data.length-1];
            byte generatorRemainder = 0b00000111;    //First bit is omitted
            // A popular variant complements remainderPolynomial here
            byte remainderPolynomial = 0x00;
            for (int i=0; i< data.length-1; i++) {
                remainderPolynomial = (byte) (data[i]^remainderPolynomial);
                for(int j=0; j<8; j++)
                {    //  8 bits per byte
                    if ((remainderPolynomial&0b10000000)!=0) {
                        remainderPolynomial  = (byte)((remainderPolynomial << 1)^generatorRemainder);
                    } else {
                        remainderPolynomial  = (byte)(remainderPolynomial << 1);
                    }
                }
            }
            // A popular variant complements remainderPolynomial here
            return remainderPolynomial;
        }


        public void deci2bin(int d, int size, int []b)
        {
            b[size-1] = d&0x01;
            for (int i = size - 2; i >= 0; i--) {
                d = d >> 1;
                b[i] = d & 0x01;
            }
        }

        public int bin2deci(int []b, int size)
        {
            int i, d=0;
            for (i = 0; i < size; i++)
                d += b[i] << (size - i - 1);
            return(d);
        }
    }
}
