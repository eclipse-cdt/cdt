/**********************************************************************
 * Created on Mar 28, 2003
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
public class ConstructorChainElement  {

	private String name;
	private final ConstructorChain ownerChain; 
	
	ConstructorChainElement( ConstructorChain chain )
	{
		ownerChain = chain; 
	}

	/**
	 * @return Name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 * @param name The name to set
	 */
	public void setName(String name) {
		this.name = name;
	}


	/**
	 * @return ConstructorChain
	 */
	public ConstructorChain getOwnerChain() {
		return ownerChain;
	}


}
