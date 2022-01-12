/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.ui.viewmodel.update;

/**
 * Extension to the VM Update policy which allows the policy to control how to
 * update missing property values in a dirty cache entry.
 *
 * @since 2.2
 */
public interface IVMUpdatePolicyExtension extends IVMUpdatePolicy {

	/**
	 * Determines whether the given dirty cache entry should have the given
	 * missing property updated.
	 *
	 * @param entry The dirty cache entry that is missing the given requested
	 * property.
	 * @param property Property missing from cache.
	 * @return If <code>true</code> cache can update the given missing property
	 * in the dirty cache entry with data from the VM node.
	 */
	public boolean canUpdateDirtyProperty(ICacheEntry entry, String property);
}
