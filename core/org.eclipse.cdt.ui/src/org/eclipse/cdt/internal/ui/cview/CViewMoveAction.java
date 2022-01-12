/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.MoveProjectAction;
import org.eclipse.ui.actions.MoveResourceAction;

/**
 * The ResourceNavigatorMoveAction is a resource move that aso updates the navigator
 * to show the result of the move.
 * It also delegates to MoveProjectAction as needed.
 *
 * @since 2.0
 */
public class CViewMoveAction extends MoveResourceAction {
	private StructuredViewer viewer;
	private MoveProjectAction moveProjectAction;

	/**
	 * Create a ResourceNavigatorMoveAction and use the supplied viewer to update the UI.
	 * @param shellProvider provider for the shell
	 * @param structureViewer StructuredViewer
	 */
	public CViewMoveAction(IShellProvider shellProvider, StructuredViewer structureViewer) {
		super(shellProvider);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, ICHelpContextIds.MOVE_ACTION);
		this.viewer = structureViewer;
		this.moveProjectAction = new MoveProjectAction(shellProvider);
	}

	/* (non-Javadoc)
	 * Method declared on IAction.
	 */
	@Override
	public void run() {
		if (moveProjectAction.isEnabled()) {
			moveProjectAction.run();
			return;
		}

		super.run();
		List<?> destinations = getDestinations();
		if (destinations != null && destinations.isEmpty() == false) {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			List<IResource> resources = new ArrayList<>();
			Iterator<?> iterator = destinations.iterator();

			while (iterator.hasNext()) {
				IResource newResource = root.findMember((IPath) iterator.next());
				if (newResource != null)
					resources.add(newResource);
			}

			this.viewer.setSelection(new StructuredSelection(resources), true);
		}

	}

	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		moveProjectAction.selectionChanged(selection);
		return super.updateSelection(selection) || moveProjectAction.isEnabled();
	}

}
