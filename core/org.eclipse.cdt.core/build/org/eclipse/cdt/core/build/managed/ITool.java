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
public interface ITool extends IBuildObject {

	/**
	 * Return the target that defines this tool, if applicable
	 * @return
	 */
	public ITarget getTarget();
	
	/**
	 * Returns the options that may be customized for this tool.
	 */
	public IOption[] getOptions();
	
	/**
	 * Creates a new option for this tool.  Generally, this should only be
	 * done by the extension and project data loaders.
	 * 
	 * @return
	 */
	public IOption createOption();
	
	/**
	 * Options are organized into categories for UI purposes.
	 * These categories are organized into a tree.  This is the root
	 * of that tree.
	 * 
	 * @return
	 */
	public IOptionCategory getTopOptionCategory();
}
