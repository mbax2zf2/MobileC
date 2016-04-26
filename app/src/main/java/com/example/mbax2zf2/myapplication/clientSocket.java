package com.example.mbax2zf2.myapplication;

import android.app.Activity;
import android.os.AsyncTask;
import android.widget.EditText;

import java.lang.reflect.Array;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mbax2zf2 on 16/4/26.
 */
public class clientSocket  extends AsyncTask<Void, Void, String>{
    private String ip;
    private int portNo;
    private String sentence;
    private Activity act;
    private byte[] resp;
    public AsyncResponse delegate = null;


    public interface AsyncResponse {
        void processFinish(byte[] output);
    }

    public clientSocket(String ipAddr, int port, String msg, AsyncResponse delegate)
    {
        super();
        this.ip=ipAddr;
        this.portNo=port;
        this.sentence=msg;
        this.delegate = delegate;
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
        byte[] data = resp.getBytes();
        delegate.processFinish(data);

    }

    protected byte[] getResp(){
        return this.resp;
    }

}
