/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.ui.actions;

import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.actions.ActionDelegate;

public abstract class AbstractTargetAction
	extends ActionDelegate
	implements IObjectActionDelegate, IWorkbenchWindowActionDelegate {
	private IWorkbenchPart fPart;
	private IWorkbenchWindow fWindow;
	private IContainer fContainer;

	protected Shell getShell() {
		if (fPart != null) {
			return fPart.getSite().getShell();
		} else if (fWindow != null) {
			return fWindow.getShell();
		}
		return MakeUIPlugin.getActiveWorkbenchShell();
	}

	protected IContainer getSelectedContainer() {
		return fContainer;
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		fPart = targetPart;
	}

	public void init(IWorkbenchWindow window) {
		fWindow = window;
	}

	public void selectionChanged(IAction action, ISelection selection) {
		boolean enabled = false;
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sel = (IStructuredSelection) selection;
			Object obj = sel.getFirstElement();
			if (obj instanceof ICElement) {
				if ( obj instanceof ICContainer || obj instanceof ICProject) {
					fContainer = (IContainer) ((ICElement) obj).getUnderlyingResource();
				} else {
					obj = ((ICElement)obj).getResource();
					if ( obj != null) {
						fContainer = ((IResource)obj).getParent();
					}
				}
			} else if (obj instanceof IResource) {
				if (obj instanceof IContainer) {
					fContainer = (IContainer) obj;
				} else {
					fContainer = ((IResource)obj).getParent();
				}
			} else {
				fContainer = null;
			}
			if (fContainer != null && MakeCorePlugin.getDefault().getTargetManager().hasTargetBuilder(fContainer.getProject())) {
				enabled = true;
			}
		}
		action.setEnabled(enabled);
	}

}
