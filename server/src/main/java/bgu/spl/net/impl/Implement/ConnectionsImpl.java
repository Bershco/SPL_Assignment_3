package bgu.spl.net.impl.Implement;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;

public class ConnectionsImpl<T> implements Connections<T>  {
    private List<ConnectionHandler<T>> connection_handlers= new LinkedList<>();
    private Map<Integer,String[]> topics = new HashMap<>();
    private Map<Integer,Integer[]> subscriptions = new HashMap<>();
    private Map<String,String> user_password = new HashMap<>();
    private Map<Integer,ConnectionHandler<T>> connectToClient = new HashMap<>();
    
    
    public boolean send(int connectionId, T msg){  //send messages to client
        connectToClient.get(connectionId).send(msg);
        return false; //TODO: why we send boolean - find out
    }

    public void send(String channel, T msg){ // send messages to all clients that are part of this topic
        for(Integer id: topics.keySet()){
            String[] subs = topics.get(id);
            for(String topic : subs){
                if(topic.equals(channel)){
                    send(id,msg);
                }
            }
        }  
    }

    public boolean checkIfSubscribed(String channel,int connectionId){
        if(topics.containsKey(connectionId)){
            String[] topics_per_client = topics.get(connectionId);
            if(topics_per_client.length==0){
                return false;
            }
            for(int i=0; i< topics_per_client.length;i++){
                if(topics_per_client[i].equals(channel)){
                    return true;
                }
            }
        }
        return false;
    }
    //TODO: functions I think we might need: checkIfConnected,check if user exists

    public void disconnect(int connectionId){
        connectToClient.remove(connectToClient);
        topics.remove(connectToClient);
        subscriptions.remove(connectToClient);
    }

    public void connect(int connectionId){
        //TODO: Add the connectionId to the dictionary and create new Connection handler
       
    }
    public boolean subscribeToChanel(String channel, int connectionId){
        //check if chanel exists
        //yes-> subscribe
        //
        return false;
    }
}
//TOPIC = CHANEL