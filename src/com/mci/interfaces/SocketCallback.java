package com.mci.interfaces;

import com.google.gson.JsonObject;

/**
 * Created by clay on 6/26/16.
 */
public interface SocketCallback {

    // uses JsonObject so no casting is necessary after coming from middleware call
    void success(String action, JsonObject data);

    // uses Object to allow various types of errors ( String, JsonObject, Exception, etc. )
    void failure(Object object);
}
