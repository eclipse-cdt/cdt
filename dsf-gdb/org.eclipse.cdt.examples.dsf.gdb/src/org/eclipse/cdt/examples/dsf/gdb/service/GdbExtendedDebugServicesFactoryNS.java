/*******************************************************************************
 * Copyright (c) 2008, 2011 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.gdb.service;

import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.gdb.service.GDBRunControl_7_0_NS;
import org.eclipse.cdt.dsf.gdb.service.GDBRunControl_7_2_NS;
import org.eclipse.cdt.dsf.service.DsfSession;

/**
 * This variant is for non-stop (NS) multi-threaded debugging, a gdb capability
 * introduced in version 7.0. We provide a specialized NS implementation of
 * the run control service; that's the only specialization.
 */
public class GdbExtendedDebugServicesFactoryNS extends GdbExtendedDebugServicesFactory {

	public GdbExtendedDebugServicesFactoryNS(String version) {
		super(version);
	}
	
	@Override
	protected IRunControl createRunControlService(DsfSession session) {
		if (GDB_7_2_VERSION.compareTo(getVersion()) <= 0) {
			return new GDBRunControl_7_2_NS(session);
		}
		return new GDBRunControl_7_0_NS(session);
	}
}
