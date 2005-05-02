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
 * This class represents a name in the program that represents a semantic object
 * in the program.
 * 
 * The toString method produces a string representation of the name as
 * appropriate for the language.
 * 
 * @author Doug Schaefer
 */
public interface IASTName extends IASTNode {

	/**
	 * Constant sentinel.
	 */
	public static final IASTName[] EMPTY_NAME_ARRAY = new IASTName[0];

	/**
	 * Resolve the semantic object this name is referring to.
	 * 
	 * @return <code>IBinding</code> binding
	 */
	public IBinding resolveBinding();

	/**
	 * Get the semantic object attached to this name.  May be null if this name
	 * has not yet been semantically resolved (@see resolveBinding)
	 * @return <code>IBinding</code> if it has been resolved, otherwise null 
	 */
	public IBinding getBinding();
	
	/** 
	 * Set the semantic object for this name to be the given binding
	 * @param binding
	 */
	public void setBinding( IBinding binding );
	
	/**
	 * Return a list of bindings in the scope of the name that have the name as
	 * a prefix.
	 * 
	 * @return <code>IBinding []</code> bindings that start with this name
	 */
	public IBinding[] resolvePrefix();

	/**
	 * Return a char array representation of the name.
	 * 
	 * @return ~ toString().toCharArray()
	 */
	public char[] toCharArray();
	
	/**
	 * Is this name being used in the AST as the introduction of a declaration?
	 * @return boolean
	 */
	public boolean isDeclaration();
	
	/**
	 * Is this name being used in the AST as a reference rather than a declaration?
	 * @return boolean
	 */
    
	public boolean isReference();
    
    /**
     * Is this name being used in the AST as a reference rather than a declaration?
     * @return boolean
     */
    public boolean isDefinition();
}
