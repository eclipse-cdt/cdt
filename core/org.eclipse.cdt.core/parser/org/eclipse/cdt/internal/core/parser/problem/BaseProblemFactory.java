/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.parser.problem;

import java.util.Map;

import org.eclipse.cdt.core.parser.IProblem;


/**
 * @author jcamelon
 */
public abstract class BaseProblemFactory {

	protected final static String PROBLEM_PATTERN = "BaseProblemFactory.problemPattern"; //$NON-NLS-1$

	public abstract String createMessage(int id, Map arguments, int lineNumber, char[] fileName ); 

	public IProblem createProblem(int id, int start, int end, int line, char[] file, String message, Map arguments, boolean warn, boolean error) {
		return new Problem( id, start, end, line, file, message, arguments, warn, error);
	} 

	public boolean checkBitmask( int id, int bitmask )
	{
		return ( id & bitmask ) != 0; 
	}
	
	protected IProblem createInternalProblem( int id, int start, int end, int line, char [] file, Map arguments, boolean warn, boolean error )
	{
		return createProblem( id, start, end, line, file, createInternalMessage( id, arguments, line, file), arguments, warn, error );
	}

	/**
	 * @param id
	 * @param arguments
	 * @param line
	 * @param file
	 * @return
	 */
	private String createInternalMessage(int id, Map arguments, int line, char[] file)
	{
		if( checkBitmask( id, IProblem.INTERNAL_RELATED ))
		{
			StringBuffer buffer = new StringBuffer();
			
			switch( id )
			{
			}
			 
			return buffer.toString();
		}
		return null;
	}

}
