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

import org.eclipse.cdt.debug.core.model.IFormattedMemoryBlock;
import org.eclipse.cdt.debug.internal.ui.views.memory.MemoryViewer;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * The "Format" action of the Memory view.
 */
public class MemoryFormatAction extends Action implements IUpdate {

	private MemoryActionSelectionGroup fGroup;

	private MemoryViewer fMemoryViewer;

	private int fFormat = 0;

	/**
	 * Constructor for MemoryFormatAction.
	 */
	public MemoryFormatAction( MemoryActionSelectionGroup group, MemoryViewer viewer, int format ) {
		super( getLabel( format ), IAction.AS_CHECK_BOX );
		fGroup = group;
		fMemoryViewer = viewer;
		fFormat = format;
	}

	/**
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update() {
		setEnabled( fMemoryViewer.canChangeFormat( fFormat ) );
		setChecked( fMemoryViewer.getCurrentFormat() == fFormat );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		try {
			fMemoryViewer.setFormat( fFormat );
			fGroup.setCurrentSelection( this );
		}
		catch( DebugException e ) {
			CDebugUIPlugin.errorDialog( ActionMessages.getString( "MemoryFormatAction.0" ), e.getStatus() ); //$NON-NLS-1$
			setChecked( false );
		}
	}

	private static String getLabel( int id ) {
		String label = ""; //$NON-NLS-1$
		switch( id ) {
			case (IFormattedMemoryBlock.MEMORY_FORMAT_HEX ):
				label = ActionMessages.getString( "MemoryFormatAction.1" ); //$NON-NLS-1$
				break;
			case (IFormattedMemoryBlock.MEMORY_FORMAT_SIGNED_DECIMAL ):
				label = ActionMessages.getString( "MemoryFormatAction.2" ); //$NON-NLS-1$
				break;
			case (IFormattedMemoryBlock.MEMORY_FORMAT_UNSIGNED_DECIMAL ):
				label = ActionMessages.getString( "MemoryFormatAction.3" ); //$NON-NLS-1$
				break;
		}
		return label;
	}

	public String getActionId() {
		return "MemoryFormat" + fFormat; //$NON-NLS-1$
	}
}
