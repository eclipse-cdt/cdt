/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.navigator;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.navigator.ILinkHelper;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IWorkingCopyManager;

import org.eclipse.cdt.internal.ui.util.EditorUtility;

/**
 * Provide support for linking view selection with active editor.
 * 
 * @since 4.0
 */
public class CNavigatorLinkHelper implements ILinkHelper {

	/*
	 * @see org.eclipse.ui.navigator.ILinkHelper#activateEditor(org.eclipse.ui.IWorkbenchPage, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void activateEditor(IWorkbenchPage page, IStructuredSelection selection) {
		if (selection == null || selection.isEmpty())
			return;
		Object element= selection.getFirstElement();
		IEditorPart part= EditorUtility.isOpenInEditor(element);
		if (part != null) {
			page.bringToTop(part);
			if (element instanceof ICElement && !(element instanceof ITranslationUnit)) {
				EditorUtility.revealInEditor(part, (ICElement) element);
			}
		}

	}

	/*
	 * @see org.eclipse.ui.navigator.ILinkHelper#findSelection(org.eclipse.ui.IEditorInput)
	 */
	public IStructuredSelection findSelection(IEditorInput input) {
		IWorkingCopyManager mgr= CUIPlugin.getDefault().getWorkingCopyManager();
		Object element= mgr.getWorkingCopy(input);
		if (element == null) {
			IFile file = ResourceUtil.getFile(input);
			if (file != null && CoreModel.hasCNature(file.getProject())) {
				element= CoreModel.getDefault().create(file);
			}
		} else {
			ITranslationUnit tUnit= ((IWorkingCopy) element).getOriginalElement();
			IFile file= (IFile) tUnit.getResource();
			if (file != null) {
				element= CoreModel.getDefault().create(file);
				if (element == null) {
					element= file;
				}
			} else {
				element= tUnit;
			}
		}
		return (element != null) ? new StructuredSelection(element) : StructuredSelection.EMPTY;
	}

}
