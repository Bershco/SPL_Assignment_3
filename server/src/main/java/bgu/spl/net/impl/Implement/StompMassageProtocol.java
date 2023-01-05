package bgu.spl.net.impl.Implement;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.IconifyAction;

import bgu.spl.net.api.StompMessagingProtocol;
import bgu.spl.net.srv.Connections;

public class StompMassageProtocol implements StompMessagingProtocol<String>{

 
    private String[] headers = {"CONNECT","SEND","UNSUBSCRIBE","SUBSCRIBE", "DISCONNECT"};
    private boolean shouldTerminate = false;
    private Connections<String> connections;
    int owner;

    @Override
    public void start(int connectionId, Connections<String> connections) {
        // TODO: Auto-generated method stub
        this.connections = connections;
        owner = connectionId;
        
    }

    @Override
    public void process(String message) {
        String[] split_message = splitFrame(message);
        String errorOrNot = isError(split_message);
        if (errorOrNot.equals("DISCONNECT")){
            shouldTerminate = true;
            String receipt = "RECEIPT" + "\n"+ "receipt-id:"+ owner +"\n"+ "^@";
            connections.send(owner, receipt);
            connections.disconnect(owner);
        }
        else{
            //SEND FRAMES AND ERROR
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
    private String isError(String[] message){
        //add if additional information to error
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
                //TODO: IMPLEMENT
            }
            else if(message[0].equals("SEND")){ 
                if(!hasDest){
                    ans = ans + "\n" + message.toString() +"\n" +"----"+"\n" + "Did not contain destination" + "^@";
                    return ans;
                }
                if(!hasEnd){
                    ans = ans + "\n" + message.toString() +"\n" +"----"+"\n" + "Did not contain destination" + "^@";
                    return ans;
                }
                if(message.length<=3){
                    ans = ans + "\n" + message.toString() +"\n" +"----"+"\n" + "frame has no body" + "^@";
                    return ans;
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
