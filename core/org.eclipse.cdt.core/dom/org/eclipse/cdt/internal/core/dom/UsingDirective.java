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

import org.eclipse.cdt.internal.core.parser.Name;


/**
 * @author jcamelon
 *
 */
public class UsingDirective extends Declaration {

	private Name namespaceName; 
	
	public UsingDirective( IScope owner )
	{
		super( owner );
	}

	/**
	 * @return String
	 */
	public Name getNamespaceName() {
		return namespaceName;
	}

	/**
	 * Sets the namespaceName.
	 * @param namespaceName The namespaceName to set
	 */
	public void setNamespaceName(Name namespaceName) {
		this.namespaceName = namespaceName;
	}
}
