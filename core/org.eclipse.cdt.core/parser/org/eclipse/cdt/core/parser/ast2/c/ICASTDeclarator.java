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
package org.eclipse.cdt.core.parser.ast2.c;

import java.util.List;

/**
 * @author Doug Schaefer
 */
public interface ICASTDeclarator extends ICASTNode {

	/**
	 * @return the name of the declarator
	 */
	public ICASTName getName();
	
	/**
	 * @return a list of ICASTPointerOperator
	 */
	public List getPointers();
	
	/**
	 * An initializer will either be an ICASTExpression or a
	 * multi-dimensional List of ICASTExpression.
	 * 
	 * @return the initializer for this declarator
	 */
	public Object getInitializer();
	
}
