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
package org.eclipse.cdt.debug.internal.ui.views.modules; 

import org.eclipse.cdt.debug.core.model.ICModule;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.internal.ui.viewers.provisional.AbstractModelProxy;
import org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.provisional.ModelDelta;
import org.eclipse.debug.internal.ui.viewers.update.DebugEventHandler;
 
/**
 * Comment for .
 */
public class ModulesViewEventHandler extends DebugEventHandler {

	/** 
	 * Constructor for ModulesViewEventHandler. 
	 */
	public ModulesViewEventHandler( AbstractModelProxy proxy ) {
		super( proxy );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.update.DebugEventHandler#handlesEvent(org.eclipse.debug.core.DebugEvent)
	 */
	protected boolean handlesEvent( DebugEvent event ) {
		if ( event.getKind() == DebugEvent.CREATE || 
			 event.getKind() == DebugEvent.TERMINATE || 
			 event.getKind() == DebugEvent.CHANGE )
			return true;
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.update.DebugEventHandler#handleChange(org.eclipse.debug.core.DebugEvent)
	 */
	protected void handleChange( DebugEvent event ) {
		if ( event.getSource() instanceof ICModule )
			fireDelta( new ModelDelta( event.getSource(), IModelDelta.STATE ) );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.update.DebugEventHandler#handleCreate(org.eclipse.debug.core.DebugEvent)
	 */
	protected void handleCreate( DebugEvent event ) {
		if ( event.getSource() instanceof IDebugTarget ) {
			refreshRoot( event );
		}
		else if ( event.getSource() instanceof ICModule ) {
			fireDelta( new ModelDelta( event.getSource(), IModelDelta.ADDED ) );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.update.DebugEventHandler#handleTerminate(org.eclipse.debug.core.DebugEvent)
	 */
	protected void handleTerminate( DebugEvent event ) {
		if ( event.getSource() instanceof IDebugTarget ) {
			refreshRoot( event );
		}
		else if ( event.getSource() instanceof ICModule ) {
			fireDelta( new ModelDelta( event.getSource(), IModelDelta.REMOVED ) );
		}
	}
}
