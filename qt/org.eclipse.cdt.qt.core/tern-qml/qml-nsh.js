function newTernServer(options) {
	return new tern.Server(options);
}

function resolveDirectory(obj) {
	return function (file, path) {
		return obj.resolveDirectory(file, path);
	};
}

function requestCallback(obj) {
	return function (err, data) {
		obj.callback(err, data);
	};
}