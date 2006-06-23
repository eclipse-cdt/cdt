/*******************************************************************************
 * Copyright (c) 2004, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.browser.opentype;

import org.eclipse.cdt.core.browser.AllTypesCache;
import org.eclipse.cdt.core.browser.ITypeInfo;
import org.eclipse.cdt.core.browser.ITypeReference;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.resources.FileStorage;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.texteditor.ITextEditor;

public class OpenTypeAction implements IWorkbenchWindowActionDelegate {

	public OpenTypeAction() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		ITypeInfo[] elements = AllTypesCache.getAllTypes();
		if (elements.length == 0) {
			String title = OpenTypeMessages.getString("OpenTypeAction.notypes.title"); //$NON-NLS-1$
			String message = OpenTypeMessages.getString("OpenTypeAction.notypes.message"); //$NON-NLS-1$
			MessageDialog.openInformation(getShell(), title, message);
			return;
		}
		
		OpenTypeDialog dialog = new OpenTypeDialog(getShell());
		dialog.setElements(elements);
		int result = dialog.open();
		if (result != IDialogConstants.OK_ID)
			return;
		
		ITypeInfo info = (ITypeInfo) dialog.getFirstResult();
		if (info == null)
			return;
		
		ITypeReference location = info.getResolvedReference();
		if (location == null) {
			// could not resolve location
			String title = OpenTypeMessages.getString("OpenTypeAction.errorTitle"); //$NON-NLS-1$
			String message = OpenTypeMessages.getFormattedString("OpenTypeAction.errorTypeNotFound", info.getQualifiedTypeName().toString()); //$NON-NLS-1$
			MessageDialog.openError(getShell(), title, message);
		} else if (!openTypeInEditor(location)) {
			// error opening editor
			String title = OpenTypeMessages.getString("OpenTypeAction.errorTitle"); //$NON-NLS-1$
			String message = OpenTypeMessages.getFormattedString("OpenTypeAction.errorOpenEditor", location.getPath().toString()); //$NON-NLS-1$
			MessageDialog.openError(getShell(), title, message);
		}
	}
	
	protected Shell getShell() {
		return CUIPlugin.getActiveWorkbenchShell();
	}
	
	/**
	 * Opens an editor and displays the selected type.
	 * 
	 * @param info Type to display.
	 * @return true if succesfully displayed.
	 */
	private boolean openTypeInEditor(ITypeReference location) {
		ITranslationUnit unit = location.getTranslationUnit();
		IEditorPart editorPart = null;
		
		try {
			if (unit != null)
				editorPart = EditorUtility.openInEditor(unit);
			if (editorPart == null) {
				// open as external file
				IPath path = location.getLocation();
				if (path != null) {
					IStorage storage = new FileStorage(path);
					editorPart = EditorUtility.openInEditor(storage);
				}
			}

			// highlight the type in the editor
			if (editorPart != null && editorPart instanceof ITextEditor) {
				ITextEditor editor = (ITextEditor) editorPart;
                if( location.isLineNumber() )
                {
                    IDocument document= editor.getDocumentProvider().getDocument(editor.getEditorInput());
                    try
                    {
                        int startOffset = document.getLineOffset(location.getOffset()-1);
                        int length=document.getLineLength(location.getOffset()-1);
                        editor.selectAndReveal(startOffset, length);
                        return true;
                    }
                    catch( BadLocationException ble )
                    {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}
}
