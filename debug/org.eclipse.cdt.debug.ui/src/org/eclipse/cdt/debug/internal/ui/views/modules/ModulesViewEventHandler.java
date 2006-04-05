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
import org.eclipse.cdt.debug.core.model.IModuleRetrieval;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.internal.ui.viewers.provisional.AbstractModelProxy;
import org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.provisional.ModelDelta;
import org.eclipse.debug.internal.ui.viewers.update.DebugEventHandler;
 
/**
 * Comment for .
 */
public class ModulesViewEventHandler extends DebugEventHandler {

	private IModuleRetrieval fModuleRetrieval;

	/** 
	 * Constructor for ModulesViewEventHandler. 
	 */
	public ModulesViewEventHandler( AbstractModelProxy proxy, IModuleRetrieval moduleRetrieval ) {
		super( proxy );
		fModuleRetrieval = moduleRetrieval;
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
			fireDelta( (ICModule)event.getSource(), IModelDelta.STATE );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.update.DebugEventHandler#handleCreate(org.eclipse.debug.core.DebugEvent)
	 */
	protected void handleCreate( DebugEvent event ) {
		if ( event.getSource() instanceof IDebugTarget ) {
			refreshRoot( event );
		}
		else if ( event.getSource() instanceof ICModule ) {
			fireDelta( (ICModule)event.getSource(), IModelDelta.ADDED );
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
			fireDelta( (ICModule)event.getSource(), IModelDelta.REMOVED );
		}
	}

	private void fireDelta( ICModule module, int flags ) {
		ModelDelta root = new ModelDelta( fModuleRetrieval, IModelDelta.NO_CHANGE );
		root.addNode( module, flags );
		fireDelta( root );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.update.DebugEventHandler#dispose()
	 */
	public synchronized void dispose() {
		super.dispose();
		fModuleRetrieval = null;
	}
}
