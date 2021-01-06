package land.cookie.cordova.plugin.zebrarfid.rfid;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SDKResult {
    public Boolean error;
    public String message;
    public String code;
    public Object data;

    public SDKResult(JSONObject data){
        this.error = false;
        this.data = data;
    }

    public SDKResult(JSONArray data){
        this.error = false;
        this.data = data;
    }

    public SDKResult(Boolean error, String message){
        this.error = error;
        this.message = message;
    }

    private SDKResult(Boolean error, JSONObject data){
        this.error = error;
        this.data = data;
    }

    private SDKResult(Boolean error, JSONArray data){
        this.error = error;
        this.data = data;
    }

    /////

    public static JSONObject errorResult(String message){
        SDKResult res = new SDKResult(true, message);
        return res.toJson();
    }

    public static JSONObject errorResult(String message, String code){
        SDKResult res = new SDKResult(true, message);
        res.code = code;
        return res.toJson();
    }

    public static JSONObject successResult(String message){
        SDKResult res = new SDKResult(false, message);
        return res.toJson();
    }

    public static JSONObject successResult(JSONObject data){
        SDKResult res = new SDKResult(false, data);
        return res.toJson();
    }

    public static JSONObject successResult(JSONArray data){
        SDKResult res = new SDKResult(false, data);
        return res.toJson();
    }

    public JSONObject toJson() {
        JSONObject res = new JSONObject();
        try {
            res.put("error", this.error);
            res.put("message", this.message);
            res.put("data", this.data);
            res.put("code", this.code);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return res;
    }

}
