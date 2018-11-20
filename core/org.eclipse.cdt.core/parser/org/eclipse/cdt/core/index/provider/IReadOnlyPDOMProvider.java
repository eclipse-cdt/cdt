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
package org.eclipse.cdt.core.index.provider;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;

/**
 * This interface is intended for ISVs to implement when plugging a mechanism
 * for read-only/off-line indexes into the CIndex.ReadOnlyPDOMProvider extension point element.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 4.0
 */
public interface IReadOnlyPDOMProvider extends IIndexProvider {
	/**
	 * Returns an array of IPDOMDescriptors that should contribute to the logical index
	 * for the specified {@link ICConfigurationDescription}
	 * @param config the configuration description whose logical index should be augmented
	 */
	public IPDOMDescriptor[] getDescriptors(ICConfigurationDescription config);
}
