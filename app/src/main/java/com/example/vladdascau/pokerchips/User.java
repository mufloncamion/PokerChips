package com.example.vladdascau.pokerchips;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by vlad.dascau on 5/23/2017.
 */
public class User extends Thread implements Runnable{
    protected String userName;
    protected Socket socket;
    protected DataOutputStream dataOutputStream;
    protected DataInputStream dataInputStream;
    protected InetAddress hostAddress;
    protected int hostPort;

    public void getStreams() throws IOException
    {
        if ( socket != null ) {
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataInputStream = new DataInputStream(socket.getInputStream());
            System.out.println("Success getting Input and Output Streams");
        }
        else
            System.out.println("Socket is null cannot retrieve input and outputstreams");

    }
    /*  User to connect to server from client */
    public void run() {
        try {
            socket = new Socket(hostAddress, hostPort);
            System.out.println("Am deschis socketul pe client cu host-ul "+hostAddress);
            getStreams();
        } catch (IOException e) {
            System.out.println("Exceptie cand fac socketul !!");
            e.printStackTrace();
        }
    }

    public User(String userName, InetAddress hostAddress, int hostPort) {
        this.userName = userName;
        this.hostAddress = hostAddress;
        this.hostPort = hostPort;
        System.out.println("Apelez constructorul pentru user");
    }
    /* =============================================== */

    /* Used to create the host */
    public  User(String userName) {
        this.userName = userName;
        System.out.println(" Apelez constructorul pentru server si creez userul " + userName);

    }
    /* */
    public void writeToStream(String message) {
        if (dataOutputStream == null) {
            System.out.println("DataOutputStream not initialized");
            return;
        }
        try {
            dataOutputStream.writeUTF(message);
            dataOutputStream.flush();
        }
        catch (Exception e)
        {
            System.out.println("Exceptie cand vreau sa scriu !");
        }
    }

    public String readFromStream() {
        String buf = new String();
        if (dataInputStream == null) {
            return "DataInputStream not initialized";
        }
        try {
            while((buf = dataInputStream.readLine()) != null)
            {
                System.out.println("A venit : "+ buf);
            }
            return buf;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return (buf.isEmpty())?"String null":buf;
    }


}
