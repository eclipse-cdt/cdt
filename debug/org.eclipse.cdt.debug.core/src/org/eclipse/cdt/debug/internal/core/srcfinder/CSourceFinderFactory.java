/*******************************************************************************
 * Copyright (c) 2010, 2016 Freescale Semiconductor and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Freescale Semiconductor - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.srcfinder;

import org.eclipse.cdt.core.ISourceFinder;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.core.runtime.IAdapterFactory;

/**
 * Adapter factory that adapts an IBinary object to an ISourceFinder
 */
public class CSourceFinderFactory implements IAdapterFactory {

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		if (adaptableObject instanceof IBinary) {
			if (adapterType.equals(ISourceFinder.class)) {
				return (T) new CSourceFinder((IBinary) adaptableObject);
			}
		}
		return null;
	}

	@Override
	public Class<?>[] getAdapterList() {
		return new Class[] { ISourceFinder.class };
	}
}
