package land.cookie.cordova.plugin.zebrascanner;

import java.util.ArrayList;
import java.util.HashMap;
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

import com.google.gson.JsonObject;
import com.zebra.scannercontrol.SDKHandler;
import com.zebra.scannercontrol.DCSSDKDefs;
import com.zebra.scannercontrol.DCSScannerInfo;
import com.zebra.scannercontrol.BarCodeView;

public class ZebraScanner extends CordovaPlugin {
    private static final String TAG = "clZebraScanner";

    private SDKHandler sdkHandler; // Zebra SDK
    private NotificationReceiver notificationReceiver;
    private CallbackContext eventEmitter;
    private CallbackContext scanCallBack;

    // Barcode scanner stuff
    private static ArrayList<DCSScannerInfo> mSNAPIList = new ArrayList<DCSScannerInfo>();
    private static ArrayList<DCSScannerInfo> mScannerInfoList = new ArrayList<DCSScannerInfo>();
//    private static HashMap<int, Object>

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
        else if ("getActiveScanners".equals(action))
            getActiveScannersAction();
        else if ("connect".equals(action))
            connectAction(args, callbackContext);
        else if ("disconnect".equals(action))
            disconnectAction(args, callbackContext);
        else if ("subscribe".equals(action))
            subscribeAction(args, callbackContext);
        else
            return false;

        // Checks to see if a scanner is connected or not.
//        if (action.equalsIgnoreCase("getScannerInfo")) {
//            final int scannerIndex;
//            if (!args.isNull(0)) {
//                scannerIndex = args.getInt(0);
//            } else {
//                scannerIndex = 0;
//            }
//
//            cordova.getActivity().runOnUiThread(new Runnable() {
//                public void run() {
//                    if (sdkHandler == null) {
//                        init();
//                    }
//
//                    try {
//                        JSONObject result = getScannerInfo(scannerIndex);
//
//                        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, result));
//                    } catch(JSONException err) {
//                        Log.e(TAG, "ERROR sending scanner info response.");
//                    }
//                }
//            });
//            return true;
//        }
//
//        // Create a reusable callback context for the native event handlers.
//        if (action.equalsIgnoreCase("handleEvents")) {
//            eventEmitter = callbackContext;
//            return true;
//        }

        return true;
    }

    @Override
    public void onDestroy() {
        notificationReceiver = null;
        sdkHandler = null;

        super.onDestroy();
    }

    private void init() {
        Log.d(TAG, "Setting up scanner SDK.");
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

        sdkHandler.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_SNAPI);
        sdkHandler.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_BT_NORMAL);
        sdkHandler.dcssdkSubsribeForEvents(notifications_mask);
        sdkHandler.dcssdkSetDelegate(notificationReceiver);
    }

    private void startScanAction(CallbackContext callbackContext) throws JSONException {
        if (scanCallBack != null) {
            callbackContext.error("Scanning is already in a progress.");
            return;
        }
        sdkHandler.dcssdkEnableAvailableScannersDetection(true);

        scanCallBack = callbackContext;
        PluginResult message = createStatusMessage("scanStart", true);
        scanCallBack.sendPluginResult(message);
    }

    private void stopScanAction(CallbackContext callbackContext) throws JSONException {
        if (scanCallBack == null) {
            callbackContext.error("Scanning was not in a progress.");
            return;
        }

        sdkHandler.dcssdkEnableAvailableScannersDetection(false);

        callbackContext.success("Scanning was stopped.");
        PluginResult message = createStatusMessage("scanStop", false);
        scanCallBack.sendPluginResult(message);
        scanCallBack = null;
    }

    private void getActiveScannersAction() {

    }

    private void connectAction(JSONArray args, CallbackContext callbackContext) {

    }

    private void disconnectAction(JSONArray args, CallbackContext callbackContext) {
//        sdkHandler.dcssdkTerminateCommunicationSession();
    }

    private void subscribeAction(JSONArray args, CallbackContext callbackContext) {

    }

    public void notifyScannerFound(DCSScannerInfo scannerInfo) throws JSONException {
        if (scanCallBack == null)
            return;

        JSONObject scanner = new JSONObject();
        scanner.put("id", scannerInfo.getScannerID());
        scanner.put("name", scannerInfo.getScannerName());
        scanner.put("model", scannerInfo.getScannerModel());
        scanner.put("serialNumber", scannerInfo.getScannerHWSerialNumber());

        PluginResult message = createStatusMessage("scannerFound","scanner", scanner, true);
        scanCallBack.sendPluginResult(message);
    }

    // Not exactly sure if this is a correct callback. TODO: test if possible
    public void notifyScannerLost(int scannerId) throws JSONException {
        if (scanCallBack == null)
            return;

        JSONObject scanner = new JSONObject();
        scanner.put("id", scannerId);

        PluginResult message = createStatusMessage("scannerLost","scanner", scanner, true);
        scanCallBack.sendPluginResult(message);
    }

    public void notifyScannerConnected() throws JSONException {
        if (eventEmitter == null)
            return;

        JSONObject result = new JSONObject();

        result.put("eventType", "scannerConnected");

        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, result);
        pluginResult.setKeepCallback(true);
        eventEmitter.sendPluginResult(pluginResult);
    }

    public void notifyScannerDisconnected(int scannerId) throws JSONException {
        if (eventEmitter == null)
            return;

        JSONObject result = new JSONObject();
        JSONObject data = new JSONObject();

        data.put("scannerId", scannerId);

        result.put("data", data);
        result.put("eventType", "scannerDisconnected");

        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, result);
        pluginResult.setKeepCallback(true);
        eventEmitter.sendPluginResult(pluginResult);
    }

    public void notifyBarcodeReceived(String barcodeData, String barcodeType, int fromScannerId) throws JSONException {
        if (eventEmitter == null)
            return;

        JSONObject result = new JSONObject();
        JSONObject data = new JSONObject();

        data.put("scannerId", fromScannerId);
        data.put("barcodeType", barcodeType);
        data.put("barcodeData", barcodeData);

        result.put("data", data);
        result.put("eventType", "barcodeReceived");

        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, result);
        pluginResult.setKeepCallback(true);
        eventEmitter.sendPluginResult(pluginResult);
    }

    // Attempts to get connected USB scanner info.
    // If one is not found, send a pairing barcode (base64 encoded JPEG) back to client.
    public JSONObject getScannerInfo(int idx) throws JSONException {
        // USB Scanner detection
        mSNAPIList.clear();
        updateScannerList();
        for (DCSScannerInfo device:getActualScannersList()) {
            if (device.getConnectionType() == DCSSDKDefs.DCSSDK_CONN_TYPES.DCSSDK_CONNTYPE_USB_SNAPI) {
                mSNAPIList.add(device);
                Log.d(TAG, "--------FOUND USB SCANNER--------");
            }
        }

        // If no scanners, we need to send a pairing barcode
        // Else attach the requested or default scanner.
        if (mSNAPIList.size() == 0) {
            Log.d(TAG, "No USB scanners found");
            String connectionBarcode = getSnapiBarcode();

            JSONObject result = new JSONObject();
            result.put("status", "pairingRequired");
            result.put("connectionBarcode", connectionBarcode);

            return result;
        } else {
            Log.d(TAG, mSNAPIList.size() + " scanners found");

            if (idx + 1 > mSNAPIList.size()) {
                idx = 0;
            }

            int scannerId = mSNAPIList.get(idx).getScannerID();

            sdkHandler.dcssdkEstablishCommunicationSession(scannerId);

            JSONObject result = new JSONObject();

            result.put("status", "paired");
            result.put("message", "Paired to scanner: " + scannerId);

            return result;
        }
    }

    private void updateScannerList() {
        if (sdkHandler != null) {
            mScannerInfoList.clear();
            sdkHandler.dcssdkGetAvailableScannersList(mScannerInfoList);
            sdkHandler.dcssdkGetActiveScannersList(mScannerInfoList);
        }
    }

    private List<DCSScannerInfo> getActualScannersList() {
        return mScannerInfoList;
    }

    // The official docs have this display a native view
    // with the barcode. Here we prefer to send the code back
    // to JS to deal with.
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
}
