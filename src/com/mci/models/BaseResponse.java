package com.mci.models;

import com.google.gson.JsonObject;

/**
 * Created by clay on 6/26/16.
 */
public class BaseResponse {
    public String id;
    public String action;
    public JsonObject data;

    public BaseResponse(String id, String action, JsonObject data){
        this.id = id;
        this.action = action;
        this.data = data;
    }
}
