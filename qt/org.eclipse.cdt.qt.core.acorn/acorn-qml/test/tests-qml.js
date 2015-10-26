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
	// ------------------- QML Header Statements ------------------
	'qml-header-statements': {

		// -------------------------- Import --------------------------
		'import QtQuick 2.2': [
			{
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
			}
		],

		'import "./file.js"': [
			{
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
			}
		],

		'import "./file.js" as MyModule': [
			{
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
			}
		],

		'import QtQuick ver': "Unexpected token (1:15)",

		'import QtQuick 0x01': "QML module must specify major and minor version (1:15)",

		'import QtQuick 1': "QML module must specify major and minor version (1:15)",

		'import QtQuick 2.2\nimport "./file.js"': [
			{
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
			}
		],

		'import QtQuick 2.2;import "./file.js"': [
			{
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
			}
		],

		'import Module 1.0 as MyModule': [
			{
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
			}
		],

		'import Qualified.Id.Test 1.0': [
			{
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
			}
		],

		// -------------------------- Pragma --------------------------
		'pragma Singleton': [
			{
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
			}
		],

		'pragma Singleton\npragma Other': [
			{
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
			}
		],

		'pragma Singleton;pragma Other': [
			{
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
		],
	},

	// ---------------------- Object Literals ---------------------
	'qml-object-literal': {
		'Window {}': [
			{
				type: "QMLObjectLiteral",
				range: [0, 9],
				loc: {
					start: { line: 1, column: 0 },
					end: { line: 1, column: 9 }
				},
				qualifiedId: {
					type: "QMLQualifiedID",
					range: [0, 6],
					loc: {
						start: { line: 1, column: 0 },
						end: { line: 1, column: 6 }
					},
					parts: [ "Window" ],
					raw: "Window"
				},
				block: {
					type: "QMLMemberBlock",
					range: [7, 9],
					loc: {
						start: { line: 1, column: 7 },
						end: { line: 1, column: 9 }
					},
					members: []
				}
			}
		],

		'QtQuick.Window {}': [
			{
				type: "QMLObjectLiteral",
				range: [0, 17],
				loc: {
					start: { line: 1, column: 0 },
					end: { line: 1, column: 17 }
				},
				qualifiedId: {
					type: "QMLQualifiedID",
					range: [0, 14],
					loc: {
						start: { line: 1, column: 0 },
						end: { line: 1, column: 14 }
					},
					parts: [ "QtQuick", "Window" ],
					raw: "QtQuick.Window"
				},
				block: {
					type: "QMLMemberBlock",
					range: [15, 17],
					loc: {
						start: { line: 1, column: 15 },
						end: { line: 1, column: 17 }
					},
					members: []
				}
			}
		],

		'Window {Button {}}': [
			{
				type: "QMLObjectLiteral",
				range: [0, 18],
				loc: {
					start: { line: 1, column: 0 },
					end: { line: 1, column: 18 }
				},
				qualifiedId: {
					type: "QMLQualifiedID",
					range: [0, 6],
					loc: {
						start: { line: 1, column: 0 },
						end: { line: 1, column: 6 }
					},
					parts: [ "Window" ],
					raw: "Window"
				},
				block: {
					type: "QMLMemberBlock",
					range: [7, 18],
					loc: {
						start: { line: 1, column: 7 },
						end: { line: 1, column: 18 }
					},
					members: [
						{
							type: "QMLObjectLiteral",
							range: [8, 17],
							loc: {
								start: { line: 1, column: 8 },
								end: { line: 1, column: 17 }
							},
							qualifiedId: {
								type: "QMLQualifiedID",
								range: [8, 14],
								loc: {
									start: { line: 1, column: 8 },
									end: { line: 1, column: 14 }
								},
								parts: [ "Button" ],
								raw: "Button"
							},
							block: {
								type: "QMLMemberBlock",
								range: [15, 17],
								loc: {
									start: { line: 1, column: 15 },
									end: { line: 1, column: 17 }
								},
								members: []
							}
						}
					]
				}
			}
		],

		'import QtQuick 2.3;\nWindow {}': [
			{
				type: "QMLHeaderStatements",
				range: [0, 19],
				loc: {
					start: { line: 1, column: 0 },
					end: { line: 1, column: 19 }
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
								value: 2.3,
								major: 2,
								minor: 3,
								raw: "2.3"
							}
						}
					}
				]
			},
			{
				type: "QMLObjectLiteral",
				range: [20, 29],
				loc: {
					start: { line: 2, column: 0 },
					end: { line: 2, column: 9 }
				},
				qualifiedId: {
					type: "QMLQualifiedID",
					range: [20, 26],
					loc: {
						start: { line: 2, column: 0 },
						end: { line: 2, column: 6 }
					},
					parts: [ "Window" ],
					raw: "Window"
				},
				block: {
					type: "QMLMemberBlock",
					range: [27, 29],
					loc: {
						start: { line: 2, column: 7 },
						end: { line: 2, column: 9 }
					},
					members: []
				}
			}
		],
	},

	// ------------------ QML Root Object Members -----------------
	'qml-root-obj-members': {

		// ------------------- Property Declarations ------------------
		'a{ property var w: 3 }': [
			{
				type: "QMLPropertyDeclaration",
				range: [3, 20],
				loc: {
					start: { line: 1, column: 3 },
					end: { line: 1, column: 20 }
				},
				default: false,
				readonly: false,
				typeInfo: {
					type: "QMLType",
					range: [12, 15],
					loc: {
						start: { line: 1, column: 12 },
						end: { line: 1, column: 15 }
					},
					isPrimitive: true,
					primitive: "var"
				},
				identifier: {
					type: "Identifier",
					range: [16, 17],
					loc: {
						start: { line: 1, column: 16 },
						end: { line: 1, column: 17 }
					},
					name: "w"
				},
				binding: {
					type: "QMLBinding",
					range: [17, 20],
					loc: {
						start: { line: 1, column: 17 },
						end: { line: 1, column: 20 }
					},
					expr: {
						type: "Literal",
						range: [19, 20],
						loc: {
							start: { line: 1, column: 19 },
							end: { line: 1, column: 20 }
						},
						value: 3,
						raw: "3"
					}
				}
			}
		],

		'a{ property boolean w: 3 }': [
			{
				type: "QMLPropertyDeclaration",
				range: [3, 24],
				loc: {
					start: { line: 1, column: 3 },
					end: { line: 1, column: 24 }
				},
				default: false,
				readonly: false,
				typeInfo: {
					type: "QMLType",
					range: [12, 19],
					loc: {
						start: { line: 1, column: 12 },
						end: { line: 1, column: 19 }
					},
					isPrimitive: true,
					primitive: "boolean"
				},
				identifier: {
					type: "Identifier",
					range: [20, 21],
					loc: {
						start: { line: 1, column: 20 },
						end: { line: 1, column: 21 }
					},
					name: "w"
				},
				binding: {
					type: "QMLBinding",
					range: [21, 24],
					loc: {
						start: { line: 1, column: 21 },
						end: { line: 1, column: 24 }
					},
					expr: {
						type: "Literal",
						range: [23, 24],
						loc: {
							start: { line: 1, column: 23 },
							end: { line: 1, column: 24 }
						},
						value: 3,
						raw: "3"
					}
				}
			}
		],

		'a{ property double w: 3 }': [
			{
				type: "QMLPropertyDeclaration",
				range: [3, 23],
				loc: {
					start: { line: 1, column: 3 },
					end: { line: 1, column: 23 }
				},
				default: false,
				readonly: false,
				typeInfo: {
					type: "QMLType",
					range: [12, 18],
					loc: {
						start: { line: 1, column: 12 },
						end: { line: 1, column: 18 }
					},
					isPrimitive: true,
					primitive: "double"
				},
				identifier: {
					type: "Identifier",
					range: [19, 20],
					loc: {
						start: { line: 1, column: 19 },
						end: { line: 1, column: 20 }
					},
					name: "w"
				},
				binding: {
					type: "QMLBinding",
					range: [20, 23],
					loc: {
						start: { line: 1, column: 20 },
						end: { line: 1, column: 23 }
					},
					expr: {
						type: "Literal",
						range: [22, 23],
						loc: {
							start: { line: 1, column: 22 },
							end: { line: 1, column: 23 }
						},
						value: 3,
						raw: "3"
					}
				}
			}
		],

		'a{ property int w: 3 }': [
			{
				type: "QMLPropertyDeclaration",
				range: [3, 20],
				loc: {
					start: { line: 1, column: 3 },
					end: { line: 1, column: 20 }
				},
				default: false,
				readonly: false,
				typeInfo: {
					type: "QMLType",
					range: [12, 15],
					loc: {
						start: { line: 1, column: 12 },
						end: { line: 1, column: 15 }
					},
					isPrimitive: true,
					primitive: "int"
				},
				identifier: {
					type: "Identifier",
					range: [16, 17],
					loc: {
						start: { line: 1, column: 16 },
						end: { line: 1, column: 17 }
					},
					name: "w"
				},
				binding: {
					type: "QMLBinding",
					range: [17, 20],
					loc: {
						start: { line: 1, column: 17 },
						end: { line: 1, column: 20 }
					},
					expr: {
						type: "Literal",
						range: [19, 20],
						loc: {
							start: { line: 1, column: 19 },
							end: { line: 1, column: 20 }
						},
						value: 3,
						raw: "3"
					}
				}
			}
		],

		'a{ property list w: 3 }': [
			{
				type: "QMLPropertyDeclaration",
				range: [3, 21],
				loc: {
					start: { line: 1, column: 3 },
					end: { line: 1, column: 21 }
				},
				default: false,
				readonly: false,
				typeInfo: {
					type: "QMLType",
					range: [12, 16],
					loc: {
						start: { line: 1, column: 12 },
						end: { line: 1, column: 16 }
					},
					isPrimitive: true,
					primitive: "list"
				},
				identifier: {
					type: "Identifier",
					range: [17, 18],
					loc: {
						start: { line: 1, column: 17 },
						end: { line: 1, column: 18 }
					},
					name: "w"
				},
				binding: {
					type: "QMLBinding",
					range: [18, 21],
					loc: {
						start: { line: 1, column: 18 },
						end: { line: 1, column: 21 }
					},
					expr: {
						type: "Literal",
						range: [20, 21],
						loc: {
							start: { line: 1, column: 20 },
							end: { line: 1, column: 21 }
						},
						value: 3,
						raw: "3"
					}
				}
			}
		],

		'a{ property color w: 3 }': [
			{
				type: "QMLPropertyDeclaration",
				range: [3, 22],
				loc: {
					start: { line: 1, column: 3 },
					end: { line: 1, column: 22 }
				},
				default: false,
				readonly: false,
				typeInfo: {
					type: "QMLType",
					range: [12, 17],
					loc: {
						start: { line: 1, column: 12 },
						end: { line: 1, column: 17 }
					},
					isPrimitive: true,
					primitive: "color"
				},
				identifier: {
					type: "Identifier",
					range: [18, 19],
					loc: {
						start: { line: 1, column: 18 },
						end: { line: 1, column: 19 }
					},
					name: "w"
				},
				binding: {
					type: "QMLBinding",
					range: [19, 22],
					loc: {
						start: { line: 1, column: 19 },
						end: { line: 1, column: 22 }
					},
					expr: {
						type: "Literal",
						range: [21, 22],
						loc: {
							start: { line: 1, column: 21 },
							end: { line: 1, column: 22 }
						},
						value: 3,
						raw: "3"
					}
				}
			}
		],

		'a{ property real w: 3 }': [
			{
				type: "QMLPropertyDeclaration",
				range: [3, 21],
				loc: {
					start: { line: 1, column: 3 },
					end: { line: 1, column: 21 }
				},
				default: false,
				readonly: false,
				typeInfo: {
					type: "QMLType",
					range: [12, 16],
					loc: {
						start: { line: 1, column: 12 },
						end: { line: 1, column: 16 }
					},
					isPrimitive: true,
					primitive: "real"
				},
				identifier: {
					type: "Identifier",
					range: [17, 18],
					loc: {
						start: { line: 1, column: 17 },
						end: { line: 1, column: 18 }
					},
					name: "w"
				},
				binding: {
					type: "QMLBinding",
					range: [18, 21],
					loc: {
						start: { line: 1, column: 18 },
						end: { line: 1, column: 21 }
					},
					expr: {
						type: "Literal",
						range: [20, 21],
						loc: {
							start: { line: 1, column: 20 },
							end: { line: 1, column: 21 }
						},
						value: 3,
						raw: "3"
					}
				}
			}
		],

		'a{ property string w: 3 }': [
			{
				type: "QMLPropertyDeclaration",
				range: [3, 23],
				loc: {
					start: { line: 1, column: 3 },
					end: { line: 1, column: 23 }
				},
				default: false,
				readonly: false,
				typeInfo: {
					type: "QMLType",
					range: [12, 18],
					loc: {
						start: { line: 1, column: 12 },
						end: { line: 1, column: 18 }
					},
					isPrimitive: true,
					primitive: "string"
				},
				identifier: {
					type: "Identifier",
					range: [19, 20],
					loc: {
						start: { line: 1, column: 19 },
						end: { line: 1, column: 20 }
					},
					name: "w"
				},
				binding: {
					type: "QMLBinding",
					range: [20, 23],
					loc: {
						start: { line: 1, column: 20 },
						end: { line: 1, column: 23 }
					},
					expr: {
						type: "Literal",
						range: [22, 23],
						loc: {
							start: { line: 1, column: 22 },
							end: { line: 1, column: 23 }
						},
						value: 3,
						raw: "3"
					}
				}
			}
		],

		'a{ property url w: 3 }': [
			{
				type: "QMLPropertyDeclaration",
				range: [3, 20],
				loc: {
					start: { line: 1, column: 3 },
					end: { line: 1, column: 20 }
				},
				default: false,
				readonly: false,
				typeInfo: {
					type: "QMLType",
					range: [12, 15],
					loc: {
						start: { line: 1, column: 12 },
						end: { line: 1, column: 15 }
					},
					isPrimitive: true,
					primitive: "url"
				},
				identifier: {
					type: "Identifier",
					range: [16, 17],
					loc: {
						start: { line: 1, column: 16 },
						end: { line: 1, column: 17 }
					},
					name: "w"
				},
				binding: {
					type: "QMLBinding",
					range: [17, 20],
					loc: {
						start: { line: 1, column: 17 },
						end: { line: 1, column: 20 }
					},
					expr: {
						type: "Literal",
						range: [19, 20],
						loc: {
							start: { line: 1, column: 19 },
							end: { line: 1, column: 20 }
						},
						value: 3,
						raw: "3"
					}
				}
			}
		],

		'a{ property QtObject w: 3 }': [
			{
				type: "QMLPropertyDeclaration",
				range: [3, 25],
				loc: {
					start: { line: 1, column: 3 },
					end: { line: 1, column: 25 }
				},
				default: false,
				readonly: false,
				typeInfo: {
					type: "QMLType",
					range: [12, 20],
					loc: {
						start: { line: 1, column: 12 },
						end: { line: 1, column: 20 }
					},
					isPrimitive: false,
					qualifiedId: {
						type: "QMLQualifiedID",
						range: [12, 20],
						loc: {
							start: { line: 1, column: 12 },
							end: { line: 1, column: 20 }
						},
						parts: [ "QtObject" ],
						raw: "QtObject"
					}
				},
				identifier: {
					type: "Identifier",
					range: [21, 22],
					loc: {
						start: { line: 1, column: 21 },
						end: { line: 1, column: 22 }
					},
					name: "w"
				},
				binding: {
					type: "QMLBinding",
					range: [22, 25],
					loc: {
						start: { line: 1, column: 22 },
						end: { line: 1, column: 25 }
					},
					expr: {
						type: "Literal",
						range: [24, 25],
						loc: {
							start: { line: 1, column: 24 },
							end: { line: 1, column: 25 }
						},
						value: 3,
						raw: "3"
					}
				}
			}
		],

		'a{ property alias c: color }': [
			{
				type: "QMLPropertyAlias",
				range: [3, 26],
				loc: {
					start: { line: 1, column: 3 },
					end: { line: 1, column: 26 }
				},
				default: false,
				readonly: false,
				identifier: {
					type: "Identifier",
					range: [18, 19],
					loc: {
						start: { line: 1, column: 18 },
						end: { line: 1, column: 19 }
					},
					name: "c"
				},
				binding: {
					type: "QMLBinding",
					range: [19, 26],
					loc: {
						start: { line: 1, column: 19 },
						end: { line: 1, column: 26 }
					},
					expr: {
						type: "Identifier",
						range: [21, 26],
						loc: {
							start: { line: 1, column: 21 },
							end: { line: 1, column: 26 }
						},
						name: "color"
					}
				}
			}
		],

		// TODO: Solve ambiguity to allow QML Object Literals in bindings
		'a{ property var b: Window {}}': "Unexpected token (1:26)",

		// --------------------- Property Bindings --------------------
		'a{ w: 3 }': [
			{
				type: "QMLProperty",
				range: [3, 7],
				loc: {
					start: { line: 1, column: 3 },
					end: { line: 1, column: 7 }
				},
				qualifiedId: {
					type: "QMLQualifiedID",
					range: [3, 4],
					loc: {
						start: { line: 1, column: 3 },
						end: { line: 1, column: 4 }
					},
					parts: [ "w" ],
					raw: "w"
				},
				binding: {
					type: "QMLBinding",
					range: [4, 7],
					loc: {
						start: { line: 1, column: 4 },
						end: { line: 1, column: 7 }
					},
					expr: {
						type: "Literal",
						range: [6, 7],
						loc: {
							start: { line: 1, column: 6 },
							end: { line: 1, column: 7 }
						},
						value: 3,
						raw: "3"
					}
				}
			}
		],

		'a{ x.y.z: 3 }': [
			{
				type: "QMLProperty",
				range: [3, 11],
				loc: {
					start: { line: 1, column: 3 },
					end: { line: 1, column: 11 }
				},
				qualifiedId: {
					type: "QMLQualifiedID",
					range: [3, 8],
					loc: {
						start: { line: 1, column: 3 },
						end: { line: 1, column: 8 }
					},
					parts: [ "x", "y", "z" ],
					raw: "x.y.z"
				},
				binding: {
					type: "QMLBinding",
					range: [8, 11],
					loc: {
						start: { line: 1, column: 8 },
						end: { line: 1, column: 11 }
					},
					expr: {
						type: "Literal",
						range: [10, 11],
						loc: {
							start: { line: 1, column: 10 },
							end: { line: 1, column: 11 }
						},
						value: 3,
						raw: "3"
					}
				}
			}
		],

		'a{ import: 3 }': "Unexpected token (1:3)",

		'a{ pragma: 3 }': "Unexpected token (1:3)",

		'a{ as: 3 }': "Unexpected token (1:3)",

		'a{ boolean: 3 }': "Unexpected token (1:3)",

		'a{ double: 3 }': "Unexpected token (1:3)",

		'a{ int: 3 }': "Unexpected token (1:3)",

		'a{ alias: 3 }': [
			{
				type: "QMLProperty",
				range: [3, 11],
				loc: {
					start: { line: 1, column: 3 },
					end: { line: 1, column: 11 }
				},
				qualifiedId: {
					type: "QMLQualifiedID",
					range: [3, 8],
					loc: {
						start: { line: 1, column: 3 },
						end: { line: 1, column: 8 }
					},
					parts: [ "alias" ],
					raw: "alias"
				},
				binding: {
					type: "QMLBinding",
					range: [8, 11],
					loc: {
						start: { line: 1, column: 8 },
						end: { line: 1, column: 11 }
					},
					expr: {
						type: "Literal",
						range: [10, 11],
						loc: {
							start: { line: 1, column: 10 },
							end: { line: 1, column: 11 }
						},
						value: 3,
						raw: "3"
					}
				}
			}
		],

		'a{ list: 3 }': [
			{
				type: "QMLProperty",
				range: [3, 10],
				loc: {
					start: { line: 1, column: 3 },
					end: { line: 1, column: 10 }
				},
				qualifiedId: {
					type: "QMLQualifiedID",
					range: [3, 7],
					loc: {
						start: { line: 1, column: 3 },
						end: { line: 1, column: 7 }
					},
					parts: [ "list" ],
					raw: "list"
				},
				binding: {
					type: "QMLBinding",
					range: [7, 10],
					loc: {
						start: { line: 1, column: 7 },
						end: { line: 1, column: 10 }
					},
					expr: {
						type: "Literal",
						range: [9, 10],
						loc: {
							start: { line: 1, column: 9 },
							end: { line: 1, column: 10 }
						},
						value: 3,
						raw: "3"
					}
				}
			}
		],

		// TODO: solve ambiguity to properly pass these 3 tests
		'a{ property: 3 }': "Unexpected token (1:11)",
		'a{ readonly: 3 }': "Unexpected token (1:11)",
		'a{ signal: 3 }': "Unexpected token (1:9)",

		'a{ color: 3 }': [
			{
				type: "QMLProperty",
				range: [3, 11],
				loc: {
					start: { line: 1, column: 3 },
					end: { line: 1, column: 11 }
				},
				qualifiedId: {
					type: "QMLQualifiedID",
					range: [3, 8],
					loc: {
						start: { line: 1, column: 3 },
						end: { line: 1, column: 8 }
					},
					parts: [ "color" ],
					raw: "color"
				},
				binding: {
					type: "QMLBinding",
					range: [8, 11],
					loc: {
						start: { line: 1, column: 8 },
						end: { line: 1, column: 11 }
					},
					expr: {
						type: "Literal",
						range: [10, 11],
						loc: {
							start: { line: 1, column: 10 },
							end: { line: 1, column: 11 }
						},
						value: 3,
						raw: "3"
					}
				}
			}
		],

		'a{ real: 3 }': [
			{
				type: "QMLProperty",
				range: [3, 10],
				loc: {
					start: { line: 1, column: 3 },
					end: { line: 1, column: 10 }
				},
				qualifiedId: {
					type: "QMLQualifiedID",
					range: [3, 7],
					loc: {
						start: { line: 1, column: 3 },
						end: { line: 1, column: 7 }
					},
					parts: [ "real" ],
					raw: "real"
				},
				binding: {
					type: "QMLBinding",
					range: [7, 10],
					loc: {
						start: { line: 1, column: 7 },
						end: { line: 1, column: 10 }
					},
					expr: {
						type: "Literal",
						range: [9, 10],
						loc: {
							start: { line: 1, column: 9 },
							end: { line: 1, column: 10 }
						},
						value: 3,
						raw: "3"
					}
				}
			}
		],

		'a{ string: 3 }': [
			{
				type: "QMLProperty",
				range: [3, 12],
				loc: {
					start: { line: 1, column: 3 },
					end: { line: 1, column: 12 }
				},
				qualifiedId: {
					type: "QMLQualifiedID",
					range: [3, 9],
					loc: {
						start: { line: 1, column: 3 },
						end: { line: 1, column: 9 }
					},
					parts: [ "string" ],
					raw: "string"
				},
				binding: {
					type: "QMLBinding",
					range: [9, 12],
					loc: {
						start: { line: 1, column: 9 },
						end: { line: 1, column: 12 }
					},
					expr: {
						type: "Literal",
						range: [11, 12],
						loc: {
							start: { line: 1, column: 11 },
							end: { line: 1, column: 12 }
						},
						value: 3,
						raw: "3"
					}
				}
			}
		],

		'a{ url: 3 }': [
			{
				type: "QMLProperty",
				range: [3, 9],
				loc: {
					start: { line: 1, column: 3 },
					end: { line: 1, column: 9 }
				},
				qualifiedId: {
					type: "QMLQualifiedID",
					range: [3, 6],
					loc: {
						start: { line: 1, column: 3 },
						end: { line: 1, column: 6 }
					},
					parts: [ "url" ],
					raw: "url"
				},
				binding: {
					type: "QMLBinding",
					range: [6, 9],
					loc: {
						start: { line: 1, column: 6 },
						end: { line: 1, column: 9 }
					},
					expr: {
						type: "Literal",
						range: [8, 9],
						loc: {
							start: { line: 1, column: 8 },
							end: { line: 1, column: 9 }
						},
						value: 3,
						raw: "3"
					}
				}
			}
		],

		'a{ onClicked: Qt.quit(0) }': [
			{
				type: "QMLProperty",
				range: [3, 24],
				loc: {
					start: { line: 1, column: 3 },
					end: { line: 1, column: 24 }
				},
				qualifiedId: {
					type: "QMLQualifiedID",
					range: [3, 12],
					loc: {
						start: { line: 1, column: 3 },
						end: { line: 1, column: 12 }
					},
					parts: [ "onClicked" ],
					raw: "onClicked"
				},
				binding: {
					type: "QMLBinding",
					range: [12, 24],
					loc: {
						start: { line: 1, column: 12 },
						end: { line: 1, column: 24 }
					},
					expr: {
						type: "CallExpression",
						range: [14, 24],
						loc: {
							start: { line: 1, column: 14 },
							end: { line: 1, column: 24 }
						},
						callee: {
							type: "MemberExpression",
							range: [14, 21],
							loc: {
								start: { line: 1, column: 14 },
								end: { line: 1, column: 21 }
							},
							object: {
								type: "Identifier",
								range: [14, 16],
								loc: {
									start: { line: 1, column: 14 },
									end: { line: 1, column: 16 }
								},
								name: "Qt"
							},
							property: {
								type: "Identifier",
								range: [17, 21],
								loc: {
									start: { line: 1, column: 17 },
									end: { line: 1, column: 21 }
								},
								name: "quit"
							},
							computed: false
						},
						arguments: [
							{
								type: "Literal",
								range: [22, 23],
								loc: {
									start: { line: 1, column: 22 },
									end: { line: 1, column: 23 }
								},
								value: 0,
								raw: "0"
							}
						]
					}
				}
			}
		],

		// ------------------- Contextual Keywords --------------------
		'a{b:pragma}': [
			{
				type: "QMLProperty",
				range: [2, 10],
				loc: {
					start: { line: 1, column: 2 },
					end: { line: 1, column: 10 }
				},
				qualifiedId: {
					type: "QMLQualifiedID",
					range: [2, 3],
					loc: {
						start: { line: 1, column: 2 },
						end: { line: 1, column: 3 }
					},
					parts: [ "b" ],
					raw: "b"
				},
				binding: {
					type: "QMLBinding",
					range: [3, 10],
					loc: {
						start: { line: 1, column: 3 },
						end: { line: 1, column: 10 }
					},
					expr: {
						type: "Identifier",
						range: [4, 10],
						loc: {
							start: { line: 1, column: 4 },
							end: { line: 1, column: 10 }
						},
						name: "pragma"
					}
				}
			}
		],

		'a{b:property}': [
			{
				type: "QMLProperty",
				range: [2, 12],
				loc: {
					start: { line: 1, column: 2 },
					end: { line: 1, column:  12 }
				},
				qualifiedId: {
					type: "QMLQualifiedID",
					range: [2, 3],
					loc: {
						start: { line: 1, column: 2 },
						end: { line: 1, column:  3 }
					},
					parts: [ "b" ],
					raw: "b"
				},
				binding: {
					type: "QMLBinding",
					range: [3, 12],
					loc: {
						start: { line: 1, column: 3 },
						end: { line: 1, column:  12 }
					},
					expr: {
						type: "Identifier",
						range: [4, 12],
						loc: {
							start: { line: 1, column: 4 },
							end: { line: 1, column: 12 }
						},
						name: "property"
					}
				}
			}
		],

		'a{b:readonly}': [
			{
				type: "QMLProperty",
				range: [2, 12],
				loc: {
					start: { line: 1, column: 2 },
					end: { line: 1, column: 12 }
				},
				qualifiedId: {
					type: "QMLQualifiedID",
					range: [2, 3],
					loc: {
						start: { line: 1, column: 2 },
						end: { line: 1, column: 3 }
					},
					parts: [ "b" ],
					raw: "b"
				},
				binding: {
					type: "QMLBinding",
					range: [3, 12],
					loc: {
						start: { line: 1, column: 3 },
						end: { line: 1, column: 12 }
					},
					expr: {
						type: "Identifier",
						range: [4, 12],
						loc: {
							start: { line: 1, column: 4 },
							end: { line: 1, column: 12 }
						},
						name: "readonly"
					}
				}
			}
		],

		'a{b:signal}': [
			{
				type: "QMLProperty",
				range: [2, 10],
				loc: {
					start: { line: 1, column: 2 },
					end: { line: 1, column: 10 }
				},
				qualifiedId: {
					type: "QMLQualifiedID",
					range: [2, 3],
					loc: {
						start: { line: 1, column: 2 },
						end: { line: 1, column: 3 }
					},
					parts: [ "b" ],
					raw: "b"
				},
				binding: {
					type: "QMLBinding",
					range: [3, 10],
					loc: {
						start: { line: 1, column: 3 },
						end: { line: 1, column: 10 }
					},
					expr: {
						type: "Identifier",
						range: [4, 10],
						loc: {
							start: { line: 1, column: 4 },
							end: { line: 1, column: 10 }
						},
						name: "signal"
					}
				}
			}
		],

		'a{b:alias}': [
			{
				type: "QMLProperty",
				range: [2, 9],
				loc: {
					start: { line: 1, column: 2 },
					end: { line: 1, column: 9 }
				},
				qualifiedId: {
					type: "QMLQualifiedID",
					range: [2, 3],
					loc: {
						start: { line: 1, column: 2 },
						end: { line: 1, column: 3 }
					},
					parts: [ "b" ],
					raw: "b"
				},
				binding: {
					type: "QMLBinding",
					range: [3, 9],
					loc: {
						start: { line: 1, column: 3 },
						end: { line: 1, column: 9 }
					},
					expr: {
						type: "Identifier",
						range: [4, 9],
						loc: {
							start: { line: 1, column: 4 },
							end: { line: 1, column: 9 }
						},
						name: "alias"
					}
				}
			}
		],

		'a{b:list}': [
			{
				type: "QMLProperty",
				range: [2, 8],
				loc: {
					start: { line: 1, column: 2 },
					end: { line: 1, column: 8 }
				},
				qualifiedId: {
					type: "QMLQualifiedID",
					range: [2, 3],
					loc: {
						start: { line: 1, column: 2 },
						end: { line: 1, column: 3 }
					},
					parts: [ "b" ],
					raw: "b"
				},
				binding: {
					type: "QMLBinding",
					range: [3, 8],
					loc: {
						start: { line: 1, column: 3 },
						end: { line: 1, column: 8 }
					},
					expr: {
						type: "Identifier",
						range: [4, 8],
						loc: {
							start: { line: 1, column: 4 },
							end: { line: 1, column: 8 }
						},
						name: "list"
					}
				}
			}
		],

		'a{b:color}': [
			{
				type: "QMLProperty",
				range: [2, 9],
				loc: {
					start: { line: 1, column: 2 },
					end: { line: 1, column: 9 }
				},
				qualifiedId: {
					type: "QMLQualifiedID",
					range: [2, 3],
					loc: {
						start: { line: 1, column: 2 },
						end: { line: 1, column: 3 }
					},
					parts: [ "b" ],
					raw: "b"
				},
				binding: {
					type: "QMLBinding",
					range: [3, 9],
					loc: {
						start: { line: 1, column: 3 },
						end: { line: 1, column: 9 }
					},
					expr: {
						type: "Identifier",
						range: [4, 9],
						loc: {
							start: { line: 1, column: 4 },
							end: { line: 1, column: 9 }
						},
						name: "color"
					}
				}
			}
		],

		'a{b:real}': [
			{
				type: "QMLProperty",
				range: [2, 8],
				loc: {
					start: { line: 1, column: 2 },
					end: { line: 1, column: 8 }
				},
				qualifiedId: {
					type: "QMLQualifiedID",
					range: [2, 3],
					loc: {
						start: { line: 1, column: 2 },
						end: { line: 1, column: 3 }
					},
					parts: [ "b" ],
					raw: "b"
				},
				binding: {
					type: "QMLBinding",
					range: [3, 8],
					loc: {
						start: { line: 1, column: 3 },
						end: { line: 1, column: 8 }
					},
					expr: {
						type: "Identifier",
						range: [4, 8],
						loc: {
							start: { line: 1, column: 4 },
							end: { line: 1, column: 8 }
						},
						name: "real"
					}
				}
			}
		],

		'a{b:string}': [
			{
				type: "QMLProperty",
				range: [2, 10],
				loc: {
					start: { line: 1, column: 2 },
					end: { line: 1, column: 10 }
				},
				qualifiedId: {
					type: "QMLQualifiedID",
					range: [2, 3],
					loc: {
						start: { line: 1, column: 2 },
						end: { line: 1, column: 3 }
					},
					parts: [ "b" ],
					raw: "b"
				},
				binding: {
					type: "QMLBinding",
					range: [3, 10],
					loc: {
						start: { line: 1, column: 3 },
						end: { line: 1, column: 10 }
					},
					expr: {
						type: "Identifier",
						range: [4, 10],
						loc: {
							start: { line: 1, column: 4 },
							end: { line: 1, column: 10 }
						},
						name: "string"
					}
				}
			}
		],

		'a{b:url}': [
			{
				type: "QMLProperty",
				range: [2, 7],
				loc: {
					start: { line: 1, column: 2 },
					end: { line: 1, column: 7 }
				},
				qualifiedId: {
					type: "QMLQualifiedID",
					range: [2, 3],
					loc: {
						start: { line: 1, column: 2 },
						end: { line: 1, column: 3 }
					},
					parts: [ "b" ],
					raw: "b"
				},
				binding: {
					type: "QMLBinding",
					range: [3, 7],
					loc: {
						start: { line: 1, column: 3 },
						end: { line: 1, column: 7 }
					},
					expr: {
						type: "Identifier",
						range: [4, 7],
						loc: {
							start: { line: 1, column: 4 },
							end: { line: 1, column: 7 }
						},
						name: "url"
					}
				}
			}
		],

		// -------------------- Signal Definitions --------------------
		'a{ signal b }': [
			{
				type: "QMLSignalDefinition",
				range: [3, 11],
				loc: {
					start: { line: 1, column: 3 },
					end: { line: 1, column: 11 }
				},
				identifier: {
					type: "Identifier",
					range: [10, 11],
					loc: {
						start: { line: 1, column: 10 },
						end: { line: 1, column: 11 }
					},
					name: "b"
				},
				parameters: []
			}
		],

		'a{ signal b () }': [
			{
				type: "QMLSignalDefinition",
				range: [3, 14],
				loc: {
					start: { line: 1, column: 3 },
					end: { line: 1, column: 14 }
				},
				identifier: {
					type: "Identifier",
					range: [10, 11],
					loc: {
						start: { line: 1, column: 10 },
						end: { line: 1, column: 11 }
					},
					name: "b"
				},
				parameters: []
			}
		],

		'a{ signal b (type param)}': [
			{
				type: "QMLSignalDefinition",
				range: [3, 24],
				loc: {
					start: { line: 1, column: 3 },
					end: { line: 1, column: 24 }
				},
				identifier: {
					type: "Identifier",
					range: [10, 11],
					loc: {
						start: { line: 1, column: 10 },
						end: { line: 1, column: 11 }
					},
					name: "b"
				},
				parameters: [
					{
						type: {
							type: "Identifier",
							range: [13, 17],
							loc: {
								start: { line: 1, column: 13 },
								end: { line: 1, column: 17 }
							},
							name: "type"
						},
						range: [ 13, 0 ],
						loc: {
							start: {
								line: 1,
								column: 13
							}
						},
						identifier: {
							type: "Identifier",
							range: [18, 23],
							loc: {
								start: { line: 1, column: 18 },
								end: { line: 1, column: 23 }
							},
							name: "param"
						}
					}
				]
			}
		],

		'a{ signal b (type1 param1, type2 param2, type3 param3)}': [
			{
				type: "QMLSignalDefinition",
				range: [3, 54],
				loc: {
					start: { line: 1, column: 3 },
					end: { line: 1, column: 54 }
				},
				identifier: {
					type: "Identifier",
					range: [10, 11],
					loc: {
						start: { line: 1, column: 10 },
						end: { line: 1, column: 11 }
					},
					name: "b"
				},
				parameters: [
					{
						type: {
							type: "Identifier",
							range: [13, 18],
							loc: {
								start: { line: 1, column: 13 },
								end: { line: 1, column: 18 }
							},
							name: "type1"
						},
						loc: {
							start: {
								line: 1,
								column: 13
							}
						},
						range: [ 13, 0 ],
						identifier: {
							type: "Identifier",
							range: [19, 25],
							loc: {
								start: { line: 1, column: 19 },
								end: { line: 1, column: 25 }
							},
							name: "param1"
						}
					},
					{
						type: {
							type: "Identifier",
							range: [27, 32],
							loc: {
								start: { line: 1, column: 27 },
								end: { line: 1, column: 32 }
							},
							name: "type2"
						},
						loc: {
							start: {
								line: 1,
								column: 27
							}
						},
						range: [
							27,
							0
						],
						identifier: {
							type: "Identifier",
							range: [33, 39],
							loc: {
								start: { line: 1, column: 33 },
								end: { line: 1, column: 39 }
							},
							name: "param2"
						}
					},
					{
						type: {
							type: "Identifier",
							range: [41, 46],
							loc: {
								start: { line: 1, column: 41 },
								end: { line: 1, column: 46 }
							},
							name: "type3"
						},
						loc: {
							start: {
								line: 1,
								column: 41
							}
						},
						range: [
							41,
							0
						],
						identifier: {
							type: "Identifier",
							range: [47, 53],
							loc: {
								start: { line: 1, column: 47 },
								end: { line: 1, column: 53 }
							},
							name: "param3"
						}
					}
				]
			}
		],
	},

	// ----------------------- QML Examples -----------------------
	'qml-examples': {

		// Hello World
		'import QtQuick 2.3\nimport QtQuick.Window 2.2\n\tWindow {\n\tvisible: true\n\n\tMouseArea {\n\t\tanchors.fill: parent\n\t\tonClicked: {\n\t\t\tQt.quit();\n\t\t}\n\t}\n\t\tText {\n\t\t\ttext: qsTr("Hello World")\n\t\t\tanchors.centerIn: parent\n\t}\n}':
		[
			{
				type: "QMLHeaderStatements",
				range: [0, 44],
				loc: {
					start: { line: 1, column: 0 },
					end: { line: 2, column: 25 }
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
								raw: "2.3",
								value: 2.3,
								major: 2,
								minor: 3
							}
						}
					},
					{
						type: "QMLImportStatement",
						range: [19, 44],
						loc: {
							start: { line: 2, column: 0 },
							end: { line: 2, column: 25 }
						},
						module: {
							type: "QMLModule",
							range: [26, 44],
							loc: {
								start: { line: 2, column: 7 },
								end: { line: 2, column: 25 }
							},
							qualifiedId: {
								type: "QMLQualifiedID",
								range: [26, 40],
								loc: {
									start: { line: 2, column: 7 },
									end: { line: 2, column: 21 }
								},
								parts: [ "QtQuick", "Window" ],
								raw: "QtQuick.Window"
							},
							version: {
								type: "QMLVersionLiteral",
								range: [41, 44],
								loc: {
									start: { line: 2, column: 22 },
									end: { line: 2, column: 25 }
								},
								raw: "2.2",
								value: 2.2,
								major: 2,
								minor: 2
							}
						}
					}
				]
			},
			{
				type: "QMLObjectLiteral",
				range: [46, 213],
				loc: {
					start: { line: 3, column: 1 },
					end: { line: 16, column: 1 }
				},
				qualifiedId: {
					type: "QMLQualifiedID",
					range: [46, 52],
					loc: {
						start: { line: 3, column: 1 },
						end: { line: 3, column: 7 }
					},
					parts: [ "Window" ],
					raw: "Window"
				},
				block: {
					type: "QMLMemberBlock",
					range: [53, 213],
					loc: {
						start: { line: 3, column: 8 },
						end: { line: 16, column: 1 }
					},
					members: [
						{
							type: "QMLProperty",
							range: [56, 69],
							loc: {
								start: { line: 4, column: 1 },
								end: { line: 4, column: 14 }
							},
							qualifiedId: {
								type: "QMLQualifiedID",
								range: [56, 63],
								loc: {
									start: { line: 4, column: 1 },
									end: { line: 4, column: 8 }
								},
								parts: [ "visible" ],
								raw: "visible"
							},
							binding: {
								type: "QMLBinding",
								range: [63, 69],
								loc: {
									start: { line: 4, column: 8 },
									end: { line: 4, column: 14 }
								},
								expr: {
									type: "Literal",
									range: [65, 69],
									loc: {
										start: { line: 4, column: 10 },
										end: { line: 4, column: 14 }
									},
									value: true,
									raw: "true"
								}
							}
						},
						{
							type: "QMLObjectLiteral",
							range: [72, 142],
							loc: {
								start: { line: 6, column: 1 },
								end: { line: 11, column: 2 }
							},
							qualifiedId: {
								type: "QMLQualifiedID",
								range: [72, 81],
								loc: {
									start: { line: 6, column: 1 },
									end: { line: 6, column: 10 }
								},
								parts: [ "MouseArea" ],
								raw: "MouseArea"
							},
							block: {
								type: "QMLMemberBlock",
								range: [82, 142],
								loc: {
									start: { line: 6, column: 11 },
									end: { line: 11, column: 2 }
								},
								members: [
									{
										type: "QMLProperty",
										range: [86, 106],
										loc: {
											start: { line: 7, column: 2 },
											end: { line: 7, column: 22 }
										},
										qualifiedId: {
											type: "QMLQualifiedID",
											range: [86, 98],
											loc: {
												start: { line: 7, column: 2 },
												end: { line: 7, column: 14 }
											},
											parts: [ "anchors", "fill" ],
											raw: "anchors.fill"
										},
										binding: {
											type: "QMLBinding",
											range: [98, 106],
											loc: {
												start: { line: 7, column: 14 },
												end: { line: 7, column: 22 }
											},
											expr: {
												type: "Identifier",
												range: [100, 106],
												loc: {
													start: { line: 7, column: 16 },
													end: { line: 7, column: 22 }
												},
												name: "parent"
											}
										}
									},
									{
										type: "QMLProperty",
										range: [109, 139],
										loc: {
											start: { line: 8, column: 2 },
											end: { line: 10, column: 3 }
										},
										qualifiedId: {
											type: "QMLQualifiedID",
											range: [109, 118],
											loc: {
												start: { line: 8, column: 2 },
												end: { line: 8, column: 11 }
											},
											parts: [ "onClicked" ],
											raw: "onClicked"
										},
										binding: {
											type: "QMLBinding",
											range: [118, 139],
											loc: {
												start: { line: 8, column: 11 },
												end: { line: 10, column: 3 }
											},
											block: {
												type: "QMLStatementBlock",
												range: [120, 139],
												loc: {
													start: { line: 8, column: 13 },
													end: { line: 10, column: 3 }
												},
												statements: [
													{
														type: "ExpressionStatement",
														range: [125, 135],
														loc: {
															start: { line: 9, column: 3 },
															end: { line: 9, column: 13 }
														},
														expression: {
															type: "CallExpression",
															range: [125, 134],
															loc: {
																start: { line: 9, column: 3 },
																end: { line: 9, column: 12 }
															},
															callee: {
																type: "MemberExpression",
																range: [125, 132],
																loc: {
																	start: { line: 9, column: 3 },
																	end: { line: 9, column: 10 }
																},
																object: {
																	type: "Identifier",
																	range: [125, 127],
																	loc: {
																		start: { line: 9, column: 3 },
																		end: { line: 9, column: 5 }
																	},
																	name: "Qt"
																},
																property: {
																	type: "Identifier",
																	range: [128, 132],
																	loc: {
																		start: { line: 9, column: 6 },
																		end: { line: 9, column: 10 }
																	},
																	name: "quit"
																},
																computed: false
															},
															arguments: []
														}
													}
												]
											}
										}
									}
								]
							}
						},
						{
							type: "QMLObjectLiteral",
							range: [145, 211],
							loc: {
								start: { line: 12, column: 2 },
								end: { line: 15, column: 2 }
							},
							qualifiedId: {
								type: "QMLQualifiedID",
								range: [145, 149],
								loc: {
									start: { line: 12, column: 2 },
									end: { line: 12, column: 6 }
								},
								parts: [ "Text" ],
								raw: "Text"
							},
							block: {
								type: "QMLMemberBlock",
								range: [150, 211],
								loc: {
									start: { line: 12, column: 7 },
									end: { line: 15, column: 2 }
								},
								members: [
									{
										type: "QMLProperty",
										range: [155, 180],
										loc: {
											start: { line: 13, column: 3 },
											end: { line: 13, column: 28 }
										},
										qualifiedId: {
											type: "QMLQualifiedID",
											range: [155, 159],
											loc: {
												start: { line: 13, column: 3 },
												end: { line: 13, column: 7 }
											},
											parts: [ "text" ],
											raw: "text"
										},
										binding: {
											type: "QMLBinding",
											range: [159, 180],
											loc: {
												start: { line: 13, column: 7 },
												end: { line: 13, column: 28 }
											},
											expr: {
												type: "CallExpression",
												range: [161, 180],
												loc: {
													start: { line: 13, column: 9 },
													end: { line: 13, column: 28 }
												},
												callee: {
													type: "Identifier",
													range: [161, 165],
													loc: {
														start: { line: 13, column: 9 },
														end: { line: 13, column: 13 }
													},
													name: "qsTr"
												},
												arguments: [
													{
														type: "Literal",
														range: [166, 179],
														loc: {
															start: { line: 13, column: 14 },
															end: { line: 13, column: 27 }
														},
														value: "Hello World",
														raw: "\"Hello World\""
													}
												]
											}
										}
									},
									{
										type: "QMLProperty",
										range: [184, 208],
										loc: {
											start: { line: 14, column: 3 },
											end: { line: 14, column: 27 }
										},
										qualifiedId: {
											type: "QMLQualifiedID",
											range: [184, 200],
											loc: {
												start: { line: 14, column: 3 },
												end: { line: 14, column: 19 }
											},
											parts: [ "anchors", "centerIn" ],
											raw: "anchors.centerIn"
										},
										binding: {
											type: "QMLBinding",
											range: [200, 208],
											loc: {
												start: { line: 14, column: 19 },
												end: { line: 14, column: 27 }
											},
											expr: {
												type: "Identifier",
												range: [202, 208],
												loc: {
													start: { line: 14, column: 21 },
													end: { line: 14, column: 27 }
												},
												name: "parent"
											}
										}
									}
								]
							}
						}
					]
				}
			}
		]
	}
};

/*
* Create the tests that will be run.  In order to avoid copying redundant
* code between tests, this for loop uses the heading to determine how it
* will build the expected AST.  For example, if the test fixture heading
* is 'qml-root-obj-members', the given code will be appended to the
* 'members' section of the Root Object's AST.  Test fixture headings that
* are not matched will have their code appended to the 'body' section of
* the main Program's AST.
*/
for (var heading in testFixture) {
	var tst = testFixture[heading];
	for (var code in tst) {
		if (typeof tst[code] === "string") {
			// Expected test result holds an error message
			testFail(code, tst[code], { ecmaVersion: 6 });
		} else {
			// Expected test result holds an AST
			switch (heading) {
				case 'qml-root-obj-members':
					test(code, {
						type: "Program",
						body: [
							{
								type: "QMLObjectLiteral",
								block: {
									type: "QMLMemberBlock",
									members: tst[code]
								}
							}
						]
					}, {
						ecmaVersion: 6,
						locations: true,
						ranges: true,
					});
					break;
				default:
					test(code, {
						type: "Program",
						body: tst[code]
					}, {
						ecmaVersion: 6,
						locations: true,
						ranges: true,
					});
			}
		}
	}
}