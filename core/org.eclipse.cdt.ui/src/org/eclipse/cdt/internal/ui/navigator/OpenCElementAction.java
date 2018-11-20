/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *     Ed Swartz (Nokia)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.navigator;

import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.actions.OpenFileAction;

/**
 * Open an editor and navigate to the source location of
 * the currently selected <code>ICElement</code>.
 * In case of multiple selections, opening is delegated to
 * the base class {@link OpenFileAction}.
 */
public class OpenCElementAction extends OpenFileAction {

	private ICElement fOpenElement;

	/**
	 * @param page
	 */
	public OpenCElementAction(IWorkbenchPage page) {
		super(page);
	}

	/*
	 * @see org.eclipse.ui.actions.OpenSystemEditorAction#run()
	 */
	@Override
	public void run() {
		if (fOpenElement != null) {
			IEditorPart part;
			try {
				part = EditorUtility.openInEditor(fOpenElement);
				if (fOpenElement instanceof ISourceReference && !(fOpenElement instanceof ITranslationUnit)) {
					EditorUtility.revealInEditor(part, fOpenElement);
				}
			} catch (CoreException exc) {
				CUIPlugin.log(exc.getStatus());
			}
		} else {
			super.run();
		}
	}

	/*
	 * @see org.eclipse.ui.actions.OpenSystemEditorAction#updateSelection(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		fOpenElement = null;
		if (selection.size() == 1) {
			Object element = selection.getFirstElement();
			if (!(element instanceof ICElement) && element instanceof IAdaptable) {
				element = ((IAdaptable) element).getAdapter(ICElement.class);
			}
			if (element instanceof ICElement && (element instanceof ISourceReference || element instanceof IBinary)) {
				fOpenElement = (ICElement) element;
			}
		}
		return fOpenElement != null || super.updateSelection(selection);
	}

}
