package com.example.vladdascau.pokerchips;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
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
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity
{

    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;
    private WifiP2pManager.PeerListListener peers;
    WifiP2pManager.DnsSdServiceResponseListener servListener;
    WifiP2pManager.DnsSdTxtRecordListener txtListener;

    private InetAddress hostAddress;

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

    private static final int MAX_PLAYERS = 3;
    private Player players[] = new Player[MAX_PLAYERS];
    private int currentNoOfPlayers = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
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

    public void findServices()
    {

        WifiP2pDnsSdServiceRequest serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        mManager.addServiceRequest(mChannel,
                serviceRequest,
                new WifiP2pManager.ActionListener()
                {
                    @Override
                    public void onSuccess()
                    {
                        System.out.println("Adding service request !!");
                        // Success!
                    }

                    @Override
                    public void onFailure(int code)
                    {
                        System.out.println("Failed adding service request !!");
                        // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                    }
                });

        mManager.discoverServices(mChannel, new WifiP2pManager.ActionListener()
        {

            @Override
            public void onSuccess()
            {
                // Success!
                System.out.println("Service discovery initiated");
                System.out.println("Success discovering Services ! " + mChannel.toString());
                mManager.setDnsSdResponseListeners(mChannel,
                        new WifiP2pManager.DnsSdServiceResponseListener()
                        {
                            @Override
                            public void onDnsSdServiceAvailable(String instanceName,
                                                                String registrationType, WifiP2pDevice device)
                            {

                                // A service has been discovered. Is this our app?
                                if (instanceName.equalsIgnoreCase("PokerChips"))
                                {
                                    // yes it is

                                    WifiP2pConfig wifiP2pConfig = new WifiP2pConfig();
                                    wifiP2pConfig.deviceAddress = device.deviceAddress;
                                    wifiP2pConfig.groupOwnerIntent = 0;
                                    wifiP2pConfig.wps.setup = WpsInfo.PBC;

                                    if (mManager != null)
                                    {
                                        mManager.connect(mChannel, wifiP2pConfig,
                                                new WifiP2pManager.ActionListener()
                                                {

                                                    @Override
                                                    public void onSuccess()
                                                    {
                                                        // WiFiDirectBroadcastReceiver will notify us.
                                                    }

                                                    @Override
                                                    public void onFailure(int reason)
                                                    {
                                                    }
                                                });
                                    }
                                }
                            }
                        }, new WifiP2pManager.DnsSdTxtRecordListener()
                        {

                            @Override
                            public void onDnsSdTxtRecordAvailable(
                                    String fullDomainName, Map<String, String> record,
                                    WifiP2pDevice device)
                            {
                                boolean isGroupOwner = device.isGroupOwner();
                                int port = Integer.parseInt(record.get("port").toString());

                                System.out.println("Am descoperit serviciul pe portul " + port);
                                // further process
                            }
                        });

            }

            @Override
            public void onFailure(int code)
            {
                // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                if (code == WifiP2pManager.P2P_UNSUPPORTED)
                {
                    System.out.println("P2P isn't supported on this device.");
                }
            }
        });

    }

    public void searchGame(View v)
    {

        amHost = false;
        findServices();

        txtListener = new WifiP2pManager.DnsSdTxtRecordListener()
        {
            @Override
        /* Callback includes:
         * fullDomain: full domain name: e.g "printer._ipp._tcp.local."
         * record: TXT record dta as a map of key/value pairs.
         * device: The device running the advertised service.
         */

            public void onDnsSdTxtRecordAvailable(
                    String fullDomain, Map record, WifiP2pDevice device)
            {
                System.out.println("DnsSdTxtRecord available -" + record.toString());

            }
        };

        servListener = new WifiP2pManager.DnsSdServiceResponseListener()
        {
            @Override
            public void onDnsSdServiceAvailable(String instanceName, String registrationType,
                                                WifiP2pDevice resourceType)
            {
                System.out.println("A venit DNS Service availabl");
                System.out.println("DnsSdTxtRecord available -" + resourceType.toString());
                // Update the device name with the human-friendly version from
                // the DnsTxtRecord, assuming one arrived.
                /* We're on the client side and we've found the service. We should implement a thread
                 * to connect to the device id and port received  */

            }
        };


    }

    ;


    public void hostService(View v)
    {

        Map record = new HashMap();
        record.put("gameName", "PokerChips");
        record.put("available", "visible");
        record.put("hostName", hostName);
        record.put("port", String.valueOf(HOST_PORT));

        amHost = true;


        // Service information.  Pass it an instance name, service type
        // _protocol._transportlayer , and the map containing
        // information other devices will want once they connect to this one.
        WifiP2pDnsSdServiceInfo serviceInfo =
                WifiP2pDnsSdServiceInfo.newInstance("PokerChips", "_presence._tcp", record);

        // Add the local service, sending the service info, network channel,
        // and listener that will be used to indicate success or failure of
        // the request.
        mManager.addLocalService(mChannel, serviceInfo, new WifiP2pManager.ActionListener()
        {

            @Override
            public void onSuccess()
            {
                System.out.println("Added local service");
            }

            @Override
            public void onFailure(int error)
            {
                System.out.println("ERRORCEPTION: Failed to add a service");
            }
        });


        /*
        mManager.createGroup(mChannel,new WifiP2pManager.ActionListener()
        {
            @Override
            public void onSuccess()
            {
                System.out.println(" Creez GRUPUL !!!!!!!!!!!!!!!!!!!!");
                WifiManager wifiMgr = (WifiManager) getSystemService(WIFI_SERVICE);
                WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
                int ip = wifiInfo.getIpAddress();
                String ipAddress = Formatter.formatIpAddress(ip);
                System.out.println("Ip-ul hostului va fi " + ipAddress);
            }

            @Override
            public void onFailure(int reason)
            {
                System.out.println("N-am putut sa creez grupul pentru ca "+reason);
            }
        });
        */
    }

    /* register the broadcast receiver with the intent values to be matched */
    @Override
    protected void onResume()
    {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }

    /* unregister the broadcast receiver */
    @Override
    protected void onPause()
    {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

}