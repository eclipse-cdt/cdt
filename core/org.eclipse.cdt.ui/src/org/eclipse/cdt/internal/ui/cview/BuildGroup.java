/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
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
 *     Alex Collins (Broadcom Corp.)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.cview;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.BuildAction;
import org.eclipse.ui.ide.IDEActionFactory;
import org.eclipse.ui.ide.ResourceUtil;

/**
 * This is the action group for workspace actions such as Build
 */
public class BuildGroup extends CViewActionGroup {

	/**
	 * An internal class which overrides the 'shouldPerformResourcePruning'
	 * method so that referenced projects aren't build twice . (The CDT
	 * managedbuild builds CDT reference project configuration as part of
	 * building the top-level project).
	 *
	 * Also ensure that files in referenced projects are saved automatically
	 * before build.
	 */
	public static class CDTBuildAction extends BuildAction {
		public CDTBuildAction(IShellProvider shell, int kind) {
			super(shell, kind);
		}

		@Override
		protected boolean updateSelection(IStructuredSelection s) {
			// Call the super since it needs to clear out some settings
			super.updateSelection(s);
			// Always build CDT projects
			return true;
		}

		@Override
		public void run() {
			// Ensure we correctly save files in all referenced projects before build
			Set<IProject> prjs = new HashSet<>();
			for (IResource resource : getSelectedResources()) {
				IProject project = resource.getProject();
				if (project != null) {
					prjs.add(project);
					try {
						prjs.addAll(Arrays.asList(project.getReferencedProjects()));
					} catch (CoreException e) {
						// Project not accessible or not open
					}
				}
			}
			saveEditors(prjs);

			// Clear the build console, and open a stream
			CUIPlugin.getDefault().startGlobalConsole();

			// Now delegate to the parent
			super.run();
		}

		/**
		 * Taken from inaccessible o.e.ui.ide.BuildUtilities.java
		 *
		 * Causes all editors to save any modified resources in the provided collection
		 * of projects depending on the user's preference.
		 * @param projects The projects in which to save editors, or <code>null</code>
		 * to save editors in all projects.
		 */
		private static void saveEditors(Collection<IProject> projects) {
			if (!BuildAction.isSaveAllSet()) {
				return;
			}
			IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
			for (IWorkbenchWindow window : windows) {
				IWorkbenchPage[] pages = window.getPages();
				for (IWorkbenchPage page : pages) {
					if (projects == null) {
						page.saveAllEditors(false);
					} else {
						IEditorPart[] editors = page.getDirtyEditors();
						for (IEditorPart editor : editors) {
							IFile inputFile = ResourceUtil.getFile(editor.getEditorInput());
							if (inputFile != null) {
								if (projects.contains(inputFile.getProject())) {
									page.saveEditor(editor, false);
								}
							}
						}
					}
				}
			}
		}
	}

	private static class RebuildAction extends CDTBuildAction {
		public RebuildAction(IShellProvider shell) {
			super(shell, IncrementalProjectBuilder.FULL_BUILD);
		}

		@Override
		protected void invokeOperation(IResource resource, IProgressMonitor monitor) throws CoreException {
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

	@Override
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
	@Override
	public void fillContextMenu(IMenuManager menu) {
		IStructuredSelection selection = (IStructuredSelection) getContext().getSelection();
		boolean isProjectSelection = true;
		boolean hasOpenProjects = false;
		boolean hasClosedProjects = false;
		boolean hasBuilder = true; // false if any project is closed or does
		// not have builder

		menu.add(new GroupMarker(BUILD_GROUP_MARKER));

		Iterator<?> resources = selection.iterator();
		while (resources.hasNext() && (!hasOpenProjects || !hasClosedProjects || hasBuilder || isProjectSelection)) {
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
	@Override
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
			if (commands.length > 0)
				return true;
		} catch (CoreException e) {
			// Cannot determine if project has builders. Project is closed
			// or does not exist. Fall through to return false.
		}
		return false;
	}

	@Override
	protected void makeActions() {
		final IWorkbenchPartSite site = getCView().getSite();

		buildAction = new CDTBuildAction(site, IncrementalProjectBuilder.INCREMENTAL_BUILD);
		buildAction.setText(CViewMessages.BuildAction_label);

		cleanAction = new CDTBuildAction(site, IncrementalProjectBuilder.CLEAN_BUILD);
		cleanAction.setText(CViewMessages.CleanAction_label);

		rebuildAction = new RebuildAction(site);
		rebuildAction.setText(CViewMessages.RebuildAction_label);
	}

	@Override
	public void updateActionBars() {
		IStructuredSelection selection = (IStructuredSelection) getContext().getSelection();
		buildAction.selectionChanged(selection);
	}
}
