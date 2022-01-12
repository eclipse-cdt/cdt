/*******************************************************************************
 * Copyright (c) 2004, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Andrew Ferguson (Symbian)
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.browser.opentype;

import org.eclipse.cdt.core.browser.ITypeInfo;
import org.eclipse.cdt.core.browser.ITypeReference;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * @noextend This class is not intended to be subclassed by clients.
 */
public class OpenTypeAction implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow fWorkbenchWindow;

	public OpenTypeAction() {
		super();
	}

	@Override
	public void run(IAction action) {
		ElementSelectionDialog dialog = new ElementSelectionDialog(getShell());
		configureDialog(dialog);
		int result = dialog.open();
		if (result != IDialogConstants.OK_ID)
			return;

		ITypeInfo info = (ITypeInfo) dialog.getFirstResult();
		if (info == null)
			return;

		ITypeReference location = info.getResolvedReference();
		if (location == null) {
			// Could not resolve location.
			String title = OpenTypeMessages.OpenTypeAction_errorTitle;
			String message = NLS.bind(OpenTypeMessages.OpenTypeAction_errorTypeNotFound,
					info.getQualifiedTypeName().toString());
			MessageDialog.openError(getShell(), title, message);
		} else if (!openTypeInEditor(location)) {
			// Error opening editor.
			String title = OpenTypeMessages.OpenTypeAction_errorTitle;
			String message = NLS.bind(OpenTypeMessages.OpenTypeAction_errorOpenEditor, location.getPath().toString());
			MessageDialog.openError(getShell(), title, message);
		}
	}

	private void configureDialog(ElementSelectionDialog dialog) {
		dialog.setTitle(OpenTypeMessages.OpenTypeDialog_title);
		dialog.setMessage(OpenTypeMessages.OpenTypeDialog_message);
		dialog.setDialogSettings(getClass().getName());
		if (fWorkbenchWindow != null) {
			IWorkbenchPage page = fWorkbenchWindow.getActivePage();
			if (page != null) {
				IWorkbenchPart part = page.getActivePart();
				if (part instanceof ITextEditor) {
					ISelection sel = ((ITextEditor) part).getSelectionProvider().getSelection();
					if (sel instanceof ITextSelection) {
						String txt = ((ITextSelection) sel).getText();
						if (!txt.isEmpty() && txt.length() < 80) {
							dialog.setFilter(txt, true);
						}
					}
				}
			}
		}
	}

	protected Shell getShell() {
		return fWorkbenchWindow.getShell();
	}

	/**
	 * Opens an editor and displays the selected type.
	 *
	 * @param info Type to display.
	 * @return true if successfully displayed.
	 */
	private boolean openTypeInEditor(ITypeReference location) {
		ICElement[] cElements = location.getCElements();
		try {
			if (cElements != null && cElements.length > 0) {
				IEditorPart editor = EditorUtility.openInEditor(cElements[0]);
				EditorUtility.revealInEditor(editor, cElements[0]);
				return true;
			}
			ITranslationUnit unit = location.getTranslationUnit();
			IEditorPart editorPart = null;

			if (unit != null)
				editorPart = EditorUtility.openInEditor(unit);
			if (editorPart == null) {
				// Open as external file.
				editorPart = EditorUtility.openInEditor(location.getLocation(), null);
			}

			// Highlight the type in the editor.
			if (editorPart != null && editorPart instanceof ITextEditor) {
				ITextEditor editor = (ITextEditor) editorPart;
				if (location.isLineNumber()) {
					IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
					try {
						int startOffset = document.getLineOffset(location.getOffset() - 1);
						int length = document.getLineLength(location.getOffset() - 1);
						editor.selectAndReveal(startOffset, length);
						return true;
					} catch (BadLocationException ble) {
						return false;
					}
				}
				editor.selectAndReveal(location.getOffset(), location.getLength());
				return true;
			}
		} catch (CModelException ex) {
			ex.printStackTrace();
			return false;
		} catch (PartInitException ex) {
			ex.printStackTrace();
			return false;
		}

		return false;
	}

	@Override
	public void dispose() {
		fWorkbenchWindow = null;
	}

	@Override
	public void init(IWorkbenchWindow window) {
		fWorkbenchWindow = window;
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
	}
}
