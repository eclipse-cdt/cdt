/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.views.disassembly;

import org.eclipse.cdt.debug.core.model.IDisassembly;
import org.eclipse.cdt.debug.internal.ui.views.AbstractDebugEventHandler;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.ui.AbstractDebugView;


/**
 * Updates the disassembly view.
 */
public class DisassemblyViewEventHandler extends AbstractDebugEventHandler {

	/**
	 * Constructor for DisassemblyViewEventHandler.
	 * 
	 * @param view
	 */
	public DisassemblyViewEventHandler( AbstractDebugView view ) {
		super( view );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.views.AbstractDebugEventHandler#doHandleDebugEvents(org.eclipse.debug.core.DebugEvent[])
	 */
	protected void doHandleDebugEvents( DebugEvent[] events ) {
		for ( int i = 0; i < events.length; i++ ) {
			DebugEvent event = events[i];
			switch( event.getKind() ) {
				case DebugEvent.TERMINATE:
					handleTerminateEvent( events[i] );
					break;
				case DebugEvent.SUSPEND:
					handleSuspendEvent( events[i] );
					break;
				case DebugEvent.RESUME:
					handleResumeEvent( events[i] );
					break;
				case DebugEvent.CHANGE:
					handleChangeEvent( events[i] );
					break;
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.views.AbstractDebugEventHandler#refresh()
	 */
	public void refresh() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.views.AbstractDebugEventHandler#refresh(java.lang.Object)
	 */
	protected void refresh( Object element ) {
	}

	private void handleTerminateEvent( DebugEvent event ) {
	}

	private void handleResumeEvent( DebugEvent event ) {
	}

	private void handleSuspendEvent( DebugEvent event ) {
	}

	private void handleChangeEvent( DebugEvent event ) {
		if ( event.getSource() instanceof IDisassembly )
			getDisassemblyView().refresh( (IDisassembly)event.getSource() );
	}

	protected DisassemblyView getDisassemblyView() {
		return (DisassemblyView)getView();
	}
}
