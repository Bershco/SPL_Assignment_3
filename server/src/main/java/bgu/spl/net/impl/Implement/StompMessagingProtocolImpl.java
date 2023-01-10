package bgu.spl.net.impl.Implement;



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
        String error = "ERROR";
        String rec_id ="";
        if(hasReceipt(message)){
            rec_id = "\n" +getReceipt(message);
        }
        String ans =error + rec_id +"\n" + "message: malformed frame received" + "\n" + "The massage:" +"\n"+ "----";
        boolean is_header =false;
        for(int i=0; i <headers.length & !is_header;i++){
            if(headers[i].equals(message[0])){
                is_header = true;
            }
        }
        if(!is_header){
            ans = error+ ans +"\n" + message.toString() +"\n" +"----"+"\n" + "Did not contain header";
        }
        else if(message[0].equals("CONNECTED")){
            connect(message);
        }
        else if(!connections.checkIfConnected(owner)){
            ans = ans +"\n" +"----"+"\n" + "can not preform actions if not connected ";
        }
        else if(message[0].equals("DISCONNECT")){
            disconnect(message);
        }
        else if(message[0].equals("SEND")){
            send(message);
        }
        else if(message[0].equals("SUBSCRIBE")){
            subscribe(message);
        }
        else if(message[0].equals("UNSUBSCRIBE")){
            unsubscribe(message);
        }

    }

    public void send(String[] message){
        String rec_id ="";
        if(hasReceipt(message)){
            rec_id = "\n" +getReceipt(message);
        }
        String ans = "ERROR" +rec_id+ "\n" + "message: malformed frame received" + "\n" + "The massage:" +"\n"+ "----"; 
       
        boolean hasError = false;
       
        if(!hasError & !hasDest(message)){
            ans = ans +"\n" + message.toString() +"\n" +"----"+"\n" + "No destination header";
            hasError = true;
            shouldTerminate = true;
        }
        if(!hasError){
            int counter = 0;
            for(int i =0; i<message.length; i++){
                if(!message[i].equals("")){
                    counter ++;
                }
            }
            if(counter<4){
                ans = ans +"\n" + message.toString() +"\n" +"----"+"\n" + "No body in the message";
                hasError = true;
                shouldTerminate = true;
            }
        }
        if(hasError){
            connections.send(owner, ans);
        }
        else{
            message_id ++;
            String msg = "MESSAGE" + "\n" + getChannel(message) + "\n" + message_id +"\n"+ connections.getSub(owner,getChannel(message))+ "\n" + getBody(message)+ "\n";
            String receipt = "";
            if(hasReceipt(message)){
                String rec = getReceipt(message);
                receipt = "RECEIPT" + "\n"+ "receipt-id:"+rec +"\n";
            }
            connections.send(getChannel(message),msg);
            connections.send(owner,receipt);
        }
        
    }
    

    public void disconnect(String[] message){
        
        String rec_id ="";
        if(hasReceipt(message)){
            rec_id = "\n" +getReceipt(message);
        }
        String ans = "ERROR" +rec_id+ "\n" + "message: malformed frame received" + "\n" + "The massage:" +"\n"+ "----"; 

        
        boolean hasError = false;
        
        if(!hasError & !hasReceipt(message)){
            ans = ans +"\n" + message.toString() +"\n" +"----"+"\n" + "No id header";
            hasError = true;
        }
        if(hasError){
            connections.send(owner, ans);
        }
        else{
            shouldTerminate = true;
            String receipt = "RECEIPT" + "\n"+ "receipt-id:"+rec_id +"\n";
            connections.send(owner, receipt);
            connections.disconnect(owner);
        }
    }

    public void unsubscribe(String[] message){
        String rec_id ="";
        if(hasReceipt(message)){
            rec_id = "\n" +getReceipt(message);
        }
        String ans = "ERROR" +rec_id+ "\n" + "message: malformed frame received" + "\n" + "The massage:" +"\n"+ "----"; 
       
        boolean hasError = false;
        
        if(!hasError & !hasId(message)){
            ans = ans +"\n" + message.toString() +"\n" +"----"+"\n" + "No id header";
            hasError = true;
        }
        if(hasError){
            shouldTerminate = true;
            connections.send(owner, ans);
        }
        else{
            
            if(!connections.unsubscribe(owner,getID(message))){
                ans = ans +"\n" + message.toString() +"\n" +"----"+"\n" + "you are not subscribed to topic";
                connections.send(owner, ans);
            }
            else{
                String receipt = "";
                if(hasReceipt(message)){
                    receipt = "RECEIPT" + "\n"+ "receipt-id:"+rec_id +"\n";
                }
                connections.send(owner,receipt);
            }
           
        }
    }

    public void connect(String[] message){
        String rec_id ="";
        if(hasReceipt(message)){
            rec_id = "\n" +getReceipt(message);
        }
        String ans = "ERROR" +rec_id+ "\n" + "message: malformed frame received" + "\n" + "The massage:" +"\n"+ "----"; 
       
        boolean hasError = false;
        
        if(!hasError & !hasVersion(message)){
            ans = ans +"\n" + message.toString() +"\n" +"----"+"\n" + "No accept-version ot incorrect version";
            hasError = true;
        }
        if(!hasError & !hasHost(message)){
            ans = ans +"\n" + message.toString() +"\n" +"----"+"\n" + "No host or incorrect host";
            hasError = true;
        }
        if(!hasError & !hasLogin(message)){
            ans = ans +"\n" + message.toString() +"\n" +"----"+"\n" + "no login info";
            hasError = true;
        }
        if(!hasError & !hasPasscode(message)){
            ans = ans +"\n" + message.toString() +"\n" +"----"+"\n" + "No passcode";
            hasError = true;
        }
        String user = getUser(message);
        if(!hasError){
            String pass = getPassword(message);
            if(connections.checkIfConnected(owner)){
                ans = ans +"\n" + message.toString() +"\n" +"----"+"\n" + "you are already logged in";
                hasError = true;
            }
            if(!hasError & connections.checkPassword(user,pass)){
                ans = ans +"\n" + message.toString() +"\n" +"----"+"\n" + "password or user is incorrect";
                hasError = true;
            }
        }
        if(hasError){
            shouldTerminate = true;
            connections.send(owner, ans);
        }
        else{
            String frame = "CONNECTED" +"\n" + "version:1.2"+ "\n";
            connections.connect(owner,user);
            connections.send(owner, frame);
            String receipt = "";
                if(hasReceipt(message)){
                    receipt = "RECEIPT" + "\n"+ "receipt-id:"+rec_id +"\n";
                }
                connections.send(owner,receipt);
            }
        
    }
   
    private void subscribe(String[] message){
        String rec_id ="";
        if(hasReceipt(message)){
            rec_id = "\n" +getReceipt(message);
        }
        String ans = "ERROR" +rec_id+ "\n" + "message: malformed frame received" + "\n" + "The massage:" +"\n"+ "----"; 

        
        boolean hasError = false;
        
        if(!hasError & !hasDest(message)){
            ans = ans +"\n" + message.toString() +"\n" +"----"+"\n" + "No destination header";
            hasError = true;
        }
        if(!hasError & !hasId(message)){
            ans = ans +"\n" + message.toString() +"\n" +"----"+"\n" + "No id header";
            hasError = true;
        }
        if(hasError){
            shouldTerminate = true;
            connections.send(owner, ans);
        }
        else{
            connections.subscribeToChanel(getChannel(message), owner, getID(message));
            String receipt = "";
                if(hasReceipt(message)){
                    rec_id = getReceipt(message);
                    receipt = "RECEIPT" + "\n"+ "receipt-id:"+rec_id +"\n";
                }
                connections.send(owner,receipt);
        }

    }

    private boolean noAdditional(int lines, String[] message){
        int counter = 0; 
        for(int i =0; i<message.length; i++){
            if(!message[i].equals("")){
                counter ++;
            }
        }
        return (counter == lines);
    }
    
    private int getID(String[] message) {
        String id = "id:";
        for(int i=0; i < message.length;i++){
            if(message[i].contains("id:")){
                int ind = message[i].indexOf("id:") + id.length();
                return Integer.parseInt(message[i].substring(ind));
            }
        }
        return 0;
    }
    private String getChannel(String[] message) {
        String dst = "destination:";
        for(int i=0; i < message.length;i++){
            if(message[i].contains("destination:")){
                int ind = message[i].indexOf("destination:") + dst.length();
                return message[i].substring(ind);
            }
        }
        return "";
    }
    private String getReceipt(String[] message) {
        String rec = "receipt:";
        for(int i=0; i < message.length;i++){
            if(message[i].contains("receipt:")){
                int ind = message[i].indexOf("receipt:") + rec.length();
                return message[i].substring(ind);
            }
        }
        return "";
    }
    private String getPassword(String[] message) {
        String pass = "passcode:";
        for(int i=0; i < message.length;i++){
            if(message[i].contains("passcode:")){
                int ind = message[i].indexOf("passcode:") + pass.length();
                return message[i].substring(ind);
            }
        }
        return "";
    }

    private String getBody(String[] message) {
        for(int i =0; i<message.length; i++){
            if(!message[i].equals("\n") && !message[i].equals("destination")&& !message[i].equals("SEND")){
                return message[i];
            }
        }
        return "";
    }

    private String getUser(String[] message) {
        String user = "login:";
        for(int i=0; i < message.length;i++){
            if(message[i].contains("login:")){
                int ind = message[i].indexOf("login:") + user.length();
                return message[i].substring(ind);
            }
        }
        return "";
    }
    private boolean hasReceipt(String[] message) {
       for(int i=0; i < message.length;i++){
            if(message[i].contains("receipt:")){
                String[] checkEmpty = message[i].trim().split(":");
                if(checkEmpty.length == 1){
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    private boolean hasId(String[] message){
        for(int i=0; i < message.length;i++){
            if(message[i].contains("id:")){
                String[] checkEmpty = message[i].trim().split(":");
                if(checkEmpty.length == 1){
                    return false;
                }
                return true;
            }
        }
        return false;
    }
    private boolean hasDest(String[] message){
        for(int i=0; i < message.length;i++){
            if(message[i].contains("destination:")){
                String[] checkEmpty = message[i].trim().split(":");
                if(checkEmpty.length == 1){
                    return false;
                }
                return true;
            }
        }
        return false;
    }
    

    private boolean hasPasscode(String[] message) {
        for(int i=0; i < message.length ; i++){
            if(message[i].contains("passcode:")){
                String[] checkEmpty = message[i].trim().split(":");
                if(checkEmpty.length == 1){
                    return false;
                }
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
                String[] checkEmpty = message[i].trim().split(":");
                if(checkEmpty.length == 1){
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    

}
