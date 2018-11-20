/*******************************************************************************
 * Copyright (c) 2015, 2015 Andrew Gvozdev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Andrew Gvozdev - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.callhierarchy;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * Action to remove items from the C++ Call Hierarchy View.
 */
public class CHRemoveFromView extends Action {
	private CHViewPart fView;

	/**
	 * Constructs a Remove From View action.
	 *
	 * @param view the Call Hierarchy view
	 */
	public CHRemoveFromView(CHViewPart view) {
		super(CHMessages.CHViewPart_RemoveFromView_label);
		fView = view;
		ISharedImages images = PlatformUI.getWorkbench().getSharedImages();
		setImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_ELCL_REMOVE));
		setDisabledImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE_DISABLED));
	}

	@Override
	public void run() {
		TreeViewer tree = fView.getTreeViewer();
		ITreeSelection selection = (ITreeSelection) tree.getSelection();
		tree.setSelection(null); // should stay before removal
		tree.remove(selection.toArray());
	}
}
