/*******************************************************************************
 * Copyright (c) 2006, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.indexview;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
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
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.core.CCoreInternals;

import org.eclipse.cdt.internal.ui.util.EditorUtility;

/**
 * @author Doug Schaefer
 *
 */
public class OpenDefinitionAction extends IndexAction {

	public OpenDefinitionAction(IndexView view, TreeViewer viewer) {
		super(view, viewer, CUIPlugin.getResourceString("IndexView.openDefinition.name"));//$NON-NLS-1$
	}
	
	private IndexNode getBindingNode() {
		ISelection selection = viewer.getSelection();
		if (!(selection instanceof IStructuredSelection))
			return null;
		Object[] objs = ((IStructuredSelection)selection).toArray();
		if (objs.length == 1 && objs[0] instanceof IndexNode) {
			IndexNode node= (IndexNode) objs[0];
			if (node.fObject instanceof IIndexBinding) {
				return node;
			}
		}
		return null;
	}

	@Override
	public void run() {
		IndexNode bindingNode= getBindingNode();
		if (bindingNode == null) {
			return;
		}
		try {
			ICProject cproject= bindingNode.getProject();
			if (cproject != null) {
				IIndex index= CCorePlugin.getIndexManager().getIndex(cproject);
				if (!openDefinition(cproject, bindingNode, index)) {
					index= CCorePlugin.getIndexManager().getIndex(CoreModel.getDefault().getCModel().getCProjects());
					openDefinition(cproject, bindingNode, index);
				}
			}
		}
		catch (CoreException e) {
			CUIPlugin.log(e);
		} 
		catch (InterruptedException e) {
		}
	}

	private boolean openDefinition(ICProject cproject, IndexNode bindingNode, IIndex index) 
			throws InterruptedException, CoreException, CModelException, PartInitException {
		index.acquireReadLock();
		try {
			if (indexView.getLastWriteAccess(cproject) != CCoreInternals.getPDOMManager().getPDOM(cproject).getLastWriteAccess()) {
				return true;
			}
			IIndexName[] defs= index.findDefinitions((IIndexBinding) bindingNode.fObject);
			if (defs.length > 0) {
				showInEditor(defs[0]);
				return true;
			}
			defs= index.findDeclarations((IIndexBinding) bindingNode.fObject);
			if (defs.length > 0) {
				showInEditor(defs[0]);
				return true;
			}
		} finally {
			index.releaseReadLock();
		}
		return false;
	}

	private void showInEditor(IIndexName name) throws CModelException, PartInitException, CoreException {
		IPath path = IndexLocationFactory.getPath(name.getFile().getLocation());
		if(path!=null) {
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
					CUIPlugin.log(e);
				}
			}
		}
	}
	
	@Override
	public boolean valid() {
		return getBindingNode() != null;
	}
}
