/*******************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.ast.complete;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ast.IASTCompilationUnit;
import org.eclipse.cdt.core.parser.ast.IASTDeclaration;
import org.eclipse.cdt.internal.core.parser.pst.ISymbol;

/**
 * @author jcamelon
 *
 */
public class ASTCompilationUnit
    extends ASTScope
    implements IASTCompilationUnit
{
	private List declarations = null;
	/**
     * @param symbol
     */
    public ASTCompilationUnit(ISymbol symbol)
    {
        super(symbol);
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
        try
        {
            requestor.enterCompilationUnit( this );
        }
        catch (Exception e)
        {
            /* do nothing */
        }
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#exitScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void exitScope(ISourceElementRequestor requestor)
    {
        try
        {
            requestor.exitCompilationUnit( this );
        }
        catch (Exception e)
        {
            /* do nothing */
        }
    }

    public Iterator getDeclarations()
    {
    	//If a callback (ie StructuralParseCallback) populates the declarations list
    	//then return that iterator, otherwise use the ASTScope implementation which
    	//gets one from the symbol table.
    	if( declarations != null )
    		return declarations.iterator();
    	
    	return super.getDeclarations();
    }
    
    public void addDeclaration(IASTDeclaration declaration)
    {
    	declarations.add(declaration);
    }
    public void initDeclarations()
	{
    	declarations = new ArrayList(0);
	}    
}
