/*******************************************************************************
 * Copyright (c) 2012, 2016 Wind River Systems and others.
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
package org.eclipse.cdt.debug.internal.core;

import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.model.ICDebugElement;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.core.model.IDebugModelProvider;

/**
 * Debug model provider returns additional model ID to use with
 * GDB event breakpoints.
 */
public class DebugModelProvider implements IDebugModelProvider, IAdapterFactory {

	private final static Class<?>[] ADAPTER_LIST = new Class[] { IDebugModelProvider.class };
	private final static String GDB_MODEL_ID = "org.eclipse.cdt.gdb"; //$NON-NLS-1$
	private final static String[] MODEL_IDS = new String[] { CDIDebugModel.getPluginIdentifier(), GDB_MODEL_ID };

	@Override
	public String[] getModelIdentifiers() {
		return MODEL_IDS;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		if (adaptableObject instanceof ICDebugElement && IDebugModelProvider.class.equals(adapterType)) {
			return (T) this;
		}
		return null;
	}

	@Override
	public Class<?>[] getAdapterList() {
		return ADAPTER_LIST;
	}

}
