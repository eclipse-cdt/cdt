/*****************************************************************
 * Copyright (c) 2010 Texas Instruments and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Patrick Chuong (Texas Instruments) - Pin and Clone Supports (331781)
 *     Patrick Chuong (Texas Instruments) - Add support for icon overlay in the debug view (Bug 334566)
 *****************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.cdt.debug.internal.ui.pinclone.DebugContextPinProvider;
import org.eclipse.cdt.debug.internal.ui.pinclone.DebugEventFilterService;
import org.eclipse.cdt.debug.internal.ui.pinclone.PinCloneUtils;
import org.eclipse.cdt.debug.ui.IPinProvider;
import org.eclipse.cdt.debug.ui.IPinProvider.IPinElementColorDescriptor;
import org.eclipse.cdt.debug.ui.IPinProvider.IPinElementHandle;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.debug.ui.contexts.IDebugContextService;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
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
import org.eclipse.ui.part.WorkbenchPart;

/**
 * Pin the selected debug context for the view. 
 */
public class PinDebugContextActionDelegate implements IViewActionDelegate, IActionDelegate2, IDebugContextListener {
	private IViewPart fPart;
	private String fPinnedContextLabel = ""; //$NON-NLS-1$
	private String fLastKnownDescription = ""; //$NON-NLS-1$
	private IAction fAction;
	private IPartListener2 fPartListener;
	private DebugContextPinProvider fProvider;
	
	public PinDebugContextActionDelegate() {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#runWithEvent(org.eclipse.jface.action.IAction, org.eclipse.swt.widgets.Event)
	 */
	@Override
	public void runWithEvent(IAction action, Event event) {
		run(action);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(IAction action) {
		if (action.isChecked()) {
			fProvider = DebugEventFilterService.getInstance().addDebugEventFilter(fPart, getActiveDebugContext());
			if (fProvider != null) {
				fLastKnownDescription = ((WorkbenchPart) fPart).getContentDescription();
				fPinnedContextLabel = getPinContextLabel(fProvider);
				PinCloneUtils.setPartContentDescription(fPart, fPinnedContextLabel);
				updatePinContextColor(fProvider); 
			}
		} else {
			fProvider = null;
			DebugEventFilterService.getInstance().removeDebugEventFilter(fPart);
			updatePinContextColor(fProvider);
			PinCloneUtils.setPartContentDescription(fPart, fLastKnownDescription);			
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void init(IAction action) {
		fAction = action;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	@Override
	public void init(IViewPart view) {
		fPart = view;

		if (fAction != null && !fAction.isChecked()) {
			IDebugContextService service = DebugUITools.getDebugContextManager().getContextService(fPart.getViewSite().getWorkbenchWindow());
			boolean pinnable = PinCloneUtils.isPinnable(fPart, service.getActiveContext());		
			fAction.setEnabled(pinnable);
		}
		
		fPart.addPropertyListener(new IPropertyListener() {			
			@Override
			public void propertyChanged(Object source, int propId) {
				if (IWorkbenchPartConstants.PROP_CONTENT_DESCRIPTION == propId) {
					// if the content description is not the pinned context label,
					// then cache it so that we can set it back when the action is unchecked.
					String desc = ((WorkbenchPart) fPart).getContentDescription();
					if (!fPinnedContextLabel.equals(desc)) {
						fLastKnownDescription = desc;
					}
					
					// if action is checked, than set it back to the pinned context label.
					if (fAction != null && fAction.isChecked()) {
						PinCloneUtils.setPartContentDescription(fPart, fPinnedContextLabel);
					}
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
			@Override
			public void partBroughtToTop(IWorkbenchPartReference partRef) {}
			@Override
			public void partClosed(IWorkbenchPartReference partRef) {
				IWorkbenchPart part = partRef.getPart(false);
				if (part.equals(fPart)) {
					if (fAction.isChecked()) {
						DebugEventFilterService.getInstance().removeDebugEventFilter(fPart);
						fAction.setChecked(false);
					}					
				}
			}
			@Override
			public void partDeactivated(IWorkbenchPartReference partRef) {}
			@Override
			public void partOpened(IWorkbenchPartReference partRef) {}
			@Override
			public void partHidden(IWorkbenchPartReference partRef) {}
			@Override
			public void partVisible(IWorkbenchPartReference partRef) {}
			@Override
			public void partInputChanged(IWorkbenchPartReference partRef) {}
			@Override
			public void partActivated(IWorkbenchPartReference partRef) {}
		};
		fPart.getSite().getWorkbenchWindow().getPartService().addPartListener(fPartListener);		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#dispose()
	 */
	@Override
	public void dispose() {
		DebugUITools.removePartDebugContextListener(fPart.getSite(), this);
		fPart.getSite().getWorkbenchWindow().getPartService().removePartListener(fPartListener);
	}
	
	protected ISelection getActiveDebugContext() {
		IDebugContextService contextService = 
			DebugUITools.getDebugContextManager().getContextService(fPart.getSite().getWorkbenchWindow());
		return contextService.getActiveContext();		
	}
	
	private String getPinContextLabel(DebugContextPinProvider provider) {
		String description = ""; //$NON-NLS-1$
		
		if (provider != null) {
			Set<String> labels = new HashSet<String>();
			for (IPinElementHandle handle : provider.getPinHandles()) {
				String tmp = getLabel(handle);
				if (tmp != null && tmp.trim().length() != 0)
					labels.add(tmp);
			}
			
			for (String label : labels) {
				if (label != null) {
					if (description.length() > 0) {
						description += ", " + label; //$NON-NLS-1$
					} else {
						description = label;
					}
				}
			}
		}
		return description;
	}
	
	private String getLabel(IPinElementHandle handle) {
		String label = ""; //$NON-NLS-1$

		if (handle != null)
			label = handle.getLabel();
		
		return label;
	}

	private boolean useMultiPinImage(Set<IPinElementHandle> handles) {
		if (handles.size() <= 1) return false;
		
		int overlayColor = IPinElementColorDescriptor.UNDEFINED;
		ImageDescriptor imageDesc = null;
		for (IPinElementHandle handle : handles) {
			IPinElementColorDescriptor colorDesc = handle.getPinElementColorDescriptor();
			if (colorDesc != null) {
				ImageDescriptor descImageDesc = colorDesc.getToolbarIconDescriptor();
				if (imageDesc != null && !imageDesc.equals(descImageDesc))
					return true;
				imageDesc = descImageDesc;
				
				int descOverlayColor = colorDesc.getOverlayColor();
				if (overlayColor != IPinElementColorDescriptor.UNDEFINED && descOverlayColor != overlayColor)
					return true;
				overlayColor = descOverlayColor;
			}
		}
		
		return false;
	}
	
	private void updatePinContextColor(DebugContextPinProvider provider) {
		ImageDescriptor imageDesc = null;
		if (provider != null) {
			Set<IPinElementHandle> handles = provider.getPinHandles();
			
			// if handles have different toolbar icon descriptor or different pin color, than use a 
			// multi-pin toolbar icon
			if (useMultiPinImage(handles))
				imageDesc =  CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_VIEW_PIN_ACTION_MULTI);
			
			if (imageDesc == null) {
				Iterator<IPinElementHandle> itr = handles.iterator();
				if (itr.hasNext()) {
					IPinElementHandle handle = itr.next();
					IPinElementColorDescriptor desc = handle.getPinElementColorDescriptor();
					if (desc != null)
						imageDesc = desc.getToolbarIconDescriptor();
					
					if (imageDesc == null && desc != null) {
						int overlayColor = desc.getOverlayColor() % IPinElementColorDescriptor.DEFAULT_COLOR_COUNT;
						
						switch (overlayColor) {
						case IPinProvider.IPinElementColorDescriptor.GREEN:
							imageDesc = CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_VIEW_PIN_ACTION_G);
							break;
						case IPinProvider.IPinElementColorDescriptor.RED:
							imageDesc = CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_VIEW_PIN_ACTION_R);
							break;
						case IPinProvider.IPinElementColorDescriptor.BLUE:
							imageDesc = CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_VIEW_PIN_ACTION_B);
							break;
						}
					}
				}
			}
		}
		
		if (imageDesc == null)
			imageDesc = CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_VIEW_PIN_ACTION);
		fAction.setImageDescriptor(imageDesc);
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.contexts.IDebugContextListener#debugContextChanged(org.eclipse.debug.ui.contexts.DebugContextEvent)
	 */
	@Override
	public void debugContextChanged(DebugContextEvent event) {
		if (fAction != null && !fAction.isChecked()) {
			final boolean pinnable = PinCloneUtils.isPinnable(fPart, event.getContext());
			if (pinnable != fAction.isEnabled()) {			
				PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {				
					@Override
					public void run() {
						fAction.setEnabled(pinnable);
					}
				});
			}
		}
	}
}
