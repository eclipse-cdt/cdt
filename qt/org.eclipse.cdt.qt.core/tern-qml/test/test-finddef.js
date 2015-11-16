var testDefinition = require("./driver.js").testDefinition;
var groupStart = require("./driver.js").groupStart;
var groupEnd = require("./driver.js").groupEnd;

var group = exports.group = "Find Definition";
groupStart(group);

testDefinition("QtObject {\n\tid: obj\n\tproperty var prop: {\n\t\tob|j\n\t}\n}", {
	origin: "test1.qml",
	start: { line: 1, ch: 5 },
	end: { line: 1, ch: 8 },
	file: "test1.qml",
	contextOffset: 16
});

testDefinition("QtObject {\n\tproperty var prop\n\tpr|op: 3\n}", {
	origin: "test1.qml",
	start: { line: 1, ch: 14 },
	end: { line: 1, ch: 18 },
	file: "test1.qml",
	contextOffset: 25
});

groupEnd();