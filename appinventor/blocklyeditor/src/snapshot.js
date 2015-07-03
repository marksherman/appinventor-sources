/**
 * @license
 * @fileoverview Visual blocks editor for App Inventor
 * Methods to snapshot project state to remote server
 *
 * @author msherman@cs.uml.edu (Mark Sherman)
 */

goog.provide('Blockly.Snapshot');

goog.require('goog.net.XhrIo');

/**
 * Retrieve Json data using XhrIo's static send() method.
 *
 * @param {string} dataUrl The url to request.
 * @param {string} snapshot data to send.
 */
Blockly.Snapshot.send = function(dataUrl, data) {
	var content = goog.json.serialize(
		{	"jsonrpc": "2.0",
			"method": "file.log",
			"params": [data],
			"id": 97 }
		);

  console.log('%%%% getData request to ['+ dataUrl + ']');

  goog.net.XhrIo.send(dataUrl, function(e) {
      var xhr = e.target;
      var obj = xhr.getResponseJson();

      console.log('%%%% getData result: ' + obj.result);
      console.log(obj);
  },
  "POST", content);
}

/**
 * Call the methods used to send snapshot to server
 *
 * @param {string} the snapshot data to send.
 */
Blockly.Snapshot.snapshot = function(data) {
	console.log("\n\n------ Snapshot! ------\n");
	console.log("Data provided:\n");
	console.log(data);
	Blockly.Snapshot.send('http://msp.cs.uml.edu/api', data);
	//Blockly.Snapshot.send('http://localhost:8000', data);
}
