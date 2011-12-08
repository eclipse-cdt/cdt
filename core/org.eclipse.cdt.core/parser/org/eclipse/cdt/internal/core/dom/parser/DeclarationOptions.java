/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser;

/** 
 * Configures the parsing of a declaration in various contexts.
 */
public class DeclarationOptions {
	final public static int ALLOW_EMPTY_SPECIFIER= 			0x01;
	final public static int ALLOW_ABSTRACT= 				0x02;
	final public static int REQUIRE_ABSTRACT= 				0x04;
	final public static int ALLOW_BITFIELD= 				0x08;
	final public static int NO_INITIALIZER=      			0x10;
	final public static int NO_CTOR_STYLE_INITIALIZER= 		0x20;
	final public static int NO_BRACED_INITIALIZER= 			0x40;
	final public static int NO_FUNCTIONS= 					0x80;
	final public static int NO_ARRAYS= 						0x100;
	final public static int NO_NESTED= 						0x200;
	final public static int ALLOW_PARAMETER_PACKS=  		0x400;
	final public static int REQUIRE_SIMPLE_NAME=    		0x800;
	final public static int ALLOW_FOLLOWED_BY_BRACE=    	0x1000;
	final public static int ALLOW_OPAQUE_ENUM=				0x2000;
	final public static int SINGLE_DTOR=					0x4000;
	final public static int ALLOW_FUNCTION_DEFINITION=		0x8000;

    public static final DeclarationOptions 
    	GLOBAL=     new DeclarationOptions(ALLOW_EMPTY_SPECIFIER | ALLOW_OPAQUE_ENUM | ALLOW_FUNCTION_DEFINITION),
    	FUNCTION_STYLE_ASM= new DeclarationOptions(ALLOW_EMPTY_SPECIFIER | NO_INITIALIZER | ALLOW_ABSTRACT | ALLOW_FUNCTION_DEFINITION),
    	C_MEMBER=   new DeclarationOptions(ALLOW_BITFIELD | ALLOW_ABSTRACT),
    	CPP_MEMBER= new DeclarationOptions(ALLOW_EMPTY_SPECIFIER | ALLOW_BITFIELD | NO_CTOR_STYLE_INITIALIZER | ALLOW_FUNCTION_DEFINITION),
    	LOCAL=	    new DeclarationOptions(ALLOW_OPAQUE_ENUM),
    	PARAMETER=  new DeclarationOptions(ALLOW_ABSTRACT | ALLOW_PARAMETER_PACKS | REQUIRE_SIMPLE_NAME | NO_BRACED_INITIALIZER | NO_CTOR_STYLE_INITIALIZER),
    	TYPEID=     new DeclarationOptions(REQUIRE_ABSTRACT | NO_INITIALIZER),
    	TYPEID_TRAILING_RETURN_TYPE= new DeclarationOptions(REQUIRE_ABSTRACT | NO_INITIALIZER | ALLOW_FOLLOWED_BY_BRACE | ALLOW_FUNCTION_DEFINITION),
    	TYPEID_NEW= new DeclarationOptions(REQUIRE_ABSTRACT | NO_INITIALIZER | NO_FUNCTIONS | NO_NESTED | ALLOW_FOLLOWED_BY_BRACE),
    	TYPEID_CONVERSION= new DeclarationOptions(REQUIRE_ABSTRACT | NO_INITIALIZER | NO_FUNCTIONS | NO_NESTED),
        EXCEPTION= new DeclarationOptions(ALLOW_ABSTRACT | NO_INITIALIZER),
        CONDITION= new DeclarationOptions(NO_CTOR_STYLE_INITIALIZER),
        C_PARAMETER_NON_ABSTRACT= new DeclarationOptions(ALLOW_ABSTRACT | ALLOW_EMPTY_SPECIFIER),
        RANGE_BASED_FOR = new DeclarationOptions(NO_INITIALIZER | REQUIRE_SIMPLE_NAME | SINGLE_DTOR);

	final public boolean fAllowEmptySpecifier;
	final public boolean fAllowAbstract;
	final public boolean fRequireAbstract;
	final public boolean fAllowBitField;
	final public boolean fAllowInitializer;
	final public boolean fAllowBracedInitializer;
	final public boolean fCanBeFollowedByBrace;
	final public boolean fAllowCtorStyleInitializer;
	final public boolean fAllowFunctions;
	final public boolean fAllowNested;
	final public boolean fAllowParameterPacks;
	final public boolean fRequireSimpleName;
	final public boolean fAllowOpaqueEnum;
	final public boolean fSingleDtor;
	final public boolean fAllowFunctionDefinition;
	
	public DeclarationOptions(int options) {
		fAllowEmptySpecifier= (options & ALLOW_EMPTY_SPECIFIER) != 0;
		fRequireAbstract= (options & REQUIRE_ABSTRACT) != 0;
		fAllowAbstract= fRequireAbstract || (options & ALLOW_ABSTRACT) != 0;
		fAllowBitField= (options & ALLOW_BITFIELD) != 0;
		fAllowInitializer= (options & NO_INITIALIZER) == 0;
		fAllowBracedInitializer= fAllowInitializer && (options & NO_BRACED_INITIALIZER) == 0;
		fAllowCtorStyleInitializer= fAllowInitializer && (options & NO_CTOR_STYLE_INITIALIZER) == 0;
		fAllowFunctions= (options & NO_FUNCTIONS) == 0;
		fAllowNested= (options & NO_NESTED) == 0;
		fAllowParameterPacks= (options & ALLOW_PARAMETER_PACKS) != 0;
		fRequireSimpleName= (options & REQUIRE_SIMPLE_NAME) != 0;
		fCanBeFollowedByBrace= fAllowBracedInitializer || (options & ALLOW_FOLLOWED_BY_BRACE) != 0;
		fAllowOpaqueEnum= (options & ALLOW_OPAQUE_ENUM) != 0;
		fSingleDtor= (options & SINGLE_DTOR) != 0;
		fAllowFunctionDefinition= (options & ALLOW_FUNCTION_DEFINITION) != 0;
	}
}
