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
		return mod(exports);
	if (typeof define === "function" && define.amd) // AMD
		return define(["exports"], mod);
	mod(root.acornQMLInjector || (root.acornQMLInjector = {})); // Plain browser env
})(this, function (exports) {
	'use strict';

	exports.inject = function (acorn) {
		// Add the 'mode' option to acorn
		acorn.defaultOptions.mode = "qml";

		// Acorn token types
		var tt = acorn.tokTypes;

		// QML token types
		var qtt = acorn.qmlTokTypes = {};
		var keywords = acorn.qmlKeywords = {};

		/*
		 * Shorthand for defining keywords in the 'keywords' variable with the following
		 * format:
		 *    keywords[name].isPrimitive     : if this is a primitive type
		 *    keywords[name].isQMLContextual : if this is a contextual keyword for QML
		 *
		 * Also stores the token's name in qtt._<keyword> for easy referencing later. None
		 * of these keywords will be tokenized and, as such, are allowed to be used in
		 * JavaScript expressions by acorn.  The 'isQMLContextual' boolean in keywords refers
		 * to those contextual keywords that are also contextual in QML's parser rules such
		 * as 'color', 'list', 'alias', etc.
		 */
		function kw(name, options) {
			if (options === undefined)
				options = {};
			qtt["_" + name] = name;
			keywords[name] = {};
			keywords[name].isPrimitive = options.isPrimitive ? true : false;
			keywords[name].isQMLContextual = options.isQMLContextual ? true : false;
		}

		// QML keywords
		kw("import");
		kw("pragma");
		kw("property", { isQMLContextual: true });
		kw("readonly", { isQMLContextual: true });
		kw("signal", { isQMLContextual: true });
		kw("as");
		kw("on", { isQMLContextual: true });
		kw("boolean", { isPrimitive: true });
		kw("double", { isPrimitive: true });
		kw("int", { isPrimitive: true });
		kw("alias", { isPrimitive: true, isQMLContextual: true });
		kw("list", { isPrimitive: true, isQMLContextual: true });
		kw("color", { isPrimitive: true, isQMLContextual: true });
		kw("real", { isPrimitive: true, isQMLContextual: true });
		kw("string", { isPrimitive: true, isQMLContextual: true });
		kw("url", { isPrimitive: true, isQMLContextual: true });

		// Future reserved words
		kw("transient");
		kw("synchronized");
		kw("abstract");
		kw("volatile");
		kw("native");
		kw("goto");
		kw("byte");
		kw("long");
		kw("char");
		kw("short");
		kw("float");

		// QML parser methods
		var pp = acorn.Parser.prototype;

		/*
		 * Parses a set of QML Header Items (QMLImport or QMLPragma)
		 */
		pp.qml_parseHeaderItemList = function () {
			var node = this.startNode();
			node.items = [];

			var loop = true;
			while (loop) {
				if (this.isContextual(qtt._import)) {
					node.items.push(this.qml_parseImport());
				} else if (this.isContextual(qtt._pragma)) {
					node.items.push(this.qml_parsePragma());
				} else {
					loop = false;
				}
			}

			return this.finishNode(node, "QMLHeaderItemList");
		};

		/*
		 * Parses a QML Pragma statement of the form:
		 *    'pragma' <QMLQualifiedID>
		 */
		pp.qml_parsePragma = function () {
			var node = this.startNode();
			this.expectContextual(qtt._pragma);
			node.id = this.qml_parseQualifiedId(true);
			this.semicolon();
			return this.finishNode(node, "QMLPragma");
		};

		/*
		 * Parses a QML Import of the form:
		 *    'import' <QMLModule> [as <QMLQualifier>]
		 *    'import' <StringLiteral> [as <QMLQualifier>]
		 *
		 * as specified by http://doc.qt.io/qt-5/qtqml-syntax-imports.html
		 */
		pp.qml_parseImport = function () {
			var node = this.startNode();

			if (!this.eatContextual(qtt._import)) {
				this.unexpected();
			}

			switch (this.type) {
			case tt.name:
				node.module = this.qml_parseModule();
				node.directory = null;
				break;
			case tt.string:
				node.module = null;
				node.directory = this.parseLiteral(this.value);
				break;
			default:
				this.unexpected();
			}

			// Parse the qualifier, if any
			if (this.isContextual(qtt._as)) {
				node.qualifier = this.qml_parseQualifier();
			}
			this.semicolon();

			return this.finishNode(node, "QMLImport");
		};

		/*
		 * Parses a QML Module of the form:
		 *    <QMLQualifiedId> <QMLVersionLiteral>
		 */
		pp.qml_parseModule = function () {
			var node = this.startNode();

			node.id = this.qml_parseQualifiedId(false);
			if (this.type === tt.num) {
				node.version = this.qml_parseVersionLiteral();
			} else {
				this.unexpected();
			}

			return this.finishNode(node, "QMLModule");
		};

		/*
		 * Parses a QML Version Literal which consists of a major and minor
		 * version separated by a '.'
		 */
		pp.qml_parseVersionLiteral = function () {
			var node = this.startNode();

			node.value = this.value;
			node.raw = this.input.slice(this.start, this.end);
			if (!(/(\d+)\.(\d+)/.exec(node.raw))) {
				this.raise(this.start, "QML module must specify major and minor version");
			}
			this.next();

			return this.finishNode(node, "QMLVersionLiteral");
		};

		/*
		 * Parses a QML Qualifier of the form:
		 *    'as' <Identifier>
		 */
		pp.qml_parseQualifier = function () {
			var node = this.startNode();
			this.expectContextual(qtt._as);
			node.id = this.qml_parseIdent(false);
			return this.finishNode(node, "QMLQualifier");
		};

		/*
		 * Parses a QML Object Definition of the form:
		 *    <QMLQualifiedId> { (<QMLObjectMember>)* }
		 *
		 * http://doc.qt.io/qt-5/qtqml-syntax-basics.html#object-declarations
		 */
		pp.qml_parseObjectDefinition = function (node, isBinding) {
			if (!node) {
				node = this.startNode();
			}
			if (!node.id) {
				node.id = this.qml_parseQualifiedId(false);
			}
			node.body = this.qml_parseObjectInitializer();
			return this.finishNode(node, isBinding ? "QMLObjectBinding" : "QMLObjectDefinition");
		};

		/*
		 * Parses a QML Object Initializer of the form:
		 *    '{' <QMLObjectMember>* '}'
		 */
		pp.qml_parseObjectInitializer = function () {
			var node = this.startNode();
			this.expect(tt.braceL);
			node.members = [];
			while (this.type !== tt.braceR) {
				node.members.push(this.qml_parseObjectMember());
			}
			this.expect(tt.braceR);
			return this.finishNode(node, "QMLObjectInitializer");
		};

		/*
		 * Parses a QML Object Member which can be one of the following:
		 *    - a QML Property Binding
		 *    - a QML Property Declaration
		 *    - a QML Property Modifier
		 *    - a QML Object Literal
		 *    - a JavaScript Function Declaration
		 *    - a QML Signal Definition
		 */
		pp.qml_parseObjectMember = function () {
			if (this.type === tt._default || this.isContextual(qtt._readonly) || this.isContextual(qtt._property)) {
				return this.qml_parsePropertyDeclaration();
			} else if (this.isContextual(qtt._signal)) {
				return this.qml_parseSignalDefinition();
			} else if (this.type === tt._function) {
				return this.qml_parseFunctionMember();
			}
			return this.qml_parseObjectDefinitionOrPropertyBinding();
		};

		/*
		 * Parses a JavaScript function as a member of a QML Object Literal
		 */
		pp.qml_parseFunctionMember = function () {
			var node = this.startNode();
			this.expect(tt._function);
			return this.parseFunction(node, true);
		};

		/*
		 * Parses a QML Object Definition or Property Binding depending on the tokens found.
		 */
		pp.qml_parseObjectDefinitionOrPropertyBinding = function (node) {
			if (!node) {
				node = this.startNode();
			}
			if (!node.id) {
				node.id = this.qml_parseQualifiedId(false);
			}
			switch (this.type) {
			case tt.braceL:
				return this.qml_parseObjectDefinition(node);
			case tt.colon:
				return this.qml_parsePropertyBinding(node);
			}
			this.unexpected();
		};

		/*
		 * Parses a QML Property Modifier of the form:
		 *    <QMLQualifiedID> 'on' <QMLQualifiedID> <QMLInitializer>

		 * TODO: Call this method in the normal parser once we can do lookahead
		 *    Without lookahead, telling the difference between an Object Declaration,
		 *    Property Binding, and Property Modifier would be too difficult.  For now,
		 *    we've implemented a workaround for Object Declarations and Property Bindings
		 *    until Acorn gets lookahead.
		 */
		pp.qml_parsePropertyModifier = function () {
			var node = this.startNode();
			node.kind = this.qml_parseQualifiedID(false);
			this.expectContextual(qtt._on);
			node.id = this.qml_parseQualifiedID(false);
			node.body = this.qml_parseObjectInitializer();
			return this.finishNode(node, "QMLPropertyModifier");
		};

		/*
		 * Parses a QML Property Binding of the form:
		 *    <QMLQualifiedID> <QMLBinding>
		 */
		pp.qml_parsePropertyBinding = function (node) {
			if (!node) {
				node = this.startNode();
			}
			if (!node.id) {
				node.id = this.qml_parseQualifiedId(false);
			}
			this.expect(tt.colon);
			node.binding = this.qml_parseBinding();
			return this.finishNode(node, "QMLPropertyBinding");
		};

		/*
		 * Parses a QML Signal Definition of the form:
		 *    'signal' <Identifier> [(<QMLPropertyType> <Identifier> [',' <QMLPropertyType> <Identifier>]* )]?
		 */
		pp.qml_parseSignalDefinition = function () {
			var node = this.startNode();

			// Parse as a qualified id in case this is not a signal definition
			var signal = this.qml_parseQualifiedId(true);
			if (signal.parts.length === 1) {
				if (signal.name !== qtt._signal) {
					this.unexpected();
				}

				if (this.type === tt.colon || this.type === tt.braceL) {
					// This is a property binding or object literal
					node.id = signal;
					return this.qml_parseObjectDefinitionOrPropertyBinding(node);
				}
			} else {
				// Signal keyword is a qualified ID.  This is not a signal definition
				node.id = signal;
				return this.qml_parseObjectDefinitionOrPropertyBinding(node);
			}

			node.id = this.qml_parseIdent(false);
			this.qml_parseSignalParams(node);
			this.semicolon();
			return this.finishNode(node, "QMLSignalDefinition");
		};

		/*
		 * Parses QML Signal Parameters of the form:
		 *    [(<QMLPropertyType> <Identifier> [',' <QMLPropertyType> <Identifier>]* )]?
		 */
		pp.qml_parseSignalParams = function (node) {
			node.params = [];
			if (this.eat(tt.parenL)) {
				if (!this.eat(tt.parenR)) {
					do {
						var param = this.startNode();
						param.kind = this.qml_parsePropertyType();
						param.id = this.qml_parseIdent(false);
						node.params.push(this.finishNode(param, "QMLParameter"));
					} while (this.eat(tt.comma));
					this.expect(tt.parenR);
				}
			}
		};

		/*
		 * Parses a QML Property Declaration of the form:
		 *    ['default'|'readonly'] 'property' <QMLType> <Identifier> [<QMLBinding>]
		 */
		pp.qml_parsePropertyDeclaration = function () {
			var node = this.startNode();

			// Parse 'default' or 'readonly'
			node.default = false;
			node.readonly = false;
			if (this.eat(tt._default)) {
				node.default = true;
			} else if (this.isContextual(qtt._readonly)) {
				// Parse as a qualified id in case this is not a property declaration
				var readonly = this.qml_parseQualifiedId(true);
				if (readonly.parts.length === 1) {
					if (this.type === tt.colon || this.type === tt.braceL) {
						// This is a property binding or object literal.
						node.id = readonly;
						return this.qml_parseObjectDefinitionOrPropertyBinding(node);
					}
					node.readonly = true;
				} else {
					// Readonly keyword is a qualified ID.  This is not a property declaration.
					node.id = readonly;
					return this.qml_parseObjectDefinitionOrPropertyBinding(node);
				}
			}

			// Parse as a qualified id in case this is not a property declaration
			var property = this.qml_parseQualifiedId(true);
			if (property.parts.length === 1 || node.default || node.readonly) {
				if (property.name !== qtt._property) {
					this.unexpected();
				}

				if (this.type === tt.colon || this.type === tt.braceL) {
					// This is a property binding or object literal.
					node.default = undefined;
					node.readonly = undefined;
					node.id = property;
					return this.qml_parseObjectDefinitionOrPropertyBinding(node);
				}
			} else {
				// Property keyword is a qualified ID.  This is not a property declaration.
				node.default = undefined;
				node.readonly = undefined;
				node.id = property;
				return this.qml_parseObjectDefinitionOrPropertyBinding(node);
			}

			node.kind = this.qml_parsePropertyType();
			if (this.value === "<") {
				this.expect(tt.relational); // '<'
				node.modifier = this.qml_parsePropertyType();
				if (this.value !== ">") {
					this.unexpected();
				}
				this.expect(tt.relational); // '>'
			}

			node.id = this.qml_parseIdent(false);
			if (!this.eat(tt.colon)) {
				node.binding = null;
				this.semicolon();
			} else {
				node.binding = this.qml_parseBinding();
			}

			return this.finishNode(node, "QMLPropertyDeclaration");
		};

		/*
		 * Parses a QML Property Type of the form:
		 *    <Identifier>
		 */
		pp.qml_parsePropertyType = function () {
			var node = this.startNode();
			node.primitive = false;
			if (this.qml_isPrimitiveType(this.type, this.value)) {
				node.primitive = true;
			}
			node.id = this.qml_parseIdent(true);
			return this.finishNode(node, "QMLPropertyType");
		};

		/*
		 * Parses one of the following possibilities for a QML Property assignment:
		 *    - QML Object Binding
		 *    - QML Array Binding
		 *    - QML Script Binding
		 */
		pp.qml_parseBinding = function () {
			if (this.options.mode === "qmltypes") {
				return this.qml_parseScriptBinding(false);
			}

			// TODO: solve ambiguity where a QML Object Literal starts with a
			// Qualified Id that looks very similar to a MemberExpression in
			// JavaScript.  For now, we just won't parse statements like:
			//      test: QMLObject {  }
			//      test: QMLObject.QualifiedId {  }
			return this.qml_parseScriptBinding(true);
		};

		/*
		 * Parses a QML Array Binding of the form:
		 *    '[' [<QMLObjectDefinition> (',' <QMLObjectDefinition>)*] ']'
		 *
		 * TODO: call this in the parser once we can use lookahead to distinguish between
		 *    a QML Array Binding and a JavaScript array.
		 */
		pp.qml_parseArrayBinding = function () {
			var node = this.startNode();
			this.expect(tt.bracketL);
			node.members = [];
			while (!this.eat(tt.bracketR)) {
				node.members.push(this.qml_parseObjectDefinition());
			}
			return this.finishNode(node, "QMLArrayBinding");
		};

		/*
		 * Parses one of the following Script Bindings:
		 *    - Single JavaScript Expression
		 *    - QML Statement Block (A block of JavaScript statements)
		 */
		pp.qml_parseScriptBinding = function (allowStatementBlock) {
			var node = this.startNode();
			node.block = false;
			if (allowStatementBlock && this.type === tt.braceL) {
				node.block = true;
				node.script = this.qml_parseStatementBlock();
			} else {
				node.script = this.parseExpression(false);
				this.semicolon();
			}
			return this.finishNode(node, "QMLScriptBinding");
		};

		/*
		 * Parses a QML Statement Block of the form:
		 *    { <Statement>* }
		 */
		pp.qml_parseStatementBlock = function () {
			var node = this.startNode();
			this.expect(tt.braceL);
			node.body = [];
			while (!this.eat(tt.braceR)) {
				node.body.push(this.parseStatement(true, false));
			}
			return this.finishNode(node, "QMLStatementBlock");
		};

		/*
		 * Parses a Qualified ID of the form:
		 *    <Identifier> ('.' <Identifier>)*
		 *
		 * If 'liberal' is true then this method will allow non-contextual QML keywords as
		 * identifiers.
		 */
		pp.qml_parseQualifiedId = function (liberal) {
			var node = this.startNode();
			node.parts = [];
			node.parts.push(this.qml_parseIdent(liberal));
			while (this.eat(tt.dot)) {
				node.parts.push(this.qml_parseIdent(liberal));
			}

			node.name = "";
			for (var i = 0; i < node.parts.length; i++) {
				node.name += node.parts[i].name;
				if (i < node.parts.length - 1) {
					node.name += ".";
				}
			}

			return this.finishNode(node, "QMLQualifiedID");
		};

		/*
		 * Parses an Identifier in a QML Context.  That is, this method uses 'isQMLContextual'
		 * to throw an error if a non-contextual QML keyword is found.
		 *
		 * If 'liberal' is true then this method will allow non-contextual QML keywords as
		 * identifiers.
		 */
		pp.qml_parseIdent = function (liberal) {
			// Check for non-contextual QML keywords
			if (!liberal) {
				if (!this.qml_isIdent(this.type, this.value)) {
					this.unexpected();
				}
			}
			return this.parseIdent(liberal);
		};

		/*
		 * Returns whether or not a given token type and name can be a QML Identifier.
		 * Uses the 'isQMLContextual' boolean of 'keywords' to determine this.
		 */
		pp.qml_isIdent = function (type, name) {
			if (type === tt.name) {
				var key;
				if ((key = keywords[name])) {
					return key.isQMLContextual;
				}
				return true;
			}
			return false;
		};

		/*
		 * Returns whether or not the current token is a QML primitive type and consumes
		 * it as a side effect if it is.
		 */
		pp.qml_eatPrimitiveType = function (type, name) {
			if (this.qml_isPrimitiveType(type, name)) {
				this.next();
				return true;
			}
			return false;
		};

		/*
		 * Returns whether or not the current token is a QML primitive type.
		 */
		pp.qml_isPrimitiveType = function (type, name) {
			if (name === "var") {
				return true;
			}

			if (type === tt.name) {
				var key;
				if ((key = keywords[name])) {
					return key.isPrimitive;
				}
			}
			return false;
		};

		acorn.plugins.qml = function (instance) {

			// Extend acorn's 'parseTopLevel' method
			instance.extend("parseTopLevel", function (nextMethod) {
				return function (node) {
					// Make parsing simpler by only allowing ECMA Version 5 or older ('import' is
					// not a keyword in this version of ECMA Script).  Qt 5.5 runs with ECMA Script
					// 5 anyway, so this makes sense.
					if (!this.options.ecmaVersion || this.options.ecmaVersion > 5) {
						throw new Error("QML only supports ECMA Script Language Specification 5 or older");
					}

					// Disabled 'qmltypes' mode for now since the normal parser can't parse it anyway
					if (this.options.mode === "qml") {
						// Force strict mode
						this.strict = true;

						// Most of QML's constructs sit at the top-level of the parse tree,
						// replacing JavaScripts top-level.  Here we are parsing such things
						// as the root object literal and header statements of QML.  Eventually,
						// these rules will delegate down to JavaScript expressions.
						node.mode = this.options.mode;
						node.headerItemList = this.qml_parseHeaderItemList();
						node.rootObject = null;
						if (this.type !== tt.eof) {
							node.rootObject = this.qml_parseObjectDefinition();
						}

						if (!this.eat(tt.eof)) {
							this.raise(this.pos, "Expected EOF after QML Root Object");
						}

						return this.finishNode(node, "QMLProgram");
					} else if (this.options.mode === "js") {
						return nextMethod.call(this, node);
					} else {
						throw new Error("Unknown mode '" + this.options.mode + "'");
					}
				};
			});
		};

		return acorn;
	};
});