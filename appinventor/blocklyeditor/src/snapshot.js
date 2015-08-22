/**
 * @license
 * @fileoverview Visual blocks editor for App Inventor
 * Methods to snapshot project state to remote server
 *
 * @author msherman@cs.uml.edu (Mark Sherman)
 */

goog.provide('Blockly.Snapshot');
goog.require('goog.net.XhrIo');

//var dataUrl = 'http://msp.cs.uml.edu/api';
var dataUrl = 'http://localhost:8000';

var xhr = new goog.net.XhrIo();
var idno = 0;

// Listen for completed RPC calls
// "Complete" could be SUCCESS or ERROR
goog.events.listen(xhr, goog.net.EventType.COMPLETE, function() {
	// Put in here anything to run regardless of success/error
});

// Listen for Successfull RPC calls
// Success event is fired AFTER complete event.
goog.events.listen(xhr, goog.net.EventType.SUCCESS, function() {
  var obj = this.getResponseJson();

	console.log('%%%% getData result: ' + obj.result);
	console.log(obj);
});

// Listen for Error-result RPC calls
// Error event is fired AFTER complete event.
goog.events.listen(xhr, goog.net.EventType.ERROR, function() {
	var obj = this.getResponseJson();
	console.log('%%%% getData resulted in error.');
});

/**
 * Prepares and Sends some snapshot data to a server
 *
 * @param {string} dataUrl The url to request.
 * @param {string} snapshot data to send.
 */
Blockly.Snapshot.send = function() {

	var metadata = {
		userName: top.BlocklyPanel_getUserEmail(),
		projectName: top.BlocklyPanel_getProjectName(),
		projectId: top.BlocklyPanel_getProjectId(),
		screenName: top.BlocklyPanel_getScreenName(),
		sessionId: top.BlocklyPanel_getSessionId(),
	};

	var data = [
		JSON.stringify(metadata),
		Blockly.SaveFile.get()
	];

	var content = goog.json.serialize(
	{	"jsonrpc": "2.0",
		"method": "file.log",
		"params": data,
		"id": ++idno }
	);

	console.log("\n\n------ Snapshot! ------ " + Date() + "\n");
	console.log("Data:\n");
	console.log(content);

	xhr.send(dataUrl, "POST", content);
};
