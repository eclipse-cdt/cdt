/*******************************************************************************
 * Copyright (c) 2017 QNX Software Systems and others.
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
 * A toolchain provider that is managed by the user. The user can manually add
 * and remove toolchains.
 *
 * It is the responsibility of the provider to manage persistence and to
 * populate the toolchains with the toolchain manager.
 *
 * @since 6.4
 */
public interface IUserToolChainProvider extends IToolChainProvider {

	/**
	 * Manually add a toolchain to be managed by this provider.
	 *
	 * @param toolChain
	 *            toolchain to be added
	 * @since 6.4
	 */
	void addToolChain(IToolChain toolChain) throws CoreException;

	/**
	 * Manually remove a toolchain managed by this provider.
	 *
	 * @param toolChain
	 *            toolchain to be removed
	 * @throws CoreException
	 */
	void removeToolChain(IToolChain toolChain) throws CoreException;

}
