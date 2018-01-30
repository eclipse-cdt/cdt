/*******************************************************************************
 * Copyright (c) 2016, 2018 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors
 * 		Red Hat Inc. - modified for use in Meson build
 *******************************************************************************/
package org.eclipse.cdt.internal.meson.core;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.cdt.meson.core.Activator;
import org.eclipse.cdt.meson.core.IMesonToolChainFile;
import org.eclipse.core.runtime.CoreException;

public class MesonToolChainFile implements IMesonToolChainFile {

	String n;
	private final Path path;
	private IToolChain toolchain;

	final Map<String, String> properties = new HashMap<>();

	public MesonToolChainFile(String n, Path path) {
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
			toolchain = tcManager.getToolChain(properties.get(MesonBuildConfiguration.TOOLCHAIN_TYPE),
					properties.get(MesonBuildConfiguration.TOOLCHAIN_ID));

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
