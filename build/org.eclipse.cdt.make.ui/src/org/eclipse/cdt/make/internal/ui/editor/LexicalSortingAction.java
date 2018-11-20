/*******************************************************************************
 * Copyright (c) 2002, 2013 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/

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
		setDisabledImageDescriptor(MakeUIImages.getImageDescriptor(MakeUIImages.IMG_DTOOL_ALPHA_SORTING));
		setImageDescriptor(MakeUIImages.getImageDescriptor(MakeUIImages.IMG_ETOOL_ALPHA_SORTING));

		fTreeViewer = treeViewer;
		fSorter = new LexicalMakefileSorter();
		boolean checked = MakeUIPlugin.getDefault().getDialogSettings().getBoolean(DIALOG_STORE_KEY);
		valueChanged(checked, false);
	}

	@Override
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
		@Override
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
