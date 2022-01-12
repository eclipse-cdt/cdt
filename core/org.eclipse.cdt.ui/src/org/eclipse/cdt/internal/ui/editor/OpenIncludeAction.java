/*******************************************************************************
 *  Copyright (c) 2005, 2012 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *     Sergey Prigogin (Google) - https://bugs.eclipse.org/bugs/show_bug.cgi?id=13221
 *     Ed Swartz (Nokia)
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import java.util.List;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IInclude;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.actions.OpenActionUtil;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;

public class OpenIncludeAction extends Action {

	private static final String PREFIX = "OpenIncludeAction."; //$NON-NLS-1$

	private static final String DIALOG_TITLE = PREFIX + "dialog.title"; //$NON-NLS-1$
	private static final String DIALOG_MESSAGE = PREFIX + "dialog.message"; //$NON-NLS-1$

	private ISelectionProvider fSelectionProvider;

	public OpenIncludeAction(ISelectionProvider provider) {
		super(CUIPlugin.getResourceString(PREFIX + "label")); //$NON-NLS-1$
		setDescription(CUIPlugin.getResourceString(PREFIX + "description")); //$NON-NLS-1$
		setToolTipText(CUIPlugin.getResourceString(PREFIX + "tooltip")); //$NON-NLS-1$

		CPluginImages.setImageDescriptors(this, CPluginImages.T_LCL, CPluginImages.IMG_MENU_OPEN_INCLUDE);

		fSelectionProvider = provider;
	}

	@Override
	public void run() {
		IInclude include = getIncludeStatement(fSelectionProvider.getSelection());
		if (include == null) {
			return;
		}
		try {
			List<IPath> filesFound = CElementIncludeResolver.resolveInclude(include);
			int nElementsFound = filesFound.size();
			if (nElementsFound == 0) {
				noElementsFound();
				return;
			}

			IPath fileToOpen;
			if (nElementsFound == 1) {
				fileToOpen = filesFound.get(0);
			} else {
				fileToOpen = chooseFile(filesFound);
			}
			if (fileToOpen != null) {
				EditorUtility.openInEditor(fileToOpen, include);
			}
		} catch (CoreException e) {
			CUIPlugin.log(e.getStatus());
		}
	}

	private static void noElementsFound() {
		MessageBox errorMsg = new MessageBox(CUIPlugin.getActiveWorkbenchShell(), SWT.ICON_ERROR | SWT.OK);
		errorMsg.setText(CUIPlugin.getResourceString("OpenIncludeAction.error")); //$NON-NLS-1$
		errorMsg.setMessage(CUIPlugin.getResourceString("OpenIncludeAction.error.description")); //$NON-NLS-1$
		errorMsg.open();
	}

	private static IPath chooseFile(List<IPath> filesFound) {
		return OpenActionUtil.selectPath(filesFound, CUIPlugin.getResourceString(DIALOG_TITLE),
				CUIPlugin.getResourceString(DIALOG_MESSAGE));
	}

	private static IInclude getIncludeStatement(ISelection sel) {
		if (!sel.isEmpty() && sel instanceof IStructuredSelection) {
			List<?> list = ((IStructuredSelection) sel).toList();
			if (list.size() == 1) {
				Object element = list.get(0);
				if (element instanceof IInclude) {
					return (IInclude) element;
				}
			}
		}
		return null;
	}

	public static boolean canActionBeAdded(ISelection selection) {
		ICElement include = getIncludeStatement(selection);
		if (include != null) {
			IResource res = include.getUnderlyingResource();
			if (res != null) {
				return true;
			}
		}
		return false;
	}
}
