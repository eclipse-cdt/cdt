/*******************************************************************************
 * Copyright (c) 2007, 2015 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.search.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.search.CSearchMessages;
import org.eclipse.cdt.internal.ui.search.CSearchUtil;
import org.eclipse.cdt.internal.ui.util.Messages;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IWorkingSetSelectionDialog;

public abstract class FindInWorkingSetAction extends FindAction {

	private IWorkingSet[] fWorkingSets;
	private String scopeDescription = ""; //$NON-NLS-1$

	public FindInWorkingSetAction(CEditor editor, String label, String tooltip, IWorkingSet[] workingSets) {
		super(editor);
		setText(label);
		setToolTipText(tooltip);
		fWorkingSets = workingSets;
	}

	public FindInWorkingSetAction(IWorkbenchSite site, String label, String tooltip, IWorkingSet[] workingSets) {
		super(site);
		setText(label);
		setToolTipText(tooltip);
		fWorkingSets = workingSets;
	}

	@Override
	final public void run() {
		IWorkingSet[] initial = fWorkingSets;
		if (fWorkingSets == null) {
			fWorkingSets = askForWorkingSets();
		}
		if (fWorkingSets != null) {
			scopeDescription = Messages.format(CSearchMessages.WorkingSetScope, CSearchUtil.toString(fWorkingSets));
			super.run();
		}
		fWorkingSets = initial;
	}

	@Override
	final protected String getScopeDescription() {
		return scopeDescription;
	}

	@Override
	final protected ICElement[] getScope() {
		if (fWorkingSets == null) {
			return ICElement.EMPTY_ARRAY;
		}
		List<ICElement> scope = new ArrayList<>();
		for (int i = 0; i < fWorkingSets.length; ++i) {
			IAdaptable[] elements = fWorkingSets[i].getElements();
			for (int j = 0; j < elements.length; ++j) {
				ICElement element = elements[j].getAdapter(ICElement.class);
				if (element != null)
					scope.add(element);
			}
		}

		return scope.toArray(new ICElement[scope.size()]);
	}

	private IWorkingSet[] askForWorkingSets() {
		IWorkingSetManager wsm = PlatformUI.getWorkbench().getWorkingSetManager();
		IWorkingSetSelectionDialog dlg = wsm.createWorkingSetSelectionDialog(getSite().getShell(), true);
		IWorkingSet[] mru = wsm.getRecentWorkingSets();
		if (mru != null && mru.length > 0) {
			dlg.setSelection(new IWorkingSet[] { mru[0] });
		}
		if (dlg.open() == Window.OK) {
			mru = dlg.getSelection();
			if (mru != null && mru.length == 1) {
				wsm.addRecentWorkingSet(mru[0]);
			}
			return mru;
		}
		return null;
	}
}
