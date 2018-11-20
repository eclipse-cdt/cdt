/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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
 *     Red Hat Inc. - convert to use with Automake editor
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.editors;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.autotools.ui.MakeUIImages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerComparator;

public class LexicalSortingAction extends Action {

	private static final String ACTION_NAME = "LexicalSortingAction"; //$NON-NLS-1$
	private static final String DIALOG_STORE_KEY = ACTION_NAME + ".sort"; //$NON-NLS-1$

	private LexicalCSorter fSorter;
	private TreeViewer fTreeViewer;

	public LexicalSortingAction() {
		super(CUIPlugin.getResourceString(ACTION_NAME + ".label")); //$NON-NLS-1$

		setDescription(CUIPlugin.getResourceString(ACTION_NAME + ".description")); //$NON-NLS-1$
		setToolTipText(CUIPlugin.getResourceString(ACTION_NAME + ".tooltip")); //$NON-NLS-1$

		MakeUIImages.setImageDescriptors(this, MakeUIImages.T_TOOL, MakeUIImages.IMG_TOOLS_ALPHA_SORTING);

		fSorter = new LexicalCSorter();
	}

	public void setTreeViewer(TreeViewer treeViewer) {
		fTreeViewer = treeViewer;
		boolean checked = CUIPlugin.getDefault().getDialogSettings().getBoolean(DIALOG_STORE_KEY);
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
			CUIPlugin.getDefault().getDialogSettings().put(DIALOG_STORE_KEY, on);
		}
	}

	private static class LexicalCSorter extends ViewerComparator {
		@Override
		public int category(Object obj) {
			if (obj instanceof ICElement) {
				ICElement elem = (ICElement) obj;
				switch (elem.getElementType()) {
				case ICElement.C_MACRO:
					return 1;
				case ICElement.C_INCLUDE:
					return 2;

				case ICElement.C_CLASS:
					return 3;
				case ICElement.C_STRUCT:
					return 4;
				case ICElement.C_UNION:
					return 5;

				case ICElement.C_FIELD:
					return 6;
				case ICElement.C_FUNCTION:
					return 7;
				}

			}
			return 0;
		}
	}

}
