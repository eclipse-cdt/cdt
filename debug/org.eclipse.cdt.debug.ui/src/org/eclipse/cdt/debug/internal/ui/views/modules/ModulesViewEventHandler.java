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
package org.eclipse.cdt.debug.internal.ui.views.modules; 

import org.eclipse.cdt.debug.core.model.ICModule;
import org.eclipse.cdt.debug.internal.ui.views.AbstractDebugEventHandler;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.ui.AbstractDebugView;
 
/**
 * Updates the Modules view.
 */
public class ModulesViewEventHandler extends AbstractDebugEventHandler {

	/** 
	 * Constructor for ModulesViewEventHandler. 
	 */
	public ModulesViewEventHandler( AbstractDebugView view ) {
		super( view );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.views.AbstractDebugEventHandler#doHandleDebugEvents(org.eclipse.debug.core.DebugEvent[])
	 */
	protected void doHandleDebugEvents( DebugEvent[] events ) {
		for ( int i = 0; i < events.length; i++ ) {
			DebugEvent event = events[i];
			switch( event.getKind() ) {
				case DebugEvent.CREATE:
				case DebugEvent.TERMINATE:
					if ( event.getSource() instanceof IDebugTarget || event.getSource() instanceof ICModule )
						refresh();
					break;
				case DebugEvent.CHANGE :
					if ( event.getSource() instanceof ICModule )
						refresh( event.getSource() );
					break;
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.views.AbstractDebugEventHandler#refresh()
	 */
	public void refresh() {
		if ( isAvailable() ) {
			getView().showViewer();
			getTreeViewer().refresh();
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.views.AbstractDebugEventHandler#refresh(java.lang.Object)
	 */
	protected void refresh( Object element ) {
		if ( isAvailable() ) {
			getView().showViewer();
			getTreeViewer().refresh( element );
			getTreeViewer().setSelection( getTreeViewer().getSelection() );
		}
	}
}
