/*******************************************************************************
 * Copyright (c) 2001 Rational Software Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     Rational Software - initial implementation
 ******************************************************************************/
package org.eclipse.cdt.core.parser;

import java.util.List;
/**
 * @author jcamelon
 *
 */
public interface IMacroDescriptor {
	
	public static class MacroType extends Enum
	{
		// two kinds of macros as defined by ISO C++98
		
		// object like - #define SYMBOL REPLACEMENT TOKENS
		public static final MacroType OBJECT_LIKE = new MacroType( 1 );
		
		// function like - #define SYMBOL( parm1, parm2 ) TOKENS USING parms
		public static final MacroType FUNCTION_LIKE = new MacroType( 2 );
		
		/**
		 * @param enumValue
		 */
		protected MacroType(int enumValue) {
			super(enumValue);
		}
		
	}

	// what kind of macro is it?
	public MacroType getMacroType();
	
	// parameters for macros of type FUNCTION_LIKE
	public List getParameters();
	
	// the RHS side of the macro separated into ITokens
	public List getTokenizedExpansion();
	
	// the symbol name
	public String getName();
	
	// the full preprocessor line of source that spawned this object
	public String getCompleteSignature();
	
	// the RHS of the macro
	public String getExpansionSignature();
	
	// similar to equals() but according to the C99 & C++98 
	public boolean compatible(IMacroDescriptor descriptor);
}