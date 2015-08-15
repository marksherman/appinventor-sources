/**
 * @license
 * @fileoverview Visual blocks editor for App Inventor
 * Methods to snapshot project state to remote server
 *
 * @author msherman@cs.uml.edu (Mark Sherman)
 */

goog.provide('Blockly.Snapshot');
goog.require('goog.net.XhrIo');

var idno = 0;

/**
 * Prepares and Sends some snapshot data to a server
 * uses XhrIo's static send() method.
 *
 * @param {string} dataUrl The url to request.
 * @param {string} snapshot data to send.
 */
Blockly.Snapshot.send = function() {
	//var dataUrl = 'http://msp.cs.uml.edu/api';
	var dataUrl = 'http://localhost:8000';

	//Blockly.mainWorkspace.getTopBlocks(false);

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

	goog.net.XhrIo.send(dataUrl, function(e) {
		var xhr = e.target;
		var obj = xhr.getResponseJson();

		console.log('%%%% getData result: ' + obj.result);
		console.log(obj);
	},
	"POST", content);
};
