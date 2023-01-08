package bgu.spl.net.srv;

import java.io.IOException;

public interface Connections<T> {

    boolean send(int connectionId, T msg);

    void send(String channel, T msg);

    void disconnect(int connectionId);
    

    //added:
    
    void addConnectionHandler(ConnectionHandler<T> handler);
    void connect(int connectionId);
    void subscribeToChanel(String channel, int connectionId,int subscription);
    boolean checkIfConnected(int owner);

    boolean checkPassword(String user, String pass);
}
