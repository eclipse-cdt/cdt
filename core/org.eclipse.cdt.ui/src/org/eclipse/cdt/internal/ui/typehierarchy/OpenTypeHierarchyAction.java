/*******************************************************************************
 * Copyright (c) 2007, 2015 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.typehierarchy;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.actions.SelectionDispatchAction;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.texteditor.ITextEditor;

public class OpenTypeHierarchyAction extends SelectionDispatchAction {

	private ITextEditor fEditor;

	public OpenTypeHierarchyAction(IWorkbenchSite site) {
		super(site);
		setText(Messages.OpenTypeHierarchyAction_label);
		setToolTipText(Messages.OpenTypeHierarchyAction_tooltip);
	}

	public OpenTypeHierarchyAction(ITextEditor editor) {
		this(editor.getSite());
		fEditor = editor;
		setEnabled(fEditor != null
				&& CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(editor.getEditorInput()) != null);
	}

	@Override
	public void run(ITextSelection sel) {
		TypeHierarchyUI.open(fEditor, sel);
	}

	@Override
	public void run(IStructuredSelection selection) {
		if (!selection.isEmpty()) {
			Object selectedObject = selection.getFirstElement();
			ICElement elem = getAdapter(selectedObject, ICElement.class);
			if (elem != null) {
				TypeHierarchyUI.open(elem, getSite().getWorkbenchWindow());
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

		Object selectedObject = selection.getFirstElement();
		ICElement elem = getAdapter(selectedObject, ICElement.class);
		if (elem != null) {
			setEnabled(TypeHierarchyUI.isValidInput(elem));
		} else {
			setEnabled(false);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T getAdapter(Object object, Class<T> desiredClass) {
		if (desiredClass.isInstance(object)) {
			return (T) object;
		}
		if (object instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) object;
			return adaptable.getAdapter(desiredClass);
		}
		return null;
	}
}
