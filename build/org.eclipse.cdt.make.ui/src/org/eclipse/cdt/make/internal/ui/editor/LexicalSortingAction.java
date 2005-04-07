/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 *
 * Contributors:
 * QNX Software Systems - initial implementation
***********************************************************************/

package org.eclipse.cdt.make.internal.ui.editor;

import org.eclipse.cdt.make.core.makefile.IDirective;
import org.eclipse.cdt.make.core.makefile.IInferenceRule;
import org.eclipse.cdt.make.core.makefile.IMacroDefinition;
import org.eclipse.cdt.make.core.makefile.ISpecialRule;
import org.eclipse.cdt.make.internal.ui.MakeUIImages;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;

public class LexicalSortingAction extends Action {

	private static final String ACTION_NAME = "LexicalSortingAction"; //$NON-NLS-1$
	private static final String DIALOG_STORE_KEY = ACTION_NAME + ".sort"; //$NON-NLS-1$

	private LexicalMakefileSorter fSorter;
	private TreeViewer fTreeViewer;

	public LexicalSortingAction(TreeViewer treeViewer) {
		super(MakeUIPlugin.getResourceString(ACTION_NAME + ".label")); //$NON-NLS-1$

		setDescription(MakeUIPlugin.getResourceString(ACTION_NAME + ".description")); //$NON-NLS-1$
		setToolTipText(MakeUIPlugin.getResourceString(ACTION_NAME + ".tooltip")); //$NON-NLS-1$
		MakeUIImages.setImageDescriptors(this, MakeUIImages.T_TOOL, MakeUIImages.IMG_TOOLS_ALPHA_SORTING);

		fTreeViewer = treeViewer;
		fSorter = new LexicalMakefileSorter();
		boolean checked = MakeUIPlugin.getDefault().getDialogSettings().getBoolean(DIALOG_STORE_KEY);
		valueChanged(checked, false);
	}

	public void run() {
		valueChanged(isChecked(), true);
	}

	private void valueChanged(boolean on, boolean store) {
		setChecked(on);
		fTreeViewer.setSorter(on ? fSorter : null);

		String key = ACTION_NAME + ".tooltip" + (on ? ".on" : ".off"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		setToolTipText(MakeUIPlugin.getResourceString(key));
		if (store) {
			MakeUIPlugin.getDefault().getDialogSettings().put(DIALOG_STORE_KEY, on);
		}
	}

	private class LexicalMakefileSorter extends ViewerSorter {

		public boolean isSorterProperty(Object element, Object property) {
			return true;
		}

		public int category(Object obj) {
			if (obj instanceof IDirective) {
				IDirective directive = (IDirective) obj;
				if (directive instanceof IMacroDefinition) {
					return 0;
				} else if (directive instanceof ISpecialRule) {
					return 1;
				} else if (directive instanceof IInferenceRule) {
					return 2;
				}
			}
			return 3;
		}
	}

}
