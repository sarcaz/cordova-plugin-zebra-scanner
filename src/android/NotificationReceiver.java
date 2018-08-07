package land.cookie.cordova.plugin.zebrascanner;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.util.Log;

import com.zebra.scannercontrol.DCSSDKDefs;
import com.zebra.scannercontrol.DCSScannerInfo;
import com.zebra.scannercontrol.FirmwareUpdateEvent;
import com.zebra.scannercontrol.IDcsSdkApiDelegate;

import land.cookie.cordova.plugin.zebrascanner.barcode.BarcodeTypes;

import org.json.JSONException;

public class NotificationReceiver extends BroadcastReceiver implements IDcsSdkApiDelegate {
    private static final String TAG = "clZebraScanner";

    NotificationReceiver() {
        Log.d(TAG, "Setting up scanner SDK.");

        int notifications_mask = 0;
        // Subscribe to scanner available/unavailable events
        notifications_mask |=
        	DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SCANNER_APPEARANCE.value
        	| DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SCANNER_DISAPPEARANCE.value;

        // Subscribe to scanner connection/disconnection events
        notifications_mask |=
        	DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SESSION_ESTABLISHMENT.value
        	| DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SESSION_TERMINATION.value;

        // Subscribe to barcode events
        notifications_mask |= DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_BARCODE.value;

        // ZebraScanner.sdkHandler.dcssdkEnableAvailableScannersDetection(true); TODO move this
        ZebraScanner.sdkHandler.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_SNAPI);
        ZebraScanner.sdkHandler.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_BT_NORMAL);
        ZebraScanner.sdkHandler.dcssdkSubsribeForEvents(notifications_mask);

        ZebraScanner.sdkHandler.dcssdkSetDelegate(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) { // TODO: delete?
//        NotificationManager mgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

//        String notificationText = intent.getStringExtra("notifications_text");
//        int notificationType = intent.getIntExtra("notifications_type", 0);

//        Log.d(TAG, notificationText);
    }

    @Override
    public void dcssdkEventScannerAppeared(DCSScannerInfo scanner) {
        Log.d(TAG, "GOT DCSScanner Info - Scanner Appeared");
        try {
            ZebraScanner.broadcastScannerFound(scanner);
        } catch(JSONException err) {
            Log.e(TAG, "ERROR broadcasting puggedin event.");
        }
    }

    @Override
    public void dcssdkEventScannerDisappeared(int scannerId) {
        // ZebraScanner.sdkHandler.dcssdkTerminateCommunicationSession(scannerId); TODO: move this?
        Log.d(TAG, "GOT DCSScanner Info - Scanner Disappeared");
        try {
            ZebraScanner.broadcastScannerLost(scannerId);
        } catch(JSONException err) {
            Log.e(TAG, "ERROR broadcasting unplugged event.");
        }
    }

    @Override
    public void dcssdkEventCommunicationSessionEstablished(DCSScannerInfo var1) {
        Log.d(TAG, "GOT DCSScanner Info - Communication Session Established");
        try {
            ZebraScanner.broadcastScannerConnected();
        } catch(JSONException err) {
            Log.e(TAG, "ERROR broadcasting connected event.");
        }
    }

    @Override
    public void dcssdkEventCommunicationSessionTerminated(int var1) {
        Log.d(TAG, "GOT DCSScanner Info - Communication Session Terminated");
        try {
            ZebraScanner.broadcastScannerDisconnected();
        } catch(JSONException err) {
            Log.e(TAG, "ERROR broadcasting disconnected event.");
        }
    }

    @Override
    public void dcssdkEventBarcode(byte[] barcodeData, int barcodeType, int fromScannerId) {
        Log.d(TAG, "GOT DCSScanner Info - Got Barcode");
        Log.d(TAG, "\nType: " + BarcodeTypes.getBarcodeTypeName(barcodeType) + ".\n From scanner: " + fromScannerId + ".\n Data: " + new String(barcodeData));
        try {
            ZebraScanner.broadcastBarcodeReceived(new String(barcodeData), BarcodeTypes.getBarcodeTypeName(barcodeType), fromScannerId);
        } catch(JSONException err) {
            Log.e(TAG, "ERROR broadcasting barcode.");
        }
    }

    @Override
    public void dcssdkEventImage(byte[] var1, int var2) {
        Log.d(TAG, "GOT Image?");
    }

    @Override
    public void dcssdkEventVideo(byte[] var1, int var2) {
        Log.d(TAG, "GOT DCSScanner Info - Got Video??");
    }

    @Override
    public void dcssdkEventFirmwareUpdate(FirmwareUpdateEvent var1) {
        Log.d(TAG, "GOT DCSScanner Info - Firmware Update Event");
    }

    @Override
    public void dcssdkEventAuxScannerAppeared(DCSScannerInfo dcsScannerInfo, DCSScannerInfo dcsScannerInfo1) {
        Log.d(TAG, "GOT DCSScanner Info - Firmware Update Event");
    }
}
