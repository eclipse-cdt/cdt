/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * Created on Jan 17, 2005
 */
package org.eclipse.cdt.internal.core.dom.parser;

import java.text.MessageFormat;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPSemantics;
import org.eclipse.cdt.internal.core.parser.ParserMessages;

/**
 * @author aniefer
 */
public class ProblemBinding implements IProblemBinding, IType, IScope {
    private final int id;
    private final char [] arg;
    
    private String message = null;
    
    public ProblemBinding( int id, char [] arg ){
        this.id = id;
        this.arg = arg;
    }
    
    protected final static String PROBLEM_PATTERN = "BaseProblemFactory.problemPattern"; //$NON-NLS-1$
    protected static final String [] errorMessages;
    static {
        errorMessages = new String [ IProblemBinding.LAST_PROBLEM ];
        errorMessages[SEMANTIC_UNIQUE_NAME_PREDEFINED - 1] 		= ParserMessages.getString("ASTProblemFactory.error.semantic.uniqueNamePredefined"); //$NON-NLS-1$ 
        errorMessages[SEMANTIC_NAME_NOT_FOUND - 1] 		 		= ParserMessages.getString("ASTProblemFactory.error.semantic.nameNotFound"); //$NON-NLS-1$
        errorMessages[SEMANTIC_NAME_NOT_PROVIDED - 1]			= ParserMessages.getString("ASTProblemFactory.error.semantic.nameNotProvided"); //$NON-NLS-1$
        errorMessages[SEMANTIC_INVALID_CONVERSION_TYPE - 1] 	= ParserMessages.getString("ASTProblemFactory.error.semantic.invalidConversionType"); //$NON-NLS-1$
        errorMessages[SEMANTIC_MALFORMED_EXPRESSION - 1]		= ParserMessages.getString("ASTProblemFactory.error.semantic.malformedExpression"); //$NON-NLS-1$ 
        errorMessages[SEMANTIC_AMBIGUOUS_LOOKUP - 1]			= ParserMessages.getString("ASTProblemFactory.error.semantic.pst.ambiguousLookup"); //$NON-NLS-1$ 
        errorMessages[SEMANTIC_INVALID_TYPE - 1]				= ParserMessages.getString("ASTProblemFactory.error.semantic.pst.invalidType"); //$NON-NLS-1$ 
        errorMessages[SEMANTIC_CIRCULAR_INHERITANCE - 1]		= ParserMessages.getString("ASTProblemFactory.error.semantic.pst.circularInheritance"); //$NON-NLS-1$
        errorMessages[SEMANTIC_INVALID_OVERLOAD - 1]			= ParserMessages.getString("ASTProblemFactory.error.semantic.pst.invalidOverload"); //$NON-NLS-1$
        errorMessages[SEMANTIC_INVALID_TEMPLATE - 1]			= ParserMessages.getString("ASTProblemFactory.error.semantic.pst.invalidTemplate"); //$NON-NLS-1$ 
        errorMessages[SEMANTIC_INVALID_USING - 1]				= ParserMessages.getString("ASTProblemFactory.error.semantic.pst.invalidUsing"); //$NON-NLS-1$
        errorMessages[SEMANTIC_BAD_VISIBILITY - 1]				= ParserMessages.getString("ASTProblemFactory.error.semantic.pst.badVisibility"); //$NON-NLS-1$
        errorMessages[SEMANTIC_UNABLE_TO_RESOLVE_FUNCTION - 1] 	= ParserMessages.getString("ASTProblemFactory.error.semantic.pst.unableToResolveFunction"); //$NON-NLS-1$
        errorMessages[SEMANTIC_INVALID_TEMPLATE_ARGUMENT - 1]	= ParserMessages.getString("ASTProblemFactory.error.semantic.pst.invalidTemplateArgument"); //$NON-NLS-1$
        errorMessages[SEMANTIC_INVALID_TEMPLATE_PARAMETER - 1]	= ParserMessages.getString("ASTProblemFactory.error.semantic.pst.invalidTemplateParameter"); //$NON-NLS-1$ 
        errorMessages[SEMANTIC_REDECLARED_TEMPLATE_PARAMETER - 1] 	= ParserMessages.getString("ASTProblemFactory.error.semantic.pst.redeclaredTemplateParameter"); //$NON-NLS-1$
        errorMessages[SEMANTIC_RECURSIVE_TEMPLATE_INSTANTIATION - 1]= ParserMessages.getString("ASTProblemFactory.error.semantic.pst.recursiveTemplateInstantiation"); //$NON-NLS-1$ 
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IProblemBinding#getID()
     */
    public int getID() {
        return id;
    }

    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IProblemBinding#getMessage()
     */
    public String getMessage() {
        if (message != null)
            return message;

        String msg = ( id >= 0 && id < LAST_PROBLEM ) ? errorMessages[ id - 1 ] : ""; //$NON-NLS-1$

        if (arg != null) {
            msg = MessageFormat.format(msg, new Object[] { new String(arg) });
        }

        Object[] args = new Object[] { msg, new String("") /*file*/, new Integer(0) /*line*/}; //$NON-NLS-1$        
        message = ParserMessages.getFormattedString(PROBLEM_PATTERN, args);
        return message;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getName()
     */
    public String getName() {
        return CPPSemantics.EMPTY_NAME;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getNameCharArray()
     */
    public char[] getNameCharArray() {
        return CPPSemantics.EMPTY_NAME_ARRAY;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
     */
    public IScope getScope() throws DOMException {
        throw new DOMException( this );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getPhysicalNode()
     */
    public IASTNode getPhysicalNode() throws DOMException {
        throw new DOMException( this );
    }

    public Object clone(){
    	//don't clone problems
        return this;
    }


    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#getParent()
     */
    public IScope getParent() throws DOMException {
        throw new DOMException( this );
    }


    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#find(java.lang.String)
     */
    public IBinding[] find( String name ) throws DOMException {
        throw new DOMException( this );
    }
}
