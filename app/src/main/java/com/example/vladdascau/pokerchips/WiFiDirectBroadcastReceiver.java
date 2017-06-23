package com.example.vladdascau.pokerchips;

/**
 * Created by vlad.dascau on 5/29/2017.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.*;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.*;

/**
 * A BroadcastReceiver that notifies of important Wi-Fi p2p events.
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver
{

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private MainActivity mActivity;
    private WifiP2pManager.PeerListListener playerz;

    private String gameName;
    private static final String DEFAULT_HOST_NAME = "PokerChips HOST";
    private String hostName = DEFAULT_HOST_NAME;
    private boolean amHost = false;

    private int initialAmount = 1000;

    ServerSocket serverSocket;
    private static final int HOST_PORT = 9020;

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
                                       MainActivity activity)
    {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mActivity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action))
        {
            // Check to see if Wi-Fi is enabled and notify appropriate activity
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED)
            {
                // Wifi P2P is enabled
                System.out.println("Wifi P2P enabled");
            } else
            {
                // Wi-Fi P2P is not enabled
                System.out.println("Wifi P2P not enabled");
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action))
        {
            // Call WifiP2pManager.requestPeers() to get a list of current peers
            // request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action))
        {
            // Respond to new connection or disconnections !

            if (mManager == null)
            {
                return;
            }
            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnected())
            {
                // we are connected with the other device, request connection
                // info to find group owner IP
                System.out.println("Connected to p2p network. Requesting network details");

                mManager.requestConnectionInfo(mChannel, new WifiP2pManager.ConnectionInfoListener()
                {
                    @Override
                    public void onConnectionInfoAvailable(WifiP2pInfo info)
                    {
                        InetAddress groupOwnerAddress = (info == null) ? null : info.groupOwnerAddress;
                        if (groupOwnerAddress == null) // Nothing to do
                            return;
                        if (info.isGroupOwner)
                        {
                            System.out.println("M-am conectat ca OWNER");
                            /* Daca sunt owner*/
                            try
                            {
                                GroupOwnerSocketHandler gosh = new GroupOwnerSocketHandler("HOST",HOST_PORT, initialAmount);
                                gosh.start();
                            } catch (Exception e)
                            {
                                System.out.println("Problem creating server socket !!");
                            }
                        } else
                        {
                            System.out.println("M-am conectat ca USER");
                            new ClientSocketHandler("User"+ Math.random() ,initialAmount, groupOwnerAddress, HOST_PORT);
                        }
                    }
                });
            } else
            {
                System.out.println("S-a intamplat un disconnect !");
            }

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action))
        {
            // Respond to this device's wifi state changing
        }
    }
}