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

package org.eclipse.cdt.core.builder.model;

/**
 * ICBuildConfigProvider represents an instance of a class
 * that initializes an empty build configuration with
 * reasonable default values.
 * <p>
 * The intent is to decouple build configuration creation
 * and initialization.
 * <p>
 * See also the <a href="../../../../../../CBuildConfig.html">CBuildConfig</a>
 * extension point documentation.
 */
public interface ICBuildConfigProvider {

	/**
	 * Set initial values in the provided build configuration
	 * working copy.
	 * 
	 * @param config build configuration to initialize.
	 */
	public void setDefaults(ICBuildConfigWorkingCopy config);
}
