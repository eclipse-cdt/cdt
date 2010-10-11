/*******************************************************************************
 *  Copyright (c) 2000, 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     John Camelon (IBM Corporation) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser;


/**
 * Description of a C/C++ syntax problems and spelling errors as detected by the lexer, preprocessor,
 * parser or the spelling engine.
 * 
 * A problem provides access to:
 * <ul>
 * <li> its location (originating source file name, source position, line number), </li>
 * <li> its message description and a predicate to check its severity (warning or error). </li>
 * <li> its ID : an number identifying the very nature of this problem. All possible IDs are listed
 * as constants on this interface. </li>
 * </ul>
 * <p> Note, that semantic problems are modeled via {@link org.eclipse.cdt.core.dom.ast.ISemanticProblem}.
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IProblem
{
	/**
	 * Returns the problem id
	 */
	int getID();

	/**
	 * Returns a human-readable message describing the problem.
	 */
	String getMessage();

	/**
	 * Returns a human-readable message string describing the problem, adding
	 * location information.
	 */
	String getMessageWithLocation();

	/**
	 * Returns a possibly empty argument array to compute the message.  
	 */
	String[] getArguments();

	/**
	 * Returns the file name in which the problem was found
	 */
	char[] getOriginatingFileName();

	/**
	 * Returns the start position of the problem (inclusive), or {@link #INT_VALUE_NOT_PROVIDED} if unknown.
	 */
	int getSourceStart();

	/**
	 * Returns the end position of the problem (inclusive), or {@link #INT_VALUE_NOT_PROVIDED} if unknown.
	 */
	int getSourceEnd();

	/**
	 * Returns the line number where the problem begins, or {@link #INT_VALUE_NOT_PROVIDED} if unknown.
	 */
	int getSourceLineNumber();

	/**
	 * Returns whether the problem is an error.
	 */
	boolean isError();

	/**
	 * Returns whether the problem is a warning.
	 */
	boolean isWarning();

	
	/**
	 * -1, returned when an offset or a line number is unknown.
	 */
	public final static int INT_VALUE_NOT_PROVIDED = -1;
	
	/**
	 * Problem Categories
	 * The high bits of a problem ID contains information about the category of a problem. 
	 * For example, (problemID & TypeRelated) != 0, indicates that this problem is type related.
	 * 
	 * A problem category can help to implement custom problem filters. Indeed, when numerous problems
	 * are listed, focusing on import related problems first might be relevant.
	 * 
	 * When a problem is tagged as Internal, it means that no change other than a local source code change
	 * can  fix the corresponding problem.
	 */
	
	/**
	 * IProblem relates to a valid error on the Scanner
	 */
	public final static int SCANNER_RELATED = 0x01000000;
	
	/**
	 * IProblem relates to a valid error on the preprocessor 
	 */
	public final static int PREPROCESSOR_RELATED = 0x02000000;
	
	/**
	 * IProblem relates to a valid syntax error in the parser
	 */
	public final static int SYNTAX_RELATED = 0x04000000;
	
	/**
	 * IProblem relates to an implementation of design limitation
	 */
	public final static int INTERNAL_RELATED = 0x10000000;


	/**
	 * Check the parameter bit-mask against an IProblem's ID to broadly segregate the 
	 * types of problems.  
	 * 
	 * @param bitmask
	 * @return true if ( (id & bit-mask ) != 0 )
	 */
	public boolean checkCategory(int bitmask);

	/**
	 * Mask to use in order to filter out the category portion of the problem ID.
	 */
	public final static int IGNORE_CATEGORIES_MASK = 0xFFFFFF;


	// Lexer
	/** 
	 * Bad character encountered by Scanner. 
	 * Required attributes: A_SCANNER_BADCHAR
	 */
	public final static int SCANNER_BAD_CHARACTER = SCANNER_RELATED | 0x001;
	
	/** 
	 * Unbounded literal string encountered by Scanner. 
	 * Required attributes: none.  
	 */
	public final static int SCANNER_UNBOUNDED_STRING = SCANNER_RELATED | 0x002;
	
	/** 
	 * Invalid escape sequence encountered by Scanner. 
	 * Required attributes: none.  
	 */
	public final static int SCANNER_INVALID_ESCAPECHAR = SCANNER_RELATED | 0x003;
	
	/** 
	 * Bad floating point encountered by Scanner. 
	 * Required attributes: none.  
	 */
	public final static int SCANNER_BAD_FLOATING_POINT = SCANNER_RELATED | 0x004;
	
	/** 
	 * Bad hexadecimal encountered by Scanner. 
	 * Required attributes: none.  
	 */
	public final static int SCANNER_BAD_HEX_FORMAT = SCANNER_RELATED | 0x005;
	
	/** 
	 * Unexpected EOF encountered by Scanner.   
	 * Required attributes: none.  
	 */
	public final static int SCANNER_UNEXPECTED_EOF = SCANNER_RELATED | 0x006;
	
	/** 
	 * Bad octal encountered by Scanner. 
	 * Required attributes: none.  
	 */
	public final static int SCANNER_BAD_OCTAL_FORMAT = SCANNER_RELATED | 0x007;

	/** 
	 * Bad decimal encountered by Scanner. 
	 * Required attributes: none.  
	 */
	public final static int SCANNER_BAD_DECIMAL_FORMAT = SCANNER_RELATED | 0x008;

	/** 
	 * Assignment '=' encountered in macro by Scanner. 
	 * Required attributes: none.  
	 */
	public final static int SCANNER_ASSIGNMENT_NOT_ALLOWED = SCANNER_RELATED | 0x009;

	/** 
	 * Division by 0 encountered in macro by Scanner. 
	 * Required attributes: none.  
	 */
	public final static int SCANNER_DIVIDE_BY_ZERO = SCANNER_RELATED | 0x00A;
	
	/** 
	 * Missing ')' encountered in macro by Scanner. 
	 * Required attributes: none.  
	 */
	public final static int SCANNER_MISSING_R_PAREN = SCANNER_RELATED | 0x00B;	

	/** 
	 * Expression syntax error encountered in macro by Scanner. 
	 * Required attributes: none.  
	 */
	public final static int SCANNER_EXPRESSION_SYNTAX_ERROR = SCANNER_RELATED | 0x00C;
	
	/** 
	 * Expression syntax error encountered in macro by Scanner. 
	 * Required attributes: none.  
	 */
	public final static int SCANNER_ILLEGAL_IDENTIFIER = SCANNER_RELATED | 0x00D;

	/** 
	 * Division by 0 encountered in macro by Scanner. 
	 * Required attributes: none.  
	 */
	public final static int SCANNER_BAD_CONDITIONAL_EXPRESSION = SCANNER_RELATED | 0x00E;
	
	/** 
	 * Bad binary encountered by Scanner. 
	 * Required attributes: none.  
	 * @since 5.1
	 */
	public final static int SCANNER_BAD_BINARY_FORMAT = SCANNER_RELATED | 0x00F;

	// Preprocessor
	/**
	 *	#error encountered by Preprocessor.  
	 * Required attributes:  A_PREPROC_POUND_ERROR
	 * @see #A_PREPROC_POUND_ERROR
	 */
	public final static int PREPROCESSOR_POUND_ERROR = PREPROCESSOR_RELATED | 0x001;
	
	/**
	 *	Inclusion not found by Preprocessor.  
	 * Required attributes: A_PREPROC_INCLUDE_FILENAME
	 * @see #A_PREPROC_INCLUDE_FILENAME
	 */	
	public final static int PREPROCESSOR_INCLUSION_NOT_FOUND = PREPROCESSOR_RELATED | 0x002;
	
	/**
	 *	Macro definition not found by Preprocessor.  
	 * Required attributes:  A_PREPROC_MACRO_NAME
	 * @see #A_PREPROC_MACRO_NAME
	 */ 
	public final static int PREPROCESSOR_DEFINITION_NOT_FOUND = PREPROCESSOR_RELATED | 0x003;
	
	/**
	 *	Preprocessor conditionals seem unbalanced.  
	 * Required attributes:  A_PREPROC_CONDITIONAL_MISMATCH
	 * @see #A_PREPROC_CONDITIONAL_MISMATCH
	 */
	
	public final static int PREPROCESSOR_UNBALANCE_CONDITION = PREPROCESSOR_RELATED | 0x004;
	
	/**
	 *	Invalid format to Macro definition.    
	 * Required attributes:  A_PREPROC_MACRO_NAME
	 * @see #A_PREPROC_MACRO_NAME
	 */	
	public final static int PREPROCESSOR_INVALID_MACRO_DEFN = PREPROCESSOR_RELATED | 0x005;
	
	/**
	 *	Invalid or unknown preprocessor directive encountered by Preprocessor.  
	 * Required attributes: A_PREPROC_UNKNOWN_DIRECTIVE
	 * @see #A_PREPROC_UNKNOWN_DIRECTIVE
	 */	
	public final static int PREPROCESSOR_INVALID_DIRECTIVE = PREPROCESSOR_RELATED | 0x006;
	
	/**
	 *	Invalid macro redefinition encountered by Preprocessor.    
	 * Required attributes: A_PREPROC_MACRO_NAME
	 * @see #A_PREPROC_MACRO_NAME
	 */	
	public final static int PREPROCESSOR_INVALID_MACRO_REDEFN = PREPROCESSOR_RELATED | 0x007;
	
	/**
	 *	Preprocessor Conditional cannot not be evaluated due.    
	 * Required attributes: A_PREPROC_CONDITION
	 * @see #A_PREPROC_CONDITION
	 */	
	public final static int PREPROCESSOR_CONDITIONAL_EVAL_ERROR = PREPROCESSOR_RELATED | 0x008;
	
	/**
	 * Invalid macro usage encountered by Preprocessor.  
	 * Required attributes: A_PREPROC_MACRO_NAME
	 * @see #A_PREPROC_MACRO_NAME
	 */	
	public final static int PREPROCESSOR_MACRO_USAGE_ERROR = PREPROCESSOR_RELATED | 0x009;
	
	/**
	 * Invalid Macro Pasting encountered by Preprocessor. 
	 * Required attributes: A_PREPROC_MACRO_NAME
	 * @see #A_PREPROC_MACRO_NAME
	 */
	public final static int PREPROCESSOR_MACRO_PASTING_ERROR = PREPROCESSOR_RELATED | 0x00A;
	
	/**
	 * Circular inclusion encountered by Preprocessor.  
	 * Required attributes: A_PREPROC_INCLUDE_FILENAME
	 * @see #A_PREPROC_INCLUDE_FILENAME
	 */	
	public final static int PREPROCESSOR_CIRCULAR_INCLUSION = PREPROCESSOR_RELATED | 0x00B;
	
	/**
	 * macro argument "..." encountered without the required ')' i.e. must be last argument if used  
	 * Required attributes: none
	 */	
	public final static int PREPROCESSOR_MISSING_RPAREN_PARMLIST = PREPROCESSOR_RELATED | 0x00C;	

	/**
	 * __VA_ARGS__ encountered in macro definition without the required '...' parameter  
	 * Required attributes: none
	 */	
	public final static int PREPROCESSOR_INVALID_VA_ARGS = PREPROCESSOR_RELATED | 0x00D;
	
	/**
	 * #warning encountered by Preprocessor.  
	 * Required attributes:  A_PREPROC_POUND_WARNING
	 * @see #A_PREPROC_POUND_WARNING
	 */
	public final static int PREPROCESSOR_POUND_WARNING = PREPROCESSOR_RELATED | 0x00E;
	
	/**
	 * Syntax error, detected by the parser.
	 */
	public final static int SYNTAX_ERROR = SYNTAX_RELATED | 0x001;

	
	@Deprecated
	public final static int SEMANTICS_RELATED = 0x08000000;
	@Deprecated
	public final static String A_PREPROC_POUND_ERROR = ""; //$NON-NLS-1$
	@Deprecated
	public final static String A_PREPROC_POUND_WARNING = ""; //$NON-NLS-1$
	@Deprecated
	public final static String A_PREPROC_INCLUDE_FILENAME = ""; //$NON-NLS-1$
	@Deprecated
	public final static String A_PREPROC_MACRO_NAME = ""; //$NON-NLS-1$
	@Deprecated
	public final static String A_PREPROC_CONDITION = ""; //$NON-NLS-1$
	@Deprecated
	public final static String A_PREPROC_UNKNOWN_DIRECTIVE = ""; //$NON-NLS-1$
	@Deprecated
	public final static String A_PREPROC_CONDITIONAL_MISMATCH = ""; //$NON-NLS-1$
	@Deprecated
	public static final String A_SCANNER_BADCHAR = ""; //$NON-NLS-1$
	@Deprecated
	public static final String A_SYMBOL_NAME = ""; //$NON-NLS-1$
	@Deprecated
	public static final String A_NAMESPACE_NAME = ""; //$NON-NLS-1$
	@Deprecated
	public static final String A_TYPE_NAME = ""; //$NON-NLS-1$
	@Deprecated
	public final static String FILENAME_NOT_PROVIDED = ""; //$NON-NLS-1$
	@Deprecated
	public final static int SEMANTIC_UNIQUE_NAME_PREDEFINED = SEMANTICS_RELATED | 0x001;
	@Deprecated
	public final static int SEMANTIC_NAME_NOT_FOUND = SEMANTICS_RELATED | 0x002;
	@Deprecated
	public final static int SEMANTIC_NAME_NOT_PROVIDED = SEMANTICS_RELATED | 0x003;
	@Deprecated
	public static final int SEMANTIC_INVALID_OVERLOAD = SEMANTICS_RELATED | 0x004;
	@Deprecated
	public static final int SEMANTIC_INVALID_USING = SEMANTICS_RELATED | 0x005;
	@Deprecated
	public static final int SEMANTIC_AMBIGUOUS_LOOKUP = SEMANTICS_RELATED | 0x006;
	@Deprecated
	public static final int SEMANTIC_INVALID_TYPE = SEMANTICS_RELATED | 0x007;
	@Deprecated
	public static final int SEMANTIC_CIRCULAR_INHERITANCE = SEMANTICS_RELATED | 0x008;
	@Deprecated
	public static final int SEMANTIC_INVALID_TEMPLATE = SEMANTICS_RELATED | 0x009;
	@Deprecated
	public static final int SEMANTIC_BAD_VISIBILITY = SEMANTICS_RELATED | 0x00A;
	@Deprecated
	public static final int SEMANTIC_UNABLE_TO_RESOLVE_FUNCTION = SEMANTICS_RELATED | 0x00B;
	@Deprecated
	public static final int SEMANTIC_INVALID_TEMPLATE_ARGUMENT = SEMANTICS_RELATED | 0x00C;
	@Deprecated
	public static final int SEMANTIC_INVALID_TEMPLATE_PARAMETER = SEMANTICS_RELATED | 0x00D;
	@Deprecated
	public static final int SEMANTIC_REDECLARED_TEMPLATE_PARAMETER = SEMANTICS_RELATED | 0x00E;
	@Deprecated
	public static final int SEMANTIC_INVALID_CONVERSION_TYPE = SEMANTICS_RELATED | 0x00F;
	@Deprecated
	public static final int SEMANTIC_MALFORMED_EXPRESSION = SEMANTICS_RELATED | 0x010;
	@Deprecated
	public static final int SEMANTIC_ILLFORMED_FRIEND = SEMANTICS_RELATED | 0x011;
	@Deprecated
	public static final int SEMANTIC_RECURSIVE_TEMPLATE_INSTANTIATION = SEMANTICS_RELATED | 0x012;
}
