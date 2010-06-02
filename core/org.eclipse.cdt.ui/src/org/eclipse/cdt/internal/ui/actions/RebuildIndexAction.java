/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.ui.actions;

import java.util.Iterator;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICProject;

public class RebuildIndexAction implements IObjectActionDelegate {

	private ISelection fSelection;

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}
	
	public void run(IAction action) {
		IStructuredSelection cElements= SelectionConverter.convertSelectionToCElements(fSelection);
		for (Iterator<?> i = cElements.iterator(); i.hasNext();) {
			Object elem = i.next();
			if (elem instanceof ICProject) {
				CCorePlugin.getIndexManager().reindex((ICProject) elem);
			}
		}
	}
	
	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		fSelection= selection;
	}
}
