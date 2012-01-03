/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdapterFactory;

import org.eclipse.cdt.core.model.ICProject;

/**
 * Adapter factory to adapt <code>ICProject</code> to <code>IProject</code>.
 *
 * @since 4.0
 */
public class CProjectAdapterFactory implements IAdapterFactory {

	private static final Class<?>[] ADAPTERS = { IProject.class };

	/*
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (IProject.class.equals(adapterType)) {
			return ((ICProject)adaptableObject).getProject();
		}
		return null;
	}

	/*
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	@Override
	public Class<?>[] getAdapterList() {
		return ADAPTERS;
	}

}
