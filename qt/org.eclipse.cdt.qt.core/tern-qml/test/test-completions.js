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
var testCompletion = driver.testCompletion;
var assertCompletion = driver.assertCompletion;
var groupStart = driver.groupStart;
var groupEnd = driver.groupEnd;

var group = exports.group = "Code Completion";
groupStart(group);

// Local Directory Completions
testCompletion('My|', {
	start: { line: 0, ch: 0 },
	end: { line: 0, ch: 2 },
	isProperty: false,
	isObjectKey: false,
	completions: [{ name: "MyObject", type: "MyObject", origin: "MyObject.qml" }]
}, function (server) {
	server.addFile("MyObject.qml", "Button {}");
});

testCompletion('import "./subdir/"\nSameDirTest {\n\t|\n}', {
	start: { line: 2, ch: 1 },
	end: { line: 2, ch: 1 },
	isProperty: false,
	isObjectKey: false,
	completions: [
		{ name: "obj", type: "?", origin: "subdir/Button.qml" },
		{ name: "Button", type: "Button", origin: "subdir/Button.qml" },
		{ name: "SameDirTest", type: "SameDirTest", origin: "subdir/SameDirTest.qml" }
	]
}, function (server) {
	server.addFile("subdir/SameDirTest.qml", "Button {}");
	server.addFile("subdir/Button.qml", "QtObject {property var obj}");
});

testCompletion('MyObject {\n\t|\n}', {
	start: { line: 1, ch: 1 },
	end: { line: 1, ch: 1 },
	isProperty: false,
	isObjectKey: false,
	completions: [
		{ name: "width", type: "number", origin: "MyObject.qml" },
		{ name: "MyObject", type: "MyObject", origin: "MyObject.qml" }
	]
}, function (server) {
	server.addFile("MyObject.qml", "Button {\n\tproperty int width\n}");
});

testCompletion('MyObject {\n\tid: obj\n\ts: obj.|\n}', {
	start: { line: 2, ch: 8 },
	end: { line: 2, ch: 8 },
	isProperty: true,
	isObjectKey: false,
	completions: [{ name: "width", type: "number", origin: "MyObject.qml" }]
}, function (server) {
	server.addFile("MyObject.qml", "Button {\n\tproperty int width\n}");
});

testCompletion('Button {\n\t|\n}', {
	start: { line: 1, ch: 1 },
	end: { line: 1, ch: 1 },
	isProperty: false,
	isObjectKey: false,
	completions: [
		{ name: "onClicked", type: "Signal Handler", origin: "Button.qml" },
		{ name: "Button", type: "Button", origin: "Button.qml" }
	]
}, function (server) {
	server.addFile("Button.qml", "QtObject {\n\signal clicked(int mouseX, int mouseY)\n}");
});

testCompletion('CButton {\n\t|\n}', {
	start: { line: 1, ch: 1 },
	end: { line: 1, ch: 1 },
	isProperty: false,
	isObjectKey: false,
	completions: [
		{ name: "height", type: "number", origin: "Button.qml" },
		{ name: "numClicks", type: "number", origin: "CButton.qml" },
		{ name: "text", type: "string", origin: "Button.qml" },
		{ name: "width", type: "number", origin: "Button.qml" },
		{ name: "Button", type: "Button", origin: "Button.qml" },
		{ name: "CButton", type: "CButton", origin: "CButton.qml" }
	]
}, function (server) {
	server.addFile("CButton.qml", "Button {\n\tproperty int numClicks\n}");
	server.addFile("Button.qml", "QtObject {\n\tproperty string text\n\tproperty int width\n\tproperty int height\n}");
});

testCompletion('CButton {\n\tid:btn\n\ts: btn.|\n}', {
	start: { line: 2, ch: 8 },
	end: { line: 2, ch: 8 },
	isProperty: true,
	isObjectKey: false,
	completions: [
		{ name: "height", type: "number", origin: "Button.qml" },
		{ name: "numClicks", type: "number", origin: "CButton.qml" },
		{ name: "text", type: "string", origin: "Button.qml" },
		{ name: "width", type: "number", origin: "Button.qml" }
	]
}, function (server) {
	server.addFile("CButton.qml", "Button {\n\tproperty int numClicks\n}");
	server.addFile("Button.qml", "QtObject {\n\tproperty string text\n\tproperty int width\n\tproperty int height\n}");
});

// Directory Import Completions
testCompletion('NotVisible {\n\t|\n}', {
	start: { line: 1, ch: 1 },
	end: { line: 1, ch: 1 },
	isProperty: false,
	isObjectKey: false,
	completions: []
}, function (server) {
	server.addFile("subdir/NotVisible.qml", "QtObject {\n\signal clicked(int mouseX, int mouseY)\n}");
});

testCompletion('import "./subdir/"\nButton {\n\t|\n}', {
	start: { line: 2, ch: 1 },
	end: { line: 2, ch: 1 },
	isProperty: false,
	isObjectKey: false,
	completions: [
		{ name: "onClicked", type: "Signal Handler", origin: "subdir/Button.qml" },
		{ name: "Button", type: "Button", origin: "subdir/Button.qml" }
	]
}, function (server) {
	server.addFile("subdir/Button.qml", "QtObject {\n\signal clicked(int mouseX, int mouseY)\n}");
});

testCompletion('import "./subdir/" as Controls\nControls.Button {\n\t|\n}', {
	start: { line: 2, ch: 1 },
	end: { line: 2, ch: 1 },
	isProperty: false,
	isObjectKey: false,
	completions: [
		{ name: "onClicked", type: "Signal Handler", origin: "subdir/Button.qml" },
		{ name: "Controls", type: "Controls", origin: "main.qml" }
	]
}, function (server) {
	server.addFile("subdir/Button.qml", "QtObject {\n\signal clicked(int mouseX, int mouseY)\n}");
});

testCompletion('import "./subdir/" as Controls\nControls.|', {
	start: { line: 1, ch: 9 },
	end: { line: 1, ch: 9 },
	isProperty: false,
	isObjectKey: false,
	completions: [
		{ name: "Button", type: "Button", origin: "subdir/Button.qml" }
	]
}, function (server) {
	server.addFile("subdir/Button.qml", "QtObject {\n\signal clicked(int mouseX, int mouseY)\n}");
});

testCompletion('import "./subdir/" as Controls\nControls.|.', {
	start: { line: 1, ch: 9 },
	end: { line: 1, ch: 9 },
	isProperty: false,
	isObjectKey: false,
	completions: [
		{ name: "Button", type: "Button", origin: "subdir/Button.qml" }
	]
}, function (server) {
	server.addFile("subdir/Button.qml", "QtObject {\n\signal clicked(int mouseX, int mouseY)\n}");
});

testCompletion('import "./subdir/" as Controls\nControls..|', {
	start: { line: 1, ch: 10 },
	end: { line: 1, ch: 10 },
	isProperty: false,
	isObjectKey: false,
	completions: []
}, function (server) {
	server.addFile("subdir/Button.qml", "QtObject {\n\signal clicked(int mouseX, int mouseY)\n}");
});

test("{Add File After Import}", function (server, callback, name) {
	var failed;
	assertCompletion(server, "", {
		start: { line: 0, ch: 0 },
		end: { line: 0, ch: 0 },
		isProperty: false,
		isObjectKey: false,
		completions: []
	}, 0, function (mis) {
		failed = mis;
	});
	if (failed) {
		return callback("fail", name, "- failed on initial file " + failed);
	}
	server.addFile("MyObject.qml", "QtObject {\n\tproperty var test\n}");
	assertCompletion(server, "", {
		start: { line: 0, ch: 0 },
		end: { line: 0, ch: 0 },
		isProperty: false,
		isObjectKey: false,
		completions: [{ name: "MyObject", type: "MyObject", origin: "MyObject.qml" }]
	}, 0, function (mis) {
		failed = mis;
	});
	if (failed) {
		return callback("fail", name, "- failed after adding file " + failed);
	}
	return callback("ok", name);
});

// Cyclic Dependency Completions
testCompletion('Cyclic {\n\t|\n}', {
	start: { line: 1, ch: 1 },
	end: { line: 1, ch: 1 },
	isProperty: false,
	isObjectKey: false,
	completions: [
		{ name: "test", type: "?", origin: "Cyclic.qml" },
		{ name: "Cyclic", type: "Cyclic", origin: "Cyclic.qml" }
	]
}, function (server) {
	server.addFile("Cyclic.qml", "Cyclic {\n\property var test\n}");
});

testCompletion('Cyclic2 {\n\t|\n}', {
	start: { line: 1, ch: 1 },
	end: { line: 1, ch: 1 },
	isProperty: false,
	isObjectKey: false,
	completions: [
		{ name: "test1", type: "?", origin: "Cyclic2.qml" },
		{ name: "test2", type: "?", origin: "OtherCyclic.qml" },
		{ name: "Cyclic2", type: "Cyclic2", origin: "Cyclic2.qml" },
		{ name: "OtherCyclic", type: "OtherCyclic", origin: "OtherCyclic.qml" }
	]
}, function (server) {
	server.addFile("Cyclic2.qml", "OtherCyclic {\n\property var test1\n}");
	server.addFile("OtherCyclic.qml", "Cyclic2 {\n\property var test2\n}");
});

testCompletion('OtherCyclic {\n\t|\n}', {
	start: { line: 1, ch: 1 },
	end: { line: 1, ch: 1 },
	isProperty: false,
	isObjectKey: false,
	completions: [
		{ name: "test2", type: "?", origin: "OtherCyclic.qml" },
		{ name: "Cyclic", type: "Cyclic", origin: "Cyclic.qml" },
		{ name: "OtherCyclic", type: "OtherCyclic", origin: "OtherCyclic.qml" }
	]
}, function (server) {
	server.addFile("Cyclic.qml", "OtherCyclic {\n\property var test1\n}");
	server.addFile("OtherCyclic.qml", "Cyclic {\n\property var test2\n}");
});

// QML Object Property Completions
testCompletion("Window {\n\tproperty int height\n\the|\n}", {
	start: { line: 2, ch: 1 },
	end: { line: 2, ch: 3 },
	isProperty: false,
	isObjectKey: false,
	completions: [{ name: "height", type: "number", origin: "main.qml" }]
});

testCompletion("Window {\n\tproperty int height\n\tproperty int width\n\tproperty string text\n\t|\n}", {
	start: { line: 4, ch: 1 },
	end: { line: 4, ch: 1 },
	isProperty: false,
	isObjectKey: false,
	completions: [
		{ name: "height", type: "number", origin: "main.qml" },
		{ name: "text", type: "string", origin: "main.qml" },
		{ name: "width", type: "number", origin: "main.qml" }
	]
});

testCompletion("Window {\n\tproperty int height\n\tObject {\n\t\t|\n\t}\n}", {
	start: { line: 3, ch: 2 },
	end: { line: 3, ch: 2 },
	isProperty: false,
	isObjectKey: false,
	completions: []
});

testCompletion("Window {\n\tproperty var prop\n\tfunction test() {\n\t\t|\n\t}\n}", {
	start: { line: 3, ch: 2 },
	end: { line: 3, ch: 2 },
	isProperty: false,
	isObjectKey: false,
	completions: [
		{ name: "prop", type: "?", origin: "main.qml" },
		{ name: "test", type: "fn()", origin: "main.qml" }
	]
}, function (server) { server.jsDefs = []; });

// QML ID Property Completions
testCompletion("Window {\n\tid: btn\n\t|\n}", {
	start: { line: 2, ch: 1 },
	end: { line: 2, ch: 1 },
	isProperty: false,
	isObjectKey: false,
	completions: []
});

testCompletion("Window {\n\tButton {\n\t\tid: btn\n\t\tproperty int height\n\t}\n\ttest: btn.|\n}", {
	start: { line: 5, ch: 11 },
	end: { line: 5, ch: 11 },
	isProperty: true,
	isObjectKey: false,
	completions: [{ name: "height", type: "number", origin: "main.qml" }]
}, function (server) { server.jsDefs = []; });

testCompletion("Window {\n\tproperty var btn\n\tButton {\n\t\tid: btn\n\t\tproperty int height\n\t}\n\ttest: btn.|\n}", {
	start: { line: 6, ch: 11 },
	end: { line: 6, ch: 11 },
	isProperty: true,
	isObjectKey: false,
	completions: [{ name: "height", type: "number", origin: "main.qml" }]
}, function (server) { server.jsDefs = []; });

testCompletion("Window {\n\tButton {\n\t\tproperty var id\n\t\tid: btn\n\t}\n\ts: bt|\n}", {
	start: { line: 5, ch: 4 },
	end: { line: 5, ch: 6 },
	isProperty: false,
	isObjectKey: false,
	completions: [{ name: "btn", type: "Button", origin: "main.qml" }]
}, function (server) { server.jsDefs = []; });

testCompletion("Window {\n\tButton {\n\t\tproperty var id\n\t\tid: btn\n\t\ts: i|\n\t}\n}", {
	start: { line: 4, ch: 5 },
	end: { line: 4, ch: 6 },
	isProperty: false,
	isObjectKey: false,
	completions: [{ name: "id", type: "?", origin: "main.qml" }]
}, function (server) { server.jsDefs = []; });

testCompletion("Window {\n\tButton {\n\t\tproperty var id: 34\n\t\tid: btn\n\t\ts: i|\n\t}\n}", {
	start: { line: 4, ch: 5 },
	end: { line: 4, ch: 6 },
	isProperty: false,
	isObjectKey: false,
	completions: [{ name: "id", type: "number", origin: "main.qml" }]
}, function (server) { server.jsDefs = []; });

testCompletion("Window {\n\tButton {\n\t\tproperty string id\n\t\tid: btn\n\t\ts: i|\n\t}\n}", {
	start: { line: 4, ch: 5 },
	end: { line: 4, ch: 6 },
	isProperty: false,
	isObjectKey: false,
	completions: [{ name: "id", type: "string", origin: "main.qml" }]
}, function (server) { server.jsDefs = []; });

testCompletion("Window {\n\tButton {\n\t\tproperty var id\n\t\ts: i|\n\t}\n}", {
	start: { line: 3, ch: 5 },
	end: { line: 3, ch: 6 },
	isProperty: false,
	isObjectKey: false,
	completions: [{ name: "id", type: "?", origin: "main.qml" }]
}, function (server) { server.jsDefs = []; });

testCompletion("Window {\n\tButton {\n\t\tproperty var id: 34\n\t\ts: i|\n\t}\n}", {
	start: { line: 3, ch: 5 },
	end: { line: 3, ch: 6 },
	isProperty: false,
	isObjectKey: false,
	completions: [{ name: "id", type: "number", origin: "main.qml" }]
}, function (server) { server.jsDefs = []; });

testCompletion("Window {\n\tButton {\n\t\tproperty string id\n\t\ts: i|\n\t}\n}", {
	start: { line: 3, ch: 5 },
	end: { line: 3, ch: 6 },
	isProperty: false,
	isObjectKey: false,
	completions: [{ name: "id", type: "string", origin: "main.qml" }]
}, function (server) { server.jsDefs = []; });

testCompletion("Window {\n\tid: wind\n\tfunction test() {\n\t\t|\n\t}\n}", {
	start: { line: 3, ch: 2 },
	end: { line: 3, ch: 2 },
	isProperty: false,
	isObjectKey: false,
	completions: [
		{ name: "test", type: "fn()", origin: "main.qml" },
		{ name: "wind", type: "Window", origin: "main.qml" }
	]
}, function (server) { server.jsDefs = []; });

// JavaScript Completions
testCompletion("Window {\n\tproperty var test: |\n}", {
	start: { line: 1, ch: 20 },
	end: { line: 1, ch: 20 },
	isProperty: false,
	isObjectKey: false,
	completions: [
		{ name: "decodeURI", type: "fn(uri: string) -> string", origin: "ecma5" },
		{ name: "decodeURIComponent", type: "fn(uri: string) -> string", origin: "ecma5" },
		{ name: "encodeURI", type: "fn(uri: string) -> string", origin: "ecma5" },
		{ name: "encodeURIComponent", type: "fn(uri: string) -> string", origin: "ecma5" },
		{ name: "eval", type: "fn(code: string)", origin: "ecma5" },
		{ name: "isFinite", type: "fn(value: number) -> bool", origin: "ecma5" },
		{ name: "isNaN", type: "fn(value: number) -> bool", origin: "ecma5" },
		{ name: "parseFloat", type: "fn(string: string) -> number", origin: "ecma5" },
		{ name: "parseInt", type: "fn(string: string, radix?: number) -> number", origin: "ecma5" },
		{ name: "test", type: "?", origin: "main.qml" },
		{ name: "undefined", type: "?", origin: "ecma5" },
		{ name: "Array", type: "fn(size: number)", origin: "ecma5" },
		{ name: "Boolean", type: "fn(value: ?) -> bool", origin: "ecma5" },
		{ name: "Date", type: "fn(ms: number)", origin: "ecma5" },
		{ name: "Error", type: "fn(message: string)", origin: "ecma5" },
		{ name: "EvalError", type: "fn(message: string)", origin: "ecma5" },
		{ name: "Function", type: "fn(body: string) -> fn()", origin: "ecma5" },
		{ name: "Infinity", type: "number", origin: "ecma5" },
		{ name: "JSON", type: "JSON", origin: "ecma5" },
		{ name: "Math", type: "Math", origin: "ecma5" },
		{ name: "NaN", type: "number", origin: "ecma5" },
		{ name: "Number", type: "fn(value: ?) -> number", origin: "ecma5" },
		{ name: "Object", type: "fn()", origin: "ecma5" },
		{ name: "RangeError", type: "fn(message: string)", origin: "ecma5" },
		{ name: "ReferenceError", type: "fn(message: string)", origin: "ecma5" },
		{ name: "RegExp", type: "fn(source: string, flags?: string)", origin: "ecma5" },
		{ name: "String", type: "fn(value: ?) -> string", origin: "ecma5" },
		{ name: "SyntaxError", type: "fn(message: string)", origin: "ecma5" },
		{ name: "TypeError", type: "fn(message: string)", origin: "ecma5" },
		{ name: "URIError", type: "fn(message: string)", origin: "ecma5" }
	]
});

testCompletion("Window {\n\ttest: |\n}", {
	start: { line: 1, ch: 7 },
	end: { line: 1, ch: 7 },
	isProperty: false,
	isObjectKey: false,
	completions: [
		{ name: "decodeURI", type: "fn(uri: string) -> string", origin: "ecma5" },
		{ name: "decodeURIComponent", type: "fn(uri: string) -> string", origin: "ecma5" },
		{ name: "encodeURI", type: "fn(uri: string) -> string", origin: "ecma5" },
		{ name: "encodeURIComponent", type: "fn(uri: string) -> string", origin: "ecma5" },
		{ name: "eval", type: "fn(code: string)", origin: "ecma5" },
		{ name: "isFinite", type: "fn(value: number) -> bool", origin: "ecma5" },
		{ name: "isNaN", type: "fn(value: number) -> bool", origin: "ecma5" },
		{ name: "parseFloat", type: "fn(string: string) -> number", origin: "ecma5" },
		{ name: "parseInt", type: "fn(string: string, radix?: number) -> number", origin: "ecma5" },
		{ name: "undefined", type: "?", origin: "ecma5" },
		{ name: "Array", type: "fn(size: number)", origin: "ecma5" },
		{ name: "Boolean", type: "fn(value: ?) -> bool", origin: "ecma5" },
		{ name: "Date", type: "fn(ms: number)", origin: "ecma5" },
		{ name: "Error", type: "fn(message: string)", origin: "ecma5" },
		{ name: "EvalError", type: "fn(message: string)", origin: "ecma5" },
		{ name: "Function", type: "fn(body: string) -> fn()", origin: "ecma5" },
		{ name: "Infinity", type: "number", origin: "ecma5" },
		{ name: "JSON", type: "JSON", origin: "ecma5" },
		{ name: "Math", type: "Math", origin: "ecma5" },
		{ name: "NaN", type: "number", origin: "ecma5" },
		{ name: "Number", type: "fn(value: ?) -> number", origin: "ecma5" },
		{ name: "Object", type: "fn()", origin: "ecma5" },
		{ name: "RangeError", type: "fn(message: string)", origin: "ecma5" },
		{ name: "ReferenceError", type: "fn(message: string)", origin: "ecma5" },
		{ name: "RegExp", type: "fn(source: string, flags?: string)", origin: "ecma5" },
		{ name: "String", type: "fn(value: ?) -> string", origin: "ecma5" },
		{ name: "SyntaxError", type: "fn(message: string)", origin: "ecma5" },
		{ name: "TypeError", type: "fn(message: string)", origin: "ecma5" },
		{ name: "URIError", type: "fn(message: string)", origin: "ecma5" }
	]
});

testCompletion("Window {\n\ttest: {\n\t\t|\n\t}\n}", {
	start: { line: 2, ch: 2 },
	end: { line: 2, ch: 2 },
	isProperty: false,
	isObjectKey: false,
	completions: [
		{ name: "decodeURI", type: "fn(uri: string) -> string", origin: "ecma5" },
		{ name: "decodeURIComponent", type: "fn(uri: string) -> string", origin: "ecma5" },
		{ name: "encodeURI", type: "fn(uri: string) -> string", origin: "ecma5" },
		{ name: "encodeURIComponent", type: "fn(uri: string) -> string", origin: "ecma5" },
		{ name: "eval", type: "fn(code: string)", origin: "ecma5" },
		{ name: "isFinite", type: "fn(value: number) -> bool", origin: "ecma5" },
		{ name: "isNaN", type: "fn(value: number) -> bool", origin: "ecma5" },
		{ name: "parseFloat", type: "fn(string: string) -> number", origin: "ecma5" },
		{ name: "parseInt", type: "fn(string: string, radix?: number) -> number", origin: "ecma5" },
		{ name: "undefined", type: "?", origin: "ecma5" },
		{ name: "Array", type: "fn(size: number)", origin: "ecma5" },
		{ name: "Boolean", type: "fn(value: ?) -> bool", origin: "ecma5" },
		{ name: "Date", type: "fn(ms: number)", origin: "ecma5" },
		{ name: "Error", type: "fn(message: string)", origin: "ecma5" },
		{ name: "EvalError", type: "fn(message: string)", origin: "ecma5" },
		{ name: "Function", type: "fn(body: string) -> fn()", origin: "ecma5" },
		{ name: "Infinity", type: "number", origin: "ecma5" },
		{ name: "JSON", type: "JSON", origin: "ecma5" },
		{ name: "Math", type: "Math", origin: "ecma5" },
		{ name: "NaN", type: "number", origin: "ecma5" },
		{ name: "Number", type: "fn(value: ?) -> number", origin: "ecma5" },
		{ name: "Object", type: "fn()", origin: "ecma5" },
		{ name: "RangeError", type: "fn(message: string)", origin: "ecma5" },
		{ name: "ReferenceError", type: "fn(message: string)", origin: "ecma5" },
		{ name: "RegExp", type: "fn(source: string, flags?: string)", origin: "ecma5" },
		{ name: "String", type: "fn(value: ?) -> string", origin: "ecma5" },
		{ name: "SyntaxError", type: "fn(message: string)", origin: "ecma5" },
		{ name: "TypeError", type: "fn(message: string)", origin: "ecma5" },
		{ name: "URIError", type: "fn(message: string)", origin: "ecma5" }
	]
});

testCompletion("Window {\n\tfunction test() {\n\t\t|\n\t}\n}", {
	start: { line: 2, ch: 2 },
	end: { line: 2, ch: 2 },
	isProperty: false,
	isObjectKey: false,
	completions: [
		{ name: "decodeURI", type: "fn(uri: string) -> string", origin: "ecma5" },
		{ name: "decodeURIComponent", type: "fn(uri: string) -> string", origin: "ecma5" },
		{ name: "encodeURI", type: "fn(uri: string) -> string", origin: "ecma5" },
		{ name: "encodeURIComponent", type: "fn(uri: string) -> string", origin: "ecma5" },
		{ name: "eval", type: "fn(code: string)", origin: "ecma5" },
		{ name: "isFinite", type: "fn(value: number) -> bool", origin: "ecma5" },
		{ name: "isNaN", type: "fn(value: number) -> bool", origin: "ecma5" },
		{ name: "parseFloat", type: "fn(string: string) -> number", origin: "ecma5" },
		{ name: "parseInt", type: "fn(string: string, radix?: number) -> number", origin: "ecma5" },
		{ name: "test", type: "fn()", origin: "main.qml" },
		{ name: "undefined", type: "?", origin: "ecma5" },
		{ name: "Array", type: "fn(size: number)", origin: "ecma5" },
		{ name: "Boolean", type: "fn(value: ?) -> bool", origin: "ecma5" },
		{ name: "Date", type: "fn(ms: number)", origin: "ecma5" },
		{ name: "Error", type: "fn(message: string)", origin: "ecma5" },
		{ name: "EvalError", type: "fn(message: string)", origin: "ecma5" },
		{ name: "Function", type: "fn(body: string) -> fn()", origin: "ecma5" },
		{ name: "Infinity", type: "number", origin: "ecma5" },
		{ name: "JSON", type: "JSON", origin: "ecma5" },
		{ name: "Math", type: "Math", origin: "ecma5" },
		{ name: "NaN", type: "number", origin: "ecma5" },
		{ name: "Number", type: "fn(value: ?) -> number", origin: "ecma5" },
		{ name: "Object", type: "fn()", origin: "ecma5" },
		{ name: "RangeError", type: "fn(message: string)", origin: "ecma5" },
		{ name: "ReferenceError", type: "fn(message: string)", origin: "ecma5" },
		{ name: "RegExp", type: "fn(source: string, flags?: string)", origin: "ecma5" },
		{ name: "String", type: "fn(value: ?) -> string", origin: "ecma5" },
		{ name: "SyntaxError", type: "fn(message: string)", origin: "ecma5" },
		{ name: "TypeError", type: "fn(message: string)", origin: "ecma5" },
		{ name: "URIError", type: "fn(message: string)", origin: "ecma5" }
	]
});

// Signal and Signal Handler Completions
testCompletion("Window {\n\tsignal clicked(int mouseX, int mouseY)\n\t|\n}", {
	start: { line: 2, ch: 1 },
	end: { line: 2, ch: 1 },
	isProperty: false,
	isObjectKey: false,
	completions: [
		{ name: "onClicked", type: "Signal Handler", origin: "main.qml" }
	]
});

testCompletion("Window {\n\tsignal clicked(int mouseX, int mouseY)\n\tButton {\n\t\t|\n\t}\n}", {
	start: { line: 3, ch: 2 },
	end: { line: 3, ch: 2 },
	isProperty: false,
	isObjectKey: false,
	completions: []
});

testCompletion("Window {\n\tsignal clicked(int mouseX, int mouseY)\n\ts: |\n}", {
	start: { line: 2, ch: 4 },
	end: { line: 2, ch: 4 },
	isProperty: false,
	isObjectKey: false,
	completions: [
		{ name: "clicked", type: "fn(mouseX: number, mouseY: number)", origin: "main.qml" },
		{ name: "onClicked", type: "Signal Handler", origin: "main.qml" }
	]
}, function (server) { server.jsDefs = []; });

testCompletion("Window {\n\tsignal error(string msg, boolean flag)\n\ts: |\n}", {
	start: { line: 2, ch: 4 },
	end: { line: 2, ch: 4 },
	isProperty: false,
	isObjectKey: false,
	completions: [
		{ name: "error", type: "fn(msg: string, flag: bool)", origin: "main.qml" },
		{ name: "onError", type: "Signal Handler", origin: "main.qml" }
	]
}, function (server) { server.jsDefs = []; });

testCompletion("Window {\n\tid: wind\n\tsignal error(string msg, boolean flag)\n\ts: wind.|\n}", {
	start: { line: 3, ch: 9 },
	end: { line: 3, ch: 9 },
	isProperty: true,
	isObjectKey: false,
	completions: [
		{ name: "error", type: "fn(msg: string, flag: bool)", origin: "main.qml" },
		{ name: "onError", type: "Signal Handler", origin: "main.qml" }
	]
}, function (server) { server.jsDefs = []; });

testCompletion("Window {\n\tsignal error(string msg, boolean flag)\n\tonError: |\n}", {
	start: { line: 2, ch: 10 },
	end: { line: 2, ch: 10 },
	isProperty: false,
	isObjectKey: false,
	completions: [
		{ name: "error", type: "fn(msg: string, flag: bool)", origin: "main.qml" },
		{ name: "flag", type: "bool", origin: "main.qml" },
		{ name: "msg", type: "string", origin: "main.qml" },
		{ name: "onError", type: "Signal Handler", origin: "main.qml" }
	]
}, function (server) { server.jsDefs = []; });

testCompletion("Window {\n\tsignal error(string msg, boolean flag)\n\tonError: {\n\t\t|\n\t}\n}", {
	start: { line: 3, ch: 2 },
	end: { line: 3, ch: 2 },
	isProperty: false,
	isObjectKey: false,
	completions: [
		{ name: "error", type: "fn(msg: string, flag: bool)", origin: "main.qml" },
		{ name: "flag", type: "bool", origin: "main.qml" },
		{ name: "msg", type: "string", origin: "main.qml" },
		{ name: "onError", type: "Signal Handler", origin: "main.qml" }
	]
}, function (server) { server.jsDefs = []; });

testCompletion("Window {\n\tproperty int msg\n\tsignal error(string msg, boolean flag)\n\tonError: {\n\t\t|\n\t}\n}", {
	start: { line: 4, ch: 2 },
	end: { line: 4, ch: 2 },
	isProperty: false,
	isObjectKey: false,
	completions: [
		{ name: "error", type: "fn(msg: string, flag: bool)", origin: "main.qml" },
		{ name: "flag", type: "bool", origin: "main.qml" },
		{ name: "msg", type: "string", origin: "main.qml" },
		{ name: "onError", type: "Signal Handler", origin: "main.qml" }
	]
}, function (server) { server.jsDefs = []; });

// Function Declarations
testCompletion("Window {\n\tfunction test() {}\n\t|\n}", {
	start: { line: 2, ch: 1 },
	end: { line: 2, ch: 1 },
	isProperty: false,
	isObjectKey: false,
	completions: []
});

testCompletion("Window {\n\tfunction test(a, b, c) {\n\t\t|\n\t}\n}", {
	start: { line: 2, ch: 2 },
	end: { line: 2, ch: 2 },
	isProperty: false,
	isObjectKey: false,
	completions: [
		{ name: "a", type: "?", origin: "main.qml" },
		{ name: "b", type: "?", origin: "main.qml" },
		{ name: "c", type: "?", origin: "main.qml" },
		{ name: "test", type: "fn(a: ?, b: ?, c: ?)", origin: "main.qml" }
	]
}, function (server) { server.jsDefs = []; });

testCompletion("Window {\n\tfunction test(a) {\n\t\ta = 3\n\t\t|\n\t}\n}", {
	start: { line: 3, ch: 2 },
	end: { line: 3, ch: 2 },
	isProperty: false,
	isObjectKey: false,
	completions: [
		{ name: "a", type: "number", origin: "main.qml" },
		{ name: "test", type: "fn(a: number)", origin: "main.qml" }
	]
}, function (server) { server.jsDefs = []; });

testCompletion('Window {\n\tfunction test(a) {\n\t\ttest("something")\n\t\t|\n\t}\n}', {
	start: { line: 3, ch: 2 },
	end: { line: 3, ch: 2 },
	isProperty: false,
	isObjectKey: false,
	completions: [
		{ name: "a", type: "string", origin: "main.qml" },
		{ name: "test", type: "fn(a: string)", origin: "main.qml" }
	]
}, function (server) { server.jsDefs = []; });

testCompletion('Window {\n\tfunction test(a) {\n\t\t|\n\t\treturn 7\n\t}\n}', {
	start: { line: 2, ch: 2 },
	end: { line: 2, ch: 2 },
	isProperty: false,
	isObjectKey: false,
	completions: [
		{ name: "a", type: "?", origin: "main.qml" },
		{ name: "test", type: "fn(a: ?) -> number", origin: "main.qml" }
	]
}, function (server) { server.jsDefs = []; });

// TODO: Uncomment once this is fixed.  The find defs version of this test does find the right definition
//       so it seems the problem is related to Tern and not the QML plugin.
//testCompletion('Window {\n\tproperty int a\n\tfunction test(a) {\n\t\t|\n\t}\n}', {
//	start: { line: 3, ch: 2 },
//	end: { line: 3, ch: 2 },
//	isProperty: false,
//	isObjectKey: false,
//	completions: [
//		{ name: "a", type: "?", origin: "main.qml" },
//		{ name: "test", type: "fn()", origin: "main.qml" }
//	]
//}, function (server) { server.jsDefs = []; });

groupEnd();