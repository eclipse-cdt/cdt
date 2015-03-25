/*******************************************************************************
 * Copyright (c) 2008 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.actions;

import java.util.ResourceBundle;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.FindNextAction;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

import org.eclipse.cdt.internal.ui.text.CWordFinder;

/**
 * Select the word at current cursor location and find the next occurrence.
 */
public class FindWordAction extends TextEditorAction {

    public static final String FIND_WORD = "FindWord"; //$NON-NLS-1$

    private ITextViewer fViewer;
    private FindNextAction fFindNext;

	/**
	 * Creates new action.
	 */
    public FindWordAction(ResourceBundle bundle, String prefix, ITextEditor editor, ITextViewer viewer) {
		super(bundle, prefix, editor);
		fViewer = viewer;
		fFindNext = new FindNextAction(bundle, prefix, editor, true);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		ITextEditor editor = getTextEditor();
		if (editor == null )
			return;

		ISelectionProvider selectionProvider = editor.getSelectionProvider();
		if (selectionProvider == null)
			return;

		ITextSelection selection = (ITextSelection) selectionProvider.getSelection();
		if (selection == null || selection.isEmpty())
			return;

		IDocumentProvider docProvider = editor.getDocumentProvider();
		IEditorInput input = editor.getEditorInput();
		if (docProvider == null || input == null)
			return;

		IDocument document = docProvider.getDocument(input);
		if (document == null)
			return;
			
		IResource resource = (input).getAdapter(IResource.class);
		if (resource == null || !(resource instanceof IFile))
			return;

		// find the word at current cursor location
		int offset = selection.getOffset();
		IRegion region = CWordFinder.findWord(document, offset);
		if (region == null || region.getLength() == 0)
			return;

		// select the word and find next occurrence
		fViewer.setSelectedRange(region.getOffset(), region.getLength());
		fFindNext.run();
	}

}
