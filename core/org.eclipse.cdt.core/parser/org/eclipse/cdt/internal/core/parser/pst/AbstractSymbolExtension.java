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
package org.eclipse.cdt.internal.core.parser.pst;

import org.eclipse.cdt.internal.core.parser.ast.complete.ASTSymbol;

/**
 * @author jcamelon
 *
 */
public abstract class AbstractSymbolExtension implements ISymbolASTExtension
{
    protected final ISymbol symbol;
    protected final ASTSymbol primaryDeclaration;

    /**
     * 
     */
    public AbstractSymbolExtension( ISymbol symbol, ASTSymbol primaryDeclaration )
    {
    	this.symbol = symbol;
    	this.primaryDeclaration = primaryDeclaration;
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbolOwner#getSymbol()
	 */
	public ISymbol getSymbol()
	{
		return symbol;
	}

    public ASTSymbol getPrimaryDeclaration()
    {
        return primaryDeclaration;
    }
}
