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

import org.eclipse.cdt.internal.core.parser.Parser.Backtrack;


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
	public void expression(Object expression) throws Backtrack;
	
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
	 * Do we wish to keep track of the lineNumbers/Offset mapping? 
	 * 
	 * By default, the value is false.  Setting it to true impacts performance but 
	 * provides that feature.  
	 * 
	 * @param value		true for the feature, false for improved performance
	 */
	public void mapLineNumbers( boolean value );
	
	/**
	 * Given an character offset into the file, return the lineNumber this offset maps to.  
	 * 
	 * @param offset	character offset in the file
	 * @return			lineNumber this offset maps to
	 * @throws NoSuchMethodException	if mapLineNumbers( true ) was not previously called
	 */
	public int getLineNumberForOffset(int offset) throws NoSuchMethodException;
	
	/**
	 * If an error was encountered, give us the offset of the token that caused the error.  
	 * 
	 * @return		-1 for no error, otherwise the character offset where we encountered 
	 * 				our first unrecoverable error.
	 */
	public int getLastErrorOffset(); 
	
	
	public void setRequestor( ISourceElementRequestor r );
	
}