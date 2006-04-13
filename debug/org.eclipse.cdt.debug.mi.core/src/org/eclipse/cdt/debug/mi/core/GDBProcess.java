/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core; 

import java.util.Map;
import org.eclipse.cdt.debug.mi.core.cdi.model.Target;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.RuntimeProcess;
 
public class GDBProcess extends RuntimeProcess {

	private Target fTarget;

	public GDBProcess( Target target, ILaunch launch, Process process, String name, Map attributes ) {
		super( launch, process, name, attributes );
		fTarget = target;
	}

	public Target getTarget() {
		return fTarget;
	}
}
