package bgu.spl.net.impl.Implement;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;

public class ConnectionsImpl<T> implements Connections<T>  {
    private List<ConnectionHandler<T>> connection_handlers= new LinkedList<>();//not sure if I need this list
    private Map<Integer,String[]> topics = new HashMap<>();
    private Map<Integer,ConnectionHandler<T>> connectToClient = new HashMap<>();
    
    
    public boolean send(int connectionId, T msg){ 
        connectToClient.get(connectionId).send(msg);
        return false; //TODO: FIX THIS WTF
    }

    public void send(String channel, T msg){
        for(Integer id: topics.keySet()){
            String[] subs = topics.get(id);
            for(String topic : subs){
                if(topic.equals(channel)){
                    send(id,msg);
                }
            }
        }
        
    }

    public void disconnect(int connectionId){
        connectToClient.remove(connectToClient);
        topics.remove(connectToClient);
    }

    public void connect(int connectionId){
        //TODO: Add the connectionId to the dictionary and create new Connection handler
       
    }
    public void subscribe(String channel, int connectionId){
        //TODO: Add the connectionId to the dictionary and create new Connection handler
    }
}
//TOPIC = CHANEL