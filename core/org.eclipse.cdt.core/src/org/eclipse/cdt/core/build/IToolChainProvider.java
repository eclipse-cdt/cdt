/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.core.build;

import org.eclipse.core.runtime.CoreException;

/**
 * A provider of toolchains. Registered with the toolChainProvider extension
 * point.
 *
 * @since 6.0
 */
public interface IToolChainProvider {

	/**
	 * Returns the id for this provider.
	 *
	 * @return id
	 */
	String getId();

	/**
	 * Initialize the list of toolchains.
	 *
	 * @param manager handle on manager to add or remove them
	 */
	default void init(IToolChainManager manager) throws CoreException {
		// By default, toolchains are created on demand
	}

	/**
	 * Called by the manager to dynamically create the toolchain.
	 *
	 * @param id
	 *            the id of the toolchain
	 * @param version
	 *            the version of the toolchain
	 * @return the toolchain initialized with the settings.
	 * @deprecated providers do not manage toolchains, call
	 *             IToolManager.getToolChain() instead.
	 */
	@Deprecated
	default IToolChain getToolChain(String id, String version) throws CoreException {
		return null;
	}

}
