# Zebra Barcode Scanner Plugin [Android]
This plugin allows you to communicate with Zebra scanners over bluetooth (possibly USB as well) on Android.

## Requirements
* Cordova 5.0.0 or higher
* Android 4.1 or higher - target Android API 26

## Limitations
* iOS is not supported. It looks like I won't get more time allocated to develop iOS part.
  However this plugin can be used instead: https://github.com/shanghai-tang/cordova-plugin-zebra-scanner
* Only one scanner can be connected. The Zebra SDK does not support multiple connections.

## Warning
I had to use SDK provided by Zebra and there are multiple bugs in it.
* Calling any method will start scanning for bluetooth devices. There was no way around it.
  The SDK object starts scanning by itself when it is created.
* There is no way to stop scanning for bluetooth devices. Calling stopScan() will stop the scan however only for few
  seconds, after that the scan will restart itself and it will keep repeating that unless a scanner is connected or
  until the plugin is destroyed.

## Install
`cordova plugin add https://github.com/PreciousBiscuit/cordova-plugin-zebra-scanner.git`

## Overview
These bluetooth barcode scanners are supported:
```
• CS4070
• LI4278
• DS6878
• RFD8500 (Tested)
• DS3678
• LI3678
```
These USB barcode scanners should be supported as well, however I cannot test it:
```
• PL3307
• DS457
• DS4308
• LS2208
• DS6878 and Presentation Cradle
• MP6210 (CSS + Scale) + EAS (Sensormatic)
```

## Methods
* [startScan](#startScan)
* [stopScan](#stopScan)
* [getAvailableDevices](#getAvailableDevices)
* [getActiveDevices](#getActiveDevices)
* [getPairingBarcode](#getPairingBarcode)
* [connect](#connect)
* [disconnect](#disconnect)
* [subscribe](#subscribe)
* [unsubscribe](#unsubscribe)

### startScan
Start scanning for bluetooth (possibly USB connected) devices. Scanning is expensive so call stopScan() as soon as
possible [see [Warning](#Warning) above]. The method returns all discovered and paired devices.

##### Success
```
{
  "status": <string>
  ["device"]: {
    "id": <number>
    ["name"]: <string>
    ["model"]: <string>
    ["serialNumber"]: <string>
  }
}
```
* status - One of these: ["scanStart", "scanStop", "deviceFound", "deviceLost"]
* device - Available for statuses "deviceFound" and "deviceLost"
  * name - Available for status "deviceFound"
  * model - Available for status "deviceFound"
  * serialNumber - Available for status "deviceFound"

##### Errors
* "Scanning is already in a progress." -- startScan() was already called but stopScan() was not.


### stopScan
Stop scanning for bluetooth devices [see [Warning](#Warning) above].

##### Success
* "ok"

##### Errors
* "Scanning was not in a progress." -- startScan() was not called.


### getAvailableDevices

##### Success

##### Errors


### getActiveDevices

##### Success

##### Errors


### getPairingBarcode

##### Success

##### Errors


### connect

##### Params

##### Success

##### Errors


### disconnect

##### Params

##### Success

##### Errors


### subscribe

##### Success

##### Errors


### unsubscribe

##### Success

##### Errors

