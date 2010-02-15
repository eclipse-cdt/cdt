/*******************************************************************************
 * Copyright (c) 2010 Freescale Semiconductor, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Freescale Semiconductor - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly;

import org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyBackend;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterFactory;

/**
 */
public class DisassemblyBackendDsfFactory implements IAdapterFactory {

	private static final Class<?>[] ADAPTERS = { IDisassemblyBackend.class };

	@SuppressWarnings("rawtypes")
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (IDisassemblyBackend.class.equals(adapterType)) {
			if (adaptableObject instanceof IAdaptable && DisassemblyBackendDsf.supportsDebugContext_((IAdaptable)adaptableObject)) {
				return new DisassemblyBackendDsf();
			}
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return ADAPTERS;
	}
}
