/*******************************************************************************
 * Copyright (c) 2017 Institute for Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.cdt.cmake.core;

import java.util.List;

/**
 * Provide access to all CMake installations known to CDT.
 * 
 * @noimplement
 * @noextend
 */
public interface ICMakeInstallationManager {

	void add(ICMakeInstallation installation);
	
	void remove(ICMakeInstallation installation);

	List<ICMakeInstallation> getInstallations();
	
	ICMakeInstallation getActive();
	
	void setActive(ICMakeInstallation installation);
	
}
