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
    private ZebraScanner mScanner;

    NotificationReceiver(ZebraScanner scanner) {
        mScanner = scanner;
    }

    @Override
    public void onReceive(Context context, Intent intent) { // TODO: delete?
//        NotificationManager mgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

//        String notificationText = intent.getStringExtra("notifications_text");
//        int notificationType = intent.getIntExtra("notifications_type", 0);

//        Log.d(TAG, notificationText);
        Log.d(TAG, "onReceive came !!!");
    }

    @Override
    public void dcssdkEventScannerAppeared(DCSScannerInfo scanner) {
        Log.d(TAG, "Scanner Appeared");
        try {
            mScanner.notifyScannerFound(scanner);
        } catch(JSONException err) {
            Log.e(TAG, "ERROR notifying appeared event.");
        }
    }

    @Override
    public void dcssdkEventScannerDisappeared(int scannerId) {
        Log.d(TAG, "Scanner Disappeared");
        try {
            mScanner.notifyScannerLost(scannerId);
        } catch(JSONException err) {
            Log.e(TAG, "ERROR notifying disappeared event.");
        }
    }

    @Override
    public void dcssdkEventCommunicationSessionEstablished(DCSScannerInfo scanner) {
        Log.d(TAG, "Communication Session Established");
        try {
            mScanner.notifyScannerConnected();
        } catch(JSONException err) {
            Log.e(TAG, "ERROR notifying connected event.");
        }
    }

    @Override
    public void dcssdkEventCommunicationSessionTerminated(int scannerId) {
        Log.d(TAG, "Communication Session Terminated");
        try {
            mScanner.notifyScannerDisconnected(scannerId);
        } catch(JSONException err) {
            Log.e(TAG, "ERROR notifying disconnected event.");
        }
    }

    @Override
    public void dcssdkEventBarcode(byte[] barcodeData, int barcodeType, int fromScannerId) {
        Log.d(TAG, "Got Barcode");
        Log.d(TAG, "\nType: " + BarcodeTypes.getBarcodeTypeName(barcodeType) + ".\n From scanner: " + fromScannerId + ".\n Data: " + new String(barcodeData));
        try {
            mScanner.notifyBarcodeReceived(new String(barcodeData), BarcodeTypes.getBarcodeTypeName(barcodeType), fromScannerId);
        } catch(JSONException err) {
            Log.e(TAG, "ERROR notifying barcode.");
        }
    }

    @Override
    public void dcssdkEventImage(byte[] var1, int var2) {
        Log.d(TAG, "Got Image?");
    }

    @Override
    public void dcssdkEventVideo(byte[] var1, int var2) {
        Log.d(TAG, "Got Video?");
    }

    @Override
    public void dcssdkEventFirmwareUpdate(FirmwareUpdateEvent var1) {
        Log.d(TAG, "Firmware Update Event");
    }

    @Override
    public void dcssdkEventAuxScannerAppeared(DCSScannerInfo dcsScannerInfo, DCSScannerInfo dcsScannerInfo1) {
        Log.d(TAG, "Aux Scanner Appeared");
    }
}
