/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.cview;

import java.util.Iterator;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.BuildAction;
import org.eclipse.ui.ide.IDEActionFactory;

/**
 * This is the action group for workspace actions such as Build
 */
public class BuildGroup extends CViewActionGroup {

	private class RebuildAction extends BuildAction {
	    public RebuildAction(Shell shell) {
	        super(shell, IncrementalProjectBuilder.FULL_BUILD);
	    }
	    protected void invokeOperation(IResource resource, IProgressMonitor monitor)
	            throws CoreException {
	    	// these are both async.  NOT what I want.
	    	((IProject) resource).build(IncrementalProjectBuilder.CLEAN_BUILD, monitor);
	    	((IProject) resource).build(IncrementalProjectBuilder.FULL_BUILD, monitor);

	    }
	}

	private BuildAction buildAction;
	private BuildAction rebuildAction;
	private BuildAction cleanAction;

	// Menu tags for the build
	final String BUILD_GROUP_MARKER = "buildGroup"; //$NON-NLS-1$
	final String BUILD_GROUP_MARKER_END = "end-buildGroup"; //$NON-NLS-1$

	public BuildGroup(CView cview) {
		super(cview);
	}

	public void fillActionBars(IActionBars actionBars) {
		actionBars.setGlobalActionHandler(IDEActionFactory.BUILD_PROJECT.getId(), buildAction);
	}

	/**
	 * Adds the build actions to the context menu.
	 * <p>
	 * The following conditions apply: build-only projects selected, auto build
	 * disabled, at least one * builder present
	 * </p>
	 * <p>
	 * No disabled action should be on the context menu.
	 * </p>
	 * 
	 * @param menu
	 *            context menu to add actions to
	 */
	public void fillContextMenu(IMenuManager menu) {
		IStructuredSelection selection = (IStructuredSelection) getContext().getSelection();
		boolean isProjectSelection = true;
		boolean hasOpenProjects = false;
		boolean hasClosedProjects = false;
		boolean hasBuilder = true; // false if any project is closed or does
		// not have builder

		menu.add(new GroupMarker(BUILD_GROUP_MARKER));

		Iterator resources = selection.iterator();
		while (resources.hasNext() && (!hasOpenProjects || !hasClosedProjects || hasBuilder || isProjectSelection)) {
			Object next = resources.next();
			IProject project = null;

			if (next instanceof IProject) {
				project = (IProject) next;
			} else if (next instanceof IAdaptable) {
				IResource res = (IResource)((IAdaptable)next).getAdapter(IResource.class);
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
				if (hasBuilder && !hasBuilder(project)) {
					hasBuilder = false;
				}
			} else {
				hasClosedProjects = true;
				hasBuilder = false;
			}
		}

		if (!selection.isEmpty() && isProjectSelection && hasBuilder) {
			buildAction.selectionChanged(selection);
			menu.add(buildAction);
//			rebuildAction.selectionChanged(selection);
//			menu.add(rebuildAction);
			cleanAction.selectionChanged(selection);
			menu.add(cleanAction);
		}
		menu.add(new GroupMarker(BUILD_GROUP_MARKER_END));
	}

	/**
	 * Handles a key pressed event by invoking the appropriate action.
	 */
	public void handleKeyPressed(KeyEvent event) {
	}

	/**
	 * Returns whether there are builders configured on the given project.
	 * 
	 * @return <code>true</code> if it has builders, <code>false</code> if
	 *         not, or if this could not be determined
	 */
	boolean hasBuilder(IProject project) {
		try {
			ICommand[] commands = project.getDescription().getBuildSpec();
			if (commands.length > 0) return true;
		} catch (CoreException e) {
			// Cannot determine if project has builders. Project is closed
			// or does not exist. Fall through to return false.
		}
		return false;
	}

	protected void makeActions() {
		Shell shell = getCView().getSite().getShell();

		buildAction = new BuildAction(shell, IncrementalProjectBuilder.FULL_BUILD);
		buildAction.setText(CViewMessages.getString("BuildAction.label")); //$NON-NLS-1$

		cleanAction = new BuildAction(shell, IncrementalProjectBuilder.CLEAN_BUILD);
		cleanAction.setText(CViewMessages.getString("CleanAction.label")); //$NON-NLS-1$
		
		rebuildAction = new RebuildAction(shell);
		rebuildAction.setText(CViewMessages.getString("RebuildAction.label")); //$NON-NLS-1$
	}

	public void updateActionBars() {
		IStructuredSelection selection = (IStructuredSelection) getContext().getSelection();
		buildAction.selectionChanged(selection);
	}
}
