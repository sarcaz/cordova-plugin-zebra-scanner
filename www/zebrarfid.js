const serviceName = 'ZebraRFID'
const zebraRFID = {
  getAvailableRFIDReaders(successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, serviceName, arguments.callee.name, []);
  },
  scanAvailableRFIDReaders(successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, serviceName, arguments.callee.name, []);
  },
  connect(successCallback, errorCallback, params) {
    cordova.exec(successCallback, errorCallback, serviceName, arguments.callee.name, [params]);
  },
  getConnectedDevice(successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, serviceName, arguments.callee.name, []);
  },
  getDeviceStatus(successCallback, errorCallback){
    cordova.exec(successCallback, errorCallback, serviceName, arguments.callee.name, []);
  },
  disconnect(successCallback, errorCallback){
    cordova.exec(successCallback, errorCallback, serviceName, arguments.callee.name, []);
  },
  startScan(successCallback, errorCallback){
    cordova.exec(successCallback, errorCallback, serviceName, arguments.callee.name, []);
  },
  stopScan(successCallback, errorCallback){
    cordova.exec(successCallback, errorCallback, serviceName, arguments.callee.name, []);
  }
}

module.exports = zebraRFID;
