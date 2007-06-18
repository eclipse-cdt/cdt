/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.ui.CElementGrouping;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.CPluginImages;


public class LexicalSortingAction extends Action {
	
	private static final String ACTION_NAME= "LexicalSortingAction"; //$NON-NLS-1$
	private static final String DIALOG_STORE_KEY= ACTION_NAME + ".sort"; //$NON-NLS-1$
	
	private LexicalCSorter fSorter;
	private TreeViewer fTreeViewer;
	
	public LexicalSortingAction(TreeViewer treeViewer) {
		super(CUIPlugin.getResourceString(ACTION_NAME + ".label")); //$NON-NLS-1$
		
		setDescription(CUIPlugin.getResourceString(ACTION_NAME + ".description")); //$NON-NLS-1$
		setToolTipText(CUIPlugin.getResourceString(ACTION_NAME + ".tooltip")); //$NON-NLS-1$
	
		CPluginImages.setImageDescriptors(this, CPluginImages.T_LCL, CPluginImages.IMG_ALPHA_SORTING);
	
		fTreeViewer= treeViewer;
		fSorter= new LexicalCSorter();
		
		boolean checked= CUIPlugin.getDefault().getDialogSettings().getBoolean(DIALOG_STORE_KEY);
		valueChanged(checked, false);
	}
	
	public void run() {
		valueChanged(isChecked(), true);
	}
	
	private void valueChanged(boolean on, boolean store) {
		setChecked(on);
		fTreeViewer.setSorter(on ? fSorter : null);
		
		String key= ACTION_NAME + ".tooltip" + (on ? ".on" : ".off"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		setToolTipText(CUIPlugin.getResourceString(key));
		
		if (store) {
			CUIPlugin.getDefault().getDialogSettings().put(DIALOG_STORE_KEY, on);
		}
	}
	
	private class LexicalCSorter extends ViewerSorter {		
		public boolean isSorterProperty(Object element, Object property) {
			return true;
		}
		
		public int category(Object obj) {
			if (obj instanceof ICElement) {
				ICElement elem= (ICElement)obj;
				switch (elem.getElementType()) {
				case ICElement.C_MACRO:
					return 2;
				case ICElement.C_INCLUDE:
					return 3;
				case ICElement.C_USING:
					return 4;
					
				case ICElement.C_TYPEDEF:
					return 10;
				case ICElement.C_CLASS: 
				case ICElement.C_CLASS_DECLARATION:
				case ICElement.C_TEMPLATE_CLASS:
				case ICElement.C_TEMPLATE_CLASS_DECLARATION:
					return 11;
				case ICElement.C_STRUCT:
				case ICElement.C_STRUCT_DECLARATION:
				case ICElement.C_TEMPLATE_STRUCT:
				case ICElement.C_TEMPLATE_STRUCT_DECLARATION:
					return 12;
				case ICElement.C_UNION:
				case ICElement.C_UNION_DECLARATION:
				case ICElement.C_TEMPLATE_UNION:
				case ICElement.C_TEMPLATE_UNION_DECLARATION:
					return 13;
				case ICElement.C_ENUMERATION:
					return 14;
					
				case ICElement.C_VARIABLE:
				case ICElement.C_VARIABLE_DECLARATION:
					return 20;
				case ICElement.C_FIELD:
					return 21;
				case ICElement.C_FUNCTION:
				case ICElement.C_FUNCTION_DECLARATION:
				case ICElement.C_TEMPLATE_FUNCTION:
				case ICElement.C_TEMPLATE_FUNCTION_DECLARATION:
					return 22;
				case ICElement.C_METHOD:
				case ICElement.C_METHOD_DECLARATION:
				case ICElement.C_TEMPLATE_METHOD:
				case ICElement.C_TEMPLATE_METHOD_DECLARATION:
					return 23;
					
				case ICElement.C_NAMESPACE:
					return 30;
				}
			} else if (obj instanceof CElementGrouping) {
				return 0;
			}
			return 100;
		}
	}
	
}
