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
 * A composite type specifier represents a ocmposite structure (contains declarations).   
 * 
 * @author Doug Schaefer
 */
public interface IASTCompositeTypeSpecifier extends IASTDeclSpecifier {

    /**
     * <code>TYPE_NAME</code> represents the relationship between an <code>IASTCompositeTypeSpecifier</code> and its <code>IASTName</code>. 
     */
    public static final ASTNodeProperty TYPE_NAME = new ASTNodeProperty( "Type Name"); //$NON-NLS-1$

	/**
     * <code>MEMBER_DECLARATION</code> represents the relationship between an <code>IASTCompositeTypeSpecifier</code> and its nested<code>IASTDeclaration</code>s. 
     */
    public static final ASTNodeProperty MEMBER_DECLARATION = new ASTNodeProperty( "Member Declaration"); //$NON-NLS-1$

	/**
	 * Get the type (key) of this composite specifier. 
	 * 
	 * @return key for this type
	 */
	public int getKey();
	
	
	/**
	 * <code>k_struct</code> represents 'struct' in C & C++
	 */
	public static final int k_struct = 1;
	/**
	 * <code>k_union</code> represents 'union' in C & C++
	 */
	public static final int k_union = 2;
	/**
	 * <code>k_last</code> allows for subinterfaces to continue enumerating keys
	 */
	public static final int k_last = k_union;
    
	
	/**
	 * Set the type (key) of this composite specifier. 
	 * 
	 * @param key
	 */
	public void setKey( int key );

	/**
	 * Return the name for this composite type. If this is an anonymous
	 * type, this will return an empty name.
	 * 
	 * @return the name of the type
	 */
	public IASTName getName();
	
	/**
	 * Set the name for this composite type.  
	 * 
	 * @param name
	 */
	public void setName( IASTName name );
	
	/**
	 * Returns a list of member declarations.
	 * 
	 * @return List of IASTDeclaration
	 */
	public IASTDeclaration[] getMembers();
	
	/**
	 * Add a member declaration. 
	 * 
	 * @param declaration
	 */
	public void addMemberDeclaration( IASTDeclaration declaration );
	
	/**
	 * Get the scope that this interface eludes to in the logical tree.
	 * 
	 * @return
	 */
	public IScope getScope();
}
