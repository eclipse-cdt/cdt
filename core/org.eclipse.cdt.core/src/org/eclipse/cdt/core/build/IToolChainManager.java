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

/**
 * The global toolchain manager. Accessed as an OSGi service.
 *
 * @since 6.0
 */
public interface IToolChainManager {

	IToolChainType getToolChainType(String id);

	IToolChain getToolChain(String typeId, String name);

	/**
	 * Returns the list of toolchains that have the given properties.
	 * 
	 * @param properties
	 *            properties of the toolchains
	 * @return the qualified toolchains
	 */
	Collection<IToolChain> getToolChainsMatching(Map<String, String> properties);

}
