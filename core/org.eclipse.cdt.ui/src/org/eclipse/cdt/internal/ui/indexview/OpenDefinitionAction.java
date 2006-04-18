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

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.resources.FileStorage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

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
		
		Object[] objs = ((IStructuredSelection)selection).toArray();
		for (int i = 0; i < objs.length; ++i) {
			if (!(objs[i] instanceof PDOMBinding))
				continue;
			
			try {
				PDOMBinding binding = (PDOMBinding)objs[i];
				PDOMName name = binding.getFirstDefinition();
				if (name == null)
					name = binding.getFirstDeclaration();
				if (name == null)
					continue;
				
				IASTFileLocation location = name.getFileLocation();
				IPath path = new Path(location.getFileName());
				Object input = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
				if (input == null)
					input = new FileStorage(path);

				IEditorPart editor = EditorUtility.openInEditor(input);
				if (editor != null && editor instanceof ITextEditor) {
					ITextEditor textEditor = (ITextEditor)editor;
					int nodeOffset = location.getNodeOffset();
					int nodeLength = location.getNodeLength();
					int offset;
					int length;
					if (nodeLength == -1) {
						// This means the offset is actually a line number
						try {
							IDocument document = textEditor.getDocumentProvider().getDocument(editor.getEditorInput());
							offset = document.getLineOffset(nodeOffset);
							length = document.getLineLength(nodeOffset);
						} catch (BadLocationException e) {
							CUIPlugin.getDefault().log(e);
							return;
						}
					} else {
						offset = nodeOffset;
						length = nodeLength;
					}
					
					textEditor.selectAndReveal(offset, length);
				}
			} catch (CoreException e) {
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
			if (objs[i] instanceof PDOMBinding)
				return true;
		return false;
	}

}
