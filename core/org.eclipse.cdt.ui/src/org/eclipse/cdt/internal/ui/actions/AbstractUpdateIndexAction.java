/*******************************************************************************
 * Copyright (c) 2007, 2014 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.actions;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public abstract class AbstractUpdateIndexAction implements IObjectActionDelegate {
	private ISelection fSelection;

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	@Override
	public void run(IAction action) {
		if (!(fSelection instanceof IStructuredSelection) && !(fSelection instanceof ITextSelection)) {
			return;
		}
		ICElement[] elements = getSelectedCElements();
		doRun(elements);
	}

	protected void doRun(ICElement[] elements) {
		try {
			CCorePlugin.getIndexManager().update(elements, getUpdateOptions());
		} catch (CoreException e) {
			CUIPlugin.log(e);
		}
	}

	/**
	 * Returns the options to update the translation.
	 * @see IIndexManager#update(ICElement[], int)
	 * @since 4.0
	 */
	abstract protected int getUpdateOptions();

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		fSelection = selection;
	}

	public boolean isEnabledFor(ISelection selection) {
		selectionChanged(null, selection);
		ICElement[] elements = getSelectedCElements();
		return elements.length > 0;
	}

	protected ICElement[] getSelectedCElements() {
		ArrayList<ICElement> tuSelection = new ArrayList<>();
		if (fSelection instanceof IStructuredSelection) {
			IStructuredSelection resources = SelectionConverter.convertSelectionToResources(fSelection);
			for (Iterator<?> i = resources.iterator(); i.hasNext();) {
				Object o = i.next();
				if (o instanceof IResource) {
					ICElement celement = CCorePlugin.getDefault().getCoreModel().create((IResource) o);
					if (celement != null) {
						tuSelection.add(celement);
					}
				}
			}
		} else if (fSelection == null || fSelection instanceof ITextSelection) {
			IProject project = EditorUtility.getProjectForActiveEditor();
			if (project != null) {
				ICProject cproject = CCorePlugin.getDefault().getCoreModel().create(project);
				if (cproject != null) {
					tuSelection.add(cproject);
				}
			}
		}
		return tuSelection.toArray(new ICElement[tuSelection.size()]);
	}
}
