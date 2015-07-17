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