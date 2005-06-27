/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
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
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ExtensibleSymbol implements Cloneable, IExtensibleSymbol {
	public ExtensibleSymbol( ParserSymbolTable table ){
		_table = table; 
	}
	
	public ExtensibleSymbol( ParserSymbolTable table, ISymbolASTExtension obj ){
		_table = table; 
		_object = obj;
	}	
	
	
	public Object clone(){
		ExtensibleSymbol copy = null;
		try{
			copy = (ExtensibleSymbol)super.clone();
		} catch ( CloneNotSupportedException e ){
			//should not happen
			return null;
		}
	
		return copy;	
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IExtensibleSymbol#getSymbolTable()
	 */
	public ParserSymbolTable getSymbolTable(){
		return _table;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IExtensibleSymbol#getASTExtension()
	 */
	public ISymbolASTExtension getASTExtension() {
		return _object;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IExtensibleSymbol#setASTExtension(org.eclipse.cdt.internal.core.parser.pst.ISymbolASTExtension)
	 */
	public void setASTExtension( ISymbolASTExtension obj ) {
		_object = obj;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IExtensibleSymbol#getContainingSymbol()
	 */
	public IContainerSymbol getContainingSymbol() {
		return _containingScope;
	}
	
	public void setContainingSymbol( IContainerSymbol scope ){ 
		_containingScope = scope;
	}
	
	private final ParserSymbolTable _table;
	private		ISymbolASTExtension	_object;				//the object associated with us
	private		IContainerSymbol	_containingScope;		//the scope that contains us
}
