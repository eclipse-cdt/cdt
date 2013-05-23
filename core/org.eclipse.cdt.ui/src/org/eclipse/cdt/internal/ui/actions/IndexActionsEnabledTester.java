/*******************************************************************************
 * Copyright (c) 2013 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.actions;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.ide.ResourceUtil;

import org.eclipse.cdt.internal.ui.search.actions.FindUnresolvedIncludesProjectAction;

public class IndexActionsEnabledTester extends PropertyTester implements ISelectionListener, IWindowListener {

	private static RebuildIndexAction fRebuildIndexAction = new RebuildIndexAction();
	private static CreateParserLogAction fCreateParserLogAction = new CreateParserLogAction();
	private static FreshenIndexAction fFreshenAllFiles = new FreshenIndexAction();
	private static UpdateUnresolvedIncludesAction fUpdateUnresolvedIncludes = new UpdateUnresolvedIncludesAction();
	private static UpdateIndexWithModifiedFilesAction fUpdateWithModifiedFiles = new UpdateIndexWithModifiedFilesAction();
	private static FindUnresolvedIncludesProjectAction fFindUnresolvedIncludes = new FindUnresolvedIncludesProjectAction();

	/* (non-Javadoc)
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue)
	{
		if(property.equals("enabled")) { //$NON-NLS-1$
			return fRebuildIndexAction.isEnabled() 
					|| fCreateParserLogAction.isEnabled() 
					|| fFreshenAllFiles.isEnabled() 
					|| fUpdateUnresolvedIncludes.isEnabled()  
					|| fUpdateWithModifiedFiles.isEnabled() 
					|| fFindUnresolvedIncludes.isEnabled();
		} else if(property.equals("rebuild")) { //$NON-NLS-1$
			return fRebuildIndexAction.isEnabled();
		} else if(property.equals("log")) { //$NON-NLS-1$
			return fCreateParserLogAction.isEnabled();
		} else if(property.equals("freshen")) { //$NON-NLS-1$
			return fFreshenAllFiles.isEnabled();
		} else if(property.equals("updateUnresolvedIncludes")) { //$NON-NLS-1$
			return fUpdateUnresolvedIncludes.isEnabled();
		} else if(property.equals("udpateWithModifiedFiles")) { //$NON-NLS-1$
			return fUpdateWithModifiedFiles.isEnabled();
		} else if(property.equals("findUnresolvedIncludes")) { //$NON-NLS-1$
			return fFindUnresolvedIncludes.isEnabled();
		}
		return false;
	}

	private IStructuredSelection getSelectedItem(Object part, Object selection) {
        if((selection != null) && (selection instanceof IStructuredSelection)) {
        	return (IStructuredSelection)selection;
        }
    	if((part != null) && (part instanceof IEditorPart)) {
    		Object selItem = null;
    		selItem = ResourceUtil.getResource(((IEditorPart)part).getEditorInput());
    		if(selItem != null) {
    			return new StructuredSelection(selItem);
    		}
    	}
        return StructuredSelection.EMPTY;
	}
	
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		IStructuredSelection sel = getSelectedItem(part, selection);
		fRebuildIndexAction.selectionChanged(sel);
		fCreateParserLogAction.selectionChanged(sel);
		fFreshenAllFiles.selectionChanged(sel);
		fUpdateUnresolvedIncludes.selectionChanged(sel);
		fUpdateWithModifiedFiles.selectionChanged(sel);
		fFindUnresolvedIncludes.selectionChanged(sel);
	}

	@Override
	public void windowActivated(IWorkbenchWindow window) {
		ISelectionService selectionService = window.getSelectionService();
		if (selectionService != null) {
			ISelection sel = selectionService.getSelection();
			selectionChanged(null, sel);
		}
	}

	@Override
	public void windowDeactivated(IWorkbenchWindow window) {
	}

	@Override
	public void windowClosed(IWorkbenchWindow window) {
		ISelectionService selectionService = window.getSelectionService();
		if (selectionService != null) {
			selectionService.removeSelectionListener(this);
		}
	}

	@Override
	public void windowOpened(IWorkbenchWindow window) {
		ISelectionService selectionService = window.getSelectionService();
		if (selectionService != null) {
			selectionService.addSelectionListener(this);
		}
	}
}
