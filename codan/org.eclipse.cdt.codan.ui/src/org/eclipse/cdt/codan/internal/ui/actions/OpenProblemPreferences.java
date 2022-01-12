/*******************************************************************************
 * Copyright (c) 2010 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.ui.actions;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.model.IProblemProfile;
import org.eclipse.cdt.codan.internal.core.model.CodanProblem;
import org.eclipse.cdt.codan.internal.core.model.CodanProblemMarker;
import org.eclipse.cdt.codan.internal.ui.dialogs.CustomizeProblemDialog;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class OpenProblemPreferences implements IObjectActionDelegate {
	private ISelection selection;
	private IWorkbenchPart targetPart;

	public OpenProblemPreferences() {
	}

	@Override
	public void run(IAction action) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;
			ArrayList<IProblem> list = new ArrayList<>();
			IResource resource = null;
			for (Iterator<?> iterator = ss.iterator(); iterator.hasNext();) {
				Object el = iterator.next();
				if (el instanceof IMarker) {
					IMarker marker = (IMarker) el;
					String id = CodanProblemMarker.getProblemId(marker);
					if (id == null)
						return;
					resource = marker.getResource();
					IProblemProfile profile = CodanProblemMarker.getProfile(resource);
					CodanProblem problem = ((CodanProblem) profile.findProblem(id));
					list.add(problem);
				}
			}
			new CustomizeProblemDialog(targetPart.getSite().getShell(), list.toArray(new IProblem[list.size()]),
					resource).open();
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.targetPart = targetPart;
	}
}
