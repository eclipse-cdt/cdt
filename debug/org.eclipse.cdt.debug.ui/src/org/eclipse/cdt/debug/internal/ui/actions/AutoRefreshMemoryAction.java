/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
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
import org.eclipse.cdt.debug.internal.ui.views.memory.MemoryViewer;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * The "Auto-Refresh" action of the Memory View.
 */
public class AutoRefreshMemoryAction extends SelectionProviderAction implements IUpdate {

	private MemoryViewer fMemoryViewer;

	/**
	 * Constructor for AutoRefreshMemoryAction.
	 */
	public AutoRefreshMemoryAction( MemoryViewer viewer ) {
		super( viewer, ActionMessages.getString( "AutoRefreshMemoryAction.0" ) ); //$NON-NLS-1$
		fMemoryViewer = viewer;
		CDebugImages.setLocalImageDescriptors( this, CDebugImages.IMG_LCL_AUTO_REFRESH );
		setDescription( ActionMessages.getString( "AutoRefreshMemoryAction.1" ) ); //$NON-NLS-1$
		setToolTipText( ActionMessages.getString( "AutoRefreshMemoryAction.2" ) ); //$NON-NLS-1$
		WorkbenchHelp.setHelp( this, ICDebugHelpContextIds.AUTO_REFRESH_MEMORY_ACTION );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update() {
		setEnabled( fMemoryViewer.canUpdate() );
		setChecked( !fMemoryViewer.isFrozen() );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		fMemoryViewer.setFrozen( !isChecked() );
	}
}
