/*******************************************************************************
 * Copyright (c) 2011, 2020 Anton Gorenkov, and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Anton Gorenkov - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.unittest.internal.launcher;

import java.util.Collections;
import java.util.Map;

import org.eclipse.cdt.dsf.gdb.IGdbDebugConstants;
import org.eclipse.cdt.dsf.gdb.launching.GDBProcess;
import org.eclipse.cdt.dsf.gdb.launching.InferiorRuntimeProcess;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.IProcessFactory;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.RuntimeProcess;

/**
 * Custom testing process factory allows to handle the output stream of the
 * testing process and prevent it from output to Console.
 */
public class TestingProcessFactory implements IProcessFactory {

	@Override
	public IProcess newProcess(ILaunch launch, Process process, String label, Map<String, String> attributes) {
			// Mimic the behavior of DSF GDBProcessFactory.
		if (attributes != null) {
			Object processTypeCreationAttrValue = attributes.get(IGdbDebugConstants.PROCESS_TYPE_CREATION_ATTR);
			if (IGdbDebugConstants.GDB_PROCESS_CREATION_VALUE.equals(processTypeCreationAttrValue)) {
				return new GDBProcess(launch, process, label, attributes);
			}

			if (IGdbDebugConstants.INFERIOR_PROCESS_CREATION_VALUE.equals(processTypeCreationAttrValue)) {
				return new InferiorRuntimeProcess(launch, process, label, attributes);
			}
			// Probably, it is CDI creating a new inferior process
		}
		return new RuntimeProcess(launch, process, label, Collections.emptyMap());
	}

}
