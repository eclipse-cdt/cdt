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
 * @author Doug Schaefer
 */
public interface IASTFieldReference extends IASTExpression {

	/**
	 * This returns an expression for the object containing the field.
	 * 
	 * @return the field owner
	 */
	public IASTExpression getFieldOwner();
	
	/**
	 * This returns the name of the field being dereferenced.
	 * 
	 * @return the name of the field
	 */
	public IASTName getFieldName();
	
	/**
	 * This returns true of this is the arrow operator and not the
	 * dot operator.
	 * 
	 * @return is this a pointer dereference
	 */
	public boolean isPointerDereference();
	
}
