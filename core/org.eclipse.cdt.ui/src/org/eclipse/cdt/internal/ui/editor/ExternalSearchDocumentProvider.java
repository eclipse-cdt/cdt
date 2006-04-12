/**********************************************************************
 * Copyright (c) 2002, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.editor;

import org.eclipse.cdt.internal.ui.text.CTextTools;
import org.eclipse.cdt.internal.ui.util.ExternalEditorInput;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.editors.text.FileDocumentProvider;

public class ExternalSearchDocumentProvider extends FileDocumentProvider {
	
	public ExternalSearchDocumentProvider(){
		super();
	}

	/*
	 * @see AbstractDocumentProvider#createElementInfo(Object)
	 */
	protected ElementInfo createElementInfo(Object element) throws CoreException {
		
		if (element instanceof ExternalEditorInput) {
		
			ExternalEditorInput externalInput = (ExternalEditorInput) element;
			
			IDocument d = createDocument(externalInput);
			IAnnotationModel m= createExternalSearchAnnotationModel(externalInput);

			FileInfo info= new FileInfo(d, m, null);
			return info;
		}
		return null;
	}
	
	/**
	 * @param externalInput
	 * @return
	 */
	private IAnnotationModel createExternalSearchAnnotationModel(ExternalEditorInput externalInput) {
	
		IStorage storage = externalInput.getStorage();
		
		IProject projectToUseForMarker = null;
		
		IFile resourceFile = CUIPlugin.getWorkspace().getRoot().getFileForLocation(storage.getFullPath());
		
		if (resourceFile == null){
			IProject[] proj = CUIPlugin.getWorkspace().getRoot().getProjects();
			
			for (int i=0; i<proj.length; i++){
				if (proj[i].isOpen()){
					projectToUseForMarker = proj[i];
					break;
				}
			}
		}
		else {
			projectToUseForMarker = resourceFile.getProject();
		}
		
		if (projectToUseForMarker != null){
			ExternalSearchAnnotationModel model = new ExternalSearchAnnotationModel(projectToUseForMarker);
			return model;
		}
		return null;
	}

	/*
	 * @see AbstractDocumentProvider#createDocument(Object)
	 */
	protected IDocument createDocument(Object element) throws CoreException {
		IDocument document= super.createDocument(element);
		if (document != null){
			CTextTools textTools = CUIPlugin.getDefault().getTextTools();
			textTools.setupCDocument(document);
		}
		return document;
	}
}
