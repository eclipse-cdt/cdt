/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/ 
package org.eclipse.cdt.debug.internal.ui.actions; 

import org.eclipse.cdt.debug.internal.ui.CDebugImages;
import org.eclipse.cdt.debug.internal.ui.ICDebugHelpContextIds;
import org.eclipse.cdt.debug.internal.ui.preferences.ICDebugPreferenceConstants;
import org.eclipse.cdt.debug.internal.ui.views.modules.ModulesView;
import org.eclipse.jface.action.Action;
 
/**
 * Action that controls the appearance of the details pane in debug views 
 * such as the modules view. Instances of this class can be created to show 
 * the detail pane underneath the main tree, to the right of the main tree, 
 * or not shown at all.
 */
public class ToggleDetailPaneAction extends Action {

	private ModulesView fModulesView;
	
	private String fOrientation;

	/** 
	 * Constructor for ToggleDetailPaneAction. 
	 */
	public ToggleDetailPaneAction( ModulesView view, String orientation, String hiddenLabel ) {
		super( "", AS_RADIO_BUTTON ); //$NON-NLS-1$
		setModulesView( view );
		setOrientation( orientation );
		if ( orientation == ICDebugPreferenceConstants.MODULES_DETAIL_PANE_UNDERNEATH ) {
			setText( ActionMessages.getString( "ToggleDetailPaneAction.0" ) ); //$NON-NLS-1$
			setToolTipText( ActionMessages.getString( "ToggleDetailPaneAction.1" ) ); //$NON-NLS-1$
			setDescription( ActionMessages.getString( "ToggleDetailPaneAction.2" ) ); //$NON-NLS-1$
			setImageDescriptor( CDebugImages.DESC_LCL_DETAIL_PANE_UNDER );
			setDisabledImageDescriptor( CDebugImages.DESC_LCL_DETAIL_PANE_UNDER_DISABLED );
			setHoverImageDescriptor( CDebugImages.DESC_LCL_DETAIL_PANE_UNDER );
		}
		else if ( orientation == ICDebugPreferenceConstants.MODULES_DETAIL_PANE_RIGHT ) {
			setText( ActionMessages.getString( "ToggleDetailPaneAction.3" ) ); //$NON-NLS-1$
			setToolTipText( ActionMessages.getString( "ToggleDetailPaneAction.4" ) ); //$NON-NLS-1$
			setDescription( ActionMessages.getString( "ToggleDetailPaneAction.5" ) ); //$NON-NLS-1$
			setImageDescriptor( CDebugImages.DESC_LCL_DETAIL_PANE_RIGHT );
			setDisabledImageDescriptor( CDebugImages.DESC_LCL_DETAIL_PANE_RIGHT_DISABLED );
			setHoverImageDescriptor( CDebugImages.DESC_LCL_DETAIL_PANE_RIGHT );
		}
		else {
			setText( hiddenLabel );
			setToolTipText( ActionMessages.getString( "ToggleDetailPaneAction.6" ) ); //$NON-NLS-1$
			setDescription( ActionMessages.getString( "ToggleDetailPaneAction.7" ) ); //$NON-NLS-1$
			setImageDescriptor( CDebugImages.DESC_LCL_DETAIL_PANE_HIDE );
			setDisabledImageDescriptor( CDebugImages.DESC_LCL_DETAIL_PANE_HIDE_DISABLED );
			setHoverImageDescriptor( CDebugImages.DESC_LCL_DETAIL_PANE_HIDE );
		}
		view.getSite().getWorkbenchWindow().getWorkbench().getHelpSystem().setHelp( this, ICDebugHelpContextIds.SHOW_DETAIL_PANE_ACTION );
	}
	
	private ModulesView getModulesView() {
		return fModulesView;
	}

	private void setModulesView( ModulesView modulesView ) {
		fModulesView = modulesView;
	}

	private void setOrientation( String orientation ) {
		fOrientation = orientation;
	}

	public String getOrientation() {
		return fOrientation;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		getModulesView().setDetailPaneOrientation( getOrientation() ); 
	}
}
