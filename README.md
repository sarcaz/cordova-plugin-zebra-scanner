# Zebra Barcode Scanner Plugin

## Beware: \[2018-08-10\] Android part is finished - documentation not yet.
The plugin was changed to be usable over bluetooth. Android part is finished.
However the documentation is not updated yet. Additionally I wasn't able to verify if USB scanners still work,
because I only have RFD8500 rfid/barcode scanner which works over bluetooth
(Connecting to rfid part of RFD8500 is done over bluetooth-serial plugin).

## Overview
This plugin should support these Zebra bluetooth barcode scanners:
```
• CS4070 (in Bluetooth SSI Profile mode)
• LI4278 (in SSI Host Server mode or Cradle Host mode by scanning pairing bar code)
• DS6878 (in SSI Host Server mode or Cradle Host mode by scanning pairing bar code)
• RFD8500 (Tested)
• DS3678 (In SSI BT Classic mode)
• LI3678 (In SSI BT Classic mode)
```
These USB scanner should be supported as well, however I cannot test it:
```
• PL3307
• DS457
• DS4308
• LS2208
• DS6878 and Presentation Cradle
• MP6210 (CSS + Scale) + EAS (Sensormatic)
```

## Setup
### Cordova CLI
`cordova plugin add https://github.com/PreciousBiscuit/cordova-plugin-zebra-scanner.git`.

## Implementation
TODO
