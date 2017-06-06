package com.example.vladdascau.pokerchips;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int MAX_PLAYERS = 3;
    private static final int HOST_PORT = 9020;
    private static final int SOCKET_TIMEOUT = 2000;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;
    private WifiP2pManager.PeerListListener peers;
    private int initialAmount = 1000;
    private Player players[];
    private int currentNoOfPlayers = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

    }

    public void search(View v) {
        /* Open a new activity where a list of games is displayed */
    }

    public void host_service(View v) {
        //  Create a string map containing information about your service.
        Map record = new HashMap();
        record.put("listenport", String.valueOf(HOST_PORT));
        record.put("buddyname", "John Doe" + (int) (Math.random() * 1000));
        record.put("available", "visible");

        // Service information.  Pass it an instance name, service type
        // _protocol._transportlayer , and the map containing
        // information other devices will want once they connect to this one.
        WifiP2pDnsSdServiceInfo serviceInfo =
                WifiP2pDnsSdServiceInfo.newInstance("_test", "_presence._tcp", record);

        // Add the local service, sending the service info, network channel,
        // and listener that will be used to indicate success or failure of
        // the request.
        mManager.addLocalService(mChannel, serviceInfo, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                // Command successful! Code isn't necessarily needed here,
                // Unless you want to update the UI or add logging statements.
                System.out.println("Success creating local service !");
            }

            @Override
            public void onFailure(int arg0) {
                // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                System.out.println("Failed creating local service !");
            }
        });


    }

    public void host(View v) {
        /* We know that if this button was pressed we are the host ! */
        try {
            players = new Player[MAX_PLAYERS];
            players[0] = new Player(0, initialAmount, null); // Local player with the same amount, playerId 0 and null Socket
        } catch (IOException e) {
            e.printStackTrace();
        }

        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                System.out.println("Am descoperit !");
            }

            @Override
            public void onFailure(int reasonCode) {
                System.out.println("N-Am descoperit !");
            }
        });
        if (mManager != null) {
            peers = new WifiP2pManager.PeerListListener() {
                @Override
                public void onPeersAvailable(WifiP2pDeviceList peers) {
                    System.out.println("Sunt peers available " + peers.getDeviceList().size()+"!!!");
                    for (final WifiP2pDevice device : peers.getDeviceList()) {
                        System.out.println(device.deviceName + " = " + device.deviceAddress);
                        WifiP2pConfig config = new WifiP2pConfig();
                        config.deviceAddress = device.deviceAddress;
                        config.wps.setup = WpsInfo.PBC;

                        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

                            @Override
                            public void onSuccess() {
                                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
                                System.out.println(" M-am conectat la " + device.deviceName + " " + device.deviceAddress);

                                mManager.requestConnectionInfo(mChannel, new WifiP2pManager.ConnectionInfoListener() {
                                    @Override
                                    public void onConnectionInfoAvailable(WifiP2pInfo info) {
                                            System.out.println("Aici in onConnectionInfoAvailable e " + info.groupFormed + " " + info.groupOwnerAddress);
                                            currentNoOfPlayers++;
                                            NetworkThread networkThread = new NetworkThread(info.groupFormed ,info.groupOwnerAddress,
                                                                    HOST_PORT, SOCKET_TIMEOUT, currentNoOfPlayers,initialAmount);
                                            Toast.makeText(getApplicationContext(), "Client socket connected " ,
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void onFailure(int reason) {
                                Toast.makeText(getApplicationContext(), "Connect failed. Retry.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            };
            mManager.requestPeers(mChannel, peers);
        }
    }

    /* register the broadcast receiver with the intent values to be matched */
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }
    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

}

class NetworkThread extends AsyncTask<String, String, String> {
    private static final String     TAG         = "NetworkAsyncTask";
    private boolean isGroupFormed = false;
    private InetAddress groupOwnerAddress;
    private int socketTimeout, hostPort, playerId, initialAmount;

    public NetworkThread(boolean groupFormed, InetAddress ownerAddress, int port, int timeout, int id, int amount){
        isGroupFormed = groupFormed;
        groupOwnerAddress = ownerAddress;
        hostPort = port;
        socketTimeout = timeout;
        playerId = id;
        initialAmount = amount;
    }

    @Override
    protected String doInBackground(String... params) {
        Log.d(TAG, "In do in background");

        try {
            if (groupOwnerAddress != null) {
                Socket socket = new Socket();

                socket.bind(null);
                socket.connect((new InetSocketAddress(groupOwnerAddress, hostPort)), socketTimeout);

                Player pl = new Player(playerId, initialAmount,socket);
                pl.writeToStream(" Welcome player #" + pl.getPlayerId());
            }
            else {
                System.out.println(" IP Address is NULL in doInBackground !!");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Cooler than you";
    }

}