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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public EditText ipText;
    public EditText portText;
    public String iptxt;
    public String  porttxt;
    public Socket sock;
    public byte[] response;


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

    public void processTask(View v){
        EditText opt = (EditText) findViewById(R.id.optText);
        opt.setText("");
        String server_addr=ipText.getText().toString();
        int port = Integer.parseInt(portText.getText().toString());
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        String msg = String.valueOf(spinner.getSelectedItem());
        switch(msg) {
            case "NACK1":
                Task1(msg,opt,server_addr,port);
                break;
            case "NACK2":
                Task2(msg,opt,server_addr,port);
                break;
            case "NACK3":
                Task3(msg,opt,server_addr,port);
                break;/*
            case "NACK4":
                Task4(msg,opt,server_addr,port);
                break;
            case "NACK5":
                Task5(msg,opt,server_addr,port);
            case "NACK6":
                Task6(msg,opt,server_addr,port);
                */
        }
    }

    public void Task1(String msg, final EditText opt,String ip, int port){
        System.out.println("Request Sent");

        new clientSocket(ip,port,msg,new clientSocket.AsyncResponse(){

            @Override
            public void processFinish(byte[] output){
                //Here you will receive the result fired from async class
                //of onPostExecute(result) method.
                String result = new String(output);
                opt.append("ASCII String Received: " + result);

            }
        }).execute();

    }

    public void Task2(String msg, final EditText opt,String ip, int port){
        new clientSocket(ip,port,msg,new clientSocket.AsyncResponse(){

            @Override
            public void processFinish(byte[] data){
                //Here you will receive the result fired from async class
                //of onPostExecute(result) method.
                String result = new String(data);
                //calculate CRC as byte
                byte crc = CRC8(data);
                //convert to unsigned int to compare with original CRC
                int c = crc & 0xFF;
                opt.append("ASCII String Received: " + result +"\n");
                opt.append("CRC of Original String: " + data[data.length - 1] + "\n");
                opt.append("Calculated CRC: " + c);

            }
        }).execute();

    }

    public void Task3(final String msg, final EditText opt, final String ip, final int port){
        new clientSocket(ip,port,msg,new clientSocket.AsyncResponse(){

            @Override
            public void processFinish(byte[] data){
                //Here you will receive the result fired from async class
                //of onPostExecute(result) method.
                String result = new String(data);
                final ArrayList<int[]> dataList = new ArrayList<>();
                int[] response = getIntArray(data);
                {
                    if(checkCRC(response)){
                        opt.append("ASCII String received correctly: \n");
                        opt.append(result);
                    }
                    else
                    {
                        new clientSocket(ip,port,msg,new clientSocket.AsyncResponse(){

                            @Override
                            public void processFinish(byte[] data){

                            }
                        }).execute();
                    }
                }

            }
        }).execute();
    }

    protected int[] getIntArray(byte[] data){
        int[] newData = new int[data.length*8];
        bytetobin(data,newData);
        return newData;
    }

    protected boolean checkCRC(int[] data)
    {
        int[] newData = new int[data.length-8];
        int[] dataCRC = new int[8];
        for(int i=0; i<newData.length;i++)
            newData[i]=data[i];
        for(int i=0; i<dataCRC.length;i++)
            dataCRC[i]=data[data.length-8+i];
        int[] calcCRC = intArrayCRC(newData);
        return Arrays.equals(dataCRC,calcCRC);
    }

    protected  int[] intArrayCRC(int[] data){
        int[] generatorRemainder = {0,0,0,0,0,1,1,1};    //First bit is omitted
        // A popular variant complements remainderPolynomial here
        int[] remainderPolynomial = {0,0,0,0,0,0,0,0};
        int[] temp = new int[8];
        for (int i=0; i< data.length-1; i+=8) {
            for(int j=0; j<8; j++)
                temp[j] = data[i+j];
            remainderPolynomial = xor(temp,remainderPolynomial);
            for(int j=0; j<8; j++)
            {    //  8 bits per byte
                if ((remainderPolynomial[0] & 1)!=0) { // AND 0b10000000
                    remainderPolynomial  = xor(lfs(remainderPolynomial), generatorRemainder);
                } else {
                    remainderPolynomial  = lfs(remainderPolynomial);
                }
            }
        }
        // A popular variant complements remainderPolynomial here
        return remainderPolynomial;
    }

    protected byte CRC8(byte[] data){
        byte generatorRemainder = (byte)0b00000111;    //First bit is omitted
        // A popular variant complements remainderPolynomial here
        byte remainderPolynomial = 0;
        for (int i=0; i< data.length-1; i+=1) {
            remainderPolynomial = (byte) (data[i]^remainderPolynomial);
            for(int j=0; j<8; j++)
            {    //  8 bits per byte
                if ((remainderPolynomial& 0b10000000)!=0) { // AND 0b10000000
                    remainderPolynomial  = (byte)((remainderPolynomial << 1)^generatorRemainder);
                } else {
                    remainderPolynomial  = (byte)(remainderPolynomial << 1);
                }
            }
        }
        // A popular variant complements remainderPolynomial here
        return remainderPolynomial;
    }

    protected int[] compareAndGenerate(int[] first, int[] second)
    {
        int[] diff = xor(first, second);
        ArrayList<Integer> differ = new ArrayList<Integer>();
        for(int i=0; i<diff.length; i++)
        {
            if(1==diff[i])
                differ.add(i);
        }

        return null;
    }


        protected int[] permuAllDiffers(ArrayList<Integer> differs, int[] message,int index){
            if(differs.size() < index)
            {
                return null;
            }
            int i= differs.get(index);
            if(true == checkCRC(message)){
                return message;
            }
            else{
                if(0 == message[i])
                    message[i]=1;
                else if(1 == message[i])
                    message[i]=0;
            }
            return null;
        }

    protected int[] xor(int[] arg1, int[] arg2)
    {
        if(arg1.length != arg2.length){
            System.out.println("Arguments have different size! Pls check!");
            return null;
        }
        int[] result = new int[arg1.length];
        for(int i=0; i<arg1.length; i++)
        {
            result[i] = arg1[i]^arg2[i];
        }
        return result;
    }

    protected int[] lfs(int[] param)
    {
        int[] temp = new int[param.length];
        for(int i=0; i<param.length;i++){
            if(param.length-1 == i) {
                temp[i] = 0;
                break;
            }
            temp[i]=param[i+1];
        }
        return temp;
    }

    public static void bytetobin(byte[] b,int[] result)
    {
        int size = b.length;
        for(int i=0; i<b.length;i++)
        {
            int[] temp = new int[8];
            deci2bin((int)b[i],8,temp);
            for(int j=0; j<8; j++)
                result[i*8+j]=temp[j];
        }
    }

    public static void deci2bin(int d, int size, int []b)
    {
        b[size-1] = d&0x01;
        for (int i = size - 2; i >= 0; i--) {
            d = d >> 1;
            b[i] = d & 0x01;
        }
    }

}
