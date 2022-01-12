/*******************************************************************************
 * Copyright (c) 2013 Mentor Graphics and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Mentor Graphics - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.internal.ui.commands;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.commands.AbstractDebugCommand;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;

/**
 * The enablement of a {@link AbstractDebugCommand} is updated only when the current
 * debug context is changed. In some cases we need to force an update without changing
 * the context. This class provides such a functionality.
 * The proper way is to modify {@link AbstractDebugCommand}.
 */
public abstract class RefreshableDebugCommand extends AbstractDebugCommand {

	protected class UpdateEnablementJob extends UIJob {

		protected UpdateEnablementJob() {
			super("Update enablement job"); //$NON-NLS-1$
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			// Reseting the current selection in the Debug view will force the enablement update
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (window != null) {
				IWorkbenchPage page = window.getActivePage();
				if (page != null) {
					IViewPart view = page.findView(IDebugUIConstants.ID_DEBUG_VIEW);
					if (view != null) {
						IWorkbenchPartSite site = view.getSite();
						if (site != null) {
							ISelectionProvider selProvider = site.getSelectionProvider();
							if (selProvider != null) {
								selProvider.setSelection(selProvider.getSelection());
							}
						}
					}
				}
			}
			return Status.OK_STATUS;
		}
	}

	protected void updateEnablement() {
		new UpdateEnablementJob().schedule();
	}
}
