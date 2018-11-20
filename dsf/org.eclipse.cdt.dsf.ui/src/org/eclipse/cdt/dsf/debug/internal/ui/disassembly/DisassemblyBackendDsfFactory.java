/*******************************************************************************
 * Copyright (c) 2010, 2015 Freescale Semiconductor, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Freescale Semiconductor - initial API and implementation
 *     Patrick Chuong (Texas Instruments) - Bug 353351
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly;

import org.eclipse.cdt.debug.internal.ui.disassembly.dsf.IDisassemblyBackend;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterFactory;

/**
 */
public class DisassemblyBackendDsfFactory implements IAdapterFactory {

	private static final Class<?>[] ADAPTERS = { IDisassemblyBackend.class };

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		if (IDisassemblyBackend.class.equals(adapterType)) {
			if (adaptableObject instanceof IAdaptable
					&& DisassemblyBackendDsf.supportsDebugContext_((IAdaptable) adaptableObject)) {
				String sessionId = ((IDMVMContext) adaptableObject).getDMContext().getSessionId();
				DsfSession session = DsfSession.getSession(sessionId);
				if (session.isActive()) {
					IAdapterFactory factory = (IAdapterFactory) session.getModelAdapter(IAdapterFactory.class);
					if (factory != null) {
						T adapter = factory.getAdapter(adaptableObject, adapterType);
						if (adapter != null)
							return adapter;
					}
				}
				return (T) new DisassemblyBackendDsf();
			}
		}
		return null;
	}

	@Override
	public Class<?>[] getAdapterList() {
		return ADAPTERS;
	}
}
