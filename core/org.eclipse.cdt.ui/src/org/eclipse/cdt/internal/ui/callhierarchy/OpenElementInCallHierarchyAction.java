/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.ui.callhierarchy;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import org.eclipse.cdt.core.browser.ITypeInfo;
import org.eclipse.cdt.core.browser.ITypeReference;
import org.eclipse.cdt.core.model.ICElement;

import org.eclipse.cdt.internal.ui.browser.opentype.OpenTypeDialog;

public class OpenElementInCallHierarchyAction implements IWorkbenchWindowActionDelegate {

	private static final int[] VISIBLE_TYPES = { 
		ICElement.C_FUNCTION, ICElement.C_METHOD, ICElement.C_VARIABLE, ICElement.C_ENUMERATOR,
		ICElement.C_FUNCTION_DECLARATION, ICElement.C_METHOD_DECLARATION, ICElement.C_VARIABLE_DECLARATION };
	
	private IWorkbenchWindow fWorkbenchWindow;

	public OpenElementInCallHierarchyAction() {
	}

	public void run(IAction action) {
		OpenTypeDialog dialog = new OpenTypeDialog(getShell());
		configureDialog(dialog);
		int result = dialog.open();
		if (result != IDialogConstants.OK_ID)
			return;
		
		ITypeInfo info = (ITypeInfo) dialog.getFirstResult();
		if (info == null)
			return;
		
		ICElement[] elements= null;
		ITypeReference location = info.getResolvedReference();
		if (location != null) {
			elements= location.getCElements();
		}
		if (elements == null || elements.length == 0) {
			String title = CHMessages.OpenElementInCallHierarchyAction_errorDlgTitle;
			String message = NLS.bind(CHMessages.OpenElementInCallHierarchyAction_errorNoDefinition, info.getQualifiedTypeName().toString());
			MessageDialog.openError(getShell(), title, message);
		} 
		else {
			CallHierarchyUI.open(elements[0], fWorkbenchWindow);
		}
	}
	
	private void configureDialog(OpenTypeDialog dialog) {
		dialog.setVisibleTypes(VISIBLE_TYPES);
	}

	private Shell getShell() {
		return fWorkbenchWindow.getShell();
	}

	public void dispose() {
		fWorkbenchWindow= null;
	}
	
	public void init(IWorkbenchWindow window) {
		fWorkbenchWindow= window;
	}
	
	public void selectionChanged(IAction action, ISelection selection) {
	}
}
