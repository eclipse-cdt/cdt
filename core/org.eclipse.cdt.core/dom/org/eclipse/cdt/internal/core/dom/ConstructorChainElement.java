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
public class ConstructorChainElement {

	private Name name;
	private List expressionList = new ArrayList(); 
	private final ConstructorChain ownerChain; 
	
	ConstructorChainElement( ConstructorChain chain )
	{
		ownerChain = chain; 
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
	public List getExpressionList() {
		return Collections.unmodifiableList( expressionList );
	}

	public void addExpression( ConstructorChainElementExpression expression )
	{
		expressionList.add( expression ); 
	}
	/**
	 * @return ConstructorChain
	 */
	public ConstructorChain getOwnerChain() {
		return ownerChain;
	}

}
