package com.example.vladdascau.pokerchips;

import java.net.InetAddress;

/**
 * Created by vlad.dascau on 6/23/2017.
 */
public class ClientSocketHandler {

    ClientSocketHandler(String userName, long initialAmount, InetAddress groupOwnerAddress, int hostPort)
    {
        try {
            int currentNoOfPlayers = PlayerManager.getInstance().getNumberOfPlayers();
            Player player = new Player(userName, initialAmount, groupOwnerAddress, hostPort);
            player.start();
            PlayerManager.getInstance().addPlayer(player);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }
}
