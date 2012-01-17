/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.internal.core.ICDebugInternalConstants;
import org.eclipse.cdt.debug.internal.ui.CDebugModelPresentation;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointsView;
import org.eclipse.debug.internal.ui.views.launch.LaunchView;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.custom.BusyIndicator;

/**
 * An action delegate that toggles the state of its viewer to show/hide full
 * paths. Note that we are not a filtering action (thus we unconditionally
 * return true in {@link #select(Viewer, Object, Object)}), but we extend
 * ViewFilterAction to get some basic, useful action behavior.
 */
public class ShowFullPathsAction extends ViewFilterAction {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.ViewFilterAction#getPreferenceKey()
	 */
	@Override
	protected String getPreferenceKey() {
		return ICDebugInternalConstants.SHOW_FULL_PATHS_PREF_KEY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public boolean select( Viewer viewer, Object parentElement, Object element ) {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run( IAction action ) {
		final StructuredViewer viewer = getStructuredViewer();
		IDebugView view = (IDebugView)getView().getAdapter( IDebugView.class );
		if (view != null) {
			IDebugModelPresentation pres = view.getPresentation( CDIDebugModel.getPluginIdentifier() );
			if ( pres != null ) {
				pres.setAttribute( CDebugModelPresentation.DISPLAY_FULL_PATHS, Boolean.valueOf( getValue() ) );
				BusyIndicator.showWhile( viewer.getControl().getDisplay(), 
										new Runnable() {
											@Override
											public void run() {
												String key = getView().getSite().getId() + "." + getPreferenceKey(); //$NON-NLS-1$
												getPreferenceStore().setValue( key, getValue() );
												CDebugUIPlugin.getDefault().savePluginPreferences();						

												// Refresh the viewer after we've set the preference because
												// DSF-based debuggers trigger off this preference.
												viewer.refresh();
											}
										} );
			}
		}		
	}
	
	/*
	 * Some debugger integrations don`t use debugTargets (e.g., DSF), so we
	 * verify if the launch has the proper attribute instead.
	 * If we don`t find any launches that allow us to enable the action, we should
     * call our parent class to keep any previous debugger integration properly
     * working with this feature.
     */
	 /** @since 7.0 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		IDebugView view = (IDebugView)getView().getAdapter(IDebugView.class);
		
		// Debug view
		if (view instanceof LaunchView) {
			ILaunchManager launchmgr = DebugPlugin.getDefault().getLaunchManager();
			ILaunch[] launches = launchmgr.getLaunches();
			for (ILaunch launch : launches) {
				if (launch.getAttribute(getPreferenceKey()) != null &&
						launch.isTerminated() == false) {
					setEnabled(true);
					return;
				}
			}
		}
		
		// Breakpoints view
		else if (view instanceof BreakpointsView) {
			IBreakpointManager bkptmgr = DebugPlugin.getDefault().getBreakpointManager();
			IBreakpoint[] bkpts = bkptmgr.getBreakpoints();
			for (IBreakpoint bkpt : bkpts) {
				try {
					Object attr = bkpt.getMarker().getAttribute(ICDebugInternalConstants.ATTR_CAPABLE_OF_SHOW_FULL_PATHS);
					if (attr != null) {
						setEnabled(true);
						return;
					}
				} catch (Exception e) {/* ignore */}
			}
		}
		super.selectionChanged(action, selection);
	}
}
