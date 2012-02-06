/*******************************************************************************
 * Copyright (c) 2011 Anton Gorenkov 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Gorenkov - initial API and implementation (Bug 210366)
 *     Marc Khouzam (Ericsson) - Add support to create the gdb process as well (Bug 210366)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.launching;

import java.util.Map;

import org.eclipse.cdt.dsf.gdb.IGdbDebugConstants;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.IProcessFactory;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.RuntimeProcess;

/**
 * Default GDB Process Factory creation of launch processes
 * using DebugPlugin.newProcess()
 * @since 4.1
 */
public class GdbProcessFactory implements IProcessFactory {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public IProcess newProcess(ILaunch launch, Process process, String label, Map attributes) {
		if (attributes != null) {
			if (IGdbDebugConstants.GDB_PROCESS_CREATION_VALUE.equals(attributes.get(IGdbDebugConstants.PROCESS_TYPE_CREATION_ATTR))) {
				return new GDBProcess(launch, process, label, attributes);
			}

			if (IGdbDebugConstants.INFERIOR_PROCESS_CREATION_VALUE.equals(attributes.get(IGdbDebugConstants.PROCESS_TYPE_CREATION_ATTR))) {
				return new InferiorRuntimeProcess(launch, process, label, attributes);
			}
		}	
		
		return new RuntimeProcess(launch, process, label, attributes);
	}
}
