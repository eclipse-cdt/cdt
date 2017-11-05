/*******************************************************************************
 * Copyright (c) 2017 Institute for Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.cdt.cmake.core;

import java.nio.file.Path;

import org.eclipse.core.runtime.Platform;;

/**
 * Provides access to the version and command of a CMake installation.
 * 
 * @noextend
 */
public interface ICMakeInstallation extends Comparable<ICMakeInstallation> {
	
	enum Type {
		SYSTEM,
		CUSTOM,
		EXTENSION,
	}

	static final String COMMAND_EXTENSION = (Platform.getOS().equals(Platform.OS_WIN32) ? ".exe" : "");

	/**
	 * The system dependent name of the "cmake" executable
	 */
	static final String CMAKE_COMMAND = "cmake" + COMMAND_EXTENSION;
	
	/**
	 * The system dependent name of the "cmake-gui" executable
	 */
	static final String CMAKE_GUI_COMMAND = "cmake-gui" + COMMAND_EXTENSION; 
	
	Path getRoot();
	
	Path getCMakeCommand();
	
	Path getCMakeGUICommand();
	
	String getVersion();
	
	Type getType();
}
