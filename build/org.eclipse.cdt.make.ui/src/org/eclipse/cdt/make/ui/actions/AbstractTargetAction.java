/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Axel Mueller - Rebuild last target
 *******************************************************************************/
package org.eclipse.cdt.make.ui.actions;

import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.ui.TargetSourceContainer;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.actions.ActionDelegate;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.EditorPart;

public abstract class AbstractTargetAction
	extends ActionDelegate
	implements IObjectActionDelegate, IWorkbenchWindowActionDelegate {
	private IWorkbenchPart fPart;
	private IWorkbenchWindow fWindow;
	private boolean isEnabled;
	/** @since 7.0 */
	protected IContainer fContainer;

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

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		fPart = targetPart;
	}

	@Override
	public void init(IWorkbenchWindow window) {
		fWindow = window;
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		isEnabled = false;
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
			} else if (obj instanceof TargetSourceContainer) {
				fContainer = ((TargetSourceContainer)obj).getContainer();
			} else if (obj instanceof IMakeTarget) {
				fContainer = ((IMakeTarget)obj).getContainer();
			} else {
				fContainer = null;
			}
		} else if (selection instanceof ITextSelection)	{
			// Key binding pressed inside active text editor
			fContainer= null;
			IWorkbenchPart part = fPart != null ? fPart : fWindow.getActivePage().getActivePart();
			if (part instanceof TextEditor) {
				IFile file = org.eclipse.ui.ide.ResourceUtil.getFile(((EditorPart) part).getEditorInput());
				if (file != null) {
					fContainer = file.getParent();
				}
			}
		}
		if (fContainer != null && MakeCorePlugin.getDefault().getTargetManager().hasTargetBuilder(fContainer.getProject())) {
			isEnabled = true;
		}
		if ( action != null )
			action.setEnabled(isEnabled);
	}

	/**
	 * @return {@code true} if the action is enabled or {@code false} otherwise.
	 *
	 * @since 7.0
	 */
	public boolean isEnabled() {
		return isEnabled;
	}
}
