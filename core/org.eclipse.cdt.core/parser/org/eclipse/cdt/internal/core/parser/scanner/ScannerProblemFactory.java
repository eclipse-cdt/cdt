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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.internal.core.parser.problem.*;

/**
 * @author jcamelon
 *
 */
public class ScannerProblemFactory extends BaseProblemFactory implements IProblemFactory
{
	protected static final Map errorMessages;
	static {
		errorMessages = new HashMap();
		errorMessages.put(
			new Integer(IProblem.PREPROCESSOR_POUND_ERROR),
			"#error encountered with text: ");
		errorMessages.put(
			new Integer(IProblem.PREPROCESSOR_INCLUSION_NOT_FOUND),
			"Preprocessor Inclusion not found:  ");
		errorMessages.put(
			new Integer(IProblem.PREPROCESSOR_DEFINITION_NOT_FOUND),
			"Macro definition not found: ");
		errorMessages.put(
			new Integer(IProblem.PREPROCESSOR_INVALID_MACRO_DEFN),
			"Macro definition malformed for macro: ");
		errorMessages.put(
			new Integer(IProblem.PREPROCESSOR_INVALID_MACRO_REDEFN),
			"Invalid macro redefinition for macro : ");
		errorMessages.put(
			new Integer(IProblem.PREPROCESSOR_UNBALANCE_CONDITION),
			"Preprocessor Conditionals unbalanced : ");
		errorMessages.put(
			new Integer(IProblem.PREPROCESSOR_CONDITIONAL_EVAL_ERROR),
			"Expression Evaluation error for condition : ");
		errorMessages.put(
			new Integer(IProblem.PREPROCESSOR_MACRO_USAGE_ERROR),
			"Macro usage error for macro :");
		errorMessages.put(
			new Integer(IProblem.PREPROCESSOR_CIRCULAR_INCLUSION),
			"Circular inclusion for file : ");
		errorMessages.put(
			new Integer(IProblem.PREPROCESSOR_INVALID_DIRECTIVE),
			"Invalid preprocessor directive :  ");
		errorMessages.put(
			new Integer(IProblem.PREPROCESSOR_MACRO_PASTING_ERROR),
			"Invalid use of macro pasting in macro : ");
		errorMessages.put(
			new Integer(IProblem.SCANNER_INVALID_ESCAPECHAR),
			"Invalid escape character encountered ");
		errorMessages.put(
			new Integer(IProblem.SCANNER_UNBOUNDED_STRING),
			"Unbounded string encountered ");
		errorMessages.put(
			new Integer(IProblem.SCANNER_BAD_FLOATING_POINT),
			"Invalid floating point format encountered ");
		errorMessages.put(
			new Integer(IProblem.SCANNER_BAD_HEX_FORMAT),
			"Invalid hexidecimal format encountered ");
		errorMessages.put(
			new Integer(IProblem.SCANNER_UNEXPECTED_EOF),
			"Unexpected End Of File encountered ");
		errorMessages.put(
			new Integer(IProblem.SCANNER_BAD_CHARACTER),
			"Bad character sequence encountered : ");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.BaseProblemFactory#createMessage()
	 */
	public String createMessage(int id, Map arguments, int lineNumber, char[] fileName)
	{
		StringBuffer buffer = new StringBuffer();

		buffer.append(PROBLEM);
		buffer.append(errorMessages.get(new Integer(id)));
		switch (id)
		{
			case IProblem.PREPROCESSOR_POUND_ERROR :
				buffer.append(arguments.get(IProblem.A_PREPROC_POUND_ERROR));
				break;
			case IProblem.PREPROCESSOR_INCLUSION_NOT_FOUND :
				buffer.append(arguments.get(IProblem.A_PREPROC_INCLUDE_FILENAME));
				break;
			case IProblem.PREPROCESSOR_DEFINITION_NOT_FOUND :
				buffer.append(arguments.get(IProblem.A_PREPROC_MACRO_NAME));
				break;
			case IProblem.PREPROCESSOR_UNBALANCE_CONDITION :
				buffer.append(arguments.get(IProblem.A_PREPROC_CONDITIONAL_MISMATCH));
				break;
			case IProblem.PREPROCESSOR_INVALID_MACRO_DEFN :
				buffer.append(arguments.get(IProblem.A_PREPROC_MACRO_NAME));
				break;
			case IProblem.PREPROCESSOR_INVALID_DIRECTIVE :
				buffer.append(arguments.get(IProblem.A_PREPROC_UNKNOWN_DIRECTIVE));
				break;
			case IProblem.PREPROCESSOR_INVALID_MACRO_REDEFN :
				buffer.append(arguments.get(IProblem.A_PREPROC_MACRO_NAME));
				break;
			case IProblem.PREPROCESSOR_CONDITIONAL_EVAL_ERROR :
				buffer.append(arguments.get(IProblem.A_PREPROC_CONDITION));
				break;
			case IProblem.PREPROCESSOR_MACRO_USAGE_ERROR :
				buffer.append(arguments.get(IProblem.A_PREPROC_MACRO_NAME));
				break;
			case IProblem.PREPROCESSOR_MACRO_PASTING_ERROR :
				buffer.append(arguments.get(IProblem.A_PREPROC_MACRO_NAME));
				break;
			case IProblem.PREPROCESSOR_CIRCULAR_INCLUSION :
				buffer.append(arguments.get(IProblem.A_PREPROC_INCLUDE_FILENAME));
				break;
			case IProblem.SCANNER_BAD_CHARACTER :
				buffer.append( arguments.get(IProblem.A_SCANNER_BADCHAR));
				break;
			case IProblem.SCANNER_UNBOUNDED_STRING :
			case IProblem.SCANNER_INVALID_ESCAPECHAR :
			case IProblem.SCANNER_BAD_FLOATING_POINT :
			case IProblem.SCANNER_BAD_HEX_FORMAT :
			case IProblem.SCANNER_UNEXPECTED_EOF :
				break;
			default :
				return null;
		}

		buffer.append( IN_FILE );
		buffer.append(fileName);
		buffer.append( ON_LINE );
		buffer.append(lineNumber);
		return buffer.toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IProblemFactory#createProblem(int, int, int, int, char[], java.lang.String, boolean, boolean)
	 */
	public IProblem createProblem(
		int id,
		int start,
		int end,
		int line,
		char[] file,
		Map arguments,
		boolean warn,
		boolean error)
	{
		if( checkBitmask( id, IProblem.INTERNAL_RELATED ) )  
			return createInternalProblem( id, start, end, line, file, arguments, warn, error );		
		
		if ( 	checkBitmask( id, IProblem.SCANNER_RELATED ) || 
				checkBitmask( id, IProblem.PREPROCESSOR_RELATED ) )
			return super.createProblem(
				id,
				start,
				end,
				line,
				file,
				createMessage(id, arguments, line, file),
				arguments,
				warn,
				error);
				
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IProblemFactory#getRequiredAttributesForId(int)
	 */
	public String[] getRequiredAttributesForId(int id)
	{
		String [] result = new String[1]; 
		switch (id)
		{
			case IProblem.PREPROCESSOR_POUND_ERROR :
				result[0] = IProblem.A_PREPROC_POUND_ERROR;
				break;
			case IProblem.PREPROCESSOR_INCLUSION_NOT_FOUND :
				result[0] = IProblem.A_PREPROC_INCLUDE_FILENAME;
				break;
			case IProblem.PREPROCESSOR_DEFINITION_NOT_FOUND :
				result[0] = IProblem.A_PREPROC_MACRO_NAME;
				break;
			case IProblem.PREPROCESSOR_UNBALANCE_CONDITION :
				result[0] = IProblem.A_PREPROC_CONDITIONAL_MISMATCH;
				break;
			case IProblem.PREPROCESSOR_INVALID_MACRO_DEFN :
				result[0] = IProblem.A_PREPROC_MACRO_NAME;
				break;
			case IProblem.PREPROCESSOR_INVALID_DIRECTIVE :
				result[0] = IProblem.A_PREPROC_UNKNOWN_DIRECTIVE;
				break;
			case IProblem.PREPROCESSOR_INVALID_MACRO_REDEFN :
				result[0] = IProblem.A_PREPROC_MACRO_NAME;
				break;
			case IProblem.PREPROCESSOR_CONDITIONAL_EVAL_ERROR :
				result[0] = IProblem.A_PREPROC_CONDITION;
				break;
			case IProblem.PREPROCESSOR_MACRO_USAGE_ERROR :
				result[0] = IProblem.A_PREPROC_MACRO_NAME;
				break;
			case IProblem.PREPROCESSOR_MACRO_PASTING_ERROR :
				result[0] = IProblem.A_PREPROC_MACRO_NAME;
				break;
			case IProblem.PREPROCESSOR_CIRCULAR_INCLUSION :
				result[0] = IProblem.A_PREPROC_INCLUDE_FILENAME;
				break;
			case IProblem.SCANNER_BAD_CHARACTER :
				result[0] = IProblem.A_SCANNER_BADCHAR; 
				break;
			case IProblem.SCANNER_UNBOUNDED_STRING :
			case IProblem.SCANNER_INVALID_ESCAPECHAR :
			case IProblem.SCANNER_BAD_FLOATING_POINT :
			case IProblem.SCANNER_BAD_HEX_FORMAT :
			case IProblem.SCANNER_UNEXPECTED_EOF :
				break;
		}
		return result;
	}
}
