/**********************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Intel Corporation - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.managedbuilder.macros;

import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IOption;

/**
 * This interface is used to represent an option context data
 *  
 * @since 3.0
 */
public interface IOptionContextData {
	/**
	 * Returns an option
	 * 
	 * @return IOption
	 */
	public IOption getOption();
	
	/**
	 * Returns IBuildObject that could be either an IToolChain or an IResourceConfiguration reference
	 * 
	 * @return IBuildObject
	 */
	public IBuildObject getParent();
}
