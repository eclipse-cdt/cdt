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
(function (mod) {
	if (typeof exports == "object" && typeof module == "object") // CommonJS
		return mod(require("acorn/dist/walk"));
	if (typeof define == "function" && define.amd) // AMD
		return define(["acorn/dist/walk"], mod);
	mod(acorn.walk); // Plain browser env
})(function (walk) {
	"use strict";

	function skipThrough(node, st, c) {
		c(node, st);
	}

	function ignore(node, st, c) {}

	function extendWalk(walker, funcs) {
		for (var prop in funcs) {
			walker[prop] = funcs[prop];
		}
	}

	extendWalk(walk.base, {
		QMLProgram: function (node, st, c) {
			c(node.headerStatements, st);
			if (node.rootObject) {
				c(node.rootObject, st, "QMLRootObject");
			}
		},
		QMLHeaderStatements: function (node, st, c) {
			for (var i = 0; i < node.statements.length; i++) {
				c(node.statements[i], st, "QMLHeaderStatement");
			}
		},
		QMLHeaderStatement: skipThrough,
		QMLImportStatement: ignore,
		QMLPragmaStatement: ignore,
		QMLRootObject: skipThrough,
		QMLObjectLiteral: function (node, st, c) {
			c(node.body, st);
		},
		QMLMemberBlock: function (node, st, c) {
			for (var i = 0; i < node.members.length; i++) {
				c(node.members[i], st, "QMLMember");
			}
		},
		QMLMember: skipThrough,
		QMLPropertyDeclaration: function (node, st, c) {
			if (node.binding) {
				c(node.binding, st);
			}
		},
		QMLSignalDefinition: ignore,
		QMLPropertyBinding: function (node, st, c) {
			c(node.binding, st);
		},
		QMLScriptBinding: function (node, st, c) {
			c(node.script, st);
		},
		QMLQualifiedID: ignore,
		QMLStatementBlock: function (node, st, c) {
			for (var i = 0; i < node.statements.length; i++) {
				c(node.statements[i], st, "Statement");
			}
		}
	});
});