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

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.internal.core.parser.ParserMessages;
import org.eclipse.cdt.internal.core.parser.problem.BaseProblemFactory;
import org.eclipse.cdt.internal.core.parser.problem.IProblemFactory;

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
			ParserMessages.getString("ScannerProblemFactory.error.preproc.error")); //$NON-NLS-1$
		errorMessages.put(
			new Integer(IProblem.PREPROCESSOR_INCLUSION_NOT_FOUND),
			ParserMessages.getString("ScannerProblemFactory.error.preproc.inclusionNotFound")); //$NON-NLS-1$
		errorMessages.put(
			new Integer(IProblem.PREPROCESSOR_DEFINITION_NOT_FOUND),
			ParserMessages.getString("ScannerProblemFactory.error.preproc.definitionNotFound")); //$NON-NLS-1$
		errorMessages.put(
			new Integer(IProblem.PREPROCESSOR_INVALID_MACRO_DEFN),
			ParserMessages.getString("ScannerProblemFactory.error.preproc.invalidMacroDefn")); //$NON-NLS-1$
		errorMessages.put(
			new Integer(IProblem.PREPROCESSOR_INVALID_MACRO_REDEFN),
			ParserMessages.getString("ScannerProblemFactory.error.preproc.invalidMacroRedefn")); //$NON-NLS-1$
		errorMessages.put(
			new Integer(IProblem.PREPROCESSOR_UNBALANCE_CONDITION),
			ParserMessages.getString("ScannerProblemFactory.error.preproc.unbalancedConditional")); //$NON-NLS-1$
		errorMessages.put(
			new Integer(IProblem.PREPROCESSOR_CONDITIONAL_EVAL_ERROR),
			ParserMessages.getString("ScannerProblemFactory.error.preproc.conditionalEval")); //$NON-NLS-1$
		errorMessages.put(
			new Integer(IProblem.PREPROCESSOR_MACRO_USAGE_ERROR),
			ParserMessages.getString("ScannerProblemFactory.error.preproc.macroUsage")); //$NON-NLS-1$
		errorMessages.put(
			new Integer(IProblem.PREPROCESSOR_CIRCULAR_INCLUSION),
			ParserMessages.getString("ScannerProblemFactory.error.preproc.circularInclusion")); //$NON-NLS-1$
		errorMessages.put(
			new Integer(IProblem.PREPROCESSOR_INVALID_DIRECTIVE),
			ParserMessages.getString("ScannerProblemFactory.error.preproc.invalidDirective")); //$NON-NLS-1$
		errorMessages.put(
			new Integer(IProblem.PREPROCESSOR_MACRO_PASTING_ERROR),
			ParserMessages.getString("ScannerProblemFactory.error.preproc.macroPasting")); //$NON-NLS-1$
		errorMessages.put(
			new Integer(IProblem.SCANNER_INVALID_ESCAPECHAR),
			ParserMessages.getString("ScannerProblemFactory.error.scanner.invalidEscapeChar")); //$NON-NLS-1$
		errorMessages.put(
			new Integer(IProblem.SCANNER_UNBOUNDED_STRING),
			ParserMessages.getString("ScannerProblemFactory.error.scanner.unboundedString")); //$NON-NLS-1$
		errorMessages.put(
			new Integer(IProblem.SCANNER_BAD_FLOATING_POINT),
			ParserMessages.getString("ScannerProblemFactory.error.scanner.badFloatingPoint")); //$NON-NLS-1$
		errorMessages.put(
			new Integer(IProblem.SCANNER_BAD_HEX_FORMAT),
			ParserMessages.getString("ScannerProblemFactory.error.scanner.badHexFormat")); //$NON-NLS-1$
		errorMessages.put(
			new Integer(IProblem.SCANNER_UNEXPECTED_EOF),
			ParserMessages.getString("ScannerProblemFactory.error.scanner.unexpectedEOF")); //$NON-NLS-1$
		errorMessages.put(
			new Integer(IProblem.SCANNER_BAD_CHARACTER),
			ParserMessages.getString("ScannerProblemFactory.error.scanner.badCharacter")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.BaseProblemFactory#createMessage()
	 */
	public String createMessage(int id, Map arguments, int lineNumber, char[] fileName)
	{
		String message = (String) errorMessages.get( new Integer(id) );
		String arg = null; 

		switch (id)
		{
			case IProblem.PREPROCESSOR_POUND_ERROR :
				arg = (String) arguments.get(IProblem.A_PREPROC_POUND_ERROR);
				break;
			case IProblem.PREPROCESSOR_INCLUSION_NOT_FOUND :
				arg = (String) arguments.get((IProblem.A_PREPROC_INCLUDE_FILENAME));
				break;
			case IProblem.PREPROCESSOR_DEFINITION_NOT_FOUND :
				arg = (String) arguments.get(IProblem.A_PREPROC_MACRO_NAME);
				break;
			case IProblem.PREPROCESSOR_UNBALANCE_CONDITION :
				arg = (String) arguments.get(IProblem.A_PREPROC_CONDITIONAL_MISMATCH);
				break;
			case IProblem.PREPROCESSOR_INVALID_MACRO_DEFN :
				arg = (String) arguments.get(IProblem.A_PREPROC_MACRO_NAME);
				break;
			case IProblem.PREPROCESSOR_INVALID_DIRECTIVE :
				arg = (String) arguments.get(IProblem.A_PREPROC_UNKNOWN_DIRECTIVE);
				break;
			case IProblem.PREPROCESSOR_INVALID_MACRO_REDEFN :
				arg = (String) arguments.get(IProblem.A_PREPROC_MACRO_NAME);
				break;
			case IProblem.PREPROCESSOR_CONDITIONAL_EVAL_ERROR :
				arg = (String) arguments.get(IProblem.A_PREPROC_CONDITION);
				break;
			case IProblem.PREPROCESSOR_MACRO_USAGE_ERROR :
				arg = (String) arguments.get(IProblem.A_PREPROC_MACRO_NAME);
				break;
			case IProblem.PREPROCESSOR_MACRO_PASTING_ERROR :
				arg = (String) arguments.get(IProblem.A_PREPROC_MACRO_NAME);
				break;
			case IProblem.PREPROCESSOR_CIRCULAR_INCLUSION :
				arg = (String) arguments.get(IProblem.A_PREPROC_INCLUDE_FILENAME);
				break;
			case IProblem.SCANNER_BAD_CHARACTER :
				arg = (String) arguments.get(IProblem.A_SCANNER_BADCHAR);
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
		
		if( arg != null ){
			message = MessageFormat.format( message, new Object [] { arg } );
		}
		
		Object [] args = { message, new String( fileName ), new Integer( lineNumber ) };
		return ParserMessages.getFormattedString( PROBLEM_PATTERN, args );
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
