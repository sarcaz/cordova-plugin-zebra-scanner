// RFIDHandler

package land.cookie.cordova.plugin.zebrarfid.rfid;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.zebra.rfid.api3.ENUM_TRANSPORT;
import com.zebra.rfid.api3.ENUM_TRIGGER_MODE;
import com.zebra.rfid.api3.Events;
import com.zebra.rfid.api3.HANDHELD_TRIGGER_EVENT_TYPE;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.RFIDReader;
import com.zebra.rfid.api3.ReaderDevice;
import com.zebra.rfid.api3.Readers;
import com.zebra.rfid.api3.RfidEventsListener;
import com.zebra.rfid.api3.RfidReadEvents;
import com.zebra.rfid.api3.RfidStatusEvents;
import com.zebra.rfid.api3.START_TRIGGER_TYPE;
import com.zebra.rfid.api3.STATUS_EVENT_TYPE;
import com.zebra.rfid.api3.STOP_TRIGGER_TYPE;
import com.zebra.rfid.api3.TagData;
import com.zebra.rfid.api3.TriggerInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import land.cookie.cordova.plugin.zebrarfid.ZebraRFID;

public class RFIDHandler implements Readers.RFIDReaderEventHandler, RfidEventsListener {

   final static String TAG = ZebraRFID.TAG;

   public RFIDReader connectedReader = null;

   private Boolean inScan = false;
   private Boolean canScan = false;

   private ZebraRFID zebraRFID;
   private Context context;
   private Readers readers;

   public RFIDHandler(Context context, ZebraRFID zebraRFID){
      this.context = context;
      this.zebraRFID = zebraRFID;
   }

   private static String getInvalidUsageMessage(InvalidUsageException e){
      if(!e.getVendorMessage().isEmpty()){
         Log.e(ZebraRFID.TAG, "InvalidUsageException " + e.getVendorMessage());
         return e.getVendorMessage();
      }
      if(!e.getInfo().isEmpty()){
         Log.e(ZebraRFID.TAG, "InvalidUsageException " + e.getInfo());
         return e.getInfo();
      }
      return e.toString();
   }

   // region RFIDReaderEventHandler

   @Override
   public void RFIDReaderAppeared(ReaderDevice readerDevice) {
      Log.d(TAG, "RFIDReaderAppeared");
   }

   @Override
   public void RFIDReaderDisappeared(ReaderDevice readerDevice) {
      Log.d(TAG, "RFIDReaderDisappeared");
   }

   // endregion RFIDReaderEventHandler

   // region RfidEventsListener

   @Override
   public void eventReadNotify(RfidReadEvents rfidReadEvents) {
      TagData[] myTags = this.connectedReader.Actions.getReadTags(100);
      if (myTags != null) {
         for (int index = 0; index < myTags.length; index++) {
            TagData tag = myTags[index];
            Log.d(TAG, "Tag ID " + tag.getTagID());
            this.zebraRFID.notifyReadTag(tag);
         }
      }
   }

   @Override
   public void eventStatusNotify(RfidStatusEvents rfidStatusEvents) {
      Object value = "";
      STATUS_EVENT_TYPE eventType = null;
      String event;

      try
      {
         eventType = rfidStatusEvents.StatusEventData.getStatusEventType();
         Log.d(TAG + "_STATUS", "Ordinal: " + eventType.ordinal);
         event = rfidStatusEvents.StatusEventData.getStatusEventType().toString();
         Log.d(TAG + "_STATUS", "Status Notification: " + event);

         // POWER_EVENT
         if(eventType == STATUS_EVENT_TYPE.POWER_EVENT){
            Events.PowerData powerData = rfidStatusEvents.StatusEventData.PowerData;
            event = "POWER_EVENT";
            JSONObject valueJSON = new JSONObject();
            try {
               valueJSON.put("power", powerData.getPower());
               valueJSON.put("cause", powerData.getCause());
               valueJSON.put("current", powerData.getCurrent());
               valueJSON.put("voltage", powerData.getVoltage());
               value = valueJSON;
               Log.d(TAG + "_STATUS", "Power: " + powerData.getCurrent());

            } catch (JSONException e) {
               Log.e(TAG, Log.getStackTraceString(e));
            }

         }

         // BATTERY_EVENT
         else if(eventType == STATUS_EVENT_TYPE.BATTERY_EVENT){
            Events.BatteryData batteryData = rfidStatusEvents.StatusEventData.BatteryData;
            event = "BATTERY_EVENT";
            JSONObject valueJSON = new JSONObject();
            try {
               valueJSON.put("cause", batteryData.getCause());
               valueJSON.put("level", batteryData.getLevel());
               valueJSON.put("charging", batteryData.getCharging());
               value = valueJSON;
               Log.d(TAG + "_STATUS", "Battery: " + batteryData.getLevel());
            } catch (JSONException e) {
               Log.e(TAG, Log.getStackTraceString(e));
            }
         }

         // HANDHELD_TRIGGER_EVENT
         else if (eventType == STATUS_EVENT_TYPE.HANDHELD_TRIGGER_EVENT){

            Events.StatusEventData statusEventData = rfidStatusEvents.StatusEventData;
            HANDHELD_TRIGGER_EVENT_TYPE handheldEvent = statusEventData.HandheldTriggerEventData.getHandheldEvent();

            if (handheldEvent == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED) {
               event = "HANDHELD_TRIGGER_PRESSED";
               value = true;
               if(this.canScan){
                  try{
                     this.startScan();
                  }catch (Exception ex){
                     Log.e(TAG, "Impossibile avviare la scansione");
                     Log.e(TAG, Log.getStackTraceString(ex));
                  }
               }else{
                  Log.e(TAG, "Scansione non avviata");
               }
            }
            if (handheldEvent == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_RELEASED) {
               event = "HANDHELD_TRIGGER_RELEASED";
               value = true;
               this.stopScan();
            }
         }else{
            Log.d(TAG + "_STATUS", "Evento non riconosciuto");
            Log.d(TAG + "_STATUS", event);
         }

         this.zebraRFID.notifyEvent(event, value);

      }
      catch (Exception e){
         e.printStackTrace();
      }
   }

   // endregion RfidEventsListener

   // region Connection

   class ConnectionTask extends AsyncTask<Void, Void, SDKResult> {

      private RFIDReader reader;

      ConnectionTask(RFIDReader reader){
         this.reader = reader;
      }

      @Override
      protected SDKResult doInBackground(Void... voids) {
         Log.d(ZebraRFID.TAG, "doInBackground - ConnectionTask");
         try {
            this.reader.connect();
            ConfigureReader(this.reader);
            return new SDKResult(false, "connected");

         } catch (InvalidUsageException e) {
            e.printStackTrace();
            String errMessage = getInvalidUsageMessage(e);
            if(errMessage.contains("Try Reconnect()")){
               return this.reconnect(this.reader);
            }else{
               Log.e(ZebraRFID.TAG, "InvalidUsageException " + errMessage);
               e.printStackTrace();
               return new SDKResult(true, errMessage);
            }

         } catch (OperationFailureException e) {
            Log.e(ZebraRFID.TAG, "OperationFailureException " + e.getStatusDescription());
            e.printStackTrace();
            return new SDKResult(true, e.getStatusDescription());
         }
      }

      @Override
      protected void onPostExecute(SDKResult result) {
         Log.d(ZebraRFID.TAG, "onPostExecute - ConnectionTask");
         super.onPostExecute(result);
         if(result.error){
            Log.e(ZebraRFID.TAG, "Cannot Connect!");
         }else{
            Log.d(ZebraRFID.TAG, "Connected!");
         }
      }

      private SDKResult reconnect(RFIDReader reader){
         try {
            this.reader.reconnect();
            ConfigureReader(this.reader);
            return new SDKResult(false, "ok");
         } catch (InvalidUsageException e) {
            e.printStackTrace();
            return new SDKResult(true, getInvalidUsageMessage(e));

         } catch (OperationFailureException e) {
            Log.e(ZebraRFID.TAG, "OperationFailureException " + e.getStatusDescription());
            e.printStackTrace();
            return new SDKResult(true, e.getStatusDescription());
         }
      }

   }

   public SDKResult connect(RFIDReader reader) {
      Log.d(TAG, "connect");
      try {
         ConnectionTask task = new ConnectionTask(reader);
         SDKResult res = task.execute().get();
         this.connectedReader = reader;
         return res;

      } catch (ExecutionException | InterruptedException e) {
         e.printStackTrace();
         Log.e(ZebraRFID.TAG, e.getMessage());
         return new SDKResult(true, e.getMessage());
      }
   }

   public SDKResult reconnect(RFIDReader reader) {
      try {
         reader.reconnect();
         return new SDKResult(false, "reconnected");

      }catch (InvalidUsageException e) {
         e.printStackTrace();
         return new SDKResult(true, getInvalidUsageMessage(e));

      }catch (OperationFailureException e) {
         e.printStackTrace();
         ConfigureReader(reader);
         return new SDKResult(false, "riconnessione");
      }
   }

   public SDKResult disconnect(RFIDReader reader) {
      try {
         reader.disconnect();
         return new SDKResult(false, "disconnect");
      } catch (InvalidUsageException | OperationFailureException e) {
         e.printStackTrace();
         return new SDKResult(true, e.getMessage());
      }
   }

   // endregion Connection

   public synchronized ArrayList<ReaderDevice> scanAvailableRFIDReaderList(){
      InvalidUsageException invalidUsageException = null;
      try {
         if(this.readers == null){
            this.readers = new Readers(context, ENUM_TRANSPORT.BLUETOOTH);
         }
         return readers.GetAvailableRFIDReaderList();
      } catch (InvalidUsageException e) {
         e.printStackTrace();
         invalidUsageException = e;
         return new ArrayList<ReaderDevice>();
      }
   }

   private void ConfigureReader(RFIDReader reader) {
      if (reader.isConnected()) {
         TriggerInfo triggerInfo = new TriggerInfo();
         triggerInfo.StartTrigger.setTriggerType(START_TRIGGER_TYPE.START_TRIGGER_TYPE_IMMEDIATE);
         triggerInfo.StopTrigger.setTriggerType(STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_IMMEDIATE);

         try {
            // receive events from reader
            reader.Events.addEventsListener(this);

            // Subscribe required status notification
            reader.Events.setInventoryStartEvent(true);
            reader.Events.setInventoryStopEvent(true);

            // enables tag read notification. if this is set to false, no tag read notification is send
            reader.Events.setReaderDisconnectEvent(true);
            reader.Events.setBatteryEvent(true);
            reader.Events.setBatchModeEvent(true);
            reader.Events.setPowerEvent(true);

            // HH event
            reader.Events.setHandheldEvent(true);
            // tag event with tag data
            reader.Events.setTagReadEvent(true);
            // application will collect tag using getReadTags API
            reader.Events.setAttachTagDataWithReadEvent(false);
            // set trigger mode as rfid so scanner beam will not come
            reader.Config.setTriggerMode(ENUM_TRIGGER_MODE.RFID_MODE, true);
            // set start and stop triggers
            reader.Config.setStartTrigger(triggerInfo.StartTrigger);
            reader.Config.setStopTrigger(triggerInfo.StopTrigger);

         } catch (InvalidUsageException | OperationFailureException e) {
            e.printStackTrace();

         }
      }
   }

   private void startScan() {
      try {
         this.connectedReader.Actions.Inventory.perform();
         this.inScan = true;
      } catch (InvalidUsageException | OperationFailureException e) {
         e.printStackTrace();
      }
   }

   private void stopScan()  {
      try {
         this.connectedReader.Actions.Inventory.stop();
         this.inScan = false;
      } catch (InvalidUsageException | OperationFailureException e) {
         e.printStackTrace();
      }
   }

   public void setCanScan(Boolean canScan) {
      this.canScan = canScan;
      if(!canScan) {
         this.stopScan();
      }
   }

   public Boolean getDeviceStatus(){
      try {
         this.connectedReader.Config.getDeviceStatus(true, false, false);
         this.connectedReader.Config.getDeviceStatus(false, true, false);
         this.connectedReader.Config.getDeviceStatus(false, false, true);
         return true;
      } catch (InvalidUsageException | OperationFailureException e) {
         Log.e(TAG, Log.getStackTraceString(e));
         e.printStackTrace();
         return false;
      }
   }

}
