/*******************************************************************************
 * Copyright (c) 2017 Institute for Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.cdt.cmake.core;

import java.util.Collection;

/**
 * Allows users to provide a custom CMake installation. 
 *
 */
public interface ICMakeInstallationProvider {

	default void init() {
	}
	
	public Collection<ICMakeInstallation> getInstallations();
	
}
