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
package org.eclipse.cdt.internal.ui.browser.opentype;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.cdt.core.browser.AllTypesCache;
import org.eclipse.cdt.core.browser.ITypeInfo;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.resources.FileStorage;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.core.search.SearchEngine;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.internal.ui.util.ExceptionHandler;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class OpenTypeAction implements IWorkbenchWindowActionDelegate {

	public OpenTypeAction() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {		

		final ICSearchScope scope= SearchEngine.createWorkspaceScope();
		final int[] kinds= { ICElement.C_NAMESPACE, ICElement.C_CLASS, ICElement.C_STRUCT,
				ICElement.C_UNION, ICElement.C_ENUMERATION, ICElement.C_TYPEDEF };
		final Collection typeList= new ArrayList();

		if (AllTypesCache.isCacheUpToDate()) {
			// run without progress monitor
			AllTypesCache.getTypes(scope, kinds, null, typeList);
		} else {
			IRunnableWithProgress runnable= new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					AllTypesCache.getTypes(scope, kinds, monitor, typeList);
					if (monitor.isCanceled()) {
						throw new InterruptedException();
					}
				}
			};

			IRunnableContext runnableContext= new ProgressMonitorDialog(getShell());
			try {
				runnableContext.run(true, true, runnable);
			} catch (InvocationTargetException e) {
				String title= OpenTypeMessages.getString("OpenTypeAction.exception.title"); //$NON-NLS-1$
				String message= OpenTypeMessages.getString("OpenTypeAction.exception.message"); //$NON-NLS-1$
				ExceptionHandler.handle(e, title, message);
				return;
			} catch (InterruptedException e) {
				// cancelled by user
				return;
			}
		}
		
		if (typeList.isEmpty()) {
			String title= OpenTypeMessages.getString("OpenTypeAction.notypes.title"); //$NON-NLS-1$
			String message= OpenTypeMessages.getString("OpenTypeAction.notypes.message"); //$NON-NLS-1$
			MessageDialog.openInformation(getShell(), title, message);
			return;
		}

		ITypeInfo[] elements= (ITypeInfo[])typeList.toArray(new ITypeInfo[typeList.size()]);

		OpenTypeDialog dialog= new OpenTypeDialog(getShell());
		dialog.setElements(elements);

		int result= dialog.open();
		if (result != IDialogConstants.OK_ID)
			return;
		
		ITypeInfo info= (ITypeInfo)dialog.getFirstResult();
		if (info == null)
			return;

		if (!openTypeInEditor(info))
		{
			// could not find definition
			String pathString= null;
			IPath path= info.getLocation();
			if (path != null)
				pathString= path.toString();
			else
				pathString= OpenTypeMessages.getString("OpenTypeAction.errorNoPath"); //$NON-NLS-1$
			String title= OpenTypeMessages.getString("OpenTypeAction.errorTitle"); //$NON-NLS-1$
			String message= OpenTypeMessages.getFormattedString("OpenTypeAction.errorMessage", pathString); //$NON-NLS-1$
			MessageDialog.openError(getShell(), title, message);
		}
	}
	
	protected Shell getShell() {
		return CUIPlugin.getActiveWorkbenchShell();
	}

	/**
	 * Opens an editor and displays the selected type.
	 * @param info Type to display.
	 * @return true if succesfully displayed.
	 */
	private boolean openTypeInEditor(ITypeInfo info) {
		IResource res= null;
		IEditorPart editorPart= null;
		IPath path= info.getPath();
		ICElement celement= info.getCElement();

		// attempt to locate the resource
		if (celement != null)
			res= celement.getUnderlyingResource();
		if (res == null)
			res= info.getResource();
		if (res == null && path != null) {
			IWorkspaceRoot wsRoot= CUIPlugin.getWorkspace().getRoot();
			res= wsRoot.getFileForLocation(path);
		}

		try {
			// open resource in editor
			if (res != null)
				editorPart= EditorUtility.openInEditor(res);
			if (editorPart == null) {
				// open as external file
				IStorage storage = new FileStorage(path);
				editorPart= EditorUtility.openInEditor(storage);
			}
			if (editorPart == null)
				return false;
		} catch (CModelException ex){
			ex.printStackTrace();
			return false;
		}
		catch(PartInitException ex) {
			ex.printStackTrace();
			return false;
		}

		// highlight the type in the editor
		if (celement != null && editorPart instanceof CEditor) {
			CEditor editor= (CEditor)editorPart;
			editor.setSelection(celement);
			return true;
		} else if (editorPart instanceof ITextEditor) {
			ITextEditor editor= (ITextEditor)editorPart;
			editor.selectAndReveal(info.getStartOffset(), info.getName().length());
			return true;
		}
		
		return false;
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
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}
}
