/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.cview;

import java.util.Iterator;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.CloseResourceAction;
import org.eclipse.ui.actions.OpenResourceAction;
import org.eclipse.ui.actions.RefreshAction;
import org.eclipse.ui.ide.IDEActionFactory;

/**
 * This is the action group for actions such as Refresh Local, and Open/Close
 * Project.
 */
public class OpenProjectGroup extends CViewActionGroup {

	private OpenResourceAction openProjectAction;
	private CloseResourceAction closeProjectAction;
	private RefreshAction refreshAction;

	public OpenProjectGroup(CView cview) {
		super(cview);
	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
		actionBars.setGlobalActionHandler(ActionFactory.REFRESH.getId(), refreshAction);
		actionBars.setGlobalActionHandler(IDEActionFactory.OPEN_PROJECT.getId(), openProjectAction);
		actionBars.setGlobalActionHandler(IDEActionFactory.CLOSE_PROJECT.getId(), closeProjectAction);
	}

	/**
	 * Adds the open project, close project and refresh resource actions to the
	 * context menu.
	 * <p>
	 * refresh-no closed project selected
	 * </p>
	 * <p>
	 * Both the open project and close project action may be on the menu at the
	 * same time.
	 * </p>
	 * <p>
	 * No disabled action should be on the context menu.
	 * </p>
	 *
	 * @param menu
	 *            context menu to add actions to
	 */
	@Override
	public void fillContextMenu(IMenuManager menu) {
		IStructuredSelection selection = (IStructuredSelection) getContext().getSelection();
		boolean isProjectSelection = true;
		boolean hasOpenProjects = false;
		boolean hasClosedProjects = false;
		Iterator<?> resources = selection.iterator();

		while (resources.hasNext() && (!hasOpenProjects || !hasClosedProjects || isProjectSelection)) {
			Object next = resources.next();
			IProject project = null;

			if (next instanceof IProject) {
				project = (IProject) next;
			} else if (next instanceof IAdaptable) {
				IResource res = ((IAdaptable) next).getAdapter(IResource.class);
				if (res instanceof IProject) {
					project = (IProject) res;
				}
			}

			if (project == null) {
				isProjectSelection = false;
				continue;
			}
			if (project.isOpen()) {
				hasOpenProjects = true;
			} else {
				hasClosedProjects = true;
			}
		}
		if (!hasClosedProjects) {
			refreshAction.selectionChanged(selection);
			menu.add(refreshAction);
		}
		if (isProjectSelection) {
			if (hasClosedProjects) {
				openProjectAction.selectionChanged(selection);
				menu.add(openProjectAction);
			}
			if (hasOpenProjects) {
				closeProjectAction.selectionChanged(selection);
				menu.add(closeProjectAction);
			}
		}
	}

	/**
	 * Handles a key pressed event by invoking the appropriate action.
	 */
	@Override
	public void handleKeyPressed(KeyEvent event) {
		if (event.keyCode == SWT.F5 && event.stateMask == 0) {
			if (refreshAction.isEnabled()) {
				refreshAction.refreshAll();
			}
			// Swallow the event
			event.doit = false;
		}
	}

	@Override
	protected void makeActions() {
		final IWorkbenchPartSite site = getCView().getSite();
		IWorkspace workspace = CUIPlugin.getWorkspace();

		openProjectAction = new OpenResourceAction(site);
		workspace.addResourceChangeListener(openProjectAction, IResourceChangeEvent.POST_CHANGE);
		closeProjectAction = new CloseResourceAction(site);
		workspace.addResourceChangeListener(closeProjectAction, IResourceChangeEvent.POST_CHANGE);
		refreshAction = new RefreshAction(site);
		refreshAction.setDisabledImageDescriptor(getImageDescriptor("dlcl16/refresh_nav.gif"));//$NON-NLS-1$
		refreshAction.setImageDescriptor(getImageDescriptor("elcl16/refresh_nav.gif"));//$NON-NLS-1$
		//		refreshAction.setHoverImageDescriptor(getImageDescriptor("clcl16/refresh_nav.gif"));//$NON-NLS-1$
	}

	@Override
	public void updateActionBars() {
		IStructuredSelection selection = (IStructuredSelection) getContext().getSelection();
		refreshAction.selectionChanged(selection);
		openProjectAction.selectionChanged(selection);
		closeProjectAction.selectionChanged(selection);
	}

	@Override
	public void dispose() {
		IWorkspace workspace = CUIPlugin.getWorkspace();
		workspace.removeResourceChangeListener(closeProjectAction);
		workspace.removeResourceChangeListener(openProjectAction);
	}

}
