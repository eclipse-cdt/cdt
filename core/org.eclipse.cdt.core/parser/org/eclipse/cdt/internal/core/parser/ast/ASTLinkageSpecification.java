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

import java.util.Iterator;

import org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol;
import org.eclipse.cdt.internal.core.parser.pst.ISymbol;

/**
 * @author jcamelon
 *
 */
public class ASTLinkageSpecification implements IASTLinkageSpecification {

	public ASTLinkageSpecification( IContainerSymbol symbol, String linkage )
	{
		this.symbol = symbol;
		symbol.setASTNode( this );
		this.linkage = linkage;  
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
	private final String linkage; 
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTLinkageSpecification#getLinkageString()
	 */
	public String getLinkageString() {
		return linkage;
	} 

}
