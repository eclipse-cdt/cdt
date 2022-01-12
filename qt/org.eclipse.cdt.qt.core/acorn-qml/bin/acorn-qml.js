#!/usr/bin/env node
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

var path = require("path");
var fs = require("fs");
var acorn = require("..");

var infile, parsed, tokens, options = {}, silent = false, compact = false, tokenize = false;

function help(status) {
	var print = (status == 0) ? console.log : console.error;
	print("usage: " + path.basename(process.argv[1]) + "[--ecma3|--ecma5|--ecma6]");
	print("        [--tokenize] [--locations] [---allow-hash-bang] [--compact] [--silent] [--module] [--help] [--] infile");
	process.exit(status);
}

for (var i = 2; i < process.argv.length; ++i) {
	var arg = process.argv[i];
	if (arg[0] != "-" && !infile) infile = arg;
	else if (arg == "--" && !infile && i + 2 == process.argv.length)
		infile = process.argv[++i];
	else if (arg == "--ecma3")
		options.ecmaVersion = 3;
	else if (arg == "--ecma5")
		options.ecmaVersion = 5;
	else if (arg == "--ecma6")
		options.ecmaVersion = 6;
	else if (arg == "--ecma7")
		options.ecmaVersion = 7;
	else if (arg == "--locations")
		options.locations = true;
	else if (arg == "--allow-hash-bang")
		options.allowHashBang = true;
	else if (arg == "--silent")
		silent = true;
	else if (arg == "--compact")
		compact = true;
	else if (arg == "--help")
		help(0);
	else if (arg == "--tokenize")
		tokenize = true;
	else if (arg == "--module")
		options.sourceType = 'module';
	else
		help(1);
}

// Enable qml parser
options.plugins = { qml: true }

try {
	var code = fs.readFileSync(infile, "utf8");

	if (!tokenize)
		parsed = acorn.parse(code, options);
	else {
		var get = acorn.tokenize(code, options);
		tokens = [];
		while (true) {
			var token = get();
			tokens.push(token);
			if (token.type.type == "eof")
				break;
		}
	}
} catch(e) {
	console.log(e.message);
	process.exit(1);
}

if (!silent)
	console.log(JSON.stringify(tokenize ? tokens : parsed, null, compact ? null : 2));
