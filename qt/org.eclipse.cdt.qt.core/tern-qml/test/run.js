var driver = require("./driver.js");

function group(name) {
	if (typeof console === "object" && console.group) {
		console.group(name);
	}
}

function groupEnd() {
	if (typeof console === "object" && console.groupEnd) {
		console.groupEnd(name);
	}
}

function log(title, message) {
	if (typeof console === "object") console.log(title, message);
}

var stats, tests = [];
tests.push(require("./test-completions.js"));
tests.push(require("./test-finddef.js"));

function report(state, code, message) {
	if (state != "ok") {++stats.failed; log(code, message);}
	++stats.testsRun;
}

group("Errors");

for (var i = 0; i < tests.length; i++) {
	var test = tests[i];
	group(test.group);
	stats = test.stats = {testsRun: 0, failed: 0};
	var config = test.config || {};
	config.group = test.group;
	var t0 = +new Date;
	driver.runTests(config, report);
	test.stats.duration = +new Date - t0;
	groupEnd();
}

groupEnd();

function outputStats(name, stats) {
	log(name + ":", stats.testsRun + " tests run in " + stats.duration + "ms; " +
		(stats.failed ? stats.failed + " failures." : "all passed."));
}

var total = {testsRun: 0, failed: 0, duration: 0};

group("Stats");

for (var i = 0; i < tests.length; i++) {
	var test = tests[i];
	var stats = test.stats;
	outputStats(test.group, stats);
	for (var key in stats) total[key] += stats[key];
}

outputStats("Total", total);

groupEnd();

if (total.failed && typeof process === "object") {
	process.stdout.write("", function () {
		process.exit(1);
	});
}