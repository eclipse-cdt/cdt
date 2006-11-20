/*******************************************************************************
 * Copyright (c) 2006 Siemens AG.
 * All rights reserved. This content and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Norbert Ploett - Initial implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.util;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionDelegate;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.resources.FileStorage;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.core.model.CModelManager;

import org.eclipse.cdt.internal.ui.editor.ExternalSearchEditor;

public class OpenExternalProblemAction extends ActionDelegate implements IObjectActionDelegate  
 {
	
	IStructuredSelection selection ;

	public OpenExternalProblemAction() {
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		
	}

	public void runWithEvent(IAction action, Event event) {
		Object object = selection.getFirstElement();
		if (object instanceof IMarker) {
			try {
				IMarker marker = (IMarker) object;
				Object attributeObject = marker.getAttribute(ICModelMarker.C_MODEL_MARKER_EXTERNAL_LOCATION);
				if (attributeObject instanceof String)  {
					String externalLocation = (String) attributeObject ;
					IPath externalPath = new Path(externalLocation);
					IEditorPart editor = null ;
					// Try to open a C editor with the project and the path
					ICProject cproject = getCProject(marker);
					if (null!=cproject) {
						ITranslationUnit tu = CoreModel.getDefault()
								.createTranslationUnitFrom(cproject,
										externalPath);
						if (null!=tu)  {
							editor = EditorUtility.openInEditor(tu);
						}
					}  else  {
						// Open in plain external editor
						IEditorInput input = new ExternalEditorInput(new FileStorage(externalPath), marker.getResource());
						editor = CUIPlugin.getActivePage().openEditor(input, ExternalSearchEditor.EDITOR_ID);
					}
					if (editor instanceof ITextEditor) {
						int lineNumber = marker.getAttribute(IMarker.LINE_NUMBER, 0);
						int currentOffset = 0 ;
						int currentLength = 0;
						ITextEditor textEditor = (ITextEditor) editor;
						IEditorInput ediinput = textEditor.getEditorInput();
						IDocument document = textEditor.getDocumentProvider().getDocument(ediinput);
						try {
							currentOffset = document.getLineOffset(lineNumber-1);
						} catch (BadLocationException e) {
						}
						textEditor.selectAndReveal(currentOffset, currentLength);
					}
				}
			} catch (CoreException e) {
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
