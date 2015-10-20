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

	pp.qml_parseHeaderStatements = function() {
		var node = this.startNode()
		node.statements = [];

		loop: {
			switch (this.type) {
				case tt._import:
					var qmlImport = this.qml_parseImportStatement();
					node.statements.push(qmlImport);
					break loop;
				case tt._pragma:
					// TODO: parse QML pragma statement
			}
		}

		return this.finishNode(node, "QMLHeaderStatements")
	}

	pp.qml_parseImportStatement = function() {
		var node = this.startNode();

		// Advance to the next token since this method should only be called
		// when the current token is 'import'
		this.next();
		node.module = this.qml_parseModuleIdentifier();
		// TODO: parse the 'as Identifier' portion of an import statement
		this.semicolon();
		return this.finishNode(node, "QMLImportStatement");
	};

	pp.qml_parseModuleIdentifier = function() {
		var node = this.startNode();

		// Parse the qualified id/string
		if (this.type == tt.name)
			node.qualifiedId = this.qml_parseQualifiedId();
		else if (this.type == tt.string) {
			node.file = this.value;
			this.next();
		} else
			this.unexpected();

		// Parse the version number
		if (this.type == tt.num) {
			node.version = this.parseLiteral(this.value);
			// TODO: check that version number has major and minor
		} else
			this.unexpected();
		return this.finishNode(node, "QMLModuleIdentifier");
	};

	pp.qml_parseQualifiedId = function() {
		var id = this.value;
		this.next();
		while(this.type == tt.dot) {
			id += '.';
			this.next();
			if (this.type == tt.name)
				id += this.value;
			else
				this.unexpected();
			this.next();
		}
		return id;
	}

	/*
	* Returns a TokenType that matches the given word or undefined if
	* no such TokenType could be found.
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
				// regardless of ecma version set in acorn.
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
				node.body.push(headerStmts)

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

				if (type !== undefined)
					return this.finishToken(type, word);

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

	return acorn
};