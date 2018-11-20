/*******************************************************************************
 * Copyright (c) 2008, 2012 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.utils;

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

/**
 * A collection of helper methods to interact with the workbench's IDocuments and IFiles
 */
public class EclipseObjects {

	static public IWorkbenchPage getActivePage() {
		return getActiveWindow().getActivePage();
	}

	/**
	 * @return the active, visible TextEditor
	 */
	static public IEditorPart getActiveEditor() {
		IWorkbenchPage page = getActivePage();

		IEditorPart activeEditor = page.getActiveEditor();
		if (page.isEditorAreaVisible() && activeEditor instanceof TextEditor) {
			return activeEditor;
		}
		return null;
	}

	/**
	 * Goes through all open editors to find the one with the specified file.
	 *
	 * @param file to search for
	 * @return the editor or null
	 */
	static public IEditorPart getEditorForFile(IFile file) {
		IWorkbenchPage page = getActivePage();
		IEditorReference[] editors = page.getEditorReferences();
		for (IEditorReference editor2 : editors) {
			IEditorPart editor = editor2.getEditor(false);
			if (editor instanceof CEditor) {
				CEditor edi = ((CEditor) editor);
				IResource resource = edi.getInputCElement().getResource();
				if (resource instanceof IFile) {
					if (((IFile) resource).equals(file)) {
						return editor;
					}
				}

			}
		}
		return null;
	}

	/**
	 * @return the file from the active editor
	 */
	static public IFile getActiveFile() {
		IEditorInput editorInput = getActiveEditor().getEditorInput();

		if (editorInput instanceof IFileEditorInput) {
			return ((IFileEditorInput) editorInput).getFile();
		}

		return null;
	}

	/**
	 * @return the document from the currently active editor
	 */
	static public IDocument getActiveDocument() {
		return getDocument(getActiveEditor());
	}

	/**
	 * @return the document opened in the editor
	 */
	static public IDocument getDocument(IEditorPart editor) {
		ITextEditor txtEditor = ((ITextEditor) editor);
		IDocumentProvider prov = txtEditor.getDocumentProvider();
		return prov.getDocument(txtEditor.getEditorInput());
	}

	public static IWorkbenchWindow getActiveWindow() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	}

	/**
	 * @return get the document that corresponds to the file
	 */
	public static IDocument getDocument(IFile file) {
		IEditorPart editor = getEditorForFile(file);
		return getDocument(editor);
	}

	/**
	 * @return return the file that contains the selection or the
	 * active file if there is no present selection
	 */
	static public IFile getFile(ISelection selection) {
		if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
			IFile file = getFile((IStructuredSelection) selection);
			return file;
		}
		return getActiveFile();
	}

	static private IFile getFile(IStructuredSelection selection) {
		Object o = selection.getFirstElement();

		if (o instanceof ICElement) {
			ICElement e = (ICElement) o;
			IResource r = e.getUnderlyingResource();
			if (r instanceof IFile) {
				return (IFile) r;
			}
		}

		return null;
	}

	/**
	 * @return the file at the specified path string
	 */
	public static IFile getFileForPathString(String filePath) {
		IPath path = new Path(filePath);
		return ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
	}
}
