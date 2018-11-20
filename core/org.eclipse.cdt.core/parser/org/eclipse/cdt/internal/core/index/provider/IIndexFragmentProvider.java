/*******************************************************************************
 * Copyright (c) 2007, 2011 Symbian Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.provider;

import org.eclipse.cdt.core.index.provider.IIndexProvider;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.core.runtime.CoreException;

/**
 * The ICProject IIndex is a logical index composed of potentially many
 * IIndexFragments. An IIndexFragmentProvider is a source of IIndexFragments.
 * <p>
 *
 * <p>
 * IndexProviders are registered via the extension point
 * <code>org.eclipse.cdt.core.CIndex</code>
 * <p>
 */
public interface IIndexFragmentProvider extends IIndexProvider {
	/**
	 * Returns an array of IIndexFragment objects to add to the specified
	 * {@link ICConfigurationDescription}.
	 *
	 * @param project
	 * @return an array of IIndexFragment objects to add to the specified
	 * {@link ICConfigurationDescription}
	 */
	IIndexFragment[] getIndexFragments(ICConfigurationDescription config) throws CoreException;
}
