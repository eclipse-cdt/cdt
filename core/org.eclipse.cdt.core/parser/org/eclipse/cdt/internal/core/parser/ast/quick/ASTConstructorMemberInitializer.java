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
package org.eclipse.cdt.internal.core.parser.ast.quick;

import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ast.IASTConstructorMemberInitializer;
import org.eclipse.cdt.core.parser.ast.IASTExpression;

/**
 * @author jcamelon
 *
 */
public class ASTConstructorMemberInitializer
    implements IASTConstructorMemberInitializer
{
	/**
     * @param string
     * @param expressionList
     */
    public ASTConstructorMemberInitializer(char[] name, IASTExpression expressionList)
    {
        this.name = name; 
        this.expressionList = expressionList;
    }
    
    private final IASTExpression expressionList; 
	private final char[] name; 
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTConstructorMemberInitializer#getExpressionList()
     */
    public IASTExpression getExpressionList()
    {
        return expressionList;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTConstructorMemberInitializer#getName()
     */
    public String getName()
    {
        return String.valueOf(name);
    }
    public char[] getNameCharArray(){
        return name;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void acceptElement(ISourceElementRequestor requestor)
    {
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#enterScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void enterScope(ISourceElementRequestor requestor)
    {
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#exitScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void exitScope(ISourceElementRequestor requestor)
    {
    }
}
