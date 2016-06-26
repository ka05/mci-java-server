package com.mci;

import com.corundumstudio.socketio.*;
import com.corundumstudio.socketio.listener.DataListener;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.mci.interfaces.SocketCallback;
import com.mci.models.BaseRequest;
import com.mci.models.BaseResponse;

import java.io.UnsupportedEncodingException;

public class ServiceApp {

    private static final int PORT = 3002;
    private static final String HOSTNAME = "localhost";
    private static final String CLIENT_ID = "1234";
    private static final String SERVER_ID = "4321";

    private static SocketIOServer server;

    public static void main(String[] args) throws InterruptedException, UnsupportedEncodingException {
        startServer();
    }

    /**
     * initializes the server and sets up listeners
     */
    private static void startServer() {
        Configuration config = new Configuration();
        config.setHostname(HOSTNAME);
        config.setPort(PORT);

        server = new SocketIOServer(config);
        server.addEventListener("getData", String.class, new DataListener<String>() {

            @Override
            public void onData(final SocketIOClient client, String data, final AckRequest ackRequest) {

                middleware(data, new SocketCallback() {
                    @Override
                    public void success(String action, JsonObject data) {
                        System.out.println("getSymbolDataFrame : Success");
                        //Acknowledge the request:
                        ackRequest.sendAckData("ACK_test");
                        String responseToSend = handleDataAction(action, data);

                        String resObjSerialized = buildResponsePacket(responseToSend);


                        client.sendEvent("serverResponse", new VoidAckCallback(){
                            @Override
                            protected void onSuccess() {}
                        }, resObjSerialized);
                    }

                    @Override
                    public void failure(Object object) {
                        // attempted connection from
                        String id = (String)object;
                        System.out.println("Attempted Connection from unauthorized client id: " + id);
                    }
                });

            }
        });

        server.start();
        System.out.println("[JAVA SERVER INFO] Java server started. Running on port: " + PORT);
    }

    //TODO: change this method to send other data back later on
    private static String buildResponsePacket(String msgToSend) {

        // build data object with msg
        JsonObject data = new JsonObject();
        data.add("msg", new JsonPrimitive(msgToSend));

        BaseRequest response = new BaseRequest(SERVER_ID, data);

        return new Gson().toJson(response); // return it as a string so that it can be send via socket
    }

    private static String handleDataAction(String action, JsonObject data) {
        String res = "";

        // handle actions here
        if(action.equals("getSymbolDataFrame")){
            String msg = data.get("msg").getAsString();
            System.out.println("Received Message: " + msg);

            res = "We got your message: " + msg;
        }

        return res;
    }

    private static void middleware(String response, SocketCallback callback){
        // a way of serializing from json string to a java class
        BaseResponse baseResponse = new Gson().fromJson(response, BaseResponse.class);

        String id = baseResponse.id;
        System.out.println("id: " + id);
        String action = baseResponse.action;
        JsonObject data = baseResponse.data;

        // only allow client with matching id to make calls
        if(id.equals(CLIENT_ID)){
            System.out.println("matched");
            callback.success(action, data);
        }else{
            callback.failure(id);
        }
    }
}
