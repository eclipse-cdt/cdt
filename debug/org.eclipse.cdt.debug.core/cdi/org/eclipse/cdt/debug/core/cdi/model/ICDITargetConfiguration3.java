/*******************************************************************************
 * Copyright (c) 2007 Freescale Semiconductor and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Freescale Semiconductor - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;

public interface ICDITargetConfiguration3 extends ICDITargetConfiguration2 {

	/**
	 * It may be desirable to update all variables when a certain event occurs.
	 * For example, some CDI clients may want all variabless updated when memory
	 * is changed (when an ICDIMemoryChangedEvent is received) because it's
	 * impractical or impossible for those backends to determine what specific
	 * variables the memory change affected.
	 * 
	 * CDT will call this method to determine desired behavior for a limited set
	 * of event types. The CDI backend should not expect to use this hook as a
	 * general control mechanism for when variables are updated.
	 * 
	 * @return Whether the value for all active variables should be invalidated
	 *         and re-fetched from the CDI backend on the occurence of the given
	 *         event
	 */
	boolean needsVariablesUpdated(ICDIEvent event);
	
	/**
	 * Same idea as needsRegistersUpdated() but for registers. Embedded systems
	 * often have memory mapped registers; changing bytes in memory might, in
	 * effect, change a register value
	 * 
	 * @return Whether the value for all active registers should be invalidated
	 *         and re-fetched from the CDI backend on the occurence of the given
	 *         event
	 */
	boolean needsRegistersUpdated(ICDIEvent event);
}
