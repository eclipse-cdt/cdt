/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.opentype;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.resources.FileStorage;
import org.eclipse.cdt.core.search.SearchEngine;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.opentype.dialogs.OpenTypeSelectionDialog;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;

/**
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class OpenTypeAction implements IWorkbenchWindowActionDelegate {

	IWorkbenchWindow window;

	public OpenTypeAction() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		this.window= window;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {

		Shell parent= CUIPlugin.getDefault().getActiveWorkbenchShell();
		OpenTypeSelectionDialog dialog= new OpenTypeSelectionDialog(parent, new ProgressMonitorDialog(parent), 
			SearchEngine.createWorkspaceScope());
		dialog.setTitle(OpenTypeMessages.getString("OpenTypeAction.dialogTitle")); //$NON-NLS-1$
		dialog.setMessage(OpenTypeMessages.getString("OpenTypeAction.dialogMessage")); //$NON-NLS-1$
		dialog.setMatchEmptyString(true);

		int result= dialog.open();
		if (result != IDialogConstants.OK_ID)
			return;
		
		TypeSearchMatch selection= (TypeSearchMatch)dialog.getFirstResult();
		if (selection == null)
			return;

		boolean revealed = false;

		// get the corresponding CElement
		ICElement celement= selection.getCElement();
		if (celement != null) {
			try {
				IResource res= celement.getUnderlyingResource();
				IEditorPart part= EditorUtility.openInEditor(res);
				if (part instanceof CEditor) {
					CEditor ed= (CEditor)part;
					ed.setSelection(celement);
					revealed = true;
				}
			}
			catch (CModelException ex){
				ex.printStackTrace();
			}
			catch(PartInitException ex) {
				ex.printStackTrace();
			}
		}
		
		if (!revealed) {
			try {
				IPath path = selection.getLocation();
				IStorage storage = new FileStorage(path);
				IEditorPart part= EditorUtility.openInEditor(storage);
				if (part instanceof CEditor) {
					CEditor ed= (CEditor)part;
					ed.selectAndReveal(selection.getStartOffset(), selection.getName().length());
					revealed = true;
				}
			}
			catch (CModelException ex){
				ex.printStackTrace();
			}
			catch(PartInitException ex) {
				ex.printStackTrace();
			}
		}
		
		if (!revealed)
		{
			// could not find definition
			String path= selection.getFilePath();
			if (path == null || path.length() == 0)
				path= OpenTypeMessages.getString("TypeSelectionDialog.unknown"); //$NON-NLS-1$
			String title= OpenTypeMessages.getString("TypeSelectionDialog.errorTitle"); //$NON-NLS-1$
			String message= OpenTypeMessages.getFormattedString("TypeSelectionDialog.dialogMessage", path); //$NON-NLS-1$
			MessageDialog.openError(parent, title, message);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}
}
