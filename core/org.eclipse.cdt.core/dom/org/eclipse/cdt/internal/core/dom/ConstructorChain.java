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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author jcamelon
 *
 */
public class ConstructorChain {

	private List chainElements = new ArrayList();  

	/**
	 * @return List
	 */
	public List getChainElements() {
		return Collections.unmodifiableList( chainElements );
	}

	public void addChainElement( ConstructorChainElement chainElement )
	{
		chainElements.add( chainElement );
	}
	
	public ConstructorChain( Declarator declarator )
	{
		this.ownerDeclarator = declarator;
	}
	
	private final Declarator ownerDeclarator; 
	/**
	 * @return Declarator
	 */
	public Declarator getOwnerDeclarator() {
		return ownerDeclarator;
	}

}
