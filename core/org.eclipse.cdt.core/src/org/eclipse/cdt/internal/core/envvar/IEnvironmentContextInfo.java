/*******************************************************************************
 * Copyright (c) 2005, 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.envvar;




/**
 * This interface is used by the Environment Variable Provider to
 * represent the given context(level) information
 * 
 * @since 3.0
 */
public interface IEnvironmentContextInfo {
	/**
	 * Returns the next lower-precedence context
	 * 
	 * @return IContextInfo
	 */
	public IEnvironmentContextInfo getNext();

	/**
	 * Returns the array of suppliers to be used for this context
	 * 
	 * @return IEnvironmentVariableSupplier[]
	 */
	public ICoreEnvironmentVariableSupplier[] getSuppliers();
	
	/**
	 * Returns the current context
	 * 
	 * @return Object
	 */
	public Object getContext();
}
