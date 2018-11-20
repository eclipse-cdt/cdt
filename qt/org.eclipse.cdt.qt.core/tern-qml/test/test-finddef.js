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
var testDefinition = driver.testDefinition;
var assertDefinition = driver.assertDefintion;
var groupStart = driver.groupStart;
var groupEnd = driver.groupEnd;

var group = exports.group = "Find Definition";
groupStart(group);

// Directory Imports
// TODO: Getting this test to pass breaks some of the type inferencing which is more important
//testDefinition('My|Object {}', {
//	origin: "MyObject.qml",
//	start: { line: 0, ch: 0 },
//	end: { line: 0, ch: 6 },
//	file: "MyObject.qml",
//	contextOffset: 0
//}, function (server) {
//	server.addFile("MyObject.qml", "Button {}");
//});

testDefinition("MyObject {\n\tte|stProp: ident\n}", {
	origin: "MyObject.qml",
	start: { line: 1, ch: 14 },
	end: { line: 1, ch: 22 },
	file: "MyObject.qml",
	contextOffset: 23
}, function (server) {
	server.addFile("MyObject.qml", "Button {\n\tproperty var testProp\n}");
});

testDefinition("MyObject {\n\ts: te|stSig()\n}", {
	origin: "MyObject.qml",
	start: { line: 1, ch: 8 },
	end: { line: 1, ch: 15 },
	file: "MyObject.qml",
	contextOffset: 17
}, function (server) {
	server.addFile("MyObject.qml", "Button {\n\tsignal testSig()\n}");
});

testDefinition("MyObject {\n\tonTe|stSig: ident\n}", {
	origin: "MyObject.qml",
	start: { line: 1, ch: 8 },
	end: { line: 1, ch: 15 },
	file: "MyObject.qml",
	contextOffset: 17
}, function (server) {
	server.addFile("MyObject.qml", "Button {\n\tsignal testSig()\n}");
});

testDefinition("MyObject {\n\tonTestSig: ar|g0\n}", {
	origin: "MyObject.qml",
	start: { line: 1, ch: 20 },
	end: { line: 1, ch: 24 },
	file: "MyObject.qml",
	contextOffset: 29
}, function (server) {
	server.addFile("MyObject.qml", "Button {\n\tsignal testSig(int arg0)\n}");
});

testDefinition("MyObject {\n\ts: te|stFn()\n}", {
	origin: "MyObject.qml",
	start: { line: 1, ch: 10 },
	end: { line: 1, ch: 16 },
	file: "MyObject.qml",
	contextOffset: 19
}, function (server) {
	server.addFile("MyObject.qml", "Button {\n\tfunction testFn() {}\n}");
});

testDefinition("MyObject {\n\ts: btn|Id\n}", {
	origin: undefined,
	start: undefined,
	end: undefined,
	file: undefined,
	contextOffset: undefined
}, function (server) {
	server.addFile("MyObject.qml", "Button {\n\tid: btnId\n}");
});

testDefinition("MyObject {\n\tot|her: ident\n}", {
	origin: undefined,
	start: undefined,
	end: undefined,
	file: undefined,
	contextOffset: undefined
}, function (server) {
	server.addFile("MyObject.qml", "Button {\n\tSecondButton {\n\t\tjproperty var other\n\t}\n}");
});

// ID Property
testDefinition("QtObject {\n\tid: obj\n\tproperty var prop: {\n\t\tob|j\n\t}\n}", {
	origin: "main.qml",
	start: { line: 1, ch: 5 },
	end: { line: 1, ch: 8 },
	file: "main.qml",
	contextOffset: 16
});

testDefinition("Window {\n\tButton {\n\t\tid: btn\n\t\tproperty int height\n\t}\n\ttest: bt|n\n}", {
	origin: "main.qml",
	start: { line: 2, ch: 6 },
	end: { line: 2, ch: 9 },
	file: "main.qml",
	contextOffset: 25
});

testDefinition("Window {\n\tproperty var btn\n\tButton {\n\t\tid: btn\n\t\tproperty int height\n\t}\n\ttest: bt|n\n}", {
	origin: "main.qml",
	start: { line: 3, ch: 6 },
	end: { line: 3, ch: 9 },
	file: "main.qml",
	contextOffset: 43
});

testDefinition("Window {\n\tproperty var btn\n\tButton {\n\t\tid: bt|n\n\t\tproperty int height\n\t}\n}", {
	origin: "main.qml",
	start: { line: 3, ch: 6 },
	end: { line: 3, ch: 9 },
	file: "main.qml",
	contextOffset: 43
});

// Property Declarations
testDefinition("QtObject {\n\tproperty var prop\n\tpr|op: 3\n}", {
	origin: "main.qml",
	start: { line: 1, ch: 14 },
	end: { line: 1, ch: 18 },
	file: "main.qml",
	contextOffset: 25
});

testDefinition("Window {\n\tproperty var btn\n\tButton {\n\t\tprop: b|tn\n\t}\n}", {
	origin: "main.qml",
	start: { line: 1, ch: 14 },
	end: { line: 1, ch: 17 },
	file: "main.qml",
	contextOffset: 23
});

testDefinition("Window {\n\tproperty var btn\n\tButton {\n\t\tButton {\n\t\t\tprop: b|tn\n\t\t}\n\t}\n}", {
	origin: "main.qml",
	start: { line: 1, ch: 14 },
	end: { line: 1, ch: 17 },
	file: "main.qml",
	contextOffset: 23
});

testDefinition("Window {\n\tButton {\n\t\tproperty var btn\n\t\tbt|n: 3\n\t}\n}", {
	origin: "main.qml",
	start: { line: 2, ch: 15 },
	end: { line: 2, ch: 18 },
	file: "main.qml",
	contextOffset: 34
});

testDefinition("Window {\n\tButton {\n\t\tproperty var btn\n\t\tButton {\n\t\t\tprop: b|tn\n\t\t}\n\t}\n}", {
	origin: undefined,
	start: undefined,
	end: undefined,
	file: undefined,
	contextOffset: undefined
});

// Signals and Signal Handlers
testDefinition("Window {\n\tsignal clic|ked(int mouseX, int mouseY)\n}", {
	origin: "main.qml",
	start: { line: 1, ch: 8 },
	end: { line: 1, ch: 15 },
	file: "main.qml",
	contextOffset: 17
});

testDefinition("Window {\n\tsignal clicked(int mouseX, int mouseY)\n\tonCli|cked: 3\n}", {
	origin: "main.qml",
	start: { line: 1, ch: 8 },
	end: { line: 1, ch: 15 },
	file: "main.qml",
	contextOffset: 17
});

testDefinition("Window {\n\tsignal clicked(int mouseX, int mouseY)\n\tonClicked: mou|seX\n}", {
	origin: "main.qml",
	start: { line: 1, ch: 20 },
	end: { line: 1, ch: 26 },
	file: "main.qml",
	contextOffset: 29
});

testDefinition("Window {\n\tsignal clicked(int mouseX, int mouseY)\n\tonClicked: cli|cked(3,4)\n}", {
	origin: "main.qml",
	start: { line: 1, ch: 8 },
	end: { line: 1, ch: 15 },
	file: "main.qml",
	contextOffset: 17
});

testDefinition("Window {\n\tsignal clicked(int mouseX, int mouseY)\n\tonClicked: onCli|cked\n}", {
	origin: "main.qml",
	start: { line: 1, ch: 8 },
	end: { line: 1, ch: 15 },
	file: "main.qml",
	contextOffset: 17
});

testDefinition("Window {\n\tsignal clicked(int mouseX, int mouseY)\n\tid: wind\n\tonClicked: wind.onCli|cked\n}", {
	origin: "main.qml",
	start: { line: 1, ch: 8 },
	end: { line: 1, ch: 15 },
	file: "main.qml",
	contextOffset: 17
});

testDefinition("Window {\n\tsignal clicked(int mouseX, int mouseY)\n\tid: wind\n\tonClicked: wind.cli|cked(3,4)\n}", {
	origin: "main.qml",
	start: { line: 1, ch: 8 },
	end: { line: 1, ch: 15 },
	file: "main.qml",
	contextOffset: 17
});

testDefinition("Window {\n\tproperty int msg\n\tsignal error(string msg, boolean flag)\n\tonError: {\n\t\tms|g\n\t}\n}", {
	origin: "main.qml",
	start: { line: 2, ch: 21 },
	end: { line: 2, ch: 24 },
	file: "main.qml",
	contextOffset: 48
}, function (server) { server.jsDefs = []; });

// Function Declarations
testDefinition("Window {\n\tfunction te|st(a) {}\n}", {
	origin: "main.qml",
	start: { line: 1, ch: 10 },
	end: { line: 1, ch: 14 },
	file: "main.qml",
	contextOffset: 19
});

testDefinition("Window {\n\tfunction test(a) {\n\t\ta|\n\t}\n}", {
	origin: "main.qml",
	start: { line: 1, ch: 15 },
	end: { line: 1, ch: 16 },
	file: "main.qml",
	contextOffset: 24
});

testDefinition("Window {\n\tfunction test(a) {\n\t\tte|st(3)\n\t}\n}", {
	origin: "main.qml",
	start: { line: 1, ch: 10 },
	end: { line: 1, ch: 14 },
	file: "main.qml",
	contextOffset: 19
});

testDefinition("Window {\n\tfunction test(a) {}\n\ts: te|st(3)\n}", {
	origin: "main.qml",
	start: { line: 1, ch: 10 },
	end: { line: 1, ch: 14 },
	file: "main.qml",
	contextOffset: 19
});

testDefinition("Window {\n\tfunction test(a) {}\n\ts: {\n\t\tte|st(3)\n\t}\n}", {
	origin: "main.qml",
	start: { line: 1, ch: 10 },
	end: { line: 1, ch: 14 },
	file: "main.qml",
	contextOffset: 19
});

testDefinition("Window {\n\tfunction test(a) {}\n\tid: wind\n\ts: {\n\t\twind.te|st(3)\n\t}\n}", {
	origin: "main.qml",
	start: { line: 1, ch: 10 },
	end: { line: 1, ch: 14 },
	file: "main.qml",
	contextOffset: 19
});

testDefinition("Window {\n\tfunction test(a) {}\n\tte|st: 3;\n}", {
	origin: undefined,
	start: undefined,
	end: undefined,
	file: undefined,
	contextOffset: undefined
});

testDefinition('Window {\n\tfunction test(a) {\n\t\ta|\n\t}\n}\n\tproperty int a', {
	origin: "main.qml",
	start: { line: 1, ch: 15 },
	end: { line: 1, ch: 16 },
	file: "main.qml",
	contextOffset: 24
});

groupEnd();