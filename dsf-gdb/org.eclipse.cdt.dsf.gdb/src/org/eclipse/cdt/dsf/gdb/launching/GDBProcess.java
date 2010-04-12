/*******************************************************************************
 * Copyright (c) 2010 Marc-Andre Laperle and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.launching;

import java.util.Map;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.RuntimeProcess;

/**
 * A process for the gdb backend to differentiate it from the inferior
 * 
 * @since 3.0
 */
public class GDBProcess extends RuntimeProcess {

	public GDBProcess(ILaunch launch, Process process, String name,
			Map<String, String> attributes) {
		super(launch, process, name, attributes);
	}

}
