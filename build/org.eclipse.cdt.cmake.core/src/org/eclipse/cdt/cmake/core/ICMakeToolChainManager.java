/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.cmake.core;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

import org.eclipse.cdt.core.build.IToolChain;

/**
 * Manages toolchain files for CMake.
 *
 * @noimplement
 * @noextend
 */
public interface ICMakeToolChainManager {

	ICMakeToolChainFile newToolChainFile(Path path);

	void addToolChainFile(ICMakeToolChainFile file);

	void removeToolChainFile(ICMakeToolChainFile file);

	ICMakeToolChainFile getToolChainFile(Path path);

	Collection<ICMakeToolChainFile> getToolChainFilesMatching(Map<String, String> properties);

	ICMakeToolChainFile getToolChainFileFor(IToolChain toolchain);

	Collection<ICMakeToolChainFile> getToolChainFiles();

	void addListener(ICMakeToolChainListener listener);

	void removeListener(ICMakeToolChainListener listener);

}
