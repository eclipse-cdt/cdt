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
		return mod(require("acorn"), require("acorn/dist/acorn_loose"), require("acorn/dist/walk"), require("acorn-qml"),
				   require("acorn-qml/loose"), require("acorn-qml/walk"), require("tern/lib/infer"), require("tern"));
	if (typeof define === "function" && define.amd) // AMD
		return define([ "acorn", "acorn/dist/acorn_loose", "acorn/dist/walk", "acorn-qml", "acorn-qml/loose", "acorn-qml/walk",
					   "tern/lib/infer", "tern"], mod);
	mod(acorn, acorn, acorn.walk, acorn, acorn, acorn.walk, tern, tern, tern.def); // Plain browser env
})(function (acorn, acornLoose, walk, acornQML, acornQMLLoose, acornQMLWalk, infer, tern, def) {
	'use strict';

	// Outside of the browser environment we have to grab 'def' from 'infer'
	if (!def) {
		def = infer.def;
	}

	// QML Scope Builder
	var ScopeBuilder = function (file, jsDefs) {
		this.file = file;
		this.file.qmlIDScope = new infer.Scope();
		this.file.qmlIDScope.name = "<qml-id>";
		this.jsDefs = jsDefs;
		this.scopes = [];
		this.jsScopes = [];
		this.propScopes = [];
		this.sigScopes = [];
	};

	ScopeBuilder.prototype.getRootScope = function () {
		return this.scopes.length > 0 ? this.scopes[0] : null;
	};

	ScopeBuilder.prototype.isRootScope = function (scope) {
		return scope === this.rootScope();
	};

	ScopeBuilder.prototype.newObjScope = function (node) {
		var rootScope = this.getRootScope(), scope;
		if (!rootScope) {
			scope = new infer.Scope(this.file.scope, node);
		} else {
			scope = new infer.Scope(rootScope, node);
		}
		scope.name = "<qml-obj>";
		this.scopes.push(scope);
		return scope;
	};

	ScopeBuilder.prototype.forEachObjScope = function (callback) {
		for (var i = 0; i < this.scopes.length; i++) {
			var scope = this.scopes[i];
			if (scope.name === "<qml-obj>") {
				callback(scope);
			}
		}
	};

	ScopeBuilder.prototype.getObjScope = function (scope) {
		while (scope && scope.name != "<qml-obj>") {
			scope = scope.prev;
		}
		return scope;
	};

	ScopeBuilder.prototype.getIDScope = function () {
		return this.file.qmlIDScope;
	};

	ScopeBuilder.prototype.newPropertyScope = function (objScope, node) {
		var propScope = new infer.Scope(null, node);
		propScope.name = "<qml-prop>";
		propScope.objScope = objScope;
		this.propScopes.push(propScope);
		return propScope;
	};

	ScopeBuilder.prototype.forEachPropertyScope = function (callback) {
		for (var i = 0; i < this.propScopes.length; i++) {
			callback(this.propScopes[i], this.propScopes[i].objScope);
		}
	};

	ScopeBuilder.prototype.newJSScope = function (scope, node) {
		var jsScope = new infer.Scope(scope, node);
		jsScope.name = "<qml-js>";
		var curOrigin = infer.cx().curOrigin;
		for (var i = 0; i < this.jsDefs.length; ++i)
			def.load(this.jsDefs[i], jsScope);
		infer.cx().curOrigin = curOrigin;
		this.jsScopes.push(jsScope);
		return jsScope;
	};

	ScopeBuilder.prototype.forEachJSScope = function (callback) {
		for (var i = 0; i < this.jsScopes.length; i++) {
			callback(this.jsScopes[i]);
		}
	};

	ScopeBuilder.prototype.newSignalScope = function (scope, node) {
		var sigScope = new infer.Scope(scope, node);
		sigScope.name = "<qml-signal>";
		this.sigScopes.push(sigScope);
		return sigScope
	};

	ScopeBuilder.prototype.hasFunctionScope = function (objScope) {
		var found = null;
		this.forEachFunctionScope(function (scope) {
			if (scope.objScope == objScope) {
				found = scope;
			}
		});
		return found;
	};

	// Helper for adding a property to a scope.
	function addVar(scope, node) {
		return scope.defProp(node.name, node);
	}

	// Helper for finding a property in a scope.
	function findProp(node, scope, pos) {
		if (pos == null || pos < 0) {
			pos = Number.MAX_VALUE;
		}
		if (node.type === "QMLQualifiedID") {
			return (function recurse(i, prop) {
				if (i >= node.parts.length || pos < node.parts[i].start) {
					return prop;
				}
				if (!prop) {
					prop = scope.hasProp(node.parts[i].name);
					if (prop) {
						return recurse(i + 1, prop);
					}
				} else {
					var obj = prop.getType();
					if (obj) {
						var p = obj.hasProp(node.parts[i].name);
						if (p) {
							return recurse(i + 1, p);
						}
					}
				}
				return null;
			})(0, null);
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

	// Helper for getting the current file's scope builder
	function getScopeBuilder() {
		return infer.cx().qmlScopeBuilder;
	}

	// Object which holds two scopes
	function Scopes(object, property) {
		this.object = object;
		this.property = property || object;
	}

	function extendTernScopeGatherer(scopeGatherer) {
		// Build up the scopes in the QML document using the scopeGatherer.  A second
		// pass will be done after the scopeGatherer executes in order to finalize a
		// few of the extra scopes such as the JavaScript, ID, Property, and Signal
		// Handler scopes.
		scopeGatherer["QMLObjectLiteral"] = function (node, scope, c) {
			var inner = node.scope = getScopeBuilder().newObjScope(node);
			c(node.body, inner);
		};
		scopeGatherer["QMLMemberBlock"] = function (node, scope, c) {
			var s = infer.cx().curOrigin;
			var propertyScope = node.scope = getScopeBuilder().newPropertyScope(scope, node);
			var scopes = new Scopes(scope, propertyScope);
			for (var i = 0; i < node.members.length; i++) {
				var member = node.members[i];
				if (member.type === "FunctionDeclaration") {
					c(member, scope);

					// Insert the JavaScript scope after the Function has had a chance to build it's own scope
					var jsScope = getScopeBuilder().newJSScope(scope, member);
					member.scope.prev = member.scope.proto = jsScope;

					// Indicate that the property is a function
					var prop = scope.hasProp(member.id.name);
					if (prop) {
						prop.isFunction = true;
					}
				} else if (member.type === "QMLPropertyDeclaration" || member.type === "QMLPropertyBinding") {
					c(member, scopes);
				} else {
					c(member, scope);
				}
			}
		};
		scopeGatherer["QMLPropertyDeclaration"] = function (node, scopes, c) {
			var inner = scopes.object;
			var prop = addVar(inner, node.id);
			if (node.binding) {
				node.binding.scope = inner;
				c(node.binding, inner);
			}
		};
		scopeGatherer["QMLPropertyBinding"] = function (node, scopes, c) {
			// Create a JavaScript scope if init is a JavaScript environment
			var inner = node.binding.scope = scopes.object;
			// Check for the 'id' property being set
			if (node.id.name == "id") {
				if (node.binding.type === "QMLScriptBinding") {
					var binding = node.binding;
					if (!binding.block && binding.script.type === "Identifier") {
						node.prop = addVar(getScopeBuilder().getIDScope(), binding.script);
					}
				}
			} else {
				// If this appears to be a signal handler, pre-emptively create a new scope that
				// will store references to the signal's arguments
				if (node.id.name.startsWith("on")) {
					inner = node.binding.scope = new infer.Scope(inner, node);
				}
			}

			// Delegate down to the expression
			c(node.binding, inner);
		};
		scopeGatherer["QMLScriptBinding"] = function (node, scope, c) {
			var inner = node.scope = getScopeBuilder().newJSScope(node.scope || scope, node);
			c(node.script, inner);
		};
		scopeGatherer["QMLStatementBlock"] = function (node, scope, c) {
			var inner = getScopeBuilder().newJSScope(node.scope || scope, node);
			node.scope = inner;
			for (var i = 0; i < node.statements.length; i++) {
				c(node.statements[i], inner, "Statement");
			}
		};
		scopeGatherer["QMLSignalDefinition"] = function (node, scope, c) {
			// Scope Builder
			var sb = getScopeBuilder();

			// Define the signal arguments in their own separate scope
			var argNames = [],
				argVals = [];
			var sigScope = sb.newSignalScope(scope, node);
			for (var i = 0; i < node.params.length; i++) {
				var param = node.params[i];
				argNames.push(param.id.name);
				argVals.push(addVar(sigScope, param.id));
			}

			// Define the signal function type which can be referenced from JavaScript
			var sig = addVar(scope, node.id);
			sig.isFunction = true;
			sig.sigType = new infer.Fn(node.id.name, new infer.AVal, argVals, argNames, infer.ANull);
			sig.sigType.sigScope = sigScope;

			// Define the signal handler property
			var handler = scope.defProp(getSignalHandlerName(node.id.name), node.id);
			handler.sig = sig.sigType;
		}
	}

	// 'infer' taken from infer.js
	function inf(node, scope, out, name) {
		var handler = infer.inferExprVisitor[node.type];
		return handler ? handler(node, scope, out, name) : infer.ANull;
	}

	// Infers the property's type from its given primitive value
	function infKind(kind, out) {
		switch (kind) {
			case "int":
			case "double":
			case "real":
				infer.cx().num.propagate(out);
				break;
			case "string":
			case "color":
				infer.cx().str.propagate(out);
				break;
			case "boolean":
				infer.cx().bool.propagate(out);
				break;
			}
	}

	// 'ret' taken from infer.js
	function ret(f) {
		return function (node, scope, out, name) {
			var r = f(node, scope, name);
			if (out) r.propagate(out);
			return r;
		};
	}

	// 'fill' taken from infer.js
	function fill(f) {
		return function(node, scope, out, name) {
			if (!out) out = new AVal;
			f(node, scope, out, name);
			return out;
		};
	}

	function extendTernInferExprVisitor(inferExprVisitor) {
		// Extend the inferExprVisitor methods
		inferExprVisitor["QMLStatementBlock"] = ret(function (node, scope, name) {
			return infer.ANull; // Statement blocks have no type
		});
		inferExprVisitor["QMLScriptBinding"] = fill(function (node, scope, out, name) {
			return inf(node.script, node.scope || scope, out, name);
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
		inferWrapper["QMLMemberBlock"] = function (node, scope, c) {
			var scopes = new Scopes(scope, node.scope);
			for (var i = 0; i < node.members.length; i++) {
				var member = node.members[i];
				if (member.type === "QMLPropertyDeclaration" || member.type === "QMLPropertyBinding") {
					c(member, scopes);
				} else {
					c(member, scope);
				}
			}
		};
		inferWrapper["QMLPropertyDeclaration"] = function (node, scopes, c) {
			var prop = findProp(node.id, scopes.property);
			if (prop) {
				infKind(node.kind, prop);
				if (node.binding) {
					c(node.binding, scopes.object);
					inf(node.binding, scopes.object, prop, node.id.name);
				}
			}
		};
		inferWrapper["QMLPropertyBinding"] = function (node, scopes, c) {
			c(node.binding, scopes.object);
			// Check for the 'id' property being set
			if (node.id.name === "id") {
				if (node.binding.type === "QMLScriptBinding") {
					var binding = node.binding;
					if (binding.script.type === "Identifier") {
						scopes.object.objType.propagate(node.prop);
					}
				}
			} else {
				var prop = findProp(node.id, scopes.property);
				if (prop) {
					if (prop.sig) {
						// This is a signal handler and we should populate its scope with
						// the arguments from its parent function.
						prop.sig.sigScope.forAllProps(function (name, prop, curr) {
							if (curr) {
								node.binding.scope.props[name] = prop;
							}
						});
					} else {
						inf(node.binding, node.binding.scope, prop, getLastIndex(node.id.parts));
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
			for (var i = 0; i < node.params.length; i++) {
				var param = node.params[i];
				infKind(param.kind.name, sig.sigType.args[i]);
			}
			sig.sigType.retval = infer.ANull;
			sig.sigType.propagate(sig);

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
		typeFinder["QMLMemberBlock"] = function (node, scope) {
			return infer.ANull;
		};
		typeFinder["FunctionDeclaration"] = function (node, scope) {
			return scope.name === "<qml-obj>" ? infer.ANull : undefined;
		};
		typeFinder["QMLScriptBinding"] = function (node, scope) {
			// Trick Tern into thinking this node is a type so that it will use
			// this node's scope when handling improperly written script bindings
			return infer.ANull;
		};
		typeFinder["QMLQualifiedID"] = function (node, scope) {
			return findProp(node, scope) || infer.ANull;
		};
		typeFinder["QML_ID"] = function (node, scope) {
			// Reverse the hack from search visitor before finding the property in
			// the id scope
			node.type = "Identifier";
			return findProp(node, getScopeBuilder().getIDScope());
		};
	}

	function extendTernSearchVisitor(searchVisitor) {
		// Extend the search visitor to traverse the scope properly
		searchVisitor["QMLObjectLiteral"] = function (node, scope, c) {
			c(node.body, node.scope);
		};
		searchVisitor["QMLMemberBlock"] = function (node, scope, c) {
			var scopes = new Scopes(scope, node.scope);
			for (var i = 0; i < node.members.length; i++) {
				var member = node.members[i];
				if (member.type === "QMLPropertyDeclaration" || member.type === "QMLPropertyBinding") {
					c(member, scopes);
				} else {
					c(member, scope);
				}
			}
		};
		searchVisitor["QMLSignalDefinition"] = function (node, scope, c) {
			c(node.id, scope);
		};
		searchVisitor["QMLPropertyDeclaration"] = function (node, scopes, c) {
			if (node.binding) {
				c(node.binding, node.binding.scope);
			}
		};
		searchVisitor["QMLPropertyBinding"] = function (node, scopes, c) {
			if (node.id.name === "id") {
				if (node.binding.type === "QMLScriptBinding") {
					var binding = node.binding;
					if (binding.script.type === "Identifier") {
						// Hack to bypass Tern's type finding algorithm which uses node.type instead
						// of the overriden type.
						binding.script.type = "QML_ID";
						c(binding.script, binding.scope, "QML_ID");
						binding.script.type = "Identifier";
					}
				}
				var prop = findProp(node.id, scopes.property);
				if (!prop) {
					return;
				}
			}
			c(node.id, scopes.property);
			c(node.binding, node.binding.scope);
		};
		searchVisitor["QML_ID"] = function(node, st, c) {};
		searchVisitor["QMLStatementBlock"] = function (node, scope, c) {
			for (var i = 0; i < node.statements.length; i++) {
				c(node.statements[i], node.scope, "Statement");
			}
		};
	}

	/*
	* Prepares acorn to consume QML syntax rather than standard JavaScript
	*/
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

	/*
	* Performs a second pass over the generated scopes directly after the scopeGatherer
	* executes and before type inferral begins.  This pass is used to finalize any
	* scopes that require information from other scopes created during the scope
	* gatherer's pass.  This includes the JavaScript scopes which require information
	* from the QML ID scope, as well as the Property scopes which require information
	* from their respective Object Literal scopes.
	*/
	function scopeGatheringSecondPass(ast, scope) {
		var scopeBuilder = getScopeBuilder();

		// Aggregate IDs and Signal Handlers for the JavaScript scopes
		scopeBuilder.forEachJSScope(function (scope) {
			// Merge any QML IDs into all JavaScript scopes
			scopeBuilder.getIDScope().forAllProps(function (name, prop, curr) {
				if (curr) {
					// Since QML checks the idScope before all others, we can safely over-write
					// conflicting property names as they will be hidden anyway.
					scope.props[name] = prop;
				}
			});
		});

		// Aggregate properties for the property scopes
		scopeBuilder.forEachPropertyScope(function (scope, objScope) {
			objScope.forAllProps(function (name, prop, curr) {
				if (curr && !prop.isFunction) {
					scope.props[name] = prop;
				}
			});
		});
	}

	// Register the QML plugin in Tern
	tern.registerPlugin("qml", function (server) {
		// First we want to replace the top-level defs array with our own and save the
		// JavaScript specific defs to a new array 'jsDefs'.  In order to make sure no
		// other plugins mess with the new defs after us, we override addDefs.
		server.jsDefs = server.defs;
		server.defs = [];
		server.addDefs = function (defs, toFront) {
			if (toFront) this.jsDefs.unshift(defs);
			else this.jsDefs.push(defs);
			if (this.cx) this.reset();
		}

		// Hook into server signals
		server.on("preParse", preParse);
		server.on("preInfer", scopeGatheringSecondPass);
		server.on("beforeLoad", function(file) {
			// Create the file's top scope
			file.scope = new infer.Scope(infer.cx().topScope);
			var name = file.name;
			var end = file.name.lastIndexOf(".qml");
			file.scope.name = end > 0 ? name.substring(0, end) : name;

			// Create the ScopeBuilder
			var sb = new ScopeBuilder(file, server.jsDefs);
			infer.cx().qmlScopeBuilder = sb;
		});

		// Extend Tern's inferencing system to include QML syntax
		extendTernScopeGatherer(infer.scopeGatherer);
		extendTernInferExprVisitor(infer.inferExprVisitor);
		extendTernInferWrapper(infer.inferWrapper);
		extendTernTypeFinder(infer.typeFinder);
		extendTernSearchVisitor(infer.searchVisitor);
	});
})