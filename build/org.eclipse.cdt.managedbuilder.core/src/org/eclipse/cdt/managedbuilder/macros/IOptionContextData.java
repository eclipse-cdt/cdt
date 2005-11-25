/*******************************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
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
	 * Returns IBuildObject that represents the option holder. 
	 * For the backward compatibility MBS will also support the cases 
	 * when this method returns either an IToolChain or IResourceConfiguration. 
	 * In this case MBS will try to obtain the option holder automatically, 
	 * but it might fail in case the tool-chain/resource configuration contains 
	 * more than one tools with the same super-class
	 * 
	 * @return IBuildObject
	 */
	public IBuildObject getParent();
}
