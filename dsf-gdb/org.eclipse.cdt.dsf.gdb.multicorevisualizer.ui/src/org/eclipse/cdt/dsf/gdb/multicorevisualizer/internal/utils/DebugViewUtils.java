/*******************************************************************************
 * Copyright (c) 2012 Tilera Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     William R. Swanson (Tilera Corporation)
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.utils;

import org.eclipse.cdt.visualizer.ui.util.GUIUtils;
import org.eclipse.cdt.visualizer.ui.util.RunnableWithResult;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.debug.internal.ui.views.launch.LaunchView;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;


/** Assorted utilities for interacting with the Debug View (aka LaunchView). */
@SuppressWarnings("restriction") // allow access to internal classes
public class DebugViewUtils
{
	// --- static utility methods ---
	
    /**
     * Returns Debug View (a.k.a. LaunchView).
     */
    public static LaunchView getDebugView() {
		return (LaunchView) getViewWithID(IDebugUIConstants.ID_DEBUG_VIEW);
    }

    /**
     * Returns tree model viewer for Debug View (a.k.a. LaunchView).
     */
    public static TreeModelViewer getDebugViewer() {
    	LaunchView debugView = getDebugView();
    	TreeModelViewer viewer = (debugView == null) ? null : (TreeModelViewer) debugView.getViewer();
    	return viewer;
    }

    /** Gets workbench view (if any) with specified ID. */
    public static IViewPart getViewWithID(String viewID) {
        final String viewID_f = viewID;
        RunnableWithResult<IViewPart> runnable =
        	new RunnableWithResult<IViewPart>() {
    			@Override
				public IViewPart call() {
    				IViewPart view = null;
					IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					if (activePage != null) {
	    				view = (viewID_f == null) ? null : activePage.findView(viewID_f);
					}
					return view;
    			}
        	};
        // run on UI thread, wait for result
        GUIUtils.execAndWait(runnable);
        IViewPart result = runnable.getResult();
        return result;
    }
    
    /**
     * Sets debug view selection.
     * Specified selection is an IStructuredSelection containing a flat list
     * of the model objects (that is, _not_ the tree view nodes) to be selected.
     */
    public static boolean setDebugViewSelection(ISelection selection)
    {
		TreeModelViewer viewer = DebugViewUtils.getDebugViewer();
		if (viewer == null || selection == null) return false;
		return viewer.trySelection(selection, true, true);
    }
}
