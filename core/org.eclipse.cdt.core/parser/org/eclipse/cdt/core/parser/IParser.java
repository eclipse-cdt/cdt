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
import org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement;


/**
 * This is the external interface that all C and C++ parsers in the CDT
 * must implement. 
 * 
 * @author jcamelon
 */
public interface IParser  {
	
	
	/**
	 * Request a parse from a pre-configured parser to parse a whole translation unit or file.  
	 * 
	 * @return	whether or not the parse was successful 
	 */
	public boolean parse();
	
	/**
	 * @param offset  offset in the input file where code completion is being requested for 
	 * @return	an IASTCompletionConstruct that provides a mechanism for determining C/C++ code completion contributions
	 */
	public IASTCompletionNode parse( int offset) throws ParseError;
	
	
	public static interface ISelectionParseResult
	{
		public IASTOffsetableNamedElement getOffsetableNamedElement();
		public String   getFilename();
	}
	/**
	 * 
	 * @param startingOffset
	 * @param endingOffset
	 * @return
	 */
	public ISelectionParseResult parse( int startingOffset, int endingOffset ) throws ParseError;
	
	/**
	 * If an error was encountered, give us the offset of the token that caused the error.  
	 * 
	 * @return		-1 for no error, otherwise the character offset where we encountered 
	 * 				our first unrecoverable error.
	 */
	public int getLastErrorOffset();
	
	public int getLastErrorLine();
	
}