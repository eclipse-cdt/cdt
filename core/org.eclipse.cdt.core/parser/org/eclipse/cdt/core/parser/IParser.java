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

import org.eclipse.cdt.core.parser.ast.IASTCompletionNode;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTNode;
import org.eclipse.cdt.core.parser.ast.IASTScope;



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
	 * @param offset  offset in the input file where code completion is being requested for 
	 * @return		an IASTCompletionConstruct that provides a mechanism for determining C/C++ code completion contributions
	 */
	public IASTCompletionNode parse( int offset )throws ParserNotImplementedException;
	
	/**
	 * 
	 * @param startingOffset
	 * @param endingOffset
	 * @return
	 */
	public IASTNode                          parse( int startingOffset, int endingOffset ) throws ParserNotImplementedException;
	
	
	/**
	 * Request a parse from a pre-configured parser to parse an expression.    
	 * 
	 * @param expression	Optional parameter representing an expression object that 
	 * 						your particular IParserCallback instance would appreciate 
	 * 	
	 * @throws Backtrack	thrown if the Scanner/Stream provided does not yield a valid
	 * 						expression	
	 */
	public IASTExpression expression(IASTScope scope) throws Backtrack;
	
	/**
	 * If an error was encountered, give us the offset of the token that caused the error.  
	 * 
	 * @return		-1 for no error, otherwise the character offset where we encountered 
	 * 				our first unrecoverable error.
	 */
	public int getLastErrorOffset(); 
	
	
}