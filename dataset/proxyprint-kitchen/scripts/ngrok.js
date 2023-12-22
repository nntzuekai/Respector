var ngrok = require('ngrok');

/*var sys = require('util');
var exec = require('child_process').exec;

child = exec('sudo killall -9 ngrok', function (error, stdout, stderr) {});*/

var externalURL="";
var port = 8080;
ngrok.connect({
	proto: 'http',
	addr: port
}, function (err, url) {
	if(err) {
		console.log(err);
		return;
	}
	var tmp = url.split(":");
	externalURL="https:"+tmp[1]+"/";
	var fs = require('fs');
	fs.writeFile("/tmp/externalURL", externalURL, function(err) {
		if(err) { return console.log(err); }
	});

});
ngrok.connect(port);

