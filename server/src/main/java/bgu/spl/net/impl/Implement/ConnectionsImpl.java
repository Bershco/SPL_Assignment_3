package bgu.spl.net.impl.Implement;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.lang.model.util.SimpleAnnotationValueVisitor9;

import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;

public class ConnectionsImpl<T> implements Connections<T>  {
    
    private Map<Integer,List<String>> topics = new HashMap<>(); //example : <id:1, [book,bloop]>
    private Map<Integer,List<Integer>> subId = new HashMap<>(); //example : <id:1, [78,80]>
    private Map<String,List<Integer>> subscriptions = new HashMap<>();// example: <book, [id:1,id:2]
    private Map<String,String> user_password = new HashMap<>(); //example: <meni,123>
    private Map<Integer,String> user_Id = new HashMap<>(); 
    private Map<Integer,ConnectionHandler<T>> connectToClient = new HashMap<>();
    int counter_handler = 0;
    

    public boolean unsubscribe(int connectionId, int sub_Id){
        if(!topics.containsKey(connectionId)){
            return false;
        }
        if(!subId.containsKey(sub_Id)){
            return false;
        } 
        
        else{
            String topic = "";
            for(String str : subscriptions.keySet()){
                List<Integer> lst = subscriptions.get(str);
                for(Integer i : lst) {
                    if(connectionId == i){
                        topic = str;
                    }
                }
            }
            subscriptions.get(topic).remove(connectionId);
            subId.get(connectionId).remove(sub_Id);
            topics.get(connectionId).remove(topic);
        }
        return true;
    }
    public boolean send(int connectionId, T msg){  //send messages to client
        try{
            connectToClient.get(connectionId).send(msg);
            return true;
        }
        catch(Exception e){
            return false; 
        }
    }

    public void send(String channel, T msg){ // send messages to all clients that are part of this topic
        for(Integer id: topics.keySet()){
            List<String> subs = topics.get(id);
            for(String topic : subs){
                if(topic.equals(channel)){
                    send(id,msg);
                }
            }
        }  
    }
    public void disconnect(int connectionId){
        user_Id.remove(connectionId);
        connectToClient.remove(connectionId);
    
    }

    public void connect(int connectionId , String user){
        user_Id.put(connectionId,user);
    }

    public boolean checkIfConnected(int connectionId){ 
        if(user_Id.containsKey(connectionId)){
            return true;
        }
        return false;
    }

    public void subscribeToChanel(String channel, int connectionId,int subscription){
        
        if(topics.containsKey(connectionId)){
            List<String> topics_per_client = topics.get(connectionId);
            for(int i=0; i< topics_per_client.size();i++){
                if(topics_per_client.get(i).equals(channel)){
                    return;
                }
            }
            topics.get(connectionId).add(channel);
            subId.get(connectionId).add(subscription);
            
        }
        else{ //subscribe
            List<String> list1 = new LinkedList<>();
            list1.add(channel);
            List<Integer> list2 = new LinkedList<>();
            list2.add(subscription);
            topics.put(connectionId,list1);
            subId.put(connectionId,list2);
        
        }
        if(!subscriptions.containsKey(channel)){
            List<Integer> list = new LinkedList<>();
            list.add(connectionId);
            subscriptions.put(channel,list);
        }
        else{
            subscriptions.get(channel).add(connectionId);
        }
     
    }



    public void addConnectionHandler(int id ,ConnectionHandler<T> handler){
        connectToClient.put(id,handler);
    }

    @Override
    public boolean checkPassword(String user, String pass) {
        if(user_password.containsKey(user)){
            if(user_password.get(user).equals(pass)){
                return false;
            }
        }
        else{
            user_password.put(user,pass);
        }
        return true;
    }
 
    @Override
    public String getSub(int owner, String channel) {
       return "";
       //TODO: Implement this method
    }
  
}
