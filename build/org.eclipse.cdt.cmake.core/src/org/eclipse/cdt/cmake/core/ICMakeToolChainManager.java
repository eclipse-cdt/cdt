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

	/**
	 * Create a new toolchain file object to be added later.
	 *
	 * @param path path where the toolchain file resides.
	 * @return new toolchain objecta
	 */
	ICMakeToolChainFile newToolChainFile(Path path);

	/**
	 * Make the given toolchain file available. Also persists the path
	 * and properties for the file so it's recreated on startup.
	 *
	 * @param file the toolchain file to be added
	 */
	void addToolChainFile(ICMakeToolChainFile file);

	/**
	 * Remove the given toolchain file.
	 *
	 * @param file the toolchain file to be removed
	 */
	void removeToolChainFile(ICMakeToolChainFile file);

	/**
	 * We no longer use Path as a key so trying to remove the use of this method.
	 * Get by toolchain instead.
	 * @deprecated
	 * @return just returns null
	 */
	@Deprecated
	ICMakeToolChainFile getToolChainFile(Path path);

	/**
	 * Return toolchain files that are applicable to toolchains with the given properties.
	 *
	 * @param properties properties to match
	 * @return toolchain files that do
	 */
	Collection<ICMakeToolChainFile> getToolChainFilesMatching(Map<String, String> properties);

	/**
	 * Return the toolchain file for the given toolchain.
	 *
	 * @param toolchain the toolchain
	 * @return the toolchain file for the toolchain
	 */
	ICMakeToolChainFile getToolChainFileFor(IToolChain toolchain);

	/**
	 * Return all available toolchain files.
	 *
	 * @return all toolchain files
	 */
	Collection<ICMakeToolChainFile> getToolChainFiles();

	/**
	 * Add a listener
	 *
	 * @param listener the listener
	 */
	void addListener(ICMakeToolChainListener listener);

	/**
	 * Remove a listener
	 *
	 * @param listener the listener
	 */
	void removeListener(ICMakeToolChainListener listener);

}
