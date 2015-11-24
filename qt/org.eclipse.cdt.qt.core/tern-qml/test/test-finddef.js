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
var testDefinition = driver.testDefinition;
var groupStart = driver.groupStart;
var groupEnd = driver.groupEnd;

var group = exports.group = "Find Definition";
groupStart(group);

// ID Property
testDefinition("QtObject {\n\tid: obj\n\tproperty var prop: {\n\t\tob|j\n\t}\n}", {
	origin: "test1.qml",
	start: { line: 1, ch: 5 },
	end: { line: 1, ch: 8 },
	file: "test1.qml",
	contextOffset: 16
});

testDefinition("Window {\n\tButton {\n\t\tid: btn\n\t\tproperty int height\n\t}\n\ttest: bt|n\n}", {
	origin: "test1.qml",
	start: { line: 2, ch: 6 },
	end: { line: 2, ch: 9 },
	file: "test1.qml",
	contextOffset: 25
});

testDefinition("Window {\n\tproperty var btn\n\tButton {\n\t\tid: btn\n\t\tproperty int height\n\t}\n\ttest: bt|n\n}", {
	origin: "test1.qml",
	start: { line: 3, ch: 6 },
	end: { line: 3, ch: 9 },
	file: "test1.qml",
	contextOffset: 43
});

testDefinition("Window {\n\tproperty var btn\n\tButton {\n\t\tid: bt|n\n\t\tproperty int height\n\t}\n}", {
	origin: "test1.qml",
	start: { line: 3, ch: 6 },
	end: { line: 3, ch: 9 },
	file: "test1.qml",
	contextOffset: 43
});

// Property Declarations
testDefinition("QtObject {\n\tproperty var prop\n\tpr|op: 3\n}", {
	origin: "test1.qml",
	start: { line: 1, ch: 14 },
	end: { line: 1, ch: 18 },
	file: "test1.qml",
	contextOffset: 25
});

testDefinition("Window {\n\tproperty var btn\n\tButton {\n\t\tprop: b|tn\n\t}\n}", {
	origin: "test1.qml",
	start: { line: 1, ch: 14 },
	end: { line: 1, ch: 17 },
	file: "test1.qml",
	contextOffset: 23
});

testDefinition("Window {\n\tproperty var btn\n\tButton {\n\t\tButton {\n\t\t\tprop: b|tn\n\t\t}\n\t}\n}", {
	origin: "test1.qml",
	start: { line: 1, ch: 14 },
	end: { line: 1, ch: 17 },
	file: "test1.qml",
	contextOffset: 23
});

testDefinition("Window {\n\tButton {\n\t\tproperty var btn\n\t\tbt|n: 3\n\t}\n}", {
	origin: "test1.qml",
	start: { line: 2, ch: 15 },
	end: { line: 2, ch: 18 },
	file: "test1.qml",
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
	origin: "test1.qml",
	start: { line: 1, ch: 8 },
	end: { line: 1, ch: 15 },
	file: "test1.qml",
	contextOffset: 17
});

testDefinition("Window {\n\tsignal clicked(int mouseX, int mouseY)\n\tonCli|cked: 3\n}", {
	origin: "test1.qml",
	start: { line: 1, ch: 8 },
	end: { line: 1, ch: 15 },
	file: "test1.qml",
	contextOffset: 17
});

testDefinition("Window {\n\tsignal clicked(int mouseX, int mouseY)\n\tonClicked: mou|seX\n}", {
	origin: "test1.qml",
	start: { line: 1, ch: 20 },
	end: { line: 1, ch: 26 },
	file: "test1.qml",
	contextOffset: 29
});

testDefinition("Window {\n\tsignal clicked(int mouseX, int mouseY)\n\tonClicked: cli|cked(3,4)\n}", {
	origin: "test1.qml",
	start: { line: 1, ch: 8 },
	end: { line: 1, ch: 15 },
	file: "test1.qml",
	contextOffset: 17
});

testDefinition("Window {\n\tsignal clicked(int mouseX, int mouseY)\n\tonClicked: onCli|cked\n}", {
	origin: "test1.qml",
	start: { line: 1, ch: 8 },
	end: { line: 1, ch: 15 },
	file: "test1.qml",
	contextOffset: 17
});

testDefinition("Window {\n\tsignal clicked(int mouseX, int mouseY)\n\tid: wind\n\tonClicked: wind.onCli|cked\n}", {
	origin: "test1.qml",
	start: { line: 1, ch: 8 },
	end: { line: 1, ch: 15 },
	file: "test1.qml",
	contextOffset: 17
});

testDefinition("Window {\n\tsignal clicked(int mouseX, int mouseY)\n\tid: wind\n\tonClicked: wind.cli|cked(3,4)\n}", {
	origin: "test1.qml",
	start: { line: 1, ch: 8 },
	end: { line: 1, ch: 15 },
	file: "test1.qml",
	contextOffset: 17
});

testDefinition("Window {\n\tproperty int msg\n\tsignal error(string msg, boolean flag)\n\tonError: {\n\t\tms|g\n\t}\n}", {
	origin: "test1.qml",
	start: { line: 2, ch: 21 },
	end: { line: 2, ch: 24 },
	file: "test1.qml",
	contextOffset: 48
}, function (server) { server.jsDefs = []; });

// Function Declarations
testDefinition("Window {\n\tfunction te|st(a) {}\n}", {
	origin: "test1.qml",
	start: { line: 1, ch: 10 },
	end: { line: 1, ch: 14 },
	file: "test1.qml",
	contextOffset: 19
});

testDefinition("Window {\n\tfunction test(a) {\n\t\ta|\n\t}\n}", {
	origin: "test1.qml",
	start: { line: 1, ch: 15 },
	end: { line: 1, ch: 16 },
	file: "test1.qml",
	contextOffset: 24
});

testDefinition("Window {\n\tfunction test(a) {\n\t\tte|st(3)\n\t}\n}", {
	origin: "test1.qml",
	start: { line: 1, ch: 10 },
	end: { line: 1, ch: 14 },
	file: "test1.qml",
	contextOffset: 19
});

testDefinition("Window {\n\tfunction test(a) {}\n\ts: te|st(3)\n}", {
	origin: "test1.qml",
	start: { line: 1, ch: 10 },
	end: { line: 1, ch: 14 },
	file: "test1.qml",
	contextOffset: 19
});

testDefinition("Window {\n\tfunction test(a) {}\n\ts: {\n\t\tte|st(3)\n\t}\n}", {
	origin: "test1.qml",
	start: { line: 1, ch: 10 },
	end: { line: 1, ch: 14 },
	file: "test1.qml",
	contextOffset: 19
});

testDefinition("Window {\n\tfunction test(a) {}\n\tid: wind\n\ts: {\n\t\twind.te|st(3)\n\t}\n}", {
	origin: "test1.qml",
	start: { line: 1, ch: 10 },
	end: { line: 1, ch: 14 },
	file: "test1.qml",
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
	origin: "test1.qml",
	start: { line: 1, ch: 15 },
	end: { line: 1, ch: 16 },
	file: "test1.qml",
	contextOffset: 24
});

groupEnd();