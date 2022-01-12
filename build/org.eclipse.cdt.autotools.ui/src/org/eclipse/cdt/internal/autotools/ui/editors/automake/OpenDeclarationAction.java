/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Red Hat Inc. - convert to use with Automake editor
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.editors.automake;

import java.net.URI;

import org.eclipse.cdt.autotools.ui.AutotoolsUIPlugin;
import org.eclipse.cdt.core.resources.FileStorage;
import org.eclipse.cdt.internal.autotools.ui.MakeUIMessages;
import org.eclipse.cdt.make.core.makefile.IDirective;
import org.eclipse.cdt.make.core.makefile.IMakefile;
import org.eclipse.cdt.make.ui.IWorkingCopyManager;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IPath;
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
		super(MakeUIMessages.getResourceBundle(), "OpenDeclarationAction.", editor); //$NON-NLS-1$
	}

	@Override
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
		IWorkingCopyManager fManager = AutomakeEditorFactory.getDefault().getWorkingCopyManager();
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
		URI fileURI = directive.getMakefile().getFileURI();
		IPath path = URIUtil.toPath(fileURI);
		IFile file = AutotoolsUIPlugin.getWorkspace().getRoot().getFileForLocation(path);
		if (file != null) {
			IWorkbenchPage p = AutotoolsUIPlugin.getActivePage();
			if (p != null) {
				IEditorPart editorPart = IDE.openEditor(p, file, true);
				if (editorPart instanceof MakefileEditor) {
					((MakefileEditor) editorPart).setSelection(directive, true);
				}
				return editorPart;
			}
		} else {
			// External file
			IStorage storage = new FileStorage(path);
			IStorageEditorInput input = new ExternalEditorInput(storage);
			IWorkbenchPage p = AutotoolsUIPlugin.getActivePage();
			if (p != null) {
				String editorID = "org.eclipse.cdt.make.editor"; //$NON-NLS-1$
				IEditorPart editorPart = IDE.openEditor(p, input, editorID, true);
				if (editorPart instanceof MakefileEditor) {
					((MakefileEditor) editorPart).setSelection(directive, true);
				}
				return editorPart;
			}

		}
		return null;
	}
}
