/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.browser.typehierarchy;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.cdt.core.browser.AllTypesCache;
import org.eclipse.cdt.core.browser.ITypeInfo;
import org.eclipse.cdt.core.browser.ITypeSearchScope;
import org.eclipse.cdt.core.browser.TypeSearchScope;
import org.eclipse.cdt.core.browser.TypeUtil;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.browser.opentype.OpenTypeMessages;
import org.eclipse.cdt.internal.ui.util.ExceptionHandler;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.progress.IProgressService;

/**
 * Refocuses the type hierarchy on a type selection from a all types dialog.
 */
public class FocusOnTypeAction extends Action {
			
	private TypeHierarchyViewPart fViewPart;
	
	public FocusOnTypeAction(TypeHierarchyViewPart part) {
		super(TypeHierarchyMessages.getString("FocusOnTypeAction.label")); //$NON-NLS-1$
		setDescription(TypeHierarchyMessages.getString("FocusOnTypeAction.description")); //$NON-NLS-1$
		setToolTipText(TypeHierarchyMessages.getString("FocusOnTypeAction.tooltip")); //$NON-NLS-1$
		
		fViewPart= part;
		WorkbenchHelp.setHelp(this,	ICHelpContextIds.FOCUS_ON_TYPE_ACTION);
	}

	/*
	 * @see Action#run
	 */
	public void run() {
	    
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

		final int[] kinds = {ICElement.C_CLASS, ICElement.C_STRUCT};
		ITypeInfo[] elements = AllTypesCache.getTypes(fScope, kinds);
		if (elements.length == 0) {
			String title = OpenTypeMessages.getString("OpenTypeAction.notypes.title"); //$NON-NLS-1$
			String message = OpenTypeMessages.getString("OpenTypeAction.notypes.message"); //$NON-NLS-1$
			MessageDialog.openInformation(getShell(), title, message);
			return;
		}
		
		FocusOnTypeDialog dialog = new FocusOnTypeDialog(getShell());
		dialog.setElements(elements);
		int result = dialog.open();
		if (result != IDialogConstants.OK_ID)
			return;
		
		ITypeInfo info = (ITypeInfo) dialog.getFirstResult();
		if (info == null)
			return;
		
		final ITypeInfo[] typesToResolve = new ITypeInfo[] { info };
		final ICElement[] foundElement = new ICElement[] { null };
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
			    foundElement[0] = TypeUtil.getElementForType(typesToResolve[0], monitor);
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

		if (foundElement[0] == null) {
			// could not resolve location
			String title = OpenTypeMessages.getString("OpenTypeAction.errorTitle"); //$NON-NLS-1$
			String message = OpenTypeMessages.getFormattedString("OpenTypeAction.errorTypeNotFound", info.getQualifiedTypeName().toString()); //$NON-NLS-1$
			MessageDialog.openError(getShell(), title, message);
		} else {
		    fViewPart.setInputElement(foundElement[0]);
		}
	}	

	protected Shell getShell() {
		return CUIPlugin.getActiveWorkbenchShell();
	}
}
