/*******************************************************************************
 * Copyright (c) 2004, 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.sourcelookup; 

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.model.IWorkbenchAdapter;
 
/**
 * Adapter factory for CDT source containers.
 */
public class SourceContainerAdapterFactory implements IAdapterFactory {

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		if (adapterType.equals(IWorkbenchAdapter.class)) {
			return (T) new SourceContainerWorkbenchAdapter();
		}
		return null;
	}

	@Override
	public Class<?>[] getAdapterList() {
		return new Class[]{ IWorkbenchAdapter.class };
	}
}
