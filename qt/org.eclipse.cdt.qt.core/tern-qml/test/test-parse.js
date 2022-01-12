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
		if (!resp.ast || resp.ast.type !== "QMLProgram") {
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
		if (!resp.ast || resp.ast.type !== "QMLProgram") {
			return callback("fail", name, "AST could not be found in response");
		}
		return callback("ok", name);
	});
});

test("{Parse empty text}", function (server, callback, name) {
	server.parseString("", null, function (err, resp) {
		if (err) {
			throw err;
		}
		if (!resp.ast) {
			return callback("fail", name, "AST could not be found in response");
		} else if (resp.ast.type !== "QMLProgram" || resp.ast.mode !== "qml") {
			return callback("fail", name, "AST was not a QMLProgram with mode 'qml'");
		}
		return callback("ok", name);
	});
});

test("{Parse text no mode}", function (server, callback, name) {
	server.parseString("import QtQuick 2.0\nModule {\n\tComponent {\n\t}\n}", null, function (err, resp) {
		if (err) {
			throw err;
		}
		if (!resp.ast) {
			return callback("fail", name, "AST could not be found in response");
		} else if (resp.ast.type !== "QMLProgram" || resp.ast.mode !== "qml") {
			return callback("fail", name, "AST was not a QMLProgram with mode 'qml'");
		}
		return callback("ok", name);
	});
});

test("{Parse text (mode: qmltypes)}", function (server, callback, name) {
	server.parseString("QtObject {\n\tobj: {\n\t\tprop1: 1,\n\t\tprop2: 2\n\t}\n}", {
		mode: "qmltypes"
	}, function (err, resp) {
		if (err) {
			throw err;
		}
		if (!resp.ast) {
			return callback("fail", name, "AST could not be found in response");
		} else if (resp.ast.type !== "QMLProgram" || resp.ast.mode !== "qmltypes") {
			return callback("fail", name, "AST was not a QMLProgram with mode 'qmltypes'");
		}
		return callback("ok", name);
	});
});

test("{Parse text with locations}", function (server, callback, name) {
	server.parseString("var w = 3", {
		mode: "js",
		locations: true
	}, function (err, resp) {
		if (err) {
			throw err;
		}
		if (!resp.ast) {
			return callback("fail", name, "AST could not be found in response");
		} else if (resp.ast.type !== "Program") {
			return callback("fail", name, "AST was not a JavaScript Program");
		} else if (!resp.ast.loc) {
			return callback("fail", name, "AST had no loc object");
		}
		return callback("ok", name);
	});
});

groupEnd();