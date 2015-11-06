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
'use strict';

(function (mod) {
	if (typeof exports == "object" && typeof module == "object") // CommonJS
		return mod(require("acorn/walk"));
	if (typeof define == "function" && define.amd) // AMD
		return define(["acorn/dist/walk"], mod);
	mod(acorn.walk); // Plain browser env
})(function (walk) {
	function skipThrough(node, st, c) {
		c(node, st)
	}

	function ignore(node, st, c) {}

	var base = walk.base;
	base["Program"] = function (node, st, c) {
		c(node.headerStatements, st);
		c(node.rootObject, st, "QMLRootObject");
	};
	base["QMLHeaderStatements"] = function (node, st, c) {
		for (var i = 0; i < node.statements.length; i++) {
			c(node.statements[i], st, "QMLHeaderStatement");
		}
	};
	base["QMLHeaderStatement"] = skipThrough;
	base["QMLImportStatement"] = ignore;
	base["QMLPragmaStatement"] = ignore;
	base["QMLRootObject"] = skipThrough;
	base["QMLObjectLiteral"] = function (node, st, c) {
		c(node.block, st);
	};
	base["QMLMemberBlock"] = function (node, st, c) {
		for (var i = 0; i < node.members.length; i++) {
			c(node.members[i], st, "QMLMember");
		}
	};
	base["QMLMember"] = skipThrough;
	base["QMLPropertyDeclaration"] = function (node, st, c) {
		if (node.init) {
			c(node.init, st);
		}
	};
	base["QMLSignalDefinition"] = ignore;
	base["QMLPropertyBinding"] = function (node, st, c) {
		c(node.expr, st);
	};
	base["QMLQualifiedID"] = ignore;
	base["QMLStatementBlock"] = function (node, st, c) {
		for (var i = 0; i < node.statements.length; i++) {
			c(node.statements[i], st, "Statement");
		}
	};
})