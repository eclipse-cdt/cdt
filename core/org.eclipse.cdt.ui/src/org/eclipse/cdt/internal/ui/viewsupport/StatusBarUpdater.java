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
package org.eclipse.cdt.internal.ui.viewsupport;


import org.eclipse.cdt.core.browser.ITypeInfo;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.ui.CUIMessages;
import org.eclipse.cdt.ui.browser.typeinfo.TypeInfoLabelProvider;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;

/**
 * Add the <code>StatusBarUpdater</code> to your ViewPart to have the statusbar
 * describing the selected elements.
 */
public class StatusBarUpdater implements ISelectionChangedListener {
	
	private final int LABEL_FLAGS= CElementLabels.DEFAULT_QUALIFIED | CElementLabels.ROOT_POST_QUALIFIED | CElementLabels.APPEND_ROOT_PATH |
			CElementLabels.M_PARAMETER_TYPES | CElementLabels.M_PARAMETER_NAMES | CElementLabels.M_APP_RETURNTYPE | CElementLabels.M_EXCEPTIONS | 
		 	CElementLabels.F_APP_TYPE_SIGNATURE;

	private final TypeInfoLabelProvider fTypeInfoLabelProvider = new TypeInfoLabelProvider(TypeInfoLabelProvider.SHOW_FULLY_QUALIFIED + TypeInfoLabelProvider.SHOW_PATH);
		 	
	private IStatusLineManager fStatusLineManager;
	
	public StatusBarUpdater(IStatusLineManager statusLineManager) {
		fStatusLineManager= statusLineManager;
	}
		
	/*
	 * @see ISelectionChangedListener#selectionChanged
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		String statusBarMessage= formatMessage(event.getSelection());
		fStatusLineManager.setMessage(statusBarMessage);
	}
	
	
	protected String formatMessage(ISelection sel) {
		if (sel instanceof IStructuredSelection && !sel.isEmpty()) {
			IStructuredSelection selection= (IStructuredSelection) sel;
			
			int nElements= selection.size();
			if (nElements > 1) {
				return CUIMessages.getFormattedString("StatusBarUpdater.num_elements_selected", String.valueOf(nElements)); //$NON-NLS-1$
			} else { 
				Object elem= selection.getFirstElement();
				if (elem instanceof ICElement) {
					return formatCElementMessage((ICElement) elem);
				} else if (elem instanceof ITypeInfo) {
					return formatTypeInfoMessage((ITypeInfo) elem);
				} else if (elem instanceof IResource) {
					return formatResourceMessage((IResource) elem);
				}
			}
		}
		return "";  //$NON-NLS-1$
	}
		
	private String formatCElementMessage(ICElement element) {
		return CElementLabels.getElementLabel(element, LABEL_FLAGS);
	}
		
	private String formatTypeInfoMessage(ITypeInfo info) {
		return fTypeInfoLabelProvider.getText(info);
	}

	private String formatResourceMessage(IResource element) {
		IContainer parent= element.getParent();
		if (parent != null && parent.getType() != IResource.ROOT)
			return element.getName() + CElementLabels.CONCAT_STRING + parent.getFullPath().makeRelative().toString();
		else
			return element.getName();
	}	

}
