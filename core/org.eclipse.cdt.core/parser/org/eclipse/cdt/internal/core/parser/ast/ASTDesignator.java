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
package org.eclipse.cdt.internal.core.parser.ast;

import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ast.IASTDesignator;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IReferenceManager;

/**
 * @author jcamelon
 *
 */
public class ASTDesignator implements IASTDesignator
{
    /**
     * @param kind
     * @param constantExpression
     * @param string
     */
    public ASTDesignator(DesignatorKind kind, IASTExpression constantExpression, char[] fieldName, int fieldOffset )
    {
        this.fieldName = fieldName;
        this.constantExpression = constantExpression;
        this.kind = kind;
        this.fieldOffset = fieldOffset;
    }
    
    private int fieldOffset;
    private final char[] fieldName;
    private final IASTExpression constantExpression;
    private final DesignatorKind kind;
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTDesignator#getKind()
     */
    public DesignatorKind getKind()
    {
        return kind;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTDesignator#arraySubscriptExpression()
     */
    public IASTExpression arraySubscriptExpression()
    {
        return constantExpression;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTDesignator#fieldName()
     */
    public String fieldName()
    {
        return String.valueOf(fieldName);
    }
    public char[] fieldNameCharArray(){
        return fieldName;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void acceptElement(ISourceElementRequestor requestor, IReferenceManager manager)
    {
        if( constantExpression != null )
        	constantExpression.acceptElement(requestor, manager);
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#enterScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void enterScope(ISourceElementRequestor requestor, IReferenceManager manager )
    {
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#exitScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void exitScope(ISourceElementRequestor requestor, IReferenceManager manager )
    {
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTDesignator#fieldOffset()
     */
    public int fieldOffset()
    {
        return fieldOffset;
    }
}
