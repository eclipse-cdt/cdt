/*******************************************************************************
 * Copyright (c) 2008, 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.service.DsfSession;

/**
 * This variant is for non-stop (NS) multi-threaded debugging, a gdb capability
 * introduced in version 7.0. We provide a specialized NS implementation of
 * the run control service; that's the only specialization.
 * 
 * @deprecated Replaced with GdbDebugServicesFactory and its constructor
 * GdbDebugServicesFactory(String, ILaunchConfiguration)
 */
@Deprecated
public class GdbDebugServicesFactoryNS extends GdbDebugServicesFactory {

	public GdbDebugServicesFactoryNS(String version) {
		super(version);
	}
	
	@Override
	protected IRunControl createRunControlService(DsfSession session) {
		if (compareVersionWith(GDB_7_2_VERSION) >= 0) {
			return new GDBRunControl_7_2_NS(session);
		}
		return new GDBRunControl_7_0_NS(session);
	}
}
