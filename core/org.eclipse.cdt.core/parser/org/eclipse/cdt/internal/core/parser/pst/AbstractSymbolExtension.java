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
package org.eclipse.cdt.internal.core.parser.pst;

import org.eclipse.cdt.internal.core.parser.ast.complete.ASTNode;
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

	public IExtensibleSymbol getExtensibleSymbol(){
		return symbol;
	}
	
    public ASTNode getPrimaryDeclaration()
    {
        return primaryDeclaration;
    }
}
