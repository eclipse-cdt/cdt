/*******************************************************************************
 * Copyright (c) 2010, 2016 Freescale Semiconductor and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
				return (T) new CSourceFinder((IBinary)adaptableObject);
			}
		}
		return null;
	}

	@Override
	public Class<?>[] getAdapterList() {
		return new Class[] { ISourceFinder.class };
	}
}
