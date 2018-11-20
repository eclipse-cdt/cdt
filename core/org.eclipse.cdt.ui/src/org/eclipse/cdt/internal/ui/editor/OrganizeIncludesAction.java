/*******************************************************************************
 * Copyright (c) 2012, 2014 Mathias Kunter and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Mathias Kunter - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.model.ASTCache;
import org.eclipse.cdt.internal.ui.BusyCursorJobRunner;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.refactoring.includes.IHeaderChooser;
import org.eclipse.cdt.internal.ui.refactoring.includes.IncludeOrganizer;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.osgi.util.NLS;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.undo.DocumentUndoManagerRegistry;
import org.eclipse.text.undo.IDocumentUndoManager;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

/**
 * Organizes the include directives and forward declarations of a source or header file.
 */
public class OrganizeIncludesAction extends TextEditorAction {
	/**
	 * Constructor
	 * @param editor The editor on which this Organize Includes action should operate.
	 */
	public OrganizeIncludesAction(ITextEditor editor) {
		super(CEditorMessages.getBundleForConstructedKeys(), "OrganizeIncludes.", editor); //$NON-NLS-1$
		CUIPlugin.getDefault().getWorkbench().getHelpSystem().setHelp(this, ICHelpContextIds.ORGANIZE_INCLUDES_ACTION);
	}

	@Override
	public void run() {
		final ITextEditor editor = getTextEditor();
		final ITranslationUnit tu = getTranslationUnit(editor);
		if (tu == null) {
			return;
		}
		if (!validateEditorInputState()) {
			return;
		}

		final IHeaderChooser headerChooser = new InteractiveHeaderChooser(CEditorMessages.OrganizeIncludes_label,
				editor.getSite().getShell());
		final MultiTextEdit[] holder = new MultiTextEdit[1];
		// We can't use SharedASTJob because IncludeOrganizer needs to disable promiscuous
		// binding resolution, and you can't mix promiscuous and non-promiscuous binding
		// resolution in the same AST.
		Job job = new Job(CEditorMessages.OrganizeIncludes_action) {
			@Override
			public IStatus run(IProgressMonitor monitor) {
				try {
					IIndex index = CCorePlugin.getIndexManager().getIndex(tu.getCProject(),
							IIndexManager.ADD_DEPENDENCIES | IIndexManager.ADD_EXTENSION_FRAGMENTS_ADD_IMPORT);
					try {
						index.acquireReadLock();
						IASTTranslationUnit ast = tu.getAST(index, ASTCache.PARSE_MODE);
						if (ast == null) {
							return CUIPlugin.createErrorStatus(NLS.bind(
									CEditorMessages.OrganizeIncludes_ast_not_available, tu.getPath().toOSString()));
						}
						IncludeOrganizer organizer = new IncludeOrganizer(tu, index, headerChooser);
						holder[0] = organizer.organizeIncludes(ast);
						return Status.OK_STATUS;
					} catch (InterruptedException e) {
						return Status.CANCEL_STATUS;
					} finally {
						index.releaseReadLock();
					}
				} catch (CoreException e) {
					return e.getStatus();
				}
			}
		};
		IStatus status = BusyCursorJobRunner.execute(job);
		if (status.isOK()) {
			MultiTextEdit edit = holder[0];
			if (edit.hasChildren()) {
				// Apply the text edit.
				IEditorInput editorInput = editor.getEditorInput();
				IDocument document = editor.getDocumentProvider().getDocument(editorInput);
				IDocumentUndoManager manager = DocumentUndoManagerRegistry.getDocumentUndoManager(document);
				manager.beginCompoundChange();
				try {
					edit.apply(document);
				} catch (MalformedTreeException e) {
					CUIPlugin.log(e);
				} catch (BadLocationException e) {
					CUIPlugin.log(e);
				}
				manager.endCompoundChange();
			}
		} else if (status.matches(IStatus.ERROR)) {
			ErrorDialog.openError(editor.getEditorSite().getShell(), CEditorMessages.OrganizeIncludes_error_title,
					CEditorMessages.OrganizeIncludes_insertion_failed, status);
		}
	}

	@Override
	public void update() {
		ITextEditor editor = getTextEditor();
		setEnabled(editor != null && getTranslationUnit(editor) != null);
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
}
