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

testFail('', "QML only supports ECMA Script Language Specification 5 or older",
		{ locations: true, ecmaVersion: 6, allowReserved: false });

test('import QtQuick 2.2', headerStatements([{
	type: "QMLImportStatement",
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
			major: 2,
			minor: 2,
			raw: "2.2"
		}
	}
}]));

test('import "./file.js"', headerStatements([{
	type: "QMLImportStatement",
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

test('import "./file.js" as MyModule', headerStatements([{
	type: "QMLImportStatement",
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

test('import QtQuick 2.2\nimport "./file.js"', headerStatements([
	{
		type: "QMLImportStatement",
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
				major: 2,
				minor: 2,
				raw: "2.2"
			}
		}
	},
	{
		type: "QMLImportStatement",
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

test('import QtQuick 2.2;import "./file.js"', headerStatements([
	{
		type: "QMLImportStatement",
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
				major: 2,
				minor: 2,
				raw: "2.2"
			}
		}
	},
	{
		type: "QMLImportStatement",
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

test('import Module 1.0 as MyModule', headerStatements([
	{
		type: "QMLImportStatement",
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
				major: 1,
				minor: 0,
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

test('import Qualified.Id.Test 1.0', headerStatements([
	{
		type: "QMLImportStatement",
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
				major: 1,
				minor: 0,
				raw: "1.0"
			}
		}
	}
]));

test('pragma Singleton', headerStatements([
	{
		type: "QMLPragmaStatement",
		loc: {
			start: { line: 1, column: 0 },
			end: { line: 1, column: 16 }
		},
		id: {
			type: "Identifier",
			loc: {
				start: { line: 1, column: 7 },
				end: { line: 1, column: 16 }
			},
			name: "Singleton"
		}
	}
]));

test('pragma Singleton\npragma Other', headerStatements([
	{
		type: "QMLPragmaStatement",
		loc: {
			start: { line: 1, column: 0 },
			end: { line: 1, column: 16 }
		},
		id: {
			type: "Identifier",
			loc: {
				start: { line: 1, column: 7 },
				end: { line: 1, column: 16 }
			},
			name: "Singleton"
		}
	},
	{
		type: "QMLPragmaStatement",
		loc: {
			start: { line: 2, column: 0 },
			end: { line: 2, column: 12 }
		},
		id: {
			type: "Identifier",
			loc: {
				start: { line: 2, column: 7 },
				end: { line: 2, column: 12 }
			},
			name: "Other"
		}
	}
]));

test('pragma Singleton;pragma Other', headerStatements([
	{
		type: "QMLPragmaStatement",
		loc: {
			start: { line: 1, column: 0 },
			end: { line: 1, column: 17 }
		},
		id: {
			type: "Identifier",
			loc: {
				start: { line: 1, column: 7 },
				end: { line: 1, column: 16 }
			},
			name: "Singleton"
		}
	},
	{
		type: "QMLPragmaStatement",
		loc: {
			start: { line: 1, column: 17 },
			end: { line: 1, column: 29 }
		},
		id: {
			type: "Identifier",
			loc: {
				start: { line: 1, column: 24 },
				end: { line: 1, column: 29 }
			},
			name: "Other"
		}
	}
]));

test('Window {}', rootObject({
	type: "QMLObjectLiteral",
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
		type: "QMLMemberBlock",
		loc: {
			start: { line: 1, column: 7 },
			end: { line: 1, column: 9 }
		},
		members: []
	}
}));

test('QtQuick.Window {}', rootObject({
	type: "QMLObjectLiteral",
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
		type: "QMLMemberBlock",
		loc: {
			start: { line: 1, column: 15 },
			end: { line: 1, column: 17 }
		},
		members: []
	}
}));

test('property {}', rootObject({
	type: "QMLObjectLiteral",
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
		type: "QMLMemberBlock",
		loc: {
			start: { line: 1, column: 9 },
			end: { line: 1, column: 11 }
		},
		members: []
	}
}));

test('readonly {}', rootObject({
	type: "QMLObjectLiteral",
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
		type: "QMLMemberBlock",
		loc: {
			start: { line: 1, column: 9 },
			end: { line: 1, column: 11 }
		},
		members: []
	}
}));

test('signal {}', rootObject({
	type: "QMLObjectLiteral",
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
		type: "QMLMemberBlock",
		loc: {
			start: { line: 1, column: 7 },
			end: { line: 1, column: 9 }
		},
		members: []
	}
}));

test('alias {}', rootObject({
	type: "QMLObjectLiteral",
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
		type: "QMLMemberBlock",
		loc: {
			start: { line: 1, column: 6 },
			end: { line: 1, column: 8 }
		},
		members: []
	}
}));

test('list {}', rootObject({
	type: "QMLObjectLiteral",
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
		type: "QMLMemberBlock",
		loc: {
			start: { line: 1, column: 5 },
			end: { line: 1, column: 7 }
		},
		members: []
	}
}));

test('color {}', rootObject({
	type: "QMLObjectLiteral",
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
		type: "QMLMemberBlock",
		loc: {
			start: { line: 1, column: 6 },
			end: { line: 1, column: 8 }
		},
		members: []
	}
}));

test('real {}', rootObject({
	type: "QMLObjectLiteral",
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
		type: "QMLMemberBlock",
		loc: {
			start: { line: 1, column: 5 },
			end: { line: 1, column: 7 }
		},
		members: []
	}
}));

test('string {}', rootObject({
	type: "QMLObjectLiteral",
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
		type: "QMLMemberBlock",
		loc: {
			start: { line: 1, column: 7 },
			end: { line: 1, column: 9 }
		},
		members: []
	}
}));

test('url {}', rootObject({
	type: "QMLObjectLiteral",
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
		type: "QMLMemberBlock",
		loc: {
			start: { line: 1, column: 4 },
			end: { line: 1, column: 6 }
		},
		members: []
	}
}));

test('Window {Button {}}', rootObjectMembers([{
	type: "QMLObjectLiteral",
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
		type: "QMLMemberBlock",
		loc: {
			start: { line: 1, column: 15 },
			end: { line: 1, column: 17 }
		},
		members: []
	}
}]));

test('a{ property {} }', rootObjectMembers([{
	type: "QMLObjectLiteral",
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
		type: "QMLMemberBlock",
		loc: {
			start: { line: 1, column: 12 },
			end: { line: 1, column: 14 }
		},
		members: []
	}
}]));

test('a{ readonly {} }', rootObjectMembers([{
	type: "QMLObjectLiteral",
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
		type: "QMLMemberBlock",
		loc: {
			start: { line: 1, column: 12 },
			end: { line: 1, column: 14 }
		},
		members: []
	}
}]));

test('a{ signal {} }', rootObjectMembers([{
	type: "QMLObjectLiteral",
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
		type: "QMLMemberBlock",
		loc: {
			start: { line: 1, column: 10 },
			end: { line: 1, column: 12 }
		},
		members: []
	}
}]));

//testFail('a{ readonly property var as: 3 }',
//		 "Unexpected token (1:25)",
//		{ locations: true, loose: false });

test('a{ readonly property var w: 3 }', rootObjectMembers([{
	type: "QMLPropertyDeclaration",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 29 }
	},
	default: false,
	readonly: true,
	kind: "var",
	id: {
		type: "Identifier",
		loc: {
			start: { line: 1, column: 25 },
			end: { line: 1, column: 26 }
		},
		name: "w"
	},
	init: {
		type: "Literal",
		loc: {
			start: { line: 1, column: 28 },
			end: { line: 1, column: 29 }
		},
		value: 3,
		raw: "3"
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
	kind: "var",
	id: {
		type: "Identifier",
		loc: {
			start: { line: 1, column: 24 },
			end: { line: 1, column: 25 }
		},
		name: "w"
	},
	init: {
		type: "Literal",
		loc: {
			start: { line: 1, column: 27 },
			end: { line: 1, column: 28 }
		},
		value: 3,
		raw: "3"
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
	kind: "var",
	id: {
		type: "Identifier",
		loc: {
			start: { line: 1, column: 16 },
			end: { line: 1, column: 17 }
		},
		name: "w"
	},
	init: {
		type: "Literal",
		loc: {
			start: { line: 1, column: 19 },
			end: { line: 1, column: 20 }
		},
		value: 3,
		raw: "3"
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
	kind: "boolean",
	id: {
		type: "Identifier",
		loc: {
			start: { line: 1, column: 20 },
			end: { line: 1, column: 21 }
		},
		name: "w"
	},
	init: {
		type: "Literal",
		loc: {
			start: { line: 1, column: 23 },
			end: { line: 1, column: 24 }
		},
		value: 3,
		raw: "3"
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
	kind: "double",
	id: {
		type: "Identifier",
		loc: {
			start: { line: 1, column: 19 },
			end: { line: 1, column: 20 }
		},
		name: "w"
	},
	init: {
		type: "Literal",
		loc: {
			start: { line: 1, column: 22 },
			end: { line: 1, column: 23 }
		},
		value: 3,
		raw: "3"
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
	kind: "int",
	id: {
		type: "Identifier",
		loc: {
			start: { line: 1, column: 16 },
			end: { line: 1, column: 17 }
		},
		name: "w"
	},
	init: {
		type: "Literal",
		loc: {
			start: { line: 1, column: 19 },
			end: { line: 1, column: 20 }
		},
		value: 3,
		raw: "3"
	}
}]));

test('a{ property list w: 3 }', rootObjectMembers([{
	type: "QMLPropertyDeclaration",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 21 }
	},
	default: false,
	readonly: false,
	kind: "list",
	id: {
		type: "Identifier",
		loc: {
			start: { line: 1, column: 17 },
			end: { line: 1, column: 18 }
		},
		name: "w"
	},
	init: {
		type: "Literal",
		loc: {
			start: { line: 1, column: 20 },
			end: { line: 1, column: 21 }
		},
		value: 3,
		raw: "3"
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
	kind: "color",
	id: {
		type: "Identifier",
		loc: {
			start: { line: 1, column: 18 },
			end: { line: 1, column: 19 }
		},
		name: "w"
	},
	init: {
		type: "Literal",
		loc: {
			start: { line: 1, column: 21 },
			end: { line: 1, column: 22 }
		},
		value: 3,
		raw: "3"
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
	kind: "real",
	id: {
		type: "Identifier",
		loc: {
			start: { line: 1, column: 17 },
			end: { line: 1, column: 18 }
		},
		name: "w"
	},
	init: {
		type: "Literal",
		loc: {
			start: { line: 1, column: 20 },
			end: { line: 1, column: 21 }
		},
		value: 3,
		raw: "3"
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
	kind: "string",
	id: {
		type: "Identifier",
		loc: {
			start: { line: 1, column: 19 },
			end: { line: 1, column: 20 }
		},
		name: "w"
	},
	init: {
		type: "Literal",
		loc: {
			start: { line: 1, column: 22 },
			end: { line: 1, column: 23 }
		},
		value: 3,
		raw: "3"
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
	kind: "url",
	id: {
		type: "Identifier",
		loc: {
			start: { line: 1, column: 16 },
			end: { line: 1, column: 17 }
		},
		name: "w"
	},
	init: {
		type: "Literal",
		loc: {
			start: { line: 1, column: 19 },
			end: { line: 1, column: 20 }
		},
		value: 3,
		raw: "3"
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
		type: "QMLQualifiedID",
		loc: {
			start: { line: 1, column: 12 },
			end: { line: 1, column: 20 }
		},
		parts: [{ type: "Identifier", name: "QtObject" }],
		name: "QtObject"
	},
	id: {
		type: "Identifier",
		loc: {
			start: { line: 1, column: 21 },
			end: { line: 1, column: 22 }
		},
		name: "w"
	},
	init: {
		type: "Literal",
		loc: {
			start: { line: 1, column: 24 },
			end: { line: 1, column: 25 }
		},
		value: 3,
		raw: "3"
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
	kind: "alias",
	id: {
		type: "Identifier",
		loc: {
			start: { line: 1, column: 18 },
			end: { line: 1, column: 19 }
		},
		name: "w"
	},
	init: {
		type: "Literal",
		loc: {
			start: { line: 1, column: 21 },
			end: { line: 1, column: 22 }
		},
		value: 3,
		raw: "3"
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
	expr: {
		type: "Literal",
		loc: {
			start: { line: 1, column: 6 },
			end: { line: 1, column: 7 }
		},
		value: 3,
		raw: "3"
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
	expr: {
		type: "Literal",
		loc: {
			start: { line: 1, column: 10 },
			end: { line: 1, column: 11 }
		},
		value: 3,
		raw: "3"
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
	expr: {
		type: "Literal",
		loc: {
			start: { line: 1, column: 10 },
			end: { line: 1, column: 11 }
		},
		value: 3,
		raw: "3"
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
	expr: {
		type: "Literal",
		loc: {
			start: { line: 1, column: 9 },
			end: { line: 1, column: 10 }
		},
		value: 3,
		raw: "3"
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
	expr: {
		type: "Literal",
		loc: {
			start: { line: 1, column: 13 },
			end: { line: 1, column: 14 }
		},
		value: 3,
		raw: "3"
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
	expr: {
		type: "Literal",
		loc: {
			start: { line: 1, column: 13 },
			end: { line: 1, column: 14 }
		},
		value: 3,
		raw: "3"
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
	expr: {
		type: "Literal",
		loc: {
			start: { line: 1, column: 11 },
			end: { line: 1, column: 12 }
		},
		value: 3,
		raw: "3"
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
	expr: {
		type: "Literal",
		loc: {
			start: { line: 1, column: 10 },
			end: { line: 1, column: 11 }
		},
		value: 3,
		raw: "3"
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
	expr: {
		type: "Literal",
		loc: {
			start: { line: 1, column: 9 },
			end: { line: 1, column: 10 }
		},
		value: 3,
		raw: "3"
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
	expr: {
		type: "Literal",
		loc: {
			start: { line: 1, column: 11 },
			end: { line: 1, column: 12 }
		},
		value: 3,
		raw: "3"
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
	expr: {
		type: "Literal",
		loc: {
			start: { line: 1, column: 8 },
			end: { line: 1, column: 9 }
		},
		value: 3,
		raw: "3"
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
	expr: {
		type: "CallExpression",
		loc: {
			start: { line: 1, column: 14 },
			end: { line: 1, column: 24 }
		},
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
			type: "Identifier",
			loc: {
				start: { line: 1, column: 13 },
				end: { line: 1, column: 17 }
			},
			name: "type"
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
				type: "Identifier",
				loc: {
					start: { line: 1, column: 13 },
					end: { line: 1, column: 18 }
				},
				name: "type1"
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
				type: "Identifier",
				loc: {
					start: { line: 1, column: 27 },
					end: { line: 1, column: 32 }
				},
				name: "type2"
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
				type: "Identifier",
				loc: {
					start: { line: 1, column: 41 },
					end: { line: 1, column: 46 }
				},
				name: "type3"
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
	expr: {
		type: "Identifier",
		loc: {
			start: { line: 1, column: 7 },
			end: { line: 1, column: 11 }
		},
		name: "test"
	}
}]));

/*
* Test the base QML Hello World program created by Eclipse CDT.
*/
test('import QtQuick 2.3\nimport QtQuick.Window 2.2\n\tWindow {\n\tvisible: true\n\n\tMouseArea {\n\t\tanchors.fill: parent\n\t\tonClicked: {\n\t\t\tQt.quit();\n\t\t}\n\t}\n\t\tText {\n\t\t\ttext: qsTr("Hello World")\n\t\t\tanchors.centerIn: parent\n\t}\n}',
	program([{
		type: "QMLImportStatement",
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
				value: 2.3,
				major: 2,
				minor: 3
			}
		}
	},
	{
		type: "QMLImportStatement",
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
				value: 2.2,
				major: 2,
				minor: 2
			}
		}
	}],{
		type: "QMLObjectLiteral",
		loc: {
			start: { line: 3, column: 1 },
			end: { line: 16, column: 1 }
		},
		id: {
			type: "QMLQualifiedID",
			loc: {
				start: { line: 3, column: 1 },
				end: { line: 3, column: 7 }
			},
			parts: [{ type: "Identifier", name: "Window" }],
			name: "Window"
		},
		body: {
			type: "QMLMemberBlock",
			loc: {
				start: { line: 3, column: 8 },
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
					expr: {
						type: "Literal",
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
						type: "QMLMemberBlock",
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
								expr: {
									type: "Identifier",
									loc: {
										start: { line: 7, column: 16 },
										end: { line: 7, column: 22 }
									},
									name: "parent"
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
								expr: {
									type: "QMLStatementBlock",
									loc: {
										start: { line: 8, column: 13 },
										end: { line: 10, column: 3 }
									},
									statements: [{
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
						]
					}
				},
				{
					type: "QMLObjectLiteral",
					loc: {
						start: { line: 12, column: 2 },
						end: { line: 15, column: 2 }
					},
					id: {
						type: "QMLQualifiedID",
						loc: {
							start: { line: 12, column: 2 },
							end: { line: 12, column: 6 }
						},
						parts: [{ type: "Identifier", name: "Text" }],
						name: "Text"
					},
					body: {
						type: "QMLMemberBlock",
						loc: {
							start: { line: 12, column: 7 },
							end: { line: 15, column: 2 }
						},
						members: [
							{
								type: "QMLPropertyBinding",
								loc: {
									start: { line: 13, column: 3 },
									end: { line: 13, column: 28 }
								},
								id: {
									type: "QMLQualifiedID",
									loc: {
										start: { line: 13, column: 3 },
										end: { line: 13, column: 7 }
									},
									parts: [{ type: "Identifier", name: "text" }],
									name: "text"
								},
								expr: {
									type: "CallExpression",
									loc: {
										start: { line: 13, column: 9 },
										end: { line: 13, column: 28 }
									},
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
							},
							{
								type: "QMLPropertyBinding",
								loc: {
									start: { line: 14, column: 3 },
									end: { line: 14, column: 27 }
								},
								id: {
									type: "QMLQualifiedID",
									loc: {
										start: { line: 14, column: 3 },
										end: { line: 14, column: 19 }
									},
									parts: [
										{ type: "Identifier", name: "anchors" },
										{ type: "Identifier", name: "centerIn" }
									],
									name: "anchors.centerIn"
								},
								expr: {
									type: "Identifier",
									name: "parent"
								}
							}
						]
					}
				}
			]
		}
	}
));

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

testLoose('import QtQuick', headerStatements([{
	type: "QMLImportStatement",
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
			major: 0,
			minor: 0,
			raw: "0.0"
		}
	}
}]));

testLoose('import ', headerStatements([{
	type: "QMLImportStatement",
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
			major: 0,
			minor: 0,
			raw: "0.0"
		}
	}
}]));

testLoose('import QtQuick 0x01', headerStatements([{
	type: "QMLImportStatement",
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
			major: 1,
			minor: 0,
			raw: "0x01"
		}
	}
}]));

testLoose('import QtQuick 1', headerStatements([{
	type: "QMLImportStatement",
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
			major: 1,
			minor: 0,
			raw: "1"
		}
	}
}]));

testLoose('import "./file.js', headerStatements([{
	type: "QMLImportStatement",
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

testLoose('import QtQuick 2.2 as ', headerStatements([{
	type: "QMLImportStatement",
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
			major: 2,
			minor: 2,
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
	type: "QMLObjectLiteral",
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
		type: "QMLMemberBlock",
		loc: {
			start: { line: 1, column: 7 },
			end: { line: 1, column: 8 }
		},
		members: []
	}
}));

testLoose('Window {\n\tprop: 3', rootObject({
	type: "QMLObjectLiteral",
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
		type: "QMLMemberBlock",
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
			}
		}]
	}
}));

testLoose('a {\n\tb {\n\n\tc {\n}', rootObject({
	type: "QMLObjectLiteral",
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
		type: "QMLMemberBlock",
		loc: {
			start: { line: 1, column: 2 },
			end: { line: 5, column: 1 }
		},
		members: [
			{
				type: "QMLObjectLiteral",
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
					type: "QMLMemberBlock",
					loc: {
						start: { line: 2, column: 3 },
						end: { line: 5, column: 1 }
					},
					members: [{
						type: "QMLObjectLiteral",
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
							type: "QMLMemberBlock",
							loc: {
								start: { line: 4, column: 3 },
								end: { line: 5, column: 1 }
							},
							members: []
						}
					}]
				}
			}
		]
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
	kind: "var",
	id: {
		type: "Identifier",
		loc: {
			start: { line: 1, column: 17 },
			end: { line: 1, column: 17 }
		},
		name: "✖"
	}
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
	expr: {
		type: "Identifier",
		loc: {
			start: { line: 1, column: 5 },
			end: { line: 1, column: 5 }
		},
		name: "✖"
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
	expr: {
		type: "Literal",
		loc: {
			start: { line: 1, column: 5 },
			end: { line: 1, column: 6 }
		},
		value: 3,
		raw: "3"
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
	expr: {
		type: "Literal",
		loc: {
			start: { line: 1, column: 13 },
			end: { line: 1, column: 14 }
		},
		value: 3,
		raw: "3"
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
	expr: {
		type: "Literal",
		loc: {
			start: { line: 1, column: 14 },
			end: { line: 1, column: 15 }
		},
		value: 3,
		raw: "3"
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
	expr: {
		type: "Literal",
		loc: {
			start: { line: 1, column: 7 },
			end: { line: 1, column: 8 }
		},
		value: 3,
		raw: "3"
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
	kind: "var",
	id: {
		type: "Identifier",
		loc: {
			start: { line: 1, column: 7 },
			end: { line: 1, column: 7 }
		},
		name: "✖"
	},
	init: null
}]));

testLoose('a{ var w }', rootObjectMembers([{
	type: "QMLPropertyDeclaration",
	loc: {
		start: { line: 1, column: 3 },
		end: { line: 1, column: 8 }
	},
	default: false,
	readonly: false,
	kind: "var",
	id: {
		type: "Identifier",
		loc: {
			start: { line: 1, column: 7 },
			end: { line: 1, column: 8 }
		},
		name: "w"
	},
	init: null
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
		type: "QMLQualifiedID",
		loc: {
			start: { line: 1, column: 3 },
			end: { line: 1, column: 6 }
		},
		parts: [{ type: "Identifier", name: "obj" }],
		name: "obj"
	},
	id: {
		type: "Identifier",
		loc: {
			start: { line: 1, column: 7 },
			end: { line: 1, column: 8 }
		},
		name: "w"
	},
	init: null
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
	kind: "var",
	id: {
		type: "Identifier",
		loc: {
			start: { line: 1, column: 16 },
			end: { line: 1, column: 17 }
		},
		name: "b"
	},
	init: {
		type: "QMLObjectLiteral",
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
			type: "QMLMemberBlock",
			loc: {
				start: { line: 1, column: 26 },
				end: { line: 1, column: 28 }
			},
			members: []
		}
	}
}]));

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
	expr: {
		type: "QMLObjectLiteral",
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
			type: "QMLMemberBlock",
			loc: {
				start: { line: 1, column: 13 },
				end: { line: 1, column: 15 }
			},
			members: []
		}
	}
}]));

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
			type: "Identifier",
			loc: {
				start: { line: 1, column: 15 },
				end: { line: 1, column: 18 }
			},
			name: "int"
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
		kind: "var",
		id: {
			type: "Identifier",
			loc: {
				start: { line: 3, column: 14 },
				end: { line: 3, column: 18 }
			},
			name: "prop"
		},
		init: null
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
		kind: "var",
		id: {
			type: "Identifier",
			loc: {
				start: { line: 3, column: 14 },
				end: { line: 3, column: 18 }
			},
			name: "prop"
		},
		init: null
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
		kind: "var",
		id: {
			type: "Identifier",
			loc: {
				start: { line: 3, column: 14 },
				end: { line: 3, column: 18 }
			},
			name: "prop"
		},
		init: null
	}
]));

/*
* Creates a Program with 'headerStatements' and 'rootObject' as the program's expected
* body.
*/
function program(headerStatements, rootObject) {
	return {
		type: "Program",
		headerStatements: {
			type: "QMLHeaderStatements",
			statements: headerStatements || []
		},
		rootObject: rootObject || null
	}
}

/*
* Creates a QMLHeaderStatements with 'stmts' as the expected statements and 'prog' as the
* parent Program.  A value of null for 'prog' will append the QMLHeaderStatements AST to
* the Program returned by program().
*/
function headerStatements(stmts, prog) {
	prog = prog || program();
	prog.headerStatements.statements = stmts;
	return prog;
}

/*
* Creates a QMLObjectLiteral with 'stmts' as the expected statements and 'prog' as the parent
* Program.  A value of null for 'prog' will append the QMLObjectLiteral AST to the Program
* returned by program().
*/
function rootObject(obj, prog) {
	prog = prog || program();
	prog.rootObject = obj;
	return prog;
}

/*
* Creates a QMLMemberBlock with 'members' as the expected members and 'obj' as the parent
* QMLObjectLiteral.  A value of null for 'obj' will append the QMLMemberBlock AST to the
* QMLObjectLiteral returned by rootObject().
*/
function rootObjectMembers(members, obj) {
	var rootObj = obj || rootObject({
		type: "QMLObjectLiteral",
		body: {
			type: "QMLMemberBlock",
			members: members
		}
	});
	return rootObj;
}

/*
* Creates a JavaScript Expression with 'expr' as the expected expression and 'objMembers' as
* the parent QMLMemberBlock.  A value of null for 'objMembers' will append the JavaScript
* Expression to the QMLMemberBlock returned by rootObjectMembers().
*/
function javaScript(expr, objMembers) {
	objMembers = objMembers || rootObjectMembers([{
		type: "QMLPropertyBinding",
		expr: expr
	}]);
	return objMembers;
}