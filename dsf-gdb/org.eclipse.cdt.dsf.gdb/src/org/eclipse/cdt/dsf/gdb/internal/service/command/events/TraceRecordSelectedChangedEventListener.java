/*******************************************************************************
 * Copyright (c) 2011 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.internal.service.command.events;

import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceRecordSelectedChangedDMEvent;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;

/**
 * Temporary class within an internal package to avoid adding a new API.
 * The class is an event listener for the ITraceRecordSelectedChangedDMEvent.
 */
public class TraceRecordSelectedChangedEventListener {

	public boolean fTracepointVisualizationEnabled = false;	
	
	@DsfServiceEventHandler
	public void eventDispatched(ITraceRecordSelectedChangedDMEvent e) {
		fTracepointVisualizationEnabled = e.isVisualizationModeEnabled();
	}
}