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

var test = require("./driver.js").test;
var testFail = require("./driver.js").testFail;
var tokTypes = require("../").tokTypes;

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
						type: "QMLModule",
						range: [7, 18],
						loc: {
							start: { line: 1, column: 7 },
							end: { line: 1, column: 18 }
						},
						qualifiedId: {
							type: "QMLQualifiedID",
							range: [7, 14],
							loc: {
								start: { line: 1, column: 7 },
								end: { line: 1, column: 14 }
							},
							parts: [ "QtQuick" ],
							raw: "QtQuick"
						},
						version: {
							type: "QMLVersionLiteral",
							range: [15, 18],
							loc: {
								start: { line: 1, column: 15 },
								end: { line: 1, column: 18 }
							},
							value: 2.2,
							major: 2,
							minor: 2,
							raw: "2.2"
						}
					}
				}
			]
		},

		'import "./file.js"': {
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
					directoryPath: {
						type:"Literal",
						range: [7, 18],
						loc: {
							start: { line: 1, column: 7 },
							end: { line: 1, column: 18 }
						},
						value: "./file.js",
						raw: "\"./file.js\""
					}
				}
			]
		},

		'import "./file.js" as MyModule': {
			type: "QMLHeaderStatements",
			range: [0, 30],
			loc: {
				start: { line: 1, column: 0 },
				end: { line: 1, column: 30 }
			},
			statements: [
				{
					type: "QMLImportStatement",
					range: [0, 30],
					loc: {
						start: { line: 1, column: 0 },
						end: { line: 1, column: 30 }
					},
					directoryPath: {
						type:"Literal",
						range: [7, 18],
						loc: {
							start: { line: 1, column: 7 },
							end: { line: 1, column: 18 }
						},
						value: "./file.js",
						raw: "\"./file.js\""
					},
					qualifier: {
						type: "QMLQualifier",
						range: [19, 30],
						loc: {
							start: { line: 1, column: 19 },
							end: { line: 1, column: 30 }
						},
						identifier: {
							type:"Identifier",
							range: [22, 30],
							loc: {
								start: { line: 1, column: 22 },
								end: { line: 1, column: 30 }
							},
							name: "MyModule"
						}
					}
				}
			]
		},

		'import QtQuick ver': "Unexpected token (1:15)",

		'import QtQuick 0x01': "QML module must specify major and minor version (1:15)",

		'import QtQuick 1': "QML module must specify major and minor version (1:15)",

		'import QtQuick 2.2\nimport "./file.js"': {
			type: "QMLHeaderStatements",
			range: [0, 37],
			loc: {
				start: { line: 1, column: 0 },
				end: { line: 2, column: 18 }
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
						type: "QMLModule",
						range: [7, 18],
						loc: {
							start: { line: 1, column: 7 },
							end: { line: 1, column: 18 }
						},
						qualifiedId: {
							type: "QMLQualifiedID",
							range: [7, 14],
							loc: {
								start: { line: 1, column: 7 },
								end: { line: 1, column: 14 }
							},
							parts: [ "QtQuick" ],
							raw: "QtQuick"
						},
						version: {
							type: "QMLVersionLiteral",
							range: [15, 18],
							loc: {
								start: { line: 1, column: 15 },
								end: { line: 1, column: 18 }
							},
							value: 2.2,
							major: 2,
							minor: 2,
							raw: "2.2"
						}
					}
				},
				{
					type: "QMLImportStatement",
					range: [19, 37],
					loc: {
						start: { line: 2, column: 0 },
						end: { line: 2, column: 18 }
					},
					directoryPath: {
						type:"Literal",
						range: [26, 37],
						loc: {
							start: { line: 2, column: 7 },
							end: { line: 2, column: 18 }
						},
						value: "./file.js",
						raw: "\"./file.js\""
					}
				}
			]
		},

		'import QtQuick 2.2;import "./file.js"': {
			type: "QMLHeaderStatements",
			range: [0, 37],
			loc: {
				start: { line: 1, column: 0 },
				end: { line: 1, column: 37 }
			},
			statements: [
				{
					type: "QMLImportStatement",
					range: [0, 19],
					loc: {
						start: { line: 1, column: 0 },
						end: { line: 1, column: 19 }
					},
					module: {
						type: "QMLModule",
						range: [7, 18],
						loc: {
							start: { line: 1, column: 7 },
							end: { line: 1, column: 18 }
						},
						qualifiedId: {
							type: "QMLQualifiedID",
							range: [7, 14],
							loc: {
								start: { line: 1, column: 7 },
								end: { line: 1, column: 14 }
							},
							parts: [ "QtQuick" ],
							raw: "QtQuick"
						},
						version: {
							type: "QMLVersionLiteral",
							range: [15, 18],
							loc: {
								start: { line: 1, column: 15 },
								end: { line: 1, column: 18 }
							},
							value: 2.2,
							major: 2,
							minor: 2,
							raw: "2.2"
						}
					}
				},
				{
					type: "QMLImportStatement",
					range: [19, 37],
					loc: {
						start: { line: 1, column: 19 },
						end: { line: 1, column: 37 }
					},
					directoryPath: {
						type:"Literal",
						range: [26, 37],
						loc: {
							start: { line: 1, column: 26 },
							end: { line: 1, column: 37 }
						},
						value: "./file.js",
						raw: "\"./file.js\""
					}
				}
			]
		},

		'import Module 1.0 as MyModule': {
			type: "QMLHeaderStatements",
			range: [0, 29],
			loc: {
				start: { line: 1, column: 0 },
				end: { line: 1, column: 29 }
			},
			statements: [
				{
					type: "QMLImportStatement",
					range: [0, 29],
					loc: {
						start: { line: 1, column: 0 },
						end: { line: 1, column: 29 }
					},
					module: {
						type: "QMLModule",
						range: [7, 17],
						loc: {
							start: { line: 1, column: 7 },
							end: { line: 1, column: 17 }
						},
						qualifiedId: {
							type: "QMLQualifiedID",
							range: [7, 13],
							loc: {
								start: { line: 1, column: 7 },
								end: { line: 1, column: 13 }
							},
							parts: [ "Module" ],
							raw: "Module"
						},
						version: {
							type: "QMLVersionLiteral",
							range: [14, 17],
							loc: {
								start: { line: 1, column: 14 },
								end: { line: 1, column: 17 }
							},
							value: 1,
							major: 1,
							minor: 0,
							raw: "1.0"
						}
					},
					qualifier: {
						type: "QMLQualifier",
						range: [18, 29],
						loc: {
							start: { line: 1, column: 18 },
							end: { line: 1, column: 29 }
						},
						identifier: {
							type:"Identifier",
							range: [21, 29],
							loc: {
								start: { line: 1, column: 21 },
								end: { line: 1, column: 29 }
							},
							name: "MyModule"
						}
					}
				}
			]
		},

		'import Qualified.Id.Test 1.0': {
			type: "QMLHeaderStatements",
			range: [0, 28],
			loc: {
				start: { line: 1, column: 0 },
				end: { line: 1, column: 28 }
			},
			statements: [
				{
					type: "QMLImportStatement",
					range: [0, 28],
					loc: {
						start: { line: 1, column: 0 },
						end: { line: 1, column: 28 }
					},
					module: {
						type: "QMLModule",
						range: [7, 28],
						loc: {
							start: { line: 1, column: 7 },
							end: { line: 1, column: 28 }
						},
						qualifiedId: {
							type: "QMLQualifiedID",
							range: [7, 24],
							loc: {
								start: { line: 1, column: 7 },
								end: { line: 1, column: 24 }
							},
							parts: [
								"Qualified",
								"Id",
								"Test"
							],
							raw: "Qualified.Id.Test"
						},
						version: {
							type: "QMLVersionLiteral",
							range: [25, 28],
							loc: {
								start: { line: 1, column: 25 },
								end: { line: 1, column: 28 }
							},
							value: 1,
							major: 1,
							minor: 0,
							raw: "1.0"
						}
					}
				}
			]
		},

		'pragma Singleton': {
			type: "QMLHeaderStatements",
			range: [0, 16],
			loc: {
				start: { line: 1, column: 0 },
				end: { line: 1, column: 16 }
			},
			statements: [
				{
					type: "QMLPragmaStatement",
					range: [0, 16],
					loc: {
						start: { line: 1, column: 0 },
						end: { line: 1, column: 16 }
					},
					identifier: {
						type: "Identifier",
						range: [7, 16],
						loc: {
							start: { line: 1, column: 7 },
							end: { line: 1, column: 16 }
						},
						name: "Singleton"
					}
				}
			]
		},

		'pragma Singleton\npragma Other': {
			type: "QMLHeaderStatements",
			range: [0, 29],
			loc: {
				start: { line: 1, column: 0 },
				end: { line: 2, column: 12 }
			},
			statements: [
				{
					type: "QMLPragmaStatement",
					range: [0, 16],
					loc: {
						start: { line: 1, column: 0 },
						end: { line: 1, column: 16 }
					},
					identifier: {
						type: "Identifier",
						range: [7, 16],
						loc: {
							start: { line: 1, column: 7 },
							end: { line: 1, column: 16 }
						},
						name: "Singleton"
					}
				},
				{
					type: "QMLPragmaStatement",
					range: [17, 29],
					loc: {
						start: { line: 2, column: 0 },
						end: { line: 2, column: 12 }
					},
					identifier: {
						type: "Identifier",
						range: [24, 29],
						loc: {
							start: { line: 2, column: 7 },
							end: { line: 2, column: 12 }
						},
						name: "Other"
					}
				}
			]
		},

		'pragma Singleton;pragma Other': {
			type: "QMLHeaderStatements",
			range: [0, 29],
			loc: {
				start: { line: 1, column: 0 },
				end: { line: 1, column: 29 }
			},
			statements: [
				{
					type: "QMLPragmaStatement",
					range: [0, 17],
					loc: {
						start: { line: 1, column: 0 },
						end: { line: 1, column: 17 }
					},
					identifier: {
						type: "Identifier",
						range: [7, 16],
						loc: {
							start: { line: 1, column: 7 },
							end: { line: 1, column: 16 }
						},
						name: "Singleton"
					}
				},
				{
					type: "QMLPragmaStatement",
					range: [17, 29],
					loc: {
						start: { line: 1, column: 17 },
						end: { line: 1, column: 29 }
					},
					identifier: {
						type: "Identifier",
						range: [24, 29],
						loc: {
							start: { line: 1, column: 24 },
							end: { line: 1, column: 29 }
						},
						name: "Other"
					}
				}
			]
		}
	}
};

for (var ns in testFixture) {
	ns = testFixture[ns];
	for (var code in ns) {
		if (typeof ns[code] === "string") {
			// Expected test result holds an error message
			testFail(code, ns[code], { ecmaVersion: 6 });
		} else {
			// Expected test result holds an AST
			test(code, {
				type: "Program",
				body: [ns[code]]
			}, {
				ecmaVersion: 6,
				locations: true,
				ranges: true,
			});
		}
	}
}