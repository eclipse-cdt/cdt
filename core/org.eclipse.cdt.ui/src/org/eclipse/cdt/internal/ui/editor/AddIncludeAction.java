/*******************************************************************************
 * Copyright (c) 2013, 2014 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import java.util.Collection;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.model.ASTCache;
import org.eclipse.cdt.internal.ui.BusyCursorJobRunner;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.refactoring.includes.IElementSelector;
import org.eclipse.cdt.internal.ui.refactoring.includes.IncludeCreator;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.undo.DocumentUndoManagerRegistry;
import org.eclipse.text.undo.IDocumentUndoManager;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

/**
 * Adds an '#include' statement and, optionally, a 'using' declaration necessary to resolve a name.
 */
public class AddIncludeAction extends TextEditorAction {
	private IElementSelector fAmbiguityResolver;

	/**
	 * Constructor
	 * @param editor The editor on which this Add Include action should operate.
	 */
	public AddIncludeAction(ITextEditor editor) {
		super(CEditorMessages.getBundleForConstructedKeys(), "AddInclude.", editor); //$NON-NLS-1$
		CUIPlugin.getDefault().getWorkbench().getHelpSystem().setHelp(this,
				ICHelpContextIds.ADD_INCLUDE_ON_SELECTION_ACTION);
		final Shell shell = editor.getEditorSite().getShell();
		fAmbiguityResolver = new IElementSelector() {
			@SuppressWarnings("unchecked")
			@Override
			public <T> T selectElement(final Collection<T> elements) {
				final Object[] result = new Object[1];
				runInUIThread(() -> {
					ElementListSelectionDialog dialog = new ElementListSelectionDialog(shell, new LabelProvider());
					dialog.setElements(elements.toArray());
					dialog.setTitle(CEditorMessages.AddInclude_label);
					dialog.setMessage(CEditorMessages.AddInclude_description);
					if (dialog.open() == Window.OK)
						result[0] = dialog.getFirstResult();
				});
				return (T) result[0];
			}
		};
	}

	@Override
	public void run() {
		final ITextEditor editor = getTextEditor();
		final ITranslationUnit tu = getTranslationUnit(editor);
		if (tu == null) {
			return;
		}
		final ISelection selection = getTextEditor().getSelectionProvider().getSelection();
		if (selection.isEmpty() || !(selection instanceof ITextSelection)) {
			return;
		}
		if (!validateEditorInputState()) {
			return;
		}

		final MultiTextEdit[] holder = new MultiTextEdit[1];
		// We can't use SharedASTJob because IncludeCreator needs to disable promiscuous
		// binding resolution, and you can't mix promiscuous and non-promiscuous binding
		// resolution in the same AST.
		Job job = new Job(CEditorMessages.AddInclude_action) {
			@Override
			public IStatus run(IProgressMonitor monitor) {
				try {
					IIndex index = CCorePlugin.getIndexManager().getIndex(tu.getCProject(),
							IIndexManager.ADD_DEPENDENCIES | IIndexManager.ADD_EXTENSION_FRAGMENTS_ADD_IMPORT);
					try {
						index.acquireReadLock();
						IASTTranslationUnit ast = tu.getAST(index, ASTCache.PARSE_MODE);
						if (ast == null) {
							return CUIPlugin.createErrorStatus(
									NLS.bind(CEditorMessages.AddInclude_ast_not_available, tu.getPath().toOSString()));
						}
						IncludeCreator creator = new IncludeCreator(tu, index, fAmbiguityResolver);
						holder[0] = creator.createInclude(ast, (ITextSelection) selection);
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
				// Apply text edits.
				IEditorInput editorInput = editor.getEditorInput();
				IDocument document = editor.getDocumentProvider().getDocument(editorInput);
				IDocumentUndoManager manager = DocumentUndoManagerRegistry.getDocumentUndoManager(document);
				manager.beginCompoundChange();
				try {
					edit.apply(document);
				} catch (MalformedTreeException | BadLocationException e) {
					CUIPlugin.log(e);
				}
				manager.endCompoundChange();
			}
		} else if (status.matches(IStatus.ERROR)) {
			ErrorDialog.openError(editor.getEditorSite().getShell(), CEditorMessages.AddInclude_error_title,
					CEditorMessages.AddInclude_insertion_failed, status);
		}
	}

	private static void runInUIThread(Runnable runnable) {
		if (Display.getCurrent() != null) {
			runnable.run();
		} else {
			Display.getDefault().syncExec(runnable);
		}
	}

	@Override
	public void update() {
		ITextEditor editor = getTextEditor();
		setEnabled(editor != null && getTranslationUnit(editor) != null);
	}

	/**
	 * Returns the translation unit of the given editor.
	 *
	 * @param editor The editor.
	 * @return The translation unit.
	 */
	private static ITranslationUnit getTranslationUnit(ITextEditor editor) {
		if (editor == null) {
			return null;
		}
		return CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(editor.getEditorInput());
	}

	/**
	 * For tests only.
	 */
	public void setAmbiguityResolver(IElementSelector fAmbiguityResolver) {
		this.fAmbiguityResolver = fAmbiguityResolver;
	}
}
