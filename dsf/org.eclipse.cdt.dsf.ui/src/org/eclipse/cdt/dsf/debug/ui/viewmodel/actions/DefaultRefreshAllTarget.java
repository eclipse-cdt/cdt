/*******************************************************************************
 * Copyright (c) 2008, 2015 Wind River Systems and others.
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
package org.eclipse.cdt.dsf.debug.ui.viewmodel.actions;

import org.eclipse.cdt.dsf.ui.viewmodel.IVMAdapter;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.update.ICachingVMProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * The default implementation of the refresh all debug target which
 * calls the active VM providers, to ask them to refresh.
 *
 * @since 1.1
 */
public class DefaultRefreshAllTarget implements IRefreshAllTarget {

	@Override
	public void refresh(ISelection debugContext) throws CoreException {
		IVMAdapter adapter = getActiveVMAdapter(debugContext);

		if (adapter != null) {
			for (IVMProvider provider : adapter.getActiveProviders()) {
				if (provider instanceof ICachingVMProvider) {
					((ICachingVMProvider) provider).refresh();
				}
			}
		}
	}

	/**
	 * @since 2.0
	 */
	protected IVMAdapter getActiveVMAdapter(ISelection debugContext) {

		if (debugContext instanceof IStructuredSelection) {
			Object activeElement = ((IStructuredSelection) debugContext).getFirstElement();
			if (activeElement instanceof IAdaptable) {
				return ((IAdaptable) activeElement).getAdapter(IVMAdapter.class);
			}
		}
		return null;
	}
}
