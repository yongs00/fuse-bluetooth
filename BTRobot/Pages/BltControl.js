var Observable = require('FuseJS/Observable');
var Bluetooth = require("Fuse/Bluetooth");

//버튼 클릭시 이벤트 처리 
function btnClicked(args) {
	console.log(JSON.stringify(args));
	console.log(args.sender);
	var result;

	//블루투스 통신 Write 
	var result = Bluetooth.write(args.sender);
};

module.exports = {
	btnClicked
};
