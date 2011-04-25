/*******************************************************************************
 * Copyright (c) 2011 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Miwako Tokugawa (Intel Corporation) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.core;

/**
 * This interface determines whether or not the option category is currently displayed.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * 
 * @since 8.0
 */
public interface IOptionCategoryApplicability {
	/**
	 * This method is queried whenever a new option category is displayed.
	 * 
	 * @param configuration  build configuration of option 
	 *                       (may be IConfiguration or IResourceConfiguration)
	 * @param optHolder		contains the holder of the option
	 * @param category         the option category itself
	 * 
	 * @return true if this option should be visible in the build options page,
	 *         false otherwise
	 */
	public boolean isOptionCategoryVisible(
			IBuildObject configuration, 
            IHoldsOptions optHolder, IOptionCategory category);           
}
