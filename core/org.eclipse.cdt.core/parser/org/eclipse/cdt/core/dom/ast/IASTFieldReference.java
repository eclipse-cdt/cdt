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

    public static final ASTNodeProperty FIELD_OWNER = new ASTNodeProperty( "Field Owner"); //$NON-NLS-1$
    public static final ASTNodeProperty FIELD_NAME  = new ASTNodeProperty( "Field Name"); //$NON-NLS-1$
    
	/**
	 * This returns an expression for the object containing the field.
	 * 
	 * @return the field owner
	 */
	public IASTExpression getFieldOwner();
	
	public void setFieldOwner( IASTExpression expression );
	
	/**
	 * This returns the name of the field being dereferenced.
	 * 
	 * @return the name of the field
	 */
	public IASTName getFieldName();
	
	public void setFieldName( IASTName name );
	
	/**
	 * This returns true of this is the arrow operator and not the
	 * dot operator.
	 * 
	 * @return is this a pointer dereference
	 */
	public boolean isPointerDereference();
	
	public void setIsPointerDereference( boolean value );
	
}
