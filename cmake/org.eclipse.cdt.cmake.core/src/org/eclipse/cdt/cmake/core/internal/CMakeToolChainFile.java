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
package org.eclipse.cdt.cmake.core.internal;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.cmake.core.ICMakeToolChainFile;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.core.runtime.CoreException;

public class CMakeToolChainFile implements ICMakeToolChainFile {

	String n;
	private final Path path;
	private IToolChain toolchain;

	final Map<String, String> properties = new HashMap<>();

	public CMakeToolChainFile(String n, Path path) {
		this.n = n;
		this.path = path;
	}

	@Override
	public Path getPath() {
		return path;
	}

	@Override
	public String getProperty(String key) {
		return properties.get(key);
	}

	@Override
	public void setProperty(String key, String value) {
		properties.put(key, value);
	}

	@Override
	public IToolChain getToolChain() throws CoreException {
		if (toolchain == null) {
			IToolChainManager tcManager = Activator.getService(IToolChainManager.class);
			toolchain = tcManager.getToolChain(properties.get(CMakeBuildConfiguration.TOOLCHAIN_TYPE),
					properties.get(CMakeBuildConfiguration.TOOLCHAIN_ID));

			if (toolchain == null) {
				Collection<IToolChain> tcs = tcManager.getToolChainsMatching(properties);
				if (!tcs.isEmpty()) {
					toolchain = tcs.iterator().next();
				}
			}
		}
		return toolchain;
	}

	boolean matches(Map<String, String> properties) {
		for (Map.Entry<String, String> property : properties.entrySet()) {
			if (!property.getValue().equals(getProperty(property.getKey()))) {
				return false;
			}
		}
		return true;
	}

}
