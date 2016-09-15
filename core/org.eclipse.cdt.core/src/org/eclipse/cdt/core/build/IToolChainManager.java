/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.core.build;

import java.util.Collection;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;

/**
 * The global toolchain manager. Accessed as an OSGi service.
 *
 * @since 6.0
 */
public interface IToolChainManager {

	IToolChainProvider getProvider(String providerId) throws CoreException;
	
	IToolChain getToolChain(String providerId, String id, String version) throws CoreException;
	
	Collection<IToolChain> getToolChains(String providerId) throws CoreException;

	Collection<IToolChain> getToolChains(String providerId, String id) throws CoreException;

	/**
	 * Returns the list of toolchains that have the given properties.
	 * 
	 * @param properties
	 *            properties of the toolchains
	 * @return the qualified toolchains
	 */
	Collection<IToolChain> getToolChainsMatching(Map<String, String> properties) throws CoreException;

	void addToolChain(IToolChain toolChain);
	
	void removeToolChain(IToolChain toolChain);

}
