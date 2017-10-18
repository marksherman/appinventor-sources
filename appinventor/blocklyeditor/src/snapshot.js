/**
 * @license
 * @fileoverview Visual blocks editor for App Inventor
 * Methods to snapshot project state to remote server
 *
 * @author msherman@cs.uml.edu (Mark Sherman)
 */

goog.provide('Blockly.Snapshot');
goog.require('goog.net.XhrIo');
goog.require('goog.net.XhrIoPool');

var dataUrl = 'http://snapshot.appinventor.mit.edu/v1.0';
//var dataUrl = 'http://localhost:8000/v1.0';

var ss_xhr = new goog.net.XhrIo();
var ss_xhrPool = new goog.net.XhrIoPool(); //defaults to max instances = 10
var idno = 0;

// Listen for completed RPC calls
// "Complete" could be SUCCESS or ERROR
// "Complete" is called before success or error
var ss_xhrComplete = function()
{
	// Put in here anything to run regardless of success/error
  console.log('%%%% getData COMPLETE');

};

// Listen for Successfull RPC calls
// Success event is fired AFTER complete event.
var ss_xhrSuccess = function() {
  var obj = this.getResponseJson();
  ss_xhrPool.releaseObject(this);
	console.log('%%%% getData Success - result: ' + obj.result);
	//console.log(obj);
};

// Listen for Error-result RPC calls
// Error event is fired AFTER complete event.
var ss_xhrError = function() {
  ss_xhrPool.releaseObject(this);
	console.log('%%%% getData resulted in error.');
};

/**
 * Prepares and Sends some snapshot data to a server
 *
 * @param {string} dataUrl The url to request.
 * @param {string} snapshot data to send.
 */
Blockly.Snapshot.send = function(eventType) {

	var projectData = {
		userName: top.BlocklyPanel_getUserEmail(),
		projectName: top.BlocklyPanel_getProjectName(),
		projectId: top.BlocklyPanel_getProjectId(),
		screenName: top.BlocklyPanel_getScreenName(),
		sessionId: top.BlocklyPanel_getSessionId(),
    	yaversion: top.YA_VERSION,
    	languageVersion: top.BLOCKS_VERSION,
    	eventType: eventType,
    	blocks: Blockly.SaveFile.get(),
    	form: top.ReplState.phoneState.formJson,
		sendDate: new Date()
  };

	var data = [
		JSON.stringify(projectData)
	];

	var content = goog.json.serialize(
	{	"jsonrpc": "2.0",
		"method": "file.log",
		"params": data,
		"id": ++idno }    // dirty, i know -Mark
	);

	console.log("\n\n------ Snapshot! (" + eventType + ")------ " + projectData.sendDate + "\n");
	//console.log("Data:\n");
	//console.log(content);

	ss_xhrPool.getObject(
    function(xhrObject)
    {
      goog.events.listen(xhrObject, goog.net.EventType.SUCCESS, ss_xhrSuccess);
      goog.events.listen(xhrObject, goog.net.EventType.COMPLETE, ss_xhrComplete);
      goog.events.listen(xhrObject, goog.net.EventType.ERROR, ss_xhrError);
      xhrObject.send(dataUrl, "POST", content);
    });
};
