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

	// Define QML token types
	var tt = acorn.tokTypes;

	/*
	* Shorthand for defining keywords in tt (acorn.tokTypes).  Creates a new key in
	* tt with the label _<keywordName>.
	*/
	function kw(name, options) {
		if (options === undefined)
			options = {};
		options.keyword = name;
		tt["_" + name] = new acorn.TokenType(name, options);
	}

	// Define QML keywords
	kw("property");
	kw("readonly");
	kw("color");
	kw("pragma");
	kw("as");

	// Define QML token contexts
	var tc = acorn.tokContexts;

	// TODO: Add QML contexts (one such example is so we can parse keywords as identifiers)

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
			switch (this.type) {
				case tt._import:
					var qmlImport = this.qml_parseImportStatement();
					node.statements.push(qmlImport);
					break;
				case tt._pragma:
					var qmlPragma = this.qml_parsePragmaStatement();
					node.statements.push(qmlPragma);
					break;
				default:
					loop = false;
			}
		}

		return this.finishNode(node, "QMLHeaderStatements");
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
		if (this.type === tt._as) {
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
		node.identifier = this.parseIdent(false);
		return this.finishNode(node, "QMLQualifier");
	}

	/*
	* Parses a Qualified ID of the form:
	*    <Identifier> ('.' <Identifier>)*
	*/
	pp.qml_parseQualifiedId = function() {
		var node = this.startNode();

		node.parts = [];
		var id = this.value;
		node.parts.push(this.value);
		this.next();
		while(this.type === tt.dot) {
			id += '.';
			this.next();
			if (this.type === tt.name) {
				id += this.value;
				node.parts.push(this.value);
			} else {
				this.unexpected();
			}
			this.next();
		}
		node.raw = id;

		return this.finishNode(node, "QMLQualifiedID");
	}

	/*
	* Returns a TokenType that matches the given word or undefined if
	* no such TokenType could be found.  This method only matches
	* QML-specific keywords.
	*
	* Uses contextual information to determine whether or not a keyword
	* such as 'color' is being used as an identifier.  If this is found
	* to be the case, tt.name is returned.
	*/
	pp.qml_getTokenType = function(word) {
		// TODO: use context to determine if this is an identifier or
		// a keyword (color, real, etc. can be used as identifiers)
		switch(word) {
			case "property":
				return tt._property;
			case "readonly":
				return tt._readonly;
			case "import":
				// Make sure that 'import' is recognized as a keyword
				// regardless of the ecma version set in acorn.
				return tt._import;
			case "color":
				return tt._color;
			case "pragma":
				return tt._pragma;
			case "as":
				return tt._as;
		}
		return undefined;
	}

	acorn.plugins.qml = function(instance) {

		// Extend acorn's 'parseTopLevel' method
		instance.extend("parseTopLevel", function(nextMethod) {
			return function(node) {
				// Most of QML's constructs sit at the top-level of the parse tree,
				// replacing JavaScripts top-level.  Here we are parsing such things
				// as the root object literal and header statements of QML.  Eventually,
				// these rules will delegate down to JavaScript expressions.
				if (!node.body)
					node.body = [];

				var headerStmts = this.qml_parseHeaderStatements();
				node.body.push(headerStmts);

				// TODO: Parse QML object root

				// TODO: don't call acorn's parseTopLevel method once the above are working
				return nextMethod.call(this, node);
			};
		});

		// Extend acorn's 'readWord' method
		instance.extend("readWord", function(nextMethod) {
			return function() {
				// Parse a word and attempt to match it to a QML keyword
				var word = this.readWord1();
				var type = this.qml_getTokenType(word);

				if (type !== undefined) {
					return this.finishToken(type, word);
				}

				// If we were unable to find a QML keyword, call acorn's implementation
				// of the readWord method.  Since we don't have access to _tokentype, and
				// subsequently _tokentype.keywords, we can't look for JavaScript keyword
				// matches ourselves.  This is unfortunate because we have to move the parser
				// backwards and let readWord call readWord1 a second time for every word
				// that is not a QML keyword.
				this.pos -= word.length;
				return nextMethod.call(this);
			};
		});
	}

	return acorn;
};