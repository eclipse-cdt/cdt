/*******************************************************************************
 * Copyright (c) 2013 Ericsson AB and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alvaro Sanchez-Leon (Ericsson AB) - Support for Step into selection (bug 244865)
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.sourcelookup;

import org.eclipse.cdt.core.model.IFunctionDeclaration;
import org.eclipse.cdt.debug.internal.ui.CDebugUIUtils;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.editor.SelectionToDeclarationJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.ITextEditor;

public class DsfSourceSelectionResolver implements Runnable {
	private ITextEditor fEditorPage = null;
	private ITextSelection fSelection = null;
	private LineLocation fLineLocation = new LineLocation();
	private IFunctionDeclaration fFunction = null;
	private boolean fSuccessful = false;

	public class LineLocation {
		private String fileName = null;
		private int lineNumber = 0;

		public String getFileName() {
			return fileName;
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		public int getLineNumber() {
			return lineNumber;
		}

		public void setLineNumber(int lineNumber) {
			this.lineNumber = lineNumber;
		}
	}

	private interface ResolveEditorRunnable extends Runnable {
		TextEditor getEditor();
	}

	public DsfSourceSelectionResolver() {

	}

	public DsfSourceSelectionResolver(ITextEditor editor, ITextSelection selection) {
		fEditorPage = editor;
		fSelection = selection;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.internal.ui.sourcelookup.IDsfSourceSelectionResolver#run()
	 */
	@Override
	public void run() {
		fEditorPage = resolveEditor();
		if (fEditorPage != null) {
			ITextSelection selection = resolveSelection();
			if (selection != null) {
				IFunctionDeclaration[] selectedFunctions = resolveSelectedFunction(selection);

				IFunctionDeclaration selFunction = null;
				if (selectedFunctions == null || selectedFunctions.length != 1 || selectedFunctions[0] == null) {
					//Unable to resolve selection to a function
					return;
				} else {
					// Continue as expected
					selFunction = selectedFunctions[0];
				}

				LineLocation selectedLine = resolveSelectedLine();
				if (selectedLine == null) {
					// Unable to resolve the selected line
					return;
				}

				fLineLocation = selectedLine;
				fFunction = selFunction;
				fSuccessful = true;
			}
		}
	}

	public ITextEditor resolveEditor() {
		if (fEditorPage != null) {
			return fEditorPage;
		}

		final IWorkbench wb = DsfUIPlugin.getDefault().getWorkbench();
		// Run in UI thread to access UI resources
		ResolveEditorRunnable reditorRunnable = new ResolveEditorRunnable() {
			TextEditor result = null;

			@Override
			public void run() {
				IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
				if (win == null || win.getActivePage() == null || win.getActivePage().getActiveEditor() == null) {
					result = null;
				} else {
					IEditorPart editorPart = win.getActivePage().getActiveEditor();
					if (editorPart instanceof CEditor) {
						result = (TextEditor) win.getActivePage().getActiveEditor();
					}
				}
			}

			@Override
			public TextEditor getEditor() {
				return result;
			}
		};

		Display.getDefault().syncExec(reditorRunnable);
		return reditorRunnable.getEditor();
	}

	private LineLocation resolveSelectedLine() {
		String errorMessage = ""; //$NON-NLS-1$
		IEditorInput input = fEditorPage.getEditorInput();
		if (input == null) {
			errorMessage = "Invalid Editor input on selection"; //$NON-NLS-1$
		} else {
			IDocument document = fEditorPage.getDocumentProvider().getDocument(input);
			if (document == null) {
				errorMessage = "Invalid Editor Document input on selection"; //$NON-NLS-1$
			} else {
				ITextSelection selection = resolveSelection();
				if (selection == null) {
					errorMessage = "Invalid selection. Only textual selections are supported"; //$NON-NLS-1$
				} else {
					String fileName = null;
					try {
						fileName = CDebugUIUtils.getEditorFilePath(input);
					} catch (CoreException e) {
						// unable to resolve the path
						DsfUIPlugin.log(e);
						return null;
					}

					if (fileName == null) {
						errorMessage = "Unable to resolve fileName from selection"; //$NON-NLS-1$
						DsfUIPlugin.logErrorMessage(errorMessage);
					} else {
						// Resolve the values
						LineLocation lineLocation = new LineLocation();

						lineLocation.setFileName(fileName);
						lineLocation.setLineNumber(selection.getStartLine() + 1);
						return lineLocation;
					}
				}
			}
		}

		DsfUIPlugin.logErrorMessage(errorMessage);
		return null;
	}

	public ITextSelection resolveSelection() {
		if (fSelection != null) {
			//Value received at construction time
			return fSelection;
		}

		ISelection selection = fEditorPage.getEditorSite().getSelectionProvider().getSelection();
		if (selection instanceof ITextSelection) {
			return (ITextSelection) selection;
		}

		return null;
	}

	private IFunctionDeclaration[] resolveSelectedFunction(ITextSelection textSelection) {
		if (textSelection != null) {
			SelectionToDeclarationJob job;
			try {
				job = new SelectionToDeclarationJob(fEditorPage, textSelection);
				job.schedule();
				job.join();
			} catch (CoreException e1) {
				DsfUIPlugin.log(e1);
				return null;
			} catch (InterruptedException e) {
				DsfUIPlugin.log(e);
				return null;
			}

			//fetch the result
			return job.getSelectedFunctions();
		}

		return null;
	}

	public LineLocation getLineLocation() {
		return fLineLocation;
	}

	public IFunctionDeclaration getFunction() {
		return fFunction;
	}

	public boolean isSuccessful() {
		return fSuccessful;
	}
}
