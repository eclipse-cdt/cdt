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

var plugin = require("../qml.js");
var infer = require("tern/lib/infer");
var driver = require("./driver.js");
var test = driver.test;
var groupStart = driver.groupStart;
var groupEnd = driver.groupEnd;

var group = exports.group = "Custom Scoping";
groupStart(group);

test("QMLObjScope::defProp()", function (server, callback, name) {
	infer.withContext(new infer.Context([], server), function () {
		// Create the scope and objects to test
		var fileScope = new infer.Scope();
		var obj = new infer.Obj(null, "MyObject");
		var scope = new plugin.QMLObjScope(fileScope, null, obj);

		// Define some properties
		scope.defProp("first", null);
		scope.defProp("second", null);

		// Make sure there are no properties in the scopes themselves
		if (fileScope.props && fileScope.props.length > 0) {
			return callback("fail", name, "- File scope contained properties (none expected)");
		}
		if (scope.props && scope.props.length > 0) {
			return callback("fail", name, "- QMLObjScope contained properties (none expected)");
		}

		// Check the Obj Type for the props we created
		if (!obj.props.first) {
			return callback("fail", name, "- Obj did not contain property 'first'");
		}
		if (!obj.props.second) {
			return callback("fail", name, "- Obj did not contain property 'second'");
		}
		return callback("ok", name);
	});
});

test("QMLObjScope::hasProp()", function (server, callback, name) {
	infer.withContext(new infer.Context([], server), function () {
		// Create the scope and objects to test
		var fileScope = new infer.Scope();
		var obj = new infer.Obj(null, "MyObject");
		var scope = new plugin.QMLObjScope(fileScope, null, obj);

		// Define a property on the Obj Type
		obj.defProp("first", null);

		// Query the scope for the prop we created and make sure it returns the right one
		var prop = scope.hasProp("first", true);
		if (!prop) {
			return callback("fail", name, "- hasProp('first') returned null");
		} else if (prop.propertyName !== "first") {
			return callback("fail", name, "- hasProp('first') returned invalid property");
		}
		return callback("ok", name);
	});
});

test("QMLObjScope::removeProp()", function (server, callback, name) {
	infer.withContext(new infer.Context([], server), function () {
		// Create the scope and objects to test
		var fileScope = new infer.Scope();
		var obj = new infer.Obj(null, "MyObject");
		var scope = new plugin.QMLObjScope(fileScope, null, obj);

		// Define some properties
		scope.defProp("first", null);
		scope.defProp("second", null);

		// Remove the properties we defined
		scope.removeProp("first");
		scope.removeProp("second");

		// Check the Obj Type for the props we created
		if (obj.props && obj.props.length > 0) {
			return callback("fail", name, "- Obj contained properties (none expected)");
		}
		return callback("ok", name);
	});
});

test("QMLObjScope::gatherProperties()", function (server, callback, name) {
	infer.withContext(new infer.Context([], server), function () {
		// Create the scope and objects to test
		var fileScope = new infer.Scope();
		var obj = new infer.Obj(null, "MyObject");
		var scope = new plugin.QMLObjScope(fileScope, null, obj);

		// Define some properties
		fileScope.defProp("third", null);
		scope.defProp("first", null);
		scope.defProp("second", null);

		// Gather the properties and store them in the order they were received
		var props = [];
		scope.gatherProperties(function (prop, obj, depth) {
			props.push(prop);
		});

		// Check the gathered properties for correctness (order matters)
		if (props.length !== 3) {
			return callback("fail", name, "- Invalid number of properties gathered (" + props.length + ")");
		}
		if (props[0] !== "first") {
			return callback("fail", name, "- props[0] was not property 'first'");
		}
		if (props[1] !== "second") {
			return callback("fail", name, "- props[1] was not property 'second'");
		}
		if (props[2] !== "third") {
			return callback("fail", name, "- props[2] was not property 'third'");
		}
		return callback("ok", name);
	});
});

test("QMLMemScope::defProp()", function (server, callback, name) {
	infer.withContext(new infer.Context([], server), function () {
		// Create the scope and objects to test
		var fileScope = new infer.Scope();
		var obj = new infer.Obj(null, "MyObject");
		var objScope = new plugin.QMLObjScope(fileScope, null, obj);
		var scope = new plugin.QMLMemScope(objScope, null, fileScope);

		// Define some properties
		scope.defProp("first", null);
		scope.defProp("second", null);

		// Make sure there are no properties in the scopes themselves
		if (fileScope.props && fileScope.props.length > 0) {
			return callback("fail", name, "- File scope contained properties (none expected)");
		}
		if (scope.props && scope.props.length > 0) {
			return callback("fail", name, "- QMLMemScope contained properties (none expected)");
		}

		// Check the Obj Type for the props we created
		if (!obj.props.first) {
			return callback("fail", name, "- Obj did not contain property 'first'");
		}
		if (!obj.props.second) {
			return callback("fail", name, "- Obj did not contain property 'second'");
		}
		return callback("ok", name);
	});
});

test("QMLMemScope::hasProp()", function (server, callback, name) {
	infer.withContext(new infer.Context([], server), function () {
		// Create the scope and objects to test
		var fileScope = new infer.Scope();
		var obj = new infer.Obj(null, "MyObject");
		var objScope = new plugin.QMLObjScope(fileScope, null, obj);
		var scope = new plugin.QMLMemScope(objScope, null, fileScope);

		// Define a property on the Obj Type and File Scope
		obj.defProp("first", null);
		fileScope.defProp("second", null);

		// Query the scope for the prop we created and make sure it returns the right one
		var prop = scope.hasProp("first", true);
		if (!prop) {
			return callback("fail", name, "- hasProp('first') returned null");
		} else if (prop.propertyName !== "first") {
			return callback("fail", name, "- hasProp('first') returned invalid property");
		}
		prop = scope.hasProp("second", true);
		if (!prop) {
			return callback("fail", name, "- hasProp('second') returned null");
		} else if (prop.propertyName !== "second") {
			return callback("fail", name, "- hasProp('second') returned invalid property");
		}
		return callback("ok", name);
	});
});

test("QMLMemScope::hasProp() [Multiple Parent Scopes]", function (server, callback, name) {
	infer.withContext(new infer.Context([], server), function () {
		// Create the scope and objects to test
		var fileScope = new infer.Scope();
		var rootObj = new infer.Obj(null, "Root");
		var rootObjScope = new plugin.QMLObjScope(fileScope, null, rootObj);
		var obj = new infer.Obj(null, "MyObject");
		var objScope = new plugin.QMLObjScope(rootObjScope, null, obj);
		var scope = new plugin.QMLMemScope(objScope, null, fileScope);

		// Define a property on the Root Obj Type and Obj Type
		rootObj.defProp("notVisible", null);
		obj.defProp("visible", null);

		// Query the scope for the prop we created and make sure it returns the right one
		var prop = scope.hasProp("notVisible", true);
		if (prop) {
			return callback("fail", name, "- found property 'notVisible'");
		}
		prop = scope.hasProp("visible", true);
		if (!prop) {
			return callback("fail", name, "- hasProp('visible') returned null");
		} else if (prop.propertyName !== "visible") {
			return callback("fail", name, "- hasProp('visible') returned invalid property");
		}
		return callback("ok", name);
	});
});

test("QMLMemScope::removeProp()", function (server, callback, name) {
	infer.withContext(new infer.Context([], server), function () {
		// Create the scope and objects to test
		var fileScope = new infer.Scope();
		var obj = new infer.Obj(null, "MyObject");
		var objScope = new plugin.QMLObjScope(fileScope, null, obj);
		var scope = new plugin.QMLMemScope(objScope, null, fileScope);

		// Define some properties
		scope.defProp("first", null);
		scope.defProp("second", null);

		// Remove the properties we defined
		scope.removeProp("first");
		scope.removeProp("second");

		// Check the Obj Type for the props we created
		if (obj.props && obj.props.length > 0) {
			return callback("fail", name, "- Obj contained properties (none expected)");
		}
		return callback("ok", name);
	});
});

test("QMLMemScope::gatherProperties()", function (server, callback, name) {
	infer.withContext(new infer.Context([], server), function () {
		// Create the scope and objects to test
		var fileScope = new infer.Scope();
		var obj = new infer.Obj(null, "MyObject");
		var objScope = new plugin.QMLObjScope(fileScope, null, obj);
		var scope = new plugin.QMLMemScope(objScope, null, fileScope);

		// Define some properties
		fileScope.defProp("third", null);
		scope.defProp("first", null);
		scope.defProp("second", null);

		// Gather the properties and store them in the order they were received
		var props = [];
		scope.gatherProperties(function (prop, obj, depth) {
			props.push(prop);
		});

		// Check the gathered properties for correctness (order matters)
		if (props.length !== 3) {
			return callback("fail", name, "- Invalid number of properties gathered (" + props.length + ")");
		}
		if (props[0] !== "first") {
			return callback("fail", name, "- props[0] was not property 'first'");
		}
		if (props[1] !== "second") {
			return callback("fail", name, "- props[1] was not property 'second'");
		}
		if (props[2] !== "third") {
			return callback("fail", name, "- props[2] was not property 'third'");
		}
		return callback("ok", name);
	});
});

test("QMLMemScope::gatherProperties() [Multiple Parent Scopes]", function (server, callback, name) {
	infer.withContext(new infer.Context([], server), function () {
		// Create the scope and objects to test
		var fileScope = new infer.Scope();
		var rootObj = new infer.Obj(null, "Root");
		var rootObjScope = new plugin.QMLObjScope(fileScope, null, rootObj);
		var obj = new infer.Obj(null, "MyObject");
		var objScope = new plugin.QMLObjScope(rootObjScope, null, obj);
		var scope = new plugin.QMLMemScope(objScope, null, fileScope);

		// Define a property on the Root Obj Type and Obj Type
		rootObj.defProp("notVisible", null);
		obj.defProp("visible", null);

		// Gather the properties and store them in the order they were received
		var props = [];
		scope.gatherProperties(function (prop, obj, depth) {
			props.push(prop);
		});

		// Check the gathered properties for correctness (order matters)
		if (props.length !== 1) {
			return callback("fail", name, "- Invalid number of properties gathered (" + props.length + ")");
		}
		if (props[0] !== "visible") {
			return callback("fail", name, "- props[0] was not property 'visible'");
		}
		return callback("ok", name);
	});
});

test("QMLJSScope::hasProp()", function (server, callback, name) {
	infer.withContext(new infer.Context([], server), function () {
		// Create the scope and objects to test
		var fileScope = new infer.Scope();
		var idScope = new infer.Scope(null, "<qml-id>");
		var jsScope = new infer.Scope(null, "<qml-js>");
		var fnScope = new infer.Scope(null, "<qml-fn>");
		var scope = new plugin.QMLJSScope(fileScope, null, idScope, jsScope, fnScope);

		// Define properties in each scope
		fileScope.defProp("first", null);
		idScope.defProp("second", null);
		jsScope.defProp("third", null);
		fnScope.defProp("fourth", null);
		scope.defProp("fifth", null);

		// Query the scope for the prop we created and make sure it returns the right one
		var prop = scope.hasProp("first", true);
		if (!prop) {
			return callback("fail", name, "- hasProp('first') returned null");
		} else if (prop.propertyName !== "first") {
			return callback("fail", name, "- hasProp('first') returned invalid property");
		}
		prop = scope.hasProp("second", true);
		if (!prop) {
			return callback("fail", name, "- hasProp('second') returned null");
		} else if (prop.propertyName !== "second") {
			return callback("fail", name, "- hasProp('second') returned invalid property");
		}
		prop = scope.hasProp("third", true);
		if (!prop) {
			return callback("fail", name, "- hasProp('third') returned null");
		} else if (prop.propertyName !== "third") {
			return callback("fail", name, "- hasProp('third') returned invalid property");
		}
		prop = scope.hasProp("fourth", true);
		if (!prop) {
			return callback("fail", name, "- hasProp('fourth') returned null");
		} else if (prop.propertyName !== "fourth") {
			return callback("fail", name, "- hasProp('fourth') returned invalid property");
		}
		prop = scope.hasProp("fifth", true);
		if (!prop) {
			return callback("fail", name, "- hasProp('fifth') returned null");
		} else if (prop.propertyName !== "fifth") {
			return callback("fail", name, "- hasProp('fifth') returned invalid property");
		}
		return callback("ok", name);
	});
});

test("QMLJSScope::gatherProperties()", function (server, callback, name) {
	infer.withContext(new infer.Context([], server), function () {
		// Create the scope and objects to test
		// Create the scope and objects to test
		var fileScope = new infer.Scope();
		var idScope = new infer.Scope(null, "<qml-id>");
		var jsScope = new infer.Scope(null, "<qml-js>");
		var fnScope = new infer.Scope(null, "<qml-fn>");
		var scope = new plugin.QMLJSScope(fileScope, null, idScope, jsScope, fnScope);

		// Define properties in each scope
		fileScope.defProp("fifth", null);
		idScope.defProp("first", null);
		jsScope.defProp("fourth", null);
		fnScope.defProp("third", null);
		scope.defProp("second", null);

		// Gather the properties and store them in the order they were received
		var props = [];
		scope.gatherProperties(function (prop, obj, depth) {
			props.push(prop);
		});

		// Check the gathered properties for correctness (order matters)
		if (props.length !== 5) {
			return callback("fail", name, "- Invalid number of properties gathered (" + props.length + ")");
		}
		if (props[0] !== "first") {
			return callback("fail", name, "- props[0] was not property 'first'");
		}
		if (props[1] !== "second") {
			return callback("fail", name, "- props[1] was not property 'second'");
		}
		if (props[2] !== "third") {
			return callback("fail", name, "- props[2] was not property 'third'");
		}
		if (props[3] !== "fourth") {
			return callback("fail", name, "- props[3] was not property 'fourth'");
		}
		if (props[4] !== "fifth") {
			return callback("fail", name, "- props[4] was not property 'fifth'");
		}
		return callback("ok", name);
	});
});

groupEnd();