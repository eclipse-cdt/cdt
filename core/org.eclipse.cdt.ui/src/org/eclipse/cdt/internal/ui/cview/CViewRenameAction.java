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

import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.RenameResourceAction;

/**
 * The ResourceNavigatorRenameAction is the rename action used by the
 * ResourceNavigator that also allows updating after rename.
 * @since 2.0
 */
public class CViewRenameAction extends RenameResourceAction {
	private TreeViewer viewer;

	/**
	 * Create a ResourceNavigatorRenameAction and use the tree of the supplied viewer
	 * for editing.
	 * @param shell Shell
	 * @param treeViewer TreeViewer
	 * @deprecated
	 */
	@Deprecated
	public CViewRenameAction(Shell shell, TreeViewer treeViewer) {
		super(shell, treeViewer.getTree());
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, ICHelpContextIds.RENAME_ACTION);
		this.viewer = treeViewer;
	}

	/**
	 * Create a ResourceNavigatorRenameAction and use the tree of the supplied viewer
	 * for editing.
	 * @param shellProvider a provider for a shell
	 * @param treeViewer TreeViewer
	 */
	public CViewRenameAction(IShellProvider shellProvider, TreeViewer treeViewer) {
		super(shellProvider, treeViewer.getTree());
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, ICHelpContextIds.RENAME_ACTION);
		this.viewer = treeViewer;
	}

	/* (non-Javadoc)
	 * Run the action to completion using the supplied path.
	 */
	@Override
	protected void runWithNewPath(IPath path, IResource resource) {
		IWorkspaceRoot root = resource.getProject().getWorkspace().getRoot();
		super.runWithNewPath(path, resource);
		if (this.viewer != null) {
			IResource newResource = root.findMember(path);
			if (newResource != null)
				this.viewer.setSelection(new StructuredSelection(newResource), true);
		}
	}

	/**
	* Handle the key release
	*/
	public void handleKeyReleased(KeyEvent event) {
		if (event.keyCode == SWT.F2 && event.stateMask == 0 && isEnabled()) {
			run();
		}
	}
}
