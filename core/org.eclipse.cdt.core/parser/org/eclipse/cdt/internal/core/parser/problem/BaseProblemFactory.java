/*******************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.problem;

import org.eclipse.cdt.core.parser.IProblem;


/**
 * @author jcamelon
 */
public abstract class BaseProblemFactory {

	protected final static String PROBLEM_PATTERN = "BaseProblemFactory.problemPattern"; //$NON-NLS-1$

	public IProblem createProblem(int id, int start, int end, int line, char[] file, char[] arg, boolean warn, boolean error) {
		return new Problem( id, start, end, line, file, arg, warn, error);
	} 

	public boolean checkBitmask( int id, int bitmask )
	{
		return ( id & bitmask ) != 0; 
	}
	
	protected IProblem createInternalProblem( int id, int start, int end, int line, char [] file, char[] arg, boolean warn, boolean error )
	{
		return createProblem( id, start, end, line, file, arg, warn, error );
	}

	/**
	 * @param id
	 * @param arguments
	 * @param line
	 * @param file
	 * @return
	 */

}
