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

var testFixture = {
	'QML': {
		'import QtQuick 2.2': {
			type: "QMLHeaderStatements",
			range: [0, 18],
			loc: {
				start: { line: 1, column: 0 },
				end: { line: 1, column: 18 }
			},
			statements: [
				{
					type: "QMLImportStatement",
					range: [0, 18],
					loc: {
						start: { line: 1, column: 0 },
						end: { line: 1, column: 18 }
					},
					module: {
						type: "QMLModuleIdentifier",
						qualifiedId: "QtQuick",
						range: [7, 18],
						loc: {
							start: { line: 1, column: 7 },
							end: { line: 1, column: 18 }
						},
						version: {
							type: "Literal",
							range: [15, 18],
							loc: {
								start: { line: 1, column: 15 },
								end: { line: 1, column: 18 }
							},
							value: 2.2,
							raw: "2.2"
						}
					}
				}
			]
		}
	}
};

if (typeof exports !== "undefined") {
	var test = require("./driver.js").test;
	var testFail = require("./driver.js").testFail;
	var tokTypes = require("../").tokTypes;
}

for (var ns in testFixture) {
	ns = testFixture[ns];
	for (var code in ns) {
		test(code, {
			type: 'Program',
			body: [ns[code]]
		}, {
			ecmaVersion: 6,
			locations: true,
			ranges: true
		});
	}
}