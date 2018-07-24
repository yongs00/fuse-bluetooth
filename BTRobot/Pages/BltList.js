var Observable = require('FuseJS/Observable');
var Bluetooth = require("Fuse/Bluetooth");

var bles= Observable();	//페어링 된 장비 리스트 
var scanDvices = Observable(); // 스캔 된 장비 리스트

//화면 활성화 시 이벤트 
function onActivated(args) {
	getListPairedDevice();
};

//기존에 페어링된 장비 리스트
function getListPairedDevice(){
	//console.log("get ListPairedDevice");
	var json = Bluetooth.getListPairedDevice(); //페어링된 장비 리스트 가져옴
	
	//console.log(json);
	json = JSON.parse(json);

	bles.clear();
	
	for (var j = 0; j < json.length; j++ ) {
		var data = {
			NAME:'',
			ADDRESS: ''
		};

		data.ADDRESS = json[j].ADDRESS;
		data.NAME = json[j].NAME;
		bles.add(data);
		//console.log(JSON.stringify(data));
	}
}

//스캔된 디바이스 클릭 시 페어링 요청
function scanDeviceClicked(item) {
	console.log(JSON.stringify(item));
	Bluetooth.pairingDevice(item.data.ADDRESS); //디바이스 페어링
};

//페어링 된 디바이스 소켓통신을 위해 연결
function deviceClicked(item) {
	console.log(JSON.stringify(item));
	var result = Bluetooth.connect(item.data.ADDRESS); //디바이스 연결
	isBusy.activate();
	//if(result)
	//	router.push("btlControl");
};

//블루투스 디바이스 스캔 
function scanStart(){
	console.log("ScanStart");
	scanDvices.clear();
	Bluetooth.scanStart();
}

//디바이스 스캔 이벤트
Bluetooth.scanDiscovered = function(message) {
	console.log("scanDiscovered js " + message);
	
	var json = JSON.parse(message);

	var data = {
		NAME:'',
		ADDRESS: ''
	};

	data.ADDRESS = json.ADDRESS;
	data.NAME = json.NAME;
	scanDvices.add(data);
	console.log(JSON.stringify(data));
	
};

//디바이스에 연결되었을 때 이벤트
Bluetooth.deviceConnected = function(message) {
	console.log("deviceConnected js " + message);
	isBusy.deactivate();
	
	if(message == "connected")
		router.push("btlControl");
};

module.exports = {
	scanStart
	, scanDeviceClicked
	, bles
	, scanDvices
	, getListPairedDevice
	, deviceClicked
	, onActivated
};
