/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
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
 *     QNX Software System
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.ui.CElementSorter;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerComparator;

public class LexicalSortingAction extends Action {

	private static final String ACTION_NAME = "LexicalSortingAction"; //$NON-NLS-1$

	private final ViewerComparator fSorter;
	private final TreeViewer fTreeViewer;
	private final String fStoreKey;

	public LexicalSortingAction(TreeViewer treeViewer) {
		this(treeViewer, ".sort"); //$NON-NLS-1$
	}

	public LexicalSortingAction(TreeViewer treeViewer, String storeKeySuffix) {
		super(CUIPlugin.getResourceString(ACTION_NAME + ".label")); //$NON-NLS-1$

		setDescription(CUIPlugin.getResourceString(ACTION_NAME + ".description")); //$NON-NLS-1$
		setToolTipText(CUIPlugin.getResourceString(ACTION_NAME + ".tooltip")); //$NON-NLS-1$

		CPluginImages.setImageDescriptors(this, CPluginImages.T_LCL, CPluginImages.IMG_ALPHA_SORTING);

		fTreeViewer = treeViewer;
		fSorter = new CElementSorter();
		fStoreKey = ACTION_NAME + storeKeySuffix;

		boolean checked = CUIPlugin.getDefault().getDialogSettings().getBoolean(fStoreKey);
		valueChanged(checked, false);
	}

	@Override
	public void run() {
		valueChanged(isChecked(), true);
	}

	private void valueChanged(boolean on, boolean store) {
		setChecked(on);
		fTreeViewer.setComparator(on ? fSorter : null);

		String key = ACTION_NAME + ".tooltip" + (on ? ".on" : ".off"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		setToolTipText(CUIPlugin.getResourceString(key));

		if (store) {
			CUIPlugin.getDefault().getDialogSettings().put(fStoreKey, on);
		}
	}

}
