/*****************************************************************
 * Copyright (c) 2010 Texas Instruments and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Patrick Chuong (Texas Instruments) - Pin and Clone Supports (331781)
 *****************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.internal.ui.pinclone.PinCloneUtils;
import org.eclipse.cdt.debug.internal.ui.pinclone.ViewIDCounterManager;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

/**
 * Opens a new view of the same type.
 */
public class OpenNewViewActionDelegate implements IViewActionDelegate {
	private IViewPart fView;
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(IAction action) {
		IViewSite site = fView.getViewSite();
		String viewId = site.getId();
		IWorkbenchWindow ww = fView.getViewSite().getWorkbenchWindow();
		if (ww != null) {
			Integer secondaryId = null;
			boolean assignSecondaryId = false;
			
			// if there is a view without a secondary id, than get the next available id.
			IViewReference[] viewRefs = ww.getActivePage().getViewReferences();
			for (IViewReference viewRef : viewRefs) {
				if (viewId.equals(viewRef.getId()) && (viewRef.getSecondaryId() == null)) {
					assignSecondaryId = true;
					break;
				}					
			}
			if (assignSecondaryId)
				secondaryId = ViewIDCounterManager.getInstance().getNextCounter(viewId);
			
			try {
				ww.getActivePage().showView(viewId, 
					secondaryId != null ? PinCloneUtils.encodeClonedPartSecondaryId(secondaryId.toString()) : null,
					IWorkbenchPage.VIEW_ACTIVATE);
			} catch (PartInitException e) {
				CDebugUIPlugin.log(e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	@Override
	public void init(IViewPart view) {
		fView = view;
	}
}
