/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.browser.typehierarchy;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.util.SelectionUtil;
import org.eclipse.cdt.internal.ui.viewsupport.CElementLabels;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;

import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Refocuses the type hierarchy on the currently selection type.
 */
public class FocusOnSelectionAction extends Action {
		
	private TypeHierarchyViewPart fViewPart;
	
	public FocusOnSelectionAction(TypeHierarchyViewPart part) {
		super(TypeHierarchyMessages.getString("FocusOnSelectionAction.label")); //$NON-NLS-1$
		setDescription(TypeHierarchyMessages.getString("FocusOnSelectionAction.description")); //$NON-NLS-1$
		setToolTipText(TypeHierarchyMessages.getString("FocusOnSelectionAction.tooltip")); //$NON-NLS-1$
		fViewPart= part;
		
		WorkbenchHelp.setHelp(this, ICHelpContextIds.FOCUS_ON_SELECTION_ACTION);
	}
	
	private ISelection getSelection() {
		ISelectionProvider provider= fViewPart.getSite().getSelectionProvider();
		if (provider != null) {
			return provider.getSelection();
		}
		return null;
	}
	

	/*
	 * @see Action#run
	 */
	public void run() {
		Object element= SelectionUtil.getSingleElement(getSelection());
		if (element instanceof ICElement) {
			fViewPart.setInputElement((ICElement)element);
		}
	}	
	
	public boolean canActionBeAdded() {
		Object element= SelectionUtil.getSingleElement(getSelection());
		if (element instanceof ICElement) {
		    ICElement type= (ICElement)element;
			setText(TypeHierarchyMessages.getFormattedString(
					"FocusOnSelectionAction.label", //$NON-NLS-1$
					CElementLabels.getTextLabel(type, 0))); 
			return true;
		}
		return false;
	}
}
