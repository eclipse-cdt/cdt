/**********************************************************************
 * Created on Mar 25, 2003
 *
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.dom;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.internal.core.parser.util.Name;

/**
 * @author jcamelon
 *
 */
public class NamespaceDefinition extends Declaration implements IScope {

	private List declarations = new LinkedList();
	private IScope ownerScope;
	private Name name = null;

	public NamespaceDefinition( IScope owner )
	{
		ownerScope = owner;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.IScope#addDeclaration(org.eclipse.cdt.internal.core.dom.Declaration)
	 */
	public void addDeclaration(Declaration declaration) {
		declarations.add( declaration ); 

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.IScope#getDeclarations()
	 */
	public List getDeclarations() {
		return declarations; 
	}


	/**
	 * @return String
	 */
	public Name getName() {
		return name;
	}

	/**
	 * @return IScope
	 */
	public IScope getOwnerScope() {
		return ownerScope;
	}

	/**
	 * Sets the name.
	 * @param name The name to set
	 */
	public void setName(Name name) {
		this.name = name;
	}

}
