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
import org.eclipse.cdt.internal.core.parser.pst.ISymbolASTExtension;
import org.eclipse.cdt.internal.core.parser.pst.ISymbol;

/**
 * @author jcamelon
 *
 */
public class SymbolExtension implements ISymbolASTExtension
{
	private ISymbol symbol;
    private final IASTDeclaration declaration; 
	private IASTDeclaration definition;
	
    /**
     * 
     */
    public SymbolExtension( IASTDeclaration declaration, IASTDeclaration definition )
    {
    	this.declaration = declaration;
    	this.definition = definition;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.pst.IDeclarationExtension#getDeclaration()
     */
    public IASTDeclaration getDeclaration()
    {
        return declaration;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.pst.IDeclarationExtension#getDefinition()
     */
    public IASTDeclaration getDefinition()
    {
        return definition;
    }
 
    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.pst.IDeclarationExtension#setDefinition(org.eclipse.cdt.core.parser.ast.IASTDeclaration)
     */
    public void setDefinition(IASTDeclaration definition)
    {
    	this.definition = definition;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.pst.IDeclarationExtension#hasBeenDefined()
     */
    public boolean hasBeenDefined()
    {
        return ( definition != null );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.pst.IDeclarationExtension#getSymbol()
     */
    public ISymbol getSymbol()
    {
        return symbol;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.pst.IDeclarationExtension#setSymbol(org.eclipse.cdt.internal.core.parser.pst.ISymbol)
     */
    public void setSymbol(ISymbol s)
    {
    	symbol = s;
    }
}
