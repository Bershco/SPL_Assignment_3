package bgu.spl.net.impl.Implement;

import javax.imageio.spi.ImageWriterSpi;
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
            disconnect(message);(message);
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
            connections.send(owner, ans);
        }
        else{
            //TODO:
            //check if was subscribed to topic - if wasnt then error i was then unsubscribe
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
        if(!hasError){
            String user = getUser(message);
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
            connections.send(owner, ans);
        }
        else{
            String frame = "CONNECTED" +"\n" + "version:1.2"+ "\n" + "^@";
            connections.connect(owner);
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
            connections.send(owner, ans);
        }
        else{
            //TODO:
            //subscribe with chanel ans subId
        }

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
            if(message[i].contains("id")){
                return true;
            }
        }
        return false;
    }
    private boolean hasDest(String[] message){
        for(int i=0; i < message.length;i++){
            if(message[i].contains("destination")){
                return true;
            }
        }
        return false;
    }
    

    private boolean hasPasscode(String[] message) {
        for(int i=0; i < message.length ; i++){
            if(message[i].contains("passcode")){
                return true;
            }
        }
        return false;
    }

    private boolean hasVersion(String[] message) {
        for(int i=0; i < message.length ; i++){
            if(message[i].contains("accept-version") & message[i].contains("1.2")){
                return true;
            }
        }
        return false;
    }
    private boolean hasHost(String[] message) {
        for(int i=0; i < message.length ; i++){
            if(message[i].contains("host") & message[i].contains("stomp.cs.bgu.ac.il")){
                return true;
            }
        }
        return false;
    }
    private boolean hasLogin(String[] message) {
        for(int i=0; i < message.length ; i++){
            if(message[i].contains("login")){
                return true;
            }
        }
        return false;
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

}
