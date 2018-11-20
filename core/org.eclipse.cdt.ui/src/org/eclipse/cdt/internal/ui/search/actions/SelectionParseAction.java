/*******************************************************************************
 * Copyright (c) 2004, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 *     Markus Schorn (Wind River Systems)
 *     Ed Swartz (Nokia)
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.search.actions;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.search.CSearchMessages;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.internal.ui.util.StatusLineHandler;
import org.eclipse.cdt.ui.ICModelBasedEditor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.texteditor.ITextEditor;

import com.ibm.icu.text.MessageFormat;

/**
 * @author aniefer
 * Created on Jun 2, 2004
 */
public class SelectionParseAction extends Action {

	protected IWorkbenchSite fSite;
	protected ICModelBasedEditor fEditor;

	public SelectionParseAction() {
		super();
	}

	public SelectionParseAction(ICModelBasedEditor editor) {
		super();
		fEditor = editor;
		fSite = editor.getSite();
	}

	public SelectionParseAction(IWorkbenchSite site) {
		super();
		fSite = site;
	}

	public IWorkbenchSite getSite() {
		return fSite;
	}

	protected void showStatusLineMessage(final String message) {
		StatusLineHandler.showStatusLineMessage(fSite, message);
	}

	protected void clearStatusLine() {
		StatusLineHandler.clearStatusLine(fSite);
	}

	protected ISelection getSelection() {
		ISelection sel = null;
		if (fSite != null && fSite.getSelectionProvider() != null) {
			sel = fSite.getSelectionProvider().getSelection();
		}

		return sel;
	}

	protected ITextSelection getSelectedStringFromEditor() {
		ISelection selection = getSelection();
		if (!(selection instanceof ITextSelection))
			return null;

		return (ITextSelection) selection;
	}

	protected void open(IPath path, int currentOffset, int currentLength) throws CoreException {
		clearStatusLine();

		IEditorPart editor = EditorUtility.openInEditor(path, fEditor.getTranslationUnit());
		ITextEditor textEditor = EditorUtility.getTextEditor(editor);
		if (textEditor != null) {
			textEditor.selectAndReveal(currentOffset, currentLength);
		} else {
			reportSourceFileOpenFailure(path);
		}
	}

	protected void open(ITranslationUnit tu, int currentOffset, int currentLength) throws CoreException {
		clearStatusLine();

		IEditorPart editor = EditorUtility.openInEditor(tu, true);
		ITextEditor textEditor = EditorUtility.getTextEditor(editor);
		if (textEditor != null) {
			textEditor.selectAndReveal(currentOffset, currentLength);
		} else {
			reportSourceFileOpenFailure(tu.getPath());
		}
	}

	protected void reportSourceFileOpenFailure(IPath path) {
		showStatusLineMessage(MessageFormat.format(CSearchMessages.SelectionParseAction_FileOpenFailure_format,
				new Object[] { path.toOSString() }));
	}

	protected void reportSelectionMatchFailure() {
		showStatusLineMessage(CSearchMessages.SelectionParseAction_SelectedTextNotSymbol_message);
	}

	protected void reportSymbolLookupFailure(String symbol) {
		showStatusLineMessage(MessageFormat.format(CSearchMessages.SelectionParseAction_SymbolNotFoundInIndex_format,
				new Object[] { symbol }));
	}

	protected void reportIncludeLookupFailure(String filename) {
		showStatusLineMessage(MessageFormat.format(CSearchMessages.SelectionParseAction_IncludeNotFound_format,
				new Object[] { filename }));
	}
}
