/**********************************************************************
 * Copyright (c) 2002, 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/
package org.eclipse.cdt.debug.internal.core.model;

import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIResumedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor;
import org.eclipse.cdt.debug.core.model.ICGlobalVariable;
import org.eclipse.cdt.debug.core.model.IGlobalVariableDescriptor;

/**
 * Represents a global variable.
 */
public class CGlobalVariable extends CVariable implements ICGlobalVariable {

	private IGlobalVariableDescriptor fDescriptor;

	/**
	 * Constructor for CGlobalVariable.
	 */
	protected CGlobalVariable( CDebugElement parent, IGlobalVariableDescriptor descriptor, ICDIVariableDescriptor cdiVariableObject ) {
		super( parent, cdiVariableObject );
		fDescriptor = descriptor;
	}

	/**
	 * Constructor for CGlobalVariable.
	 */
	protected CGlobalVariable( CDebugElement parent, IGlobalVariableDescriptor descriptor, ICDIVariableDescriptor cdiVariableObject, String message ) {
		super( parent, cdiVariableObject, message );
		fDescriptor = descriptor;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICVariable#canEnableDisable()
	 */
	public boolean canEnableDisable() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener#handleDebugEvents(org.eclipse.cdt.debug.core.cdi.event.ICDIEvent[])
	 */
	public void handleDebugEvents( ICDIEvent[] events ) {
		for( int i = 0; i < events.length; i++ ) {
			ICDIEvent event = events[i];
			if ( event instanceof ICDIResumedEvent ) {
				ICDIObject source = event.getSource();
				if ( source != null ) {
					ICDITarget cdiTarget = source.getTarget();
					if (  getCDITarget().equals( cdiTarget ) ) {
						setChanged( false );
					}
				}
			}
		}
		super.handleDebugEvents( events );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICGlobalVariable#getDescriptor()
	 */
	public IGlobalVariableDescriptor getDescriptor() {
		return fDescriptor;
	}
}