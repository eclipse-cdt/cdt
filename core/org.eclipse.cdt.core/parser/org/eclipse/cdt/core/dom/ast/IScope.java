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
 * 
 * @author Doug Schaefer
 */
public interface IScope {

	/**
     * Get the IASTName for this scope, may be null 
     * @return
     * @throws DOMException
     */
    public IASTName getScopeName() throws DOMException;
    
	/**
	 * Scopes are arranged hierarchically. Lookups will generally
	 * flow upward to find resolution.
	 * 
	 * @return
	 */
	public IScope getParent() throws DOMException;

	/**
	 * This is the general lookup entry point. It returns the list of
	 * valid bindings for a given name.  The lookup proceeds as an unqualified
	 * lookup.  Constructors are not considered during this lookup and won't be returned.
	 * No attempt is made to resolve potential ambiguities or perform access checking.
	 * 
	 * @param searchString
	 * @return List of IBinding
	 */
	public IBinding[] find(String name) throws DOMException;

    /**
     * Return the physical IASTNode that this scope was created for
     * @return
     */
    public IASTNode getPhysicalNode() throws DOMException;
}
