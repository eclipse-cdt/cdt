function newTernServer(options) {
	return new tern.Server(options);
}

function requestCallback(obj) {
	return function(err, data) {
		obj.callback(err, data);
	}
}
