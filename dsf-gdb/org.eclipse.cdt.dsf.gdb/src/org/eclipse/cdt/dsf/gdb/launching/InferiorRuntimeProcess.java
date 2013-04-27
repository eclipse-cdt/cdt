/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	Ericsson - Initial API and implementation
 *  Marc Khouzam (Ericsson) - Display exit code in process console (Bug 402054)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.launching;

import java.util.Map;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.RuntimeProcess;

/**
 * A process for the inferior to know it belongs to a DSF-GDB session
 * 
 * @since 4.0
 */
public class InferiorRuntimeProcess extends RuntimeProcess {

	private String fLabel;
	
	public InferiorRuntimeProcess(ILaunch launch, Process process, String name,
			Map<String, String> attributes) {
		super(launch, process, name, attributes);
		fLabel = name;
	}

	@Override
	public String getLabel() {
		return fLabel;
	}
	
	/**
	 * Allows to update the label of this process.
	 * @since 4.2
	 */
	public void setLabel(String label) {
		fLabel = label;
	}
}
