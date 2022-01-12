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
 *     Anton Leherbauer (Wind River Systems) - adaptations for Common Navigator
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.navigator;

import java.util.Iterator;

import org.eclipse.cdt.internal.ui.cview.BuildGroup;
import org.eclipse.cdt.internal.ui.cview.CViewMessages;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.actions.BuildAction;
import org.eclipse.ui.ide.IDEActionFactory;
import org.eclipse.ui.navigator.ICommonMenuConstants;

/**
 * Common Navigator compatible clone of {@link org.eclipse.cdt.internal.ui.cview.BuildGroup}.
 * <p>
 * Adds action "Clean Project" and group marker "buildGroup" to the context menu.
 * </p>
 * @see org.eclipse.cdt.internal.ui.cview.BuildGroup
 * @see org.eclipse.ui.actions.BuildAction
 */
public class CNavigatorBuildActionGroup extends AbstractCNavigatorActionGroup {

	private BuildAction fBuildAction;
	private BuildAction fCleanAction;

	// Menu tags for the build
	final String BUILD_GROUP_MARKER = "buildGroup"; //$NON-NLS-1$
	final String BUILD_GROUP_MARKER_END = "end-buildGroup"; //$NON-NLS-1$

	/**
	 * Create action group associated with given view part.
	 * @param viewPart
	 */
	public CNavigatorBuildActionGroup(IViewPart viewPart) {
		super(viewPart);
	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
		// register a CDT Build project action which prevents projects with refs from being built twice
		actionBars.setGlobalActionHandler(IDEActionFactory.BUILD_PROJECT.getId(), fBuildAction);
		updateActionBars();
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
			fCleanAction.selectionChanged(selection);
			if (fCleanAction.isEnabled()) {
				IContributionItem oldBuild = menu.find(BuildAction.ID_BUILD);
				if (oldBuild != null) {
					menu.insertAfter(BuildAction.ID_BUILD, fCleanAction);
					// Replace ResourceMgmtActionProvier's build action with our own
					if (oldBuild instanceof ActionContributionItem
							&& ((ActionContributionItem) oldBuild).getAction() instanceof BuildAction) {
						menu.remove(oldBuild);
						menu.insertBefore(fCleanAction.getId(), fBuildAction);
					}
				} else {
					menu.insertAfter(ICommonMenuConstants.GROUP_BUILD, fCleanAction);
					menu.insertAfter(ICommonMenuConstants.GROUP_BUILD, fBuildAction);
				}
			}
		}
		menu.appendToGroup(ICommonMenuConstants.GROUP_BUILD, new Separator(BUILD_GROUP_MARKER));
		menu.appendToGroup(BUILD_GROUP_MARKER, new GroupMarker(BUILD_GROUP_MARKER_END));
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
		fBuildAction = new BuildGroup.CDTBuildAction(getViewPart().getSite(),
				IncrementalProjectBuilder.INCREMENTAL_BUILD);
		fCleanAction = new BuildGroup.CDTBuildAction(getViewPart().getSite(), IncrementalProjectBuilder.CLEAN_BUILD);
		fCleanAction.setText(CViewMessages.CleanAction_label);
	}

	@Override
	public void updateActionBars() {
		IStructuredSelection selection = (IStructuredSelection) getContext().getSelection();
		fBuildAction.selectionChanged(selection);
		fCleanAction.selectionChanged(selection);
	}
}
