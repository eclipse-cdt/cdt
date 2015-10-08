/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
grammar QML;

import ECMAScript;

qmlProgram
	: qmlHeaderItem* qmlObjectRoot EOF
	;

qmlHeaderItem
	: qmlImportDeclaration
	| qmlPragmaDeclaration
	;

qmlImportDeclaration
	: 'import' qmlQualifiedId DecimalLiteral ('as' Identifier)? semi
	| 'import' StringLiteral DecimalLiteral? ('as' Identifier)? semi
	;

qmlQualifiedId
	: qmlIdentifier ('.' qmlIdentifier)*
	;

qmlPragmaDeclaration
	: 'pragma' Identifier semi
	;

qmlObjectRoot
	: qmlObjectLiteral?
	;

qmlObjectLiteral
	: qmlQualifiedId qmlMembers 
	;

qmlMembers
	: '{' qmlMember* '}'
	;

qmlMember
	: qmlAttribute
	| qmlObjectLiteral
	| qmlPropertyDeclaration
	| singleExpression semi
	;

qmlAttribute
	: qmlQualifiedId ':' singleExpression semi
	| qmlQualifiedId ':' qmlObjectLiteral
	| qmlQualifiedId ':' qmlMembers
	;

qmlPropertyDeclaration
	: READONLY? 'property' qmlPropertyType qmlIdentifier (':' singleExpression)? semi
	;

qmlPropertyType
	: BOOLEAN
	| DOUBLE
	| INT
	| LIST
	| COLOR
	| REAL
	| STRING
	| URL
	| VAR
	;

qmlIdentifier
	: Identifier
	// Allow a few keywords as identifiers
	| LIST
	| COLOR
	| REAL
	| STRING
	| URL
	;

// QML reserved words
READONLY : 'readonly' ;

// QML future reserved words
TRANSIENT :'transient';
SYNCHRONIZED : 'synchronized' ;
ABSTRACT : 'abstract' ;
VOLATILE : 'volatile' ;
NATIVE : 'native' ;
GOTO : 'goto' ;
BYTE : 'byte' ;
LONG : 'long' ;
CHAR : 'char' ;
SHORT : 'short' ;
FLOAT : 'float' ;

// QML basic types
BOOLEAN : 'boolean' ;
DOUBLE : 'double' ;
INT : 'int' ;
LIST : 'list' ;
COLOR : 'color' ;
REAL : 'real' ;
STRING : 'string' ;
URL : 'url' ;
VAR : 'var' ;