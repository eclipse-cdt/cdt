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
			c(node.headerItemList, st);
			if (node.rootObject) {
				c(node.rootObject, st, "QMLRootObject");
			}
		},
		QMLHeaderItemList: function (node, st, c) {
			for (var i = 0; i < node.items.length; i++) {
				c(node.items[i], st, "QMLHeaderItem");
			}
		},
		QMLHeaderItem: skipThrough,
		QMLImport: ignore,
		QMLPragma: ignore,
		QMLRootObject: skipThrough,
		QMLObjectDefinition: function (node, st, c) {
			c(node.body, st);
		},
		QMLObjectInitializer: function (node, st, c) {
			for (var i = 0; i < node.members.length; i++) {
				c(node.members[i], st, "QMLObjectMember");
			}
		},
		QMLObjectMember: skipThrough,
		QMLPropertyDeclaration: function (node, st, c) {
			if (node.binding) {
				c(node.binding, st, "QMLBinding");
			}
		},
		QMLSignalDefinition: ignore,
		QMLPropertyBinding: function (node, st, c) {
			c(node.binding, st, "QMLBinding");
		},
		QMLBinding: skipThrough,
		QMLObjectBinding: function (node, st, c) {
			c(node.body, st);
		},
		QMLArrayBinding: function (node, st, c) {
			for (var i = 0; i < node.elements.length; i++) {
				c(node.elements[i], st);
			}
		},
		QMLScriptBinding: function (node, st, c) {
			c(node.script, st);
		},
		QMLQualifiedID: ignore,
		QMLStatementBlock: function (node, st, c) {
			for (var i = 0; i < node.body.length; i++) {
				c(node.body[i], st, "Statement");
			}
		}
	});
});