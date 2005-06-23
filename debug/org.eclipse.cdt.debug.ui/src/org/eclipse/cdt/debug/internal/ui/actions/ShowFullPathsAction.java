/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
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
import org.eclipse.cdt.debug.internal.ui.CDebugModelPresentation;
import org.eclipse.cdt.debug.internal.ui.preferences.ICDebugPreferenceConstants;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.custom.BusyIndicator;

/**
 * An action delegate that toggles the state of its viewer to show/hide full paths.
 */
public class ShowFullPathsAction extends ViewFilterAction {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.ViewFilterAction#getPreferenceKey()
	 */
	protected String getPreferenceKey() {
		return ICDebugPreferenceConstants.PREF_SHOW_FULL_PATHS; 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public boolean select( Viewer viewer, Object parentElement, Object element ) {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run( IAction action ) {
		final StructuredViewer viewer = getStructuredViewer();
		IDebugView view = (IDebugView)getView().getAdapter( IDebugView.class );
		if (view != null) {
			IDebugModelPresentation pres = view.getPresentation( CDIDebugModel.getPluginIdentifier() );
			if ( pres != null ) {
				pres.setAttribute( CDebugModelPresentation.DISPLAY_FULL_PATHS, ( getValue() ? Boolean.TRUE : Boolean.FALSE ) );
				BusyIndicator.showWhile( viewer.getControl().getDisplay(), 
										new Runnable() {
											public void run() {
												viewer.refresh();
												IPreferenceStore store = getPreferenceStore();
												String key = getView().getSite().getId() + "." + getPreferenceKey(); //$NON-NLS-1$
												store.setValue( key, getValue() );
												CDebugUIPlugin.getDefault().savePluginPreferences();						
											}
										} );
			}
		}		
	}
}
