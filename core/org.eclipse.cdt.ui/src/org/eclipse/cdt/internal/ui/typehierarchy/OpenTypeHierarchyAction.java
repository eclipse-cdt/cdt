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

package org.eclipse.cdt.internal.ui.typehierarchy;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchSite;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.ui.actions.SelectionDispatchAction;

import org.eclipse.cdt.internal.ui.editor.CEditor;


public class OpenTypeHierarchyAction extends SelectionDispatchAction {

	private CEditor fEditor;

	public OpenTypeHierarchyAction(IWorkbenchSite site) {
		super(site);
		setText(Messages.OpenTypeHierarchyAction_label);
		setToolTipText(Messages.OpenTypeHierarchyAction_tooltip);
	}
	
	public OpenTypeHierarchyAction(CEditor editor) {
		this(editor.getSite());
		fEditor= editor;
		setEnabled(fEditor != null && fEditor.getInputCElement() != null);
	}

	public void run(ITextSelection sel) {
		TypeHierarchyUI.open(fEditor, sel);
	}
	
	public void run(IStructuredSelection selection) {
		if (!selection.isEmpty()) {
			Object selectedObject= selection.getFirstElement();
			ICElement elem= (ICElement) getAdapter(selectedObject, ICElement.class);
			if (elem != null) {
				TypeHierarchyUI.open(elem, getSite().getWorkbenchWindow());
			}
		}
	}

	public void selectionChanged(ITextSelection sel) {
	}
			
	public void selectionChanged(IStructuredSelection selection) {
		if (selection.isEmpty()) {
			setEnabled(false);
			return;
		}
		
		Object selectedObject= selection.getFirstElement();
		ICElement elem= (ICElement) getAdapter(selectedObject, ICElement.class);
		if (elem != null) {
			setEnabled(TypeHierarchyUI.isValidInput(elem));
		}
		else {
			setEnabled(false);
		}
	}

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
