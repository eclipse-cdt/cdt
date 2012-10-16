/*******************************************************************************
 * Copyright (c) 2012 Mathias Kunter and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mathias Kunter - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.SharedASTJob;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.refactoring.includes.IncludeOrganizer;

/**
 * Organizes the include directives and forward declarations of a source or header file.
 */
public class OrganizeIncludesAction extends TextEditorAction {
	/**
	 * Constructor
	 * @param editor The editor on which this organize includes action should operate.
	 */
	public OrganizeIncludesAction(ITextEditor editor) {
		// TODO Fix ID's
		super(CEditorMessages.getBundleForConstructedKeys(), "OrganizeIncludes.", editor); //$NON-NLS-1$
		CUIPlugin.getDefault().getWorkbench().getHelpSystem().setHelp(this, ICHelpContextIds.ADD_INCLUDE_ON_SELECTION_ACTION);
	}

	/**
	 * Returns the translation unit of the given editor.
	 * @param editor The editor.
	 * @return The translation unit.
	 */
	private static ITranslationUnit getTranslationUnit(ITextEditor editor) {
		if (editor == null) {
			return null;
		}
		return CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(editor.getEditorInput());
	}

	@Override
	public void run() {
		final ITextEditor editor = getTextEditor();
		final ITranslationUnit tu = getTranslationUnit(editor);
		if (tu == null) {
			return;
		}
		try {
			if (!validateEditorInputState()) {
				return;
			}

			SharedASTJob job = new SharedASTJob(CEditorMessages.OrganizeIncludes_label, tu) {
				@Override
				public IStatus runOnAST(ILanguage lang, IASTTranslationUnit ast) throws CoreException {
					IIndex index= CCorePlugin.getIndexManager().getIndex(tu.getCProject(),
							IIndexManager.ADD_DEPENDENCIES | IIndexManager.ADD_EXTENSION_FRAGMENTS_ADD_IMPORT);
					try {
						index.acquireReadLock();
						IncludeOrganizer organizer = new IncludeOrganizer(editor, tu, index);
						organizer.organizeIncludes(ast);
						return Status.OK_STATUS;
					} catch (InterruptedException e) {
						return Status.CANCEL_STATUS;
					} finally {
						index.releaseReadLock();
					}
				}
			};
			job.schedule();
			job.join();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	@Override
	public void update() {
		ITextEditor editor = getTextEditor();
		setEnabled(editor != null && getTranslationUnit(editor) != null);
	}
}
