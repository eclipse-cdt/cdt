/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *    Anton Leherbauer (Wind River Systems)
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.internal.core.parser.ParserMessages;

import com.ibm.icu.text.MessageFormat;


/**
 * Models problems, all problems should derive from this class.
 */
public class ASTProblem extends ASTNode implements IASTProblem {
	
    private final int id;
    private final char[] arg;
	private boolean isError= false;

    public ASTProblem(IASTNode parent, ASTNodeProperty property, int id, char[] arg, boolean isError, int startNumber, int endNumber) {
		setParent(parent);
		setPropertyInParent(property);
		setOffset(startNumber);
		setLength(endNumber-startNumber);

		this.isError= isError;
        this.id = id;
        this.arg = arg;
    }

	public ASTProblem(int id, char[] arg, boolean isError) {
        this.id = id;
        this.arg = arg;
        this.isError= isError;
	}

	public ASTProblem copy() {
		ASTProblem problem = new ASTProblem(id, arg == null ? null : arg.clone(), isError);
		problem.setOffsetAndLength(this);
		return problem;
	}
	
	public int getID() {
        return id;
    }

    public boolean isError() {
        return isError;
    }
    
    public boolean isWarning() {
        return !isError;
    }

    public String getMessageWithLocation() {
        String msg= getMessage();

        char[] file= getOriginatingFileName();
        int line= getSourceLineNumber();
        Object[] args = new Object[] { msg, new String(file), new Integer(line) };
        return ParserMessages.getFormattedString("BaseProblemFactory.problemPattern", args); //$NON-NLS-1$
    }

    public static String getMessage(int id, String arg) {
        String msg = errorMessages.get(new Integer(id));
        if (msg == null)
            msg = ""; //$NON-NLS-1$

        if (arg != null) {
            return MessageFormat.format(msg, new Object[] {arg});
        }
        return msg;
    }
    
    public String getMessage() {
    	return getMessage(id, arg == null ? null : new String(arg));
    }

    public boolean checkCategory(int bitmask) {
        return ((id & bitmask) != 0);
    }

    public String[] getArguments() {
        return arg == null ? new String[0] : new String[] {new String(arg)};
    }

    public char[] getArgument() {
    	return arg;
    }
    

    protected static final Map<Integer, String> errorMessages;
    static {
    	errorMessages = new HashMap<Integer, String>();
    	errorMessages.put(new Integer(PREPROCESSOR_POUND_ERROR), 
    			ParserMessages.getString("ScannerProblemFactory.error.preproc.error")); //$NON-NLS-1$
    	errorMessages.put(new Integer(PREPROCESSOR_POUND_WARNING), 
    			ParserMessages.getString("ScannerProblemFactory.error.preproc.warning")); //$NON-NLS-1$
    	errorMessages.put(new Integer(PREPROCESSOR_INCLUSION_NOT_FOUND), 
    			ParserMessages.getString("ScannerProblemFactory.error.preproc.inclusionNotFound")); //$NON-NLS-1$
    	errorMessages.put(new Integer(PREPROCESSOR_DEFINITION_NOT_FOUND),
    			ParserMessages.getString("ScannerProblemFactory.error.preproc.definitionNotFound")); //$NON-NLS-1$
    	errorMessages.put(new Integer(PREPROCESSOR_INVALID_MACRO_DEFN), 
    			ParserMessages.getString("ScannerProblemFactory.error.preproc.invalidMacroDefn")); //$NON-NLS-1$
    	errorMessages.put(new Integer(PREPROCESSOR_INVALID_MACRO_REDEFN),
    			ParserMessages.getString("ScannerProblemFactory.error.preproc.invalidMacroRedefn")); //$NON-NLS-1$
    	errorMessages.put(new Integer(PREPROCESSOR_UNBALANCE_CONDITION), 
    			ParserMessages.getString("ScannerProblemFactory.error.preproc.unbalancedConditional")); //$NON-NLS-1$
    	errorMessages.put(new Integer(PREPROCESSOR_CONDITIONAL_EVAL_ERROR),
    			ParserMessages.getString("ScannerProblemFactory.error.preproc.conditionalEval")); //$NON-NLS-1$
    	errorMessages.put(new Integer(PREPROCESSOR_MACRO_USAGE_ERROR), 
    			ParserMessages.getString("ScannerProblemFactory.error.preproc.macroUsage")); //$NON-NLS-1$
    	errorMessages.put(new Integer(PREPROCESSOR_CIRCULAR_INCLUSION), 
    			ParserMessages.getString("ScannerProblemFactory.error.preproc.circularInclusion")); //$NON-NLS-1$
    	errorMessages.put(new Integer(PREPROCESSOR_INVALID_DIRECTIVE), 
    			ParserMessages.getString("ScannerProblemFactory.error.preproc.invalidDirective")); //$NON-NLS-1$
    	errorMessages.put(new Integer(PREPROCESSOR_MACRO_PASTING_ERROR), 
    			ParserMessages.getString("ScannerProblemFactory.error.preproc.macroPasting")); //$NON-NLS-1$
    	errorMessages.put(new Integer(PREPROCESSOR_MISSING_RPAREN_PARMLIST),
    			ParserMessages.getString("ScannerProblemFactory.error.preproc.missingRParen")); //$NON-NLS-1$       
    	errorMessages.put(new Integer(PREPROCESSOR_INVALID_VA_ARGS), 
    			ParserMessages.getString("ScannerProblemFactory.error.preproc.invalidVaArgs")); //$NON-NLS-1$       
    	errorMessages.put(new Integer(SCANNER_INVALID_ESCAPECHAR), 
    			ParserMessages.getString("ScannerProblemFactory.error.scanner.invalidEscapeChar")); //$NON-NLS-1$
    	errorMessages.put(new Integer(SCANNER_UNBOUNDED_STRING), 
    			ParserMessages.getString("ScannerProblemFactory.error.scanner.unboundedString")); //$NON-NLS-1$
    	errorMessages.put(new Integer(SCANNER_BAD_FLOATING_POINT), 
    			ParserMessages.getString("ScannerProblemFactory.error.scanner.badFloatingPoint")); //$NON-NLS-1$
    	errorMessages.put(new Integer(SCANNER_BAD_BINARY_FORMAT), 
    			ParserMessages.getString("ScannerProblemFactory.error.scanner.badBinaryFormat")); //$NON-NLS-1$
    	errorMessages.put(new Integer(SCANNER_BAD_HEX_FORMAT), 
    			ParserMessages.getString("ScannerProblemFactory.error.scanner.badHexFormat")); //$NON-NLS-1$
    	errorMessages.put(new Integer(SCANNER_BAD_OCTAL_FORMAT), 
    			ParserMessages.getString("ScannerProblemFactory.error.scanner.badOctalFormat")); //$NON-NLS-1$
    	errorMessages.put(new Integer(SCANNER_BAD_DECIMAL_FORMAT), 
    			ParserMessages.getString("ScannerProblemFactory.error.scanner.badDecimalFormat")); //$NON-NLS-1$
    	errorMessages.put(new Integer(SCANNER_ASSIGNMENT_NOT_ALLOWED), 
    			ParserMessages.getString("ScannerProblemFactory.error.scanner.assignmentNotAllowed")); //$NON-NLS-1$        
    	errorMessages.put(new Integer(SCANNER_DIVIDE_BY_ZERO), 
    			ParserMessages.getString("ScannerProblemFactory.error.scanner.divideByZero")); //$NON-NLS-1$
    	errorMessages.put(new Integer(SCANNER_MISSING_R_PAREN), 
    			ParserMessages.getString("ScannerProblemFactory.error.scanner.missingRParen")); //$NON-NLS-1$
    	errorMessages.put(new Integer(SCANNER_EXPRESSION_SYNTAX_ERROR), 
    			ParserMessages.getString("ScannerProblemFactory.error.scanner.expressionSyntaxError")); //$NON-NLS-1$
    	errorMessages.put(new Integer(SCANNER_ILLEGAL_IDENTIFIER), 
    			ParserMessages.getString("ScannerProblemFactory.error.scanner.illegalIdentifier")); //$NON-NLS-1$
    	errorMessages.put(new Integer(SCANNER_BAD_CONDITIONAL_EXPRESSION),
    			ParserMessages.getString("ScannerProblemFactory.error.scanner.badConditionalExpression")); //$NON-NLS-1$        
    	errorMessages.put(new Integer(SCANNER_UNEXPECTED_EOF), 
    			ParserMessages.getString("ScannerProblemFactory.error.scanner.unexpectedEOF")); //$NON-NLS-1$
    	errorMessages.put(new Integer(SCANNER_BAD_CHARACTER), 
    			ParserMessages.getString("ScannerProblemFactory.error.scanner.badCharacter")); //$NON-NLS-1$
    	errorMessages.put(new Integer(SYNTAX_ERROR), 
    			ParserMessages.getString("ParserProblemFactory.error.syntax.syntaxError")); //$NON-NLS-1$
	}

    /*
	 * @see org.eclipse.cdt.core.parser.IProblem#getOriginatingFileName()
	 */
	public char[] getOriginatingFileName() {
		return getContainingFilename().toCharArray();
	}

	/*
	 * @see org.eclipse.cdt.core.parser.IProblem#getSourceEnd()
	 */
	public int getSourceEnd() {
		final IASTFileLocation location= getFileLocation();
		if (location != null) {
			return location.getNodeOffset() + location.getNodeLength() - 1;
		}
		return INT_VALUE_NOT_PROVIDED;
	}

	/*
	 * @see org.eclipse.cdt.core.parser.IProblem#getSourceLineNumber()
	 */
	public int getSourceLineNumber() {
		final IASTFileLocation location= getFileLocation();
		if (location != null) {
			return location.getStartingLineNumber();
		}
		return INT_VALUE_NOT_PROVIDED;
	}

	/*
	 * @see org.eclipse.cdt.core.parser.IProblem#getSourceStart()
	 */
	public int getSourceStart() {
		final IASTFileLocation location= getFileLocation();
		if (location != null) {
			return location.getNodeOffset();
		}
		return INT_VALUE_NOT_PROVIDED;
	}
}
