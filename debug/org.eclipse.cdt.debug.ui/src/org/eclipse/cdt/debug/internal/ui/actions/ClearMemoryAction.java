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
 * The "Clear" action of the Memory view.
 */
public class ClearMemoryAction extends SelectionProviderAction implements IUpdate {

	private MemoryViewer fMemoryViewer;

	/**
	 * Constructor for ClearMemoryAction.
	 */
	public ClearMemoryAction( MemoryViewer viewer ) {
		super( viewer, ActionMessages.getString( "ClearMemoryAction.0" ) ); //$NON-NLS-1$
		fMemoryViewer = viewer;
		CDebugImages.setLocalImageDescriptors( this, CDebugImages.IMG_LCL_MEMORY_CLEAR );
		setDescription( ActionMessages.getString( "ClearMemoryAction.1" ) ); //$NON-NLS-1$
		setToolTipText( ActionMessages.getString( "ClearMemoryAction.2" ) ); //$NON-NLS-1$
		WorkbenchHelp.setHelp( this, ICDebugHelpContextIds.MEMORY_CLEAR_ACTION );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update() {
		setEnabled( fMemoryViewer.canUpdate() );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		fMemoryViewer.clear();
	}
}
