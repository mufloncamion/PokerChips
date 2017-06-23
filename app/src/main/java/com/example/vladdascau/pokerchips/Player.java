package com.example.vladdascau.pokerchips;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by vlad.dascau on 5/23/2017.
 */
public class Player extends User {
    private long currentAmount;
    private int positionInTop;
    private boolean isHost,isSmallBlind, isBigBlind;

    public long getCurrentAmount() {
        return currentAmount;
    }

    // Varianta apelata de client
    public Player(String username, long currentAmount, InetAddress hostAddress, int hostPort) throws IOException {
        super(username, hostAddress, hostPort);
        this.currentAmount = currentAmount;
    }

    // Varianta apelata de server
    public Player(String username, long currentAmount)
    {
        super(username);
        this.currentAmount = currentAmount;
    }

    public void setCurrentAmount(long currentAmount) {
        this.currentAmount = currentAmount;
    }

    public int getPositionInTop() {
        return positionInTop;
    }

    public void setPositionInTop(int positionInTop) {
        this.positionInTop = positionInTop;
    }

    public boolean isHost() {
        return isHost;
    }

    public void setHost(boolean host) {
        isHost = host;
    }

    public boolean isSmallBlind() {
        return isSmallBlind;
    }

    public void setSmallBlind(boolean smallBlind) {
        isSmallBlind = smallBlind;
    }

    public boolean isBigBlind() {
        return isBigBlind;
    }

    public void setBigBlind(boolean bigBlind) {
        isBigBlind = bigBlind;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public DataOutputStream getDataOutputStream() {
        return dataOutputStream;
    }

    public void setDataOutputStream(DataOutputStream dataOutputStream) {
        this.dataOutputStream = dataOutputStream;
    }

    public DataInputStream getDataInputStream() {
        return dataInputStream;
    }

    public void setDataInputStream(DataInputStream dataInputStream) {
        this.dataInputStream = dataInputStream;
    }
}
