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

import org.eclipse.cdt.internal.core.parser.util.Name;

/**
 * @author jcamelon
 *
 */
public class EnumeratorWrapper {

	private final EnumerationWrapper parent;
	private Name name;  
	
	EnumeratorWrapper( EnumerationWrapper myParent )
	{
		this.parent = myParent;
	}

	/**
	 * @return Name
	 */
	public Name getName() {
		return name;
	}

	/**
	 * @return EnumerationWrapper
	 */
	public EnumerationWrapper getParent() {
		return parent;
	}

	/**
	 * Sets the name.
	 * @param name The name to set
	 */
	public void setName(Name name) {
		this.name = name;
	}

}
