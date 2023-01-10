package bgu.spl.net.impl.stomp;

import java.util.MissingFormatArgumentException;

import bgu.spl.net.impl.Implement.StompMessageEncoderDecoder;
import bgu.spl.net.impl.Implement.StompMessagingProtocolImpl;
import bgu.spl.net.srv.Server;

public class StompServer {

    public static void main(String[] args) {
        if (args.length < 2)
            throw new MissingFormatArgumentException("Either port or server type is missing.");
        int port = Integer.parseInt(args[0]);
        if(args[1].equals("tpc")){
            Server.threadPerClient(
                port, //port
                () -> new StompMessagingProtocolImpl(), //protocol factory
                () -> new StompMessageEncoderDecoder() //message encoder decoder factory
        ).serve();
        }
        if(args[1].equals("reactor")){
            Server.reactor(
                10, 
                port, 
                () -> new StompMessagingProtocolImpl(),
                () -> new StompMessageEncoderDecoder()).serve();
        }
    }
}
