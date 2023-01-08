package bgu.spl.net.impl.Implement;

import javax.imageio.spi.ImageWriterSpi;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane.IconifyAction;

import bgu.spl.net.api.StompMessagingProtocol;
import bgu.spl.net.srv.Connections;

public class StompMessagingProtocolImpl implements StompMessagingProtocol<String>{

 
    private String[] headers = {"CONNECT","SEND","UNSUBSCRIBE","SUBSCRIBE", "DISCONNECT"};
    private boolean shouldTerminate = false;
    private Connections<String> connections;
    int owner;
    private int message_id = 0;

    @Override
    public void start(int connectionId, Connections<String> connections) {
        this.connections = connections;
        owner = connectionId;  
    }

    @Override
    public void process(String message) {
        String[] split_message = splitFrame(message);
        isError(split_message);
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
    private void isError(String[] message){
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
        else if(message[0].equals("CONNECTED")){
            connect(message);
        }
        else if(message[0].equals("DISCONNECT")){
            disconnect(message);
        }
        else if(message[0].equals("SEND")){
            //TODO : implement
        }
        else if(message[0].equals("SUBSCRIBE")){
            subscribe(message);
        }
        else if(message[0].equals("UNSUBSCRIBE")){
            unsubscribe(message);
        }

    }

    public void send(String[] message){
        
        String ans = "ERROR" + "\n" + "message: malformed frame received" + "\n" + "The massage:" +"\n"+ "----"; 
        boolean hasEnd = message[message.length-1] == "^@";
        boolean hasError = false;
        if(!hasEnd){
            ans = ans +"\n" + message.toString() +"\n" +"----"+"\n" + "No null character" + "^@";
            hasError = true;
            shouldTerminate = true;
        }
        if(!hasError & !hasDest(message)){
            ans = ans +"\n" + message.toString() +"\n" +"----"+"\n" + "No destination header" + "^@";
            hasError = true;
            shouldTerminate = true;
        }
        if(!hasError){
            int counter = 0;
            for(int i =0; i<message.length; i++){
                if(!message[i].equals("\n")){
                    counter ++;
                }
            }
            if(counter<4){
                ans = ans +"\n" + message.toString() +"\n" +"----"+"\n" + "No body in the message" + "^@";
                hasError = true;
                shouldTerminate = true;
            }
        }
        if(hasError){
            connections.send(owner, ans);
        }
        else{
            message_id ++;
            String msg = "MESSAGE" + "\n" + getChannel(message) + "\n" + message_id + "\n" + getBody(message) +"\n"+ connections.getSub(owner,getChannel(message))+"^@" ;
            connections.send(getChannel(message),msg);
        }
        
    }
    

    private String getBody(String[] message) {
        for(int i =0; i<message.length; i++){
            if(!message[i].equals("\n") && !message[i].equals("destination")&& !message[i].equals("SEND")){
                return message[i];
            }
        }
        return "";
    }

    public void disconnect(String[] message){
        String ans = "ERROR" + "\n" + "message: malformed frame received" + "\n" + "The massage:" +"\n"+ "----"; 
        boolean hasEnd = message[message.length-1] == "^@";
        boolean hasError = false;
        if(!hasEnd){
            ans = ans +"\n" + message.toString() +"\n" +"----"+"\n" + "No null character" + "^@";
            hasError = true;
        }
        if(!hasError & !hasReceipt(message)){
            ans = ans +"\n" + message.toString() +"\n" +"----"+"\n" + "No id header" + "^@";
            hasError = true;
        }
        if(hasError){
            connections.send(owner, ans);
        }
        else{
            shouldTerminate = true;
            String rec_id = getReceipt(message);
            String receipt = "RECEIPT" + "\n"+ "receipt-id:"+rec_id +"\n"+ "^@";
            connections.send(owner, receipt);
            connections.disconnect(owner);
        }
    }

    public void unsubscribe(String[] message){
        String ans = "ERROR" + "\n" + "message: malformed frame received" + "\n" + "The massage:" +"\n"+ "----"; 
        boolean hasEnd = message[message.length-1] == "^@";
        boolean hasError = false;
        if(!hasEnd){
            ans = ans +"\n" + message.toString() +"\n" +"----"+"\n" + "No null character" + "^@";
            hasError = true;
        }
        if(!hasError & !hasId(message)){
            ans = ans +"\n" + message.toString() +"\n" +"----"+"\n" + "No id header" + "^@";
            hasError = true;
        }
        if(hasError){
            shouldTerminate = true;
            connections.send(owner, ans);
        }
        else{
            
            if(!connections.unsubscribe(owner,getID(message))){
                ans = ans +"\n" + message.toString() +"\n" +"----"+"\n" + "you are not subscribed to topic" + "^@";
                connections.send(owner, ans);
            }
           
        }
    }

    public void connect(String[] message){
        String ans = "ERROR" + "\n" + "message: malformed frame received" + "\n" + "The massage:" +"\n"+ "----"; 
        boolean hasEnd = message[message.length-1] == "^@";
        boolean hasError = false;
        if(!hasEnd){
            ans = ans +"\n" + message.toString() +"\n" +"----"+"\n" + "No null character" + "^@";
            hasError = true;
        }
        if(!hasError & !hasVersion(message)){
            ans = ans +"\n" + message.toString() +"\n" +"----"+"\n" + "No accept-version ot incorrect version" + "^@";
            hasError = true;
        }
        if(!hasError & !hasHost(message)){
            ans = ans +"\n" + message.toString() +"\n" +"----"+"\n" + "No host or incorrect host" + "^@";
            hasError = true;
        }
        if(!hasError & !hasLogin(message)){
            ans = ans +"\n" + message.toString() +"\n" +"----"+"\n" + "no login info" + "^@";
            hasError = true;
        }
        if(!hasError & !hasPasscode(message)){
            ans = ans +"\n" + message.toString() +"\n" +"----"+"\n" + "No passcode" + "^@";
            hasError = true;
        }
        String user = getUser(message);
        if(!hasError){
            String pass = getPassword(message);
            if(connections.checkIfConnected(owner)){
                ans = ans +"\n" + message.toString() +"\n" +"----"+"\n" + "you are already logged in" + "^@";
                hasError = true;
            }
            if(!hasError & connections.checkPassword(user,pass)){
                ans = ans +"\n" + message.toString() +"\n" +"----"+"\n" + "password or user is incorrect" + "^@";
                hasError = true;
            }
        }
        if(hasError){
            shouldTerminate = true;
            connections.send(owner, ans);
        }
        else{
            String frame = "CONNECTED" +"\n" + "version:1.2"+ "\n" + "^@";
            connections.connect(owner,user);
            connections.send(owner, frame);
        }
    }
   
    private void subscribe(String[] message){
        String ans = "ERROR" + "\n" + "message: malformed frame received" + "\n" + "The massage:" +"\n"+ "----"; 
        boolean hasEnd = message[message.length-1] == "^@";
        boolean hasError = false;
        if(!hasEnd){
            ans = ans +"\n" + message.toString() +"\n" +"----"+"\n" + "No null character" + "^@";
            hasError = true;
        }
        if(!hasError & !hasDest(message)){
            ans = ans +"\n" + message.toString() +"\n" +"----"+"\n" + "No destination header" + "^@";
            hasError = true;
        }
        if(!hasError & !hasId(message)){
            ans = ans +"\n" + message.toString() +"\n" +"----"+"\n" + "No id header" + "^@";
            hasError = true;
        }
        if(hasError){
            shouldTerminate = true;
            connections.send(owner, ans);
        }
        else{
            connections.subscribeToChanel(getChannel(message), owner, getID(message));
        }

    }
    
    private int getID(String[] message) {
        for(int i=0; i < message.length;i++){
            if(message[i].contains("id:")){
                int ind = message[i].indexOf("id:");
                return Integer.parseInt(message[i].substring(ind));
            }
        }
        return 0;
    }
    private String getChannel(String[] message) {
        for(int i=0; i < message.length;i++){
            if(message[i].contains("destination:")){
                int ind = message[i].indexOf("destination:");
                return message[i].substring(ind);
            }
        }
        return "";
    }
    private String getReceipt(String[] message) {
        for(int i=0; i < message.length;i++){
            if(message[i].contains("receipt")){
                int ind = message[i].indexOf("receipt");
                return message[i].substring(ind);
            }
        }
        return "";
    }
    private String getPassword(String[] message) {
        for(int i=0; i < message.length;i++){
            if(message[i].contains("passcode:")){
                int ind = message[i].indexOf("passcode:");
                return message[i].substring(ind);
            }
        }
        return "";
    }

    private String getUser(String[] message) {
        for(int i=0; i < message.length;i++){
            if(message[i].contains("login:")){
                int ind = message[i].indexOf("login:");
                return message[i].substring(ind);
            }
        }
        return "";
    }
    private boolean hasReceipt(String[] message) {
       for(int i=0; i < message.length;i++){
            if(message[i].contains("receipt")){
                return true;
            }
        }
        return false;
    }

    private boolean hasId(String[] message){
        for(int i=0; i < message.length;i++){
            if(message[i].contains("id:")){
                return true;
            }
        }
        return false;
    }
    private boolean hasDest(String[] message){
        for(int i=0; i < message.length;i++){
            if(message[i].contains("destination:")){
                return true;
            }
        }
        return false;
    }
    

    private boolean hasPasscode(String[] message) {
        for(int i=0; i < message.length ; i++){
            if(message[i].contains("passcode:")){
                return true;
            }
        }
        return false;
    }

    private boolean hasVersion(String[] message) {
        for(int i=0; i < message.length ; i++){
            if(message[i].contains("accept-version:") & message[i].contains("1.2")){
                return true;
            }
        }
        return false;
    }
    private boolean hasHost(String[] message) {
        for(int i=0; i < message.length ; i++){
            if(message[i].contains("host:") & message[i].contains("stomp.cs.bgu.ac.il")){
                return true;
            }
        }
        return false;
    }
    private boolean hasLogin(String[] message) {
        for(int i=0; i < message.length ; i++){
            if(message[i].contains("login:")){
                return true;
            }
        }
        return false;
    }

    

}
