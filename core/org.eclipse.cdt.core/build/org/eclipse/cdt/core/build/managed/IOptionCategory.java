/**********************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.core.build.managed;

/**
 * 
 */
public interface IOptionCategory {

	/**
	 * Returns the options that have been assigned to this category.
	 * 
	 * @return
	 */
	public IOption[] getOptions();
	
	/**
	 * Returns the list of children of this node in the option category tree
	 * 
	 * @return
	 */
	public IOptionCategory[] getChildCategories();
	
}
