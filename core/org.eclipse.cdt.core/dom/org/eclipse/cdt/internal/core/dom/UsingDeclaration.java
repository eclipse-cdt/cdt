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


/**
 * @author jcamelon
 *
 */
public class UsingDeclaration extends Declaration {

	private Name mappedName;
	boolean isTypename = false;
	
	public UsingDeclaration( IScope owner )
	{
		super( owner ); 
	}
	/**
	 * @return String
	 */
	public Name getMappedName() {
		return mappedName;
	}

	/**
	 * Sets the mapping.
	 * @param mapping The mapping to set
	 */
	public void setMappedName(Name mapping) {
		this.mappedName = mapping;
	}

	/**
	 * @return boolean
	 */
	public boolean isTypename() {
		return isTypename;
	}

	/**
	 * Sets the isTypename.
	 * @param isTypename The isTypename to set
	 */
	public void setTypename(boolean isTypename) {
		this.isTypename = isTypename;
	}

}
