/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.utils;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class EclipseObjects {
	static public IWorkbenchPage getActivePage() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
	}
	
	static public IEditorPart getActiveEditor() {
		IEditorPart editor = null;

		IWorkbenchPage page = getActivePage();
		
		if (page.isEditorAreaVisible()
				&& page.getActiveEditor() != null
				&& page.getActiveEditor() instanceof TextEditor) {
			editor = page.getActiveEditor();
		}
		return editor;
	}
	
	static public IEditorPart getEditorForFile(IFile file) {
		IWorkbenchPage page = getActivePage();
		IEditorReference[] editors = page.getEditorReferences();
		for (int i = 0; i < editors.length; i++) {
			IEditorPart editor = editors[i].getEditor(false);
			if (editor instanceof CEditor) {
				CEditor edi = ((CEditor)editor);
				IResource resource = edi.getInputCElement().getResource();
				if (resource instanceof IFile) {
					if( (( IFile )resource).equals(file) ){
						return editor;
					}	
				}
				
			}
		}
		return null;
	}
	
	static public IFile getActiveFile(){
		IEditorInput edi = getActiveEditor().getEditorInput();

		IFile aFile = null;
		if(edi instanceof IFileEditorInput){
			aFile = ((IFileEditorInput)edi).getFile();
		}
		
		return aFile;
	}
	
	static public IDocument getActiveDocument() {
		return getDocument( getActiveEditor() );
	}
	
	static public IDocument getDocument() {
		ITextEditor txtEditor = ((ITextEditor)getActiveEditor());
		if(txtEditor == null)
			return null;
		IDocumentProvider prov = txtEditor.getDocumentProvider();
		return prov.getDocument(txtEditor.getEditorInput());
	}
	
	static public IDocument getDocument(IEditorPart editor) {
		ITextEditor txtEditor = ((ITextEditor)editor);
		IDocumentProvider prov = txtEditor.getDocumentProvider();
		return prov.getDocument(txtEditor.getEditorInput());
	}

	public static IWorkbenchWindow getActiveWindow() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	}

	public static IDocument getDocument(IFile file) {
		IEditorPart editor = getEditorForFile(file);
		return getDocument(editor);
	}
	
	static public IFile getFile(ISelection selection) {
		if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
			IFile file = getFile((IStructuredSelection)selection);
			return file;
		} else {
			return EclipseObjects.getActiveFile();
		}
	}

	static private IFile getFile(IStructuredSelection selection) {
		IFile file = null;
		Object o = selection.getFirstElement();
		
		if (o instanceof ICElement) {
			ICElement e= (ICElement) o;
		    IResource r= e.getUnderlyingResource();
		    if (r instanceof IFile) {
		        file= (IFile) r;
		    }
		} 
		
		return file;
	}
	
	public static IFile getFileForPathString(String path) {
		IPath ipath = new Path(path);
		return ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(ipath);
	}
	
	public static IFile getFile(IASTNode node){
		if(node == null)
			return null;
		return getFileForPathString(node.getFileLocation().getFileName());
	}
}
