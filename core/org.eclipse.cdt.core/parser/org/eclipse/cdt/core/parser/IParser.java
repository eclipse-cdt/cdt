/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.parser;

import org.eclipse.cdt.core.parser.ast.IASTExpression;



/**
 * This is the external interface that all C and C++ parsers in the CDT
 * must implement. 
 * 
 * @author jcamelon
 */
public interface IParser {
	
	
	/**
	 * Request a parse from a pre-configured parser to parse a whole translation unit or file.  
	 * 
	 * @return		whether or not the parse was successful 
	 */
	public boolean parse();
	
	
	/**
	 * Request a parse from a pre-configured parser to parse an expression.    
	 * 
	 * @param expression	Optional parameter representing an expression object that 
	 * 						your particular IParserCallback instance would appreciate 
	 * 	
	 * @throws Backtrack	thrown if the Scanner/Stream provided does not yield a valid
	 * 						expression	
	 */
	public IASTExpression expression() throws Backtrack;
	
	/**
	 * Is the parser configured for ANSI C or ANSI C++?
	 * 
	 * @return	true for C++, false for C
	 */
	public boolean isCppNature();
	
	/**
	 * Set the Parser explicitly to be a C or C++ parser.
	 * 
	 * @param b		true for C++, false for C 
	 */
	public void setCppNature(boolean b);
	
	/**
	 * If an error was encountered, give us the offset of the token that caused the error.  
	 * 
	 * @return		-1 for no error, otherwise the character offset where we encountered 
	 * 				our first unrecoverable error.
	 */
	public int getLastErrorOffset(); 
	
	
}