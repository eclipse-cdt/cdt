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
public interface ICASTClassSpecifier extends ICASTTypeSpecifier {

	/**
	 * Is this a class, struct, or union.
	 * 
	 * @return the class-key
	 */
	public int getClassKey();
	public static final int ck_class = 1;
	public static final int ck_struct = 2;
	public static final int ck_union = 3;
	
	/**
	 * @return the name of the class
	 */
	public ICASTName getName();

	/**
	 * @return List of ICASTBaseSpecifier
	 */
	public List getBaseSpecifiers();

	
}
