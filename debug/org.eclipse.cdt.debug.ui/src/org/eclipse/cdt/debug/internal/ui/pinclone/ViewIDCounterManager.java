/*****************************************************************
 * Copyright (c) 2010, 2011 Texas Instruments and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Patrick Chuong (Texas Instruments) - Pin and Clone Supports (331781)
 *****************************************************************/
package org.eclipse.cdt.debug.internal.ui.pinclone;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * This class provides counter id for view that support multiple instances.
 * It is assumed that view will use the counter id for it's secondary id.
 */
public final class ViewIDCounterManager {
	private static ViewIDCounterManager INSTANCE;
	private static boolean fInitialized = false;
	
	private boolean fShuttingDown = false;
	private final Map<String, Set<Integer>> viewIdToNextCounterMap = Collections.synchronizedMap(new HashMap<String, Set<Integer>>());
	
	private ViewIDCounterManager() {
		initListeners();
	}
	
	/**
	 * Returns an instance of the view id counter manager.
	 * 
	 * @return the counter manager.
	 */
	synchronized public static ViewIDCounterManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ViewIDCounterManager();
		}
		return INSTANCE;
	}	
	
	/**
	 * Initialize this view ID counter manager. Catch up opened view and set the title
	 * accordingly from the view's secondary id.
	 */
	synchronized public void init() {
		if (fInitialized) return;
		fInitialized = true;
		
		new WorkbenchJob("Initializing pinnable view") { //$NON-NLS-1$
			{ setSystem(true); }
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
				for (IWorkbenchWindow window : windows) {
					IViewReference[] viewRefs = window.getActivePage().getViewReferences();
					for (IViewReference viewRef : viewRefs) {
						try {
							// initialize the view id counter map
						    if (PinCloneUtils.isClonedPart(viewRef)) {
						    	String id = viewRef.getId();
						    	String secondaryId = viewRef.getSecondaryId();
						        Set<Integer> secondaryIdSet = viewIdToNextCounterMap.get(id);
						        if (secondaryIdSet == null) {
						            secondaryIdSet = new HashSet<Integer>();
						            viewIdToNextCounterMap.put(id, secondaryIdSet);
						        }
						        secondaryId = PinCloneUtils.decodeClonedPartSecondaryId(secondaryId);   
						        secondaryIdSet.add(Integer.valueOf(secondaryId));
						    }
						    
						    // set the view title
						    IViewPart part = viewRef.getView(false);
						    if (part != null && PinCloneUtils.isClonedPart(part)) {
						        PinCloneUtils.setPartTitle(part);
						    }						
						} catch (Exception e) {
							CDebugUIPlugin.log(e);					
						}
					}			
				}
				return Status.OK_STATUS;
			}
		}.schedule();
	}
	
	private void initListeners() {
		try {
			// add a workbench listener to listen to preShutdown and ignore view part close event
			IWorkbench wb = PlatformUI.getWorkbench();
			wb.addWorkbenchListener(new IWorkbenchListener() {
				@Override
				public void postShutdown(IWorkbench workbench) {}
				
				@Override
				public boolean preShutdown(IWorkbench workbench, boolean forced) {
					fShuttingDown = true;
					return true;
				}
			});
			
			final IPartListener2 partListener = new IPartListener2() {
				@Override
				public void partVisible(IWorkbenchPartReference partRef) {}					
				@Override
				public void partInputChanged(IWorkbenchPartReference partRef) {}						
				@Override
				public void partHidden(IWorkbenchPartReference partRef) {}						
				@Override
				public void partDeactivated(IWorkbenchPartReference partRef) {}																		
				@Override
				public void partBroughtToTop(IWorkbenchPartReference partRef) {}						
				@Override
				public void partActivated(IWorkbenchPartReference partRef) {}

				@Override
				public void partOpened(IWorkbenchPartReference partRef) {
					if (partRef instanceof IViewReference) {
						IViewPart part = ((IViewReference) partRef).getView(false);
						if (part != null && PinCloneUtils.isClonedPart(part)) {
							PinCloneUtils.setPartTitle(part);
						}
					}					
				}
				
				@Override
				public void partClosed(IWorkbenchPartReference partRef) {
					if (!fShuttingDown)
						recycleCounterId(partRef);
				}
			};
			
			// subscribe to existing workbench window listener
			for (IWorkbenchWindow ww : wb.getWorkbenchWindows()) {
				ww.getPartService().addPartListener(partListener);
			}
			
			// subscribe to new workbench window listener
			wb.addWindowListener(new IWindowListener() {					
				@Override
				public void windowDeactivated(IWorkbenchWindow window) {}												
				@Override
				public void windowActivated(IWorkbenchWindow window) {}				
				@Override
				public void windowClosed(IWorkbenchWindow window) {}
				
				@Override
				public void windowOpened(IWorkbenchWindow window) {
					window.getPartService().addPartListener(partListener);
				}		
			});
		} catch (Exception e) {
			CDebugUIPlugin.log(e);
		}
	}
	
	private void recycleCounterId(IWorkbenchPartReference partRef) {
		if (partRef instanceof IViewReference) {
			IViewReference viewRef = ((IViewReference) partRef);
			IWorkbenchPart part = viewRef.getPart(false);
			if ( !(part instanceof IViewPart) || !PinCloneUtils.isClonedPart((IViewPart) part))
				return;
			
			String viewId = viewRef.getId();
			String secondaryId = viewRef.getSecondaryId();
			
			if (secondaryId != null) {
				Set<Integer> secondaryIdSet = viewIdToNextCounterMap.get(viewId);
				if (secondaryIdSet != null) {	
					secondaryIdSet.remove(new Integer(PinCloneUtils.decodeClonedPartSecondaryId(secondaryId)));
				}
			}
		}
	}
	
	public Integer getNextCounter(String viewId) {
		Set<Integer> secondaryIdSet = viewIdToNextCounterMap.get(viewId);
		if (secondaryIdSet == null) {
			secondaryIdSet = new HashSet<Integer>();
			viewIdToNextCounterMap.put(viewId, secondaryIdSet);
		}
		
		for (int i = 1; i < Integer.MAX_VALUE; ++i) {
			Integer next = new Integer(i);
			if (!secondaryIdSet.contains(next)) {
				secondaryIdSet.add(next);
				return next;
			}
		}
		
		return Integer.valueOf(0);
	}
}
