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
* [zebraScanner.startScan](#startscan)
* [zebraScanner.stopScan](#stopscan)
* [zebraScanner.getAvailableDevices](#getavailabledevices)
* [zebraScanner.getActiveDevices](#getactivedevices)
* [zebraScanner.getPairingBarcode](#getpairingbarcode)
* [zebraScanner.connect](#connect)
* [zebraScanner.disconnect](#disconnect)
* [zebraScanner.subscribe](#subscribe)
* [zebraScanner.unsubscribe](#unsubscribe)

### startScan
Starts scanning for bluetooth (possibly USB connected) devices. Scanning is expensive so call stopScan() as soon as
possible [see [Warning](#warning) above]. The method returns all discovered and paired devices.

```javascript
zebraScanner.startScan(successCallback, errorCallback)
```

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
* "Scanning is already in a progress." -- startScan() was already called but stopScan() was not


### stopScan
Stops scanning for bluetooth devices [see [Warning](#warning) above].

```javascript
zebraScanner.stopScan(successCallback, errorCallback)
```

##### Success
"ok"

##### Errors
* "Scanning was not in a progress." -- startScan() was not called


### getAvailableDevices
Returns all devices found by zebra SDK including paired devices.

```javascript
zebraScanner.getAvailableDevices(successCallback, errorCallback)
```

##### Success
An array of devices
```
[
  "device": {
    "id": <number>
    "name": <string>
    "model": <string>
    "serialNumber": <string>
  },
  ...
]
```

##### Errors
None


### getActiveDevices
Returns all connected devices. Zebra SDK supports only one connection so this method returns maximum one device.

```javascript
zebraScanner.getActiveDevices(successCallback, errorCallback)
```

##### Success
An array of devices
```
[
  "device": {
    "id": <number>
    "name": <string>
    "model": <string>
    "serialNumber": <string>
  }
]
```

##### Errors
None


### getPairingBarcode
Returns barcode for pairing devices. The barcode is JPG encoded in base64.

```javascript
zebraScanner.getPairingBarcode(successCallback, errorCallback)
```

##### Success
A string with base64 encoded JPG

##### Errors
None


### connect

```javascript
zebraScanner.connect(successCallback, errorCallback, params)
```

##### Params
```
{
  "deviceId": <number>
}
```
* deviceId - ID of a device retrieved from methods startScan() or getAvailableDevices()

##### Success

##### Errors


### disconnect

```javascript
zebraScanner.disconnect(successCallback, errorCallback, params)
```

##### Params
```
{
  "deviceId": <number>
}
```
* deviceId - ID of a device retrieved from methods startScan() or getAvailableDevices()

##### Success

##### Errors


### subscribe

```javascript
zebraScanner.subscribe(successCallback, errorCallback)
```

##### Success

##### Errors


### unsubscribe

```javascript
zebraScanner.unsubscribe(successCallback, errorCallback)
```

##### Success

##### Errors

