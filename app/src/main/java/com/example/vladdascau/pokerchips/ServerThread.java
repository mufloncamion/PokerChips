package com.example.vladdascau.pokerchips;

import java.net.Socket;

/**
 * Created by vlad.dascau on 6/13/2017.
 */
public class ServerThread extends Thread {
    private Socket socket;
    public ServerThread(Socket socket) {
        this.socket = socket;
    }
    public void run() {

    }
    public void otherMethod() {
        //Signal to the thread that it needs to do something (which should then be handled in the run method)
    }
}

