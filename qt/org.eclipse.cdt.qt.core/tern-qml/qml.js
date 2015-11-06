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
	if (typeof exports === "object" && typeof module === "object") // CommonJS
		return mod(require("tern/lib/infer"), require("tern/lib/tern"));
	if (typeof define === "function" && define.amd) // AMD
		return define(["tern/lib/infer", "tern/lib/tern"], mod);
	mod(tern, tern); // Plain browser env
})(function (infer, tern) {
	'use strict';

	// Define a few shorthand variables/functions
	var ANull = infer.ANull;
	var AVal = infer.AVal;
	var Scope = infer.Scope;
	var Obj = infer.Obj;
	var Fn = infer.Fn;
	var Prim = infer.Prim;

	function addVar(scope, node) {
		return scope.defProp(node.name, node);
	}

	function skipThrough(node, st, c) {
		c(node, st)
	}

	function ignore(node, st, c) {}

	// Register the QML plugin in Tern
	tern.registerPlugin("qml", function (server) {
		extendTernScopeGatherer(infer.scopeGatherer);
		extendTernInferExprVisitor(infer.inferExprVisitor);
		extendTernInferWrapper(infer.inferWrapper);
		extendTernSearchVisitor(infer.searchVisitor);
		server.on("preParse", preParse);
	});

	function preParse(text, options) {
		var plugins = options.plugins;
		if (!plugins) plugins = options.plugins = {};
		plugins["qml"] = true;
	}

	function extendTernScopeGatherer(scopeGatherer) {
		scopeGatherer["QMLImportStatement"] = function (node, scope, c) {
			if (node.qualifier) {
				addVar(scope, node.qualifier.id);
			}
		};
		scopeGatherer["QMLObjectLiteral"] = function (node, scope, c) {
			var inner = node.scope = new Scope(scope, node);
			inner.forward = undefined;
			c(node.block, inner);
		};
		scopeGatherer["QMLPropertyDeclaration"] = function (node, scope, c) {
			var prop = addVar(scope, node.id);
			if (node.init) {
				c(node.init, scope);
			}
		};
		scopeGatherer["QMLPropertyBinding"] = function (node, scope, c) {
			// Check for the 'id' property being set
			var idParts = node.id.parts;
			if (idParts.length == 1 && idParts[0].name === "id") {
				if (node.expr.type === "Identifier") {
					node.prop = scope.prev.defProp(node.expr.name, node.expr);
				}
			}

			// If this appears to be a signal handler, pre-emptively create a new scope that
			// will store references to the signal's arguments
			var last = getLastIndex(idParts).name;
			var inner = scope;
			if (last.startsWith("on")) {
				inner = node.scope = new Scope(scope, node);
			}

			// Delegate down to the expression
			c(node.expr, inner);
		};
		scopeGatherer["QMLStatementBlock"] = function (node, scope, c) {
			var inner = node.scope = new Scope(scope, node);
			for (var i = 0; i < node.statements.length; i++) {
				c(node.statements[i], inner, "Statement");
			}
		};
		scopeGatherer["QMLSignalDefinition"] = function (node, scope, c) {
			// Define the signal arguments in their own separate scope
			var argNames = [],
				argVals = [];
			var fnScope = new Scope(scope, node);
			for (var i = 0; i < node.params.length; i++) {
				var param = node.params[i];
				argNames.push(param.id.name);
				argVals.push(addVar(fnScope, param.id));
			}

			// Define the signal function which can be referenced from JavaScript
			var sig = scope.defProp(node.id.name, new AVal);
			sig.fnType = new Fn(node.id.name, new AVal, argVals, argNames, ANull);
			sig.fnType.fnScope = fnScope;

			// Define the signal handler property
			var handler = scope.defProp(getSignalHandlerName(node.id.name), node.id);
			handler.sig = sig.fnType;
		}
	}

	function extendTernInferExprVisitor(inferExprVisitor) {
		// ret' taken from infer.js
		function ret(f) {
			return function (node, scope, out, name) {
				var r = f(node, scope, name);
				if (out) r.propagate(out);
				return r;
			};
		}

		// Extend the inferExprVisitor methods
		inferExprVisitor["QMLStatementBlock"] = ret(function (node, scope, name) {
			return ANull; // Statement blocks have no type
		});
		inferExprVisitor["QMLObjectLiteral"] = ret(function (node, scope, name) {
			return node.scope.objType;
		});
	}

	function findProp(node, scope) {
		if (node.type === "QMLQualifiedID") {
			// For now we can only find a property for a qualified id if it has a single part
			var prop = node.parts.length > 1 ? null : scope.hasProp(node.parts[0].name)
			if (!prop) {
				// Try to find the full qualified id by name if the previous search was unsuccessful
				prop = scope.hasProp(node.name);
			}
			return prop;
		} else if (node.type === "Identifier") {
			return scope.hasProp(node.name);
		}
		return null;
	}

	function getLastIndex(arr) {
		return arr[arr.length - 1];
	}

	function getSignalHandlerName(str) {
		return "on" + str.charAt(0).toUpperCase() + str.slice(1);
	}

	function extendTernInferWrapper(inferWrapper) {
		// 'infer' taken from infer.js
		function inf(node, scope, out, name) {
			var handler = infer.inferExprVisitor[node.type];
			return handler ? handler(node, scope, out, name) : ANull;
		}

		// Extend the inferWrapper methods
		inferWrapper["QMLObjectLiteral"] = function (node, scope, c) {
			// Define a new Obj which represents this Object Literal
			var obj = node.scope.objType = new Obj(true, node.id.name);
			// node.scope will contain all object properties so we don't have to walk the AST to find them
			node.scope.forAllProps(function (name, prop, curr) {
				if (curr) {
					// Copy the property into the new type so that references to both of them
					// will update the same object.
					obj.props[name] = prop;
				}
			});
			c(node.block, node.scope);
		};
		inferWrapper["QMLPropertyDeclaration"] = function (node, scope, c) {
			var prop = findProp(node.id, scope);
			// Infer the property's type from its assigned type
			switch (node.kind) {
			case "int":
			case "double":
			case "real":
				infer.cx().num.propagate(prop);
				break;
			case "string":
			case "color":
				infer.cx().str.propagate(prop);
				break;
			case "boolean":
				infer.cx().bool.propagate(prop);
				break;
			}
			// Also infer the type from its init expression
			if (node.init) {
				inf(node.init, scope, prop, node.id.name);
				c(node.init, scope);
			}
		};
		inferWrapper["QMLPropertyBinding"] = function (node, scope, c) {
			var prop = findProp(node.id, scope);
			if (prop) {
				if (prop.sig) {
					// This is a signal handler and we should populate its scope with
					// the arguments from its parent function.
					prop.sig.fnScope.forAllProps(function (name, prop, curr) {
						if (curr) {
							node.scope.props[name] = prop;
						}
					});
				} else {
					inf(node.expr, scope, prop, getLastIndex(node.id.parts));
				}
			} else {
				// Check for the 'id' property being set
				var idParts = node.id.parts;
				if (idParts.length == 1 && idParts[0].name === "id") {
					if (node.expr.type === "Identifier") {
						scope.objType.propagate(node.prop);
					}
				}
			}
			c(node.expr, node.scope || scope);
		};
		inferWrapper["QMLStatementBlock"] = function (node, scope, c) {
			for (var i = 0; i < node.statements.length; i++) {
				c(node.statements[i], node.scope, "Statement");
			}
		};
		inferWrapper["QMLSignalDefinition"] = function (node, scope, c) {
			var sig = scope.getProp(node.id.name);
			var retval = new Obj(true, "Signal");
			sig.fnType.retval = retval;
			sig.fnType.propagate(sig);

			var handler = scope.getProp(getSignalHandlerName(node.id.name));
			var obj = new Obj(true, "Signal Handler");
			obj.propagate(handler);
		}
	}

	function extendTernSearchVisitor(searchVisitor) {
		// Extend the search visitor to traverse the scope properly
		searchVisitor["QMLObjectLiteral"] = function (node, scope, c) {
			c(node.block, node.scope);
		};
		searchVisitor["QMLPropertyDeclaration"] = function (node, scope, c) {
			c(node.id, scope);
			if (node.init) {
				c(node.init, scope);
			}
		}
		searchVisitor["QMLPropertyBinding"] = function (node, scope, c) {
			// A binding that is referencing a signal holds a scope.  Other property bindings do not.
			c(node.id, node.scope || scope);
			c(node.expr, node.scope || scope);
		};
		searchVisitor["QMLQualifiedID"] = function (node, scope, c) {
			for (var i = 0; i < node.parts.length; i++) {
				c(node.parts[i], scope);
			}
		};
		searchVisitor["QMLStatementBlock"] = function (node, scope, c) {
			for (var i = 0; i < node.statements.length; i++) {
				c(node.statements[i], node.scope, "Statement");
			}
		};
	}
})