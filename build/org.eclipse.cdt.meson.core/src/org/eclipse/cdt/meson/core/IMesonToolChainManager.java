/*******************************************************************************
 * Copyright (c) 2016, 2018 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Red Hat Inc. - modified for use with Meson builder
 *******************************************************************************/
package org.eclipse.cdt.meson.core;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

import org.eclipse.cdt.core.build.IToolChain;

/**
 * Manages toolchain files for Meson.
 *
 * @noimplement
 * @noextend
 */
public interface IMesonToolChainManager {

	IMesonToolChainFile newToolChainFile(Path path);

	void addToolChainFile(IMesonToolChainFile file);

	void removeToolChainFile(IMesonToolChainFile file);

	IMesonToolChainFile getToolChainFile(Path path);

	Collection<IMesonToolChainFile> getToolChainFilesMatching(Map<String, String> properties);

	IMesonToolChainFile getToolChainFileFor(IToolChain toolchain);

	Collection<IMesonToolChainFile> getToolChainFiles();

	void addListener(IMesonToolChainListener listener);

	void removeListener(IMesonToolChainListener listener);

}
