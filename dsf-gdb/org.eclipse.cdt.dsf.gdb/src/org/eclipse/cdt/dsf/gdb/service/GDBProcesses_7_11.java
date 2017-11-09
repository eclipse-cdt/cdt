/*******************************************************************************
 * Copyright (c) 2017 Kichwa Coders and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Kichwa Coders - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import org.eclipse.cdt.dsf.mi.service.IMIRunControl;
import org.eclipse.cdt.dsf.mi.service.IMIRunControl.MIRunMode;
import org.eclipse.cdt.dsf.service.DsfSession;

/**
 * @since 5.4
 */
public class GDBProcesses_7_11 extends GDBProcesses_7_10 {

	public GDBProcesses_7_11(DsfSession session) {
		super(session);
	}

	@Override
	protected boolean targetAttachRequiresTrailingNewline() {
		IMIRunControl runControl = getServicesTracker().getService(IMIRunControl.class);
		if (runControl != null && runControl.getRunMode() == MIRunMode.NON_STOP) {
			return false;
		}
		return true;
	}
}
