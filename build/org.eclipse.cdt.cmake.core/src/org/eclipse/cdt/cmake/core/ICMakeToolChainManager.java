/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.cmake.core;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

/**
 * Manages toolchain files for CMake.
 * 
 * @noimplement
 */
public interface ICMakeToolChainManager {

	ICMakeToolChainFile newToolChainFile(Path path);

	void addToolChainFile(ICMakeToolChainFile file);

	void removeToolChainFile(ICMakeToolChainFile file);

	ICMakeToolChainFile getToolChainFile(Path path);

	Collection<ICMakeToolChainFile> getToolChainFilesMatching(Map<String, String> properties);

	Collection<ICMakeToolChainFile> getToolChainFiles();

	void addListener(ICMakeToolChainListener listener);

	void removeListener(ICMakeToolChainListener listener);

}
