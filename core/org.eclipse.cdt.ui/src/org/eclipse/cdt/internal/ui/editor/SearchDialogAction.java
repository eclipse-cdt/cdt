/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Aug 12, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.cdt.internal.ui.editor;

import java.util.List;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * @author bgheorgh
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class SearchDialogAction extends Action {
	private static final String PREFIX= "SearchDialogAction."; //$NON-NLS-1$
	private static final String C_SEARCH_PAGE_ID= "org.eclipse.cdt.ui.CSearchPage";  //$NON-NLS-1$

	private ISelectionProvider fSelectionProvider;
	private ITextEditor fEditor;
	private IWorkbenchWindow fWorkbenchWindow;
	
	public SearchDialogAction(ISelectionProvider provider, CEditor editor) {
		super(CUIPlugin.getResourceString(PREFIX + "label")); //$NON-NLS-1$
		setDescription(CUIPlugin.getResourceString(PREFIX + "description")); //$NON-NLS-1$
		setToolTipText(CUIPlugin.getResourceString(PREFIX + "tooltip")); //$NON-NLS-1$
		
		if(provider instanceof CContentOutlinePage) {
			setImageDescriptor( CPluginImages.DESC_OBJS_CSEARCH );
		}
		
		fSelectionProvider= provider;
		fEditor = editor;
	}
	
	public SearchDialogAction(ISelectionProvider provider, IWorkbenchWindow window) {
		
		super(CUIPlugin.getResourceString(PREFIX + "label")); //$NON-NLS-1$
		setDescription(CUIPlugin.getResourceString(PREFIX + "description")); //$NON-NLS-1$
		setToolTipText(CUIPlugin.getResourceString(PREFIX + "tooltip")); //$NON-NLS-1$
		
		if(provider instanceof CContentOutlinePage) {
			setImageDescriptor( CPluginImages.DESC_OBJS_CSEARCH );
		}
		
		fSelectionProvider= provider;
		fWorkbenchWindow = window;
	}
			
	public void run() {
		String search_name;
		
		ISelection selection= fSelectionProvider.getSelection();
		if(selection instanceof ITextSelection) {
			search_name = ((ITextSelection)selection).getText();
			if(search_name.length() == 0) return;
		} else {
			ICElement element= getElement(selection);
			if (element == null) {
				return;
			}
			search_name = element.getElementName();
		}
		
		if (fEditor != null){
			NewSearchUI.openSearchDialog(fEditor.getEditorSite().getWorkbenchWindow(),C_SEARCH_PAGE_ID);
		} 
		else if (fWorkbenchWindow != null){
			NewSearchUI.openSearchDialog(fWorkbenchWindow,C_SEARCH_PAGE_ID);
		}
	}


	private static ICElement getElement(ISelection sel) {
		if (!sel.isEmpty() && sel instanceof IStructuredSelection) {
			List list= ((IStructuredSelection)sel).toList();
			if (list.size() == 1) {
				Object element= list.get(0);
				if (element instanceof ICElement) {
					return (ICElement)element;
				}
			}
		}
		return null;
	}
	
	public static boolean canActionBeAdded(ISelection selection) {
		if(selection instanceof ITextSelection) {
			return (((ITextSelection)selection).getLength() > 0);
		} else {
			return getElement(selection) != null;
		}
	}	


	public static String getEditorID(String name) {
		IEditorRegistry registry = PlatformUI.getWorkbench().getEditorRegistry();
		if (registry != null) {
			IEditorDescriptor descriptor = registry.getDefaultEditor(name);
			if (descriptor != null) {
				return descriptor.getId();
			} else {
				//getDefaultEditor is deprecated, The system external editor is the default editor
				return IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID;

			}
		}
		return null;
	}

}