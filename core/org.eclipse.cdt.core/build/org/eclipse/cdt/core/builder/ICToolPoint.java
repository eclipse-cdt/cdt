/**********************************************************************
 * Copyright (c) 2002,2003 Timesys Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Timesys - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.core.builder;

import org.eclipse.cdt.core.builder.model.ICTool;
import org.eclipse.core.runtime.CoreException;

/**
 * Interface representing an instance of
 * a CTool extension point.
 */
public interface ICToolPoint {

	/**
	 * Returns the unique id for the provider.
	 * 
	 * @return unique id.
	 */
	public String getId();

	/**
	 * Returns the name of the provider.
	 * 
	 * @return provider name.
	 */
	public String getName();

	/**
	 * Returns the string identifying the type of tool.
	 * 
	 * @return type string.
	 */
	public String getType();

	/**
	 * Returns the name of the provider's
	 * implementing class.
	 * 
	 * @return name of the provider's implementing class.
	 */
	public String getProviderClassName();

	/**
	 * Returns an instance of the provider's
	 * implementing class.
	 * 
	 * @return instance of ICToolProvider.
	 */
	public ICTool getProvider() throws CoreException;
}
