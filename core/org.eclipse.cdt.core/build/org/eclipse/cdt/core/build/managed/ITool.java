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
public interface ITool {

	/**
	 * Returns the name of the tool.
	 * 
	 * @return
	 */
	public String getName();
	
	/**
	 * Return the target that defines this tool, if applicable
	 * @return
	 */
	public ITarget getTarget();
	
	/**
	 * Returns the tool that this tool inherits properties from.
	 * @return
	 */
	public ITool getParent();
	
	/**
	 * Returns the options that may be customized for this tool.
	 */
	public IOption[] getOptions();
	
	/**
	 * Options are organized into categories for UI purposes.
	 * These categories are organized into a tree.  This is the root
	 * of that tree.
	 * 
	 * @return
	 */
	public IOptionCategory getTopOptionCategory();
}
