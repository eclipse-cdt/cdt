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
package org.eclipse.cdt.internal.core.parser.ast.complete;

import java.util.List;

import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ast.IASTConstructorMemberInitializer;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IReferenceManager;

/**
 * @author jcamelon
 *
 */
public class ASTConstructorMemberInitializer
    implements IASTConstructorMemberInitializer
{
    private final int nameOffset;
	private final boolean requireNameResolution;
	private final char[] name;
    private final IASTExpression expression;
    private List references;
    /**
     * 
     */
    public ASTConstructorMemberInitializer( IASTExpression expression, char[] name, int nameOffset, List references, boolean requireNameResolution )
    {
    	this.expression = expression;
    	this.name = name;
    	this.nameOffset = nameOffset; 
    	this.references = references;
    	this.requireNameResolution = requireNameResolution;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTConstructorMemberInitializer#getExpressionList()
     */
    public IASTExpression getExpressionList()
    {
        return expression;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTConstructorMemberInitializer#getName()
     */
    public String getName()
    {
        return String.valueOf( name );
    }
    public char[] getNameArray(){
        return name;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void acceptElement(ISourceElementRequestor requestor, IReferenceManager manager)
    {
    	manager.processReferences( references, requestor );
    	references = null;
    	if( expression != null )
    		expression.freeReferences(manager);
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#enterScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void enterScope(ISourceElementRequestor requestor, IReferenceManager manager)
    {
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#exitScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void exitScope(ISourceElementRequestor requestor, IReferenceManager manager)
    {
    }
	/**
	 * @return
	 */
	public boolean requiresNameResolution()
	{
		return requireNameResolution;
	}

	/**
	 * @return
	 */
	public List getReferences()
	{
		return references;
	}
	/**
	 * @return
	 */
	public int getNameOffset()
	{
		return nameOffset;
	}

}
