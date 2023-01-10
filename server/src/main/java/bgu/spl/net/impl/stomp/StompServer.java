package bgu.spl.net.impl.stomp;

import bgu.spl.net.impl.Implement.StompMessageEncoderDecoder;
import bgu.spl.net.impl.Implement.StompMessagingProtocolImpl;
import bgu.spl.net.srv.Server;

public class StompServer {

    public static void main(String[] args) {
        
        if(args[1].equals("tpc")){
            Server.threadPerClient(
                7777, //port
                () -> new StompMessagingProtocolImpl(), //protocol factory
                () -> new StompMessageEncoderDecoder() //message encoder decoder factory
        ).serve();
        }
        if(args[1].equals("reactor")){
            Server.reactor(
                10, 
                7777, 
                () -> new StompMessagingProtocolImpl(),
                () -> new StompMessageEncoderDecoder()).serve();
        }
    }
}
