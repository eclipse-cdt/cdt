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
 * The "Memory Unit Size" action.
 */
public class MemorySizeAction extends Action implements IUpdate {

	private MemoryActionSelectionGroup fGroup;

	private MemoryViewer fMemoryViewer;

	private int fId = 0;

	/**
	 * Constructor for MemorySizeAction.
	 */
	public MemorySizeAction( MemoryActionSelectionGroup group, MemoryViewer viewer, int id ) {
		super( getLabel( id ), IAction.AS_CHECK_BOX );
		fGroup = group;
		fMemoryViewer = viewer;
		fId = id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update() {
		setEnabled( fMemoryViewer.canUpdate() );
		setChecked( fMemoryViewer.getCurrentWordSize() == fId );
	}

	private static String getLabel( int id ) {
		String label = ""; //$NON-NLS-1$
		switch( id ) {
			case IFormattedMemoryBlock.MEMORY_SIZE_BYTE:
			case IFormattedMemoryBlock.MEMORY_SIZE_HALF_WORD:
			case IFormattedMemoryBlock.MEMORY_SIZE_WORD:
			case IFormattedMemoryBlock.MEMORY_SIZE_DOUBLE_WORD:
				// Examples of the display for the following value are "1 byte" and "8 bytes".
				// Normally placeholders in {} are not translated, except when they are choice forms,
				// where the strings after each "#" are to be translated. 
				label = CDebugUIPlugin.getFormattedString( ActionMessages.getString( "MemorySizeAction.0" ), new Integer( id ) ); //$NON-NLS-1$
				break;
		}
		return label;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		try {
			fMemoryViewer.setWordSize( fId );
			fGroup.setCurrentSelection( this );
		}
		catch( DebugException e ) {
			CDebugUIPlugin.errorDialog( ActionMessages.getString( "MemorySizeAction.1" ), e.getStatus() ); //$NON-NLS-1$
			setChecked( false );
		}
	}

	public String getActionId() {
		return "MemorySize" + fId; //$NON-NLS-1$
	}
}
