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
"use strict";

(function(mod) {
	if (typeof exports == "object" && typeof module == "object") // CommonJS
		return mod(require("tern/lib/infer"), require("tern/lib/tern"));
	if (typeof define == "function" && define.amd) // AMD
		return define([ "tern/lib/infer", "tern/lib/tern" ], mod);
	mod(tern, tern); // Plain browser env
})(function(infer, tern) {
    // Define a few shorthand variables/functions
    var Scope = infer.Scope;
    function skipThrough(node, st, c) { c(node, st) }
    function ignore(node, st, c) {}

    // Register the QML plugin in Tern
	tern.registerPlugin("qml", function(server) {
		extendTernScopeGatherer(infer.scopeGatherer);
        extendTernInferWrapper(infer.inferWrapper);
        extendTernTypeFinder(infer.typeFinder);
        extendTernSearchVisitor(infer.searchVisitor);
        server.on("preParse", preParse);
	});

	function preParse(text, options) {
		var plugins = options.plugins;
		if (!plugins) plugins = options.plugins = {};
		plugins["qml"] = true;
	}

	function extendTernScopeGatherer(scopeGatherer) {
        scopeGatherer["QMLModule"] = function(node, scope, c) {
            scope.defProp(node.qualifiedId.raw, node.qualifiedId);
        }
		scopeGatherer["QMLMemberBlock"] = function(node, scope, c) {
            var inner = node.scope = new Scope(scope, node);
            for (var i = 0; i < node.members.length; i++) {
				c(node.members[i], inner, "QMLMember");
			}
        }
		scopeGatherer["QMLStatementBlock"] = function(node, scope, c) {
            var inner = node.scope = new Scope(scope, node);
            for (var i = 0; i < node.statements.length; i++) {
				c(node.statements[i], inner, "Statement");
			}
        }
	}

	function extendTernInferWrapper(inferWrapper) {
		// TODO: Implement the AST walk methods for inferWrapper
    }

	function extendTernTypeFinder(typeFinder) {
		// TODO: Implement the AST walk methods for typeFinder
	}

	function extendTernSearchVisitor(searchVisitor) {
		// TODO: Implement the AST walk methods for searchVisitor
	}
})