/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 ******************************************************************************/
/*
 * Created on Apr 21, 2004
 */
package org.eclipse.cdt.internal.core.parser.pst;

import java.util.Iterator;

import org.eclipse.cdt.core.parser.ast.IASTNode;
import org.eclipse.cdt.internal.core.parser.ast.complete.ASTNode;
import org.eclipse.cdt.internal.core.parser.ast.complete.ASTSymbol;

/**
 * @author aniefer
 */
public class ExtensibleSymbolExtension implements ISymbolASTExtension {
    protected final IExtensibleSymbol extensibleSymbol;
    protected final ASTNode primaryDeclaration;
    
	public ExtensibleSymbolExtension( IExtensibleSymbol symbol, IASTNode node ){
		primaryDeclaration = (ASTNode) node;
		extensibleSymbol = symbol;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbolASTExtension#getPrimaryDeclaration()
	 */
	public ASTNode getPrimaryDeclaration() {
		return primaryDeclaration;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbolASTExtension#getAllDefinitions()
	 */
	public Iterator getAllDefinitions() {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbolASTExtension#addDefinition(org.eclipse.cdt.internal.core.parser.ast.complete.ASTSymbol)
	 */
	public void addDefinition(ASTSymbol definition) throws ExtensionException {
		// TODO Auto-generated method stub
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ISymbolOwner#getSymbol()
	 */
	public ISymbol getSymbol() {
		if( extensibleSymbol instanceof ISymbol )
			return (ISymbol) extensibleSymbol;
		else 
			return null;
	}
	
	public IExtensibleSymbol getExtensibleSymbol(){
		return extensibleSymbol;
	}
}
