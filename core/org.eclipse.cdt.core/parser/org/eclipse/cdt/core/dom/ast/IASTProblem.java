/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.core.dom.ast;

import org.eclipse.cdt.internal.core.parser.ParserMessages;

/**
 * @author jcamelon
 * 
 * Description of a C/C++ parse/compilation problem, as detected by the parser or some of the underlying
 * clients of the parser. 
 * 
 * A problem provides access to:
 * <ul>
 * <li> its location (originating source file name, source position, line number), </li>
 * <li> its message description and a predicate to check its severity (warning or error). </li>
 * <li> its ID : an number identifying the very nature of this problem. All possible IDs are listed
 * as constants on this interface. </li>
 * </ul>
 */
public interface IASTProblem extends IASTNode {
    /**
     * Returns the problem id
     * 
     * @return the problem id
     */
    int getID();

    /**
     * Answer a localized, human-readable message string which describes the problem.
     * 
     * @return a localized, human-readable message string which describes the problem
     */
    String getMessage();

    /**
     * Return to the client a map between parameter names and values.  
     * 
     * The keys and values are all Strings.  
     * 
     *
     * @return a map between parameter names and values.
     */
    String getArguments();

    /**
     * Checks the severity to see if the Error bit is set.
     * 
     * @return true if the Error bit is set for the severity, false otherwise
     */
    boolean isError();

    /**
     * Checks the severity to see if the Warning bit is not set.
     * 
     * @return true if the Warning bit is not set for the severity, false otherwise
     */
    boolean isWarning();

    /**
     * Unknown Numeric Value for line numbers and offsets; use this constant
     */
    public final static int INT_VALUE_NOT_PROVIDED = -1;
    
    /**
     * Unknown filename sentinel value
     */
    public final static String FILENAME_NOT_PROVIDED = ParserMessages.getString("IProblem.unknownFileName"); //$NON-NLS-1$

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
     * IProblem relates to a valid semantical error in the parser
     */
    public final static int SEMANTICS_RELATED = 0x08000000;
    
    /**
     * IProblem relates to an implementation of design limitation
     */
    public final static int INTERNAL_RELATED = 0x10000000;


    /**
     * Check the parameter bitmask against an IProblem's ID to broadly segregate the 
     * types of problems.  
     * 
     * @param bitmask
     * @return true if ( (id & bitmask ) != 0 )
     */
    public boolean checkCategory(int bitmask);

    /**
     * Mask to use in order to filter out the category portion of the problem ID.
     */
    public final static int IGNORE_CATEGORIES_MASK = 0xFFFFFF;

    /**
     * Below are listed all available problem attributes.  The JavaDoc for each  problem ID indicates
     * when they should be contributed to creating a problem of that type.  
     */

    // Preprocessor IProblem attributes  
    /**
     * The text that follows a #error preprocessor directive 
     */
    public final static String A_PREPROC_POUND_ERROR = ParserMessages.getString("IProblem.preproc.poundError"); //$NON-NLS-1$

    /**
     * The filename that failed somehow in an preprocessor include directive
     */
    public final static String A_PREPROC_INCLUDE_FILENAME = ParserMessages.getString("IProblem.preproc.include"); //$NON-NLS-1$

    /**
     * A preprocessor macro name
     */
    public final static String A_PREPROC_MACRO_NAME = ParserMessages.getString("IProblem.preproc.macro"); //$NON-NLS-1$

    /**
     * A preprocessor conditional that could not be evaluated
     * 
     * #if X + Y == Z       <== that one, if X, Y or Z are not defined 
     * #endif 
     */
    public final static String A_PREPROC_CONDITION = ParserMessages.getString("IProblem.preproc.condition"); //$NON-NLS-1$

    /**
     * A preprocessor directive that could not be interpretted
     * 
     * e.g.  #blah 
     */
    public final static String A_PREPROC_UNKNOWN_DIRECTIVE = ParserMessages.getString("IProblem.preproc.unknownDirective"); //$NON-NLS-1$

    /**
     * The preprocessor conditional statement that caused an unbalanced mismatch.  
     * 
     * #if X 
     * #else
     * #else        <=== that one
     * #endif 
     */
    public final static String A_PREPROC_CONDITIONAL_MISMATCH = ParserMessages.getString("IProblem.preproc.conditionalMismatch"); //$NON-NLS-1$

    /**
     * The Bad character encountered in scanner 
     */
    public static final String A_SCANNER_BADCHAR = null;

    /**
     * A_SYMBOL_NAME  - symbol name 
     */
    public static final String A_SYMBOL_NAME = ParserMessages.getString("IProblem.symbolName"); //$NON-NLS-1$
    
    /**
     * A_NAMESPACE_NAME = namespace name
     */
    public static final String A_NAMESPACE_NAME = ParserMessages.getString("IProblem.namespaceName"); //$NON-NLS-1$
    
    /**
     * A_TYPE_NAME - type name 
     */
    public static final String A_TYPE_NAME = ParserMessages.getString("IProblem.typeName"); //$NON-NLS-1$
    
    /**
     * Below are listed all available problem IDs. Note that this list could be augmented in the future, 
     * as new features are added to the C/C++ core implementation.
     */

    /*
     * Scanner Problems
     */
     
    /** 
     * Bad character encountered by Scanner. 
     * Required attributes: A_SCANNER_BADCHAR
     * @see #A_SCANNER_BADCHAR  
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
     * Bad hexidecimal encountered by Scanner. 
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
    

    /*
     * Preprocessor Problems
     */
     
    /**
     *  #error encountered by Preprocessor.  
     * Required attributes:  A_PREPROC_POUND_ERROR
     * @see #A_PREPROC_POUND_ERROR
     */
    public final static int PREPROCESSOR_POUND_ERROR = PREPROCESSOR_RELATED | 0x001;
    
    /**
     *  Inclusion not found by Preprocessor.  
     * Required attributes: A_PREPROC_INCLUDE_FILENAME
     * @see #A_PREPROC_INCLUDE_FILENAME
     */ 
    public final static int PREPROCESSOR_INCLUSION_NOT_FOUND = PREPROCESSOR_RELATED | 0x002;
    
    /**
     *  Macro definition not found by Preprocessor.  
     * Required attributes:  A_PREPROC_MACRO_NAME
     * @see #A_PREPROC_MACRO_NAME
     */ 
    public final static int PREPROCESSOR_DEFINITION_NOT_FOUND = PREPROCESSOR_RELATED | 0x003;
    
    /**
     *  Preprocessor conditionals seem unbalanced.  
     * Required attributes:  A_PREPROC_CONDITIONAL_MISMATCH
     * @see #A_PREPROC_CONDITIONAL_MISMATCH
     */
    
    public final static int PREPROCESSOR_UNBALANCE_CONDITION = PREPROCESSOR_RELATED | 0x004;
    
    /**
     *  Invalid format to Macro definition.    
     * Required attributes:  A_PREPROC_MACRO_NAME
     * @see #A_PREPROC_MACRO_NAME
     */ 
    public final static int PREPROCESSOR_INVALID_MACRO_DEFN = PREPROCESSOR_RELATED | 0x005;
    
    /**
     *  Invalid or unknown preprocessor directive encountered by Preprocessor.  
     * Required attributes: A_PREPROC_UNKNOWN_DIRECTIVE
     * @see #A_PREPROC_UNKNOWN_DIRECTIVE
     */ 
    public final static int PREPROCESSOR_INVALID_DIRECTIVE = PREPROCESSOR_RELATED | 0x006;
    
    /**
     *  Invalid macro redefinition encountered by Preprocessor.    
     * Required attributes: A_PREPROC_MACRO_NAME
     * @see #A_PREPROC_MACRO_NAME
     */ 
    public final static int PREPROCESSOR_INVALID_MACRO_REDEFN = PREPROCESSOR_RELATED | 0x007;
    
    /**
     *  Preprocessor Conditional cannot not be evaluated due.    
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
    
    /*
     * Parser Syntactic Problems
     */
    public final static int SYNTAX_ERROR = SYNTAX_RELATED | 0x001;

    /*
     * Parser Semantic Problems
     */
    
    /**
     * Attempt to add a unique symbol, yet the value was already defined.
     * Require attributes: A_SYMBOL_NAME
     * @see #A_SYMBOL_NAME  
     */
    public final static int SEMANTIC_UNIQUE_NAME_PREDEFINED = SEMANTICS_RELATED | 0x001;
    
    /**
     * Attempt to use a symbol that was not found. 
     * Require attributes: A_SYMBOL_NAME
     * @see #A_SYMBOL_NAME  
     */ 
    public final static int SEMANTIC_NAME_NOT_FOUND = SEMANTICS_RELATED | 0x002;

    /**
     * Name not provided in context that it was required.   
     * Require attributes: none
     */
    public final static int SEMANTIC_NAME_NOT_PROVIDED = SEMANTICS_RELATED | 0x003;

    /**
     * Invalid overload of a particular name.
     * Required attributes: A_SYMBOL_NAME
     * @see #A_SYMBOL_NAME  
     */
    public static final int SEMANTIC_INVALID_OVERLOAD = SEMANTICS_RELATED | 0x004;

    /**
     * Invalid using directive.  
     * Required attributes: A_NAMESPACE_NAME
     * @see #A_NAMESPACE_NAME
     */
    public static final int SEMANTIC_INVALID_USING = SEMANTICS_RELATED | 0x005;
    
    /**
     * Ambiguous lookup for given name. 
     * Required attributes: A_SYMBOL_NAME
     * @see #A_SYMBOL_NAME
     */
    public static final int SEMANTIC_AMBIGUOUS_LOOKUP = SEMANTICS_RELATED | 0x006;

    /**
     * Invalid type provided
     * Required attribugtes: A_TYPE_NAME
     * @see #A_TYPE_NAME
     */
    public static final int SEMANTIC_INVALID_TYPE = SEMANTICS_RELATED | 0x007;

    public static final int SEMANTIC_CIRCULAR_INHERITANCE = SEMANTICS_RELATED | 0x008;

    public static final int SEMANTIC_INVALID_TEMPLATE = SEMANTICS_RELATED | 0x009;

    public static final int SEMANTIC_BAD_VISIBILITY = SEMANTICS_RELATED | 0x00A;

    public static final int SEMANTIC_UNABLE_TO_RESOLVE_FUNCTION = SEMANTICS_RELATED | 0x00B;

    public static final int SEMANTIC_INVALID_TEMPLATE_ARGUMENT = SEMANTICS_RELATED | 0x00C;

    public static final int SEMANTIC_INVALID_TEMPLATE_PARAMETER = SEMANTICS_RELATED | 0x00D;

    public static final int SEMANTIC_REDECLARED_TEMPLATE_PARAMETER = SEMANTICS_RELATED | 0x00E;

    public static final int SEMANTIC_INVALID_CONVERSION_TYPE = SEMANTICS_RELATED | 0x00F;

    public static final int SEMANTIC_MALFORMED_EXPRESSION = SEMANTICS_RELATED | 0x010;

    public static final int SEMANTIC_ILLFORMED_FRIEND = SEMANTICS_RELATED | 0x011;
    
    public static final int SEMANTIC_RECURSIVE_TEMPLATE_INSTANTIATION = SEMANTICS_RELATED | 0x012;
}
