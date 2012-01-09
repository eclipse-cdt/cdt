/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.dnd;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.CUIMessages;
import org.eclipse.cdt.internal.ui.util.ExceptionHandler;

public class FileTransferDragAdapter implements TransferDragSourceListener {
	private final ISelectionProvider provider;

	public FileTransferDragAdapter(ISelectionProvider provider) {
		super();
		this.provider = provider;
		Assert.isNotNull(provider);
	}

	@Override
	public Transfer getTransfer() {
		return FileTransfer.getInstance();
	}

	@Override
	public void dragStart(DragSourceEvent event) {
		event.doit = !getResources().isEmpty();
	}

	@Override
	public void dragSetData(DragSourceEvent event) {
		event.data = getResourceLocations(getResources());
	}

	@Override
	public void dragFinished(DragSourceEvent event) {
		if (event.doit) {
			if (event.detail == DND.DROP_MOVE) {
				// Never delete resources when dragging outside Eclipse.				
				// See: http://bugs.eclipse.org/bugs/show_bug.cgi?id=30543
			} else if (event.detail == DND.DROP_NONE || event.detail == DND.DROP_TARGET_MOVE) {
				runOperation(new RefreshOperation(getResources()), true, false);
			}
		}
	}

	private static class RefreshOperation extends WorkspaceModifyOperation {
		private final Set<IResource> roots;

		public RefreshOperation(List<IResource> resources) {
			super();

			roots = new HashSet<IResource>(resources.size());

			for (IResource resource : resources) {
				IResource parent = resource.getParent();
				roots.add(parent != null ? parent : resource);
			}
		}

		@Override
		public void execute(IProgressMonitor monitor) throws CoreException {
			try {
				monitor.beginTask(CUIMessages.FileTransferDragAdapter_refreshing, roots.size()); 
				MultiStatus status = new MultiStatus(CUIPlugin.getPluginId(), IStatus.OK, CUIMessages.FileTransferDragAdapter_problem, null); 

				for (IResource resource : roots) {
					try {
						resource.refreshLocal(
							IResource.DEPTH_ONE,
							new SubProgressMonitor(monitor, 1));
					} catch (CoreException e) {
						status.add(e.getStatus());
					}
				}

				if (!status.isOK())
					throw new CoreException(status);
			} finally {
				monitor.done();
			}
		}
	}

	private List<IResource> getResources() {
		List<IResource> result = Collections.emptyList();
		ISelection selection = provider.getSelection();

		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structured = (IStructuredSelection) selection;

			result = new ArrayList<IResource>(structured.size());

			for (Iterator<?> iterator = structured.iterator(); iterator.hasNext();) {
				Object object = iterator.next();
				IResource resource = null;

				if (object instanceof IResource) {
					resource = (IResource) object;
				} else if (object instanceof IAdaptable) {
					resource = (IResource) ((IAdaptable) object).getAdapter(IResource.class);
				}

				if (resource != null)
					result.add(resource);
			}
		}

		return result;
	}

	private static String[] getResourceLocations(List<IResource> resources) {
		if (!resources.isEmpty()) {
			int count = resources.size();
			List<String> locations = new ArrayList<String>(count);
			
			for (IResource resource : resources) {
				IPath location = resource.getLocation();
				
				if (location != null) {
					locations.add(location.toOSString());
				}
			}
			
			String[] result = new String[locations.size()];
			
			locations.toArray(result);
			
			return result;
		}
		return null;
	}

	private static void runOperation(
		IRunnableWithProgress operation,
		boolean fork,
		boolean cancelable) {
		try {
			IWorkbench workbench = CUIPlugin.getDefault().getWorkbench();
			IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
			Shell parent = window.getShell();

			new ProgressMonitorDialog(parent).run(fork, cancelable, operation);
		} catch (InterruptedException e) {
			// Do nothing. Operation has been canceled by user.
		} catch (InvocationTargetException e) {
			String message = CUIMessages.FileTransferDragAdapter_problem; 
			String title = CUIMessages.FileTransferDragAdapter_problemTitle; 

			ExceptionHandler.handle(e, title, message);
		}
	}
}
