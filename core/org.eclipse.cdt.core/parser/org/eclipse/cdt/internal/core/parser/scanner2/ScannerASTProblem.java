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
package org.eclipse.cdt.internal.core.parser.scanner2;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.parser.ParserMessages;

/**
 * @author jcamelon
 */
public class ScannerASTProblem extends ASTNode implements IASTProblem {
    private IASTNode parent;

    private ASTNodeProperty property;

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.dom.ast.IASTNode#getParent()
     */
    public IASTNode getParent() {
        return parent;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.dom.ast.IASTNode#setParent(org.eclipse.cdt.core.dom.ast.IASTNode)
     */
    public void setParent(IASTNode node) {
        this.parent = node;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.dom.ast.IASTNode#getPropertyInParent()
     */
    public ASTNodeProperty getPropertyInParent() {
        return property;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.dom.ast.IASTNode#setPropertyInParent(org.eclipse.cdt.core.dom.ast.ASTNodeProperty)
     */
    public void setPropertyInParent(ASTNodeProperty property) {
        this.property = property;
    }

    private final char[] arg;

    private final int id;

    private final boolean isError;

    private final boolean isWarning;

    private String message = null;

    public ScannerASTProblem(int id, char[] arg, boolean warn, boolean error) {
        this.id = id;
        this.arg = arg;
        this.isWarning = warn;
        this.isError = error;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.parser.IASTProblem#getID()
     */
    public int getID() {
        return id;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.parser.IASTProblem#isError()
     */
    public boolean isError() {
        return isError;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.parser.IASTProblem#isWarning()
     */
    public boolean isWarning() {
        return isWarning;
    }

    protected static final Map errorMessages;
    static {
        errorMessages = new HashMap();
        errorMessages
                .put(
                        new Integer(IASTProblem.SEMANTIC_UNIQUE_NAME_PREDEFINED),
                        ParserMessages
                                .getString("ASTProblemFactory.error.semantic.uniqueNamePredefined")); //$NON-NLS-1$
        errorMessages
                .put(
                        new Integer(IASTProblem.SEMANTIC_NAME_NOT_FOUND),
                        ParserMessages
                                .getString("ASTProblemFactory.error.semantic.nameNotFound")); //$NON-NLS-1$
        errorMessages
                .put(
                        new Integer(IASTProblem.SEMANTIC_NAME_NOT_PROVIDED),
                        ParserMessages
                                .getString("ASTProblemFactory.error.semantic.nameNotProvided")); //$NON-NLS-1$
        errorMessages
                .put(
                        new Integer(IASTProblem.SEMANTIC_INVALID_CONVERSION_TYPE),
                        ParserMessages
                                .getString("ASTProblemFactory.error.semantic.invalidConversionType")); //$NON-NLS-1$
        errorMessages
                .put(
                        new Integer(IASTProblem.SEMANTIC_MALFORMED_EXPRESSION),
                        ParserMessages
                                .getString("ASTProblemFactory.error.semantic.malformedExpression")); //$NON-NLS-1$        
        errorMessages
                .put(
                        new Integer(IASTProblem.SEMANTIC_AMBIGUOUS_LOOKUP),
                        ParserMessages
                                .getString("ASTProblemFactory.error.semantic.pst.ambiguousLookup")); //$NON-NLS-1$
        errorMessages
                .put(
                        new Integer(IASTProblem.SEMANTIC_INVALID_TYPE),
                        ParserMessages
                                .getString("ASTProblemFactory.error.semantic.pst.invalidType")); //$NON-NLS-1$
        errorMessages
                .put(
                        new Integer(IASTProblem.SEMANTIC_CIRCULAR_INHERITANCE),
                        ParserMessages
                                .getString("ASTProblemFactory.error.semantic.pst.circularInheritance")); //$NON-NLS-1$
        errorMessages
                .put(
                        new Integer(IASTProblem.SEMANTIC_INVALID_OVERLOAD),
                        ParserMessages
                                .getString("ASTProblemFactory.error.semantic.pst.invalidOverload")); //$NON-NLS-1$
        errorMessages
                .put(
                        new Integer(IASTProblem.SEMANTIC_INVALID_TEMPLATE),
                        ParserMessages
                                .getString("ASTProblemFactory.error.semantic.pst.invalidTemplate")); //$NON-NLS-1$
        errorMessages
                .put(
                        new Integer(IASTProblem.SEMANTIC_INVALID_USING),
                        ParserMessages
                                .getString("ASTProblemFactory.error.semantic.pst.invalidUsing")); //$NON-NLS-1$
        errorMessages
                .put(
                        new Integer(IASTProblem.SEMANTIC_BAD_VISIBILITY),
                        ParserMessages
                                .getString("ASTProblemFactory.error.semantic.pst.badVisibility")); //$NON-NLS-1$
        errorMessages
                .put(
                        new Integer(
                                IASTProblem.SEMANTIC_UNABLE_TO_RESOLVE_FUNCTION),
                        ParserMessages
                                .getString("ASTProblemFactory.error.semantic.pst.unableToResolveFunction")); //$NON-NLS-1$
        errorMessages
                .put(
                        new Integer(IASTProblem.SEMANTIC_INVALID_TEMPLATE_ARGUMENT),
                        ParserMessages
                                .getString("ASTProblemFactory.error.semantic.pst.invalidTemplateArgument")); //$NON-NLS-1$
        errorMessages
                .put(
                        new Integer(
                                IASTProblem.SEMANTIC_INVALID_TEMPLATE_PARAMETER),
                        ParserMessages
                                .getString("ASTProblemFactory.error.semantic.pst.invalidTemplateParameter")); //$NON-NLS-1$
        errorMessages
                .put(
                        new Integer(
                                IASTProblem.SEMANTIC_REDECLARED_TEMPLATE_PARAMETER),
                        ParserMessages
                                .getString("ASTProblemFactory.error.semantic.pst.redeclaredTemplateParameter")); //$NON-NLS-1$
        errorMessages
                .put(
                        new Integer(
                                IASTProblem.SEMANTIC_RECURSIVE_TEMPLATE_INSTANTIATION),
                        ParserMessages
                                .getString("ASTProblemFactory.error.semantic.pst.recursiveTemplateInstantiation")); //$NON-NLS-1$
        errorMessages
                .put(
                        new Integer(IASTProblem.PREPROCESSOR_POUND_ERROR),
                        ParserMessages
                                .getString("ScannerProblemFactory.error.preproc.error")); //$NON-NLS-1$
        errorMessages
                .put(
                        new Integer(IASTProblem.PREPROCESSOR_INCLUSION_NOT_FOUND),
                        ParserMessages
                                .getString("ScannerProblemFactory.error.preproc.inclusionNotFound")); //$NON-NLS-1$
        errorMessages
                .put(
                        new Integer(IASTProblem.PREPROCESSOR_DEFINITION_NOT_FOUND),
                        ParserMessages
                                .getString("ScannerProblemFactory.error.preproc.definitionNotFound")); //$NON-NLS-1$
        errorMessages
                .put(
                        new Integer(IASTProblem.PREPROCESSOR_INVALID_MACRO_DEFN),
                        ParserMessages
                                .getString("ScannerProblemFactory.error.preproc.invalidMacroDefn")); //$NON-NLS-1$
        errorMessages
                .put(
                        new Integer(IASTProblem.PREPROCESSOR_INVALID_MACRO_REDEFN),
                        ParserMessages
                                .getString("ScannerProblemFactory.error.preproc.invalidMacroRedefn")); //$NON-NLS-1$
        errorMessages
                .put(
                        new Integer(IASTProblem.PREPROCESSOR_UNBALANCE_CONDITION),
                        ParserMessages
                                .getString("ScannerProblemFactory.error.preproc.unbalancedConditional")); //$NON-NLS-1$
        errorMessages
                .put(
                        new Integer(
                                IASTProblem.PREPROCESSOR_CONDITIONAL_EVAL_ERROR),
                        ParserMessages
                                .getString("ScannerProblemFactory.error.preproc.conditionalEval")); //$NON-NLS-1$
        errorMessages
                .put(
                        new Integer(IASTProblem.PREPROCESSOR_MACRO_USAGE_ERROR),
                        ParserMessages
                                .getString("ScannerProblemFactory.error.preproc.macroUsage")); //$NON-NLS-1$
        errorMessages
                .put(
                        new Integer(IASTProblem.PREPROCESSOR_CIRCULAR_INCLUSION),
                        ParserMessages
                                .getString("ScannerProblemFactory.error.preproc.circularInclusion")); //$NON-NLS-1$
        errorMessages
                .put(
                        new Integer(IASTProblem.PREPROCESSOR_INVALID_DIRECTIVE),
                        ParserMessages
                                .getString("ScannerProblemFactory.error.preproc.invalidDirective")); //$NON-NLS-1$
        errorMessages
                .put(
                        new Integer(IASTProblem.PREPROCESSOR_MACRO_PASTING_ERROR),
                        ParserMessages
                                .getString("ScannerProblemFactory.error.preproc.macroPasting")); //$NON-NLS-1$
        errorMessages
                .put(
                        new Integer(
                                IASTProblem.PREPROCESSOR_MISSING_RPAREN_PARMLIST),
                        ParserMessages
                                .getString("ScannerProblemFactory.error.preproc.missingRParen")); //$NON-NLS-1$       
        errorMessages
                .put(
                        new Integer(IASTProblem.PREPROCESSOR_INVALID_VA_ARGS),
                        ParserMessages
                                .getString("ScannerProblemFactory.error.preproc.invalidVaArgs")); //$NON-NLS-1$       
        errorMessages
                .put(
                        new Integer(IASTProblem.SCANNER_INVALID_ESCAPECHAR),
                        ParserMessages
                                .getString("ScannerProblemFactory.error.scanner.invalidEscapeChar")); //$NON-NLS-1$
        errorMessages
                .put(
                        new Integer(IASTProblem.SCANNER_UNBOUNDED_STRING),
                        ParserMessages
                                .getString("ScannerProblemFactory.error.scanner.unboundedString")); //$NON-NLS-1$
        errorMessages
                .put(
                        new Integer(IASTProblem.SCANNER_BAD_FLOATING_POINT),
                        ParserMessages
                                .getString("ScannerProblemFactory.error.scanner.badFloatingPoint")); //$NON-NLS-1$
        errorMessages
                .put(
                        new Integer(IASTProblem.SCANNER_BAD_HEX_FORMAT),
                        ParserMessages
                                .getString("ScannerProblemFactory.error.scanner.badHexFormat")); //$NON-NLS-1$
        errorMessages
                .put(
                        new Integer(IASTProblem.SCANNER_BAD_OCTAL_FORMAT),
                        ParserMessages
                                .getString("ScannerProblemFactory.error.scanner.badOctalFormat")); //$NON-NLS-1$
        errorMessages
                .put(
                        new Integer(IASTProblem.SCANNER_BAD_DECIMAL_FORMAT),
                        ParserMessages
                                .getString("ScannerProblemFactory.error.scanner.badDecimalFormat")); //$NON-NLS-1$
        errorMessages
                .put(
                        new Integer(IASTProblem.SCANNER_ASSIGNMENT_NOT_ALLOWED),
                        ParserMessages
                                .getString("ScannerProblemFactory.error.scanner.assignmentNotAllowed")); //$NON-NLS-1$        
        errorMessages
                .put(
                        new Integer(IASTProblem.SCANNER_DIVIDE_BY_ZERO),
                        ParserMessages
                                .getString("ScannerProblemFactory.error.scanner.divideByZero")); //$NON-NLS-1$
        errorMessages
                .put(
                        new Integer(IASTProblem.SCANNER_MISSING_R_PAREN),
                        ParserMessages
                                .getString("ScannerProblemFactory.error.scanner.missingRParen")); //$NON-NLS-1$
        errorMessages
                .put(
                        new Integer(IASTProblem.SCANNER_EXPRESSION_SYNTAX_ERROR),
                        ParserMessages
                                .getString("ScannerProblemFactory.error.scanner.expressionSyntaxError")); //$NON-NLS-1$
        errorMessages
                .put(
                        new Integer(IASTProblem.SCANNER_ILLEGAL_IDENTIFIER),
                        ParserMessages
                                .getString("ScannerProblemFactory.error.scanner.illegalIdentifier")); //$NON-NLS-1$
        errorMessages
                .put(
                        new Integer(IASTProblem.SCANNER_BAD_CONDITIONAL_EXPRESSION),
                        ParserMessages
                                .getString("ScannerProblemFactory.error.scanner.badConditionalExpression")); //$NON-NLS-1$        
        errorMessages
                .put(
                        new Integer(IASTProblem.SCANNER_UNEXPECTED_EOF),
                        ParserMessages
                                .getString("ScannerProblemFactory.error.scanner.unexpectedEOF")); //$NON-NLS-1$
        errorMessages
                .put(
                        new Integer(IASTProblem.SCANNER_BAD_CHARACTER),
                        ParserMessages
                                .getString("ScannerProblemFactory.error.scanner.badCharacter")); //$NON-NLS-1$
        errorMessages.put(new Integer(IASTProblem.SYNTAX_ERROR), ParserMessages
                .getString("ParserProblemFactory.error.syntax.syntaxError")); //$NON-NLS-1$
    }

    protected final static String PROBLEM_PATTERN = "BaseProblemFactory.problemPattern"; //$NON-NLS-1$

    public String getMessage() {
        if (message != null)
            return message;

        String msg = (String) errorMessages.get(new Integer(id));
        if (msg == null)
            msg = ""; //$NON-NLS-1$

        if (arg != null) {
            msg = MessageFormat.format(msg, new Object[] { new String(arg) });
        }

        Object[] args = new Object[] { msg, new String(""), new Integer(0) }; //$NON-NLS-1$        
        message = ParserMessages.getFormattedString(PROBLEM_PATTERN, args);
        return message;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.parser.IASTProblem#checkCategory(int)
     */
    public boolean checkCategory(int bitmask) {
        return ((id & bitmask) != 0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.parser.IASTProblem#getArguments()
     */
    public String getArguments() {
        return arg != null ? String.valueOf(arg) : ""; //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.dom.ast.IASTNode#getTranslationUnit()
     */
    public IASTTranslationUnit getTranslationUnit() {
        if (this instanceof IASTTranslationUnit)
            return (IASTTranslationUnit) this;
        IASTNode node = getParent();
        while (!(node instanceof IASTTranslationUnit) && node != null) {
            node = node.getParent();
        }
        return (IASTTranslationUnit) node;
    }

}
