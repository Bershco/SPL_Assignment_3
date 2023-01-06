package bgu.spl.net.impl.Implement;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.IconifyAction;

import bgu.spl.net.api.StompMessagingProtocol;
import bgu.spl.net.srv.Connections;

public class StompMassageProtocol implements StompMessagingProtocol<String>{

    //TODO : understand where do I get the receipt id
    //TODO : when subscribe and unsubscribe we have a subscription id - where the fuch do I save it and what doest it representes
    // second to do - probably in connectionImpl
 
    private String[] headers = {"CONNECT","SEND","UNSUBSCRIBE","SUBSCRIBE", "DISCONNECT"};
    private boolean shouldTerminate = false;
    private Connections<String> connections;
    int owner;

    @Override
    public void start(int connectionId, Connections<String> connections) {
        this.connections = connections;
        owner = connectionId;
        
    }

    @Override
    public void process(String message) {
        String[] split_message = splitFrame(message);
        String errorOrNot = isError(split_message);
        String frame;
        //TODO: understand what I send here for every frame and if others should be implemented in server
        if (errorOrNot.equals("DISCONNECT")){
            shouldTerminate = true;
            String receipt = "RECEIPT" + "\n"+ "receipt-id:"+"NO IDEA WHERE TO GET IT FROM" +"\n"+ "^@";
            connections.send(owner, receipt);
            connections.disconnect(owner);
        }
        else if(errorOrNot.equals("CONNECT")){
           frame = "CONNECTED" +"\n" + "version:1.2"+ "\n" + "^@";
           connections.send(owner, frame);
        }
        else if(errorOrNot.equals("SUBSCRIBE")){
         
        }
        else if(errorOrNot.equals("UNSUBSCRIBE")){

        }
        else if(errorOrNot.equals("SEND")){

        }
        else{
            connections.send(owner, errorOrNot);
        }
        
    }
        
    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }
   
    private String[] splitFrame(String message){
        String regex = "\n";
        String[] splited = message.split(regex);
        return splited;
    }


    //checks the correctness if the FRAME(headers and such)
    private String isError(String[] message){
        //todo: add if additional information to error - receipt number
        String ans = "ERROR" + "\n" + "message: malformed frame received" + "\n" + "The massage:" +"\n"+ "----";
        boolean is_header =false;
        for(int i=0; i <headers.length & !is_header;i++){
            if(headers[i].equals(message[0])){
                is_header = true;
            }
        }
        if(!is_header){
            ans = ans +"\n" + message.toString() +"\n" +"----"+"\n" + "Did not contain header" + "^@";
        }
        else{
            boolean hasDest = hasDest(message); 
            boolean hasEnd = message[message.length-1] == "^@";
            boolean hasId = hasId(message);
            if(message[0].equals("CONNECT")){
                //TODO: check user and pasword and somehow connect the user and create a connection handler for him --> 
                //some people say it should be implemented in server
            }
            else if(message[0].equals("SEND")){ 
                if(!hasDest){ //has destination
                    return ans + "\n" + message.toString() +"\n" +"----"+"\n" + "Did not contain destination" + "^@";
                }
                if(!hasEnd){ 
                    return ans + "\n" + message.toString() +"\n" +"----"+"\n" + "Did not contain destination" + "^@";   
                }
                if(message.length<=3){ //has all fields needed- such as body
                    return ans + "\n" + message.toString() +"\n" +"----"+"\n" + "frame has no body" + "^@";
                }
                
            }
            else if(message[0].equals("SUBSCRIBE")){
                if(!hasDest){
                    ans = ans + "\n" + message.toString() +"\n" +"----"+"\n" + "Did not contain destination" + "^@";
                    return ans;
                }
                if(!hasId){
                    ans = ans + "\n" + message.toString() +"\n" +"----"+"\n" + "Did not contain id" + "^@";
                    return ans;
                }
                if(message.length>4){
                    ans = ans + "\n" + message.toString() +"\n" +"----"+"\n" + "frame body" + "^@";
                    return ans;
                }
            }
            else if(!hasEnd){
                ans = ans +"\n" + message.toString() +"\n" +"----"+"\n" + "No null character" + "^@";
                return ans;
            }
        }
        return message[0]; //return header
    }
    private boolean hasDest(String[] message){
        for(int i=0; i < message.length;i++){
            String regex = ":";
            String[] split = message[i].split(regex);
            if(split[0].equals("destination")){
                return true;
            }
        }
        return false;
    }
    private boolean hasId(String[] message){
        for(int i=0; i < message.length;i++){
            String regex = ":";
            String[] split = message[i].split(regex);
            if(split[0].equals("id")){
                return true;
            }
        }
        return false;
    }
}
