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

import java.util.List;

/**
 * @author Doug Schaefer
 */
public interface IASTCompositeTypeSpecifier extends IASTDeclSpecifier {

    public static final ASTNodeProperty TYPE_NAME = new ASTNodeProperty( "Type Name"); //$NON-NLS-1$
    public static final ASTNodeProperty MEMBER_DECLARATION = new ASTNodeProperty( "Member Declaration"); //$NON-NLS-1$
	/**
	 * Is this a struct or a union or some other type of composite type.
	 * 
	 * @return key for this type
	 */
	public int getKey();
	public static final int k_struct = 1;
	public static final int k_union = 2;
	public static final int k_last = k_union;
    
	
	public void setKey( int key );
	/**
	 * Return the name for this composite type. If this is an anonymous
	 * type, this will return null.
	 * 
	 * @return the name of the type or null
	 */
	public IASTName getName();
	
	public void setName( IASTName name );
	
	/**
	 * Returns a list of member declarations.
	 * 
	 * @return List of IASTDeclaration
	 */
	public List getMembers();
	
	public void addMemberDeclaration( IASTDeclaration declaration );
}
