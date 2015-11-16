"use strict";

var fs = require("fs"), path = require("path");

// Acorn and Acorn-QML
var acorn = require("acorn"),
	acorn_loose = require("acorn/dist/acorn_loose"),
	walk = require("acorn/dist/walk"),
	acornQML = require("acorn-qml"),
	acornQML_loose = require("acorn-qml/loose"),
	acornQML_walk = require("acorn-qml/walk");

// Tern and Tern-QML
var tern = require("tern"),
	ternQML = require("../qml.js");

var projectDir = path.resolve(__dirname, "..");
var resolve = function(pth) {
	return path.resolve(projectDir, pth);
};
var testCases = [];
var groupName;

function TestCase(group, code, run) {
	this.group = group;
	this.runTest = run || function (server, callback) {
		callback("fail", code, "runTest function was not provided.");
	};
}

exports.groupStart = function(group) {
	groupName = group;
}

exports.groupEnd = function() {
	groupName = undefined;
}

exports.test = function (code, runTest) {
	testCases.push(new TestCase(groupName || "Default", code, runTest));
}

exports.testCompletion = function (code, expected, beforeTest) {
	testCases.push(new TestCase(groupName || "Default", code, function (server, callback) {
		var trimmedCode = code;
		// Extract the cursor position
		var pos = code.indexOf("|");
		var split = code.split("|");
		trimmedCode = split[0] + split[1];

		if (beforeTest) {
			beforeTest(server);
		}
		assertCompletion(server, trimmedCode, expected, pos, function (mis) {
			if (mis) {
				callback("fail", code, mis);
			} else {
				callback("ok", code);
			}
		});
	}, beforeTest));
}

exports.testDefinition = function (code, expected, beforeTest) {
	testCases.push(new TestCase(groupName || "Default", code, function (server, callback) {
		var trimmedCode = code;
		// Extract the cursor position
		var pos = code.indexOf("|");
		var split = code.split("|");
		trimmedCode = split[0] + split[1];

		if (beforeTest) {
			beforeTest(server);
		}
		assertDefinition(server, trimmedCode, expected, pos, function (mis) {
			if (mis) {
				callback("fail", code, mis);
			} else {
				callback("ok", code);
			}
		});
	}, beforeTest));
}

exports.runTests = function(config, callback) {
	for (var i = 0; i < testCases.length; ++i) {
		var test = testCases[i];
		if (test.group === config.group) {
			var server = createServer();
			test.runTest(server, callback);
		}
	}
};

function createServer(defs) {
	var plugins = {};
	plugins["qml"] = {};
	var server = new tern.Server({
		ecmaVersion: 5,
		plugins : plugins,
		defs : [ require("./ecma5-defs.js") ]
	});
	return server;
}

function assertCompletion (server, code, expected, pos, callback) {
	server.addFile("test1.qml", code);
	server.request({
		query : {
			type: "completions",
			file: "test1.qml",
			end: pos,
			types: true,
			docs: false,
			urls: false,
			origins: true,
			caseInsensitive: true,
			lineCharPositions: true,
			expandWordForward: false,
			guess: false
		}
	}, function(err, resp) {
		if (err) {
			throw err;
		}
		var mis = misMatch(expected, resp);
		callback(mis);
	});
};

function assertDefinition (server, code, expected, pos, callback) {
	server.addFile("test1.qml", code);
	server.request({
		query : {
			type: "definition",
			file: "test1.qml",
			end: pos,
			types: true,
			docs: false,
			urls: false,
			origins: true,
			caseInsensitive: true,
			lineCharPositions: true,
			expandWordForward: false,
			guess: false
		}
	}, function(err, resp) {
		if (err) {
			throw err;
		}
		var mis = misMatch(expected, resp);
		callback(mis);
	});
};

function ppJSON(v) {
	return v instanceof RegExp ? v.toString() : JSON.stringify(v, null, 2);
}

function addPath(str, pt) {
	if (str.charAt(str.length-1) == ")")
		return str.slice(0, str.length-1) + "/" + pt + ")";
	return str + " (" + pt + ")";
}

var misMatch = exports.misMatch = function(exp, act) {
	if (!exp || !act || (typeof exp != "object") || (typeof act != "object")) {
		if (exp !== act) return ppJSON(exp) + " !== " + ppJSON(act);
	} else if (exp instanceof RegExp || act instanceof RegExp) {
		var left = ppJSON(exp), right = ppJSON(act);
		if (left !== right) return left + " !== " + right;
	} else if (exp.splice) {
		if (!act.slice) return ppJSON(exp) + " != " + ppJSON(act);
		if (act.length != exp.length) return "array length mismatch " + exp.length + " != " + act.length;
		for (var i = 0; i < act.length; ++i) {
			var mis = misMatch(exp[i], act[i]);
			if (mis) return addPath(mis, i);
		}
	} else {
		for (var prop in exp) {
			var mis = misMatch(exp[prop], act[prop]);
			if (mis) return addPath(mis, prop);
		}
	}
};