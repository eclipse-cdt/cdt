/*******************************************************************************
 * Copyright (c) 2007, 2013 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.actions;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.util.EditorUtility;

public abstract class AbstractUpdateIndexAction implements IObjectActionDelegate {
	private ISelection fSelection;

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	@Override
	public void run(IAction action) {
		if(!(fSelection instanceof IStructuredSelection) && !(fSelection instanceof ITextSelection)) {
			return;
		}
		ICProject[] projects = getSelectedCProjects();
		doRun(projects);
	}

	protected void doRun(ICProject[] projects) {
		try {
			CCorePlugin.getIndexManager().update(projects, getUpdateOptions());
		} catch (CoreException e) {
			CUIPlugin.log(e);
		}
	}

	/**
	 * Return the options to update the translation.
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
		ICProject[] project = getSelectedCProjects();
		return project.length > 0;
	}
	
	protected ICProject[] getSelectedCProjects() {
		ArrayList<ICProject> tuSelection= new ArrayList<ICProject>();
		if(fSelection instanceof IStructuredSelection) {
			IStructuredSelection resources = SelectionConverter.convertSelectionToResources(fSelection);
			for (Iterator<?> i= resources.iterator(); i.hasNext();) {
				Object o= i.next();
				if(o instanceof IResource) {
					ICProject cproject= CCorePlugin.getDefault().getCoreModel().create(((IResource)o).getProject());
					if(cproject != null) {
						tuSelection.add(cproject);
					}
				}
			}
		} else if(fSelection == null || fSelection instanceof ITextSelection) {
			IProject project = EditorUtility.getProjectForActiveEditor();
			if(project != null) {
				ICProject cproject= CCorePlugin.getDefault().getCoreModel().create(project);
				if(cproject != null) {
					tuSelection.add(cproject);
				}
			}
		}
		return tuSelection.toArray(new ICProject[tuSelection.size()]);
	}
}
