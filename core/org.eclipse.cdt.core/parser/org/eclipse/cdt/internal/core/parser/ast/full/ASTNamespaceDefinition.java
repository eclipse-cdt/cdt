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
package org.eclipse.cdt.internal.core.parser.ast.full;

import java.util.Iterator;

import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol;
import org.eclipse.cdt.internal.core.parser.pst.ISymbol;


/**
 * @author jcamelon
 *
 */
public class ASTNamespaceDefinition implements IASTFNamespaceDefinition {

	public ASTNamespaceDefinition( IContainerSymbol symbol, String name )
	{
		this.symbol = symbol;
		this.name = name;
	}

	private final String name; 
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTScope#getDeclarations()
	 */
	public Iterator getDeclarations() {
		return new ScopeIterator( symbol.getContainedSymbols() );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ast.IPSTSymbolExtension#getSymbol()
	 */
	public ISymbol getSymbol() {
		return symbol;
	}

	private final IContainerSymbol symbol;

	private int nameOffset = 0, startingOffset = 0, endingOffset = 0;
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getElementNameOffset()
	 */
	public int getElementNameOffset() {
		return nameOffset;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#setNameOffset(int)
	 */
	public void setElementNameOffset(int o) {
		nameOffset = o;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#setStartingOffset(int)
	 */
	public void setStartingOffset(int o) {
		startingOffset = o;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#setEndingOffset(int)
	 */
	public void setEndingOffset(int o) {
		endingOffset = o;	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getElementStartingOffset()
	 */
	public int getElementStartingOffset() {
		return startingOffset;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getElementEndingOffset()
	 */
	public int getElementEndingOffset() {
		return endingOffset;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ast.IPSTContainerExtension#getContainerSymbol()
	 */
	public IContainerSymbol getContainerSymbol() {
		return symbol;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTDeclaration#getOwnerScope()
	 */
	public IASTScope getOwnerScope() {
		return (IPSTContainerExtension)symbol.getContainingSymbol().getASTNode();
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTQualifiedNameElement#getFullyQualifiedName()
     */
    public String[] getFullyQualifiedName()
    {
        // TODO Auto-generated method stub
        return null;
    } 
}
