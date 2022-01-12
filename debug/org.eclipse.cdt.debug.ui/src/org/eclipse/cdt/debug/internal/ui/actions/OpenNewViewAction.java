/*******************************************************************************
 * Copyright (c) 2010, 2014 Texas Instruments and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Patrick Chuong (Texas Instruments) - Initial implementation of run()
 *     Marc Dumais (Ericsson) - Bug 437692
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.internal.ui.CDebugImages;
import org.eclipse.cdt.debug.internal.ui.pinclone.PinCloneUtils;
import org.eclipse.cdt.debug.internal.ui.pinclone.ViewIDCounterManager;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

/**
 * Opens a new view of a configured type.
 */
public class OpenNewViewAction extends Action {
	private IViewPart fView;

	public OpenNewViewAction() {
	}

	@Override
	public String getText() {
		return ActionMessages.getString("OpenNewViewActionText"); //$NON-NLS-1$
	}

	@Override
	public String getId() {
		return "org.eclipse.cdt.debug.ui.toolbar.openNewView"; //$NON-NLS-1$
	}

	@Override
	public String getToolTipText() {
		return ActionMessages.getString("OpenNewViewActionTooltipText"); //$NON-NLS-1$
	}

	@Override
	public ImageDescriptor getDisabledImageDescriptor() {
		return CDebugImages.DESC_LCL_OPEN_NEW_VIEW;
	}

	@Override
	public ImageDescriptor getHoverImageDescriptor() {
		return CDebugImages.DESC_LCL_OPEN_NEW_VIEW;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return CDebugImages.DESC_LCL_OPEN_NEW_VIEW;
	}

	@Override
	public void run() {
		if (fView == null)
			return;

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

	/** Sets the view, that this action will open a new instance of */
	public void init(IViewPart view) {
		fView = view;
	}
}
