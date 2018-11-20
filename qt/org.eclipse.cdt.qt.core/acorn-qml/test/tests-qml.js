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
var testFail = driver.testFail;
var tokTypes = driver.tokTypes;

testFail('', "QML only supports ECMA Script Language Specification 5 or older",
		{ locations: true, ecmaVersion: 6, allowReserved: false });

test('import QtQuick 2.02', headerItemList([{
	type: "QMLImport",
	loc: {
			start: { line: 1, column: 0 },
			end: { line: 1, column: 19 }
		},
	module: {
		type: "QMLModule",
		loc: {
			start: { line: 1, column: 7 },
			end: { line: 1, column: 19 }
		},
		id: {
			type: "QMLQualifiedID",
			loc: {
				start: { line: 1, column: 7 },
				end: { line: 1, column: 14 }
			},
			parts: [{ type: "Identifier", name: "QtQuick" }],
			name: "QtQuick"
		},
		version: {
			type: "QMLVersionLiteral",
			loc: {
				start: { line: 1, column: 15 },
				end: { line: 1, column: 19 }
			},
			value: 2.02,
			raw: "2.02"
		}
	}
}]));

test('import "./file.js"', headerItemList([{
	type: "QMLImport",
	loc: {
		start: { line: 1, column: 0 },
		end: { line: 1, column: 18 }
	},
	directory: {
		type: "Literal",
		loc: {
			start: { line: 1, column: 7 },
			end: { line: 1, column: 18 }
		},
		value: "./file.js",
		raw: "\"./file.js\""
	}
}]));

test('import "./file.js" as MyModule', headerItemList([{
	type: "QMLImport",
	loc: {
		start: { line: 1, column: 0 },
		end: { line: 1, column: 30 }
	},
	directory: {
		type: "Literal",
		loc: {
			start: { line: 1, column: 7 },
			end: { line: 1, column: 18 }
		},
		value: "./file.js",
		raw: "\"./file.js\""
	},
	qualifier: {
		type: "QMLQualifier",
		loc: {
			start: { line: 1, column: 19 },
			end: { line: 1, column: 30 }
		},
		id: {
			type: "Identifier",
			loc: {
				start: { line: 1, column: 22 },
				end: { line: 1, column: 30 }
			},
			name: "MyModule"
		}
	}
}]));

testFail('import QtQuick ver',
		"Unexpected token (1:15)",
		{ locations: true, loose: false });

testFail('import QtQuick 0x01',
		"QML module must specify major and minor version (1:15)",
		{ locations: true, loose: false });

testFail('import QtQuick 1',
		"QML module must specify major and minor version (1:15)",
		{ locations: true, loose: false });

test('import QtQuick 2.2\nimport "./file.js"', headerItemList([
	{
		type: "QMLImport",
		loc: {
			start: { line: 1, column: 0 },
			end: { line: 1, column: 18 }
		},
		module: {
			type: "QMLModule",
			loc: {
				start: { line: 1, column: 7 },
				end: { line: 1, column: 18 }
			},
			id: {
				type: "QMLQualifiedID",
				loc: {
					start: { line: 1, column: 7 },
					end: { line: 1, column: 14 }
				},
				parts: [{ type: "Identifier", name: "QtQuick" }],
				name: "QtQuick"
			},
			version: {
				type: "QMLVersionLiteral",
				loc: {
					start: { line: 1, column: 15 },
					end: { line: 1, column: 18 }
				},
				value: 2.2,
				raw: "2.2"
			}
		}
	},
	{
		type: "QMLImport",
		loc: {
			start: { line: 2, column: 0 },
			end: { line: 2, column: 18 }
		},
		directory: {
			type: "Literal",
			loc: {
				start: { line: 2, column: 7 },
				end: { line: 2, column: 18 }
			},
			value: "./file.js",
			raw: "\"./file.js\""
		}
	}
]));

test('import QtQuick 2.2;import "./file.js"', headerItemList([
	{
		type: "QMLImport",
		loc: {
			start: { line: 1, column: 0 },
			end: { line: 1, column: 19 }
		},
		module: {
			type: "QMLModule",
			loc: {
				start: { line: 1, column: 7 },
				end: { line: 1, column: 18 }
			},
			id: {
				type: "QMLQualifiedID",
				loc: {
					start: { line: 1, column: 7 },
					end: { line: 1, column: 14 }
				},
				parts: [{ type: "Identifier", name: "QtQuick" }],
				name: "QtQuick"
			},
			version: {
				type: "QMLVersionLiteral",
				loc: {
					start: { line: 1, column: 15 },
					end: { line: 1, column: 18 }
				},
				value: 2.2,
				raw: "2.2"
			}
		}
	},
	{
		type: "QMLImport",
		loc: {
			start: { line: 1, column: 19 },
			end: { line: 1, column: 37 }
		},
		directory: {
			type: "Literal",
			loc: {
				start: { line: 1, column: 26 },
				end: { line: 1, column: 37 }
			},
			value: "./file.js",
			raw: "\"./file.js\""
		}
	}
]));

test('import Module 1.0 as MyModule', headerItemList([
	{
		type: "QMLImport",
		loc: {
			start: { line: 1, column: 0 },
			end: { line: 1, column: 29 }
		},
		module: {
			type: "QMLModule",
			loc: {
				start: { line: 1, column: 7 },
				end: { line: 1, column: 17 }
			},
			id: {
				type: "QMLQualifiedID",
				loc: {
					start: { line: 1, column: 7 },
					end: { line: 1, column: 13 }
				},
				parts: [{ type: "Identifier", name: "Module" }],
				name: "Module"
			},
			version: {
				type: "QMLVersionLiteral",
				loc: {
					start: { line: 1, column: 14 },
					end: { line: 1, column: 17 }
				},
				value: 1,
				raw: "1.0"
			}
		},
		qualifier: {
			type: "QMLQualifier",
			loc: {
				start: { line: 1, column: 18 },
				end: { line: 1, column: 29 }
			},
			id: {
				type: "Identifier",
				loc: {
					start: { line: 1, column: 21 },
					end: { line: 1, column: 29 }
				},
				name: "MyModule"
			}
		}
	}
]));

test('import Qualified.Id.Test 1.0', headerItemList([
	{
		type: "QMLImport",
		loc: {
			start: { line: 1, column: 0 },
			end: { line: 1, column: 28 }
		},
		module: {
			type: "QMLModule",
			loc: {
				start: { line: 1, column: 7 },
				end: { line: 1, column: 28 }
			},
			id: {
				type: "QMLQualifiedID",
				loc: {
					start: { line: 1, column: 7 },
					end: { line: 1, column: 24 }
				},
				parts: [
					{ type: "Identifier", name: "Qualified" },
					{ type: "Identifier", name: "Id" },
					{ type: "Identifier", name: "Test" }
				],
				name: "Qualified.Id.Test"
			},
			version: {
				type: "QMLVersionLiteral",
				loc: {
					start: { line: 1, column: 25 },
					end: { line: 1, column: 28 }
				},
				value: 1,
				raw: "1.0"
			}
		}
	}
]));

test('pragma Singleton', headerItemList([
	{
		type: "QMLPragma",
		loc: {
			start: { line: 1, column: 0 },
			end: { line: 1, column: 16 }
		},
		id: {
			type: "QMLQualifiedID",
			loc: {
				start: { line: 1, column: 7 },
				end: { line: 1, column: 16 }
			},
			parts: [{ type: "Identifier", name: "Singleton" }],
			name: "Singleton"
		}
	}
]));

test('pragma Singleton\npragma Other', headerItemList([
	{
		type: "QMLPragma",
		loc: {
			start: { line: 1, column: 0 },
			end: { line: 1, column: 16 }
		},
		id: {
			type: "QMLQualifiedID",
			loc: {
				start: { line: 1, column: 7 },
				end: { line: 1, column: 16 }
			},
			parts: [{ type: "Identifier", name: "Singleton" }],
			name: "Singleton"
		}
	},
	{
		type: "QMLPragma",
		loc: {
			start: { line: 2, column: 0 },
			end: { line: 2, column: 12 }
		},
		id: {
			type: "QMLQualifiedID",
			loc: {
				start: { line: 2, column: 7 },
				end: { line: 2, column: 12 }
			},
			parts: [{ type: "Identifier", name: "Other" }],
			name: "Other"
		}
	}
]));

test('pragma Singleton;pragma Other', headerItemList([
	{
		type: "QMLPragma",
		loc: {
			start: { line: 1, column: 0 },
			end: { line: 1, column: 17 }
		},
		id: {
			type: "QMLQualifiedID",
			loc: {
				start: { line: 1, column: 7 },
				end: { line: 1, column: 16 }
			},
			parts: [{ type: "Identifier", name: "Singleton" }],
			name: "Singleton"
		}
	},
	{
		type: "QMLPragma",
		loc: {
			start: { line: 1, column: 17 },
			end: { line: 1, column: 29 }
		},
		id: {
			type: "QMLQualifiedID",
			loc: {
				start: { line: 1, column: 24 },
				end: { line: 1, column: 29 }
			},
			parts: [{ type: "Identifier", name: "Other" }],
			name: "Other"
		}
	}
]));

test('Window {}', rootObject({
	type: "QMLObjectDefinition",
	loc: {
		start: { line: 1, column: 0 },
		end: { line: 1, column: 9 }
	},
	id: {
		type: "QMLQualifiedID",
		loc: {
			start: { line: 1, column: 0 },
			end: { line: 1, column: 6 }
		},
		parts: [{ type: "Identifier", name: "Window" }],
		name: "Window"
	},
	body: {
		type: "QMLObjectInitializer",
		loc: {
			start: { line: 1, column: 7 },
			end: { line: 1, column: 9 }
		},
		members: []
	}
}));

test('QtQuick.Window {}', rootObject({
	type: "QMLObjectDefinition",
	loc: {
		start: { line: 1, column: 0 },
		end: { line: 1, column: 17 }
	},
	id: {
		type: "QMLQualifiedID",
		loc: {
			start: { line: 1, column: 0 },
			end: { line: 1, column: 14 }
		},
		parts: [
			{ type: "Identifier", name: "QtQuick" },
			{ type: "Identifier", name: "Window" }
		],
		name: "QtQuick.Window"
	},
	body: {
		type: "QMLObjectInitializer",
		loc: {
			start: { line: 1, column: 15 },
			end: { line: 1, column: 17 }
		},
		members: []
	}
}));

test('property {}', rootObject({
	type: "QMLObjectDefinition",
	loc: {
		start: { line: 1, column: 0 },
		end: { line: 1, column: 11 }
	},
	id: {
		type: "QMLQualifiedID",
		loc: {
			start: { line: 1, column: 0 },
			end: { line: 1, column: 8 }
		},
		parts: [{ type: "Identifier", name: "property" }],
		name: "property"
	},
	body: {
		type: "QMLObjectInitializer",
		loc: {
			start: { line: 1, column: 9 },
			end: { line: 1, column: 11 }
		},
		members: []
	}
}));

test('readonly {}', rootObject({
	type: "QMLObjectDefinition",
	loc: {
		start: { line: 1, column: 0 },
		end: { line: 1, column: 11 }
	},
	id: {
		type: "QMLQualifiedID",
		loc: {
			start: { line: 1, column: 0 },
			end: { line: 1, column: 8 }
		},
		parts: [{ type: "Identifier", name: "readonly" }],
		name: "readonly"
	},
	body: {
		type: "QMLObjectInitializer",
		loc: {
			start: { line: 1, column: 9 },
			end: { line: 1, column: 11 }
		},
		members: []
	}
}));

test('signal {}', rootObject({
	type: "QMLObjectDefinition",
	loc: {
		start: { line: 1, column: 0 },
		end: { line: 1, column: 9 }
	},
	id: {
		type: "QMLQualifiedID",
		loc: {
			start: { line: 1, column: 0 },
			end: { line: 1, column: 6 }
		},
		parts: [{ type: "Identifier", name: "signal" }],
		name: "signal"
	},
	body: {
		type: "QMLObjectInitializer",
		loc: {
			start: { line: 1, column: 7 },
			end: { line: 1, column: 9 }
		},
		members: []
	}
}));

test('alias {}', rootObject({
	type: "QMLObjectDefinition",
	loc: {
		start: { line: 1, column: 0 },
		end: { line: 1, column: 8 }
	},
	id: {
		type: "QMLQualifiedID",
		loc: {
			start: { line: 1, column: 0 },
			end: { line: 1, column: 5 }
		},
		parts: [{ type: "Identifier", name: "alias" }],
		name: "alias"
	},
	body: {
		type: "QMLObjectInitializer",
		loc: {
			start: { line: 1, column: 6 },
			end: { line: 1, column: 8 }
		},
		members: []
	}
}));

test('list {}', rootObject({
	type: "QMLObjectDefinition",
	loc: {
		start: { line: 1, column: 0 },
		end: { line: 1, column: 7 }
	},
	id: {
		type: "QMLQualifiedID",
		loc: {
			start: { line: 1, column: 0 },
			end: { line: 1, column: 4 }
		},
		parts: [{ type: "Identifier", name: "list" }],
		name: "list"
	},
	body: {
		type: "QMLObjectInitializer",
		loc: {
			start: { line: 1, column: 5 },
			end: { line: 1, column: 7 }
		},
		members: []
	}
}));

test('color {}', rootObject({
	type: "QMLObjectDefinition",
	loc: {
		start: { line: 1, column: 0 },
		end: { line: 1, column: 8 }
	},
	id: {
		type: "QMLQualifiedID",
		loc: {
			start: { line: 1, column: 0 },
			end: { line: 1, column: 5 }
		},
		parts: [{ type: "Identifier", name: "color" }],
		name: "color"
	},
	body: {
		type: "QMLObjectInitializer",
		loc: {
			start: { line: 1, column: 6 },
			end: { line: 1, column: 8 }
		},
		members: []
	}
}));

test('real {}', rootObject({
	type: "QMLObjectDefinition",
	loc: {
		start: { line: 1, column: 0 },
		end: { line: 1, column: 7 }
	},
	id: {
		type: "QMLQualifiedID",
		loc: {
			start: { line: 1, column: 0 },
			end: { line: 1, column: 4 }
		},
		parts: [{ type: "Identifier", name: "real" }],
		name: "real"
	},
	body: {
		type: "QMLObjectInitializer",
		loc: {
			start: { line: 1, column: 5 },
			end: { line: 1, column: 7 }
		},
		members: []
	}
}));

test('string {}', rootObject({
	type: "QMLObjectDefinition",
	loc: {
		start: { line: 1, column: 0 },
		end: { line: 1, column: 9 }
	},
	id: {
		type: "QMLQualifiedID",
		loc: {
			start: { line: 1, column: 0 },
			end: { line: 1, column: 6 }
		},
		parts: [{ type: "Identifier", name: "string" }],
		name: "string"
	},
	body: {
		type: "QMLObjectInitializer",
		loc: {
			start: { line: 1, column: 7 },
			end: { line: 1, column: 9 }
		},
		members: []
	}
}));

test('url {}', rootObject({
	type: "QMLObjectDefinition",
	loc: {
		start: { line: 1, column: 0 },
		end: { line: 1, column: 6 }
	},
	id: {
		type: "QMLQualifiedID",
		loc: {
			start: { line: 1, column: 0 },
			end: { line: 1, column: 3 }
		},
		parts: [{ type: "Identifier", name: "url" }],
		name: "url"
	},
	body: {
		type: "QMLObjectInitializer",
		loc: {
			start: { line: 1, column: 4 },
			end: { line: 1, column: 6 }
		},
		members: []
	}
}));

test('Window {Button {}}', rootObjectMembers([{
	type: "QMLObjectDefinition",
	loc: {
		start: { line: 1, column: 8 },
		end: { line: 1, column: 17 }
	},
	id: {
		type: "QMLQualifiedID",
		loc: {
			start: { line: 1, column: 8 },
			end: { line: 1, column: 14 }
		},
		parts: [{ type: "Identifier", name: "Button" }],
		name: "Button"
	},
	body: {
		type: "QMLObjectInitializer",
		loc: {
			start: { line: 1, column: 15 },
			end: { line: 1, column: 17 }
		},
		members: []
	}
}]));

test('a{ property {} }', rootObjectMembers([{
	type: "QMLObjectDefinition",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 14 }
	},
	id: {
		type: "QMLQualifiedID",
		loc: {
			start: { line: 1, column: 3 },
			end: { line: 1, column: 11 }
		},
		parts: [{ type: "Identifier", name: "property" }],
		name: "property"
	},
	body: {
		type: "QMLObjectInitializer",
		loc: {
			start: { line: 1, column: 12 },
			end: { line: 1, column: 14 }
		},
		members: []
	}
}]));

test('a{ readonly {} }', rootObjectMembers([{
	type: "QMLObjectDefinition",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 14 }
	},
	id: {
		type: "QMLQualifiedID",
		loc: {
			start: { line: 1, column: 3 },
			end: { line: 1, column: 11 }
		},
		parts: [{ type: "Identifier", name: "readonly" }],
		name: "readonly"
	},
	body: {
		type: "QMLObjectInitializer",
		loc: {
			start: { line: 1, column: 12 },
			end: { line: 1, column: 14 }
		},
		members: []
	}
}]));

test('a{ signal {} }', rootObjectMembers([{
	type: "QMLObjectDefinition",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 12 }
	},
	id: {
		type: "QMLQualifiedID",
		loc: {
			start: { line: 1, column: 3 },
			end: { line: 1, column: 9 }
		},
		parts: [{ type: "Identifier", name: "signal" }],
		name: "signal"
	},
	body: {
		type: "QMLObjectInitializer",
		loc: {
			start: { line: 1, column: 10 },
			end: { line: 1, column: 12 }
		},
		members: []
	}
}]));

testFail('a{ readonly property var as: 3 }',
		 "Unexpected token (1:25)",
		 { locations: true, loose: false });

test('a{ readonly property var w: 3 }', rootObjectMembers([{
	type: "QMLPropertyDeclaration",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 29 }
	},
	default: false,
	readonly: true,
	kind: {
		type: "QMLPropertyType",
		loc: {
			start: { line: 1, column: 21 },
			end: { line: 1, column: 24 }
		},
		primitive: true,
		id: { type: "Identifier", name: "var"}
	},
	id: {
		type: "Identifier",
		loc: {
			start: { line: 1, column: 25 },
			end: { line: 1, column: 26 }
		},
		name: "w"
	},
	binding: {
		type: "QMLScriptBinding",
		loc: {
			start: { line: 1, column: 28 },
			end: { line: 1, column: 29 }
		},
		script: {
			type: "Literal",
			value: 3,
			raw: "3"
		}
	}
}]));

test('a{ default property var w: 3 }', rootObjectMembers([{
	type: "QMLPropertyDeclaration",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 28 }
	},
	default: true,
	readonly: false,
	kind: {
		type: "QMLPropertyType",
		loc: {
			start: { line: 1, column: 20 },
			end: { line: 1, column: 23 }
		},
		primitive: true,
		id: { type: "Identifier", name: "var"}
	},
	id: {
		type: "Identifier",
		loc: {
			start: { line: 1, column: 24 },
			end: { line: 1, column: 25 }
		},
		name: "w"
	},
	binding: {
		type: "QMLScriptBinding",
		loc: {
			start: { line: 1, column: 27 },
			end: { line: 1, column: 28 }
		},
		script: {
			type: "Literal",
			value: 3,
			raw: "3"
		}
	}
}]));

testFail('a{ property var public: 3 }',
		"The keyword 'public' is reserved (1:16)",
		{ locations: true, loose: false });

testFail('a{ property var export: 3 }',
		"The keyword 'export' is reserved (1:16)",
		{ locations: true, loose: false });

test('a{ property var w: 3 }', rootObjectMembers([{
	type: "QMLPropertyDeclaration",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 20 }
	},
	default: false,
	readonly: false,
	kind: {
		type: "QMLPropertyType",
		loc: {
			start: { line: 1, column: 12 },
			end: { line: 1, column: 15 }
		},
		primitive: true,
		id: { type: "Identifier", name: "var"}
	},
	id: {
		type: "Identifier",
		loc: {
			start: { line: 1, column: 16 },
			end: { line: 1, column: 17 }
		},
		name: "w"
	},
	binding: {
		type: "QMLScriptBinding",
		loc: {
			start: { line: 1, column: 19 },
			end: { line: 1, column: 20 }
		},
		script: {
			type: "Literal",
			value: 3,
			raw: "3"
		}
	}
}]));

test('a{ property boolean w: 3 }', rootObjectMembers([{
	type: "QMLPropertyDeclaration",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 24 }
	},
	default: false,
	readonly: false,
	kind: {
		type: "QMLPropertyType",
		loc: {
			start: { line: 1, column: 12 },
			end: { line: 1, column: 19 }
		},
		primitive: true,
		id: { type: "Identifier", name: "boolean"}
	},
	id: {
		type: "Identifier",
		loc: {
			start: { line: 1, column: 20 },
			end: { line: 1, column: 21 }
		},
		name: "w"
	},
	binding: {
		type: "QMLScriptBinding",
		loc: {
			start: { line: 1, column: 23 },
			end: { line: 1, column: 24 }
		},
		script: {
			type: "Literal",
			value: 3,
			raw: "3"
		}
	}
}]));

test('a{ property double w: 3 }', rootObjectMembers([{
	type: "QMLPropertyDeclaration",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 23 }
	},
	default: false,
	readonly: false,
	kind: {
		type: "QMLPropertyType",
		loc: {
			start: { line: 1, column: 12 },
			end: { line: 1, column: 18 }
		},
		primitive: true,
		id: { type: "Identifier", name: "double"}
	},
	id: {
		type: "Identifier",
		loc: {
			start: { line: 1, column: 19 },
			end: { line: 1, column: 20 }
		},
		name: "w"
	},
	binding: {
		type: "QMLScriptBinding",
		loc: {
			start: { line: 1, column: 22 },
			end: { line: 1, column: 23 }
		},
		script: {
			type: "Literal",
			value: 3,
			raw: "3"
		}
	}
}]));

test('a{ property int w: 3 }', rootObjectMembers([{
	type: "QMLPropertyDeclaration",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 20 }
	},
	default: false,
	readonly: false,
	kind: {
		type: "QMLPropertyType",
		loc: {
			start: { line: 1, column: 12 },
			end: { line: 1, column: 15 }
		},
		primitive: true,
		id: { type: "Identifier", name: "int"}
	},
	id: {
		type: "Identifier",
		loc: {
			start: { line: 1, column: 16 },
			end: { line: 1, column: 17 }
		},
		name: "w"
	},
	binding: {
		type: "QMLScriptBinding",
		loc: {
			start: { line: 1, column: 19 },
			end: { line: 1, column: 20 }
		},
		script: {
			type: "Literal",
			value: 3,
			raw: "3"
		}
	}
}]));

test('a{ property list<Type> w: 3 }', rootObjectMembers([{
	type: "QMLPropertyDeclaration",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 27 }
	},
	default: false,
	readonly: false,
	kind: {
		type: "QMLPropertyType",
		loc: {
			start: { line: 1, column: 12 },
			end: { line: 1, column: 16 }
		},
		primitive: true,
		id: { type: "Identifier", name: "list"},
	},
	modifier: {
		type: "QMLPropertyType",
		loc: {
			start: { line: 1, column: 17 },
			end: { line: 1, column: 21 }
		},
		primitive: false,
		id: { type: "Identifier", name: "Type" }
	},
	id: {
		type: "Identifier",
		loc: {
			start: { line: 1, column: 23 },
			end: { line: 1, column: 24 }
		},
		name: "w"
	},
	binding: {
		type: "QMLScriptBinding",
		loc: {
			start: { line: 1, column: 26 },
			end: { line: 1, column: 27 }
		},
		script: {
			type: "Literal",
			value: 3,
			raw: "3"
		}
	}
}]));

test('a{ property color w: 3 }', rootObjectMembers([{
	type: "QMLPropertyDeclaration",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 22 }
	},
	default: false,
	readonly: false,
	kind: {
		type: "QMLPropertyType",
		loc: {
			start: { line: 1, column: 12 },
			end: { line: 1, column: 17 }
		},
		primitive: true,
		id: { type: "Identifier", name: "color"}
	},
	id: {
		type: "Identifier",
		loc: {
			start: { line: 1, column: 18 },
			end: { line: 1, column: 19 }
		},
		name: "w"
	},
	binding: {
		type: "QMLScriptBinding",
		loc: {
			start: { line: 1, column: 21 },
			end: { line: 1, column: 22 }
		},
		script: {
			type: "Literal",
			value: 3,
			raw: "3"
		}
	}
}]));

test('a{ property real w: 3 }', rootObjectMembers([{
	type: "QMLPropertyDeclaration",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 21 }
	},
	default: false,
	readonly: false,
	kind: {
		type: "QMLPropertyType",
		loc: {
			start: { line: 1, column: 12 },
			end: { line: 1, column: 16 }
		},
		primitive: true,
		id: { type: "Identifier", name: "real"}
	},
	id: {
		type: "Identifier",
		loc: {
			start: { line: 1, column: 17 },
			end: { line: 1, column: 18 }
		},
		name: "w"
	},
	binding: {
		type: "QMLScriptBinding",
		loc: {
			start: { line: 1, column: 20 },
			end: { line: 1, column: 21 }
		},
		script: {
			type: "Literal",
			value: 3,
			raw: "3"
		}
	}
}]));

test('a{ property string w: 3 }', rootObjectMembers([{
	type: "QMLPropertyDeclaration",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 23 }
	},
	default: false,
	readonly: false,
	kind: {
		type: "QMLPropertyType",
		loc: {
			start: { line: 1, column: 12 },
			end: { line: 1, column: 18 }
		},
		primitive: true,
		id: { type: "Identifier", name: "string"}
	},
	id: {
		type: "Identifier",
		loc: {
			start: { line: 1, column: 19 },
			end: { line: 1, column: 20 }
		},
		name: "w"
	},
	binding: {
		type: "QMLScriptBinding",
		loc: {
			start: { line: 1, column: 22 },
			end: { line: 1, column: 23 }
		},
		script: {
			type: "Literal",
			value: 3,
			raw: "3"
		}
	}
}]));

test('a{ property url w: 3 }', rootObjectMembers([{
	type: "QMLPropertyDeclaration",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 20 }
	},
	default: false,
	readonly: false,
	kind: {
		type: "QMLPropertyType",
		loc: {
			start: { line: 1, column: 12 },
			end: { line: 1, column: 15 }
		},
		primitive: true,
		id: { type: "Identifier", name: "url"}
	},
	id: {
		type: "Identifier",
		loc: {
			start: { line: 1, column: 16 },
			end: { line: 1, column: 17 }
		},
		name: "w"
	},
	binding: {
		type: "QMLScriptBinding",
		loc: {
			start: { line: 1, column: 19 },
			end: { line: 1, column: 20 }
		},
		script: {
			type: "Literal",
			value: 3,
			raw: "3"
		}
	}
}]));


test('a{ property QtObject w: 3 }', rootObjectMembers([{
	type: "QMLPropertyDeclaration",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 25 }
	},
	default: false,
	readonly: false,
	kind: {
		type: "QMLPropertyType",
		loc: {
			start: { line: 1, column: 12 },
			end: { line: 1, column: 20 }
		},
		primitive: false,
		id: { type: "Identifier", name: "QtObject"}
	},
	id: {
		type: "Identifier",
		loc: {
			start: { line: 1, column: 21 },
			end: { line: 1, column: 22 }
		},
		name: "w"
	},
	binding: {
		type: "QMLScriptBinding",
		loc: {
			start: { line: 1, column: 24 },
			end: { line: 1, column: 25 }
		},
		script: {
			type: "Literal",
			value: 3,
			raw: "3"
		}
	}
}]));

test('a{ property alias w: 3 }', rootObjectMembers([{
	type: "QMLPropertyDeclaration",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 22 }
	},
	default: false,
	readonly: false,
	kind: {
		type: "QMLPropertyType",
		loc: {
			start: { line: 1, column: 12 },
			end: { line: 1, column: 17 }
		},
		primitive: true,
		id: { type: "Identifier", name: "alias"}
	},
	id: {
		type: "Identifier",
		loc: {
			start: { line: 1, column: 18 },
			end: { line: 1, column: 19 }
		},
		name: "w"
	},
	binding: {
		type: "QMLScriptBinding",
		loc: {
			start: { line: 1, column: 21 },
			end: { line: 1, column: 22 }
		},
		script: {
			type: "Literal",
			value: 3,
			raw: "3"
		}
	}
}]));

test('a{ w: 3 }', rootObjectMembers([{
	type: "QMLPropertyBinding",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 7 }
	},
	id: {
		type: "QMLQualifiedID",
		loc: {
			start: { line: 1, column: 3 },
			end: { line: 1, column: 4 }
		},
		parts: [{ type: "Identifier", name: "w" }],
		name: "w"
	},
	binding: {
		type: "QMLScriptBinding",
		loc: {
			start: { line: 1, column: 6 },
			end: { line: 1, column: 7 }
		},
		script: {
			type: "Literal",
			value: 3,
			raw: "3"
		}
	}
}]));

test('a{ x.y.z: 3 }', rootObjectMembers([{
	type: "QMLPropertyBinding",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 11 }
	},
	id: {
		type: "QMLQualifiedID",
		loc: {
			start: { line: 1, column: 3 },
			end: { line: 1, column: 8 }
		},
		parts: [
			{ type: "Identifier", name: "x" },
			{ type: "Identifier", name: "y" },
			{ type: "Identifier", name: "z" }
		],
		name: "x.y.z"
	},
	binding: {
		type: "QMLScriptBinding",
		loc: {
			start: { line: 1, column: 10 },
			end: { line: 1, column: 11 }
		},
		script: {
			type: "Literal",
			value: 3,
			raw: "3"
		}
	}
}]));

testFail('a{ import: 3 }',
		 "Unexpected token (1:3)",
		{ locations: true, loose: false });

testFail('a{ pragma: 3 }',
		 "Unexpected token (1:3)",
		 { locations: true, loose: false });

testFail('a{ as: 3 }',
		 "Unexpected token (1:3)",
		 { locations: true, loose: false });

testFail('a{ boolean: 3 }',
		 "Unexpected token (1:3)",
		 { locations: true, loose: false });

testFail('a{ double: 3 }',
		 "Unexpected token (1:3)",
		 { locations: true, loose: false });

testFail('a{ int: 3 }',
		 "Unexpected token (1:3)",
		 { locations: true, loose: false });

test('a{ alias: 3 }', rootObjectMembers([{
	type: "QMLPropertyBinding",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 11 }
	},
	id: {
		type: "QMLQualifiedID",
		loc: {
			start: { line: 1, column: 3 },
			end: { line: 1, column: 8 }
		},
		parts: [{ type: "Identifier", name: "alias" }],
		name: "alias"
	},
	binding: {
		type: "QMLScriptBinding",
		loc: {
			start: { line: 1, column: 10 },
			end: { line: 1, column: 11 }
		},
		script: {
			type: "Literal",
			value: 3,
			raw: "3"
		}
	}
}]));

test('a{ list: 3 }', rootObjectMembers([{
	type: "QMLPropertyBinding",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 10 }
	},
	id: {
		type: "QMLQualifiedID",
		loc: {
			start: { line: 1, column: 3 },
			end: { line: 1, column: 7 }
		},
		parts: [{ type: "Identifier", name: "list" }],
		name: "list"
	},
	binding: {
		type: "QMLScriptBinding",
		loc: {
			start: { line: 1, column: 9 },
			end: { line: 1, column: 10 }
		},
		script: {
			type: "Literal",
			value: 3,
			raw: "3"
		}
	}
}]));

test('a{ property: 3 }', rootObjectMembers([{
	type: "QMLPropertyBinding",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 14 }
	},
	id: {
		type: "QMLQualifiedID",
		loc: {
			start: { line: 1, column: 3 },
			end: { line: 1, column: 11 }
		},
		parts: [{ type: "Identifier", name: "property" }],
		name: "property"
	},
	binding: {
		type: "QMLScriptBinding",
		loc: {
			start: { line: 1, column: 13 },
			end: { line: 1, column: 14 }
		},
		script: {
			type: "Literal",
			value: 3,
			raw: "3"
		}
	}
}]));

test('a{ readonly: 3 }', rootObjectMembers([{
	type: "QMLPropertyBinding",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 14 }
	},
	id: {
		type: "QMLQualifiedID",
		loc: {
			start: { line: 1, column: 3 },
			end: { line: 1, column: 11 }
		},
		parts: [{ type: "Identifier", name: "readonly" }],
		name: "readonly"
	},
	binding: {
		type: "QMLScriptBinding",
		loc: {
			start: { line: 1, column: 13 },
			end: { line: 1, column: 14 }
		},
		script: {
			type: "Literal",
			value: 3,
			raw: "3"
		}
	}
}]));

test('a{ signal: 3 }', rootObjectMembers([{
	type: "QMLPropertyBinding",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 12 }
	},
	id: {
		type: "QMLQualifiedID",
		loc: {
			start: { line: 1, column: 3 },
			end: { line: 1, column: 9 }
		},
		parts: [{ type: "Identifier", name: "signal" }],
		name: "signal"
	},
	binding: {
		type: "QMLScriptBinding",
		loc: {
			start: { line: 1, column: 11 },
			end: { line: 1, column: 12 }
		},
		script: {
			type: "Literal",
			value: 3,
			raw: "3"
		}
	}
}]));

test('a{ color: 3 }', rootObjectMembers([{
	type: "QMLPropertyBinding",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 11 }
	},
	id: {
		type: "QMLQualifiedID",
		loc: {
			start: { line: 1, column: 3 },
			end: { line: 1, column: 8 }
		},
		parts: [{ type: "Identifier", name: "color" }],
		name: "color"
	},
	binding: {
		type: "QMLScriptBinding",
		loc: {
			start: { line: 1, column: 10 },
			end: { line: 1, column: 11 }
		},
		script: {
			type: "Literal",
			value: 3,
			raw: "3"
		}
	}
}]));

test('a{ real: 3 }', rootObjectMembers([{
	type: "QMLPropertyBinding",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 10 }
	},
	id: {
		type: "QMLQualifiedID",
		loc: {
			start: { line: 1, column: 3 },
			end: { line: 1, column: 7 }
		},
		parts: [{ type: "Identifier", name: "real" }],
		name: "real"
	},
	binding: {
		type: "QMLScriptBinding",
		loc: {
			start: { line: 1, column: 9 },
			end: { line: 1, column: 10 }
		},
		script: {
			type: "Literal",
			value: 3,
			raw: "3"
		}
	}
}]));

test('a{ string: 3 }', rootObjectMembers([{
	type: "QMLPropertyBinding",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 12 }
	},
	id: {
		type: "QMLQualifiedID",
		loc: {
			start: { line: 1, column: 3 },
			end: { line: 1, column: 9 }
		},
		parts: [{ type: "Identifier", name: "string" }],
		name: "string"
	},
	binding: {
		type: "QMLScriptBinding",
		loc: {
			start: { line: 1, column: 11 },
			end: { line: 1, column: 12 }
		},
		script: {
			type: "Literal",
			value: 3,
			raw: "3"
		}
	}
}]));

test('a{ url: 3 }', rootObjectMembers([{
	type: "QMLPropertyBinding",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 9 }
	},
	id: {
		type: "QMLQualifiedID",
		loc: {
			start: { line: 1, column: 3 },
			end: { line: 1, column: 6 }
		},
		parts: [{ type: "Identifier", name: "url" }],
		name: "url"
	},
	binding: {
		type: "QMLScriptBinding",
		loc: {
			start: { line: 1, column: 8 },
			end: { line: 1, column: 9 }
		},
		script: {
			type: "Literal",
			value: 3,
			raw: "3"
		}
	}
}]));

test('a{ onClicked: Qt.quit(0) }', rootObjectMembers([{
	type: "QMLPropertyBinding",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 24 }
	},
	id: {
		type: "QMLQualifiedID",
		loc: {
			start: { line: 1, column: 3 },
			end: { line: 1, column: 12 }
		},
		parts: [{ type: "Identifier", name: "onClicked" }],
		name: "onClicked"
	},
	binding: {
		type: "QMLScriptBinding",
		loc: {
			start: { line: 1, column: 14 },
			end: { line: 1, column: 24 }
		},
		script: {
			type: "CallExpression",
			callee: {
				type: "MemberExpression",
				loc: {
					start: { line: 1, column: 14 },
					end: { line: 1, column: 21 }
				},
				object: {
					type: "Identifier",
					loc: {
						start: { line: 1, column: 14 },
						end: { line: 1, column: 16 }
					},
					name: "Qt"
				},
				property: {
					type: "Identifier",
					loc: {
						start: { line: 1, column: 17 },
						end: { line: 1, column: 21 }
					},
					name: "quit"
				},
				computed: false
			},
			arguments: [{
				type: "Literal",
				loc: {
					start: { line: 1, column: 22 },
					end: { line: 1, column: 23 }
				},
				value: 0,
				raw: "0"
			}]
		}
	}
}]));

test('a{b:pragma}', javaScript({
	type: "Identifier",
	loc: {
		start: { line: 1, column: 4 },
		end: { line: 1, column: 10 }
	},
	name: "pragma"
}));

test('a{b:property}', javaScript({
	type: "Identifier",
	loc: {
		start: { line: 1, column: 4 },
		end: { line: 1, column: 12 }
	},
	name: "property"
}));

test('a{b:readonly}', javaScript({
	type: "Identifier",
	loc: {
		start: { line: 1, column: 4 },
		end: { line: 1, column: 12 }
	},
	name: "readonly"
}));

test('a{b:signal}', javaScript({
	type: "Identifier",
	loc: {
		start: { line: 1, column: 4 },
		end: { line: 1, column: 10 }
	},
	name: "signal"
}));

test('a{b:alias}', javaScript({
	type: "Identifier",
	loc: {
		start: { line: 1, column: 4 },
		end: { line: 1, column: 9 }
	},
	name: "alias"
}));

test('a{b:list}', javaScript({
	type: "Identifier",
	loc: {
		start: { line: 1, column: 4 },
		end: { line: 1, column: 8 }
	},
	name: "list"
}));

test('a{b:color}', javaScript({
	type: "Identifier",
	loc: {
		start: { line: 1, column: 4 },
		end: { line: 1, column: 9 }
	},
	name: "color"
}));

test('a{b:real}', javaScript({
	type: "Identifier",
	loc: {
		start: { line: 1, column: 4 },
		end: { line: 1, column: 8 }
	},
	name: "real"
}));

test('a{b:string}', javaScript({
	type: "Identifier",
	loc: {
		start: { line: 1, column: 4 },
		end: { line: 1, column: 10 }
	},
	name: "string"
}));

test('a{b:url}', javaScript({
	type: "Identifier",
	loc: {
		start: { line: 1, column: 4 },
		end: { line: 1, column: 7 }
	},
	name: "url"
}));

test('a{b:[]}', javaScript({
	type: "ArrayExpression",
	loc: {
		start: { line: 1, column: 4 },
		end: { line: 1, column: 6 }
	},
	elements: []
}));

test('a{b:[{}]}', javaScript({
	type: "ArrayExpression",
	loc: {
		start: { line: 1, column: 4 },
		end: { line: 1, column: 8 }
	},
	elements: [{
		type: "ObjectExpression",
		loc: {
			start: { line: 1, column: 5 },
			end: { line: 1, column: 7 }
		},
	}]
}));

test('a{ function fn() {} }', rootObjectMembers([{
	type: "FunctionDeclaration",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 19 }
	},
	id: {
		type: "Identifier",
		loc: {
			start: { line: 1, column: 12 },
			end: { line: 1, column: 14 }
		},
		name: "fn"
	},
	params: [],
	body: {
		type: "BlockStatement",
		loc: {
			start: { line: 1, column: 17 },
			end: { line: 1, column: 19 }
		},
		body: []
	}
}]));

test('a{ function add(a, b) { return a + b } }', rootObjectMembers([{
	type: "FunctionDeclaration",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 38 }
	},
	id: {
		type: "Identifier",
		name: "add"
	},
	params: [
		{ type: "Identifier", name: "a" },
		{ type: "Identifier", name: "b" }
	],
	body: {
		type: "BlockStatement",
		loc: {
			start: { line: 1, column: 22 },
			end: { line: 1, column: 38 }
		},
		body: [{
			type: "ReturnStatement",
			argument: {
				type: "BinaryExpression",
				left: { type: "Identifier", name: "a" },
				operator: "+",
				right: { type: "Identifier", name: "b" }
			}
		}]
	}
}]));

testFail('a{ function () {} }',
		"Unexpected token (1:12)",
		{ locations: true, loose: false });

test('a{ signal b }', rootObjectMembers([{
	type: "QMLSignalDefinition",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 11 }
	},
	id: {
		type: "Identifier",
		loc: {
			start: { line: 1, column: 10 },
			end: { line: 1, column: 11 }
		},
		name: "b"
	},
	params: []
}]));

test('a{ signal b () }', rootObjectMembers([{
	type: "QMLSignalDefinition",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 14 }
	},
	id: {
		type: "Identifier",
		loc: {
			start: { line: 1, column: 10 },
			end: { line: 1, column: 11 }
		},
		name: "b"
	},
	params: []
}]));

test('a{ signal b (type param)}', rootObjectMembers([{
	type: "QMLSignalDefinition",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 24 }
	},
	id: {
		type: "Identifier",
		loc: {
			start: { line: 1, column: 10 },
			end: { line: 1, column: 11 }
		},
		name: "b"
	},
	params: [{
		type: "QMLParameter",
		loc: {
			start: { line: 1, column: 13 },
			end: { line: 1, column: 23 }
		},
		kind: {
			type: "QMLPropertyType",
			loc: {
				start: { line: 1, column: 13 },
				end: { line: 1, column: 17 }
			},
			primitive: false,
			id: { type: "Identifier", name: "type" }
		},
		id: {
			type: "Identifier",
			loc: {
				start: { line: 1, column: 18 },
				end: { line: 1, column: 23 }
			},
			name: "param"
		}
	}]
}]));

test('a{ signal b (type1 param1, type2 param2, type3 param3)}', rootObjectMembers([{
	type: "QMLSignalDefinition",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 54 }
	},
	id: {
		type: "Identifier",
		loc: {
			start: { line: 1, column: 10 },
			end: { line: 1, column: 11 }
		},
		name: "b"
	},
	params: [
		{
			type: "QMLParameter",
			loc: {
				start: { line: 1, column: 13 },
				end: { line: 1, column: 25 }
			},
			kind: {
				type: "QMLPropertyType",
				loc: {
					start: { line: 1, column: 13 },
					end: { line: 1, column: 18 }
				},
				primitive: false,
				id: { type: "Identifier", name: "type1" }
			},
			id: {
				type: "Identifier",
				loc: {
					start: { line: 1, column: 19 },
					end: { line: 1, column: 25 }
				},
				name: "param1"
			}
		},
		{
			type: "QMLParameter",
			loc: {
				start: { line: 1, column: 27 },
				end: { line: 1, column: 39 }
			},
			kind: {
				type: "QMLPropertyType",
				loc: {
					start: { line: 1, column: 27 },
					end: { line: 1, column: 32 }
				},
				primitive: false,
				id: { type: "Identifier", name: "type2" }
			},
			id: {
				type: "Identifier",
				loc: {
					start: { line: 1, column: 33 },
					end: { line: 1, column: 39 }
				},
				name: "param2"
			}
		},
		{
			type: "QMLParameter",
			loc: {
				start: { line: 1, column: 41 },
				end: { line: 1, column: 53 }
			},
			kind: {
				type: "QMLPropertyType",
				loc: {
					start: { line: 1, column: 41 },
					end: { line: 1, column: 46 }
				},
				primitive: false,
				id: { type: "Identifier", name: "type3" }
			},
			id: {
				type: "Identifier",
				loc: {
					start: { line: 1, column: 47 },
					end: { line: 1, column: 53 }
				},
				name: "param3"
			}
		}
	]
}]));

test('a{ id: test }', rootObjectMembers([{
	type: "QMLPropertyBinding",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 11 }
	},
	id: {
		type: "QMLQualifiedID",
		loc: {
			start: { line: 1, column: 3 },
			end: { line: 1, column: 5 }
		},
		parts: [{
			type: "Identifier",
			loc: {
				start: { line: 1, column: 3 },
				end: { line: 1, column: 5 }
			},
			name: "id"
		}],
		name: "id"
	},
	binding: {
		type: "QMLScriptBinding",
		loc: {
			start: { line: 1, column: 7 },
			end: { line: 1, column: 11 }
		},
		script: {
			type: "Identifier",
			name: "test"
		}
	}
}]));

/*
* Test the base QML Hello World program created by Eclipse CDT.
*/
test('import QtQuick 2.3\nimport QtQuick.Window 2.2\nWindow {\n\tvisible: true\n\n\tMouseArea {\n\t\tanchors.fill: parent\n\t\tonClicked: {\n\t\t\tQt.quit();\n\t\t}\n\t}\n\tText {\n\t\ttext: qsTr("Hello World")\n\t\tanchors.centerIn: parent\n\t}\n}',
	program([{
		type: "QMLImport",
		loc: {
			start: { line: 1, column: 0
			},
			end: { line: 1, column: 18 }
		},
		module: {
			type: "QMLModule",
			loc: {
				start: { line: 1, column: 7 },
				end: { line: 1, column: 18 }
			},
			id: {
				type: "QMLQualifiedID",
				loc: {
					start: { line: 1, column: 7 },
					end: { line: 1, column: 14 }
				},
				parts: [{ type: "Identifier", name: "QtQuick" }],
				name: "QtQuick"
			},
			version: {
				type: "QMLVersionLiteral",
				loc: {
					start: { line: 1, column: 15 },
					end: { line: 1, column: 18 }
				},
				raw: "2.3",
			}
		}
	},
	{
		type: "QMLImport",
		loc: {
			start: { line: 2, column: 0 },
			end: { line: 2, column: 25 }
		},
		module: {
			type: "QMLModule",
			loc: {
				start: { line: 2, column: 7 },
				end: { line: 2, column: 25 }
			},
			id: {
				type: "QMLQualifiedID",
				loc: {
					start: { line: 2, column: 7 },
					end: { line: 2, column: 21 }
				},
				parts: [
					{ type: "Identifier", name: "QtQuick" },
					{ type: "Identifier", name: "Window" }
				],
				name: "QtQuick.Window"
			},
			version: {
				type: "QMLVersionLiteral",
				loc: {
					start: { line: 2, column: 22 },
					end: { line: 2, column: 25 }
				},
				raw: "2.2",
			}
		}
	}],{
		type: "QMLObjectDefinition",
		loc: {
			start: { line: 3, column: 0 },
			end: { line: 16, column: 1 }
		},
		id: {
			type: "QMLQualifiedID",
			loc: {
				start: { line: 3, column: 0 },
				end: { line: 3, column: 6 }
			},
			parts: [{ type: "Identifier", name: "Window" }],
			name: "Window"
		},
		body: {
			type: "QMLObjectInitializer",
			loc: {
				start: { line: 3, column: 7 },
				end: { line: 16, column: 1 }
			},
			members: [
				{
					type: "QMLPropertyBinding",
					loc: {
						start: { line: 4, column: 1 },
						end: { line: 4, column: 14 }
					},
					id: {
						type: "QMLQualifiedID",
						loc: {
							start: { line: 4, column: 1 },
							end: { line: 4, column: 8 }
						},
						parts: [{ type: "Identifier", name: "visible" }],
						name: "visible"
					},
					binding: {
						type: "QMLScriptBinding",
						loc: {
							start: { line: 4, column: 10 },
							end: { line: 4, column: 14 }
						},
						script: {
							type: "Literal",
							value: true,
							raw: "true"
						}
					}
				},
				{
					type: "QMLObjectDefinition",
					loc: {
						start: { line: 6, column: 1 },
						end: { line: 11, column: 2 }
					},
					id: {
						type: "QMLQualifiedID",
						loc: {
							start: { line: 6, column: 1 },
							end: { line: 6, column: 10 }
						},
						parts: [{ type: "Identifier", name: "MouseArea" }],
						name: "MouseArea"
					},
					body: {
						type: "QMLObjectInitializer",
						loc: {
							start: { line: 6, column: 11 },
							end: { line: 11, column: 2 }
						},
						members: [
							{
								type: "QMLPropertyBinding",
								loc: {
									start: { line: 7, column: 2 },
									end: { line: 7, column: 22 }
								},
								id: {
									type: "QMLQualifiedID",
									loc: {
										start: { line: 7, column: 2 },
										end: { line: 7, column: 14 }
									},
									parts: [
										{ type: "Identifier", name: "anchors" },
										{ type: "Identifier", name: "fill" }
									],
									name: "anchors.fill"
								},
								binding: {
									type: "QMLScriptBinding",
									loc: {
										start: { line: 7, column: 16 },
										end: { line: 7, column: 22 }
									},
									script: {
										type: "Identifier",
										name: "parent"
									}
								}
							},
							{
								type: "QMLPropertyBinding",
								loc: {
									start: { line: 8, column: 2 },
									end: { line: 10, column: 3 }
								},
								id: {
									type: "QMLQualifiedID",
									loc: {
										start: { line: 8, column: 2 },
										end: { line: 8, column: 11 }
									},
									parts: [{ type: "Identifier", name: "onClicked" }],
									name: "onClicked"
								},
								binding: {
									type: "QMLScriptBinding",
									loc: {
										start: { line: 8, column: 13 },
										end: { line: 10, column: 3 }
									},
									script: {
										type: "QMLStatementBlock",
										body: [{
											type: "ExpressionStatement",
											expression: {
												type: "CallExpression",
												callee: {
													type: "MemberExpression",
													object: {
														type: "Identifier",
														name: "Qt"
													},
													property: {
														type: "Identifier",
														name: "quit"
													},
													computed: false
												},
												arguments: []
											}
										}]
									}
								}
							}
						]
					}
				},
				{
					type: "QMLObjectDefinition",
					loc: {
						start: { line: 12, column: 1 },
						end: { line: 15, column: 2 }
					},
					id: {
						type: "QMLQualifiedID",
						loc: {
							start: { line: 12, column: 1 },
							end: { line: 12, column: 5 }
						},
						parts: [{ type: "Identifier", name: "Text" }],
						name: "Text"
					},
					body: {
						type: "QMLObjectInitializer",
						loc: {
							start: { line: 12, column: 6 },
							end: { line: 15, column: 2 }
						},
						members: [
							{
								type: "QMLPropertyBinding",
								loc: {
									start: { line: 13, column: 2 },
									end: { line: 13, column: 27 }
								},
								id: {
									type: "QMLQualifiedID",
									loc: {
										start: { line: 13, column: 2 },
										end: { line: 13, column: 6 }
									},
									parts: [{ type: "Identifier", name: "text" }],
									name: "text"
								},
								binding: {
									type: "QMLScriptBinding",
									loc: {
										start: { line: 13, column: 8 },
										end: { line: 13, column: 27 }
									},
									script: {
										type: "CallExpression",
										callee: {
											type: "Identifier",
											name: "qsTr"
										},
										arguments: [{
											type: "Literal",
											value: "Hello World",
											raw: "\"Hello World\""
										}]
									}
								}
							},
							{
								type: "QMLPropertyBinding",
								loc: {
									start: { line: 14, column: 2 },
									end: { line: 14, column: 26 }
								},
								id: {
									type: "QMLQualifiedID",
									loc: {
										start: { line: 14, column: 2 },
										end: { line: 14, column: 18 }
									},
									parts: [
										{ type: "Identifier", name: "anchors" },
										{ type: "Identifier", name: "centerIn" }
									],
									name: "anchors.centerIn"
								},
								binding: {
									type: "QMLScriptBinding",
									script: {
										type: "Identifier",
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
), { locations: true, qmltypes: false });

/***************************************************************************
*                            Loose Parser Tests                            *
****************************************************************************/
function testLoose(code, ast, options) {
	var opts = options || {};
	opts.loose = true;
	opts.normal = false;
	opts.locations = true;
	test(code, ast, opts);
}

testLoose('import QtQuick', headerItemList([{
	type: "QMLImport",
	loc: {
			start: { line: 1, column: 0 },
			end: { line: 1, column: 14 }
		},
	module: {
		type: "QMLModule",
		loc: {
			start: { line: 1, column: 7 },
			end: { line: 1, column: 14 }
		},
		id: {
			type: "QMLQualifiedID",
			loc: {
				start: { line: 1, column: 7 },
				end: { line: 1, column: 14 }
			},
			parts: [{ type: "Identifier", name: "QtQuick" }],
			name: "QtQuick"
		},
		version: {
			type: "QMLVersionLiteral",
			loc: {
				start: { line: 1, column: 14 },
				end: { line: 1, column: 14 }
			},
			value: 0,
			raw: "0.0"
		}
	}
}]));

testLoose('import ', headerItemList([{
	type: "QMLImport",
	loc: {
			start: { line: 1, column: 0 },
			end: { line: 1, column: 7 }
		},
	module: {
		type: "QMLModule",
		loc: {
			start: { line: 1, column: 7 },
			end: { line: 1, column: 7 }
		},
		id: {
			type: "QMLQualifiedID",
			loc: {
				start: { line: 1, column: 7 },
				end: { line: 1, column: 7 }
			},
			parts: [{ type: "Identifier", name: "✖" }],
			name: "✖"
		},
		version: {
			type: "QMLVersionLiteral",
			loc: {
				start: { line: 1, column: 7 },
				end: { line: 1, column: 7 }
			},
			value: 0,
			raw: "0.0"
		}
	}
}]));

testLoose('import QtQuick 0x01', headerItemList([{
	type: "QMLImport",
	loc: {
			start: { line: 1, column: 0 },
			end: { line: 1, column: 19 }
		},
	module: {
		type: "QMLModule",
		loc: {
			start: { line: 1, column: 7 },
			end: { line: 1, column: 19 }
		},
		id: {
			type: "QMLQualifiedID",
			loc: {
				start: { line: 1, column: 7 },
				end: { line: 1, column: 14 }
			},
			parts: [{ type: "Identifier", name: "QtQuick" }],
			name: "QtQuick"
		},
		version: {
			type: "QMLVersionLiteral",
			loc: {
				start: { line: 1, column: 15 },
				end: { line: 1, column: 19 }
			},
			value: 1,
			raw: "0x01"
		}
	}
}]));

testLoose('import QtQuick 1', headerItemList([{
	type: "QMLImport",
	loc: {
			start: { line: 1, column: 0 },
			end: { line: 1, column: 16 }
		},
	module: {
		type: "QMLModule",
		loc: {
			start: { line: 1, column: 7 },
			end: { line: 1, column: 16 }
		},
		id: {
			type: "QMLQualifiedID",
			loc: {
				start: { line: 1, column: 7 },
				end: { line: 1, column: 14 }
			},
			parts: [{ type: "Identifier", name: "QtQuick" }],
			name: "QtQuick"
		},
		version: {
			type: "QMLVersionLiteral",
			loc: {
				start: { line: 1, column: 15 },
				end: { line: 1, column: 16 }
			},
			value: 1,
			raw: "1"
		}
	}
}]));

testLoose('import "./file.js', headerItemList([{
	type: "QMLImport",
	loc: {
		start: { line: 1, column: 0 },
		end: { line: 1, column: 17 }
	},
	directory: {
		type: "Literal",
		loc: {
			start: { line: 1, column: 7 },
			end: { line: 1, column: 17 }
		},
		value: "./file.js",
		raw: "\"./file.js"
	}
}]));

testLoose('import QtQuick 2.2 as ', headerItemList([{
	type: "QMLImport",
	loc: {
			start: { line: 1, column: 0 },
			end: { line: 1, column: 22 }
		},
	module: {
		type: "QMLModule",
		loc: {
			start: { line: 1, column: 7 },
			end: { line: 1, column: 18 }
		},
		id: {
			type: "QMLQualifiedID",
			loc: {
				start: { line: 1, column: 7 },
				end: { line: 1, column: 14 }
			},
			parts: [{ type: "Identifier", name: "QtQuick" }],
			name: "QtQuick"
		},
		version: {
			type: "QMLVersionLiteral",
			loc: {
				start: { line: 1, column: 15 },
				end: { line: 1, column: 18 }
			},
			value: 2.2,
			raw: "2.2"
		}
	},
	qualifier: {
		type: "QMLQualifier",
		loc: {
			start: { line: 1, column: 19 },
			end: { line: 1, column: 22 }
		},
		id: {
			type: "Identifier",
			loc: {
				start: { line: 1, column: 22 },
				end: { line: 1, column: 22 }
			},
			name: "✖"
		}
	}
}]));

testLoose('Window {', rootObject({
	type: "QMLObjectDefinition",
	loc: {
		start: { line: 1, column: 0 },
		end: { line: 1, column: 8 }
	},
	id: {
		type: "QMLQualifiedID",
		loc: {
			start: { line: 1, column: 0 },
			end: { line: 1, column: 6 }
		},
		parts: [{ type: "Identifier", name: "Window" }],
		name: "Window"
	},
	body: {
		type: "QMLObjectInitializer",
		loc: {
			start: { line: 1, column: 7 },
			end: { line: 1, column: 8 }
		},
		members: []
	}
}));

testLoose('Window {\n\tprop: 3', rootObject({
	type: "QMLObjectDefinition",
	loc: {
		start: { line: 1, column: 0 },
		end: { line: 2, column: 8 }
	},
	id: {
		type: "QMLQualifiedID",
		loc: {
			start: { line: 1, column: 0 },
			end: { line: 1, column: 6 }
		},
		parts: [{ type: "Identifier", name: "Window" }],
		name: "Window"
	},
	body: {
		type: "QMLObjectInitializer",
		loc: {
			start: { line: 1, column: 7 },
			end: { line: 2, column: 8 }
		},
		members: [{
			type: "QMLPropertyBinding",
			loc: {
				start: { line: 2, column: 1 },
				end: { line: 2, column: 8 }
			},
			id: {
				type: "QMLQualifiedID",
				loc: {
					start: { line: 2, column: 1 },
					end: { line: 2, column: 5 }
				},
				parts: [{ type: "Identifier", name: "prop" }],
				name: "prop"
			},
			binding: {
				type: "QMLScriptBinding",
				loc: {
					start: { line: 2, column: 7 },
					end: { line: 2, column: 8 }
				},
				script: {
					type: "Literal",
					value: 3,
					raw: "3"
				}
			}
		}]
	}
}));

testLoose('a {\n\tb {\n\n\tc {\n}', rootObject({
	type: "QMLObjectDefinition",
	loc: {
		start: { line: 1, column: 0 },
		end: { line: 5, column: 1 }
	},
	id: {
		type: "QMLQualifiedID",
		loc: {
			start: { line: 1, column: 0 },
			end: { line: 1, column: 1 }
		},
		parts: [{ type: "Identifier", name: "a" }],
		name: "a"
	},
	body: {
		type: "QMLObjectInitializer",
		loc: {
			start: { line: 1, column: 2 },
			end: { line: 5, column: 1 }
		},
		members: [{
			type: "QMLObjectDefinition",
			loc: {
				start: { line: 2, column: 1 },
				end: { line: 5, column: 1 }
			},
			id: {
				type: "QMLQualifiedID",
				loc: {
					start: { line: 2, column: 1 },
					end: { line: 2, column: 2 }
				},
				parts: [{ type: "Identifier", name: "b" }],
				name: "b"
			},
			body: {
				type: "QMLObjectInitializer",
				loc: {
					start: { line: 2, column: 3 },
					end: { line: 5, column: 1 }
				},
				members: [{
					type: "QMLObjectDefinition",
					loc: {
						start: { line: 4, column: 1 },
						end: { line: 5, column: 1 }
					},
					id: {
						type: "QMLQualifiedID",
						loc: {
							start: { line: 4, column: 1 },
							end: { line: 4, column: 2 }
						},
						parts: [{ type: "Identifier", name: "c" }],
						name: "c"
					},
					body: {
						type: "QMLObjectInitializer",
						loc: {
							start: { line: 4, column: 3 },
							end: { line: 5, column: 1 }
						},
						members: []
					}
				}]
			}
		}]
	}
}));

testLoose('a{ property var  }', rootObjectMembers([{
	type: "QMLPropertyDeclaration",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 17 }
	},
	default: false,
	readonly: false,
	kind: {
		type: "QMLPropertyType",
		loc: {
			start: { line: 1, column: 12 },
			end: { line: 1, column: 15 }
		},
		primitive: true,
		id: { type: "Identifier", name: "var"}
	},
	id: {
		type: "Identifier",
		loc: {
			start: { line: 1, column: 17 },
			end: { line: 1, column: 17 }
		},
		name: "✖"
	},
	binding: null
}]));

testLoose('a{ w }', rootObjectMembers([{
	type: "QMLPropertyBinding",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 5 }
	},
	id: {
		type: "QMLQualifiedID",
		loc: {
			start: { line: 1, column: 3 },
			end: { line: 1, column: 4 }
		},
		parts: [{ type: "Identifier", name: "w" }],
		name: "w"
	},
	binding: {
		type: "QMLScriptBinding",
		loc: {
			start: { line: 1, column: 5 },
			end: { line: 1, column: 5 }
		},
		script: {
			type: "Identifier",
			name: "✖"
		}
	}
}]));

testLoose('a{ w:  }', rootObjectMembers([{
	type: "QMLPropertyBinding",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 7 }
	},
	id: {
		type: "QMLQualifiedID",
		loc: {
			start: { line: 1, column: 3 },
			end: { line: 1, column: 4 }
		},
		parts: [{ type: "Identifier", name: "w" }],
		name: "w"
	},
	binding: {
		type: "QMLScriptBinding",
		loc: {
			start: { line: 1, column: 4 },
			end: { line: 1, column: 7 }
		},
		script: {
			type: "Identifier",
			name: "✖"
		}
	}
}]));

testLoose('a{ : 3 }', rootObjectMembers([{
	type: "QMLPropertyBinding",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 6 }
	},
	id: {
		type: "QMLQualifiedID",
		loc: {
			start: { line: 1, column: 3 },
			end: { line: 1, column: 3 }
		},
		parts: [{ type: "Identifier", name: "✖" }],
		name: "✖"
	},
	binding: {
		type: "QMLScriptBinding",
		loc: {
			start: { line: 1, column: 5 },
			end: { line: 1, column: 6 }
		},
		script: {
			type: "Literal",
			value: 3,
			raw: "3"
		}
	}
}]));

testLoose('a{ anchors.: 3 }', rootObjectMembers([{
	type: "QMLPropertyBinding",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 14 }
	},
	id: {
		type: "QMLQualifiedID",
		loc: {
			start: { line: 1, column: 3 },
			end: { line: 1, column: 11 }
		},
		parts: [
			{ type: "Identifier", name: "anchors" },
			{ type: "Identifier", name: "✖" }
		],
		name: "anchors.✖"
	},
	binding: {
		type: "QMLScriptBinding",
		loc: {
			start: { line: 1, column: 13 },
			end: { line: 1, column: 14 }
		},
		script: {
			type: "Literal",
			value: 3,
			raw: "3"
		}
	}
}]));

testLoose('a{ anchors..: 3 }', rootObjectMembers([{
	type: "QMLPropertyBinding",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 15 }
	},
	id: {
		type: "QMLQualifiedID",
		loc: {
			start: { line: 1, column: 3 },
			end: { line: 1, column: 12 }
		},
		parts: [
			{ type: "Identifier", name: "anchors" },
			{ type: "Identifier", name: "✖" },
			{ type: "Identifier", name: "✖" }
		],
		name: "anchors.✖.✖"
	},
	binding: {
		type: "QMLScriptBinding",
		loc: {
			start: { line: 1, column: 14 },
			end: { line: 1, column: 15 }
		},
		script: {
			type: "Literal",
			value: 3,
			raw: "3"
		}
	}
}]));

testLoose('a{ ..: 3 }', rootObjectMembers([{
	type: "QMLPropertyBinding",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 8 }
	},
	id: {
		type: "QMLQualifiedID",
		loc: {
			start: { line: 1, column: 3 },
			end: { line: 1, column: 5 }
		},
		parts: [
			{ type: "Identifier", name: "✖" },
			{ type: "Identifier", name: "✖" },
			{ type: "Identifier", name: "✖" }
		],
		name: "✖.✖.✖"
	},
	binding: {
		type: "QMLScriptBinding",
		loc: {
			start: { line: 1, column: 7 },
			end: { line: 1, column: 8 }
		},
		script: {
			type: "Literal",
			value: 3,
			raw: "3"
		}
	}
}]));

testLoose('a{ var }', rootObjectMembers([{
	type: "QMLPropertyDeclaration",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 7 }
	},
	default: false,
	readonly: false,
	kind: {
		type: "QMLPropertyType",
		loc: {
			start: { line: 1, column: 3 },
			end: { line: 1, column: 6 }
		},
		primitive: true,
		id: { type: "Identifier", name: "var"}
	},
	id: {
		type: "Identifier",
		loc: {
			start: { line: 1, column: 7 },
			end: { line: 1, column: 7 }
		},
		name: "✖"
	},
	binding: null
}]));

testLoose('a{ var w }', rootObjectMembers([{
	type: "QMLPropertyDeclaration",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 8 }
	},
	default: false,
	readonly: false,
	kind: {
		type: "QMLPropertyType",
		loc: {
			start: { line: 1, column: 3 },
			end: { line: 1, column: 6 }
		},
		primitive: true,
		id: { type: "Identifier", name: "var"}
	},
	id: {
		type: "Identifier",
		loc: {
			start: { line: 1, column: 7 },
			end: { line: 1, column: 8 }
		},
		name: "w"
	},
	binding: null
}]));

testLoose('a{ obj w }', rootObjectMembers([{
	type: "QMLPropertyDeclaration",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 8 }
	},
	default: false,
	readonly: false,
	kind: {
		type: "QMLPropertyType",
		loc: {
			start: { line: 1, column: 3 },
			end: { line: 1, column: 6 }
		},
		primitive: false,
		id: { type: "Identifier", name: "obj"}
	},
	id: {
		type: "Identifier",
		loc: {
			start: { line: 1, column: 7 },
			end: { line: 1, column: 8 }
		},
		name: "w"
	},
	binding: null
}]));

// TODO: Allow this to run with the normal parser once the ambiguity is solved
testLoose('a{ property var b: Window {} }', rootObjectMembers([{
	type: "QMLPropertyDeclaration",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 28 }
	},
	default: false,
	readonly: false,
	kind: {
		type: "QMLPropertyType",
		loc: {
			start: { line: 1, column: 12 },
			end: { line: 1, column: 15 }
		},
		primitive: true,
		id: { type: "Identifier", name: "var"}
	},
	id: {
		type: "Identifier",
		loc: {
			start: { line: 1, column: 16 },
			end: { line: 1, column: 17 }
		},
		name: "b"
	},
	binding: {
		type: "QMLObjectBinding",
		loc: {
			start: { line: 1, column: 19 },
			end: { line: 1, column: 28 }
		},
		id: {
			type: "QMLQualifiedID",
			loc: {
				start: { line: 1, column: 19 },
				end: { line: 1, column: 25 }
			},
			parts: [{ type: "Identifier", name: "Window" }],
			name: "Window"
		},
		body: {
			type: "QMLObjectInitializer",
			loc: {
				start: { line: 1, column: 26 },
				end: { line: 1, column: 28 }
			},
			members: []
		}
	}
}]), { locations: true, qmltypes: false });

// TODO: Allow this to run with the normal parser once the ambiguity is solved
testLoose('a{ b: Window {} }', rootObjectMembers([{
	type: "QMLPropertyBinding",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 15 }
	},
	id: {
		type: "QMLQualifiedID",
		loc: {
			start: { line: 1, column: 3 },
			end: { line: 1, column: 4 }
		},
		parts: [{ type: "Identifier", name: "b" }],
		name: "b"
	},
	binding: {
		type: "QMLObjectBinding",
		loc: {
			start: { line: 1, column: 6 },
			end: { line: 1, column: 15 }
		},
		id: {
			type: "QMLQualifiedID",
			loc: {
				start: { line: 1, column: 6 },
				end: { line: 1, column: 12 }
			},
			parts: [{ type: "Identifier", name: "Window" }],
			name: "Window"
		},
		body: {
			type: "QMLObjectInitializer",
			loc: {
				start: { line: 1, column: 13 },
				end: { line: 1, column: 15 }
			},
			members: []
		}
	}
}]), { locations: true, qmltypes: false });

testLoose('a{ signal }', rootObjectMembers([{
	type: "QMLSignalDefinition",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 10 }
	},
	id: {
		type: "Identifier",
		loc: {
			start: { line: 1, column: 10 },
			end: { line: 1, column: 10 }
		},
		name: "✖"
	},
	params: []
}]));

testLoose('a{ signal () }', rootObjectMembers([{
	type: "QMLSignalDefinition",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 12 }
	},
	id: {
		type: "Identifier",
		loc: {
			start: { line: 1, column: 10 },
			end: { line: 1, column: 10 }
		},
		name: "✖"
	},
	params: []
}]));

testLoose('a{ signal test( }', rootObjectMembers([{
	type: "QMLSignalDefinition",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 16 }
	},
	id: {
		type: "Identifier",
		loc: {
			start: { line: 1, column: 10 },
			end: { line: 1, column: 14 }
		},
		name: "test"
	},
	params: []
}]));

testLoose('a{ signal test(int test }', rootObjectMembers([{
	type: "QMLSignalDefinition",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 24 }
	},
	id: {
		type: "Identifier",
		loc: {
			start: { line: 1, column: 10 },
			end: { line: 1, column: 14 }
		},
		name: "test"
	},
	params: [{
		type: "QMLParameter",
		loc: {
			start: { line: 1, column: 15 },
			end: { line: 1, column: 23 }
		},
		kind: {
			type: "QMLPropertyType",
			loc: {
				start: { line: 1, column: 15 },
				end: { line: 1, column: 18 }
			},
			primitive: true,
			id: { type: "Identifier", name: "int" }
		},
		id: {
			type: "Identifier",
			loc: {
				start: { line: 1, column: 19 },
				end: { line: 1, column: 23 }
			},
			name: "test"
		}
	}]
}]));

testLoose('Window {\n\tfunction\n\tproperty var prop\n}', rootObjectMembers([
	{
		type: "FunctionDeclaration",
		loc: {
			start: { line: 2, column: 1 },
			end: { line: 3, column: 9 }
		},
		id: {
			type: "Identifier",
			loc: {
				start: { line: 3, column: 1 },
				end: { line: 3, column: 9 }
			},
			name: "property"
		},
		params: [],
		body: {
			type: "BlockStatement",
			loc: {
				start: { line: 3, column: 9 },
				end: { line: 3, column: 9 }
			},
			body: []
		}
	},
	{
		type: "QMLPropertyDeclaration",
		loc: {
			start: { line: 3, column: 10 },
			end: { line: 3, column: 18 }
		},
		default: false,
		readonly: false,
		kind: {
			type: "QMLPropertyType",
			loc: {
				start: { line: 3, column: 10 },
				end: { line: 3, column: 13 }
			},
			primitive: true,
			id: { type: "Identifier", name: "var"}
		},
		id: {
			type: "Identifier",
			loc: {
				start: { line: 3, column: 14 },
				end: { line: 3, column: 18 }
			},
			name: "prop"
		},
		binding: null
	}
]));

testLoose('Window {\n\tfunction (something)\n\tproperty var prop\n}', rootObjectMembers([
	{
		type: "FunctionDeclaration",
		loc: {
			start: { line: 2, column: 1 },
			end: { line: 2, column: 21 }
		},
		id: {
			type: "Identifier",
			loc: {
				start: { line: 2, column: 10 },
				end: { line: 2, column: 10 }
			},
			name: "✖"
		},
		params: [
			{
				type: "Identifier",
				loc: {
					start: { line: 2, column: 11 },
					end: { line: 2, column: 20 }
				},
				name: "something"
			}
		],
		body: {
			type: "BlockStatement",
			loc: {
				start: { line: 2, column: 21 },
				end: { line: 2, column: 21 }
			},
			body: []
		}
	},
	{
		type: "QMLPropertyDeclaration",
		loc: {
			start: { line: 3, column: 1 },
			end: { line: 3, column: 18 }
		},
		default: false,
		readonly: false,
		kind: {
			type: "QMLPropertyType",
			loc: {
				start: { line: 3, column: 10 },
				end: { line: 3, column: 13 }
			},
			primitive: true,
			id: { type: "Identifier", name: "var"}
		},
		id: {
			type: "Identifier",
			loc: {
				start: { line: 3, column: 14 },
				end: { line: 3, column: 18 }
			},
			name: "prop"
		},
		binding: null
	}
]));

testLoose('Window {\n\tfunction (\n\tproperty var prop\n}', rootObjectMembers([
	{
		type: "FunctionDeclaration",
		loc: {
			start: { line: 2, column: 1 },
			end: { line: 3, column: 1 }
		},
		id: {
			type: "Identifier",
			loc: {
				start: { line: 2, column: 10 },
				end: { line: 2, column: 10 }
			},
			name: "✖"
		},
		params: [],
		body: {
			type: "BlockStatement",
			loc: {
				start: { line: 3, column: 1 },
				end: { line: 3, column: 1 }
			},
			body: []
		}
	},
	{
		type: "QMLPropertyDeclaration",
		loc: {
			start: { line: 3, column: 1 },
			end: { line: 3, column: 18 }
		},
		default: false,
		readonly: false,
		kind: {
			type: "QMLPropertyType",
			loc: {
				start: { line: 3, column: 10 },
				end: { line: 3, column: 13 }
			},
			primitive: true,
			id: { type: "Identifier", name: "var"}
		},
		id: {
			type: "Identifier",
			loc: {
				start: { line: 3, column: 14 },
				end: { line: 3, column: 18 }
			},
			name: "prop"
		},
		binding: null
	}
]));

// TODO: Allow this to run on the normal parser once the ambiguity is solved
testLoose("a{ QtObject on test {} }", rootObjectMembers([{
	type: "QMLPropertyModifier",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 22 }
	},
	kind: {
		type: "QMLQualifiedID",
		loc: {
			start: { line: 1, column: 3 },
			end: { line: 1, column: 11 }
		},
		parts: [{ type: "Identifier", name: "QtObject" }],
		name: "QtObject"
	},
	id: {
		type: "QMLQualifiedID",
		loc: {
			start: { line: 1, column: 15 },
			end: { line: 1, column: 19 }
		},
		parts: [{ type: "Identifier", name: "test" }],
		name: "test"
	},
	body: {
		type: "QMLObjectInitializer",
		loc: {
			start: { line: 1, column: 20 },
			end: { line: 1, column: 22 }
		},
		members: []
	}
}]));

// TODO: Allow this to run on the normal parser once the ambiguity is solved
testLoose("a{ QtObject on test {} }", rootObjectMembers([{
	type: "QMLPropertyModifier",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 22 }
	},
	kind: {
		type: "QMLQualifiedID",
		loc: {
			start: { line: 1, column: 3 },
			end: { line: 1, column: 11 }
		},
		parts: [{ type: "Identifier", name: "QtObject" }],
		name: "QtObject"
	},
	id: {
		type: "QMLQualifiedID",
		loc: {
			start: { line: 1, column: 15 },
			end: { line: 1, column: 19 }
		},
		parts: [{ type: "Identifier", name: "test" }],
		name: "test"
	},
	body: {
		type: "QMLObjectInitializer",
		loc: {
			start: { line: 1, column: 20 },
			end: { line: 1, column: 22 }
		},
		members: []
	}
}]));

testLoose("a{ QtObject. on test {} }", rootObjectMembers([{
	type: "QMLPropertyBinding",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 23 }
	},
	id: {
		type: "QMLQualifiedID",
		loc: {
			start: { line: 1, column: 3 },
			end: { line: 1, column: 15 }
		},
		parts: [
			{ type: "Identifier", name: "QtObject" },
			{ type: "Identifier", name: "on" }
		],
		name: "QtObject.on"
	},
	binding: {
		type: "QMLObjectBinding",
		loc: {
			start: { line: 1, column: 16 },
			end: { line: 1, column: 23 }
		},
		id: {
			type: "QMLQualifiedID",
			loc: {
				start: { line: 1, column: 16 },
				end: { line: 1, column: 20 }
			},
			parts: [{ type: "Identifier", name: "test" }],
			name: "test"
		},
		body: {
			type: "QMLObjectInitializer",
			loc: {
				start: { line: 1, column: 21 },
				end: { line: 1, column: 23 }
			},
			members: []
		}
	}
}]), { locations: true, qmltypes: false } );

// TODO: Allow this to run on the normal parser once the ambiguity is solved
testLoose("a{ s: [ QtObject {} ] }", rootObjectMembers([{
	type: "QMLPropertyBinding",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 21 }
	},
	id: {
		type: "QMLQualifiedID",
		loc: {
			start: { line: 1, column: 3 },
			end: { line: 1, column: 4 }
		},
		parts: [{ type: "Identifier", name: "s" }],
		name: "s"
	},
	binding: {
		type: "QMLArrayBinding",
		loc: {
			start: { line: 1, column: 6 },
			end: { line: 1, column: 21 }
		},
		elements: [
			{
				type: "QMLObjectDefinition",
				loc: {
					start: { line: 1, column: 8 },
					end: { line: 1, column: 19 }
				},
				id: {
					type: "QMLQualifiedID",
					loc: {
						start: { line: 1, column: 8 },
						end: { line: 1, column: 16 }
					},
					parts: [{ type: "Identifier", name: "QtObject" }],
					name: "QtObject"
				},
				body: {
					type: "QMLObjectInitializer",
					loc: {
						start: { line: 1, column: 17 },
						end: { line: 1, column: 19 }
					},
					members: []
				}
			}
		]
	}
}]), { locations: true, qmltypes: false });

// TODO: Allow this to run on the normal parser once the ambiguity is solved
testLoose("a{ s: [ QtObject {}, QtObject {} ] }", rootObjectMembers([{
	type: "QMLPropertyBinding",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 34 }
	},
	id: {
		type: "QMLQualifiedID",
		loc: {
			start: { line: 1, column: 3 },
			end: { line: 1, column: 4 }
		},
		parts: [{ type: "Identifier", name: "s" }],
		name: "s"
	},
	binding: {
		type: "QMLArrayBinding",
		loc: {
			start: { line: 1, column: 6 },
			end: { line: 1, column: 34 }
		},
		elements: [
			{
				type: "QMLObjectDefinition",
				loc: {
					start: { line: 1, column: 8 },
					end: { line: 1, column: 19 }
				},
				id: {
					type: "QMLQualifiedID",
					loc: {
						start: { line: 1, column: 8 },
						end: { line: 1, column: 16 }
					},
					parts: [{ type: "Identifier", name: "QtObject" }],
					name: "QtObject"
				},
				body: {
					type: "QMLObjectInitializer",
					loc: {
						start: { line: 1, column: 17 },
						end: { line: 1, column: 19 }
					},
					members: []
				}
			},
			{
				type: "QMLObjectDefinition",
				loc: {
					start: { line: 1, column: 21 },
					end: { line: 1, column: 32 }
				},
				id: {
					type: "QMLQualifiedID",
					loc: {
						start: { line: 1, column: 21 },
						end: { line: 1, column: 29 }
					},
					parts: [{ type: "Identifier", name: "QtObject" }],
					name: "QtObject"
				},
				body: {
					type: "QMLObjectInitializer",
					loc: {
						start: { line: 1, column: 30 },
						end: { line: 1, column: 32 }
					},
					members: []
				}
			}
		]
	}
}]), { locations: true, qmltypes: false });

// TODO: Allow this to run on the normal parser once the ambiguity is solved
testLoose("a{ s: [ QtObject ] }", rootObjectMembers([{
	type: "QMLPropertyBinding",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 18 }
	},
	id: {
		type: "QMLQualifiedID",
		loc: {
			start: { line: 1, column: 3 },
			end: { line: 1, column: 4 }
		},
		parts: [{ type: "Identifier", name: "s" }],
		name: "s"
	},
	binding: {
		type: "QMLScriptBinding",
		loc: {
			start: { line: 1, column: 6 },
			end: { line: 1, column: 18 }
		},
		block: false,
		script: {
			type: "ArrayExpression",
			loc: {
				start: { line: 1, column: 6 },
				end: { line: 1, column: 18 }
			},
			elements: [
				{
					type: "Identifier",
					loc: {
						start: { line: 1, column: 8 },
						end: { line: 1, column: 16 }
					},
					name: "QtObject"
				}
			]
		}
	}
}]
), { locations: true, qmltypes: false });

// TODO: Allow this to run on the normal parser once the ambiguity is solved
testLoose("a{ property var s: [ QtObject {} ] }", rootObjectMembers([{
	type: "QMLPropertyDeclaration",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 34 }
	},
	default: false,
	readonly: false,
	kind: {
		type: "QMLPropertyType",
		loc: {
			start: { line: 1, column: 12 },
			end: { line: 1, column: 15 }
		},
		primitive: true,
		id: { type: "Identifier", name: "var" }
	},
	id: {
		type: "Identifier",
		loc: {
			start: { line: 1, column: 16 },
			end: { line: 1, column: 17 }
		},
		name: "s"
	},
	binding: {
		type: "QMLArrayBinding",
		loc: {
			start: { line: 1, column: 19 },
			end: { line: 1, column: 34 }
		},
		elements: [
			{
				type: "QMLObjectDefinition",
				loc: {
					start: { line: 1, column: 21 },
					end: { line: 1, column: 32 }
				},
				id: {
					type: "QMLQualifiedID",
					loc: {
						start: { line: 1, column: 21 },
						end: { line: 1, column: 29 }
					},
					parts: [{ type: "Identifier", name: "QtObject" }],
					name: "QtObject"
				},
				body: {
					type: "QMLObjectInitializer",
					loc: {
						start: { line: 1, column: 30 },
						end: { line: 1, column: 32 }
					},
					members: []
				}
			}
		]
	}
}]), { locations: true, qmltypes: false });

// TODO: Allow this to run on the normal parser once the ambiguity is solved
testLoose("a{ property var s: [ QtObject ] }", rootObjectMembers([{
	type: "QMLPropertyDeclaration",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 31 }
	},
	default: false,
	readonly: false,
	kind: {
		type: "QMLPropertyType",
		loc: {
			start: { line: 1, column: 12 },
			end: { line: 1, column: 15 }
		},
		primitive: true,
		id: { type: "Identifier", name: "var" }
	},
	id: {
		type: "Identifier",
		loc: {
			start: { line: 1, column: 16 },
			end: { line: 1, column: 17 }
		},
		name: "s"
	},
	binding: {
		type: "QMLScriptBinding",
		loc: {
			start: { line: 1, column: 19 },
			end: { line: 1, column: 31 }
		},
		block: false,
		script: {
			type: "ArrayExpression",
			loc: {
				start: { line: 1, column: 19 },
				end: { line: 1, column: 31 }
			},
			elements: [
				{
					type: "Identifier",
					loc: {
						start: { line: 1, column: 21 },
						end: { line: 1, column: 29 }
					},
					name: "QtObject"
				}
			]
		}
	}
}]), { locations: true, qmltypes: false });

testLoose("a{ s: [ QtObject {} }", rootObjectMembers([{
	type: "QMLPropertyBinding",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 20 }
	},
	id: {
		type: "QMLQualifiedID",
		loc: {
			start: { line: 1, column: 3 },
			end: { line: 1, column: 4 }
		},
		parts: [{ type: "Identifier", name: "s" }],
		name: "s"
	},
	binding: {
		type: "QMLArrayBinding",
		loc: {
			start: { line: 1, column: 6 },
			end: { line: 1, column: 20 }
		},
		elements: [
			{
				type: "QMLObjectDefinition",
				loc: {
					start: { line: 1, column: 8 },
					end: { line: 1, column: 19 }
				},
				id: {
					type: "QMLQualifiedID",
					loc: {
						start: { line: 1, column: 8 },
						end: { line: 1, column: 16 }
					},
					parts: [{ type: "Identifier", name: "QtObject" }],
					name: "QtObject"
				},
				body: {
					type: "QMLObjectInitializer",
					loc: {
						start: { line: 1, column: 17 },
						end: { line: 1, column: 19 }
					},
					members: []
				}
			}
		]
	}
}]), { locations: true, qmltypes: false });

testLoose("a{ s: [ QtObject { ] }", rootObjectMembers([{
	type: "QMLPropertyBinding",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 22 }
	},
	id: {
		type: "QMLQualifiedID",
		loc: {
			start: { line: 1, column: 3 },
			end: { line: 1, column: 4 }
		},
		parts: [{ type: "Identifier", name: "s" }],
		name: "s"
	},
	binding: {
		type: "QMLArrayBinding",
		loc: {
			start: { line: 1, column: 6 },
			end: { line: 1, column: 22 }
		},
		elements: [
			{
				type: "QMLObjectDefinition",
				loc: {
					start: { line: 1, column: 8 },
					end: { line: 1, column: 22 }
				},
				id: {
					type: "QMLQualifiedID",
					loc: {
						start: { line: 1, column: 8 },
						end: { line: 1, column: 16 }
					},
					parts: [{ type: "Identifier", name: "QtObject" }],
					name: "QtObject"
				},
				body: {
					type: "QMLObjectInitializer",
					loc: {
						start: { line: 1, column: 17 },
						end: { line: 1, column: 22 }
					},
					members: []
				}
			}
		]
	}
}]), { locations: true, qmltypes: false });

/***************************************************************************
*                          QMLTypes Parser Tests                           *
****************************************************************************/
function testQMLTypes(code, ast, options) {
	var opts = options || {};
	opts.qmltypes = true;
	opts.locations = true;
	test(code, ast, opts);
}

testQMLTypes("a{ b: {} }", javaScript({
	type: "ObjectExpression",
	loc: {
		start: { line: 1, column: 6 },
		end: { line: 1, column: 8 }
	},
	properties: []
}));

testQMLTypes('a{ b: "test" }', javaScript({
	type: "Literal",
	loc: {
		start: { line: 1, column: 6 },
		end: { line: 1, column: 12 }
	},
	value: "test",
	raw: '"test"'
}));

testQMLTypes('a{ b: ["one", "two"] }', javaScript({
	type: "ArrayExpression",
	loc: {
		start: { line: 1, column: 6 },
		end: { line: 1, column: 20 }
	},
	elements: [
		{
			type: "Literal",
			loc: {
				start: { line: 1, column: 7 },
				end: { line: 1, column: 12 }
			},
			value: "one",
			raw: '"one"'
		},
		{
			type: "Literal",
			loc: {
				start: { line: 1, column: 14 },
				end: { line: 1, column: 19 }
			},
			value: "two",
			raw: '"two"'
		}
	]
}));

/*
* Creates a Program with 'headerItemList' and 'rootObject' as the program's expected
* body.
*/
function program(headerItemList, rootObject) {
	return {
		type: "QMLProgram",
		headerItemList: {
			type: "QMLHeaderItemList",
			items: headerItemList || []
		},
		rootObject: rootObject || null
	};
}

/*
* Creates a QMLHeaderItemList with 'stmts' as the expected statements and 'prog' as the
* parent Program.  A value of null for 'prog' will append the QMLHeaderItemList AST to
* the Program returned by program().
*/
function headerItemList(items, prog) {
	prog = prog || program();
	prog.headerItemList.items = items;
	return prog;
}

/*
* Creates a QMLObjectDefinition with 'stmts' as the expected statements and 'prog' as the parent
* Program.  A value of null for 'prog' will append the QMLObjectDefinition AST to the Program
* returned by program().
*/
function rootObject(obj, prog) {
	prog = prog || program();
	prog.rootObject = obj;
	return prog;
}

/*
* Creates a QMLObjectInitializer with 'members' as the expected members and 'obj' as the parent
* QMLObjectDefinition.  A value of null for 'obj' will append the QMLObjectInitializer AST to the
* QMLObjectDefinition returned by rootObject().
*/
function rootObjectMembers(members, obj) {
	var rootObj = obj || rootObject({
		type: "QMLObjectDefinition",
		body: {
			type: "QMLObjectInitializer",
			members: members
		}
	});
	return rootObj;
}

/*
* Creates a JavaScript Expression with 'expr' as the expected expression and 'objMembers' as
* the parent QMLObjectInitializer.  A value of null for 'objMembers' will append the JavaScript
* Expression to the QMLObjectInitializer returned by rootObjectMembers().
*/
function javaScript(expr, objMembers) {
	objMembers = objMembers || rootObjectMembers([{
		type: "QMLPropertyBinding",
		binding: {
			type: "QMLScriptBinding",
			script: expr
		}
	}]);
	return objMembers;
}