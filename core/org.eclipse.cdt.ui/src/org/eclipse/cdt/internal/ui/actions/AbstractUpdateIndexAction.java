/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    Anton Leherbauer (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.actions;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CUIPlugin;

public abstract class AbstractUpdateIndexAction implements IObjectActionDelegate {

	private ISelection fSelection;

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	@Override
	public void run(IAction action) {
		if (!(fSelection instanceof IStructuredSelection))
			return;

		IStructuredSelection cElements= SelectionConverter.convertSelectionToCElements(fSelection);
		Iterator<?> i= cElements.iterator();
		ArrayList<ICElement> tuSelection= new ArrayList<ICElement>();
		while (i.hasNext()) {
			Object o= i.next();
			if (o instanceof ICProject || o instanceof ICContainer || o instanceof ITranslationUnit) {
				tuSelection.add((ICElement) o);
			}
		}
		ICElement[] tuArray= tuSelection.toArray(new ICElement[tuSelection.size()]);

		try {
			CCorePlugin.getIndexManager().update(tuArray, getUpdateOptions());
		}
		catch (CoreException e) {
			CUIPlugin.log(e);
		}
	}

	/**
	 * Return the options to update the translation.
	 * @see IIndexManager#update(ICElement[], int)
	 * @since 4.0
	 */
	abstract protected int getUpdateOptions();

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		fSelection = selection;
	}
}
