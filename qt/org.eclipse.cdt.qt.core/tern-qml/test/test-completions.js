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
"use-strict";

var driver = require("./driver.js");
var test = driver.test;
var testCompletion = driver.testCompletion;
var groupStart = driver.groupStart;
var groupEnd = driver.groupEnd;

var group = exports.group = "Code Completion"
groupStart(group);

// Import Completions
//testCompletion("import QtQu|", {
//	start: { line: 0, ch: 7 },
//	end: { line: 0, ch: 11 },
//	isProperty: false,
//	isObjectKey: false,
//	completions: [{ name: "QtQuick", type: "", origin: "QML" }]
//});
//
//testCompletion("import QtQuick 2.|", {
//	start: { line: 0, ch: 17 },
//	end: { line: 0, ch: 17 },
//	isProperty: false,
//	isObjectKey: false,
//	completions: [
//		{ name: "0", type: "number", origin: "QML" },
//		{ name: "1", type: "number", origin: "QML" },
//		{ name: "2", type: "number", origin: "QML" },
//		{ name: "3", type: "number", origin: "QML" },
//		{ name: "4", type: "number", origin: "QML" },
//		{ name: "5", type: "number", origin: "QML" }
//	]
//});
//
//testCompletion('import "other.qml" as Other\n|', {
//	start: { line: 1, ch: 0 },
//	end: { line: 1, ch: 0 },
//	isProperty: false,
//	isObjectKey: false,
//	completions: [{ name: "Other", type: "?", origin: "test1.qml" }]
//})
//
//testCompletion('My|', {
//	start: { line: 0, ch: 2 },
//	end: { line: 0, ch: 2 },
//	isProperty: false,
//	isObjectKey: false,
//	completions: [{ name: "MyObject", type: "Button", origin: "MyObject.qml" }]
//}, function (server) {
//	server.addFile("MyObject.qml", "Button {\n\tproperty int width\n}");
//});

// QML Object Property Completions
testCompletion("Window {\n\tproperty int height\n\the|\n}", {
	start: { line: 2, ch: 1 },
	end: { line: 2, ch: 3 },
	isProperty: false,
	isObjectKey: false,
	completions: [{ name: "height", type: "number", origin: "test1.qml" }]
});

testCompletion("Window {\n\tproperty int height\n\tproperty int width\n\tproperty string text\n\t|\n}", {
	start: { line: 4, ch: 1 },
	end: { line: 4, ch: 1 },
	isProperty: false,
	isObjectKey: false,
	completions: [
		{ name: "height", type: "number", origin: "test1.qml" },
		{ name: "text", type: "string", origin: "test1.qml" },
		{ name: "width", type: "number", origin: "test1.qml" }
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
		{ name: "prop", type: "?", origin: "test1.qml" },
		{ name: "test", type: "fn()", origin: "test1.qml" }
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
	completions: [{ name: "height", type: "number", origin: "test1.qml" }]
}, function (server) { server.jsDefs = []; });

testCompletion("Window {\n\tproperty var btn\n\tButton {\n\t\tid: btn\n\t\tproperty int height\n\t}\n\ttest: btn.|\n}", {
	start: { line: 6, ch: 11 },
	end: { line: 6, ch: 11 },
	isProperty: true,
	isObjectKey: false,
	completions: [{ name: "height", type: "number", origin: "test1.qml" }]
}, function (server) { server.jsDefs = []; });

testCompletion("Window {\n\tButton {\n\t\tproperty var id\n\t\tid: btn\n\t}\n\ts: bt|\n}", {
	start: { line: 5, ch: 4 },
	end: { line: 5, ch: 6 },
	isProperty: false,
	isObjectKey: false,
	completions: [{ name: "btn", type: "Button", origin: "test1.qml" }]
}, function (server) { server.jsDefs = []; });

testCompletion("Window {\n\tButton {\n\t\tproperty var id\n\t\tid: btn\n\t\ts: i|\n\t}\n}", {
	start: { line: 4, ch: 5 },
	end: { line: 4, ch: 6 },
	isProperty: false,
	isObjectKey: false,
	completions: [{ name: "id", type: "?", origin: "test1.qml" }]
}, function (server) { server.jsDefs = []; });

testCompletion("Window {\n\tButton {\n\t\tproperty var id: 34\n\t\tid: btn\n\t\ts: i|\n\t}\n}", {
	start: { line: 4, ch: 5 },
	end: { line: 4, ch: 6 },
	isProperty: false,
	isObjectKey: false,
	completions: [{ name: "id", type: "number", origin: "test1.qml" }]
}, function (server) { server.jsDefs = []; });

testCompletion("Window {\n\tButton {\n\t\tproperty string id\n\t\tid: btn\n\t\ts: i|\n\t}\n}", {
	start: { line: 4, ch: 5 },
	end: { line: 4, ch: 6 },
	isProperty: false,
	isObjectKey: false,
	completions: [{ name: "id", type: "string", origin: "test1.qml" }]
}, function (server) { server.jsDefs = []; });

testCompletion("Window {\n\tButton {\n\t\tproperty var id\n\t\ts: i|\n\t}\n}", {
	start: { line: 3, ch: 5 },
	end: { line: 3, ch: 6 },
	isProperty: false,
	isObjectKey: false,
	completions: [{ name: "id", type: "?", origin: "test1.qml" }]
}, function (server) { server.jsDefs = []; });

testCompletion("Window {\n\tButton {\n\t\tproperty var id: 34\n\t\ts: i|\n\t}\n}", {
	start: { line: 3, ch: 5 },
	end: { line: 3, ch: 6 },
	isProperty: false,
	isObjectKey: false,
	completions: [{ name: "id", type: "number", origin: "test1.qml" }]
}, function (server) { server.jsDefs = []; });

testCompletion("Window {\n\tButton {\n\t\tproperty string id\n\t\ts: i|\n\t}\n}", {
	start: { line: 3, ch: 5 },
	end: { line: 3, ch: 6 },
	isProperty: false,
	isObjectKey: false,
	completions: [{ name: "id", type: "string", origin: "test1.qml" }]
}, function (server) { server.jsDefs = []; });

testCompletion("Window {\n\tid: wind\n\tfunction test() {\n\t\t|\n\t}\n}", {
	start: { line: 3, ch: 2 },
	end: { line: 3, ch: 2 },
	isProperty: false,
	isObjectKey: false,
	completions: [
		{ name: "test", type: "fn()", origin: "test1.qml" },
		{ name: "wind", type: "Window", origin: "test1.qml" }
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
		{ name: "test", type: "?", origin: "test1.qml" },
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
		{ name: "test", type: "fn()", origin: "test1.qml" },
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
		{ name: "onClicked", type: "Signal Handler", origin: "test1.qml" }
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
		{ name: "clicked", type: "fn(mouseX: number, mouseY: number)", origin: "test1.qml" },
		{ name: "onClicked", type: "Signal Handler", origin: "test1.qml" }
	]
}, function (server) { server.jsDefs = []; });

testCompletion("Window {\n\tsignal error(string msg, boolean flag)\n\ts: |\n}", {
	start: { line: 2, ch: 4 },
	end: { line: 2, ch: 4 },
	isProperty: false,
	isObjectKey: false,
	completions: [
		{ name: "error", type: "fn(msg: string, flag: bool)", origin: "test1.qml" },
		{ name: "onError", type: "Signal Handler", origin: "test1.qml" }
	]
}, function (server) { server.jsDefs = []; });

testCompletion("Window {\n\tid: wind\n\tsignal error(string msg, boolean flag)\n\ts: wind.|\n}", {
	start: { line: 3, ch: 9 },
	end: { line: 3, ch: 9 },
	isProperty: true,
	isObjectKey: false,
	completions: [
		{ name: "error", type: "fn(msg: string, flag: bool)", origin: "test1.qml" },
		{ name: "onError", type: "Signal Handler", origin: "test1.qml" }
	]
}, function (server) { server.jsDefs = []; });

testCompletion("Window {\n\tsignal error(string msg, boolean flag)\n\tonError: |\n}", {
	start: { line: 2, ch: 10 },
	end: { line: 2, ch: 10 },
	isProperty: false,
	isObjectKey: false,
	completions: [
		{ name: "error", type: "fn(msg: string, flag: bool)", origin: "test1.qml" },
		{ name: "flag", type: "bool", origin: "test1.qml" },
		{ name: "msg", type: "string", origin: "test1.qml" },
		{ name: "onError", type: "Signal Handler", origin: "test1.qml" }
	]
}, function (server) { server.jsDefs = []; });

testCompletion("Window {\n\tsignal error(string msg, boolean flag)\n\tonError: {\n\t\t|\n\t}\n}", {
	start: { line: 3, ch: 2 },
	end: { line: 3, ch: 2 },
	isProperty: false,
	isObjectKey: false,
	completions: [
		{ name: "error", type: "fn(msg: string, flag: bool)", origin: "test1.qml" },
		{ name: "flag", type: "bool", origin: "test1.qml" },
		{ name: "msg", type: "string", origin: "test1.qml" },
		{ name: "onError", type: "Signal Handler", origin: "test1.qml" }
	]
}, function (server) { server.jsDefs = []; });

testCompletion("Window {\n\tproperty int msg\n\tsignal error(string msg, boolean flag)\n\tonError: {\n\t\t|\n\t}\n}", {
	start: { line: 4, ch: 2 },
	end: { line: 4, ch: 2 },
	isProperty: false,
	isObjectKey: false,
	completions: [
		{ name: "error", type: "fn(msg: string, flag: bool)", origin: "test1.qml" },
		{ name: "flag", type: "bool", origin: "test1.qml" },
		{ name: "msg", type: "string", origin: "test1.qml" },
		{ name: "onError", type: "Signal Handler", origin: "test1.qml" }
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
		{ name: "a", type: "?", origin: "test1.qml" },
		{ name: "b", type: "?", origin: "test1.qml" },
		{ name: "c", type: "?", origin: "test1.qml" },
		{ name: "test", type: "fn(a: ?, b: ?, c: ?)", origin: "test1.qml" }
	]
}, function (server) { server.jsDefs = []; });

testCompletion("Window {\n\tfunction test(a) {\n\t\ta = 3\n\t\t|\n\t}\n}", {
	start: { line: 3, ch: 2 },
	end: { line: 3, ch: 2 },
	isProperty: false,
	isObjectKey: false,
	completions: [
		{ name: "a", type: "number", origin: "test1.qml" },
		{ name: "test", type: "fn(a: number)", origin: "test1.qml" }
	]
}, function (server) { server.jsDefs = []; });

testCompletion('Window {\n\tfunction test(a) {\n\t\ttest("something")\n\t\t|\n\t}\n}', {
	start: { line: 3, ch: 2 },
	end: { line: 3, ch: 2 },
	isProperty: false,
	isObjectKey: false,
	completions: [
		{ name: "a", type: "string", origin: "test1.qml" },
		{ name: "test", type: "fn(a: string)", origin: "test1.qml" }
	]
}, function (server) { server.jsDefs = []; });

testCompletion('Window {\n\tfunction test(a) {\n\t\t|\n\t\treturn 7\n\t}\n}', {
	start: { line: 2, ch: 2 },
	end: { line: 2, ch: 2 },
	isProperty: false,
	isObjectKey: false,
	completions: [
		{ name: "a", type: "?", origin: "test1.qml" },
		{ name: "test", type: "fn(a: ?) -> number", origin: "test1.qml" }
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
//		{ name: "a", type: "?", origin: "test1.qml" },
//		{ name: "test", type: "fn()", origin: "test1.qml" }
//	]
//}, function (server) { server.jsDefs = []; });

groupEnd();