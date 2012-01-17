/*******************************************************************************
 * Copyright (c) 2010 Freescale Semiconductor and others.
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

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof IBinary) {
			if (adapterType.equals(ISourceFinder.class)) {
				return new CSourceFinder((IBinary)adaptableObject);
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[] { ISourceFinder.class };
	}
}
