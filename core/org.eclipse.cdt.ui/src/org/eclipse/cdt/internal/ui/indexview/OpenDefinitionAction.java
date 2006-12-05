/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.indexview;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.util.EditorUtility;

/**
 * @author Doug Schaefer
 *
 */
public class OpenDefinitionAction extends IndexAction {

	public OpenDefinitionAction(TreeViewer viewer) {
		super(viewer, CUIPlugin.getResourceString("IndexView.openDefinition.name"));//$NON-NLS-1$
	}
	
	public void run() {
		ISelection selection = viewer.getSelection();
		if (!(selection instanceof IStructuredSelection))
			return;
		
		try {
			IIndex index= CCorePlugin.getIndexManager().getIndex(CoreModel.getDefault().getCModel().getCProjects());
			Object[] objs = ((IStructuredSelection)selection).toArray();
			for (int i = 0; i < objs.length; ++i) {
				if (!(objs[i] instanceof IIndexBinding))
					continue;
			
				index.acquireReadLock();
				try {
					IIndexBinding binding = (IIndexBinding)objs[i];
					IIndexName[] defs= index.findDefinitions(binding);
					for (int j = 0; j < defs.length; j++) {
						IIndexName name = defs[j];
						showInEditor(name);
					}
				} finally {
					index.releaseReadLock();
				}
			}
		}
		catch (CoreException e) {
			CUIPlugin.getDefault().log(e);
		} 
		catch (InterruptedException e) {
		}
	}

	private void showInEditor(IIndexName name) throws CModelException, PartInitException {
		IPath path = new Path(name.getFileName());
		IEditorPart editor = EditorUtility.openInEditor(path, null);
		if (editor != null && editor instanceof ITextEditor) {
			ITextEditor textEditor = (ITextEditor)editor;
			int nodeOffset = name.getNodeOffset();
			int nodeLength = name.getNodeLength();
			try {
				if (nodeLength == -1) {
					// This means the offset is actually a line number
					IDocument document = textEditor.getDocumentProvider().getDocument(editor.getEditorInput());
					nodeOffset = document.getLineOffset(nodeOffset);
					nodeLength = document.getLineLength(nodeOffset);
				} 
				textEditor.selectAndReveal(nodeOffset, nodeLength);
			} catch (BadLocationException e) {
				CUIPlugin.getDefault().log(e);
			}
		}
	}
	
	public boolean valid() {
		ISelection selection = viewer.getSelection();
		if (!(selection instanceof IStructuredSelection))
			return false;
		Object[] objs = ((IStructuredSelection)selection).toArray();
		for (int i = 0; i < objs.length; ++i)
			if (objs[i] instanceof IIndexBinding)
				return true;
		return false;
	}

}
