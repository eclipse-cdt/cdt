/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
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
 * @since 5.0
 */
public class DeclarationOptions {
	final public static int ALLOW_EMPTY_SPECIFIER= 	0x01;
	final public static int ALLOW_ABSTRACT= 		0x02;
	final public static int REQUIRE_ABSTRACT= 		0x04;
	final public static int ALLOW_BITFIELD= 		0x08;
	final public static int NO_INITIALIZER=      	0x10;
	final public static int ALLOW_CONSTRUCTOR_INITIALIZER= 0x20;
	final public static int NO_FUNCTIONS= 			0x40;
	final public static int NO_ARRAYS= 				0x80;
	final public static int NO_NESTED= 				0x100;

    public static final DeclarationOptions 
    	GLOBAL=     new DeclarationOptions(ALLOW_EMPTY_SPECIFIER | ALLOW_CONSTRUCTOR_INITIALIZER),
    	FUNCTION_STYLE_ASM= new DeclarationOptions(ALLOW_EMPTY_SPECIFIER | NO_INITIALIZER),
    	C_MEMBER=   new DeclarationOptions(ALLOW_BITFIELD | ALLOW_ABSTRACT),
    	CPP_MEMBER= new DeclarationOptions(ALLOW_EMPTY_SPECIFIER | ALLOW_BITFIELD),
    	LOCAL=	    new DeclarationOptions(ALLOW_CONSTRUCTOR_INITIALIZER),
    	PARAMETER=  new DeclarationOptions(ALLOW_ABSTRACT),
    	TYPEID=     new DeclarationOptions(REQUIRE_ABSTRACT | NO_INITIALIZER),
    	TYPEID_NEW= new DeclarationOptions(REQUIRE_ABSTRACT | NO_INITIALIZER | NO_FUNCTIONS | NO_NESTED),
    	TYPEID_CONVERSION= new DeclarationOptions(REQUIRE_ABSTRACT | NO_INITIALIZER | NO_FUNCTIONS | NO_NESTED),
        EXCEPTION= new DeclarationOptions(ALLOW_ABSTRACT | NO_INITIALIZER),
        CONDITION= new DeclarationOptions(ALLOW_CONSTRUCTOR_INITIALIZER),
        C_PARAMETER_NON_ABSTRACT= new DeclarationOptions(ALLOW_ABSTRACT | ALLOW_EMPTY_SPECIFIER);

	final public boolean fAllowEmptySpecifier;
	final public boolean fAllowAbstract;
	final public boolean fRequireAbstract;
	final public boolean fAllowBitField;
	final public boolean fAllowInitializer;
	final public boolean fAllowConstructorInitializer;
	final public boolean fAllowFunctions;
	final public boolean fAllowNested;
	
	public DeclarationOptions(int options) {
		fAllowEmptySpecifier= (options & ALLOW_EMPTY_SPECIFIER) != 0;
		fRequireAbstract= (options & REQUIRE_ABSTRACT) != 0;
		fAllowAbstract= fRequireAbstract || (options & ALLOW_ABSTRACT) != 0;
		fAllowBitField= (options & ALLOW_BITFIELD) != 0;
		fAllowInitializer= (options & NO_INITIALIZER) == 0;
		fAllowConstructorInitializer= fAllowInitializer && (options & ALLOW_CONSTRUCTOR_INITIALIZER) != 0;
		fAllowFunctions= (options & NO_FUNCTIONS) == 0;
		fAllowNested= (options & NO_NESTED) == 0;
	}
}
