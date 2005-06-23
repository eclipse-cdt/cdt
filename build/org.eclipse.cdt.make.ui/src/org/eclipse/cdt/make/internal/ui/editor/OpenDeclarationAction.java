/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.ui.editor;

import org.eclipse.cdt.core.resources.FileStorage;
import org.eclipse.cdt.internal.ui.util.ExternalEditorInput;
import org.eclipse.cdt.make.core.makefile.IDirective;
import org.eclipse.cdt.make.core.makefile.IMakefile;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.internal.ui.text.WordPartDetector;
import org.eclipse.cdt.make.ui.IWorkingCopyManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

public class OpenDeclarationAction extends TextEditorAction {

	public OpenDeclarationAction() {
		this(null);
	}

	public OpenDeclarationAction(ITextEditor editor) {
		super(MakeUIPlugin.getDefault().getResourceBundle(), "OpenDeclarationAction.", editor); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		ITextEditor editor = getTextEditor();
		if (editor == null) {
			return;
		}
		ISelectionProvider provider = editor.getSelectionProvider();
		if (provider == null) {
			return;
		}
		IDirective[] directives = null;
		IWorkingCopyManager fManager = MakeUIPlugin.getDefault().getWorkingCopyManager();
		IMakefile makefile = fManager.getWorkingCopy(editor.getEditorInput());
		if (makefile != null) {
			IDocumentProvider prov = editor.getDocumentProvider();
			IDocument doc = prov.getDocument(editor.getEditorInput());
			try {
				ITextSelection textSelection = (ITextSelection) provider.getSelection();
				int offset = textSelection.getOffset();
				WordPartDetector wordPart = new WordPartDetector(doc, textSelection.getOffset());
				String name = wordPart.toString();
				if (WordPartDetector.inMacro(doc, offset)) {
					directives = makefile.getMacroDefinitions(name);
					if (directives.length == 0) {
						directives = makefile.getBuiltinMacroDefinitions(name);
					}
				} else {
					directives = makefile.getTargetRules(name);
				}
				if (directives != null && directives.length > 0) {
					openInEditor(directives[0]);
				}
			} catch (Exception x) {
				//
			}
		}
	}

	private static IEditorPart openInEditor(IDirective directive) throws PartInitException {
		String filename = directive.getFileName();
		IPath path = new Path(filename);
		IFile file = MakeUIPlugin.getWorkspace().getRoot().getFileForLocation(path);
		if (file != null) {
			IWorkbenchPage p = MakeUIPlugin.getActivePage();
			if (p != null) {
				IEditorPart editorPart = IDE.openEditor(p, file, true);
				if (editorPart instanceof MakefileEditor) {
					((MakefileEditor)editorPart).setSelection(directive, true);
				}
				return editorPart;
			}
		} else {
			// External file
			IStorage storage = new FileStorage(path);
			IStorageEditorInput input = new ExternalEditorInput(storage);
			IWorkbenchPage p = MakeUIPlugin.getActivePage();
			if (p != null) {
				String editorID = "org.eclipse.cdt.make.editor"; //$NON-NLS-1$
				IEditorPart editorPart = IDE.openEditor(p, input, editorID, true);
				if (editorPart instanceof MakefileEditor) {
					((MakefileEditor)editorPart).setSelection(directive, true);
				}
				return editorPart;
			}
			
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.ui.actions.SelectionDispatchAction#selectionChanged(org.eclipse.jface.text.ITextSelection)
	 */
	//public void selectionChanged(ITextSelection selection) {
		//setEnabled(fEditor != null);
	//}
}
