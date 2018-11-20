/*******************************************************************************
 * Copyright (c) 2014, 2015 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.service.DsfSession;

/**
 * Adapt to GDB 7.4 where breakpoints are for all inferiors at once.
 *
 * @since 4.4
 */
public class GDBProcesses_7_4 extends GDBProcesses_7_3 {

	public GDBProcesses_7_4(DsfSession session) {
		super(session);
	}

	/**
	 * A container context that is not an IBreakpointsTargetDMContext.
	 */
	private static class GDBContainerDMC_7_4 extends MIContainerDMC implements IMemoryDMContext {
		public GDBContainerDMC_7_4(String sessionId, IProcessDMContext processDmc, String groupId) {
			super(sessionId, processDmc, groupId);
		}
	}

	@Override
	public IMIContainerDMContext createContainerContext(IProcessDMContext processDmc, String groupId) {
		return new GDBContainerDMC_7_4(getSession().getId(), processDmc, groupId);
	}

}
