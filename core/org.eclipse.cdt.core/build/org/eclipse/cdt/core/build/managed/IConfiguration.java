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

import org.eclipse.core.resources.IResource;

/**
 * 
 */
public interface IConfiguration extends IBuildObject {

	/**
	 * Returns the target for this configuration.
	 * 
	 * @return
	 */
	public ITarget getTarget();
	
	/**
	 * Returns the resource that owns the target that owns the configuration.
	 * @return
	 */
	public IResource getOwner();
	
	/**
	 * Returns the tools that are used in this configuration.
	 * 
	 * @return
	 */
	public ITool[] getTools();
	
}
