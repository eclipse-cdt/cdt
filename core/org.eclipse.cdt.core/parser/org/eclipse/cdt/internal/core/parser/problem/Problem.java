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

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.internal.core.parser.ParserMessages;

/**
 * @author jcamelon
 *
 */
public class Problem implements IProblem {

	private final char[] arg;
	private final int id;
	private final int sourceStart;
	private final int sourceEnd;
	private final int lineNumber;

	private final boolean isError;
	private final boolean isWarning;	
	private final char[] originatingFileName;
	
	private String message = null;

	public Problem( int id, int start, int end, int line, char [] file, char[] arg, boolean warn, boolean error )
	{
		this.id = id;
		this.sourceStart = start;
		this.sourceEnd = end;
		this.lineNumber = line;
		this.originatingFileName = file;
		this.arg = arg;
		this.isWarning = warn;
		this.isError = error;
	}
	

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IProblem#getID()
	 */
	public int getID() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IProblem#getOriginatingFileName()
	 */
	public char[] getOriginatingFileName() {
		return originatingFileName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IProblem#getSourceEnd()
	 */
	public int getSourceEnd() {
		return sourceEnd;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IProblem#getSourceLineNumber()
	 */
	public int getSourceLineNumber() {
		return lineNumber;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IProblem#getSourceStart()
	 */
	public int getSourceStart() {
		return sourceStart;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IProblem#isError()
	 */
	public boolean isError() {
		return isError;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IProblem#isWarning()
	 */
	public boolean isWarning() {
		return isWarning;
	}

	protected static final Map errorMessages;
	static {
		errorMessages = new HashMap();
		errorMessages.put( 
				new Integer( IProblem.SEMANTIC_UNIQUE_NAME_PREDEFINED),
				ParserMessages.getString("ASTProblemFactory.error.semantic.uniqueNamePredefined")); //$NON-NLS-1$
		errorMessages.put( 
				new Integer( IProblem.SEMANTIC_NAME_NOT_FOUND), 
				ParserMessages.getString("ASTProblemFactory.error.semantic.nameNotFound")); //$NON-NLS-1$
		errorMessages.put( 
				new Integer( IProblem.SEMANTIC_NAME_NOT_PROVIDED), 
				ParserMessages.getString("ASTProblemFactory.error.semantic.nameNotProvided")); //$NON-NLS-1$
		errorMessages.put(
				new Integer( IProblem.SEMANTIC_INVALID_CONVERSION_TYPE ),
				ParserMessages.getString("ASTProblemFactory.error.semantic.invalidConversionType")); //$NON-NLS-1$
		errorMessages.put(
				new Integer( IProblem.SEMANTIC_AMBIGUOUS_LOOKUP ),
				ParserMessages.getString("ASTProblemFactory.error.semantic.pst.ambiguousLookup")); //$NON-NLS-1$
		errorMessages.put(
				new Integer( IProblem.SEMANTIC_INVALID_TYPE ),
				ParserMessages.getString("ASTProblemFactory.error.semantic.pst.invalidType")); //$NON-NLS-1$
		errorMessages.put(
				new Integer( IProblem.SEMANTIC_CIRCULAR_INHERITANCE ),
				ParserMessages.getString("ASTProblemFactory.error.semantic.pst.circularInheritance")); //$NON-NLS-1$
		errorMessages.put(
				new Integer( IProblem.SEMANTIC_INVALID_OVERLOAD ),
				ParserMessages.getString("ASTProblemFactory.error.semantic.pst.invalidOverload")); //$NON-NLS-1$
		errorMessages.put(
				new Integer( IProblem.SEMANTIC_INVALID_TEMPLATE ),
				ParserMessages.getString("ASTProblemFactory.error.semantic.pst.invalidTemplate")); //$NON-NLS-1$
		errorMessages.put(
				new Integer( IProblem.SEMANTIC_INVALID_USING ),
				ParserMessages.getString("ASTProblemFactory.error.semantic.pst.invalidUsing")); //$NON-NLS-1$
		errorMessages.put(
				new Integer( IProblem.SEMANTIC_BAD_VISIBILITY ),
				ParserMessages.getString("ASTProblemFactory.error.semantic.pst.badVisibility")); //$NON-NLS-1$
		errorMessages.put(
				new Integer( IProblem.SEMANTIC_UNABLE_TO_RESOLVE_FUNCTION ),
				ParserMessages.getString("ASTProblemFactory.error.semantic.pst.unableToResolveFunction")); //$NON-NLS-1$
		errorMessages.put(
				new Integer( IProblem.SEMANTIC_INVALID_TEMPLATE_ARGUMENT ),
				ParserMessages.getString("ASTProblemFactory.error.semantic.pst.invalidTemplateArgument")); //$NON-NLS-1$
		errorMessages.put(
				new Integer( IProblem.SEMANTIC_INVALID_TEMPLATE_PARAMETER ),
				ParserMessages.getString("ASTProblemFactory.error.semantic.pst.invalidTemplateParameter")); //$NON-NLS-1$
		errorMessages.put(
				new Integer( IProblem.SEMANTIC_REDECLARED_TEMPLATE_PARAMETER ),
				ParserMessages.getString("ASTProblemFactory.error.semantic.pst.redeclaredTemplateParameter")); //$NON-NLS-1$
		errorMessages.put( 
		        new Integer( IProblem.SEMANTIC_RECURSIVE_TEMPLATE_INSTANTIATION ),
				ParserMessages.getString("ASTProblemFactory.error.semantic.pst.recursiveTemplateInstantiation")); //$NON-NLS-1$
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
		errorMessages.put( 
				new Integer( IProblem.SYNTAX_ERROR ), 
				ParserMessages.getString( "ParserProblemFactory.error.syntax.syntaxError")); //$NON-NLS-1$
	}
	protected final static String PROBLEM_PATTERN = "BaseProblemFactory.problemPattern"; //$NON-NLS-1$

	public String getMessage()
	{
		if( message != null )
			return message;
		
		String msg = (String) errorMessages.get( new Integer(id) );
		if( msg == null )
			msg = "";  //$NON-NLS-1$
		
		if( arg != null ){
			msg = MessageFormat.format( msg, new Object [] { arg } );
		}
		
		Object [] args = { msg, new String( originatingFileName ), new Integer( lineNumber ) };
		
		message = ParserMessages.getFormattedString( PROBLEM_PATTERN, args ); 
		return message; 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IProblem#checkCategory(int)
	 */
	public boolean checkCategory(int bitmask) {
		return ((id & bitmask) != 0 );
	}


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IProblem#getArguments()
	 */
	public String getArguments() {
		return String.valueOf(arg);
	}

}
