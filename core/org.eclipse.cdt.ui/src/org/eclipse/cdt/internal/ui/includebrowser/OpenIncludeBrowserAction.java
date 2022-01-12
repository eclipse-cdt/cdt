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

package org.eclipse.cdt.internal.ui.includebrowser;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.actions.SelectionDispatchAction;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.texteditor.ITextEditor;

public class OpenIncludeBrowserAction extends SelectionDispatchAction {

	private ITextEditor fEditor;

	public OpenIncludeBrowserAction(IWorkbenchSite site) {
		super(site);
		setText(IBMessages.OpenIncludeBrowserAction_label);
		setToolTipText(IBMessages.OpenIncludeBrowserAction_tooltip);
	}

	public OpenIncludeBrowserAction(ITextEditor editor) {
		this(editor.getSite());
		fEditor = editor;
		setEnabled(fEditor != null
				&& CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(editor.getEditorInput()) != null);
	}

	@Override
	public void run(ITextSelection sel) {
		IncludeBrowserUI.open(fEditor, sel);
	}

	@Override
	public void run(IStructuredSelection selection) {
		if (!selection.isEmpty()) {
			Object selectedObject = selection.getFirstElement();
			ICElement elem = getAdapter(selectedObject, ICElement.class);
			if (elem != null) {
				IncludeBrowserUI.open(getSite().getWorkbenchWindow(), elem);
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
			setEnabled(isValidElement(elem));
		} else {
			setEnabled(false);
		}
	}

	private boolean isValidElement(ICElement elem) {
		if (elem instanceof ISourceReference) {
			return true;
		}
		return false;
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
