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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ISafeRunnable;

/**
 * The global toolchain manager. Accessed as an OSGi service.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 6.0
 */
public interface IToolChainManager {

	/**
	 * Return the provider with the given id
	 *
	 * @param providerId
	 *            id
	 * @return provider
	 * @throws CoreException
	 */
	IToolChainProvider getProvider(String providerId) throws CoreException;

	/**
	 * Return the UI label for the toolchain type.
	 *
	 * @param id
	 *            type toolchain type id
	 * @return name of the type
	 * @since 6.4
	 */
	String getToolChainTypeName(String typeId);

	/**
	 * Return the toolchain from the given provider with the given id and version.
	 *
	 * @param providerId
	 *            id of provider
	 * @param id
	 *            id of toolchain
	 * @param version
	 *            version of toolchain
	 * @return the toolchain
	 * @throws CoreException
	 * @deprecated version is now irrelevant. id's are unique.
	 */
	@Deprecated
	default IToolChain getToolChain(String providerId, String id, String version) throws CoreException {
		return getToolChain(providerId, id);
	}

	/**
	 * Return the toolChain with the given type and id.
	 *
	 * @param typeId
	 *            id of toolchain type
	 * @param id
	 *            id of toolchain
	 * @return the toolchain
	 * @throws CoreException
	 * @since 6.4
	 */
	IToolChain getToolChain(String typeId, String id) throws CoreException;

	/**
	 * Return the toolchains provided by the given provider
	 *
	 * @param providerId
	 *            id of provider
	 * @return toolchains the provider provides
	 * @throws CoreException
	 * @deprecated we no longer organize toolchains by provider id.
	 */
	@Deprecated
	default Collection<IToolChain> getToolChains(String providerId) throws CoreException {
		return null;
	}

	/**
	 * Return all versions of toolchains with the given id provided by the given
	 * provider.
	 *
	 * @param providerId
	 *            id of provider
	 * @param id
	 *            id of toolchains
	 * @return toolchains with the given id provided by the provider
	 * @throws CoreException
	 * @deprecated toolchains no longer have multiple versions per id
	 */
	@Deprecated
	default Collection<IToolChain> getToolChains(String providerId, String id) throws CoreException {
		return null;
	}

	/**
	 * Returns the list of toolchains that have the given properties.
	 *
	 * @param properties
	 *            properties of the toolchains
	 * @return the qualified toolchains
	 */
	Collection<IToolChain> getToolChainsMatching(Map<String, String> properties) throws CoreException;

	/**
	 * Return all of the toolchains.
	 *
	 * @since 6.4
	 */
	Collection<IToolChain> getAllToolChains() throws CoreException;

	/**
	 * Set the preference order of the toolchains. This controls the order
	 * toolchains are returned in the other methods in this interface. Often, the
	 * first toolchain in a list is the default toolchain to use in a build
	 * configuration.
	 *
	 * @param orderedToolchains
	 * @throws CoreException
	 * @since 6.4
	 */
	void setToolChainOrder(List<IToolChain> orderedToolchains) throws CoreException;

	/**
	 * Add a toolchain.
	 *
	 * @param toolChain
	 *            the toolchain
	 */
	void addToolChain(IToolChain toolChain);

	/**
	 * Remove a toolchain
	 *
	 * @param toolChain
	 *            the toolchain
	 */
	void removeToolChain(IToolChain toolChain);

	/**
	 * Add a listener for toolchains added or removed. The listener is a simple
	 * runnable that is called when an event occurs.
	 *
	 * @param listener
	 *            runnable that is called when a toolchain is added or removed
	 * @since 6.4
	 */
	void addToolChainListener(ISafeRunnable listener);

	/**
	 * Remove a listener.
	 *
	 * @param listener
	 *            the listener to remove
	 * @since 6.4
	 */
	void removeToolChainListener(ISafeRunnable listener);

}
