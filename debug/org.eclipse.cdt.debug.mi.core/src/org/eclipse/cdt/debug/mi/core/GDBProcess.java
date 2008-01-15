/*******************************************************************************
 * Copyright (c) 2004, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Alena Laskavaia (QNX) - Fix for 186172
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core;

import java.util.Map;
import org.eclipse.cdt.debug.mi.core.cdi.model.Target;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.RuntimeProcess;
 
public class GDBProcess extends RuntimeProcess {

	// volatile because the field may be accessed concurrently during construction
	private volatile Target fTarget;

	public GDBProcess(Target target, ILaunch launch, Process process, String name, Map attributes) {
		super( launch, process, name, attributes );
		fTarget = target;
		fireChangeEvent();
	}

	public Target getTarget() {
		return fTarget;
	}
}
