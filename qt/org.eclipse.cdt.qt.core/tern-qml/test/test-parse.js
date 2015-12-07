/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
"use strict";

var driver = require("./driver.js");
var test = driver.test;
var groupStart = driver.groupStart;
var groupEnd = driver.groupEnd;

var group = exports.group = "Parse Query";
groupStart(group);

test("{Parse existing file}", function (server, callback, name) {
	server.addFile("main.qml", "import QtQuick 2.0\nModule {\n\tComponent {\n\t}\n}");
	server.request({
		query: {
			type: "parseFile",
			file: "main.qml"
		}
	}, function (err, resp) {
		if (err) {
			throw err;
		}
		if (!resp.ast && resp.ast.type === "QMLProgram") {
			return callback("fail", name, "AST could not be found in response");
		}
		return callback("ok", name);
	});
});

test("{Parse given file}", function (server, callback, name) {
	server.request({
		files: [
			{
				name: "main.qml",
				text: "import QtQuick 2.0\nModule {\n\tComponent {\n\t}\n}",
				type: "full"
			}
		],
		query: {
			type: "parseFile",
			file: "main.qml"
		}
	}, function (err, resp) {
		if (err) {
			throw err;
		}
		if (!resp.ast && resp.ast.type === "QMLProgram") {
			return callback("fail", name, "AST could not be found in response");
		}
		return callback("ok", name);
	});
});

test("{Parse text}", function (server, callback, name) {
	server.request({
		query: {
			type: "parseString",
			text: "import QtQuick 2.0\nModule {\n\tComponent {\n\t}\n}"
		}
	}, function (err, resp) {
		if (err) {
			throw err;
		}
		if (!resp.ast && resp.ast.type === "QMLProgram") {
			return callback("fail", name, "AST could not be found in response");
		}
		return callback("ok", name);
	});
});

groupEnd();