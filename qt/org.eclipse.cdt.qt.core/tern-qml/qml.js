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
(function (root, mod) {
	if (typeof exports === "object" && typeof module === "object") // CommonJS
		return mod(exports, require("acorn"), require("acorn/dist/acorn_loose"), require("acorn/dist/walk"),
		require("acorn-qml"), require("acorn-qml/loose"), require("acorn-qml/walk"), require("tern"),
		require("tern/lib/infer"), require("tern/lib/signal"));
	if (typeof define === "function" && define.amd) // AMD
		return define(["exports", "acorn/dist/acorn", "acorn/dist/acorn_loose", "acorn/dist/walk", "acorn-qml",
			"acorn-qml/loose", "acorn-qml/walk", "tern", "tern/lib/infer", "tern/lib/signal"], mod);
	mod(root.ternQML || (root.ternQML = {}), acorn, acorn, acorn.walk, acorn, acorn, acorn.walk, tern, tern, tern.signal); // Plain browser env
})(this, function (exports, acorn, acornLoose, walk, acornQML, acornQMLLoose, acornQMLWalk, tern, infer, signal) {
	'use strict';

	// Grab 'def' from 'infer' (used for jsDefs)
	var def = infer.def;

	// 'extend' taken from infer.js
	function extend(proto, props) {
		var obj = Object.create(proto);
		if (props) {
			for (var prop in props) obj[prop] = props[prop];
		}
		return obj;
	}

	// QML Import Handler
	var qmlImportHandler = exports.importHandler = null;
	var ImportHandler = function (server) {
		this.server = server;
		this.imports = null;
	};
	ImportHandler.prototype = {
		reset: function () {
			this.imports = null;
		},
		resolveDirectory: function (file, path) {
			var impl = this.server.options.resolveDirectory;
			if (impl) {
				return impl(file, path);
			}
			// Getting to this point means that we were unable to find an implementation of
			// the 'resolveDirectory' method.  The only time this should happen is during
			// a test case which we expect to have an import of the style "./ ..." and nothing
			// else.  This method will simply remove the './', add the file's base, and return.
			if (!path) {
				// If no path was specified, return the base directory of the file
				var dir = file.name;
				dir = dir.substring(0, dir.lastIndexOf("/") + 1);
				return dir;
			}
			if (path.substring(0, 2) === "./") {
				path = file.directory + path.substring(2);
			}
			if (path.substr(path.length - 1, 1) !== "/") {
				path = path + "/";
			}
			return path;
		},
		resolveModule: function (module) {
			var impl = this.server.options.resolveModule;
			if (impl) {
				return impl(module);
			}
		},
		updateDirectoryImportList: function () {
			if (!this.imports) {
				this.imports = {};
			}
			var dir, f;
			var seenDirs = {};
			for (var i = 0; i < this.server.files.length; i++) {
				var file = this.server.files[i];
				dir = file.directory;
				f = file.nameExt;
				if (!dir) {
					// Resolve the directory name and file name/extension
					dir = file.directory = this.resolveDirectory(file, null);
					f = file.nameExt = this.getFileNameAndExtension(file);
				}
				seenDirs[dir] = true;
				// No file scope means the file was recently added/changed and we should
				// update its import reference
				if (!file.scope) {
					// Check for a valid QML Object Identifier
					if (f.extension === "qml") {
						var ch = f.name.charAt(0);
						if (ch.toUpperCase() === ch && f.name.indexOf(".") === -1) {
							// Create the array for this directory if necessary
							if (!this.imports[dir]) {
								this.imports[dir] = {};
							}

							// Create an Obj to represent this import
							var obj = new infer.Obj(null, f.name);
							obj.origin = file.name;
							this.imports[dir][f.name] = obj;
						}
					}
				}
			}
			for (dir in this.imports) {
				if (!(dir in seenDirs)) {
					this.imports[dir] = undefined;
				}
			}
		},
		getFileNameAndExtension: function (file) {
			var fileName = file.name.substring(file.name.lastIndexOf("/") + 1);
			var dot = fileName.lastIndexOf(".");
			return {
				name: dot >= 0 ? fileName.substring(0, dot) : fileName,
				extension: fileName.substring(dot + 1)
			};
		},
		resolveObject: function (loc, name) {
			return loc[name];
		},
		defineImport: function (scope, loc, name, obj) {
			var prop = scope.defProp(name);
			var objRef = new ObjRef(loc, name, this);
			prop.objType = objRef;
			objRef.propagate(prop);
		},
		defineImports: function (file, scope) {
			scope = scope || file.scope;

			// Add any imports from the current directory
			var imports = this.imports[file.directory];
			var f = file.nameExt;
			if (imports) {
				for (var name in imports) {
					if (f.name !== name) {
						this.defineImport(scope, imports, name, imports[name]);
					}
				}
			}

			// Walk the AST for any imports
			var ih = this;
			walk.simple(file.ast, {
				QMLImport: function (node) {
					var prop = null;
					var scope = file.scope;
					if (node.qualifier) {
						prop = file.scope.defProp(node.qualifier.id.name, node.qualifier.id);
						prop.origin = file.name;
						var obj = new infer.Obj(null, node.qualifier.id.name);
						obj.propagate(prop);
						prop.objType = obj;
						scope = obj;
					}
					if (node.directory) {
						var dir = ih.resolveDirectory(file, node.directory.value);
						var imports = ih.imports[dir];
						if (imports) {
							for (var name in imports) {
								ih.defineImport(scope, imports, name, imports[name]);
							}
						}
					}
				}
			});
		},
		createQMLObjectType: function (file, node, isRoot) {
			// Find the imported object
			var obj = this.getQMLObjectType(file, node.id);
			// If this is the root, connect the imported object to the root object
			if (isRoot) {
				var tmp = this.getRootQMLObjectType(file, node.id);
				if (tmp) {
					// Hook up the Obj Reference
					tmp.proto = obj;
					obj = tmp;
					obj.originNode = node.id;

					// Break any cyclic dependencies
					while ((tmp = tmp.proto)) {
						if (tmp.resolve() == obj.resolve()) {
							tmp.proto = null;
						}
					}
				}
			}
			return obj;
		},
		getQMLObjectType: function (file, qid) {
			var prop = findProp(qid, file.scope);
			if (prop) {
				return prop.objType;
			}
			return new infer.Obj(null, qid.name);
		},
		getRootQMLObjectType: function (file, qid) {
			var f = file.nameExt;
			var imports = this.imports[file.directory];
			if (imports && imports[f.name]) {
				return imports[f.name];
			}
			return new infer.Obj(null, qid.name);
		}
	};

	// 'isInteger' taken from infer.js
	function isInteger(str) {
		var c0 = str.charCodeAt(0);
		if (c0 >= 48 && c0 <= 57) return !/\D/.test(str);
		else return false;
	}

	/*
	 * We have to redefine 'hasProp' to make it work with our scoping.  The original 'hasProp'
	 * function checked proto.props instead of using proto.hasProp.
	 */
	infer.Obj.prototype.hasProp = function (prop, searchProto) {
		if (isInteger(prop)) prop = this.normalizeIntegerProp(prop);
		var found = this.props[prop];
		if (searchProto !== false && this.proto && !found)
			found = this.proto.hasProp(prop, true);
		return found;
	};

	// Creating a resolve function on 'infer.Obj' so we can simplify some of our 'ObjRef' logic
	infer.Obj.prototype.resolve = function () {
		return this;
	};

	/*
	 * QML Object Reference
	 *
	 * An ObjRef behaves exactly the same as an ordinary 'infer.Obj' object, except that it
	 * mirrors its internal state to a referenced object.  This object is resolved by the QML
	 * Import Handler each time the ObjRef is accessed (including getting and setting internal
	 * variables).  In theory this means we don't have to know at runtime whether or not an
	 * object is an ObjRef or an infer.Obj.
	 */
	var ObjRef = function (loc, lookup, ih) {
		// Using underscores for property names so we don't accidentally collide with any
		// 'infer.Obj' property names (which would cause a stack overflow if we were to
		// try to access them here).
		this._loc = loc;
		this._objLookup = lookup;
		this._ih = ih;
		var obj = this.resolve();
		// Use Object.defineProperty to setup getter and setter methods that delegate
		// to the resolved object's properties.  We only need to do this once since all
		// 'infer.Obj' objects should have the same set of property names.
		for (var propertyName in obj) {
			if (!(obj[propertyName] instanceof Function)) {
				(function () {
					var prop = propertyName;
					Object.defineProperty(this, prop, {
						enumerable: true,
						get: function () {
							return this.resolve()[prop];
						},
						set: function (value) {
							this.resolve()[prop] = value;
						}
					});
				}).call(this);
			}
		}
	};
	ObjRef.prototype = extend(infer.Type.prototype, {
		resolve: function () {
			return this._ih.resolveObject(this._loc, this._objLookup);
		}
	});
	(function () {
		// Wire up all base functions to use the resolved object's implementation
		for (var _func in infer.Obj.prototype) {
			if (_func !== "resolve") {
				(function () {
					var fn = _func;
					ObjRef.prototype[fn] = function () {
						return this.resolve()[fn](arguments[0], arguments[1], arguments[2], arguments[3]);
					};
				})();
			}
		}
	})();

	/*
	 * QML Object Scope (inherits methods from infer.Scope)
	 *
	 * A QML Object Scope does not contain its own properties.  Instead, its properties
	 * are defined in its given Object Type and resolved from there.  Any properties
	 * defined within the Object Type are visible without qualifier to any downstream
	 * scopes.
	 */
	var QMLObjScope = exports.QMLObjScope = function (prev, originNode, objType) {
		infer.Scope.call(this, prev, originNode, false);
		this.objType = objType;
	};
	QMLObjScope.prototype = extend(infer.Scope.prototype, {
		hasProp: function (prop, searchProto) {
			// Search for a property in the Object type.
			// Always search the Object Type's prototype as well
			var found = this.objType.hasProp(prop, true);
			if (found) {
				return found;
			}

			// Search for a property in the prototype (previous scope)
			if (this.proto && searchProto !== false) {
				return this.proto.hasProp(prop, searchProto);
			}
		},
		defProp: function (prop, originNode) {
			return this.objType.defProp(prop, originNode);
		},
		removeProp: function (prop) {
			return this.objType.removeProp(prop);
		},
		gatherProperties: function (f, depth) {
			// Gather properties from the Object Type and its prototype(s)
			var obj = this.objType;
			var callback = function (prop, obj, d) {
				f(prop, obj, depth);
			};
			while (obj) {
				obj.gatherProperties(callback, depth);
				obj = obj.proto;
			}
			// gather properties from the prototype (previous scope)
			if (this.proto) {
				this.proto.gatherProperties(f, depth + 1);
			}
		}
	});

	/*
	 * QML Member Scope (inherits methods from infer.Scope)
	 *
	 * A QML Member Scope is a bit of a special case when it comes to QML scoping.  Like
	 * the QML Object Scope, it does not contain any properties of its own.  The reason
	 * that it is special is it only gathers properties from its immediate predecessor
	 * that aren't functions (i.e. They don't have the 'isFunction' flag set.  The
	 * 'isFunction' flag is created by QML signal properties and JavaScript functions
	 * that are QML Members.)
	 */
	var QMLMemScope = exports.QMLMemScope = function (prev, originNode, fileScope) {
		infer.Scope.call(this, prev, originNode, false);
		this.fileScope = fileScope;
	};
	QMLMemScope.prototype = extend(infer.Scope.prototype, {
		hasProp: function (prop, searchProto) {
			// Search for a property in the prototype
			var found = null;
			if (this.proto) {
				// Don't continue searching after the previous scope
				found = this.proto.hasProp(prop, false);
				if (found && !found.isFunction) {
					return found;
				}
			}

			// Search for a property in the file Scope
			if (this.fileScope) {
				return this.fileScope.hasProp(prop, searchProto);
			}
		},
		defProp: function (prop, originNode) {
			return this.prev.defProp(prop, originNode);
		},
		removeProp: function (prop) {
			return this.prev.removeProp(prop);
		},
		gatherProperties: function (f, depth) {
			// Gather properties from the prototype (previous scope)
			var found = null;
			if (this.proto) {
				this.proto.gatherProperties(function (prop, obj, d) {
					// Don't continue passed the predecessor by checking depth
					if (d === depth) {
						var propObj = obj.hasProp(prop);
						if (propObj && !propObj.isFunction) {
							f(prop, obj, d);
						}
					}
				}, depth);
			}
			// Gather properties from the file Scope
			this.fileScope.gatherProperties(f, depth);
		}
	});

	/*
	 * QML JavaScript Scope (inherits methods from infer.Scope)
	 *
	 * A QML JavaScript Scope also contains references to the file's ID Scope, the global
	 * JavaScript Scope, and a possible function parameter scope.  Most likely, this
	 * scope will not contain its own properties.  The resolution order for 'getProp' and
	 * 'hasProp' are:
	 *    1. The ID Scope
	 *    2. This Scope's properties
	 *    3. The Function Scope (if it exists)
	 *    4. The JavaScript Scope
	 *    5. The Previous Scope in the chain
	 */
	var QMLJSScope = exports.QMLJSScope = function (prev, originNode, idScope, jsScope, fnScope) {
		infer.Scope.call(this, prev, originNode, false);
		this.idScope = idScope;
		this.jsScope = jsScope;
		this.fnScope = fnScope;
	};
	QMLJSScope.prototype = extend(infer.Scope.prototype, {
		hasProp: function (prop, searchProto) {
			if (isInteger(prop)) {
				prop = this.normalizeIntegerProp(prop);
			}
			// Search the ID scope
			var found = null;
			if (this.idScope) {
				found = this.idScope.hasProp(prop, searchProto);
			}
			// Search the current scope
			if (!found) {
				found = this.props[prop];
			}
			// Search the Function Scope
			if (!found && this.fnScope) {
				found = this.fnScope.hasProp(prop, searchProto);
			}
			// Search the JavaScript Scope
			if (!found && this.jsScope) {
				found = this.jsScope.hasProp(prop, searchProto);
			}
			// Search the prototype (previous scope)
			if (!found && this.proto && searchProto !== false) {
				found = this.proto.hasProp(prop, searchProto);
			}
			return found;
		},
		gatherProperties: function (f, depth) {
			// Gather from the ID Scope
			if (this.idScope) {
				this.idScope.gatherProperties(f, depth);
			}
			// Gather from the current scope
			for (var prop in this.props) {
				f(prop, this, depth);
			}
			// Gather from the Function Scope
			if (this.fnScope) {
				this.fnScope.gatherProperties(f, depth);
			}
			// Gather from the JS Scope
			if (this.jsScope) {
				this.jsScope.gatherProperties(f, depth);
			}
			// Gather from the prototype (previous scope)
			if (this.proto) {
				this.proto.gatherProperties(f, depth + 1);
			}
		}
	});

	// QML Scope Builder
	var ScopeBuilder = function (file, jsDefs) {
		// File Scope
		this.scope = file.scope;
		this.file = file;
		// ID Scope
		this.idScope = new infer.Scope();
		this.idScope.name = "<qml-id>";
		// JavaScript Scope
		this.jsScope = new infer.Scope();
		this.jsScope.name = "<qml-js>";
		var curOrigin = infer.cx().curOrigin;
		for (var i = 0; i < jsDefs.length; ++i) {
			def.load(jsDefs[i], this.jsScope);
		}
		infer.cx().curOrigin = curOrigin;
	};
	ScopeBuilder.prototype = {
		newObjScope: function (node) {
			var obj = qmlImportHandler.createQMLObjectType(this.file, node, !this.rootScope);
			var scope = new QMLObjScope(this.rootScope || this.scope, node, obj);
			scope.name = "<qml-obj>";
			if (!this.rootScope) {
				this.rootScope = scope;
			}
			return scope;
		},
		getIDScope: function () {
			return this.idScope;
		},
		newMemberScope: function (objScope, node) {
			var memScope = new QMLMemScope(objScope, node, this.scope);
			memScope.name = "<qml-member>";
			return memScope;
		},
		newJSScope: function (scope, node, fnScope) {
			var jsScope = new QMLJSScope(scope, node, this.idScope, this.jsScope, fnScope);
			jsScope.name = "<qml-js>";
			return jsScope;
		},
	};

	// Helper for adding a variable to a scope.
	function addVar(scope, node) {
		return scope.defProp(node.name, node);
	}

	// Helper for finding a property in a scope.
	function findProp(node, scope, pos) {
		if (pos === null || pos === undefined || pos < 0) {
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

	// Helper for getting the current context's scope builder
	function getScopeBuilder() {
		return infer.cx().qmlScopeBuilder;
	}

	// 'infer' taken from infer.js
	function inf(node, scope, out, name) {
		var handler = infer.inferExprVisitor[node.type];
		return handler ? handler(node, scope, out, name) : infer.ANull;
	}

	// Infers the property's type from its given primitive value
	function infKind(kind, out) {
		// TODO: infer list type
		if (kind.primitive) {
			switch (kind.id.name) {
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
		return function (node, scope, out, name) {
			if (!out) out = new infer.AVal();
			f(node, scope, out, name);
			return out;
		};
	}

	// Helper method to get the last index of an array
	function getLastIndex(arr) {
		return arr[arr.length - 1];
	}

	// Helper method to get the signal handler name of a signal function
	function getSignalHandlerName(str) {
		return "on" + str.charAt(0).toUpperCase() + str.slice(1);
	}

	// Object which holds two scopes.  Used to store both the Member Scope and Object
	// Scope for QML Property Bindings and Declarations.
	function Scopes(obj, mem) {
		this.object = obj;
		this.member = mem;
	}

	// Helper to add functionality to a set of walk methods
	function extendWalk(walker, funcs) {
		for (var prop in funcs) {
			walker[prop] = funcs[prop];
		}
	}

	function extendTernScopeGatherer(scopeGatherer) {
		// Extend the Tern scopeGatherer to build up our custom QML scoping
		extendWalk(scopeGatherer, {
			QMLObjectDefinition: function (node, scope, c) {
				var inner = node.scope = getScopeBuilder().newObjScope(node);
				c(node.body, inner);
			},
			QMLObjectBinding: function (node, scope, c) {
				var inner = node.scope = getScopeBuilder().newObjScope(node);
				c(node.body, inner);
			},
			QMLObjectInitializer: function (node, scope, c) {
				var memScope = node.scope = getScopeBuilder().newMemberScope(scope, node);
				for (var i = 0; i < node.members.length; i++) {
					var member = node.members[i];
					if (member.type === "FunctionDeclaration") {
						c(member, scope);

						// Insert the JavaScript scope after the Function has had a chance to build it's own scope
						var jsScope = getScopeBuilder().newJSScope(scope, member, member.scope);
						jsScope.fnType = member.scope.fnType;
						member.scope.prev = member.scope.proto = null;
						member.scope = jsScope;

						// Indicate that the property is a function
						var prop = scope.hasProp(member.id.name);
						if (prop) {
							prop.isFunction = true;
						}
					} else if (member.type === "QMLPropertyDeclaration" || member.type === "QMLPropertyBinding") {
						c(member, new Scopes(scope, memScope));
					} else {
						c(member, scope);
					}
				}
			},
			QMLPropertyDeclaration: function (node, scopes, c) {
				var prop = addVar(scopes.member, node.id);
				if (node.binding) {
					c(node.binding, scopes.object);
				}
			},
			QMLPropertyBinding: function (node, scopes, c) {
				// Check for the 'id' property being set
				if (node.id.name == "id") {
					if (node.binding.type === "QMLScriptBinding") {
						var binding = node.binding;
						if (!binding.block && binding.script.type === "Identifier") {
							node.prop = addVar(getScopeBuilder().getIDScope(), binding.script);
						}
					}
				}
				// Delegate down to the expression
				c(node.binding, scopes.object);
			},
			QMLScriptBinding: function (node, scope, c) {
				var inner = node.scope = getScopeBuilder().newJSScope(scope, node);
				c(node.script, inner);
			},
			QMLStatementBlock: function (node, scope, c) {
				var inner = getScopeBuilder().newJSScope(scope, node);
				node.scope = inner;
				for (var i = 0; i < node.body.length; i++) {
					c(node.body[i], inner, "Statement");
				}
			},
			QMLSignalDefinition: function (node, scope, c) {
				// Scope Builder
				var sb = getScopeBuilder();

				// Define the signal arguments in their own separate scope
				var argNames = [];
				var argVals = [];
				var sigScope = new infer.Scope(null, node);
				for (var i = 0; i < node.params.length; i++) {
					var param = node.params[i];
					argNames.push(param.id.name);
					argVals.push(addVar(sigScope, param.id));
				}

				// Define the signal function type which can be referenced from JavaScript
				var sig = addVar(scope, node.id);
				sig.isFunction = true;
				sig.sigType = new infer.Fn(node.id.name, new infer.AVal(), argVals, argNames, infer.ANull);
				sig.sigType.sigScope = sigScope;

				// Define the signal handler property
				var handler = scope.defProp(getSignalHandlerName(node.id.name), node.id);
				handler.sig = sig.sigType;
			}
		});
	}

	function extendTernInferExprVisitor(inferExprVisitor) {
		// Extend the inferExprVisitor methods
		extendWalk(inferExprVisitor, {
			QMLStatementBlock: ret(function (node, scope, name) {
				return infer.ANull; // TODO: check return statements
			}),
			QMLScriptBinding: fill(function (node, scope, out, name) {
				return inf(node.script, node.scope, out, name);
			}),
			QMLObjectBinding: ret(function (node, scope, name) {
				return node.scope.objType;
			}),
			QMLArrayBinding: ret(function (node, scope, name) {
				return new infer.Arr(null); // TODO: populate with type of array contents
			})
		});
	}

	function extendTernInferWrapper(inferWrapper) {
		// Extend the inferWrapper methods
		extendWalk(inferWrapper, {
			QMLObjectDefinition: function (node, scope, c) {
				c(node.body, node.scope);
			},
			QMLObjectBinding: function (node, scope, c) {
				c(node.body, node.scope);
			},
			QMLObjectInitializer: function (node, scope, c) {
				for (var i = 0; i < node.members.length; i++) {
					var member = node.members[i];
					if (member.type === "QMLPropertyDeclaration" || member.type === "QMLPropertyBinding") {
						c(member, new Scopes(scope, node.scope));
					} else {
						c(member, scope);
					}
				}
			},
			QMLPropertyDeclaration: function (node, scopes, c) {
				var prop = findProp(node.id, scopes.member);
				if (prop) {
					infKind(node.kind, prop);
					if (node.binding) {
						c(node.binding, scopes.object);
						inf(node.binding, scopes.object, prop, node.id.name);
					}
				}
			},
			QMLPropertyBinding: function (node, scopes, c) {
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
					var prop = findProp(node.id, scopes.member);
					if (prop) {
						if (prop.sig) {
							// This is a signal handler
							node.binding.scope.fnScope = prop.sig.sigScope;
						} else {
							inf(node.binding, scopes.object, prop, getLastIndex(node.id.parts));
						}
					}
				}
			},
			QMLScriptBinding: function (node, scope, c) {
				c(node.script, node.scope);
			},
			QMLStatementBlock: function (node, scope, c) {
				for (var i = 0; i < node.body.length; i++) {
					c(node.body[i], node.scope, "Statement");
				}
			},
			QMLSignalDefinition: function (node, scope, c) {
				var sig = scope.getProp(node.id.name);
				for (var i = 0; i < node.params.length; i++) {
					var param = node.params[i];
					infKind(param.kind, sig.sigType.args[i]);
				}
				sig.sigType.retval = infer.ANull;
				sig.sigType.propagate(sig);

				var handler = scope.getProp(getSignalHandlerName(node.id.name));
				var obj = new infer.Obj(true, "Signal Handler");
				obj.propagate(handler);
			}
		});
	}

	function extendTernTypeFinder(typeFinder) {
		// Extend the type finder to return valid types for QML AST elements
		extendWalk(typeFinder, {
			QMLObjectDefinition: function (node, scope) {
				return node.scope.objType;
			},
			QMLObjectBinding: function (node, scope) {
				return node.scope.objType;
			},
			QMLObjectInitializer: function (node, scope) {
				return infer.ANull;
			},
			FunctionDeclaration: function (node, scope) {
				// Quick little hack to get 'findExprAt' to find a Function Declaration which
				// is a QML Object Member.  All other Function Declarations are ignored.
				return scope.name === "<qml-obj>" ? infer.ANull : undefined;
			},
			QMLScriptBinding: function (node, scope) {
				// Trick Tern into thinking this node is a type so that it will use
				// this node's scope when handling improperly written script bindings
				return infer.ANull;
			},
			QMLQualifiedID: function (node, scope) {
				return findProp(node, scope) || infer.ANull;
			},
			QML_ID: function (node, scope) {
				// Reverse the hack from search visitor before finding the property in
				// the id scope
				node.type = "Identifier";
				return findProp(node, getScopeBuilder().getIDScope());
			}
		});
	}

	function extendTernSearchVisitor(searchVisitor) {
		// Extend the search visitor to traverse the scope properly
		extendWalk(searchVisitor, {
			QMLObjectDefinition: function (node, scope, c) {
				c(node.body, node.scope);
			},
			QMLObjectBinding: function (node, scope, c) {
				c(node.body, node.scope);
			},
			QMLObjectInitializer: function (node, scope, c) {
				for (var i = 0; i < node.members.length; i++) {
					var member = node.members[i];
					if (member.type === "QMLPropertyDeclaration" || member.type === "QMLPropertyBinding") {
						c(member, new Scopes(scope, node.scope));
					} else {
						c(member, scope);
					}
				}
			},
			QMLSignalDefinition: function (node, scope, c) {
				c(node.id, scope);
			},
			QMLPropertyDeclaration: function (node, scopes, c) {
				c(node.id, scopes.member);
				if (node.binding) {
					c(node.binding, scopes.object);
				}
			},
			QMLPropertyBinding: function (node, scopes, c) {
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
					var prop = findProp(node.id, scopes.member);
					if (!prop) {
						return;
					}
				}
				c(node.id, scopes.member);
				c(node.binding, scopes.object);
			},
			QMLScriptBinding: function (node, scope, c) {
				c(node.script, node.scope);
			},
			QML_ID: function (node, st, c) {
				// Ignore
			},
			QMLStatementBlock: function (node, scope, c) {
				for (var i = 0; i < node.body.length; i++) {
					c(node.body[i], node.scope, "Statement");
				}
			}
		});
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
		plugins.qml = true;

		// Register qml plugin with loose parser
		var pluginsLoose = options.pluginsLoose;
		if (!pluginsLoose) pluginsLoose = options.pluginsLoose = {};
		pluginsLoose.qml = true;
	}

	/*
	 * Initializes the file's top level scope and creates a ScopeBuilder to facilitate
	 * the creation of QML scopes.
	 */
	function beforeLoad(file) {
		// We dont care for the Context's top scope
		file.scope = null;

		// Update the ImportHandler
		qmlImportHandler.updateDirectoryImportList();

		// Create the file's top scope
		file.scope = new infer.Scope(infer.cx().topScope);
		var name = file.name;
		var end = file.name.lastIndexOf(".qml");
		file.scope.name = end > 0 ? name.substring(0, end) : name;

		// Get the ImportHandler to define imports for us
		qmlImportHandler.defineImports(file, file.scope);

		// Create the ScopeBuilder
		var sb = new ScopeBuilder(file, infer.cx().parent.jsDefs);
		infer.cx().qmlScopeBuilder = sb;
	}

	/*
	 * Helper to reset some of the internal state of the QML plugin when the server
	 * resets
	 */
	function reset() {
		qmlImportHandler.reset();
	}

	/*
	 * Called when a completions query is made to the server
	 */
	function completions(file, query) {
		// We can get relatively simple completions on QML Object Types for free if we
		// update the Context.paths variable.  Tern uses this variable to complete
		// non Member Expressions that contain a '.' character.  Will be much more
		// accurate if we just roll our own completions for this in the future, but it
		// works relatively well for now.
		var cx = infer.cx();
		cx.paths = {};
		for (var prop in file.scope.props) {
			cx.paths[prop] = file.scope[prop];
		}
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
		};

		// Create a method on the server object that parses a string. We can't make this
		// a query due to the fact that tern always does its infer processing on every
		// query regardless of its contents.
		server.parseString = function (text, options, callback) {
			try {
				var opts = {
					allowReturnOutsideFunction: true,
					allowImportExportEverywhere: true,
					ecmaVersion: this.options.ecmaVersion
				};
				for (var opt in options) {
					opts[opt] = options[opt];
				}
				text = this.signalReturnFirst("preParse", text, opts) || text;
				var ast = infer.parse(text, opts);
				callback(null, {
					ast: ast
				});
				this.signal("postParse", ast, text);
			} catch (err) {
				callback(err, null);
			}
		};

		// Create the QML Import Handler
		qmlImportHandler = exports.importHandler = new ImportHandler(server);

		// Define the 'parseFile' query type.
		tern.defineQueryType("parseFile", {
			takesFile: true,
			run: function (srv, query, file) {
				return {
					ast: file.ast
				};
			}
		});

		// Hook into server signals
		server.on("preParse", preParse);
		server.on("beforeLoad", beforeLoad);
		server.on("postReset", reset);
		server.on("completion", completions);

		// Extend Tern's inferencing system to include QML syntax
		extendTernScopeGatherer(infer.scopeGatherer);
		extendTernInferExprVisitor(infer.inferExprVisitor);
		extendTernInferWrapper(infer.inferWrapper);
		extendTernTypeFinder(infer.typeFinder);
		extendTernSearchVisitor(infer.searchVisitor);
	});
});