/*******************************************************************************
 * Copyright (c) 2008, 2012 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.refactoring.actions;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.refactoring.utils.EclipseObjects;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchSite;

/**
 * Common base class for refactoring actions.
 * @since 5.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public abstract class RefactoringAction extends Action {
	protected CEditor fEditor;
	private IWorkbenchSite fSite;
	private ICElement fElement;
	private boolean saveRequired;

	public RefactoringAction(String label) {
		super(label);
	}

	/**
	 * Sets behavior with respect to saving dirty editors.
	 * @param saveRequired if <code>true</code>, dirty editors will be saved before refactoring.
	 *
	 * @deprecated saving of editors should be controlled by refactoring runner, not by the action.
	 * @since 5.3
	 */
	@Deprecated
	public void setSaveRequired(boolean saveRequired) {
		this.saveRequired = saveRequired;
	}

	public void setEditor(IEditorPart editor) {
		fEditor = null;
		fSite = null;
		if (editor instanceof CEditor) {
			fEditor = (CEditor) editor;
		}
		setEnabled(fEditor != null);
	}

	public void setSite(IWorkbenchSite site) {
		fEditor = null;
		fSite = site;
	}

	@Override
	public final void run() {
		if (saveRequired) {
			EclipseObjects.getActivePage().saveAllEditors(true);
			if (EclipseObjects.getActivePage().getDirtyEditors().length != 0) {
				return;
			}
		}
		if (fEditor != null) {
			ISelectionProvider provider = fEditor.getSelectionProvider();
			if (provider != null) {
				ISelection s = provider.getSelection();
				if (s instanceof ITextSelection) {
					IWorkingCopy wc = CUIPlugin.getDefault().getWorkingCopyManager()
							.getWorkingCopy(fEditor.getEditorInput());
					if (wc != null)
						run(fEditor.getSite(), wc, (ITextSelection) s);
				}
			}
		} else if (fSite != null && fElement != null) {
			run(fSite, fElement);
		}
	}

	public void updateSelection(ICElement elem) {
		fElement = elem;
		setEnabled(elem != null);
	}

	public abstract void run(IShellProvider shellProvider, IWorkingCopy wc, ITextSelection s);

	public abstract void run(IShellProvider shellProvider, ICElement elem);
}
