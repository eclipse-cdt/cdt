/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.core.cdi.event;

/**
 * 
 * An event listener registers with the event manager to receive event 
 * notification from the CDI model objects.
 * 
 * @since Jul 10, 2002
 */
public interface ICDIEventListener {
	/**
	 * Notifies this listener of the given event.
	 * 
	 * @param event - the event
	 */
	void handleDebugEvents(ICDIEvent[] event);
}
