/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.ICDIEndSteppingRange;

/**
 */
public class EndSteppingRange extends SessionObject implements ICDIEndSteppingRange  {

	public EndSteppingRange(Session session) {
		super(session);
	}
}
