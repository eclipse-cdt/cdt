/**********************************************************************
 * Copyright (c) 2002-2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.parser;

import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.internal.core.parser.problem.BaseProblemFactory;
import org.eclipse.cdt.internal.core.parser.problem.IProblemFactory;

/**
 * @author jcamelon
 *
 */
public class ParserProblemFactory extends BaseProblemFactory
		implements
			IProblemFactory {

	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IProblemFactory#createProblem(int, int, int, int, char[], java.lang.String, boolean, boolean)
	 */
	public IProblem createProblem(
		int id,
		int start,
		int end,
		int line,
		char[] file,
		char[] arg,
		boolean warn,
		boolean error)
	{
		if( checkBitmask( id, IProblem.INTERNAL_RELATED ) )  
			return createInternalProblem( id, start, end, line, file, arg, warn, error );		
		
		if ( 	checkBitmask( id, IProblem.SYNTAX_RELATED ) )
			return super.createProblem(
				id,
				start,
				end,
				line,
				file,
				arg,
				warn,
				error);
				
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.problem.IProblemFactory#getRequiredAttributesForId(int)
	 */
	public String getRequiredAttributesForId(int id) {
		return ""; //$NON-NLS-1$
	}

}
