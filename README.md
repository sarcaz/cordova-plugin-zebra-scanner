# Zebra Barcode Scanner Plugin [Android]
This plugin allows you to communicate with Zebra scanners over bluetooth (possibly USB as well) on Android.

## Requirements
* Cordova 5.0.0 or higher
* Android 4.1 or higher - target Android API 26

## Limitations
* iOS is not supported. It looks like I won't get more time allocated to develop iOS part.
  However you can try this plugin: https://github.com/shanghai-tang/cordova-plugin-zebra-scanner

## Warning
I had to use SDK provided by Zebra and there are multiple bugs in it.
* Calling any method will start scanning for bluetooth devices. There was no way around it.
  The SDK object starts scanning by itself when it is created.
* There is no way to stop scanning for bluetooth devices. Calling stopScan() will stop the scan however only for few
  seconds, after that the scan will restart itself and it will keep repeating that unless a scanner is connected or
  until plugin is destroyed.

## Install
### Cordova
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

## Implementation
TODO
