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

import org.eclipse.cdt.debug.internal.ui.views.memory.MemoryViewer;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * The "Number Of Memory View Coulumns" action.
 */
public class MemoryNumberOfColumnAction extends Action implements IUpdate {

	private MemoryActionSelectionGroup fGroup;

	private MemoryViewer fMemoryViewer;

	private int fNumberOfColumns = 0;

	/**
	 * Constructor for MemoryNumberOfColumnAction.
	 */
	public MemoryNumberOfColumnAction( MemoryActionSelectionGroup group, MemoryViewer viewer, int numberOfColumns ) {
		super( getLabel( numberOfColumns ), IAction.AS_CHECK_BOX );
		fGroup = group;
		fMemoryViewer = viewer;
		fNumberOfColumns = numberOfColumns;
	}

	private static String getLabel( int numberOfColumns ) {
		return CDebugUIPlugin.getFormattedString( ActionMessages.getString( "MemoryNumberOfColumnAction.0" ), new Integer( numberOfColumns ) ); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update() {
		setEnabled( fMemoryViewer.canUpdate() );
		setChecked( fMemoryViewer.getCurrentNumberOfColumns() == fNumberOfColumns );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		try {
			fMemoryViewer.setNumberOfColumns( fNumberOfColumns );
			fGroup.setCurrentSelection( this );
		}
		catch( DebugException e ) {
			CDebugUIPlugin.errorDialog( ActionMessages.getString( "MemoryNumberOfColumnAction.1" ), e.getStatus() ); //$NON-NLS-1$
			setChecked( false );
		}
	}

	public String getActionId() {
		return "MemoryNumberOfColumns" + fNumberOfColumns; //$NON-NLS-1$
	}
}
