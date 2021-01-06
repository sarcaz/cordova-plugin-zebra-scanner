package land.cookie.cordova.plugin.zebrarfid;

import android.util.Log;

import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.MEMORY_BANK;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.RFIDReader;
import com.zebra.rfid.api3.ReaderDevice;
import com.zebra.rfid.api3.TAG_FIELD;
import com.zebra.rfid.api3.TagData;
import com.zebra.rfid.api3.TagStorageSettings;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import land.cookie.cordova.plugin.zebrarfid.rfid.RFIDHandler;
import land.cookie.cordova.plugin.zebrarfid.rfid.SDKResult;

public class ZebraRFID extends CordovaPlugin {

    public static String TAG = "ZebraRFID";
    private static CallbackContext onEventCallback = null;       // subscriber per gli eventi di info

    private RFIDHandler sdkHandler = null;
    private ArrayList<ReaderDevice> availableRFIDReaderList;
    private ReaderDevice connectedReader = null;

    private CallbackContext onTagCallback = null;

    @Override
    public boolean execute (
            String action, final JSONArray args, final CallbackContext callbackContext
    ) throws JSONException {
        if (sdkHandler == null) init();

        switch (action){
            case "getAvailableRFIDReaders":getAvailableRFIDReadersAction(callbackContext);return true;
            case "scanAvailableRFIDReaders":scanAvailableRFIDReadersAction(callbackContext);return true;
            case "connect":connectAction(args, callbackContext);return true;
            case "disconnect":disconnectAction(callbackContext);return true;
            case "getDeviceStatus":getDeviceStatusAction(callbackContext);return true;
            case "getConnectedDevice":getConnectedDeviceAction(callbackContext);return true;
            case "startScan":startScanAction(callbackContext);return true;
            case "stopScan": stopScanAction(callbackContext);return true;
            default:
                Log.w(TAG, "Azione non supportata");
                return false;
        }
    }

    private void init() {
        Log.d(TAG, "Setting up Zebra SDK.");
        sdkHandler = new RFIDHandler(this.cordova.getActivity().getApplicationContext(), this);
    }

    // region ACTIONS

    private void connectAction(JSONArray params, CallbackContext callbackContext) throws JSONException{
        JSONObject param = params.optJSONObject(0);
        if (param == null) {
            callbackContext.error(SDKResult.errorResult("Missing parameters address"));
            return;
        }
        String address = param.optString("address");
        if ( address==null || address.isEmpty()) {
            callbackContext.error(SDKResult.errorResult("Invalid parameter - address"));
            return;
        }

        // check if is same device connected
        if(this.connectedReader != null && this.connectedReader.getAddress().equals(address)){
            Log.d(TAG, "ReConnectionTask");
            try{
                SDKResult res = this.sdkHandler.reconnect(this.connectedReader.getRFIDReader());
                if(res.error){
                    callbackContext.error(res.toJson());
                }else{
                    callbackContext.success(SDKResult.successResult(readerDeviceToJson(this.connectedReader)));
                }
            }catch (Exception e){
                Log.e(TAG, "Errore riconnessione");
                Log.e(TAG, "exception: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // find reader
        for (ReaderDevice readerDevice : this.availableRFIDReaderList) {
            if(readerDevice.getAddress().equals(address)){
                Log.d(TAG, "ConnectionTask");
                RFIDReader rfidReader = readerDevice.getRFIDReader();
                SDKResult res = this.sdkHandler.connect(rfidReader);
                Log.d(TAG, "IsConnected? " + rfidReader.isConnected());
                if(res.error){
                    callbackContext.error(res.toJson());
                }else{
                    this.connectedReader = readerDevice;
                    callbackContext.success(SDKResult.successResult(readerDeviceToJson(this.connectedReader)));
                }
            }
        }
    }

    private void disconnectAction(CallbackContext callbackContext) {
        Log.d(TAG, "disconnectAction");
        this.sdkHandler.disconnect(this.connectedReader.getRFIDReader());
        this.connectedReader = null;
        callbackContext.success("ok");
    }

    private void stopScanAction(CallbackContext callbackContext) {
        Log.d(TAG, "stopScanAction");

        if(this.sdkHandler == null){
            callbackContext.error(SDKResult.errorResult("plugin not initialized is null"));
            return;
        }

        if (this.connectedReader == null) {
            callbackContext.error(SDKResult.errorResult("no connected reader"));
            return;
        }

        this.sdkHandler.setCanScan(false);

        PluginResult message = new PluginResult(PluginResult.Status.OK, "stop_scan");
        if(this.onTagCallback != null){
            this.onTagCallback.sendPluginResult(message);
            this.onTagCallback = null;
        }

        callbackContext.success(SDKResult.successResult("ok"));
    }

    private void startScanAction(CallbackContext callbackContext) {
        Log.d(TAG, "startScanAction");

        if(this.connectedReader == null){
            callbackContext.error("device_not_connected");
        }else{

            try {
                // Configuro cosa voglio leggere
                TagStorageSettings tagStorageSettings = null;
                tagStorageSettings = this.connectedReader.getRFIDReader().Config.getTagStorageSettings();
                TAG_FIELD[] tagField = new TAG_FIELD[1];
                tagField[0] = TAG_FIELD.ALL_TAG_FIELDS;
                tagStorageSettings.setTagFields(tagField);

                this.connectedReader.getRFIDReader().Config.setTagStorageSettings(tagStorageSettings);

            } catch (InvalidUsageException | OperationFailureException e) {
                Log.d(TAG, "startScanAction - Impossibile impostare le impostazioni del lettore");
                e.printStackTrace();
            }

            this.onTagCallback = callbackContext;
            this.sdkHandler.setCanScan(true);

            PluginResult message = new PluginResult(PluginResult.Status.OK, "ok");
            message.setKeepCallback(true);
            callbackContext.sendPluginResult(message);
        }
    }

    private void scanAvailableRFIDReadersAction(CallbackContext callbackContext) throws JSONException {
      /*
        Overwrite all device
      */

        Log.d(TAG, "scanAvailableRFIDReadersAction");

        if(this.connectedReader != null){
            SDKResult res = new SDKResult(true, "Device already connected");
            res.code = "device_already_connected";
            callbackContext.error(res.toJson());
            return;
        }

        JSONArray devices = new JSONArray();
        this.availableRFIDReaderList = sdkHandler.scanAvailableRFIDReaderList();
        for (ReaderDevice deviceInfo : this.availableRFIDReaderList) {
            devices.put(readerDeviceToJson(deviceInfo));
        }
        callbackContext.success(new SDKResult(devices).toJson());
    }

    private void getAvailableRFIDReadersAction(CallbackContext callbackContext) throws JSONException {

        if(this.availableRFIDReaderList == null){
//            SDKResult res = new SDKResult(true, "RFIDHandler not initialized");
//            callbackContext.error(res.toJson());
            JSONArray devices = new JSONArray();
            callbackContext.success(devices);
            return;
        }

        JSONArray devices = new JSONArray();
        for (ReaderDevice deviceInfo : this.availableRFIDReaderList) {
            devices.put(readerDeviceToJson(deviceInfo));
        }

        callbackContext.success(SDKResult.successResult(devices));
    }

    private void getDeviceStatusAction(CallbackContext callbackContext){

        ZebraRFID.onEventCallback = callbackContext;

        if(this.sdkHandler.getDeviceStatus()){
            PluginResult message = new PluginResult(PluginResult.Status.OK, "ok");
            message.setKeepCallback(true);
            ZebraRFID.onEventCallback.sendPluginResult(message);

        }else{
            callbackContext.error(SDKResult.errorResult("ko"));
        }
    }

    private void getConnectedDeviceAction(CallbackContext callbackContext) {
        Log.d(TAG, "getConnectedDeviceAction");
        if(this.connectedReader == null){
            callbackContext.error(SDKResult.errorResult("Device Not connected", "not_connected"));
            return;
        }
        try {
            JSONObject res = SDKResult.successResult(this.readerDeviceToJson(this.connectedReader));
            callbackContext.success(res);
        } catch (JSONException e) {
            e.printStackTrace();
            callbackContext.error("unable to parse object");
        }
    }

    // endregion ACTIONS

    public void notifyReadTag(TagData tag) {
        if(this.onTagCallback == null) return;
        Log.d(TAG, "onReadTag: " + tag.getTagID());

        JSONObject res = new JSONObject();

        try {
            res.put("epc", tag.getTagID());
            res.put("memory_bank_data", tag.getMemoryBankData());
            res.put("memory_bank_size", tag.getMemoryBankDataAllocatedSize());
            res.put("memory_bank_offset", tag.getMemoryBankDataOffset());
            res.put("number_words", tag.getNumberOfWords());
            res.put("pc", tag.getPC());
            res.put("seen_count", tag.getTagSeenCount());
            res.put("has_location", tag.isContainsLocationInfo());
            res.put("crc", tag.getCRC());

            if(tag.LocationInfo != null){
                res.put("distance", tag.LocationInfo.getRelativeDistance());
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        PluginResult message = new PluginResult(PluginResult.Status.OK, res);
        message.setKeepCallback(true);
        this.onTagCallback.sendPluginResult(message);
    }

    public void notifyEvent(String event, Object value) {

        Log.d(TAG, "notifyEvent");

        if(ZebraRFID.onEventCallback == null) {
            Log.d(TAG, "notifyEvent: onEventCallback is null");
            return;
        }

        if(event == null) {
            Log.d(TAG, "notifyEvent: event is null");
            return;
        };

        Log.d(TAG, "event: " + event + ": " + value);
        try {
            JSONObject ris = new JSONObject();
            ris.put("event", event);
            ris.put("value", value);
            PluginResult message = new PluginResult(PluginResult.Status.OK, ris);
            message.setKeepCallback(true);

            if(ZebraRFID.onEventCallback != null) ZebraRFID.onEventCallback.sendPluginResult(message);

        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
            e.printStackTrace();
        }
    }

    private JSONObject readerDeviceToJson(ReaderDevice readerDevice) throws JSONException {
        JSONObject device = new JSONObject();
        RFIDReader reader = readerDevice.getRFIDReader();
        device.put("address", readerDevice.getAddress());
        device.put("name", readerDevice.getName());
        device.put("password", readerDevice.getPassword());
        device.put("isConnected", reader.isConnected());
        return device;
    }

}


