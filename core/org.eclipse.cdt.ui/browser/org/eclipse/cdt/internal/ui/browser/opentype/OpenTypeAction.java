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
import org.eclipse.cdt.core.browser.AllTypesCache;
import org.eclipse.cdt.core.browser.ITypeInfo;
import org.eclipse.cdt.core.browser.ITypeReference;
import org.eclipse.cdt.core.browser.ITypeSearchScope;
import org.eclipse.cdt.core.browser.TypeSearchScope;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.resources.FileStorage;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.internal.ui.util.ExceptionHandler;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
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
		final ITypeSearchScope fScope = new TypeSearchScope(true);
		if (!AllTypesCache.isCacheUpToDate(fScope)) {
			IRunnableWithProgress runnable = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					AllTypesCache.updateCache(fScope, monitor);
					if (monitor.isCanceled()) {
						throw new InterruptedException();
					}
				}
			};
			
			IProgressService service = PlatformUI.getWorkbench().getProgressService();
			try {
				service.busyCursorWhile(runnable);
			} catch (InvocationTargetException e) {
				String title = OpenTypeMessages.getString("OpenTypeAction.exception.title"); //$NON-NLS-1$
				String message = OpenTypeMessages.getString("OpenTypeAction.exception.message"); //$NON-NLS-1$
				ExceptionHandler.handle(e, title, message);
				return;
			} catch (InterruptedException e) {
				// cancelled by user
				return;
			}
		}

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
			final ITypeInfo[] typesToResolve = new ITypeInfo[] { info };
			IRunnableWithProgress runnable = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					AllTypesCache.resolveTypeLocation(typesToResolve[0], monitor);
					if (monitor.isCanceled()) {
						throw new InterruptedException();
					}
				}
			};
			
			IProgressService service = PlatformUI.getWorkbench().getProgressService();
			try {
				service.busyCursorWhile(runnable);
			} catch (InvocationTargetException e) {
				String title = OpenTypeMessages.getString("OpenTypeAction.exception.title"); //$NON-NLS-1$
				String message = OpenTypeMessages.getString("OpenTypeAction.exception.message"); //$NON-NLS-1$
				ExceptionHandler.handle(e, title, message);
				return;
			} catch (InterruptedException e) {
				// cancelled by user
				return;
			}

			location = info.getResolvedReference();
		}

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