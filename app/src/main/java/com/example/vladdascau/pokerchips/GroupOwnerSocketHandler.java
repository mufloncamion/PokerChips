package com.example.vladdascau.pokerchips;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by vlad.dascau on 6/23/2017.
 */
public class GroupOwnerSocketHandler extends Thread implements Runnable {
    private static boolean serverSocketAlreadyOpen = false;
    ServerSocket serverSocket = null;
    private Player host;
    private int hostPort;
    private String hostName;

    private long amount;
    GroupOwnerSocketHandler(String hostName, int port, long initialAmount) {
        hostPort = port;
        amount = initialAmount;
        this.hostName = hostName;
    }

    public void run()
    {
        try {
            if (serverSocketAlreadyOpen) {
                System.out.println("Server socket already open !");
                return;
            }
            serverSocketAlreadyOpen = true;
            int currentNoOfPlayers = PlayerManager.getInstance().getNumberOfPlayers();
            serverSocket = new ServerSocket(hostPort);
            host = new Player(hostName, amount);
            PlayerManager.getInstance().addPlayer(host);

            System.out.println("Apelez accept !");
            Socket client = serverSocket.accept();

        } catch (IOException e) {
            System.out.println("Nu a mers sa deschid serversocket in threadul meu !");
            e.printStackTrace();
        }

    }

}
