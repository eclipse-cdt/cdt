/*******************************************************************************
 * Copyright (c) 2005, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.macros;

import org.eclipse.core.runtime.IPath;

/**
 * This interface is used to represent file context data
 *  
 * @since 3.0
 * @noextend This class is not intended to be subclassed by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IFileContextData {
	
	/**
	 * Returns the input file location
	 * @return IPath
	 */
	public IPath getInputFileLocation();
	
	/**
	 * Returns the output file location
	 * @return IPath
	 */
	public IPath getOutputFileLocation();
	
	/**
	 * Returns the option context data
	 * 
	 * @return IOptionContextData
	 */
	public IOptionContextData getOptionContextData();
}
