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
	: 'import' qmlQualifiedId DecimalLiteral ('as' Identifier)? ';'?
	| 'import' StringLiteral DecimalLiteral? ('as' Identifier)? ';'?
	;

qmlQualifiedId
	: Identifier ('.' Identifier)*
	;

qmlPragmaDeclaration
	: 'pragma' Identifier ';'?
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
	: qmlQualifiedId ':' singleExpression
	| qmlObjectLiteral
	| 'readonly'? 'property' qmlPropertyType Identifier (':' singleExpression)?
	| functionDeclaration
	;


qmlPropertyType
	: Identifier // TODO
	| 'var'
	;	