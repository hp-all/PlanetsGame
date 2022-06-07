package main;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.locks.*;

public class ReadingRunnable implements Runnable{
    public static final int PORT = 4000;
    private Socket soc;
    private BufferedReader bufRe;
    private String line = "nothing";
    private boolean restart = false;
    public ReadingRunnable(String hostName) {
        try {
            soc = new Socket(hostName, PORT);
            bufRe = new BufferedReader(new InputStreamReader(soc.getInputStream()));
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    @Override
    public void run() {
        while(true) {
            try {
                line = bufRe.readLine();
            } catch (IOException e) {
                restart = true;
            }
        }
    }
    synchronized String getLine() {
        String s = line;
        return s;
    }
    synchronized boolean shouldRestart() {
        return restart;
    }
    public void close()
    {
        try {
            bufRe.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
