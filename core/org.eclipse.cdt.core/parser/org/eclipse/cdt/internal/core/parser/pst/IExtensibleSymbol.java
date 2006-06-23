/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.parser.pst;

/**
 * @author aniefer
 */
public interface IExtensibleSymbol {
	/**
	 * get the instance of ParserSymbolTable with wich this symbol is associated
	 * @return ParserSymbolTable
	 */
	public ParserSymbolTable getSymbolTable();
	
	/**
	 * get the ISymbolASTExtension attached to this symbol
	 * @return ISymbolASTExtension
	 */
	public ISymbolASTExtension getASTExtension();
	
	/**
	 * attach an ISymbolASTExtension to this symbol
	 * @param obj
	 */
	public void setASTExtension( ISymbolASTExtension obj );
	
	public IContainerSymbol getContainingSymbol();
	
	public void setContainingSymbol( IContainerSymbol scope );
}
