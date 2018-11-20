/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
"use strict";

var fs = require("fs"), path = require("path");
var ternQML = require("../qml.js");
var tern = require("tern");

var projectDir = path.resolve(__dirname, "..");
var resolve = function (pth) {
	return path.resolve(projectDir, pth);
};
var testCases = [];
var groupName;

function TestCase(group, code, run) {
	this.code = code;
	this.group = group;
	this.runTest = run || function (server, callback, code) {
		callback("fail", this.code, "runTest function was not provided.");
	};
}

exports.isolate = function (code) {
	for (var i = 0; i < testCases.length; i++) {
		var test = testCases[i];
		if (test.group === groupName && test.code !== code) {
			testCases.splice(i, 1);
			i--;
		}
	}
};

exports.groupStart = function (group) {
	groupName = group;
};

exports.groupEnd = function () {
	groupName = undefined;
};

exports.test = function (code, runTest) {
	testCases.push(new TestCase(groupName || "Default", code, runTest));
};

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
};

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
};

exports.runTests = function (config, callback) {
	for (var i = 0; i < testCases.length; ++i) {
		var test = testCases[i];
		if (test.group === config.group) {
			var server = createServer();
			test.runTest(server, callback, test.code);
		}
	}
};

function createServer(defs) {
	var plugins = {};
	plugins.qml = true;
	var server = new tern.Server({
		ecmaVersion: 5,
		plugins: plugins,
		defs: [ require("./ecma5-defs.js") ]
	});
	return server;
}

var assertCompletion = exports.assertCompletion = function (server, code, expected, pos, callback) {
	server.addFile("main.qml", code);
	server.request({
		files: [
			{
				name: "main.qml",
				text: code,
				type: "full"
			}
		],
		query: {
			type: "completions",
			file: "main.qml",
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
	}, function (err, resp) {
		if (err) {
			throw err;
		}
		var mis = misMatch(expected, resp);
		callback(mis);
	});
};

var assertDefinition = exports.assertDefinition = function (server, code, expected, pos, callback) {
	server.request({
		files: [
			{
				name: "main.qml",
				text: code,
				type: "full"
			}
		],
		query: {
			type: "definition",
			file: "main.qml",
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
	}, function (err, resp) {
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
	if (str.charAt(str.length - 1) == ")")
		return str.slice(0, str.length - 1) + "/" + pt + ")";
	return str + " (" + pt + ")";
}

var misMatch = exports.misMatch = function (exp, act) {
	var mis = null;
	if (!exp || !act || (typeof exp != "object") || (typeof act != "object")) {
		if (exp !== act) return ppJSON(exp) + " !== " + ppJSON(act);
	} else if (exp instanceof RegExp || act instanceof RegExp) {
		var left = ppJSON(exp), right = ppJSON(act);
		if (left !== right) return left + " !== " + right;
	} else if (exp.splice) {
		if (!act.slice) return ppJSON(exp) + " != " + ppJSON(act);
		if (act.length != exp.length) return "array length mismatch " + exp.length + " != " + act.length;
		for (var i = 0; i < act.length; ++i) {
			mis = misMatch(exp[i], act[i]);
			if (mis) return addPath(mis, i);
		}
	} else {
		for (var prop in exp) {
			mis = misMatch(exp[prop], act[prop]);
			if (mis) return addPath(mis, prop);
		}
	}
};