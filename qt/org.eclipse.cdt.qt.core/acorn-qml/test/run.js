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

// Get the driver and test code
var driver = require("./driver.js");
require("./tests-qml.js");

// Get and inject the QML plugin into Acorn
var acorn = require("acorn");
require("acorn/dist/acorn_loose");
require("..");
require("../loose");

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

var stats, modes = {
	"Normal QML": {
		config: {
			parse: acorn.parse,
			filter: function (test) {
				var opts = test.options || {};
				return opts.normal !== false && opts.qmltypes !== true;
			}
		}
	},
	"Loose QML": {
		config: {
			parse: acorn.parse_dammit,
			filter: function (test) {
				var opts = test.options || {};
				return opts.loose !== false && opts.qmltypes !== true;
			}
		}
	},
	"Loose QMLTypes": {
		config: {
			parse: acorn.parse_dammit,
			options: {
				mode: "qmltypes"
			},
			filter: function (test) {
				var opts = test.options || {};
				return opts.loose !== false && opts.qmltypes !== false;
			}
		}
	}
};

function report(state, code, message) {
	if (state != "ok") {++stats.failed; log(code, message);}
	++stats.testsRun;
}

group("Errors");

for (var name in modes) {
	group(name);
	var mode = modes[name];
	stats = mode.stats = {testsRun: 0, failed: 0};
	var t0 = +new Date();
	driver.runTests(mode.config, report);
	mode.stats.duration = +new Date() - t0;
	groupEnd();
}

groupEnd();

function outputStats(name, stats) {
	log(name + ":", stats.testsRun + " tests run in " + stats.duration + "ms; " +
		(stats.failed ? stats.failed + " failures." : "all passed."));
}

var total = {testsRun: 0, failed: 0, duration: 0};

group("Stats");

for (var name in modes) {
	var stats = modes[name].stats;
	outputStats(name + " parser", stats);
	for (var key in stats) total[key] += stats[key];
}

outputStats("Total", total);

groupEnd();

if (total.failed && typeof process === "object") {
	process.stdout.write("", function () {
		process.exit(1);
	});
}