/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.ui.callhierarchy;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IEnumeration;
import org.eclipse.cdt.core.model.IEnumerator;
import org.eclipse.cdt.core.model.IFunctionDeclaration;
import org.eclipse.cdt.core.model.IVariableDeclaration;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.actions.SelectionDispatchAction;


public class OpenCallHierarchyAction extends SelectionDispatchAction {

	private ITextEditor fEditor;

	public OpenCallHierarchyAction(IWorkbenchSite site) {
		super(site);
		setText(CHMessages.OpenCallHierarchyAction_label);
		setToolTipText(CHMessages.OpenCallHierarchyAction_tooltip);
	}
	
	public OpenCallHierarchyAction(ITextEditor editor) {
		this(editor.getSite());
		fEditor= editor;
		setEnabled(fEditor != null && CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(editor.getEditorInput()) != null);
	}

	@Override
	public void run(ITextSelection sel) {
		CallHierarchyUI.open(fEditor, sel);
	}
	
	@Override
	public void run(IStructuredSelection selection) {
		if (!selection.isEmpty()) {
			Object selectedObject= selection.getFirstElement();
			ICElement elem= (ICElement) getAdapter(selectedObject, ICElement.class);
			if (elem != null) {
				CallHierarchyUI.open(getSite().getWorkbenchWindow(), elem);
			}
		}
	}

	@Override
	public void selectionChanged(ITextSelection sel) {
	}
			
	@Override
	public void selectionChanged(IStructuredSelection selection) {
		if (selection.isEmpty()) {
			setEnabled(false);
			return;
		}
		
		Object selectedObject= selection.getFirstElement();
		ICElement elem= (ICElement) getAdapter(selectedObject, ICElement.class);
		if (elem != null) {
			setEnabled(isValidElement(elem));
		}
		else {
			setEnabled(false);
		}
	}

	private boolean isValidElement(ICElement elem) {
		if (elem instanceof IFunctionDeclaration) {
			return true;
		}
		if (elem instanceof IVariableDeclaration) {
			return !(elem instanceof IEnumeration);
		}
		if (elem instanceof IEnumerator) {
			return true;
		}
		return false;
	}

	@SuppressWarnings("rawtypes")
	private Object getAdapter(Object object, Class desiredClass) {
		if (desiredClass.isInstance(object)) {
			return object;
		}
		if (object instanceof IAdaptable) {
			IAdaptable adaptable= (IAdaptable) object;
			return adaptable.getAdapter(desiredClass);
		}
		return null;
	}
}
