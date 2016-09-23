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

    private static final int PORT = 3002; // Port number server runs on
    private static final String HOSTNAME = "localhost"; // address server runs on
    private static final String CLIENT_ID = "1234"; // id of client who is connecting. They must pass in this "id" in their json object
    private static final String SERVER_ID = "4321"; // id to pass back to client so they know it is us. just for safe measure

    private static SocketIOServer server; // class instance of our server

    /**
     * Main Method
     * @param args
     * @throws InterruptedException
     * @throws UnsupportedEncodingException
     */
    public static void main(String[] args) throws InterruptedException, UnsupportedEncodingException {
        startServer();
    }

    /**
     * initializes the server and sets up listeners
     */
    private static void startServer() {

        // sets up the hostname and port.
        // NOTE: later we will probably run on a more obscure port for security reasons.
        // for now it doesnt matter since we are only doing local development hence "localhost"
        Configuration config = new Configuration();
        config.setHostname(HOSTNAME);
        config.setPort(PORT);

        // create server with configuration
        server = new SocketIOServer(config);

        // event handler for "getData"
        // when client ( our node server ) emits "getData" to this server.
        server.addEventListener("getData", String.class, new DataListener<String>() {

            /**
             *
             * @param client : connection instance to client
             * @param data :
             * @param ackRequest
             */
            @Override
            public void onData(final SocketIOClient client, String data, final AckRequest ackRequest) {
                // data received from client

                // use middleware method to validate the client is legit
                // SocketCallback is simply an interface class that is used as a closure with a success and error callback
                middleware(data, new SocketCallback() {
                    @Override
                    public void success(String action, JsonObject data) {
                        System.out.println("getSymbolDataFrame : Success");

                        //Acknowledge the request:
                        ackRequest.sendAckData("getDataAcknowledgement");

                        // handle the "action" in the data object sent in
                        String responseToSend = handleDataAction(action, data);

                        // generate the response packet to send back to the client ( node server )
                        String resObjSerialized = buildResponsePacket(responseToSend);

                        // send the response back to the client ( node server )
                        client.sendEvent("serverResponse", new VoidAckCallback(){
                            @Override
                            protected void onSuccess() {}
                        }, resObjSerialized);
                    }

                    @Override
                    public void failure(Object object) {
                        // attempted connection from __.
                        String id = (String)object;
                        // an unknown client tried to connect but is not getting in. HAHA!
                        System.out.println("Attempted Connection from unauthorized client id: " + id);
                    }
                });
            }
        });

        server.start(); // start the server
        System.out.println("[JAVA SERVER INFO] Java server started. Running on port: " + PORT);
    }

    /**
     * builds a serialized json string to send back to the client
     * @param msgToSend
     * @return
     */
    //TODO: change this method to send other data back later on
    private static String buildResponsePacket(String msgToSend) {

        // build data object with msg
        JsonObject data = new JsonObject();
        data.add("msg", new JsonPrimitive(msgToSend));

        BaseRequest response = new BaseRequest(SERVER_ID, data);

        return new Gson().toJson(response); // return it as a string so that it can be send via socket
    }

    /**
     * Dispatch method using "action" from data request
     *
     * handle the action send in - get whatever they want and return it.
     *
     * @param action
     * @param data
     * @return
     */
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

    /**
     * Handles validating the client to ensure they are an allowed client.
     *
     * TODO: change hardcoded id into a secure hash to check.
     *
     * @param response
     * @param callback
     */
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
