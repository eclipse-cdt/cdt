/*******************************************************************************
 * Copyright (c) 2014, 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marc Khouzam (Ericsson) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.gdb.service;

import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.gdb.service.GDBRunControl_7_0_NS;
import org.eclipse.cdt.dsf.gdb.service.GDBRunControl_7_2_NS;
import org.eclipse.cdt.dsf.service.DsfSession;

public class GdbExtendedDebugServicesFactoryNS extends GdbExtendedDebugServicesFactory {

	public GdbExtendedDebugServicesFactoryNS(String version) {
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
