package land.cookie.cordova.plugin.zebrascanner;

import java.util.ArrayList;
import java.util.List;
import java.io.ByteArrayOutputStream;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Base64;
import android.util.Log;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.zebra.scannercontrol.SDKHandler;
import com.zebra.scannercontrol.DCSSDKDefs;
import com.zebra.scannercontrol.DCSScannerInfo;
import com.zebra.scannercontrol.BarCodeView;

public class ZebraScanner extends CordovaPlugin {
    private static final String TAG = "clZebraScanner";

    private SDKHandler sdkHandler; // Zebra SDK
    private NotificationReceiver notificationReceiver;
    private CallbackContext scanCallBack;
    // SDK does not support multiple connected devices.
    private CallbackContext connectionCallBack;
    private CallbackContext subscriptionCallback;

    @Override
    public boolean execute (
            String action, final JSONArray args, final CallbackContext callbackContext
    ) throws JSONException {
        if (sdkHandler == null)
            init();

        if ("startScan".equals(action))
            startScanAction(callbackContext);
        else if ("stopScan".equals(action))
            stopScanAction(callbackContext);
        else if ("getAvailableDevices".equals(action))
            getAvailableDevicesAction(callbackContext);
        else if ("getActiveDevices".equals(action))
            getActiveDevicesAction(callbackContext);
        else if ("getPairingBarcode".equals(action))
            getPairingBarcodeAction(callbackContext);
        else if ("connect".equals(action))
            connectAction(args, callbackContext);
        else if ("disconnect".equals(action))
            disconnectAction(args, callbackContext);
        else if ("subscribe".equals(action))
            subscribeAction(callbackContext);
        else if ("unsubscribe".equals(action))
            unsubscribeAction(callbackContext);
        else
            return false;
        return true;
    }

    private void init() {
        Log.d(TAG, "Setting up Zebra SDK.");
        sdkHandler = new SDKHandler(this.cordova.getActivity().getApplicationContext());
        notificationReceiver = new NotificationReceiver(this);

        // Subscribe to barcode events
        int notifications_mask = DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_BARCODE.value
        // Subscribe to scanner available/unavailable events
                | DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SCANNER_APPEARANCE.value
                | DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SCANNER_DISAPPEARANCE.value
        // Subscribe to scanner connection/disconnection events
                | DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SESSION_ESTABLISHMENT.value
                | DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SESSION_TERMINATION.value;

        sdkHandler.dcssdkSubsribeForEvents(notifications_mask);
        sdkHandler.dcssdkSetDelegate(notificationReceiver);
        sdkHandler.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_BT_NORMAL);
        // Warning: This argument does nothing
//        sdkHandler.dcssdkEnableAvailableScannersDetection(true);
    }

    // There is a bug in Zebra SDK so this method is pointless for now.
    private void startScanAction(CallbackContext callbackContext) throws JSONException {
        if (scanCallBack != null) {
            callbackContext.error("Scanning is already in a progress.");
            return;
        }

        Log.d(TAG, "Starting scan.");
        sdkHandler.dcssdkStartScanForAvailableDevices();

        scanCallBack = callbackContext;
        PluginResult message = createStatusMessage("scanStart", true);
        scanCallBack.sendPluginResult(message);
    }

    // There is a bug in Zebra SDK so this method is pointless for now.
    private void stopScanAction(CallbackContext callbackContext) throws JSONException {
        if (scanCallBack == null) {
            callbackContext.error("Scanning was not in a progress.");
            return;
        }

        Log.d(TAG, "Stopping scan.");
        sdkHandler.dcssdkStopScanningDevices();

        callbackContext.success("Scanning was stopped.");
        PluginResult message = createStatusMessage("scanStop", false);
        scanCallBack.sendPluginResult(message);
        scanCallBack = null;
    }

    private void getAvailableDevicesAction(CallbackContext callbackContext) throws JSONException {
        List<DCSScannerInfo> deviceInfos = new ArrayList<DCSScannerInfo>();
        sdkHandler.dcssdkGetAvailableScannersList(deviceInfos);

        JSONArray devices = new JSONArray();
        for (DCSScannerInfo deviceInfo : deviceInfos) {
            devices.put(createScannerDevice(deviceInfo));
        }
        callbackContext.success(devices);
    }

    private void getActiveDevicesAction(CallbackContext callbackContext) throws JSONException {
        List<DCSScannerInfo> deviceInfos = new ArrayList<DCSScannerInfo>();
        sdkHandler.dcssdkGetActiveScannersList(deviceInfos);

        JSONArray devices = new JSONArray();
        for (DCSScannerInfo deviceInfo : deviceInfos) {
            devices.put(createScannerDevice(deviceInfo));
        }
        callbackContext.success(devices);
    }

    private void getPairingBarcodeAction(CallbackContext callbackContext) {
        String barcode = getSnapiBarcode();
        callbackContext.success(barcode);
    }

    private void connectAction(JSONArray params, CallbackContext callbackContext) {
        JSONObject param = params.optJSONObject(0);
        if (param == null) {
            callbackContext.error("Missing parameter");
            return;
        }
        int deviceId = param.optInt("deviceId"); // TODO:
        if (deviceId == 0) {
            callbackContext.error("Invalid parameter - deviceId");
            return;
        }

        Log.d(TAG, "Connecting to scanner " + deviceId + ".");
        DCSSDKDefs.DCSSDK_RESULT result = sdkHandler.dcssdkEstablishCommunicationSession(deviceId);

        if (result == DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_SUCCESS) {
            connectionCallBack = callbackContext;
        }
        else {
            if (result == DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_SCANNER_NOT_AVAILABLE)
                callbackContext.error("Scanner " + deviceId + " is not available.");
            else if (result == DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_SCANNER_ALREADY_ACTIVE)
                callbackContext.error("Already connected to scanner " + deviceId + ".");
            else
                callbackContext.error("Unable to connect to scanner " + deviceId + ".");
            Log.d(TAG, "Connection to scanner " + deviceId + " failed.");
        }
    }

    private void disconnectAction(JSONArray params, CallbackContext callbackContext) {
        JSONObject param = params.optJSONObject(0);
        if (param == null) {
            callbackContext.error("Missing parameter");
            return;
        }
        int deviceId = param.optInt("deviceId");
        if (deviceId == 0) {
            callbackContext.error("Invalid parameter - deviceId");
            return;
        }

        Log.d(TAG, "Disconnecting from scanner " + deviceId + ".");
        DCSSDKDefs.DCSSDK_RESULT result = sdkHandler.dcssdkTerminateCommunicationSession(deviceId);

        if (result == DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_SUCCESS) {
            callbackContext.success("ok");
        }
        else {
            if (result == DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_SCANNER_NOT_AVAILABLE)
                callbackContext.error("Scanner " + deviceId + " is not available.");
            else if (result == DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_SCANNER_NOT_ACTIVE)
                callbackContext.error("Never connected to scanner " + deviceId + ".");
            else
                callbackContext.error("Unable to disconnect from scanner " + deviceId + ".");
            Log.d(TAG, "Connection to scanner " + deviceId + " failed.");
        }
    }

    private void subscribeAction(CallbackContext callbackContext) {
        if (connectionCallBack == null) {
            callbackContext.error("No connected scanner");
            return;
        }

        subscriptionCallback = callbackContext;
    }

    private void unsubscribeAction(CallbackContext callbackContext) {
        if (subscriptionCallback == null) {
            callbackContext.error("No active subscription");
            return;
        }

        subscriptionCallback = null;
    }

    public void notifyDeviceFound(DCSScannerInfo deviceInfo) throws JSONException {
        if (scanCallBack == null)
            return;

        JSONObject device = createScannerDevice(deviceInfo);

        PluginResult message = createStatusMessage("deviceFound","device", device, true);
        scanCallBack.sendPluginResult(message);
    }

    public void notifyDeviceLost(int deviceId) throws JSONException {
        if (scanCallBack == null)
            return;

        JSONObject device = new JSONObject();
        device.put("id", deviceId);

        PluginResult message = createStatusMessage("deviceLost","device", device, true);
        scanCallBack.sendPluginResult(message);
    }

    public void notifyDeviceConnected(DCSScannerInfo deviceInfo) throws JSONException {
        if (connectionCallBack == null)
            return;

        JSONObject device = new JSONObject();
        device.put("id", deviceInfo.getScannerID());

        PluginResult message = createStatusMessage("connected", "device", device, true);
        connectionCallBack.sendPluginResult(message);
        Log.d(TAG, "Connection to scanner " + deviceInfo.getScannerID() + " was successful.");
    }

    public void notifyDeviceDisconnected(int deviceId) throws JSONException {
        if (connectionCallBack == null)
            return;

        JSONObject device = new JSONObject();
        device.put("id", deviceId);

        PluginResult message = createStatusMessage("disconnected", "device", device, false);
        connectionCallBack.sendPluginResult(message);
        connectionCallBack = null;
        subscriptionCallback = null;
        Log.d(TAG, "Scanner " + deviceId + " was disconnected.");
    }

    public void notifyBarcodeReceived(String barcodeData, String barcodeType, int fromScannerId) throws JSONException {
        if (subscriptionCallback == null)
            return;

        JSONObject data = new JSONObject();
        data.put("deviceId", fromScannerId);

        JSONObject barcode = new JSONObject();
        barcode.put("type", barcodeType);
        barcode.put("data", barcodeData);
        data.put("barcode", barcode);

        PluginResult message = new PluginResult(PluginResult.Status.OK, data);
        message.setKeepCallback(true);
        subscriptionCallback.sendPluginResult(message);
    }

    private PluginResult createStatusMessage(String status, boolean keepCallback) throws JSONException {
        return createStatusMessage(status, null, null, keepCallback);
    }

    private PluginResult createStatusMessage(String status, String dataKey, JSONObject data, boolean keepCallback)
            throws JSONException {
        JSONObject msgData = new JSONObject();
        msgData.put("status", status);

        if (dataKey != null && !dataKey.isEmpty() && data != null) {
            msgData.put(dataKey, data);
        }

        PluginResult message = new PluginResult(PluginResult.Status.OK, msgData);
        message.setKeepCallback(keepCallback);
        return message;
    }

    private JSONObject createScannerDevice(DCSScannerInfo scanner) throws JSONException {
        JSONObject device = new JSONObject();
        device.put("id", scanner.getScannerID());
        device.put("name", scanner.getScannerName());
        device.put("model", scanner.getScannerModel());
        device.put("serialNumber", scanner.getScannerHWSerialNumber());
        return device;
    }

    private String getSnapiBarcode() {
        BarCodeView barCodeView = sdkHandler.dcssdkGetUSBSNAPIWithImagingBarcode();
        barCodeView.setSize(500, 100);
        return base64Encode(getBitmapFromView(barCodeView), Bitmap.CompressFormat.JPEG, 100);
    }

    // Convert native view to bitmap
    private Bitmap getBitmapFromView(BarCodeView view) {
        Log.d(TAG, view.getWidth() + " " + view.getHeight());
        Bitmap converted = Bitmap.createBitmap(500, 100, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(converted);
        Drawable bgDrawable = view.getBackground();

        if (bgDrawable != null) {
            bgDrawable.draw(canvas);
        } else {
            canvas.drawColor(Color.WHITE);
        }

        view.draw(canvas);

        return converted;
    }

    private String base64Encode(Bitmap image, Bitmap.CompressFormat format, int quality) {
        ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
        image.compress(format, quality, byteArrayStream);
        return Base64.encodeToString(byteArrayStream.toByteArray(), Base64.DEFAULT);
    }
}
