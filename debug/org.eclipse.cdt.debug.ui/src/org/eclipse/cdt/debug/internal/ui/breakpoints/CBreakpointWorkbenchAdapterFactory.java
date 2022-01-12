/*******************************************************************************
 * Copyright (c) 2004, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.breakpoints;

import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.debug.core.model.ICWatchpoint;
import org.eclipse.cdt.debug.internal.ui.CDebugUIMessages;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.WorkbenchAdapter;

/**
 * Adapter factory for C/C++ breakpoints.
 */
public class CBreakpointWorkbenchAdapterFactory implements IAdapterFactory {

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		if (adapterType != IWorkbenchAdapter.class || !(adaptableObject instanceof ICBreakpoint)) {
			return null;
		}
		return (T) new WorkbenchAdapter() {
			@Override
			public String getLabel(Object o) {
				// for now
				if (o instanceof ICLineBreakpoint) {
					return CDebugUIMessages.getString("CBreakpointWorkbenchAdapterFactory.0"); //$NON-NLS-1$
				}
				if (o instanceof ICWatchpoint) {
					return CDebugUIMessages.getString("CBreakpointWorkbenchAdapterFactory.1"); //$NON-NLS-1$
				}
				return super.getLabel(o);
			}
		};
	}

	@Override
	public Class<?>[] getAdapterList() {
		return new Class[] { IWorkbenchAdapter.class };
	}
}
