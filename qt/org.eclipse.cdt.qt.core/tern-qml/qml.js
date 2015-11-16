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
		return mod(require("acorn"), require("acorn/dist/acorn_loose"), require("tern/lib/infer"), require("tern/lib/tern"));
	if (typeof define === "function" && define.amd) // AMD
		return define([ "acorn", "acorn/dist/acorn_loose", "tern/lib/infer", "tern/lib/tern", "tern/lib/def"], mod);
	mod(acorn, acorn, tern, tern, tern.def); // Plain browser env
})(function (acorn, acornLoose, infer, tern, def) {
	'use strict';

	// Outside of the browser environment we have to grab 'def' from 'infer'
	if (!def) {
		def = infer.def;
	}

	// Helper for adding a property to a scope.
	function addVar(scope, node) {
		if (node.type === "QMLQualifiedID") {
			var currScope = scope;
			for (var i = 1; i < node.parts.length; i++) {
				var name = node.parts[i].name;
				curr = curr.hasProp(name);
			}
			return prop;
		} else if (node.type === "Identifier") {
			return scope.defProp(node.name, node);
		}
		return null;
	}

	// Helper for finding a property in a scope.
	function findProp(node, scope) {
		if (node.type === "QMLQualifiedID") {
			var curr = scope;
			for (var i = 0; i < node.parts.length; i++) {
				var name = node.parts[i].name;
				curr = curr.hasProp(name);
				if (!curr) {
					return null;
				}
			}
			return curr;
		} else if (node.type === "Identifier") {
			return scope.hasProp(node.name);
		}
		return null;
	}

	// Helper for getting the server from the current context
	function getServer() {
		var parent = infer.cx().parent;
		return parent instanceof tern.Server ? parent : null;
	}

	function preParse(text, options) {
		// Force ECMA Version to 5
		options.ecmaVersion = 5;

		// Register qml plugin with main parser
		var plugins = options.plugins;
		if (!plugins) plugins = options.plugins = {};
		plugins["qml"] = true;

		// Register qml plugin with loose parser
		var pluginsLoose = options.pluginsLoose;
		if (!pluginsLoose) pluginsLoose = options.pluginsLoose = {};
		pluginsLoose["qml"] = true;
	}

	function extendTernScopeGatherer(scopeGatherer) {
		scopeGatherer["QMLImportStatement"] = function (node, scope, c) {
			if (node.qualifier) {
				addVar(scope, node.qualifier.id);
			}
		};
		scopeGatherer["QMLObjectLiteral"] = function (node, scope, c) {
			var inner = node.scope = new infer.Scope(scope, node);
			c(node.body, inner);
		};
		scopeGatherer["QMLPropertyDeclaration"] = function (node, scope, c) {
			var prop = addVar(scope, node.id);
			var inner = scope;
			if (node.init) {
				// Create a JavaScript scope if init is a JavaScript environment
				if (!node.init.type.startsWith("QML")) {
					inner = node.scope = getServer().createJSScope(scope, node);
				}
				c(node.init, inner);
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

			// Create a JavaScript scope if init is a JavaScript environment
			var inner = scope;
			if (!node.expr.type.startsWith("QML")) {
				inner = node.scope = getServer().createJSScope(scope, node);
			} else {
				// If this appears to be a signal handler, pre-emptively create a new scope that
				// will store references to the signal's arguments
				var last = getLastIndex(idParts).name;
				if (last.startsWith("on")) {
					inner = node.scope = new infer.Scope(scope, node);
				}
			}

			// Delegate down to the expression
			c(node.expr, inner);
		};
		scopeGatherer["QMLStatementBlock"] = function (node, scope, c) {
			var inner = node.scope = getServer().createJSScope(scope, node);
			for (var i = 0; i < node.statements.length; i++) {
				c(node.statements[i], inner, "Statement");
			}
		};
		scopeGatherer["QMLSignalDefinition"] = function (node, scope, c) {
			// Define the signal arguments in their own separate scope
			var argNames = [],
				argVals = [];
			var fnScope = new infer.Scope(scope, node);
			for (var i = 0; i < node.params.length; i++) {
				var param = node.params[i];
				argNames.push(param.id.name);
				argVals.push(addVar(fnScope, param.id));
			}

			// Define the signal function type which can be referenced from JavaScript
			var sig = addVar(scope, node.id);
			sig.fnType = new infer.Fn(node.id.name, new infer.AVal, argVals, argNames, infer.ANull);
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
			return infer.ANull; // Statement blocks have no type
		});
		inferExprVisitor["QMLObjectLiteral"] = ret(function (node, scope, name) {
			return node.scope.objType;
		});
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
			return handler ? handler(node, scope, out, name) : infer.ANull;
		}

		// Extend the inferWrapper methods
		inferWrapper["QMLObjectLiteral"] = function (node, scope, c) {
			// Define a new Obj which represents this Object Literal
			var obj = node.scope.objType = new infer.Obj(true, node.id.name);
			// node.scope will contain all object properties so we don't have to walk the AST to find them
			node.scope.forAllProps(function (name, prop, curr) {
				if (curr) {
					// Copy the property into the new type so that references to both of them
					// will update the same object.
					obj.props[name] = prop;
				}
			});
			c(node.body, node.scope);
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
				c(node.init, scope);
				inf(node.init, scope, prop, node.id.name);
			}
		};
		inferWrapper["QMLPropertyBinding"] = function (node, scope, c) {
			c(node.expr, node.scope || scope);
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
		};
		inferWrapper["QMLStatementBlock"] = function (node, scope, c) {
			for (var i = 0; i < node.statements.length; i++) {
				c(node.statements[i], node.scope, "Statement");
			}
		};
		inferWrapper["QMLSignalDefinition"] = function (node, scope, c) {
			var sig = scope.getProp(node.id.name);
			var retval = new infer.Obj(true, "Signal");
			sig.fnType.retval = retval;
			sig.fnType.propagate(sig);

			var handler = scope.getProp(getSignalHandlerName(node.id.name));
			var obj = new infer.Obj(true, "Signal Handler");
			obj.propagate(handler);
		}
	}

	function extendTernTypeFinder(typeFinder) {
		// Extend the type finder to return valid types for QML AST elements
		typeFinder["QMLObjectLiteral"] = function (node, scope) {
			return node.scope.objType;
		};
		typeFinder["QMLStatementBlock"] = function (node, scope) {
			return infer.ANull;
		};
		typeFinder["QMLQualifiedID"] = function (node, scope) {
			return findProp(node, scope);
		}
	}

	function extendTernSearchVisitor(searchVisitor) {
		// Extend the search visitor to traverse the scope properly
		searchVisitor["QMLObjectLiteral"] = function (node, scope, c) {
			c(node.body, node.scope);
		};
		searchVisitor["QMLPropertyBinding"] = function (node, scope, c) {
			c(node.id, scope);
			// A binding that is referencing a signal holds a scope.  Other property bindings do not.
			c(node.expr, node.scope || scope);
		};
		searchVisitor["QMLStatementBlock"] = function (node, scope, c) {
			for (var i = 0; i < node.statements.length; i++) {
				c(node.statements[i], node.scope, "Statement");
			}
		};
	}

	// Register the QML plugin in Tern
	tern.registerPlugin("qml", function (server) {
		// First we want to replace the top-level defs array with our own and save the
		// JavaScript specific defs to a new array 'jsDefs'.  In order to make sure no
		// other plugins mess with the new defs after us, we override addDefs and add
		// a new method called 'addQMLDefs' to facilitate adding QML specific definitions.
		server.jsDefs = server.defs;
		server.defs = [];
		server.addQMLDefs = function (defs, toFront) {
			if (toFront) this.defs.unshift(defs)
			else this.defs.push(defs)
			if (this.cx) this.reset()
		}
		server.addDefs = function (defs, toFront) {
			if (toFront) this.jsDefs.unshift(defs)
			else this.jsDefs.push(defs)
			if (this.cx) this.reset()
		}

		// Add a new method to server which creates a js scope based on jsDefs
		server.createJSScope = function (prev, node, isBlock) {
			var scope = new infer.Scope(prev, node, isBlock);
			if (this.jsDefs) for (var i = 0; i < this.jsDefs.length; ++i)
        		def.load(this.jsDefs[i], scope);
			return scope;
		};

		// Force Tern to use the QML plugin for Acorn
		server.on("preParse", preParse);

		// Add a new scope to a file which will hold its root Object and such
		server.on("beforeLoad", function(file) {
			file.scope = new infer.Scope(infer.cx().topScope);
			file.scope.name = file.name;
		});

		// Extend Tern's inferencing system to include QML syntax
		extendTernScopeGatherer(infer.scopeGatherer);
		extendTernInferExprVisitor(infer.inferExprVisitor);
		extendTernInferWrapper(infer.inferWrapper);
		extendTernTypeFinder(infer.typeFinder);
		extendTernSearchVisitor(infer.searchVisitor);
	});
})