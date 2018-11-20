/*******************************************************************************
 * Copyright (c) 2009, 2015 Alena Laskavaia
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.ui.actions;

import java.util.Iterator;

import org.eclipse.cdt.codan.core.CodanRuntime;
import org.eclipse.cdt.codan.core.model.CheckerLaunchMode;
import org.eclipse.cdt.codan.internal.core.CheckersTimeStats;
import org.eclipse.cdt.codan.internal.ui.CodanUIMessages;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class RunCodeAnalysis implements IObjectActionDelegate {
	private ISelection sel;

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// nothing
	}

	@Override
	public void run(IAction action) {
		if (sel instanceof IStructuredSelection) {
			final IStructuredSelection ss = (IStructuredSelection) sel;
			Job job = new Job(CodanUIMessages.Job_TitleRunningAnalysis) {
				@Override
				protected IStatus run(final IProgressMonitor monitor) {
					try {
						int count = ss.size();
						monitor.beginTask(getName(), count * 100);
						if (monitor.isCanceled())
							return Status.CANCEL_STATUS;
						for (Iterator iterator = ss.iterator(); iterator.hasNext();) {
							Object o = iterator.next();
							if (o instanceof IAdaptable) {
								o = ((IAdaptable) o).getAdapter(IResource.class);
							}
							if (o instanceof IResource) {
								IResource res = (IResource) o;
								SubProgressMonitor subMon = new SubProgressMonitor(monitor, 100);
								CodanRuntime.getInstance().getBuilder().processResource(res, subMon,
										CheckerLaunchMode.RUN_ON_DEMAND);
								if (subMon.isCanceled())
									return Status.CANCEL_STATUS;
							}
							if (monitor.isCanceled())
								return Status.CANCEL_STATUS;
						}
						return Status.OK_STATUS;
					} finally {
						CheckersTimeStats.getInstance().traceStats();
						CheckersTimeStats.getInstance().reset();
					}
				}
			};
			job.setUser(true);
			job.schedule();
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.sel = selection;
	}
}
