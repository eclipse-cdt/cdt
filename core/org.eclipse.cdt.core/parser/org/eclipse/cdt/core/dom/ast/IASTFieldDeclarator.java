/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * This represents a field in a struct. This allows for the specification of
 * size for a bit field.
 * 
 * @author Doug Schaefer
 */
public interface IASTFieldDeclarator extends IASTDeclarator {

    ASTNodeProperty FIELD_SIZE = new ASTNodeProperty( "BitField Size"); //$NON-NLS-1$
	/**
	 * This returns the number of bits if this is a bit field.
	 * If it is not a bit field, it returns null.
	 * 
	 * @return size of bit field or null.
	 */
	public IASTExpression getBitFieldSize();
	
	public void setBitFieldSize( IASTExpression size );
	
}
