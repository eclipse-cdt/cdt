/**********************************************************************
 * Copyright (c) 2002,2003 Timesys Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Timesys - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.core.builder;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.builder.model.ICToolchain;
import org.eclipse.cdt.core.builder.model.ICToolchainProvider;

/**
 * Abstract base class to make the life of ICToolchainProvider
 * implementers somewhat simpler.
 * <p>
 * Provides default implementations of all methods, such that
 * a basic toolchain can be defined simply by implementing the
 * abstract doRefresh() method.
 * <p>
 * Examples:
 * <p>
 * <code>
 * class CGenericToolchain extends ACToolchainProvider {
 * 	void doRefresh() {
 * 		ICToolchain tc = readToolchainInfoFromFile();
 * 		addToolchain(tc.getId(), tc);
 *	}
 * }
 * </code>
 */
public abstract class ACToolchainProvider implements ICToolchainProvider {

	/**
	 * Internal map of toolchain ID to toolchain instances.
	 */
	private Map fToolchainMap;

	/**
	 * Constructor.
	 * <br>
	 * Creates a new toolchain map, then calls the abstract
	 * doRefresh() method to allow derived classes to populate
	 * the map.
	 */
	public ACToolchainProvider() {
		fToolchainMap = new HashMap();
		doRefresh();
	}

	/**
	 * Determines if a toolchain exists in the internal map of
	 * toolchain instances.
	 * 
	 * @param id toolchain identifier.
	 * @return true if there is a toolchain instances that corresponds
	 * to the provided id.
	 */
	protected boolean toolchainExists(String id) {
		return fToolchainMap.containsKey(id);
	}

	/**
	 * Add a toolchain to the internal map of toolchain instances.
	 * 
	 * @param id toolchain identifier.
	 * @param tc toolchain instance.
	 */
	protected void addToolchain(String id, ICToolchain tc) {
		fToolchainMap.put(id, tc);
	}

	/**
	 * Helper method used to retrieve a toolchain from the internal
	 * map of toolchain instances.
	 * 
	 * @param id toolchain identifier.
	 * @return toolchain instance, or null if not found.
	 */
	protected ICToolchain getToolchainHelper(String id) {
		ICToolchain tc = null;
		Object obj = fToolchainMap.get(id);
		if (obj instanceof ICToolchain) {
			tc = (ICToolchain) obj;
		}
		return tc;
	}

	/**
	 * Remove a toolchain from the internal map of toolchain instances.
	 * 
	 * @param id toolchain identifier.
	 * @return true if toolchain is removed.
	 */
	protected boolean removeToolchain(String id) {
		boolean exists = toolchainExists(id);
		if (exists) {
			Object obj = fToolchainMap.remove(id);
			obj = null;
		}
		return exists;
	}

	/**
	 * @see org.eclipse.cdt.core.builder.model.ICToolchainProvider#getToolchain(String)
	 */
	public ICToolchain getToolchain(String id) {
		return getToolchainHelper(id);
	}

	/**
	 * @see org.eclipse.cdt.core.builder.model.ICToolchainProvider#getToolchains()
	 */
	public ICToolchain[] getToolchains() {
		Collection tcc = fToolchainMap.values();
		return (ICToolchain[]) tcc.toArray(new ICToolchain[tcc.size()]);
	}

	/**
	 * Implemented by derived classes. Called whenever the toolchain list needs
	 * to be refreshed.
	 */
	abstract public void doRefresh();
}
