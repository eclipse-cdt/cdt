package org.eclipse.cdt.internal.ui.editor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;


import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.ui.CPlugin;
import org.eclipse.cdt.internal.ui.CPluginImages;


public class LexicalSortingAction extends Action {
	
	private static final String ACTION_NAME= "LexicalSortingAction";
	private static final String DIALOG_STORE_KEY= ACTION_NAME + ".sort";
	
	private LexicalCSorter fSorter;
	private TreeViewer fTreeViewer;
	
	public LexicalSortingAction(TreeViewer treeViewer) {
		super(CPlugin.getResourceString(ACTION_NAME + ".label"));
		
		setDescription(CPlugin.getResourceString(ACTION_NAME + ".description"));
		setToolTipText(CPlugin.getResourceString(ACTION_NAME + ".tooltip"));
	
		CPluginImages.setImageDescriptors(this, CPluginImages.T_LCL, CPluginImages.IMG_ALPHA_SORTING);
	
		fTreeViewer= treeViewer;
		fSorter= new LexicalCSorter();
		
		boolean checked= CPlugin.getDefault().getDialogSettings().getBoolean(DIALOG_STORE_KEY);
		valueChanged(checked, false);
	}
	
	public void run() {
		valueChanged(isChecked(), true);
	}
	
	private void valueChanged(boolean on, boolean store) {
		setChecked(on);
		fTreeViewer.setSorter(on ? fSorter : null);
		
		String key= ACTION_NAME + ".tooltip" + (on ? ".on" : ".off");
		setToolTipText(CPlugin.getResourceString(key));
		
		if (store) {
			CPlugin.getDefault().getDialogSettings().put(DIALOG_STORE_KEY, on);
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
					case ICElement.C_MACRO: return 1;
					case ICElement.C_INCLUDE: return 2;
					
					case ICElement.C_CLASS: return 3;
					case ICElement.C_STRUCT: return 4;
					case ICElement.C_UNION: return 5;
					
					case ICElement.C_FIELD: return 6;
					case ICElement.C_FUNCTION: return 7;		
				}
				
			}
			return 0;
		}
	};
	
};
