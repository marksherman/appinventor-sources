goog.require('goog.net.XhrIo');


/**
 * Retrieve Json data using XhrIo's static send() method.
 *
 * @param {string} dataUrl The url to request.
 */
function getData(dataUrl) {
	var content = goog.json.serialize(
		{	"jsonrpc": "2.0",
			"method": "math.add",
			"params": [2, 4],
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
