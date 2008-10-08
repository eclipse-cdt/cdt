/*******************************************************************************
 * Copyright (c) 2008 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.gdb.internal.provisional.service;

import org.eclipse.dd.dsf.debug.service.IRunControl;
import org.eclipse.dd.dsf.service.DsfSession;

public class GdbDebugServicesFactoryNS extends GdbDebugServicesFactory {

	public GdbDebugServicesFactoryNS(String version) {
		super(version);
	}
	
	@Override
	protected IRunControl createRunControlService(DsfSession session) {
		return new GDBRunControl_7_0_NS(session);
	}
}
