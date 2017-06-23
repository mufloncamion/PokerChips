package com.example.vladdascau.pokerchips;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by vlad.dascau on 6/23/2017.
 */
public class PlayerManager
{

    private static PlayerManager instance = null;
    private static final int MAX_PLAYERS = 5;
    HashMap<String,Player> players = new HashMap<String,Player>();

    protected PlayerManager()
    {
    }

    int getNumberOfPlayers()
    {
        return players.size();
    }

    public static PlayerManager getInstance()
    {
        if (instance == null)
        {
            instance = new PlayerManager();
        }
        return instance;
    }

    public boolean addPlayer(Player player)
    {
        if (players.containsKey(player.userName))
        {
            System.out.println("User "+player.userName + " already exists !");
            return false;
        }
        if (getNumberOfPlayers() >= MAX_PLAYERS)
        {
            System.out.println("Maximum number of players already reached !");
            return false;
        }

        players.put(player.userName,player);
        return true;
    }

    public boolean deletePlayer(Player player)
    {
        if (!players.containsKey(player.userName))
        {
            System.out.println("User "+player.userName + " does not exist !");
            return false;
        }
        if ( getNumberOfPlayers() <= 1 )
        {
            System.out.println("Cannot delete user " + player.userName + "! Minimum number of players is 1 !");
            return false;
        }
        if ( player.isHost() )
        {
            System.out.println("Cannot delete player " + player.userName + " since he is the host !");
            return false;
        }

        players.remove(player);
        return true;
    }


}
