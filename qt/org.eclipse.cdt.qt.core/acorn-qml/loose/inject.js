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

// This will only be visible globally if we are in a browser environment
var injectQMLLoose;

(function (root, mod) {
	if (typeof exports === "object" && typeof module === "object") // CommonJS
		return mod(module.exports);
	if (typeof define === "function" && define.amd) // AMD
		return define(["exports"], mod);
	mod(root.acornQMLLooseInjector || (root.acornQMLLooseInjector = {})); // Plain browser env
})(this, function (exports) {
	"use strict";

	exports.inject = function (acorn) {
		// Acorn token types
		var tt = acorn.tokTypes;

		// QML token types
		var qtt = acorn.qmlTokTypes;
		var keywords = acorn.qmlKeywords;

		// QML parser methods
		var lp = acorn.LooseParser.prototype;
		var pp = acorn.Parser.prototype;

		/*
		 * Parses a set of QML Header Statements which can either be of
		 * the type import or pragma
		 */
		lp.qml_parseHeaderStatements = function () {
			var node = this.startNode();
			node.statements = [];

			var loop = true;
			while (loop) {
				if (this.isContextual(qtt._import)) {
					node.statements.push(this.qml_parseImportStatement());
				} else if (this.isContextual(qtt._pragma)) {
					node.statements.push(this.qml_parsePragmaStatement());
				} else {
					loop = false;
				}
			}

			return this.finishNode(node, "QMLHeaderStatements");
		};

		/*
		 * Parses a QML Pragma statement of the form:
		 *    'pragma' <Identifier>
		 */
		lp.qml_parsePragmaStatement = function () {
			var node = this.startNode();
			this.expectContextual(qtt._pragma);
			node.id = this.parseIdent(false);
			this.semicolon();
			return this.finishNode(node, "QMLPragmaStatement");
		};

		/*
		 * Parses a QML Import statement of the form:
		 *    'import' <ModuleIdentifier> <Version.Number> [as <Qualifier>]
		 *    'import' <DirectoryPath> [as <Qualifier>]
		 *
		 * as specified by http://doc.qt.io/qt-5/qtqml-syntax-imports.html
		 */
		lp.qml_parseImportStatement = function () {
			var node = this.startNode();
			this.expectContextual(qtt._import);

			switch (this.tok.type) {
			case tt.string:
				node.module = null;
				node.directory = this.parseExprAtom();
				break;
			default:
				node.module = this.qml_parseModule();
				node.directory = null;
				break;
			}

			// Parse the qualifier, if any
			if (this.isContextual(qtt._as)) {
				node.qualifier = this.qml_parseQualifier();
			}
			this.semicolon();

			return this.finishNode(node, "QMLImportStatement");
		};

		/*
		 * Parses a QML Module of the form:
		 *    <QMLQualifiedId> <QMLVersionLiteral> ['as' <QMLQualifier>]?
		 */
		lp.qml_parseModule = function () {
			var node = this.startNode();

			node.id = this.qml_parseQualifiedId(false);
			node.version = this.qml_parseVersionLiteral();

			return this.finishNode(node, "QMLModule");
		};

		/*
		 * Parses a QML Version Literal which consists of a major and minor
		 * version separated by a '.'
		 */
		lp.qml_parseVersionLiteral = function () {
			var node = this.startNode();

			node.raw = this.input.slice(this.tok.start, this.tok.end);
			node.value = this.tok.value;
			var matches;
			if (this.tok.type === tt.num) {
				if ((matches = /(\d+)\.(\d+)/.exec(node.raw))) {
					node.major = parseInt(matches[1]);
					node.minor = parseInt(matches[2]);
					this.next();
				} else {
					node.major = parseInt(node.raw);
					node.minor = 0;
					this.next();
				}
			} else {
				node.major = 0;
				node.minor = 0;
				node.value = 0;
				node.raw = "0.0";
			}

			return this.finishNode(node, "QMLVersionLiteral");
		};

		/*
		 * Parses a QML Qualifier of the form:
		 *    'as' <Identifier>
		 */
		lp.qml_parseQualifier = function () {
			var node = this.startNode();
			this.expectContextual(qtt._as);
			node.id = this.qml_parseIdent(false);
			return this.finishNode(node, "QMLQualifier");
		};

		/*
		 * Parses a QML Object Literal of the form:
		 *    <QualifiedId> { (<QMLMember>)* }
		 *
		 * http://doc.qt.io/qt-5/qtqml-syntax-basics.html#object-declarations
		 */
		lp.qml_parseObjectLiteral = function () {
			var node = this.startNode();
			node.id = this.qml_parseQualifiedId(false);
			node.body = this.qml_parseMemberBlock();
			return this.finishNode(node, "QMLObjectLiteral");
		};

		/*
		 * Parses a QML Member Block of the form:
		 *    { <QMLMember>* }
		 */
		lp.qml_parseMemberBlock = function () {
			var node = this.startNode();
			this.pushCx();
			this.expect(tt.braceL);
			var blockIndent = this.curIndent, line = this.curLineStart;
			node.members = [];
			while (!this.closes(tt.braceR, blockIndent, line, true)) {
				var member = this.qml_parseMember();
				if (member) {
					node.members.push(member);
				}
			}
			this.popCx();
			this.eat(tt.braceR);
			return this.finishNode(node, "QMLMemberBlock");
		};

		/*
		 * Parses a QML Member which can be one of the following:
		 *    - a QML Property Binding
		 *    - a Property Declaration (or Alias)
		 *    - a QML Object Literal
		 *    - a JavaScript Function Declaration
		 *    - a Signal Definition
		 */
		lp.qml_parseMember = function () {
			if (this.tok.type === tt._default || this.isContextual(qtt._readonly) || this.isContextual(qtt._property) || this.qml_isPrimitiveType(this.tok.type, this.tok.value)) {
				return this.qml_parsePropertyDeclaration();
			} else if (this.isContextual(qtt._signal)) {
				return this.qml_parseSignalDefinition();
			} else if (this.tok.type === tt._function) {
				return this.qml_parseFunctionMember();
			} else if (this.qml_isIdent(this.tok.type, this.tok.value) || this.tok.type === tt.dot) {
				var la = this.lookAhead(1);
				if (this.qml_isIdent(la.type, la.value)) {
					// Two identifiers in a row means this is most likely a property declaration
					// with the 'property' token missing
					return this.qml_parsePropertyDeclaration();
				} else {
					var node = this.qml_parseObjectLiteralOrPropertyBinding();
					if (node) {
						return node;
					} else {
						return this.qml_parsePropertyBinding();
					}
				}
			} else if (this.tok.type === tt.colon) {
				return this.qml_parsePropertyBinding();
			} else if (this.tok.type === tt.braceL) {
				return this.qml_parseObjectLiteral();
			}
			// ignore the current token if it didn't pass the previous tests
			this.next();
		};

		/*
		 * Parses a JavaScript function as a member of a QML Object Literal
		 */
		lp.qml_parseFunctionMember = function () {
			var node = this.startNode();
			this.expect(tt._function);
			return this.qml_parseFunction(node, true);
		};

		/*
		* QML version of 'parseFunction' needed to have proper error tolerant parsing
		* for QML member functions versus their JavaScript counterparts.  The main
		* difference between the two functions is that this implementation will not
		* forcefully insert '(' and '{' tokens for the body and parameters.  Instead,
		* it will silently create an empty parameter list or body and let parsing
		* continue normally.
		*/
		lp.qml_parseFunction = function(node, isStatement) {
			this.initFunction(node);
			if (this.tok.type === tt.name) node.id = this.parseIdent();
			else if (isStatement) node.id = this.dummyIdent();
			node.params = this.tok.type === tt.parenL ? this.parseFunctionParams() : [];
			if (this.tok.type === tt.braceL) {
				node.body = this.parseBlock();
			} else {
				if (this.options.locations) {
					node.body = this.startNodeAt([ this.last.end, this.last.loc.end ]);
				} else {
					node.body = this.startNodeAt(this.last.end);
				}
				node.body.body = [];
				this.finishNode(node.body, "BlockStatement");
			}
			return this.finishNode(node, isStatement ? "FunctionDeclaration" : "FunctionExpression");
		};

		/*
		 * Parses a QML Object Literal or Property Binding depending on the tokens found.
		 */
		lp.qml_parseObjectLiteralOrPropertyBinding = function () {
			var i = 1, la = this.tok;
			if (this.qml_isIdent(la.type, la.value)) {
				la = this.lookAhead(i++);
			}
			while (la.type === tt.dot) {
				la = this.lookAhead(i++);
				if (this.qml_isIdent(la.type, la.value)) {
					la = this.lookAhead(i++);
				}
			}

			switch (la.type) {
			case tt.braceL:
				return this.qml_parseObjectLiteral();
			case tt.colon:
				return this.qml_parsePropertyBinding();
			}
			return null;
		};

		/*
		 * Parses a QML Property of the form:
		 *    <QMLQualifiedID> <QMLBinding>
		 */
		lp.qml_parsePropertyBinding = function () {
			var node = this.startNode();
			node.id = this.qml_parseQualifiedId(false);
			var start = this.storeCurrentPos();
			this.expect(tt.colon);
			node.binding = this.qml_parseBinding(start);
			return this.finishNode(node, "QMLPropertyBinding");
		};

		/*
		 * Parses a QML Signal Definition of the form:
		 *    'signal' <Identifier> [(<Type> <Identifier> [',' <Type> <Identifier>]* )]?
		 */
		lp.qml_parseSignalDefinition = function () {
			var node = this.startNode();

			// Check if this is an object literal or property binding first
			var objOrBind = this.qml_parseObjectLiteralOrPropertyBinding();
			if (objOrBind) {
				return objOrBind;
			}
			this.expectContextual(qtt._signal);

			node.id = this.qml_parseIdent(false);
			this.qml_parseSignalParams(node);
			this.semicolon();
			return this.finishNode(node, "QMLSignalDefinition");
		};

		/*
		* Checks if the given node is a dummy identifier
		*/
		function isDummy(node) {
			return node.name === "✖";
		}

		/*
		 * Parses QML Signal Parameters of the form:
		 *    [(<Type> <Identifier> [',' <Type> <Identifier>]* )]?
		 */
		lp.qml_parseSignalParams = function (node) {
			this.pushCx();
			var indent = this.curIndent, line = this.curLineStart;
			node.params = [];
			if (this.eat(tt.parenL)) {
				while (!this.closes(tt.parenR, indent + 1, line)) {
					var param = this.startNode();

					param.kind = this.qml_parseIdent(true);

					// Break out of an infinite loop where we continously consume dummy ids
					if (isDummy(param.kind) && this.tok.type !== tt.comma) {
						break;
					}

					param.id = this.qml_parseIdent(false);
					node.params.push(this.finishNode(param, "QMLParameter"));

					// Break out of an infinite loop where we continously consume dummy ids
					if (isDummy(param.id) && this.tok.type !== tt.comma) {
						break;
					}
					this.eat(tt.comma);
				}
				this.popCx();
				if (!this.eat(tt.parenR)) {
					// If there is no closing brace, make the node span to the start
					// of the next token (this is useful for Tern)
					this.last.end = this.tok.start;
					if (this.options.locations) this.last.loc.end = this.tok.loc.start;
				}
			}
		};

		/*
		 * Parses a QML Property Declaration (or Alias) of the form:
		 *    ['default'|'readonly'] 'property' <QMLType> <Identifier> [<QMLBinding>]
		 */
		lp.qml_parsePropertyDeclaration = function () {
			var node = this.startNode();
			var objOrBind = null;

			// Parse 'default' or 'readonly'
			node.default = false;
			node.readonly = false;
			if (this.eat(tt._default)) {
				node.default = true;
			} else if (this.isContextual(qtt._readonly)) {
				objOrBind = this.qml_parseObjectLiteralOrPropertyBinding();
				if (objOrBind) {
					objOrBind.default = undefined;
					objOrBind.readonly = undefined;
					return objOrBind;
				}
				this.expectContextual(qtt._readonly);
				node.readonly = true;
			}

			if (!node.default && !node.readonly) {
				objOrBind = this.qml_parseObjectLiteralOrPropertyBinding();
				if (objOrBind) {
					return objOrBind;
				}
				this.expectContextual(qtt._property);
			} else {
				this.expectContextual(qtt._property);
			}


			node.kind = this.qml_parseKind();
			node.id = this.qml_parseIdent(false);

			var start = this.storeCurrentPos();
			if (this.eat(tt.colon)) {
				node.binding = this.qml_parseBinding(start);
			} else {
				node.binding = null;
				this.semicolon();
			}

			return this.finishNode(node, "QMLPropertyDeclaration");
		};

		/*
		 * Parses one of the following possibilities for a QML Property assignment:
		 *    - QML Object Literal
		 *    - QML Script Binding
		 */
		lp.qml_parseBinding = function (start) {
			if (this.tok.type === tt.braceL) {
				return this.qml_parseScriptBinding(start);
			}
			// Perform look ahead to determine whether this is an expression or
			// a QML Object Literal
			var i = 1, la = this.tok;
			if (this.qml_isIdent(la.type, la.value)) {
				la = this.lookAhead(i++);
			}
			while (la.type === tt.dot) {
				la = this.lookAhead(i++);
				if (this.qml_isIdent(la.type, la.value)) {
					la = this.lookAhead(i++);
				}
			}

			if (la.type === tt.braceL) {
				return this.qml_parseObjectLiteral();
			} else {
				return this.qml_parseScriptBinding(start);
			}
		};

		/*
		 * Parses one of the following Script Bindings:
		 *    - Single JavaScript Expression
		 *    - QML Statement Block (A block of JavaScript statements)
		 */
		lp.qml_parseScriptBinding = function (start) {
			// Help out Tern a little by starting the Script Binding at the end of
			// the colon token (only if we consume invalid syntax).
			var node = this.startNodeAt(start);
			node.block = false;
			if (this.tok.type === tt.braceL) {
				node.block = true;
				node.script = this.qml_parseStatementBlock();
			} else {
				node.script = this.parseExpression(false);
				this.semicolon();
			}

			// If this node consumed valid syntax, reset its start position
			if (node.script.type !== "Identifier" || node.script.name !== "✖") {
				if (node.loc) {
					node.loc.start = node.script.loc.start;
				}
				if (node.range) {
					node.range = node.script.range;
				}
				node.start = node.script.start;
				node.end = node.script.end;
			}

			return this.finishNode(node, "QMLScriptBinding");
		};

		/*
		 * Parses a QML Statement Block of the form:
		 *    { <JavaScript Statement>* }
		 */
		lp.qml_parseStatementBlock = function () {
			var node = this.startNode();
			this.pushCx();
			this.expect(tt.braceL);
			var blockIndent = this.curIndent,
				line = this.curLineStart;
			node.statements = [];
			while (!this.closes(tt.braceR, blockIndent, line, true)) {
				node.statements.push(this.parseStatement(true, false));
			}
			this.popCx();
			this.eat(tt.braceR);
			return this.finishNode(node, "QMLStatementBlock");
		};

		/*
		 * Parses a QML Type which can be either a Qualified ID or a primitive type keyword.
		 */
		lp.qml_parseKind = function () {
			var value = this.tok.value;
			if (this.qml_eatPrimitiveType(this.tok.type, value)) {
				return value;
			} else {
				return this.qml_parseQualifiedId(false);
			}
		};

		/*
		 * Parses a Qualified ID of the form:
		 *    <Identifier> ('.' <Identifier>)*
		 *
		 * If 'liberal' is true then this method will allow non-contextual QML keywords as
		 * identifiers.
		 */
		lp.qml_parseQualifiedId = function (liberal) {
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
		lp.qml_parseIdent = function (liberal) {
			// Check for non-contextual QML keywords
			if (!liberal) {
				if (!this.qml_isIdent(this.tok.type, this.tok.value)) {
					return this.dummyIdent();
				}
			}
			return this.parseIdent();
		};

		/*
		* Checks the next token to see if it matches the given contextual keyword.  If the
		* contextual keyword was not found, this function looks ahead at the next two tokens
		* and jumps ahead if it was found there.  Returns whether or not the keyword was found.
		*/
		lp.expectContextual = function(name) {
			if (this.eatContextual(name)) return true;
			for (var i = 1; i <= 2; i++) {
				if (this.lookAhead(i).type == tt.name && this.lookAhead(i).value === name) {
					for (var j = 0; j < i; j++) this.next();
					return true;
				}
			}
		};

		// Functions left un-changed from the main parser
		lp.qml_isIdent = pp.qml_isIdent;
		lp.qml_eatPrimitiveType = pp.qml_eatPrimitiveType;
		lp.qml_isPrimitiveType = pp.qml_isPrimitiveType;

		acorn.pluginsLoose.qml = function (instance) {

			// Extend acorn's 'parseTopLevel' method
			instance.extend("parseTopLevel", function (nextMethod) {
				return function () {
					// Make parsing simpler by only allowing ECMA Version 5 or older ('import' is
					// not a keyword in this version of ECMA Script).  Qt 5.5 runs with ECMA Script
					// 5 anyway, so this makes sense.
					if (!this.options.ecmaVersion || this.options.ecmaVersion > 5) {
						throw new Error("QML only supports ECMA Script Language Specification 5 or older");
					}

					// Most of QML's constructs sit at the top-level of the parse tree,
					// replacing JavaScripts top-level.  Here we are parsing such things
					// as the root object literal and header statements of QML.  Eventually,
					// these rules will delegate down to JavaScript expressions.
					var node = this.startNode();
					node.headerStatements = this.qml_parseHeaderStatements();
					node.rootObject = null;
					if (this.tok.type !== tt.eof) {
						node.rootObject = this.qml_parseObjectLiteral();
					}

					return this.finishNode(node, "QMLProgram");
				};
			});
		};

		return acorn;
	};
});