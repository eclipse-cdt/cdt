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
package org.eclipse.cdt.internal.core.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.internal.core.parser.Token;
import org.eclipse.cdt.internal.core.parser.util.Name;

/**
 * @author jcamelon
 *
 */
public class EnumerationWrapper {
	
	private Name name; 
	private List enumerators = new ArrayList();
	private final IParent parent;   
	private final Token key;  

	public EnumerationWrapper( IParent incoming, Token enumKey )
	{
		this.parent= incoming;
		key = enumKey;
	}

	/**
	 * @return Name
	 */
	public Name getName() {
		return name;
	}

	/**
	 * Sets the name.
	 * @param name The name to set
	 */
	public void setName(Name name) {
		this.name = name;
	}
	
	

	/**
	 * @return List
	 */
	public List getEnumerators() {
		return enumerators;
	}

	public void addEnumerator( EnumeratorWrapper in )
	{
		enumerators.add( in );
	}

	/**
	 * @return ICElementWrapper
	 */
	public IParent getParent() {
		return parent;
	}

	/**
	 * @return Token
	 */
	public Token getClassKind() {
		return key;
	}

}
