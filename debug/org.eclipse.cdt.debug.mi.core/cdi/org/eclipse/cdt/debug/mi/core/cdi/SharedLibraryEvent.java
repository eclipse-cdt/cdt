/*
 * 
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.ICDISharedLibraryEvent;

/**
 *
 */
public class SharedLibraryEvent extends SessionObject implements ICDISharedLibraryEvent {
	
	public SharedLibraryEvent(Session session) {
		super(session);
	}

}
