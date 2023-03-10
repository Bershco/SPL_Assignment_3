package bgu.spl.net.srv;

//import java.io.IOException;

public interface Connections<T> {

    

    boolean send(int connectionId, T msg);

    void send(String channel, T msg);

    void disconnect(int connectionId);
    

    //added:
    
    void addConnectionHandler(int id, ConnectionHandler<T> handler);
    void connect(int connectionId,String user);
    void subscribeToChanel(String channel, int connectionId,int subscription);
    boolean checkIfConnected(int owner);
    boolean checkPassword(String user, String pass);
    boolean unsubscribe(int owner, int id);
    int getSub(int owner, String channel);

    boolean checkIfHasTopic(int owner, String channel);
}
