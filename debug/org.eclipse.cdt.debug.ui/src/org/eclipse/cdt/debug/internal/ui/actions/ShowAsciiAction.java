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
 * Enter type comment.
 * 
 * @since: Oct 21, 2002
 */
public class ShowAsciiAction extends SelectionProviderAction implements IUpdate {

	private MemoryViewer fMemoryViewer;

	/**
	 * Constructor for ShowAsciiAction.
	 * 
	 * @param provider
	 * @param text
	 */
	public ShowAsciiAction( MemoryViewer viewer ) {
		super( viewer, ActionMessages.getString( "ShowAsciiAction.0" ) ); //$NON-NLS-1$
		fMemoryViewer = viewer;
		CDebugImages.setLocalImageDescriptors( this, CDebugImages.IMG_LCL_SHOW_ASCII );
		setDescription( ActionMessages.getString( "ShowAsciiAction.1" ) ); //$NON-NLS-1$
		setToolTipText( ActionMessages.getString( "ShowAsciiAction.2" ) ); //$NON-NLS-1$
		WorkbenchHelp.setHelp( this, ICDebugHelpContextIds.MEMORY_SHOW_ASCII_ACTION );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update() {
		setEnabled( fMemoryViewer.canShowAscii() );
		setChecked( fMemoryViewer.showAscii() );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		fMemoryViewer.setShowAscii( isChecked() );
	}
}