package com.mci.client;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mci.models.BaseRequest;
import com.mci.models.BaseResponse;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import java.net.URISyntaxException;

/**
 * Created by clay on 6/26/16.
 */
public class SocketClient {
    private static final String CLIENT_ID = "1234";
    private static final String HOSTNAME = "localhost";
    private static final String PORT = "3002";


    //TODO: write client class for Scott

    public static void main(String[] args) throws URISyntaxException {

        // initialize connection
        Socket socket = IO.socket("http://"+HOSTNAME+":"+PORT);
        
        // connection handler
        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {

                // after it connects emit a json request to send to ServiceApp.java server
                String dataToSend = buildRequestPacket("Client is sending you a message");
                System.out.println("connected: sending data: " + dataToSend);
                socket.emit("getData",dataToSend);
            }

        });
               
        // when ServiceApp.java responds
        socket.on("serverResponse", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                System.out.println("server res: " + args[0]);
                BaseResponse baseResponse = new Gson().fromJson(((String)args[0]), BaseResponse.class);
                System.out.println("msg: " + baseResponse.data.get("msg").getAsString());
            }

        });
        
        // socket disconnection handling
        socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                System.out.println("disconnected: " + args);
            }

        });
        socket.connect();
    }

    private static String buildRequestPacket(String msgToSend) {

        // build data object with msg
        JsonObject data = new JsonObject();
        data.add("msg", new JsonPrimitive(msgToSend));

        BaseResponse response = new BaseResponse(CLIENT_ID, "getSymbolDataFrame", data);

        return new Gson().toJson(response); // return it as a string so that it can be send via socket
    }
}
