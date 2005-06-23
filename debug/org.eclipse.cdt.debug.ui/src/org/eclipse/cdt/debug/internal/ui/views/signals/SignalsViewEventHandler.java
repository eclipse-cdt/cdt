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

package org.eclipse.cdt.debug.internal.ui.views.signals;

import org.eclipse.cdt.debug.core.model.ICSignal;
import org.eclipse.cdt.debug.internal.ui.views.AbstractDebugEventHandler;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.ui.AbstractDebugView;


/**
 * Updates the signals view.
 *
 * @since: Mar 8, 2004
 */
public class SignalsViewEventHandler extends AbstractDebugEventHandler {

	/**
	 * Constructs a new event handler on the given view
	 * 
	 * @param view signals view
	 */
	public SignalsViewEventHandler( AbstractDebugView view ) {
		super( view );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.views.AbstractDebugEventHandler#doHandleDebugEvents(org.eclipse.debug.core.DebugEvent[])
	 */
	protected void doHandleDebugEvents( DebugEvent[] events ) {
		for( int i = 0; i < events.length; i++ ) {
			DebugEvent event = events[i];
			switch( event.getKind() ) {
				case DebugEvent.CREATE:
				case DebugEvent.TERMINATE:
					if ( event.getSource() instanceof IDebugTarget || event.getSource() instanceof ICSignal )
						refresh();
					break;
				case DebugEvent.SUSPEND:
					refresh();
					break;
				case DebugEvent.CHANGE:
					if ( event.getSource() instanceof ICSignal )
						refresh( event.getSource() );
					break;
			}
		}
	}
}
