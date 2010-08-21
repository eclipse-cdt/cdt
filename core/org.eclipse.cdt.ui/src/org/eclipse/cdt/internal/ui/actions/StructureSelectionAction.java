/*******************************************************************************
 * Copyright (c) 2010 Tomasz Wesolowski and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tomasz Wesolowski - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.actions;

import java.util.ResourceBundle;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CDTUITools;
import org.eclipse.cdt.ui.text.SharedASTJob;

import org.eclipse.cdt.internal.core.model.ext.SourceRange;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.editor.SelectionHistory;

public abstract class StructureSelectionAction extends TextEditorAction {

	protected final SelectionHistory history;

	protected StructureSelectionAction(ResourceBundle bundle, String prefix, ITextEditor editor, SelectionHistory history) {
		super(bundle, prefix, editor);
		this.history = history;
	}

	private final class ExpandSelectionJob extends SharedASTJob {
		public ISourceRange newSourceRange;
		private SourceRange currentSourceRange;

		private ExpandSelectionJob(String name, ITranslationUnit tUnit, CEditor cEditor, SourceRange range) {
			super(name, tUnit);
			currentSourceRange = range;
			newSourceRange = null;
		}

		@Override
		public IStatus runOnAST(ILanguage lang, IASTTranslationUnit ast) throws CoreException {
			newSourceRange = doExpand(ast,currentSourceRange);
			return Status.OK_STATUS;
		}
	}

	public static final String ENCLOSING = "StructureSelectEnclosing"; //$NON-NLS-1$
	public static final String NEXT = "StructureSelectNext"; //$NON-NLS-1$
	public static final String PREVIOUS = "StructureSelectPrevious"; //$NON-NLS-1$
	public static final String HISTORY = "StructureSelectHistory"; //$NON-NLS-1$
	
	@Override
	public void run() {
		IEditorPart editorPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (!(editorPart instanceof CEditor)) {
			return;
		}
		final CEditor cEditor = (CEditor) editorPart;

		ITranslationUnit tu = (ITranslationUnit) CDTUITools.getEditorInputCElement(cEditor.getEditorInput());
		
		ITextSelection selection;
		try {
			selection = (ITextSelection) cEditor.getSelectionProvider().getSelection();
		} catch (ClassCastException e) {
			return;
		}
		
		final int offset = selection.getOffset();
		final int length = selection.getLength();
		
		ExpandSelectionJob expandSelectionJob = new ExpandSelectionJob("expand selection", tu, cEditor, new SourceRange(offset, length)); //$NON-NLS-1$
		
		expandSelectionJob.schedule();
		try {
			expandSelectionJob.join();
		} catch (InterruptedException e) {
			return;
		}
		
		if (expandSelectionJob.newSourceRange != null) {
			history.ignoreSelectionChanges();
			cEditor.setSelection(expandSelectionJob.newSourceRange, true);
			history.listenToSelectionChanges();
		}
	}

	protected abstract ISourceRange doExpand(IASTTranslationUnit ast, SourceRange currentSourceRange);

	protected boolean nodeContains(IASTNode node, int position) {
		IASTFileLocation fl = node.getFileLocation();
		return position >= fl.getNodeOffset() && position <= fl.getNodeOffset() + fl.getNodeLength();
	}

	protected boolean samePosition(IASTNode node, SourceRange current) {
		IASTFileLocation fl = node.getFileLocation();
		return fl.getNodeOffset() == current.getStartPos() && fl.getNodeLength() == current.getLength();
	}
}
