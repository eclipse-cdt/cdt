/*******************************************************************************
 * Copyright (c) 2006, 2008 Siemens AG and others.
 * All rights reserved. This content and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Norbert Ploett (Siemens AG)
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.util;

import java.io.File;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionDelegate;
import org.eclipse.ui.ide.IDE;

import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.core.model.CModelManager;

public class OpenExternalProblemAction extends ActionDelegate implements IObjectActionDelegate {
	
	IStructuredSelection selection ;

	public OpenExternalProblemAction() {
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		
	}

	@Override
	public void runWithEvent(IAction action, Event event) {
		Object object = selection.getFirstElement();
		if (object instanceof IMarker) {
			try {
				IMarker marker = (IMarker) object;
				Object attributeObject = marker.getAttribute(ICModelMarker.C_MODEL_MARKER_EXTERNAL_LOCATION);
				if (attributeObject instanceof String)  {
					String externalLocation = (String) attributeObject;
					IPath externalPath = new Path(externalLocation);
					
					File file = externalPath.toFile() ;
					if (!file.canRead()) {
						MessageBox errorMsg = new MessageBox(CUIPlugin.getActiveWorkbenchShell(), SWT.ICON_ERROR | SWT.OK);
						errorMsg.setText(Messages.OpenExternalProblemAction_ErrorOpeningFile);
						errorMsg.setMessage(NLS.bind(Messages.OpenExternalProblemAction_CannotReadExternalLocation, externalPath));
						errorMsg.open();
						return;
					}
					
					IEditorPart editor = EditorUtility.openInEditor(externalPath, getCProject(marker));
					if (editor != null) {
						IDE.gotoMarker(editor, marker);
					}
				}
			} catch (CoreException e) {
				CUIPlugin.log(e.getStatus());
			}
		}
	}
	
	private ICProject getCProject(IMarker marker)  {
		ICProject cproject = null ;
		
		if (marker.getResource() instanceof IProject) {
			IProject project = (IProject) marker.getResource();
			cproject = CModelManager.getDefault().create(project);
		}
		return cproject ;
	}
	
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		boolean enable = false;
		if (selection instanceof IStructuredSelection) {
			Object object = ((IStructuredSelection) selection).getFirstElement();
			if (object instanceof IMarker) {
				try {
					IMarker marker = (IMarker) object;
					if ((marker.isSubtypeOf(ICModelMarker.C_MODEL_PROBLEM_MARKER))
							&&(null!=marker.getAttribute(ICModelMarker.C_MODEL_MARKER_EXTERNAL_LOCATION, null))) {
							enable = true;
					}
					this.selection = (IStructuredSelection)selection;
					action.setEnabled(enable);
				} catch (CoreException e) {
				}
			}
		}
	}
	
}
