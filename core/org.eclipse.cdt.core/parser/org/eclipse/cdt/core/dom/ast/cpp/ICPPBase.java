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
package org.eclipse.cdt.core.dom.ast.cpp;

/**
 * Represents the relationship between a class and one of its base classes.
 * 
 * @author Doug Schaefer
 */
public interface ICPPBase {

	/**
	 * The base class.
	 * 
	 * @return
	 */
	public ICPPClassType getBaseClass();
	
	/**
	 * The visibility qualifier applied to the base class.
	 * 
	 * @return
	 */
	public int getVisibility();
	
	public static final int v_private = 1;
	public static final int v_protected = 2;
	public static final int v_public = 3;

	/**
	 * Whether this is a virtual base class.
	 * 
	 * @return
	 */
	public boolean isVirtual();

}
