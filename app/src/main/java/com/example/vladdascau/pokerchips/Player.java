package com.example.vladdascau.pokerchips;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by vlad.dascau on 5/23/2017.
 */
public class Player extends User {
    private int playerId;
    private long currentAmount;
    private int positionInTop;
    private boolean isHost,isSmallBlind, isBigBlind;
    private Socket socket;
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public long getCurrentAmount() {
        return currentAmount;
    }

    public Player(int playerId, long currentAmount, Socket sock) throws IOException {
        this.playerId = playerId;
        this.currentAmount = currentAmount;
        if (sock != null) {
            dataOutputStream = new DataOutputStream(sock.getOutputStream());
            dataInputStream = new DataInputStream(sock.getInputStream());
        }

    }

    public void writeToStream(String message) {
        try {
            dataOutputStream.writeUTF(message);
            dataOutputStream.flush();
        }
        catch (Exception e)
        {
            System.out.println("Cannot write to Player " + playerId +"(" + e.getMessage() + ")");
        }
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
