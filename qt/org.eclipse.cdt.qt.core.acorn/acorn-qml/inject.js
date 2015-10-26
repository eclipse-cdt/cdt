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

module.exports = function(acorn) {
	// Acorn token types
	var tt = acorn.tokTypes;

	// QML token types
	var qtt = {};
	var keywords = {};

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
	kw("property", { isQMLContextual : true });
	kw("readonly", { isQMLContextual : true });
	kw("signal", { isQMLContextual : true });
	kw("as");
	kw("boolean", { isPrimitive: true });
	kw("double", { isPrimitive: true });
	kw("int", { isPrimitive: true });
	kw("alias", { isQMLContextual: true });
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
	* Parses a set of QML Header Statements which can either be of
	* the type import or pragma
	*/
	pp.qml_parseHeaderStatements = function() {
		var node = this.startNode()
		node.statements = [];

		var loop = true;
		while (loop) {
			if (this.type === tt._import || this.isContextual(qtt._import)) {
				var qmlImport = this.qml_parseImportStatement();
				node.statements.push(qmlImport);
			} else if (this.isContextual(qtt._pragma)) {
				var qmlPragma = this.qml_parsePragmaStatement();
				node.statements.push(qmlPragma);
			} else {
				loop = false;
			}
		}

		if (node.statements.length > 0) {
			return this.finishNode(node, "QMLHeaderStatements");
		}
		return undefined;
	}

	/*
	* Parses a QML Pragma statement of the form:
	*    'pragma' <Identifier>
	*/
	pp.qml_parsePragmaStatement = function() {
		var node = this.startNode();
		this.next();
		node.identifier = this.parseIdent(false);
		this.semicolon();
		return this.finishNode(node, "QMLPragmaStatement");
	}

	/*
	* Parses a QML Import statement of the form:
	*    'import' <ModuleIdentifier> <Version.Number> [as <Qualifier>]
	*    'import' <DirectoryPath> [as <Qualifier>]
	*
	* as specified by http://doc.qt.io/qt-5/qtqml-syntax-imports.html
	*/
	pp.qml_parseImportStatement = function() {
		var node = this.startNode();
		this.next();

		// The type of import varies solely on the next token
		switch(this.type) {
			case tt.name:
				node.module = this.qml_parseModule();
				break;
			case tt.string:
				node.directoryPath = this.parseLiteral(this.value);
				break;
			default:
				this.unexpected();
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
	*    <QualifiedId> <VersionLiteral>
	*/
	pp.qml_parseModule = function() {
		var node = this.startNode();

		node.qualifiedId = this.qml_parseQualifiedId();
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
	pp.qml_parseVersionLiteral = function() {
		var node = this.startNode();

		node.raw = this.input.slice(this.start, this.end);
		node.value = this.value;
		var matches;
		if (matches = /(\d+)\.(\d+)/.exec(node.raw)) {
			node.major = parseInt(matches[1]);
			node.minor = parseInt(matches[2]);
		} else {
			this.raise(this.start, "QML module must specify major and minor version");
		}

		this.next();
		return this.finishNode(node, "QMLVersionLiteral");
	}

	/*
	* Parses a QML Qualifier of the form:
	*    'as' <Identifier>
	*/
	pp.qml_parseQualifier = function() {
		var node = this.startNode();
		this.next();
		node.identifier = this.qml_parseIdent(false);
		return this.finishNode(node, "QMLQualifier");
	}

	/*
	* Parses a QML Object Literal of the form:
	*    <QualifiedId> { (<QMLMember>)* }
	*
	* http://doc.qt.io/qt-5/qtqml-syntax-basics.html#object-declarations
	*/
	pp.qml_parseObjectLiteral = function(node) {
		if (!node) {
			node = this.startNode();
		}
		if (!node.qualifiedId) {
			node.qualifiedId = this.qml_parseQualifiedId();
		}
		node.block = this.qml_parseMemberBlock();
		return this.finishNode(node, "QMLObjectLiteral");
	}

	/*
	* Parses a QML Member Block of the form:
	*    { <QMLMember>* }
	*/
	pp.qml_parseMemberBlock = function() {
		var node = this.startNode();
		this.expect(tt.braceL);
		node.members = [];
		while (!this.eat(tt.braceR)) {
			node.members.push(this.qml_parseMember());
		}
		return this.finishNode(node, "QMLMemberBlock");
	}

	/*
	* Parses a QML Member which can be one of the following:
	*    - a QML Property Binding
	*    - a Property Declaration (or Alias)
	*    - a QML Object Literal
	*    - a JavaScript Function Declaration
	*    - a Signal Definition
	*/
	pp.qml_parseMember = function() {
		var node = this.startNode();

		if (this.type === tt._default
				|| this.isContextual(qtt._default)
				|| this.isContextual(qtt._readonly)
				|| this.isContextual(qtt._property)) {
			return this.qml_parsePropertyDeclaration(node);
		} else if (this.isContextual(qtt._signal)) {
			return this.qml_parseSignalDefinition(node);
		} else if (this.type === tt._function) {
			return this.parseFunctionStatement(node);
		}

		node.qualifiedId = this.qml_parseQualifiedId();
		switch(this.type) {
			case tt.braceL:
				return this.qml_parseObjectLiteral(node);
			case tt.colon:
				return this.qml_parseProperty(node);
		}

		this.unexpected();
	}

	/*
	* Parses a QML Property of the form:
	*    <QMLQualifiedID> <QMLBinding>
	*/
	pp.qml_parseProperty = function(node) {
		if (!node) {
			node = this.startNode();
		}
		if (!node.qualifiedId) {
			node.qualifiedId = this.qml_parseQualifiedId();
		}
		node.binding = this.qml_parseBinding();
		return this.finishNode(node, "QMLProperty");
	}

	/*
	* Parses a QML Signal Definition of the form:
	*    'signal' <Identifier> [(<Type> <Identifier> [',' <Type> <Identifier>]* )]?
	*/
	pp.qml_parseSignalDefinition = function(node) {
		if (!node) {
			node = this.startNode();
		}
		this.next();
		node.identifier = this.qml_parseIdent(false);
		node.parameters = [];
		if (this.type === tt.parenL) {
			this.next();
			if (this.type !== tt.parenR) {
				do {
					var paramNode = this.startNode();
					paramNode.type = this.qml_parseIdent(false);
					paramNode.identifier = this.qml_parseIdent(false);
					node.parameters.push(paramNode);
				} while(this.eat(tt.comma));
			}
			if (!this.eat(tt.parenR)) {
				this.unexpected();
			}
		}
		this.semicolon();
		return this.finishNode(node, "QMLSignalDefinition");
	}

	/*
	* Parses a QML Property Declaration (or Alias) of the form:
	*    ['default'|'readonly'] 'property' <QMLType> <Identifier> [<QMLBinding>]
	*/
	pp.qml_parsePropertyDeclaration = function(node) {
		node["default"] = false;
		node["readonly"] = false;

		if (this.type === tt._default || this.isContextual(qtt._default)) {
			node["default"] = true;
			this.next();
		} else if (this.eatContextual(qtt._readonly)) {
			node["readonly"] = true;
		}
		this.expectContextual(qtt._property);
		node.typeInfo = this.qml_parseType();
		node.identifier = this.qml_parseIdent(false);
		if (this.type !== tt.colon) {
			this.semicolon();
		} else {
			node.binding = this.qml_parseBinding();
		}

		if (node.typeInfo.type === qtt._alias) {
			node.typeInfo = undefined;
			return this.finishNode(node, "QMLPropertyAlias");
		}
		return this.finishNode(node, "QMLPropertyDeclaration");
	}

	/*
	* Parses a QML Binding of the form:
	*    ':' (<Expression>|<QMLStatementBlock>)
	*/
	pp.qml_parseBinding = function() {
		var node = this.startNode();
		this.expect(tt.colon);

		// TODO: solve ambiguity where a QML Object Literal starts with a
		// Qualified Id that looks very similar to a MemberExpression in
		// JavaScript.  For now, we just won't parse statements like:
		//      test: QMLObject {  }
		//      test: QMLObject.QualifiedId {  }

		if (this.type === tt.braceL) {
			node.block = this.qml_parseStatementBlock();
			return this.finishNode(node, "QMLBinding");
		}
		node.expr = this.parseExpression(false);
		this.semicolon();
		return this.finishNode(node, "QMLBinding");
	}

	/*
	* Parses a QML Statement Block of the form:
	*    { <JavaScript Statement>* }
	*/
	pp.qml_parseStatementBlock = function() {
		var node = this.startNode();
		this.expect(tt.braceL);
		node.statements = [];
		while(!this.eat(tt.braceR)) {
			node.statements.push(this.parseStatement(true, false));
		}
		return this.finishNode(node, "QMLStatementBlock");
	}

	/*
	* Parses a QML Type which can be either a Qualified ID or a primitive type keyword.
	* Returns a node of type qtt._alias if the type keyword parsed was "alias".
	*/
	pp.qml_parseType = function() {
		var node = this.startNode();

		if (this.type === tt.name || this.type === tt._var) {
			var value = this.value;
			if (this.qml_eatPrimitiveType(value)) {
				node.isPrimitive = true;
				node.primitive = value;
			} else if (this.eatContextual(qtt._alias)) {
				return this.finishNode(node, qtt._alias);
			} else {
				node.isPrimitive = false;
				node.qualifiedId = this.qml_parseQualifiedId();
			}
		} else {
			this.unexpected();
		}

		return this.finishNode(node, "QMLType");
	}

	/*
	* Parses a Qualified ID of the form:
	*    <Identifier> ('.' <Identifier>)*
	*/
	pp.qml_parseQualifiedId = function() {
		var node = this.startNode();

		node.parts = [];
		if (!this.qml_isIdent(this.type, this.value)) {
			this.unexpected();
		}
		var id = this.value;
		this.next();
		node.parts.push(id);
		while(this.type === tt.dot) {
			id += '.';
			this.next();
			if (!this.qml_isIdent(this.type, this.value)) {
				this.unexpected();
			}
			id += this.value;
			node.parts.push(this.value);
			this.next();
		}
		node.raw = id;

		return this.finishNode(node, "QMLQualifiedID");
	}

	/*
	* Parses an Identifier in a QML Context.  That is, this method uses 'isQMLContextual'
	* to throw an error if a non-contextual QML keyword is found.
	*/
	pp.qml_parseIdent = function(liberal) {
		// Check for non-contextual QML keywords
		if (this.type === tt.name) {
			for (var key in keywords) {
				if (!keywords[key].isQMLContextual && this.isContextual(key)) {
					this.unexpected();
				}
			}
		}
		return this.parseIdent(liberal);
	}

	/*
	* Returns whether or not a given token type and name can be a QML Identifier.
	* Uses the 'isQMLContextual' boolean of 'keywords' to determine this.
	*/
	pp.qml_isIdent = function(type, name) {
		if (type === tt.name) {
			var key;
			if (key = keywords[name]) {
				return key.isQMLContextual
			}
			return true;
		}
		return false;
	}

	/*
	* Returns whether or not the current token is a QML primitive type and consumes
	* it as a side effect if it is.
	*/
	pp.qml_eatPrimitiveType = function(name) {
		if (this.qml_isPrimitiveType(name)) {
			this.next();
			return true;
		}
		return false;
	}

	/*
	* Returns whether or not the current token is a QML primitive type.
	*/
	pp.qml_isPrimitiveType = function(name) {
		if (name === "var") {
			return true;
		}

		var key;
		if (key = keywords[name]) {
			return key.isPrimitive;
		}
		return false;
	}

	acorn.plugins.qml = function(instance) {

		// Extend acorn's 'parseTopLevel' method
		instance.extend("parseTopLevel", function(nextMethod) {
			return function(node) {
				// Most of QML's constructs sit at the top-level of the parse tree,
				// replacing JavaScripts top-level.  Here we are parsing such things
				// as the root object literal and header statements of QML.  Eventually,
				// these rules will delegate down to JavaScript expressions.
				if (!node.body) {
					node.body = [];
				}

				var headerStmts = this.qml_parseHeaderStatements();
				if (headerStmts !== undefined) {
					node.body.push(headerStmts);
				}

				if (this.type !== tt.eof) {
					var objRoot = this.qml_parseObjectLiteral();
					if (objRoot !== undefined) {
						node.body.push(objRoot);
					}
				}

				if (!this.eat(tt.eof)) {
					this.raise(this.pos, "Expected EOF after QML Root Object");
				}

				return this.finishNode(node, "Program");
			};
		});
	}

	return acorn;
};