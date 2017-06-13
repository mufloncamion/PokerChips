package com.example.vladdascau.pokerchips;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.net.wifi.p2p.nsd.WifiP2pServiceRequest;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.games.Players;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;
    private WifiP2pManager.PeerListListener peers;
    WifiP2pManager.DnsSdServiceResponseListener servListener;
    WifiP2pManager.DnsSdTxtRecordListener txtListener;

    private InetAddress hostAddress;

    private static final int MAX_PLAYERS = 3;
    private static final int HOST_PORT = 9020;
    private static final int SOCKET_TIMEOUT = 2000;
    private static final String DEFAULT_HOST_NAME = "PokerChips HOST";
    private static final String DEFAULT_GAME_NAME = "PokerChips Game";

    final static int RC_SELECT_PLAYERS = 10000;
    final HashMap<String, String> peerz = new HashMap<String, String>();

    private String gameName;
    private String hostName = DEFAULT_HOST_NAME;
    private boolean amHost = false;

    private int initialAmount = 1000;
    private Player players[] = new Player[MAX_PLAYERS];
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

    ;

    public void findServices() {
        mManager.setDnsSdResponseListeners(mChannel, servListener, txtListener);

        WifiP2pServiceRequest serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        mManager.addServiceRequest(mChannel,
                serviceRequest,
                new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        // Success!
                        System.out.println("Success adding Service Request !");
                    }

                    @Override
                    public void onFailure(int code) {
                        // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                    }
                });

        mManager.discoverServices(mChannel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // Success!
                System.out.println("Success discovering Services ! " + mChannel.toString());
            }

            @Override
            public void onFailure(int code) {
                // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                if (code == WifiP2pManager.P2P_UNSUPPORTED) {
                    System.out.println("P2P isn't supported on this device.");
                }
            }
        });

    }

    public void searchGame(View v) {

        amHost = false;

        txtListener = new WifiP2pManager.DnsSdTxtRecordListener() {
            @Override
        /* Callback includes:
         * fullDomain: full domain name: e.g "printer._ipp._tcp.local."
         * record: TXT record dta as a map of key/value pairs.
         * device: The device running the advertised service.
         */

            public void onDnsSdTxtRecordAvailable(
                    String fullDomain, Map record, WifiP2pDevice device) {
                System.out.println("DnsSdTxtRecord available -" + record.toString());

            }
        };

        servListener = new WifiP2pManager.DnsSdServiceResponseListener() {
            @Override
            public void onDnsSdServiceAvailable(String instanceName, String registrationType,
                                                WifiP2pDevice resourceType) {

                // Update the device name with the human-friendly version from
                // the DnsTxtRecord, assuming one arrived.
                currentNoOfPlayers++;
                try {
                    players[currentNoOfPlayers] = new Player(currentNoOfPlayers, initialAmount, null); // null e socketul corespunzator fiecaruia pe care il initializam la startul jocului
                }
                catch (Exception e)
                {
                    System.out.println("Exception creating new player !");
                }
                System.out.println("- Service discovered from - " + resourceType.deviceName + " !!");
                /* We're on the client side and we've found the service. We should implement a thread
                 * to connect to the device id and port received  */
            }
        };
        findServices();
    };

    public void hostService(View v) {

        Map record = new HashMap();
        record.put("port", String.valueOf(HOST_PORT));
        record.put("gameName", "PokerChips");
        record.put("available", "visible");
        record.put("hostName", hostName);

        amHost = true;

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
                System.out.println("Pornesc LOCAL Service !");
            }

            @Override
            public void onFailure(int arg0) {
                // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
            }
        });

    }


    public void hostGame(View v) {
        /* We know that if this button was pressed we are the host ! */
        try {
            players[0] = new Player(0, initialAmount, null); // Local player with the same amount, playerId 0 and null Socket
        } catch (IOException e) {
            e.printStackTrace();
        }
        TextView uName = (TextView) findViewById(R.id.userName);
        String gameName = "Game " + uName.getText();

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
                    System.out.println("Sunt peers available " + peers.getDeviceList().size() + "!!!");
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
                                        //NetworkThread networkThread = new NetworkThread(info.groupFormed ,info.groupOwnerAddress,
                                        //                      HOST_PORT, SOCKET_TIMEOUT, currentNoOfPlayers,initialAmount);
                                        Toast.makeText(getApplicationContext(), "Client socket connected ",
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
