var testCompletion = require("./driver.js").testCompletion
var groupStart = require("./driver.js").groupStart;
var groupEnd = require("./driver.js").groupEnd;

var group = exports.group = "Code Completion"
groupStart(group);

// Import Completions
testCompletion("import QtQu|", {
	start: { line: 0, ch: 7 },
	end: { line: 0, ch: 11 },
	isProperty: false,
	isObjectKey: false,
	completions: [{ name: "QtQuick", type: "", origin: "QML" }]
});

testCompletion("import QtQuick 2.|", {
	start: { line: 0, ch: 17 },
	end: { line: 0, ch: 17 },
	isProperty: false,
	isObjectKey: false,
	completions: [
		{ name: "0", type: "number", origin: "QML" },
		{ name: "1", type: "number", origin: "QML" },
		{ name: "2", type: "number", origin: "QML" },
		{ name: "3", type: "number", origin: "QML" },
		{ name: "4", type: "number", origin: "QML" },
		{ name: "5", type: "number", origin: "QML" }
	]
});

testCompletion('import "other.qml" as Other\n|', {
	start: { line: 1, ch: 0 },
	end: { line: 1, ch: 0 },
	isProperty: false,
	isObjectKey: false,
	completions: [{ name: "Other", type: "?", origin: "test1.qml" }]
})

testCompletion('import "test2.qml" as Other\nWindow {\n\tOther {\n\t\t|\n\t}\n}', {
	start: { line: 3, ch: 2 },
	end: { line: 3, ch: 2 },
	isProperty: false,
	isObjectKey: false,
	completions: [{ name: "width", type: "number", origin: "test2.qml" }]
}, function (server) {
	server.options.getFile = function (name, callback) {
		if (name == "test2.qml") {
			return callback(null, "Button {\n\tproperty int width\n}");
		}
		return callback(null, null);
	}
});

// Property Binding Completions
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

testCompletion("Window {\n\ttest:| \n}", {
	start: { line: 1, ch: 6 },
	end: { line: 1, ch: 6 },
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

// Signal Completions
testCompletion("Window {\n\tsignal clicked(int mouseX, int mouseY)\n\t|\n}", {
	start: { line: 2, ch: 1 },
	end: { line: 2, ch: 1 },
	isProperty: false,
	isObjectKey: false,
	completions: [
		/* TODO: Should not include this line */ { name: "clicked", type: "fn(mouseX: ?, mouseY: ?) -> Signal", origin: "test1.qml" },
		{ name: "onClicked", type: "Signal Handler", origin: "test1.qml" }
	]
});

groupEnd();