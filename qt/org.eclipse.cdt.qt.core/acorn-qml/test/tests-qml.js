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
                        id: {
                            type: "QMLQualifiedID",
                            range: [7, 14],
                            loc: {
                                start: { line: 1, column: 7 },
                                end: { line: 1, column: 14 }
                            },
                            parts: [{
                                "type": "Identifier",
                                "range": [7, 14],
                                "loc": {
                                    "start": { "line": 1, "column": 7 },
                                    "end": { "line": 1, "column": 14 }
                                },
                                "name": "QtQuick"
                            }],
                            name: "QtQuick"
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
                    directory: {
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
                    directory: {
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
                        id: {
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
                        id: {
                            type: "QMLQualifiedID",
                            range: [7, 14],
                            loc: {
                                start: { line: 1, column: 7 },
                                end: { line: 1, column: 14 }
                            },
                            parts: [{
                                "type": "Identifier",
                                "range": [7, 14],
                                "loc": {
                                    "start": { "line": 1, "column": 7 },
                                    "end": { "line": 1, "column": 14 }
                                },
                                "name": "QtQuick"
                            }],
                            name: "QtQuick"
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
                    directory: {
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
                        id: {
                            type: "QMLQualifiedID",
                            range: [7, 14],
                            loc: {
                                start: { line: 1, column: 7 },
                                end: { line: 1, column: 14 }
                            },
                            parts: [{
                                "type": "Identifier",
                                "range": [7, 14],
                                "loc": {
                                    "start": { "line": 1, "column": 7 },
                                    "end": { "line": 1, "column": 14 }
                                },
                                "name": "QtQuick"
                            }],
                            name: "QtQuick"
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
                    directory: {
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
                        id: {
                            type: "QMLQualifiedID",
                            range: [7, 13],
                            loc: {
                                start: { line: 1, column: 7 },
                                end: { line: 1, column: 13 }
                            },
                            parts: [{
                                "type": "Identifier",
                                "range": [7, 13],
                                "loc": {
                                    "start": { "line": 1, "column": 7 },
                                    "end": { "line": 1, "column": 13 }
                                },
                                "name": "Module"
                            }],
                            name: "Module"
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
                        id: {
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
                        id: {
                            type: "QMLQualifiedID",
                            range: [7, 24],
                            loc: {
                                start: { line: 1, column: 7 },
                                end: { line: 1, column: 24 }
                            },
                            parts: [
                                {
                                    "type": "Identifier",
                                    "range": [7, 16],
                                    "loc": {
                                        "start": { "line": 1, "column": 7 },
                                        "end": { "line": 1, "column": 16 }
                                    },
                                    "name": "Qualified"
                                },
                                {
                                    "type": "Identifier",
                                    "range": [17, 19],
                                    "loc": {
                                        "start": { "line": 1, "column": 17 },
                                        "end": { "line": 1, "column": 19 }
                                    },
                                    "name": "Id"
                                },
                                {
                                    "type": "Identifier",
                                    "range": [20, 24],
                                    "loc": {
                                        "start": { "line": 1, "column": 20 },
                                        "end": { "line": 1, "column": 24 }
                                    },
                                    "name": "Test"
                                }
                            ],
                            name: "Qualified.Id.Test"
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

		// -------------------------- Pragma --------------------------
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
					id: {
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
                    id: {
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
                    id: {
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
                    id: {
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
                    id: {
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
	},

	// ---------------------- Object Literals ---------------------
	'qml-object-literal': {
		'Window {}': {
            type: "QMLObjectLiteral",
            range: [0, 9],
            loc: {
                start: { line: 1, column: 0 },
                end: { line: 1, column: 9 }
            },
            id: {
                type: "QMLQualifiedID",
                range: [0, 6],
                loc: {
                    start: { line: 1, column: 0 },
                    end: { line: 1, column: 6 }
                },
                parts: [{
                    "type": "Identifier",
                    "range": [0, 6],
                    "loc": {
                        "start": { "line": 1, "column": 0 },
                        "end": { "line": 1, "column": 6 }
                    },
                    "name": "Window"
                }],
                name: "Window"
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
        },

		'QtQuick.Window {}': {
            type: "QMLObjectLiteral",
            range: [0, 17],
            loc: {
                start: { line: 1, column: 0 },
                end: { line: 1, column: 17 }
            },
            id: {
                type: "QMLQualifiedID",
                range: [0, 14],
                loc: {
                    start: { line: 1, column: 0 },
                    end: { line: 1, column: 14 }
                },
                parts: [
                    {
                        "type": "Identifier",
                        "range": [0, 7],
                        "loc": {
                            "start": { "line": 1, "column": 0 },
                            "end": { "line": 1, "column": 7 }
                        },
                        "name": "QtQuick"
                    },
                    {
                        "type": "Identifier",
                        "range": [8, 14],
                        "loc": {
                            "start": { "line": 1, "column": 8 },
                            "end": { "line": 1, "column": 14 }
                        },
                        "name": "Window"
                    }
                ],
                name: "QtQuick.Window"
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
        },

        'property {}': {
            type: "QMLObjectLiteral",
            range: [0, 11],
            loc: {
                start: { line: 1, column: 0 },
                end: { line: 1, column: 11 }
            },
            id: {
                type: "QMLQualifiedID",
                range: [0, 8],
                loc: {
                    start: { line: 1, column: 0 },
                    end: { line: 1, column: 8 }
                },
                parts: [{
                    "type": "Identifier",
                    "range": [0, 8],
                    "loc": {
                        "start": { "line": 1, "column": 0 },
                        "end": { "line": 1, "column": 8 }
                    },
                    "name": "property"
                }],
                name: "property"
            },
            block: {
                type: "QMLMemberBlock",
                range: [9, 11],
                loc: {
                    start: { line: 1, column: 9 },
                    end: { line: 1, column: 11 }
                },
                members: []
            }
        },

        'readonly {}': {
            type: "QMLObjectLiteral",
            range: [0, 11],
            loc: {
                start: { line: 1, column: 0 },
                end: { line: 1, column: 11 }
            },
            id: {
                type: "QMLQualifiedID",
                range: [0, 8],
                loc: {
                    start: { line: 1, column: 0 },
                    end: { line: 1, column: 8 }
                },
                parts: [{
                    "type": "Identifier",
                    "range": [0, 8],
                    "loc": {
                        "start": { "line": 1, "column": 0 },
                        "end": { "line": 1, "column": 8 }
                    },
                    "name": "readonly"
                }],
                name: "readonly"
            },
            block: {
                type: "QMLMemberBlock",
                range: [9, 11],
                loc: {
                    start: { line: 1, column: 9 },
                    end: { line: 1, column: 11 }
                },
                members: []
            }
        },

        'signal {}': {
            type: "QMLObjectLiteral",
            range: [0, 9],
            loc: {
                start: { line: 1, column: 0 },
                end: { line: 1, column: 9 }
            },
            id: {
                type: "QMLQualifiedID",
                range: [0, 6],
                loc: {
                    start: { line: 1, column: 0 },
                    end: { line: 1, column: 6 }
                },
                parts: [{
                    "type": "Identifier",
                    "range": [0, 6],
                    "loc": {
                        "start": { "line": 1, "column": 0 },
                        "end": { "line": 1, "column": 6 }
                    },
                    "name": "signal"
                }],
                name: "signal"
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
        },

        'alias {}': {
            type: "QMLObjectLiteral",
            range: [0, 8],
            loc: {
                start: { line: 1, column: 0 },
                end: { line: 1, column: 8 }
            },
            id: {
                type: "QMLQualifiedID",
                range: [0, 5],
                loc: {
                    start: { line: 1, column: 0 },
                    end: { line: 1, column: 5 }
                },
                parts: [{
                    "type": "Identifier",
                    "range": [0, 5],
                    "loc": {
                        "start": { "line": 1, "column": 0 },
                        "end": { "line": 1, "column": 5 }
                    },
                    "name": "alias"
                }],
                name: "alias"
            },
            block: {
                type: "QMLMemberBlock",
                range: [6, 8],
                loc: {
                    start: { line: 1, column: 6 },
                    end: { line: 1, column: 8 }
                },
                members: []
            }
        },

        'list {}': {
            type: "QMLObjectLiteral",
            range: [0, 7],
            loc: {
                start: { line: 1, column: 0 },
                end: { line: 1, column: 7 }
            },
            id: {
                type: "QMLQualifiedID",
                range: [0, 4],
                loc: {
                    start: { line: 1, column: 0 },
                    end: { line: 1, column: 4 }
                },
                parts: [{
                    "type": "Identifier",
                    "range": [0, 4],
                    "loc": {
                        "start": { "line": 1, "column": 0 },
                        "end": { "line": 1, "column": 4 }
                    },
                    "name": "list"
                }],
                name: "list"
            },
            block: {
                type: "QMLMemberBlock",
                range: [5, 7],
                loc: {
                    start: { line: 1, column: 5 },
                    end: { line: 1, column: 7 }
                },
                members: []
            }
        },

        'color {}': {
            type: "QMLObjectLiteral",
            range: [0, 8],
            loc: {
                start: { line: 1, column: 0 },
                end: { line: 1, column: 8 }
            },
            id: {
                type: "QMLQualifiedID",
                range: [0, 5],
                loc: {
                    start: { line: 1, column: 0 },
                    end: { line: 1, column: 5 }
                },
                parts: [{
                    "type": "Identifier",
                    "range": [0, 5],
                    "loc": {
                        "start": { "line": 1, "column": 0 },
                        "end": { "line": 1, "column": 5 }
                    },
                    "name": "color"
                }],
                name: "color"
            },
            block: {
                type: "QMLMemberBlock",
                range: [6, 8],
                loc: {
                    start: { line: 1, column: 6 },
                    end: { line: 1, column: 8 }
                },
                members: []
            }
        },

        'real {}': {
            type: "QMLObjectLiteral",
            range: [0, 7],
            loc: {
                start: { line: 1, column: 0 },
                end: { line: 1, column: 7 }
            },
            id: {
                type: "QMLQualifiedID",
                range: [0, 4],
                loc: {
                    start: { line: 1, column: 0 },
                    end: { line: 1, column: 4 }
                },
                parts: [{
                    "type": "Identifier",
                    "range": [0, 4],
                    "loc": {
                        "start": { "line": 1, "column": 0 },
                        "end": { "line": 1, "column": 4 }
                    },
                    "name": "real"
                }],
                name: "real"
            },
            block: {
                type: "QMLMemberBlock",
                range: [5, 7],
                loc: {
                    start: { line: 1, column: 5 },
                    end: { line: 1, column: 7 }
                },
                members: []
            }
        },

        'string {}': {
            type: "QMLObjectLiteral",
            range: [0, 9],
            loc: {
                start: { line: 1, column: 0 },
                end: { line: 1, column: 9 }
            },
            id: {
                type: "QMLQualifiedID",
                range: [0, 6],
                loc: {
                    start: { line: 1, column: 0 },
                    end: { line: 1, column: 6 }
                },
                parts: [{
                    "type": "Identifier",
                    "range": [0, 6],
                    "loc": {
                        "start": { "line": 1, "column": 0 },
                        "end": { "line": 1, "column": 6 }
                    },
                    "name": "string"
                }],
                name: "string"
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
        },

        'url {}': {
            type: "QMLObjectLiteral",
            range: [0, 6],
            loc: {
                start: { line: 1, column: 0 },
                end: { line: 1, column: 6 }
            },
            id: {
                type: "QMLQualifiedID",
                range: [0, 3],
                loc: {
                    start: { line: 1, column: 0 },
                    end: { line: 1, column: 3 }
                },
                parts: [{
                    "type": "Identifier",
                    "range": [0, 3],
                    "loc": {
                        "start": { "line": 1, "column": 0 },
                        "end": { "line": 1, "column": 3 }
                    },
                    "name": "url"
                }],
                name: "url"
            },
            block: {
                type: "QMLMemberBlock",
                range: [4, 6],
                loc: {
                    start: { line: 1, column: 4 },
                    end: { line: 1, column: 6 }
                },
                members: []
            }
        },

		'Window {Button {}}': {
            type: "QMLObjectLiteral",
            range: [0, 18],
            loc: {
                start: { line: 1, column: 0 },
                end: { line: 1, column: 18 }
            },
            id: {
                type: "QMLQualifiedID",
                range: [0, 6],
                loc: {
                    start: { line: 1, column: 0 },
                    end: { line: 1, column: 6 }
                },
                parts: [{
                    "type": "Identifier",
                    "range": [0, 6],
                    "loc": {
                        "start": { "line": 1, "column": 0 },
                        "end": { "line": 1, "column": 6 }
                    },
                    "name": "Window"
                }],
                name: "Window"
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
                        id: {
                            type: "QMLQualifiedID",
                            range: [8, 14],
                            loc: {
                                start: { line: 1, column: 8 },
                                end: { line: 1, column: 14 }
                            },
                            parts: [{
                                "type": "Identifier",
                                "range": [8, 14],
                                "loc": {
                                    "start": { "line": 1, "column": 8 },
                                    "end": { "line": 1, "column": 14 }
                                },
                                "name": "Button"
                            }],
                            name: "Button"
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
				kind: "var",
				id: {
					type: "Identifier",
					range: [16, 17],
					loc: {
						start: { line: 1, column: 16 },
						end: { line: 1, column: 17 }
					},
					name: "w"
				},
                init: {
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
				kind: "boolean",
				id: {
					type: "Identifier",
					range: [20, 21],
					loc: {
						start: { line: 1, column: 20 },
						end: { line: 1, column: 21 }
					},
					name: "w"
				},
                init: {
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
				kind: "double",
				id: {
					type: "Identifier",
					range: [19, 20],
					loc: {
						start: { line: 1, column: 19 },
						end: { line: 1, column: 20 }
					},
					name: "w"
				},
                init: {
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
				kind: "int",
				id: {
					type: "Identifier",
					range: [16, 17],
					loc: {
						start: { line: 1, column: 16 },
						end: { line: 1, column: 17 }
					},
					name: "w"
				},
                init: {
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
				kind: "list",
				id: {
					type: "Identifier",
					range: [17, 18],
					loc: {
						start: { line: 1, column: 17 },
						end: { line: 1, column: 18 }
					},
					name: "w"
				},
                init: {
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
				kind: "color",
				id: {
					type: "Identifier",
					range: [18, 19],
					loc: {
						start: { line: 1, column: 18 },
						end: { line: 1, column: 19 }
					},
					name: "w"
				},
                init: {
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
				kind: "real",
				id: {
					type: "Identifier",
					range: [17, 18],
					loc: {
						start: { line: 1, column: 17 },
						end: { line: 1, column: 18 }
					},
					name: "w"
				},
                init: {
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
				kind: "string",
				id: {
					type: "Identifier",
					range: [19, 20],
					loc: {
						start: { line: 1, column: 19 },
						end: { line: 1, column: 20 }
					},
					name: "w"
				},
                init: {
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
				kind: "url",
				id: {
					type: "Identifier",
					range: [16, 17],
					loc: {
						start: { line: 1, column: 16 },
						end: { line: 1, column: 17 }
					},
					name: "w"
				},
                init: {
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
				kind: {
                    type: "QMLQualifiedID",
                    range: [12, 20],
                    loc: {
                        start: { line: 1, column: 12 },
                        end: { line: 1, column: 20 }
                    },
                    parts: [{
                        "type": "Identifier",
                        "range": [12, 20],
                        "loc": {
                            "start": { "line": 1, "column": 12 },
                            "end": { "line": 1, "column": 20 }
                        },
                        "name": "QtObject"
                    }],
                    name: "QtObject"
				},
				id: {
					type: "Identifier",
					range: [21, 22],
					loc: {
						start: { line: 1, column: 21 },
						end: { line: 1, column: 22 }
					},
					name: "w"
				},
				init: {
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
		],

		'a{ property alias c: color }': [
			{
				type: "QMLPropertyDeclaration",
				range: [3, 26],
				loc: {
					start: { line: 1, column: 3 },
					end: { line: 1, column: 26 }
				},
				default: false,
				readonly: false,
                kind: "alias",
				id: {
					type: "Identifier",
					range: [18, 19],
					loc: {
						start: { line: 1, column: 18 },
						end: { line: 1, column: 19 }
					},
					name: "c"
				},
				init: {
                    type: "Identifier",
                    range: [21, 26],
                    loc: {
                        start: { line: 1, column: 21 },
                        end: { line: 1, column: 26 }
                    },
                    name: "color"
                }
			}
		],

		// TODO: Solve ambiguity to allow QML Object Literals in bindings
		'a{ property var b: Window {}}': "Unexpected token (1:26)",

		// --------------------- Property Bindings --------------------
		'a{ w: 3 }': [
			{
				type: "QMLPropertyBinding",
				range: [3, 7],
				loc: {
					start: { line: 1, column: 3 },
					end: { line: 1, column: 7 }
				},
				id: {
					type: "QMLQualifiedID",
					range: [3, 4],
					loc: {
						start: { line: 1, column: 3 },
						end: { line: 1, column: 4 }
					},
					parts: [{
                        "type": "Identifier",
                        "range": [3, 4],
                        "loc": {
                            "start": { "line": 1, "column": 3 },
                            "end": { "line": 1, "column": 4 }
                        },
                        "name": "w"
                    }],
					name: "w"
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
		],

		'a{ x.y.z: 3 }': [
			{
				type: "QMLPropertyBinding",
				range: [3, 11],
				loc: {
					start: { line: 1, column: 3 },
					end: { line: 1, column: 11 }
				},
				id: {
					type: "QMLQualifiedID",
					range: [3, 8],
					loc: {
						start: { line: 1, column: 3 },
						end: { line: 1, column: 8 }
					},
					parts: [
                        {
                            "type": "Identifier",
                            "range": [3, 4],
                            "loc": {
                                "start": { "line": 1, "column": 3 },
                                "end": { "line": 1, "column": 4 }
                            },
                            "name": "x"
                        },
                        {
                            "type": "Identifier",
                            "range": [5, 6],
                            "loc": {
                                "start": { "line": 1, "column": 5 },
                                "end": { "line": 1, "column": 6 }
                            },
                            "name": "y"
                        },
                        {
                            "type": "Identifier",
                            "range": [7, 8],
                            "loc": {
                                "start": { "line": 1, "column": 7 },
                                "end": { "line": 1, "column": 8 }
                            },
                            "name": "z"
                        }
                    ],
					name: "x.y.z"
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
		],

		'a{ import: 3 }': "Unexpected token (1:3)",

		'a{ pragma: 3 }': "Unexpected token (1:3)",

		'a{ as: 3 }': "Unexpected token (1:3)",

		'a{ boolean: 3 }': "Unexpected token (1:3)",

		'a{ double: 3 }': "Unexpected token (1:3)",

		'a{ int: 3 }': "Unexpected token (1:3)",

		'a{ alias: 3 }': [
			{
				type: "QMLPropertyBinding",
				range: [3, 11],
				loc: {
					start: { line: 1, column: 3 },
					end: { line: 1, column: 11 }
				},
				id: {
					type: "QMLQualifiedID",
					range: [3, 8],
					loc: {
						start: { line: 1, column: 3 },
						end: { line: 1, column: 8 }
					},
					parts: [{
                        "type": "Identifier",
                        "range": [3, 8],
                        "loc": {
                            "start": { "line": 1, "column": 3 },
                            "end": { "line": 1, "column": 8 }
                        },
                        "name": "alias"
                    }],
					name: "alias"
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
		],

		'a{ list: 3 }': [
			{
				type: "QMLPropertyBinding",
				range: [3, 10],
				loc: {
					start: { line: 1, column: 3 },
					end: { line: 1, column: 10 }
				},
				id: {
					type: "QMLQualifiedID",
					range: [3, 7],
					loc: {
						start: { line: 1, column: 3 },
						end: { line: 1, column: 7 }
					},
					parts: [{
                        "type": "Identifier",
                        "range": [3, 7],
                        "loc": {
                            "start": { "line": 1, "column": 3 },
                            "end": { "line": 1, "column": 7 }
                        },
                        "name": "list"
                    }],
					name: "list"
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
		],

		'a{ property: 3 }': [
			{
				type: "QMLPropertyBinding",
				range: [3, 14],
				loc: {
					start: { line: 1, column: 3 },
					end: { line: 1, column: 14 }
				},
				id: {
					type: "QMLQualifiedID",
					range: [3, 11],
					loc: {
						start: { line: 1, column: 3 },
						end: { line: 1, column: 11 }
					},
					parts: [{
                        "type": "Identifier",
                        "range": [3, 11],
                        "loc": {
                            "start": { "line": 1, "column": 3 },
                            "end": { "line": 1, "column": 11 }
                        },
                        "name": "property"
                    }],
					name: "property"
				},
                expr: {
                    type: "Literal",
                    range: [13, 14],
                    loc: {
                        start: { line: 1, column: 13 },
                        end: { line: 1, column: 14 }
                    },
                    value: 3,
                    raw: "3"
                }
			}
		],

		'a{ readonly: 3 }': [
			{
				type: "QMLPropertyBinding",
				range: [3, 14],
				loc: {
					start: { line: 1, column: 3 },
					end: { line: 1, column: 14 }
				},
				id: {
					type: "QMLQualifiedID",
					range: [3, 11],
					loc: {
						start: { line: 1, column: 3 },
						end: { line: 1, column: 11 }
					},
					parts: [{
                        "type": "Identifier",
                        "range": [3, 11],
                        "loc": {
                            "start": { "line": 1, "column": 3 },
                            "end": { "line": 1, "column": 11 }
                        },
                        "name": "readonly"
                    }],
					name: "readonly"
				},
                expr: {
                    type: "Literal",
                    range: [13, 14],
                    loc: {
                        start: { line: 1, column: 13 },
                        end: { line: 1, column: 14 }
                    },
                    value: 3,
                    raw: "3"
                }
			}
		],

		'a{ signal: 3 }': [
			{
				type: "QMLPropertyBinding",
				range: [3, 12],
				loc: {
					start: { line: 1, column: 3 },
					end: { line: 1, column: 12 }
				},
				id: {
					type: "QMLQualifiedID",
					range: [3, 9],
					loc: {
						start: { line: 1, column: 3 },
						end: { line: 1, column: 9 }
					},
					parts: [{
                        "type": "Identifier",
                        "range": [3, 9],
                        "loc": {
                            "start": { "line": 1, "column": 3 },
                            "end": { "line": 1, "column": 9 }
                        },
                        "name": "signal"
                    }],
					name: "signal"
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
		],

		'a{ color: 3 }': [
			{
				type: "QMLPropertyBinding",
				range: [3, 11],
				loc: {
					start: { line: 1, column: 3 },
					end: { line: 1, column: 11 }
				},
				id: {
					type: "QMLQualifiedID",
					range: [3, 8],
					loc: {
						start: { line: 1, column: 3 },
						end: { line: 1, column: 8 }
					},
					parts: [{
                        "type": "Identifier",
                        "range": [3, 8],
                        "loc": {
                            "start": { "line": 1, "column": 3 },
                            "end": { "line": 1, "column": 8 }
                        },
                        "name": "color"
                    }],
					name: "color"
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
		],

		'a{ real: 3 }': [
			{
				type: "QMLPropertyBinding",
				range: [3, 10],
				loc: {
					start: { line: 1, column: 3 },
					end: { line: 1, column: 10 }
				},
				id: {
					type: "QMLQualifiedID",
					range: [3, 7],
					loc: {
						start: { line: 1, column: 3 },
						end: { line: 1, column: 7 }
					},
					parts: [{
                        "type": "Identifier",
                        "range": [3, 7],
                        "loc": {
                            "start": { "line": 1, "column": 3 },
                            "end": { "line": 1, "column": 7 }
                        },
                        "name": "real"
                    }],
					name: "real"
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
		],

		'a{ string: 3 }': [
			{
				type: "QMLPropertyBinding",
				range: [3, 12],
				loc: {
					start: { line: 1, column: 3 },
					end: { line: 1, column: 12 }
				},
				id: {
					type: "QMLQualifiedID",
					range: [3, 9],
					loc: {
						start: { line: 1, column: 3 },
						end: { line: 1, column: 9 }
					},
					parts: [{
                        "type": "Identifier",
                        "range": [3, 9],
                        "loc": {
                            "start": { "line": 1, "column": 3 },
                            "end": { "line": 1, "column": 9 }
                        },
                        "name": "string"
                    }],
					name: "string"
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
		],

		'a{ url: 3 }': [
			{
				type: "QMLPropertyBinding",
				range: [3, 9],
				loc: {
					start: { line: 1, column: 3 },
					end: { line: 1, column: 9 }
				},
				id: {
					type: "QMLQualifiedID",
					range: [3, 6],
					loc: {
						start: { line: 1, column: 3 },
						end: { line: 1, column: 6 }
					},
					parts: [{
                        "type": "Identifier",
                        "range": [3, 6],
                        "loc": {
                            "start": { "line": 1, "column": 3 },
                            "end": { "line": 1, "column": 6 }
                        },
                        "name": "url"
                    }],
					name: "url"
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
		],

		'a{ onClicked: Qt.quit(0) }': [
			{
				type: "QMLPropertyBinding",
				range: [3, 24],
				loc: {
					start: { line: 1, column: 3 },
					end: { line: 1, column: 24 }
				},
				id: {
					type: "QMLQualifiedID",
					range: [3, 12],
					loc: {
						start: { line: 1, column: 3 },
						end: { line: 1, column: 12 }
					},
					parts: [{
                        "type": "Identifier",
                        "range": [3, 12],
                        "loc": {
                            "start": { "line": 1, "column": 3 },
                            "end": { "line": 1, "column": 12 }
                        },
                        "name": "onClicked"
                    }],
					name: "onClicked"
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
		],

        'a{ property {} }': [
            {
                type: "QMLObjectLiteral",
                range: [3, 14],
                loc: {
                    start: { line: 1, column: 3 },
                    end: { line: 1, column: 14 }
                },
                id: {
                    type: "QMLQualifiedID",
                    range: [3, 11],
                    loc: {
                        start: { line: 1, column: 3 },
                        end: { line: 1, column: 11 }
                    },
                    parts: [
                        {
                            type: "Identifier",
                            range: [3, 11],
                            loc: {
                                start: { line: 1, column: 3 },
                                end: { line: 1, column: 11 }
                            },
                            name: "property"
                        }
                    ],
                    name: "property"
                },
                block: {
                    type: "QMLMemberBlock",
                    range: [12, 14],
                    loc: {
                        start: { line: 1, column: 12 },
                        end: { line: 1, column: 14 }
                    },
                    members: []
                }
            }
        ],

        'a{ readonly {} }': [
            {
                type: "QMLObjectLiteral",
                range: [3, 14],
                loc: {
                    start: { line: 1, column: 3 },
                    end: { line: 1, column: 14 }
                },
                default: false,
                readonly: false,
                id: {
                    type: "QMLQualifiedID",
                    range: [3, 11],
                    loc: {
                        start: { line: 1, column: 3 },
                        end: { line: 1, column: 11 }
                    },
                    parts: [
                        {
                            type: "Identifier",
                            range: [3, 11],
                            loc: {
                                start: { line: 1, column: 3 },
                                end: { line: 1, column: 11 }
                            },
                            name: "readonly"
                        }
                    ],
                    name: "readonly"
                },
                block: {
                    type: "QMLMemberBlock",
                    range: [12, 14],
                    loc: {
                        start: { line: 1, column: 12 },
                        end: { line: 1, column: 14 }
                    },
                    members: []
                }
            }
        ],

        'a{ signal {} }': [
            {
                type: "QMLObjectLiteral",
                range: [3, 12],
                loc: {
                    start: { line: 1, column: 3 },
                    end: { line: 1, column: 12 }
                },
                id: {
                    type: "QMLQualifiedID",
                    range: [3, 9],
                    loc: {
                        start: { line: 1, column: 3 },
                        end: { line: 1, column: 9 }
                    },
                    parts: [
                        {
                            type: "Identifier",
                            range: [3, 9],
                            loc: {
                                start: { line: 1, column: 3 },
                                end: { line: 1, column: 9 }
                            },
                            name: "signal"
                        }
                    ],
                    name: "signal"
                },
                block: {
                    type: "QMLMemberBlock",
                    range: [10, 12],
                    loc: {
                        start: { line: 1, column: 10 },
                        end: { line: 1, column: 12 }
                    },
                    members: []
                }
            }
        ],

		// ------------------- Contextual Keywords --------------------
		'a{b:pragma}': [
			{
				type: "QMLPropertyBinding",
				range: [2, 10],
				loc: {
					start: { line: 1, column: 2 },
					end: { line: 1, column: 10 }
				},
				id: {
					type: "QMLQualifiedID",
					range: [2, 3],
					loc: {
						start: { line: 1, column: 2 },
						end: { line: 1, column: 3 }
					},
					parts: [{
                        "type": "Identifier",
                        "range": [2, 3],
                        "loc": {
                            "start": { "line": 1, "column": 2 },
                            "end": { "line": 1, "column": 3 }
                        },
                        "name": "b"
                    }],
					name: "b"
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
		],

		'a{b:property}': [
			{
				type: "QMLPropertyBinding",
				range: [2, 12],
				loc: {
					start: { line: 1, column: 2 },
					end: { line: 1, column:  12 }
				},
				id: {
					type: "QMLQualifiedID",
					range: [2, 3],
					loc: {
						start: { line: 1, column: 2 },
						end: { line: 1, column:  3 }
					},
					parts: [{
                        "type": "Identifier",
                        "range": [2, 3],
                        "loc": {
                            "start": { "line": 1, "column": 2 },
                            "end": { "line": 1, "column": 3 }
                        },
                        "name": "b"
                    }],
					name: "b"
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
		],

		'a{b:readonly}': [
			{
				type: "QMLPropertyBinding",
				range: [2, 12],
				loc: {
					start: { line: 1, column: 2 },
					end: { line: 1, column: 12 }
				},
				id: {
					type: "QMLQualifiedID",
					range: [2, 3],
					loc: {
						start: { line: 1, column: 2 },
						end: { line: 1, column: 3 }
					},
					parts: [{
                        "type": "Identifier",
                        "range": [2, 3],
                        "loc": {
                            "start": { "line": 1, "column": 2 },
                            "end": { "line": 1, "column": 3 }
                        },
                        "name": "b"
                    }],
					name: "b"
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
		],

		'a{b:signal}': [
			{
				type: "QMLPropertyBinding",
				range: [2, 10],
				loc: {
					start: { line: 1, column: 2 },
					end: { line: 1, column: 10 }
				},
				id: {
					type: "QMLQualifiedID",
					range: [2, 3],
					loc: {
						start: { line: 1, column: 2 },
						end: { line: 1, column: 3 }
					},
					parts: [{
                        "type": "Identifier",
                        "range": [2, 3],
                        "loc": {
                            "start": { "line": 1, "column": 2 },
                            "end": { "line": 1, "column": 3 }
                        },
                        "name": "b"
                    }],
					name: "b"
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
		],

		'a{b:alias}': [
			{
				type: "QMLPropertyBinding",
				range: [2, 9],
				loc: {
					start: { line: 1, column: 2 },
					end: { line: 1, column: 9 }
				},
				id: {
					type: "QMLQualifiedID",
					range: [2, 3],
					loc: {
						start: { line: 1, column: 2 },
						end: { line: 1, column: 3 }
					},
					parts: [{
                        "type": "Identifier",
                        "range": [2, 3],
                        "loc": {
                            "start": { "line": 1, "column": 2 },
                            "end": { "line": 1, "column": 3 }
                        },
                        "name": "b"
                    }],
					name: "b"
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
		],

		'a{b:list}': [
			{
				type: "QMLPropertyBinding",
				range: [2, 8],
				loc: {
					start: { line: 1, column: 2 },
					end: { line: 1, column: 8 }
				},
				id: {
					type: "QMLQualifiedID",
					range: [2, 3],
					loc: {
						start: { line: 1, column: 2 },
						end: { line: 1, column: 3 }
					},
					parts: [{
                        "type": "Identifier",
                        "range": [2, 3],
                        "loc": {
                            "start": { "line": 1, "column": 2 },
                            "end": { "line": 1, "column": 3 }
                        },
                        "name": "b"
                    }],
					name: "b"
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
		],

		'a{b:color}': [
			{
				type: "QMLPropertyBinding",
				range: [2, 9],
				loc: {
					start: { line: 1, column: 2 },
					end: { line: 1, column: 9 }
				},
				id: {
					type: "QMLQualifiedID",
					range: [2, 3],
					loc: {
						start: { line: 1, column: 2 },
						end: { line: 1, column: 3 }
					},
					parts: [{
                        "type": "Identifier",
                        "range": [2, 3],
                        "loc": {
                            "start": { "line": 1, "column": 2 },
                            "end": { "line": 1, "column": 3 }
                        },
                        "name": "b"
                    }],
					name: "b"
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
		],

		'a{b:real}': [
			{
				type: "QMLPropertyBinding",
				range: [2, 8],
				loc: {
					start: { line: 1, column: 2 },
					end: { line: 1, column: 8 }
				},
				id: {
					type: "QMLQualifiedID",
					range: [2, 3],
					loc: {
						start: { line: 1, column: 2 },
						end: { line: 1, column: 3 }
					},
					parts: [{
                        "type": "Identifier",
                        "range": [2, 3],
                        "loc": {
                            "start": { "line": 1, "column": 2 },
                            "end": { "line": 1, "column": 3 }
                        },
                        "name": "b"
                    }],
					name: "b"
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
		],

		'a{b:string}': [
			{
				type: "QMLPropertyBinding",
				range: [2, 10],
				loc: {
					start: { line: 1, column: 2 },
					end: { line: 1, column: 10 }
				},
				id: {
					type: "QMLQualifiedID",
					range: [2, 3],
					loc: {
						start: { line: 1, column: 2 },
						end: { line: 1, column: 3 }
					},
					parts: [{
                        "type": "Identifier",
                        "range": [2, 3],
                        "loc": {
                            "start": { "line": 1, "column": 2 },
                            "end": { "line": 1, "column": 3 }
                        },
                        "name": "b"
                    }],
					name: "b"
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
		],

		'a{b:url}': [
			{
				type: "QMLPropertyBinding",
				range: [2, 7],
				loc: {
					start: { line: 1, column: 2 },
					end: { line: 1, column: 7 }
				},
				id: {
					type: "QMLQualifiedID",
					range: [2, 3],
					loc: {
						start: { line: 1, column: 2 },
						end: { line: 1, column: 3 }
					},
					parts: [{
                        "type": "Identifier",
                        "range": [2, 3],
                        "loc": {
                            "start": { "line": 1, "column": 2 },
                            "end": { "line": 1, "column": 3 }
                        },
                        "name": "b"
                    }],
					name: "b"
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
				id: {
					type: "Identifier",
					range: [10, 11],
					loc: {
						start: { line: 1, column: 10 },
						end: { line: 1, column: 11 }
					},
					name: "b"
				},
				params: []
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
				id: {
					type: "Identifier",
					range: [10, 11],
					loc: {
						start: { line: 1, column: 10 },
						end: { line: 1, column: 11 }
					},
					name: "b"
				},
				params: []
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
				id: {
					type: "Identifier",
					range: [10, 11],
					loc: {
						start: { line: 1, column: 10 },
						end: { line: 1, column: 11 }
					},
					name: "b"
				},
				params: [
					{
						type: "QMLParameter",
						range: [ 13, 23 ],
						loc: {
							start: { line: 1, column: 13 },
                            end: { line: 1, column: 23 }
						},
                        kind: {
							type: "Identifier",
							range: [13, 17],
							loc: {
								start: { line: 1, column: 13 },
								end: { line: 1, column: 17 }
							},
							name: "type"
						},
						id: {
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
				id: {
					type: "Identifier",
					range: [10, 11],
					loc: {
						start: { line: 1, column: 10 },
						end: { line: 1, column: 11 }
					},
					name: "b"
				},
				params: [
					{
						type: "QMLParameter",
						range: [ 13, 25 ],
						loc: {
							start: { line: 1, column: 13 },
                            end: { line: 1, column: 25 }
						},
                        kind: {
							type: "Identifier",
							range: [13, 18],
							loc: {
								start: { line: 1, column: 13 },
								end: { line: 1, column: 18 }
							},
							name: "type1"
						},
						id: {
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
						type: "QMLParameter",
						range: [ 27, 39 ],
						loc: {
							start: { line: 1, column: 27 },
                            end: { line: 1, column: 39 }
						},
                        kind: {
							type: "Identifier",
							range: [27, 32],
							loc: {
								start: { line: 1, column: 27 },
								end: { line: 1, column: 32 }
							},
							name: "type2"
						},
						id: {
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
						type: "QMLParameter",
						range: [ 41, 53 ],
						loc: {
							start: { line: 1, column: 41 },
                            end: { line: 1, column: 53 }
						},
                        kind: {
							type: "Identifier",
							range: [41, 46],
							loc: {
								start: { line: 1, column: 41 },
								end: { line: 1, column: 46 }
							},
							name: "type3"
						},
						id: {
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

        'a{ id: test }': [
			{
				type: "QMLPropertyBinding",
				range: [3, 11],
				loc: {
					start: { line: 1, column: 3 },
					end: { line: 1, column: 11 }
				},
                id: {
                    type: "QMLQualifiedID",
                    range: [3, 5],
                    loc: {
                        start: { line: 1, column: 3 },
                        end: { line: 1, column: 5 }
                    },
                    parts: [{
                        type: "Identifier",
                        range: [3, 5],
                        loc: {
                            start: { line: 1, column: 3 },
                            end: { line: 1, column: 5 }
                        },
                        name: "id"
                    }],
                    name: "id"
                },
				expr: {
					type: "Identifier",
					range: [7, 11],
					loc: {
						start: { line: 1, column: 7 },
						end: { line: 1, column: 11 }
					},
					name: "test"
				}
			}
		],

        'a{ id: test; }': [
			{
				type: "QMLPropertyBinding",
				range: [3, 12],
				loc: {
					start: { line: 1, column: 3 },
					end: { line: 1, column: 12 }
				},
                id: {
                    type: "QMLQualifiedID",
                    range: [3, 5],
                    loc: {
                        start: { line: 1, column: 3 },
                        end: { line: 1, column: 5 }
                    },
                    parts: [{
                        type: "Identifier",
                        range: [3, 5],
                        loc: {
                            start: { line: 1, column: 3 },
                            end: { line: 1, column: 5 }
                        },
                        name: "id"
                    }],
                    name: "id"
                },
				expr: {
					type: "Identifier",
					range: [7, 11],
					loc: {
						start: { line: 1, column: 7 },
						end: { line: 1, column: 11 }
					},
					name: "test"
				}
			}
		]
	},

	// ----------------------- QML Examples -----------------------
	'qml-examples': {

		// Hello World
		'import QtQuick 2.3\nimport QtQuick.Window 2.2\n\tWindow {\n\tvisible: true\n\n\tMouseArea {\n\t\tanchors.fill: parent\n\t\tonClicked: {\n\t\t\tQt.quit();\n\t\t}\n\t}\n\t\tText {\n\t\t\ttext: qsTr("Hello World")\n\t\t\tanchors.centerIn: parent\n\t}\n}':
		{
            type: "Program",
            headerStatements: {
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
                            id: {
                                type: "QMLQualifiedID",
                                range: [7, 14],
                                loc: {
                                    start: { line: 1, column: 7 },
                                    end: { line: 1, column: 14 }
                                },
                                parts: [
                                    {
                                        type: "Identifier",
                                        range: [7, 14],
                                        loc: {
                                            start: { line: 1, column: 7 },
                                            end: { line: 1, column: 14 }
                                        },
                                        name: "QtQuick"
                                    }
                                ],
                                name: "QtQuick"
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
                            id: {
                                type: "QMLQualifiedID",
                                range: [26, 40],
                                loc: {
                                    start: { line: 2, column: 7 },
                                    end: { line: 2, column: 21 }
                                },
                                parts: [
                                    {
                                        type: "Identifier",
                                        range: [26, 33],
                                        loc: {
                                            start: { line: 2, column: 7 },
                                            end: { line: 2, column: 14 }
                                        },
                                        name: "QtQuick"
                                    },
                                    {
                                        type: "Identifier",
                                        range: [34, 40],
                                        loc: {
                                            start: { line: 2, column: 15 },
                                            end: { line: 2, column: 21 }
                                        },
                                        name: "Window"
                                    }
                                ],
                                name: "QtQuick.Window"
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
            rootObject: {
                type: "QMLObjectLiteral",
                range: [46, 213],
                loc: {
                    start: { line: 3, column: 1 },
                    end: { line: 16, column: 1 }
                },
                id: {
                    type: "QMLQualifiedID",
                    range: [46, 52],
                    loc: {
                        start: { line: 3, column: 1 },
                        end: { line: 3, column: 7 }
                    },
                    parts: [
                        {
                            type: "Identifier",
                            range: [46, 52],
                            loc: {
                                start: { line: 3, column: 1 },
                                end: { line: 3, column: 7 }
                            },
                            name: "Window"
                        }
                    ],
                    name: "Window"
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
                            type: "QMLPropertyBinding",
                            range: [56, 69],
                            loc: {
                                start: { line: 4, column: 1 },
                                end: { line: 4, column: 14 }
                            },
                            id: {
                                type: "QMLQualifiedID",
                                range: [56, 63],
                                loc: {
                                    start: { line: 4, column: 1 },
                                    end: { line: 4, column: 8 }
                                },
                                parts: [
                                    {
                                        type: "Identifier",
                                        range: [56, 63],
                                        loc: {
                                            start: { line: 4, column: 1 },
                                            end: { line: 4, column: 8 }
                                        },
                                        name: "visible"
                                    }
                                ],
                                name: "visible"
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
                        },
                        {
                            type: "QMLObjectLiteral",
                            range: [72, 142],
                            loc: {
                                start: { line: 6, column: 1 },
                                end: { line: 11, column: 2 }
                            },
                            id: {
                                type: "QMLQualifiedID",
                                range: [72, 81],
                                loc: {
                                    start: { line: 6, column: 1 },
                                    end: { line: 6, column: 10 }
                                },
                                parts: [
                                    {
                                        type: "Identifier",
                                        range: [72, 81],
                                        loc: {
                                            start: { line: 6, column: 1 },
                                            end: { line: 6, column: 10 }
                                        },
                                        name: "MouseArea"
                                    }
                                ],
                                name: "MouseArea"
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
                                        type: "QMLPropertyBinding",
                                        range: [86, 106],
                                        loc: {
                                            start: { line: 7, column: 2 },
                                            end: { line: 7, column: 22 }
                                        },
                                        id: {
                                            type: "QMLQualifiedID",
                                            range: [86, 98],
                                            loc: {
                                                start: { line: 7, column: 2 },
                                                end: { line: 7, column: 14 }
                                            },
                                            parts: [
                                                {
                                                    type: "Identifier",
                                                    range: [86, 93],
                                                    loc: {
                                                        start: { line: 7, column: 2 },
                                                        end: { line: 7, column: 9 }
                                                    },
                                                    name: "anchors"
                                                },
                                                {
                                                    type: "Identifier",
                                                    range: [94, 98],
                                                    loc: {
                                                        start: { line: 7, column: 10 },
                                                        end: { line: 7, column: 14 }
                                                    },
                                                    name: "fill"
                                                }
                                            ],
                                            name: "anchors.fill"
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
                                    },
                                    {
                                        type: "QMLPropertyBinding",
                                        range: [109, 139],
                                        loc: {
                                            start: { line: 8, column: 2 },
                                            end: { line: 10, column: 3 }
                                        },
                                        id: {
                                            type: "QMLQualifiedID",
                                            range: [109, 118],
                                            loc: {
                                                start: { line: 8, column: 2 },
                                                end: { line: 8, column: 11 }
                                            },
                                            parts: [
                                                {
                                                    type: "Identifier",
                                                    range: [109, 118],
                                                    loc: {
                                                        start: { line: 8, column: 2 },
                                                        end: { line: 8, column: 11 }
                                                    },
                                                    name: "onClicked"
                                                }
                                            ],
                                            name: "onClicked"
                                        },
                                        expr: {
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
                            id: {
                                type: "QMLQualifiedID",
                                range: [145, 149],
                                loc: {
                                    start: { line: 12, column: 2 },
                                    end: { line: 12, column: 6 }
                                },
                                parts: [
                                    {
                                        type: "Identifier",
                                        range: [145, 149],
                                        loc: {
                                            start: { line: 12, column: 2 },
                                            end: { line: 12, column: 6 }
                                        },
                                        name: "Text"
                                    }
                                ],
                                name: "Text"
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
                                        type: "QMLPropertyBinding",
                                        range: [155, 180],
                                        loc: {
                                            start: { line: 13, column: 3 },
                                            end: { line: 13, column: 28 }
                                        },
                                        id: {
                                            type: "QMLQualifiedID",
                                            range: [155, 159],
                                            loc: {
                                                start: { line: 13, column: 3 },
                                                end: { line: 13, column: 7 }
                                            },
                                            parts: [
                                                {
                                                    type: "Identifier",
                                                    range: [155, 159],
                                                    loc: {
                                                        start: { line: 13, column: 3 },
                                                        end: { line: 13, column: 7 }
                                                    },
                                                    name: "text"
                                                }
                                            ],
                                            name: "text"
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
                                    },
                                    {
                                        type: "QMLPropertyBinding",
                                        range: [184, 208],
                                        loc: {
                                            start: { line: 14, column: 3 },
                                            end: { line: 14, column: 27 }
                                        },
                                        id: {
                                            type: "QMLQualifiedID",
                                            range: [184, 200],
                                            loc: {
                                                start: { line: 14, column: 3 },
                                                end: { line: 14, column: 19 }
                                            },
                                            parts: [
                                                {
                                                    type: "Identifier",
                                                    range: [184, 191],
                                                    loc: {
                                                        start: { line: 14, column: 3 },
                                                        end: { line: 14, column: 10 }
                                                    },
                                                    name: "anchors"
                                                },
                                                {
                                                    type: "Identifier",
                                                    range: [192, 200],
                                                    loc: {
                                                        start: { line: 14, column: 11 },
                                                        end: { line: 14, column: 19 }
                                                    },
                                                    name: "centerIn"
                                                }
                                            ],
                                            name: "anchors.centerIn"
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
                                ]
                            }
                        }
                    ]
                }
            }
        }
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
                case 'qml-header-statements':
                    test(code, {
						type: "Program",
                        headerStatements: tst[code],
						rootObject: null
					}, {
						ecmaVersion: 6,
						locations: true,
						ranges: true,
					});
					break;
                case 'qml-object-literal':
                    test(code, {
						type: "Program",
                        headerStatements: {
                            type: "QMLHeaderStatements",
                            range: [0, 0],
                        },
						rootObject: tst[code]
					}, {
						ecmaVersion: 6,
						locations: true,
						ranges: true,
					});
					break;
				case 'qml-root-obj-members':
					test(code, {
						type: "Program",
                        headerStatements: {
                            type: "QMLHeaderStatements",
                            range: [0, 0],
                        },
						rootObject: {
                            type: "QMLObjectLiteral",
                            block: {
                                type: "QMLMemberBlock",
                                members: tst[code]
                            }
                        }
					}, {
						ecmaVersion: 6,
						locations: true,
						ranges: true,
					});
					break;
				default:
					test(code, tst[code], {
						ecmaVersion: 6,
						locations: true,
						ranges: true,
					});
			}
		}
	}
}