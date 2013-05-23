/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.actions;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;

import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.internal.ui.util.SelectionUtil;

public class RebuildIndexAction implements IObjectActionDelegate, IWorkbenchWindowActionDelegate {
	private ISelection fSelection;
	private boolean isEnabled;

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	@Override
	public void run(IAction action) {	
		if(!(fSelection instanceof IStructuredSelection) && !(fSelection instanceof ITextSelection)) {
			return;
		}
		ArrayList<ICElement> tuSelection= new ArrayList<ICElement>();
		if(fSelection instanceof IStructuredSelection) {
			IStructuredSelection cElements= SelectionConverter.convertSelectionToCElements(fSelection);
			for (Iterator<?> i= cElements.iterator(); i.hasNext();) {
				Object o= i.next();
				if (o instanceof ICProject || o instanceof ICContainer || o instanceof ITranslationUnit) {
					tuSelection.add((ICElement) o);
				}
			}
		} else if(fSelection instanceof ITextSelection) {
			IProject project = EditorUtility.getProjectForActiveEditor();
			if(project != null) {
				ICProject cproject= CCorePlugin.getDefault().getCoreModel().create(project);
				if(cproject != null) {
					tuSelection.add(cproject);
				}
			}
		}
		ICElement[] tuArray= tuSelection.toArray(new ICElement[tuSelection.size()]);
		for (ICElement elem : tuArray) {
			if(elem instanceof ICProject) {
				CCorePlugin.getIndexManager().reindex((ICProject) elem);
			}
		}
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(ISelection selection) {
		fSelection= selection;
		isEnabled = false;
		if(selection == null || selection instanceof ITextSelection) {
			IProject project = EditorUtility.getProjectForActiveEditor();
			if(project != null) {
				isEnabled = CoreModel.hasCNature(project);
			}
		} else if(selection instanceof IStructuredSelection) {
			Object selectedElement = ((IStructuredSelection)selection).getFirstElement();
			if(selectedElement instanceof IProject) {
				isEnabled = CoreModel.hasCNature((IProject)selectedElement) && ((IProject)selectedElement).isOpen();
			} else if(selectedElement instanceof ITranslationUnit) {
				isEnabled = true;
			}
		}
	}

	@Override
	public void init(IWorkbenchWindow window) {
	}
	
	@Override
	public void dispose() {
	}

	/**
	 * @return {@code true} if the action is enabled or {@code false} otherwise.
	 */
	public boolean isEnabled() {
		selectionChanged(SelectionUtil.getActiveSelection());
		return isEnabled;
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		selectionChanged(selection);
	}
}
