/*******************************************************************************
 * Copyright (C) 2006, 2010 Siemens AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.core;

import org.eclipse.core.runtime.IPath;

/**
 * An IOptionPathConverter converts between tool-specific paths
 * and their platform locations
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IOptionPathConverter {

	/**
	 * Convert from a tool specific path to a platform location, e.g.
	 * "/usr/include" for a Cygwin tool gets converted to
	 * "c:\\cygwin\\usr\\include"
	 * @param toolSpecificPath The string representation of the tool-specific path
	 * @param option TODO
	 * @param tool TODO
	 * @return A path which is a meaningful platform location
	 * or null, if the conversion fails. 
	 */
	IPath convertToPlatformLocation(String toolSpecificPath, IOption option, ITool tool);
	
}
