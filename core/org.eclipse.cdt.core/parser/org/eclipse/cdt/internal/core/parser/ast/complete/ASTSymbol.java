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

import org.eclipse.cdt.core.parser.ast.IASTDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.internal.core.parser.pst.ISymbol;
import org.eclipse.cdt.internal.core.parser.pst.ISymbolOwner;

/**
 * @author jcamelon
 *
 */
public abstract class ASTSymbol extends ASTSymbolOwner implements ISymbolOwner, IASTDeclaration
{
	
    public ASTSymbol( ISymbol symbol )
    {
        super(symbol);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTScopedElement#getOwnerScope()
     */
    public IASTScope getOwnerScope()
    {
    	if( symbol.getContainingSymbol() != null )
    		return (IASTScope)symbol.getContainingSymbol().getASTExtension().getPrimaryDeclaration();
    	return null;
    }

}
