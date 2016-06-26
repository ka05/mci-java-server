package com.mci.models;

import com.google.gson.JsonObject;

/**
 * Created by clay on 6/26/16.
 */
public class BaseRequest {
    public String id;
    public JsonObject data;

    public BaseRequest(String id, JsonObject data){
        this.id = id;
        this.data = data;
    }
}
