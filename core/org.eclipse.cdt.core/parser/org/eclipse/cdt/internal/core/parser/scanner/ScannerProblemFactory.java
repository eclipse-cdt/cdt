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
package org.eclipse.cdt.internal.core.parser.scanner;

import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.internal.core.parser.problem.BaseProblemFactory;
import org.eclipse.cdt.internal.core.parser.problem.IProblemFactory;

/**
 * @author jcamelon
 *
 */
public class ScannerProblemFactory extends BaseProblemFactory implements IProblemFactory
{
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
		
		if ( 	checkBitmask( id, IProblem.SCANNER_RELATED ) || 
				checkBitmask( id, IProblem.PREPROCESSOR_RELATED ) )
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
	 * @see org.eclipse.cdt.internal.core.parser.IProblemFactory#getRequiredAttributesForId(int)
	 */
	public String getRequiredAttributesForId(int id)
	{
		switch (id)
		{
			case IProblem.PREPROCESSOR_POUND_ERROR :
				return  IProblem.A_PREPROC_POUND_ERROR;
			case IProblem.PREPROCESSOR_INCLUSION_NOT_FOUND :
				return  IProblem.A_PREPROC_INCLUDE_FILENAME;
			case IProblem.PREPROCESSOR_DEFINITION_NOT_FOUND :
				return  IProblem.A_PREPROC_MACRO_NAME;
			case IProblem.PREPROCESSOR_UNBALANCE_CONDITION :
				return  IProblem.A_PREPROC_CONDITIONAL_MISMATCH;
			case IProblem.PREPROCESSOR_INVALID_MACRO_DEFN :
				return  IProblem.A_PREPROC_MACRO_NAME;
			case IProblem.PREPROCESSOR_INVALID_DIRECTIVE :
				return  IProblem.A_PREPROC_UNKNOWN_DIRECTIVE;
			case IProblem.PREPROCESSOR_INVALID_MACRO_REDEFN :
				return  IProblem.A_PREPROC_MACRO_NAME;
			case IProblem.PREPROCESSOR_CONDITIONAL_EVAL_ERROR :
				return  IProblem.A_PREPROC_CONDITION;
			case IProblem.PREPROCESSOR_MACRO_USAGE_ERROR :
				return  IProblem.A_PREPROC_MACRO_NAME;
			case IProblem.PREPROCESSOR_MACRO_PASTING_ERROR :
				return  IProblem.A_PREPROC_MACRO_NAME;
			case IProblem.PREPROCESSOR_CIRCULAR_INCLUSION :
				return  IProblem.A_PREPROC_INCLUDE_FILENAME;
			case IProblem.SCANNER_BAD_CHARACTER :
				return  IProblem.A_SCANNER_BADCHAR; 
			case IProblem.SCANNER_UNBOUNDED_STRING :
			case IProblem.SCANNER_INVALID_ESCAPECHAR :
			case IProblem.SCANNER_BAD_FLOATING_POINT :
			case IProblem.SCANNER_BAD_HEX_FORMAT :
			case IProblem.SCANNER_UNEXPECTED_EOF :
				break;
		}
		return null;
	}
}
