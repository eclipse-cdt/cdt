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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.debug.internal.ui.pinclone.DebugContextPinProvider;
import org.eclipse.cdt.debug.internal.ui.pinclone.DebugEventFilterService;
import org.eclipse.cdt.debug.internal.ui.pinclone.PinCloneUtils;
import org.eclipse.cdt.debug.ui.IPinProvider.IPinElementHandle;
import org.eclipse.cdt.debug.ui.IPinProvider.IPinHandleLabelProvider;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.debug.ui.contexts.IDebugContextService;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;

/**
 * Pin the selected debug context for the view. 
 */
public class PinDebugContextActionDelegate implements IViewActionDelegate, IActionDelegate2, IDebugContextListener {
	IViewPart fPart;
	IAction fAction;
	IPartListener2 fPartListener;
	DebugContextPinProvider fProvider;
	
	public PinDebugContextActionDelegate() {}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#runWithEvent(org.eclipse.jface.action.IAction, org.eclipse.swt.widgets.Event)
	 */
	public void runWithEvent(IAction action, Event event) {
		run(action);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		if (action.isChecked()) {
			fProvider = DebugEventFilterService.getInstance().addDebugEventFilter(fPart, getActiveDebugContext());
			if (fProvider != null) {
				// TODO: set image descriptor
				updatePinContextLabel(fProvider);
			}			
		} else {
			fProvider = null;
			DebugEventFilterService.getInstance().removeDebugEventFilter(fPart);
			// TODO: remove image descriptor
			updatePinContextLabel(fProvider);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
	 */
	public void init(IAction action) {
		fAction = action;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init(IViewPart view) {
		fPart = view;

		if (fAction != null && !fAction.isChecked()) {
			IDebugContextService service = DebugUITools.getDebugContextManager().getContextService(fPart.getViewSite().getWorkbenchWindow());
			boolean pinnable = PinCloneUtils.isPinnable(fPart, service.getActiveContext());		
			fAction.setEnabled(pinnable);
		}
		
		fPart.addPropertyListener(new IPropertyListener() {			
			public void propertyChanged(Object source, int propId) {
				if (IWorkbenchPartConstants.PROP_CONTENT_DESCRIPTION == propId) {
					updatePinContextLabel(fProvider);
				} else if (IWorkbenchPartConstants.PROP_PART_NAME == propId) {
					PinCloneUtils.setPartTitle(fPart);
				}
			}
		});		
		
		DebugUITools.addPartDebugContextListener(fPart.getSite(), this);
		
		// Platform AbstractDebugView saves action check state,
		// in our case, we don't want this behavior.
		// Listens to part close and set the check state off.
		fPartListener = new IPartListener2() {					
			public void partBroughtToTop(IWorkbenchPartReference partRef) {}
			public void partClosed(IWorkbenchPartReference partRef) {
				IWorkbenchPart part = partRef.getPart(false);
				if (part.equals(fPart)) {
					if (fAction.isChecked()) {
						DebugEventFilterService.getInstance().removeDebugEventFilter(fPart);
						fAction.setChecked(false);
					}					
				}
			}
			public void partDeactivated(IWorkbenchPartReference partRef) {}
			public void partOpened(IWorkbenchPartReference partRef) {}
			public void partHidden(IWorkbenchPartReference partRef) {}
			public void partVisible(IWorkbenchPartReference partRef) {}
			public void partInputChanged(IWorkbenchPartReference partRef) {}
			public void partActivated(IWorkbenchPartReference partRef) {}
		};
		fPart.getSite().getWorkbenchWindow().getPartService().addPartListener(fPartListener);		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#dispose()
	 */
	public void dispose() {
		DebugUITools.removePartDebugContextListener(fPart.getSite(), this);
		fPart.getSite().getWorkbenchWindow().getPartService().removePartListener(fPartListener);
	}
	
	protected ISelection getActiveDebugContext() {
		IDebugContextService contextService = 
			DebugUITools.getDebugContextManager().getContextService(fPart.getSite().getWorkbenchWindow());
		return contextService.getActiveContext();		
	}
	
	private void updatePinContextLabel(DebugContextPinProvider provider) {
		String description = ""; //$NON-NLS-1$
		
		if (provider != null) {
			Set<String> labels = new HashSet<String>();
			Set<IPinElementHandle> handles = provider.getPinHandles();
			for (IPinElementHandle handle : handles) {
				String tmp = getLabel(handle);
				if (tmp != null && tmp.trim().length() != 0)
					labels.add(tmp);
			}
			
			for (String label : labels) {
				if (label != null) {
					if (description.length() > 0) {
						description += "," + label; //$NON-NLS-1$
					} else {
						description = label;
					}
				}
			}
		}
				
		PinCloneUtils.setPartContentDescription(fPart, description);
	}
	
	private String getLabel(IPinElementHandle handle) {
		String label = ""; //$NON-NLS-1$

		if (handle instanceof IAdaptable) {
			IPinHandleLabelProvider provider = 
				(IPinHandleLabelProvider) ((IAdaptable) handle).getAdapter(IPinHandleLabelProvider.class);
			if (provider != null)
				label = provider.getLabel();
		}
		
		return label;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.contexts.IDebugContextListener#debugContextChanged(org.eclipse.debug.ui.contexts.DebugContextEvent)
	 */
	public void debugContextChanged(DebugContextEvent event) {
		if (fAction != null && !fAction.isChecked()) {
			final boolean pinnable = PinCloneUtils.isPinnable(fPart, event.getContext());
			if (pinnable != fAction.isEnabled()) {			
				PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {				
					public void run() {
						fAction.setEnabled(pinnable);
					}
				});
			}
		}
	}
}
