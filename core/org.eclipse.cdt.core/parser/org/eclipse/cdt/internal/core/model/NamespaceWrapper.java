package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.internal.core.parser.Token;
import org.eclipse.cdt.internal.core.parser.util.Name;

/**********************************************************************
 * Created on Apr 1, 2003
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
public class NamespaceWrapper implements ICElementWrapper{
	private Name name; 
	private final IParent parent;
	private ICElement element; 
	private Token firstToken; 
	
	public NamespaceWrapper( IParent incoming, Token namespace)
	{
		this.parent= incoming;
		firstToken = namespace;
	}

	/**
	 * Returns the name.
	 * @return Name
	 */
	public Name getName() {
		return name;
	}

	/**
	 * Returns the parent.
	 * @return IParent
	 */
	public IParent getParent() {
		return parent;
	}

	/**
	 * Sets the name.
	 * @param name The name to set
	 */
	public void setName(Name name) {
		this.name = name;
	}

	/**
	 * @see org.eclipse.cdt.internal.core.model.ICElementWrapper#getElement()
	 */
	public ICElement getElement() {
		return element;
	}

	/**
	 * @see org.eclipse.cdt.internal.core.model.ICElementWrapper#setElement(org.eclipse.cdt.core.model.IParent)
	 */
	public void setElement(ICElement item) {
		element = item;
	}

	/**
	 * @return
	 */
	public Token getFirstToken() {
		return firstToken;
	}


}
