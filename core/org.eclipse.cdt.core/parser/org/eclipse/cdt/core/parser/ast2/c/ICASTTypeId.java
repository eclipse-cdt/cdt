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
public interface ICASTTypeId extends ICASTNode {

	/**
	 * @return List of ICASTTypeSpecifier
	 */
	public List getTypeSpecifiers();

	/**
	 * This is the abstract-declarator for this type id. Abstract declarators
	 * are declarators without a name. Expect the name field fo the declarator
	 * to be null.
	 * 
	 * @return
	 */
	public ICASTDeclarator getDeclarator();
	
}
